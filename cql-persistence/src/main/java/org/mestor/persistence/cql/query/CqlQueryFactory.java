/******************************************************************************************************/
/*                                                                                                    */
/*    Infinidat Ltd.  -  Proprietary and Confidential Material                                        */
/*                                                                                                    */
/*    Copyright (C) 2013, Infinidat Ltd. - All Rights Reserved                                        */
/*                                                                                                    */
/*    NOTICE: All information contained herein is, and remains the property of Infinidat Ltd.         */
/*    All information contained herein is protected by trade secret or copyright law.                 */
/*    The intellectual and technical concepts contained herein are proprietary to Infinidat Ltd.,     */
/*    and may be protected by U.S. and Foreign Patents, or patents in progress.                       */
/*                                                                                                    */
/*    Redistribution or use, in source or binary forms, with or without modification,                 */
/*    are strictly forbidden unless prior written permission is obtained from Infinidat Ltd.          */
/*                                                                                                    */
/*                                                                                                    */
/******************************************************************************************************/

package org.mestor.persistence.cql.query;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gte;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lte;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static org.mestor.persistence.cql.management.CommandHelper.quote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.persistence.cql.CqlPersistor;
import org.mestor.persistence.cql.CqlPersistorProperties;
import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.OrderByInfo;
import org.mestor.query.QueryInfo;
import org.mestor.util.Pair;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

public class CqlQueryFactory {
	private final static Pattern countPattern = Pattern.compile("count\\(.*\\)", Pattern.CASE_INSENSITIVE);
	private final static Pattern subqueryPattern = Pattern.compile("(\\w+)\\s*(?:=|<|>|=|<=|>=|\\s+IN\\s+|\\s+LIKE\\s+)\\s*\\(?\\s*subquery\\((\\d+)\\)\\s*\\)?", Pattern.CASE_INSENSITIVE);
	private final EntityContext context;

	private final String partitionFieldName;
	private final Object partitionFieldValue;


	public CqlQueryFactory(final EntityContext context) {
		this.context = context;

		final Map<String, Object> properties = context.getProperties();
		final Object[] partitionData = CqlPersistorProperties.PARTITION_KEY.getValue(properties);
		partitionFieldName = (String)partitionData[0];
		partitionFieldValue = partitionData[1];
	}


	public Collection<CompiledQuery> createQuery(final QueryInfo query,
			final Map<String, Object> parameterValues) {
		final List<CompiledQuery> res = new ArrayList<CompiledQuery>();
		createQuery(query, res, parameterValues);
		return res;
	}



	private void createQuery(final QueryInfo query, final Collection<CompiledQuery> qls,
			final Map<String, Object> parameterValues) {
		final ClauseInfo where = query.getWhere();
		final String entityName = getSingleFrom(query);
		final EntityMetadata<?> emd = context.getEntityMetadata(entityName);
		final String tableName = emd.getTableName();
		final String keyspace = emd.getSchemaName();
		final Statement statement;
		Class<?> resultType = emd.getEntityType();
		switch(query.getType()) {
			case DELETE: {
				final Delete.Selection delete = delete();
				final Map<String, Object> what = query.getWhat();
				if (what != null) {
					for (final Entry<String, Object> w : what.entrySet()) {
						if (w.getValue() != null) {
							throw new IllegalArgumentException();
						}
						delete.column(getColumnName(emd, w.getKey(), false));
					}
				}
				final Delete.Where cqlWhere = delete.from(quote(keyspace), quote(tableName)).where();


				final Collection<Clause> clauses = createClause(emd, where, qls, parameterValues);
				if (clauses != null) {
					for (final Clause clause : clauses) {
						cqlWhere.and(clause);
					}
				}


				statement = cqlWhere;
				break;
			}
 			case INSERT: {
 				final Insert insert = insertInto(quote(keyspace), quote(tableName));
 				for (final Entry<String, Object> v : query.getWhat().entrySet()) {
 					insert.value(getColumnName(emd, v.getKey(), false), v.getValue());
 				}
				statement = insert;
				break;
 			}
			case SELECT: {
				final Select.Selection selection = QueryBuilder.select();
				final Map<String, Object> fields = query.getWhat();

				if (fields != null) {
					// special case for count(*)
					final boolean isCount = isCount(fields);
					if (isCount) {
						selection.column("count(*)");
						resultType = Long.class;
					} else{
						for (final String field : fields.keySet()) {
							selection.column(getColumnName(emd, field, false));
						}
					}
				} else {
					selection.all();
				}


				final Select select = selection.from(quote(keyspace), quote(tableName));
				final Integer limit = query.getLimit();
				if (limit != null) {
					select.limit(limit);
				}


				final Select.Where selectWhere = select.where();

				final Collection<Ordering> orderings = new ArrayList<>();
				final Collection<OrderByInfo> orders = query.getOrders();
				if (orders != null) {
					for (final OrderByInfo order : orders) {
						final String field = getKeyColumnName(emd, order.getField());
						switch(order.getOrder()) {
							case ASC:
								orderings.add(QueryBuilder.asc(field));
								break;
							case DSC:
								orderings.add(QueryBuilder.desc(field));
								break;
							default:
								throw new IllegalArgumentException(String.valueOf(order.getOrder()));
						}
					}
				}

				if (!orderings.isEmpty()) {
					select.orderBy(orderings.toArray(new Ordering[0]));
				}

				final AtomicBoolean addPartitionClause = new AtomicBoolean(!orderings.isEmpty());
				final Collection<Clause> clauses = createClause(emd, where, qls, parameterValues, !orderings.isEmpty(), addPartitionClause);

				if (addPartitionClause.get()) {
					selectWhere.and(eq(partitionFieldName, partitionFieldValue));
				}

				if (clauses != null) {
					if (clauses.size() > 0) {
						// Add this just in case although it is not always needed.
						// ALLOW FILTERING is needed when filtering by more than one indexed field or by primary key when it is composite.
						// TODO: check whether this may cause performance problems. If yes implement code that adds ALLOW FILTERING only if needed.
						select.allowFiltering();
					}


					for (final Clause clause : clauses) {
						selectWhere.and(clause);
					}
				}


				statement = selectWhere;
				break;
			}
			case UPDATE: {
				final Update update = update(quote(keyspace), quote(tableName));
 				for (final Entry<String, Object> v : query.getWhat().entrySet()) {
 					update.with(set(getColumnName(emd, v.getKey(), false), v.getValue()));
 				}
				final Update.Where updateWhere = update.where();
				final Collection<Clause> clauses = createClause(emd, where, qls, parameterValues);
				if (clauses != null) {
					for (final Clause clause : clauses) {
						updateWhere.and(clause);
					}
				}
				statement = update;
				break;
			}
			default:
				throw new IllegalArgumentException(query.getType().name());

		}



		qls.add(new CompiledQuery(statement.getQueryString(), query, resultType));
	}


	private boolean isCount(final Map<String, Object> fields) {
		if (fields.size() == 1) {
			final Object firstValue = fields.values().iterator().next();
			if(firstValue instanceof String){
				return countPattern.matcher((String)firstValue).matches();
			}
		}
		return false;
	}


	String getSingleFrom(final QueryInfo query) {
		final Map<String, String> from = query.getFrom();
		if (from.size() != 1) {
			throw new IllegalArgumentException("Multiple \"from\" is not supported here");
		}

		// from1 defines entry that maps alias to real name of the table.
		// However if alias does not exist probably value is null
		final Entry<String, String> from1 = from.entrySet().iterator().next();
		final String v = from1.getValue();
		if (v != null) {
			return v;
		}

		return from1.getKey();
	}

	private Collection<Clause> createClause(final EntityMetadata<?> emd,
			final ClauseInfo where,
			final Collection<CompiledQuery> qls,
			final Map<String, Object> parameterValues) {

		return createClause(emd,
				where,
				qls,
				parameterValues,
				false,
				new AtomicBoolean(false));

	}

	private Collection<Clause> createClause(final EntityMetadata<?> emd,
			final ClauseInfo where,
			final Collection<CompiledQuery> qls,
			final Map<String, Object> parameterValues,
			final boolean useKeys,
			final AtomicBoolean addPartitionClause) {
		if (where == null) {
			return null;
		}
		Object expression = where.getExpression();

		if (expression instanceof ClauseInfo) {
			return createClause(emd, (ClauseInfo)expression, qls, parameterValues, useKeys, addPartitionClause);
		} else if (expression.getClass().isArray() && ClauseInfo.class.equals(expression.getClass().getComponentType())) {
			if (Operand.AND.equals(where.getOperator())) {
				final Collection<Clause> clauses = new ArrayList<>();
				for ( final Object expr : (Object[])expression) {
					clauses.addAll(createClause(emd, (ClauseInfo)expr, qls, parameterValues, useKeys, addPartitionClause));
				}
				return clauses;
			}
			throw new UnsupportedOperationException("Operator " + where.getOperator() + " is not supported now");
		} else if (expression instanceof QueryInfo) {
			createQuery((QueryInfo)expression, qls, parameterValues);
			expression = "subquery(" + (qls.size() - 1) + ")";
		}

		final String field = where.getField();
		final Object value;
		if(expression instanceof ArgumentInfo){
			final ArgumentInfo<?> ai = (ArgumentInfo<?>)expression;
			if(parameterValues != null && parameterValues.containsKey(field)){
				value = parameterValues.get(field);
			} else {
				throw new IllegalArgumentException("Parameter is not bound: " + ai.getName());
			}
		} else {
			value = expression;
		}

		final String column = getColumnName(emd, field, useKeys);
		final FieldMetadata<?, ?, ?> fmd = emd.getField(column);
		if (fmd.isKey()) {
			addPartitionClause.set(true);
		}


		final Operand op = where.getOperator();
		switch(op) {
			case EQ:
				return Collections.singleton(eq(column, value));
			case GE:
				return Collections.singleton(gte(column, value));
			case GT:
				return Collections.singleton(gt(column, value));
			case LE:
				return Collections.singleton(lte(column, value));
			case LT:
				return Collections.singleton(lt(column, value));
			case IN:
				if (value.getClass().isArray()) {
					return Collections.singleton(in(column, (Object[])value));
				} else if (value instanceof Collection) {
					return Collections.singleton(in(column, ((Collection<?>)value).toArray()));
				}
				return Collections.singleton(in(column, value));
			case LIKE:
				if (value instanceof String) {
					final String str = (String)value;
					if (!str.startsWith("%") && str.endsWith("%")) {
						return Collections.singleton(gte(column, value));
					} else if(!str.startsWith("%") && !str.endsWith("%")) {
						return Collections.singleton(eq(column, value));
					}
				}
			//$FALL-THROUGH$ - likes except "%..." and "...%" are not supported
			case NE:
			case NOT_LIKE:
			case NOT:
				throw new UnsupportedOperationException("Operation " + op + " is not supported by CQL");
			default:
				throw new IllegalArgumentException("Unknown argument " + op);
		}
	}


	public List<Pair<String, Integer>> getSubqueryIndexes(final String query) {
		final Matcher m = subqueryPattern.matcher(query);

		final List<Pair<String, Integer>> result = new ArrayList<>();
		//for (int start = 0; m.find(); start = m.end()) {
		while (m.find()) {
			result.add(new Pair<String, Integer>(m.group(1), Integer.parseInt(m.group(2))));
		}

		return result;
	}

	public String formatSubquery(final String query, final int index, final String subqueryValues) {
		return query.replaceAll("subquery\\(" + index + "\\)", subqueryValues);
	}


	private String getColumnName(final EntityMetadata<?> emd, final String fieldName, final boolean useKeys) {
		if (fieldName == null) {
			return null; // typical for select * from ...
		}

		final String name = useKeys ? CqlPersistor.primaryKeyName(fieldName) : fieldName;

		final FieldMetadata<?, ?, ?> fmd = emd.getFieldByName(name);
		if (fmd == null) {
			throw new IllegalArgumentException("Unknown field " + name);
		}

		final String column = fmd.getColumn();
		return column;
	}


	private String getKeyColumnName(final EntityMetadata<?> emd, final String fieldName) {
		if (fieldName == null) {
			return null; // typical for select * from ...
		}

		FieldMetadata<?, ?, ?> fmd = emd.getFieldByName(fieldName);
		if (fmd != null && fmd.isKey()) {
			return fmd.getColumn();
		}

		fmd = emd.getFieldByName(CqlPersistor.primaryKeyName(fieldName));
		if (fmd == null) {
			throw new IllegalArgumentException("Unknown field " + fieldName);
		}

		return fmd.getColumn();
	}

}

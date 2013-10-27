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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;
import org.mestor.util.Pair;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

public class CqlQueryFactory {
	private final static Pattern countPattern = Pattern.compile("count\\([*1]\\)", Pattern.CASE_INSENSITIVE);
	private final static Pattern subqueryPattern = Pattern.compile("(\\w+)\\s*(?:=|<|>|=|<=|>=|\\s+IN\\s+|\\s+LIKE\\s+)\\s*\\(?\\s*subquery\\((\\d+)\\)\\s*\\)?", Pattern.CASE_INSENSITIVE);
	private final EntityContext context;


	public CqlQueryFactory(final EntityContext context) {
		this.context = context;
	}


	public Collection<Pair<String, QueryInfo>> createQuery(final QueryInfo query) {
		return createQuery(query, new ArrayList<Pair<String, QueryInfo>>());
	}



	private Collection<Pair<String, QueryInfo>> createQuery(final QueryInfo query, final Collection<Pair<String, QueryInfo>> qls) {
		final ClauseInfo where = query.getWhere();
		final String entityName = getSingleFrom(query);
		final EntityMetadata<?> emd = context.getEntityMetadata(entityName);
		final String keyspace = emd.getSchemaName();
		Statement statement;
		switch(query.getType()) {
			case DELETE: {
				final Delete.Selection delete = delete();
				final Map<String, Object> what = query.getWhat();
				if (what != null) {
					for (final Entry<String, Object> w : what.entrySet()) {
						if (w.getValue() != null) {
							throw new IllegalArgumentException();
						}
						delete.column(getColumnName(emd, w.getKey()));
					}
				}
				final Delete.Where cqlWhere = delete.from(quote(keyspace), quote(getSingleFrom(query))).where();


				final Collection<Clause> clauses = createClause(emd, where, qls);
				if (clauses != null) {
					for (final Clause clause : clauses) {
						cqlWhere.and(clause);
					}
				}


				statement = cqlWhere;
				break;
			}
 			case INSERT: {
 				final Insert insert = insertInto(quote(keyspace), quote(getSingleFrom(query)));
 				for (final Entry<String, Object> v : query.getWhat().entrySet()) {
 					insert.value(getColumnName(emd, v.getKey()), v.getValue());
 				}
				statement = insert;
				break;
 			}
			case SELECT: {
				final Select.Selection selection = QueryBuilder.select();
				final Map<String, Object> fields = query.getWhat();
				if (fields != null) {
					if (fields.size() == 1 && countPattern.matcher(fields.keySet().iterator().next()).matches()) {
						// special case for count(*)
						selection.column(fields.keySet().iterator().next());
					} else {
						for (final String field : fields.keySet()) {
							selection.column(getColumnName(emd, field));
						}
					}
				} else {
					selection.all();
				}


				final Select select = selection.from(quote(keyspace), quote(getSingleFrom(query)));
				final Integer limit = query.getLimit();
				if (limit != null) {
					select.limit(limit);
				}


				final Select.Where selectWhere = select.where();

				final Collection<Clause> clauses = createClause(emd, where, qls);
				if (clauses != null) {
					for (final Clause clause : clauses) {
						selectWhere.and(clause);
					}
				}

				statement = selectWhere;
				break;
			}
			case UPDATE: {
				final Update update = update(quote(keyspace), quote(getSingleFrom(query)));
 				for (final Entry<String, Object> v : query.getWhat().entrySet()) {
 					update.with(set(getColumnName(emd, v.getKey()), v.getValue()));
 				}
				statement = update;

				final Update.Where updateWhere = update.where();
				final Collection<Clause> clauses = createClause(emd, where, qls);
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


		qls.add(new Pair<String, QueryInfo>(statement.getQueryString(), query));

		return qls;
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



	private Collection<Clause> createClause(final EntityMetadata<?> emd, final ClauseInfo where, final Collection<Pair<String, QueryInfo>> qls) {
		if (where == null) {
			return null;
		}
		Object expression = where.getExpression();

		if (expression instanceof ClauseInfo) {
			return createClause(emd, (ClauseInfo)expression, qls);
		} else if (expression.getClass().isArray() && ClauseInfo.class.equals(expression.getClass().getComponentType())) {
			if (Operand.AND.equals(where.getOperand())) {
				final Collection<Clause> clauses = new ArrayList<>();
				for ( final Object expr : (Object[])expression) {
					clauses.addAll(createClause(emd, (ClauseInfo)expr, qls));
				}
				return clauses;
			}
			throw new UnsupportedOperationException("Operator " + where.getOperand() + " is not supported now");
		} else if (expression instanceof QueryInfo) {
			createQuery((QueryInfo)expression, qls);
			expression = "subquery(" + (qls.size() - 1) + ")";
		}

		final String column = getColumnName(emd, where.getField());
		final Operand op = where.getOperand();
		switch(op) {
			case EQ:
				return Collections.singleton(eq(column, expression));
			case GE:
				return Collections.singleton(gte(column, expression));
			case GT:
				return Collections.singleton(gt(column, expression));
			case LE:
				return Collections.singleton(lte(column, expression));
			case LT:
				return Collections.singleton(lt(column, expression));
			case IN:
				if (expression.getClass().isArray()) {
					return Collections.singleton(in(column, (Object[])expression));
				} else if (expression instanceof Collection) {
					return Collections.singleton(in(column, ((Collection<?>)expression).toArray()));
				}
				return Collections.singleton(in(column, expression));
			case LIKE:
				if (expression instanceof String) {
					final String str = (String)expression;
					if (!str.startsWith("%") && str.endsWith("%")) {
						return Collections.singleton(gte(column, expression));
					} else if(!str.startsWith("%") && !str.endsWith("%")) {
						return Collections.singleton(eq(column, expression));
					}
				}
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


	private String getColumnName(final EntityMetadata<?> emd, final String fieldName) {
		if (fieldName == null) {
			return null; // typical for select * from ...
		}

		final FieldMetadata<?, ?, ?> fmd = emd.getFieldByName(fieldName);
		if (fmd == null) {
			throw new IllegalArgumentException("Unknown field " + fieldName);
		}

		final String column = fmd.getColumn();
		return column;
	}
}

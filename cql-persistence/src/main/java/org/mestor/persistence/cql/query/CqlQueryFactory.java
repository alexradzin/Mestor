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
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

public class CqlQueryFactory {
	private final static Pattern countPattern = Pattern.compile("count\\([*1]\\)");
	private final String keyspace;


	public CqlQueryFactory(final String keyspace) {
		this.keyspace = keyspace;
	}


	public Collection<String> createQuery(final QueryInfo query) {
		return createQuery(query, new ArrayList<String>());
	}



	private Collection<String> createQuery(final QueryInfo query, final Collection<String> qls) {
		final ClauseInfo where = query.getWhere();
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
						delete.column(quote(w.getKey()));
					}
				}
				final Delete.Where cqlWhere = delete.from(quote(keyspace), quote(getSingleFrom(query))).where();


				final Collection<Clause> clauses = createClause(where, qls);
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
 					insert.value(v.getKey(), v.getValue());
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
							selection.column(quote(field));
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

				final Collection<Clause> clauses = createClause(where, qls);
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
 					update.with(set(quote(v.getKey()), v.getValue()));
 				}
				statement = update;

				final Update.Where updateWhere = update.where();
				final Collection<Clause> clauses = createClause(where, qls);
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

		qls.add(statement.getQueryString());

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



	private Collection<Clause> createClause(final ClauseInfo where, final Collection<String> qls) {
		if (where == null) {
			return null;
		}
		Object expression = where.getExpression();

		if (expression instanceof ClauseInfo) {
			return createClause(where, qls);
		} else if (expression.getClass().isArray() && ClauseInfo.class.equals(expression.getClass().getComponentType())) {
			if (Operand.AND.equals(where.getOperand())) {
				final Collection<Clause> clauses = new ArrayList<>();
				for ( final Object expr : (Object[])expression) {
					clauses.addAll(createClause((ClauseInfo)expr, qls));
				}
				return clauses;
			}
			throw new UnsupportedOperationException("Operator " + where.getOperand() + " is not supported now");
		} else if (expression instanceof QueryInfo) {
			createQuery((QueryInfo)expression, qls);
			expression = "subquery(" + qls.size() + ")";
		}

		final String field = quote(where.getField());
		final Operand op = where.getOperand();
		switch(op) {
			case EQ:
				return Collections.singleton(eq(field, expression));
			case GE:
				return Collections.singleton(gte(field, expression));
			case GT:
				return Collections.singleton(gt(field, expression));
			case LE:
				return Collections.singleton(lte(field, expression));
			case LT:
				return Collections.singleton(lt(field, expression));
			case IN:
				if (expression.getClass().isArray()) {
					return Collections.singleton(in(field, (Object[])expression));
				} else if (expression instanceof Collection) {
					return Collections.singleton(in(field, ((Collection<?>)expression).toArray()));
				}
				return Collections.singleton(in(field, expression));
			case LIKE:
				if (expression instanceof String) {
					final String str = (String)expression;
					if (!str.startsWith("%") && str.endsWith("%")) {
						return Collections.singleton(gte(field, expression));
					} else if(!str.startsWith("%") && !str.endsWith("%")) {
						return Collections.singleton(eq(field, expression));
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
}

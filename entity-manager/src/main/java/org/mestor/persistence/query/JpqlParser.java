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

package org.mestor.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.CriteriaLanguageParser;
import org.mestor.query.OrderByInfo;
import org.mestor.query.OrderByInfo.Order;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;

import com.google.common.collect.ObjectArrays;

public class JpqlParser implements CriteriaLanguageParser {
	private final static Pattern selectPattern = Pattern.compile("^(SELECT)\\s+(.+)\\s+(FROM)\\s+(.*)", Pattern.CASE_INSENSITIVE);
	private final static Pattern updatePattern = Pattern.compile("^(UPDATE)\\s+(.+).*$", Pattern.CASE_INSENSITIVE); //TBD
	private final static Pattern deletePattern = Pattern.compile("^(DELETE)\\s+(FROM)\\s+(.*)", Pattern.CASE_INSENSITIVE); //TBD

	private final static Pattern joinPattern = Pattern.compile("^(.+)\\s+(JOIN)\\s+(.*)", Pattern.CASE_INSENSITIVE);

	private final static Pattern wherePattern = Pattern.compile("^(.+)\\s+(WHERE)\\s+(.*)", Pattern.CASE_INSENSITIVE);
	private final static Pattern orderByPattern = Pattern.compile("^(.+)\\s+(ORDER\\s+BY)\\s+(.*)", Pattern.CASE_INSENSITIVE);

	private final static Pattern splitByOperator = Pattern.compile("\\b((?<=>=|<=|=|>|<|IN)|(?=>=|<=|=|>|<|IN))\\b", Pattern.CASE_INSENSITIVE);
	private final static Pattern logicalOpSplit = Pattern.compile(" AND ", Pattern.CASE_INSENSITIVE); //TODO: add support of OR


	private final static Map<Pattern, QueryType> queryTypes = new HashMap<Pattern, QueryType>() {{
		put(selectPattern, QueryType.SELECT);
		put(updatePattern, QueryType.UPDATE);
		put(deletePattern, QueryType.DELETE);
	}};

	private final static Map<Pattern, Collection<Pattern>> queryOptionalParts = new HashMap<Pattern, Collection<Pattern>>() {{
		put(selectPattern, Arrays.asList(wherePattern, orderByPattern));
		put(updatePattern, Collections.singleton(wherePattern));
		put(deletePattern, Collections.singleton(wherePattern));
	}};



	@Override
	public <T> QueryInfo createCriteria(final String qlString, final Class<T> resultClass) {
		if (joinPattern.matcher(qlString).find()) {
			throw new UnsupportedOperationException("JOINs are not supported");
		}


		for (final Entry<Pattern, QueryType> queryDef : queryTypes.entrySet()) {
			Matcher m = queryDef.getKey().matcher(qlString);
			if (!m.find()) {
				continue;
			}

			String[] parts = capture(m);
			for (final Pattern p : queryOptionalParts.get(queryDef.getKey())) {
				final String restStr = parts[parts.length - 1];
				m = p.matcher(restStr);
				if (!m.find()) {
					continue;
				}

				parts = ObjectArrays.concat(Arrays.copyOf(parts, parts.length - 1), capture(m), String.class);
			}

			switch(queryDef.getValue()) {
				case SELECT:
					return createQuery(parts, resultClass);
				case UPDATE:
					return createUpdate(parts, resultClass);
				case DELETE:
					return createDelete(parts, resultClass);
				default:
					throw new IllegalStateException("Unknown query type " + qlString);
			}
		}


		throw new IllegalArgumentException(qlString);
	}


	private <T> QueryInfo createQuery(final String[] parts, final Class<T> resultClass) {
		final String[] fromEntityAlias = parseFromAlias(QueryType.SELECT, parts[3]);
		final Map<String, String> from = parseFrom(fromEntityAlias);

		final String fields = parts[1];


		final Map<String, Object> what = parseFieldsToSelect(fields, fromEntityAlias);

		final List<ClauseInfo> clauses = new ArrayList<>();
		final int nextBlockIndex = parseWhereClause(parts, 4, clauses);

		final ClauseInfo where = createWhereClause(clauses);


		final Collection<OrderByInfo> orders = parts.length > nextBlockIndex + 1 && parts[nextBlockIndex].equalsIgnoreCase("ORDER BY") ? new ArrayList<OrderByInfo>() : null;

		if (orders != null) {
			final String[] orderRules = parts[nextBlockIndex + 1].split("\\s*,\\s*");
			for (final String orderRule : orderRules) {
				String[] orderDef = orderRule.split("\\s+");
				if (orderDef.length < 2) {
					// TODO: add info about the alias name here.
					final String fieldName = getFieldName(orderDef[0]);

					orderDef = new String[] {fieldName, Order.ASC.name()};
				}
				orders.add(new OrderByInfo(getFieldName(orderDef[0]), Order.valueOf(orderDef[1].toUpperCase())));
			}
		}

		return new QueryInfo(QueryType.SELECT, what, from, where, orders);
	}


	private <T> QueryInfo createUpdate(final String[] parts, final Class<T> resultClass) {
		throw new UnsupportedOperationException("Update is TBD");
	}


	private <T> QueryInfo createDelete(final String[] parts, final Class<T> resultClass) {
		final String[] fromEntityAlias = parseFromAlias(QueryType.DELETE, parts[2]);
		final Map<String, String> from = parseFrom(fromEntityAlias);

		final List<ClauseInfo> clauses = new ArrayList<>();
		parseWhereClause(parts, 3, clauses);

		final ClauseInfo where = createWhereClause(clauses);

		return new QueryInfo(QueryType.DELETE, null, from, where, null);

	}

	private String[] capture(final Matcher m) {
		final int n = m.groupCount();
		final String[] capture = new String[n];
		for (int i = 0; i < n; i++) {
			capture[i] = m.group(i + 1);
		}
		return capture;
	}

	private boolean isParameter(final String arg){
		return arg.startsWith(":") || arg.startsWith("?");
	}

	private Object createArgument(final Operand op, final String spec) {
		if (spec.startsWith(":")) {
			return new ArgumentInfo<Object>(spec.substring(1), null);
		}
		if (spec.startsWith("?")) {
			return new ArgumentInfo<Object>(Integer.parseInt(spec.substring(1)), null);
		}


		if (op.isArrayParameter()) {
			final String[] strarr = spec.replaceFirst("^\\s*\\(\\s*", "").replaceFirst("\\s*\\)\\s*", "").split("\\s*,\\s*");
			final int n = strarr.length;
			final Object[] args = new Object[n];

			for (int i = 0; i < n; i++) {
				args[i] = createArgument(Operand.EQ, strarr[i]);
			}

			return args;
		}



		if (spec.startsWith("'") && spec.endsWith("'")) {
			return spec.substring(1, spec.length() - 1);
		}


		for (final Class<?> c : new Class[] {Integer.class, Long.class, Double.class}) {
			try {
				return c.getMethod("valueOf", String.class).invoke(null, spec);
			} catch (final ReflectiveOperationException e) {
				// Ignore. This string is not as string representation of current type
			}
		}

		if ("true".equals(spec)) {
			return true;
		}
		if ("false".equals(spec)) {
			return false;
		}

		return spec;
	}

	/**
	 * This method splits logical condition (e.g. x>=5) into tokens (e.g. 'x', '>=', '5')
	 * @param condition
	 * @return array of tokens
	 */
	private String[] splitCondition(String condition) {
		// Actually regular expression splitByOperator should do the job itself. However I failed to created
		// regex that works in all cases for all operators, removes spaces etc. So I had to do some additional
		// work by processing string before and after slitting.
		condition = condition.replace("'", "").replaceAll("\\s*([<>=])", "$1").replaceAll("([<>=])\\s+", "$1").replaceAll("([<>=])([:?])", "$1__$2");
		final String[] conditionOperands = splitByOperator.split(condition);

		final int n = conditionOperands.length;
		for (int i = 0; i < n; i++) {
			conditionOperands[i] = conditionOperands[i].trim().replaceFirst("^__([:?])", "$1");
		}

		return conditionOperands;
	}

	/**
	 * Accepts dot separated name specification and returns the name itself:
	 * <ul>
	 * 	<li><code>p.id -> id</code></li>
	 * 	<li><code>p.address.street.name -> name</code></li>
	 * <ul>
	 * @param def
	 * @return the name
	 */
	private String getFieldName(final String def) {
		final String[] path = def.split("\\.");
		final String fieldName = path[path.length - 1];
		return fieldName;

	}


	private String[] parseFromAlias(final QueryType queryType, final String spec) {
		final String[] fromList = spec.split("\\s*,\\s*");
		if (fromList.length > 1) {
			throw new UnsupportedOperationException(queryType + " from several tables in one statement is not supported");
		}
		final String[] fromEntityAlias = fromList[0].split("\\s+");

		return fromEntityAlias;
	}


	private Map<String, String> parseFrom(final String[] fromEntityAlias) {
		return Collections.singletonMap(fromEntityAlias.length < 2 ? fromEntityAlias[0] : fromEntityAlias[1], fromEntityAlias[0]);
	}


	private int parseWhereClause(final String[] parts, int nextBlockIndex, final List<ClauseInfo> clauses) {
		if (parts.length > nextBlockIndex + 1 && parts[nextBlockIndex].equalsIgnoreCase("WHERE")) {
			final String[] conditions = logicalOpSplit.split(parts[nextBlockIndex + 1]);

			for (final String condition : conditions) {
				final String[] conditionOperators = splitCondition(condition);
				if (conditionOperators.length == 3) {
					String arg1 = conditionOperators[0];
					Operand op = Operand.bySymbol(conditionOperators[1]);
					String arg2 = conditionOperators[2];
					if(isParameter(arg1) && isParameter(arg2)){
						throw new UnsupportedOperationException("Double-parameter expressions are not supported");
					}
					if(isParameter(arg1)) {
						final String t = arg1;
						arg1 = arg2;
						arg2 = t;
						op = changeDirection(op);
					}
					final ClauseInfo clause = new ClauseInfo(
							getFieldName(arg1),
							op,
							createArgument(op, arg2));
					clauses.add(clause);
				}
				//TODO: add validation and exceptions if needed.
				//TODO: add IN
			}

			nextBlockIndex += 2;
		}
		return nextBlockIndex;
	}

	private Operand changeDirection(final Operand op) {
		switch(op){
			case EQ:	return Operand.EQ;
			case NE:	return Operand.NE;
			case GE:	return Operand.LE;
			case LE:	return Operand.GE;
			case GT:	return Operand.LT;
			case LT:	return Operand.GT;
			default:
				throw new IllegalArgumentException();
		}
	}


	private ClauseInfo createWhereClause(final List<ClauseInfo> clauses) {
		final ClauseInfo where =
				clauses.size() == 0 ?
					null :
					clauses.size() == 1 ?
							clauses.get(0) :
							new ClauseInfo(null, Operand.AND, clauses.toArray(new ClauseInfo[0]));

		return where;
	}


	private Map<String, Object> parseFieldsToSelect(String fields, final String[] entityAlias) {
		fields = fields.replace(" ", "");
		if("*".equals(fields)) {
			return null;
		}

		if(entityAlias.length > 1 && (entityAlias[1].equals(fields) || ("OBJECT("+entityAlias[1]+")").equalsIgnoreCase(fields))) {
			return null;
		}



		final Map<String, Object> what = new LinkedHashMap<String, Object>();
		for (final String f : fields.split("\\s*,\\s*")) {
			String[] nameAndAlias = f.split("\\.");
			if (nameAndAlias.length == 1) {
				nameAndAlias = new String[] {entityAlias[0], nameAndAlias[0]};
			}
			if (nameAndAlias.length > 2) {
				throw new UnsupportedOperationException("Reference to nested property in query is not supported: " + f);
			}

			what.put(nameAndAlias[0], nameAndAlias[1]);
		}

		return what;
	}

}



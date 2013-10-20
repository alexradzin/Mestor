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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;


//and
//asc
//conjunction
//count
//createQuery
//desc
//equal
//ge
//greaterThanOrEqualTo
//gt
//isNull
//le
//like
//literal
//lt
//max
//min
//notEqual
//or
//sum
//upper

//TODO: create the following hierarchy

//public abstract class SelectionImpl<X> implements Selection<X>, InternalSelection, Serializable{
//public class ExpressionImpl<X> extends SelectionImpl<X> implements Expression<X>, InternalExpression{
//public class FunctionExpressionImpl<X> extends ExpressionImpl<X>{
//public class CompoundExpressionImpl extends FunctionExpressionImpl<Boolean> implements Predicate{
//public class InImpl<T> extends CompoundExpressionImpl implements In<T> {
//public class PredicateImpl extends CompoundExpressionImpl implements Predicate {
//
//public abstract class SelectionImpl<X> implements Selection<X>, InternalSelection, Serializable{
//public class ExpressionImpl<X> extends SelectionImpl<X> implements Expression<X>, InternalExpression{
//public class PathImpl<X> extends ExpressionImpl<X> implements Path<X>{
//public class FromImpl<Z, X>  extends PathImpl<X> implements javax.persistence.criteria.From<Z, X> {
//public class RootImpl<X> extends FromImpl<X, X> implements Root<X> {


public class CriteriaBuilderImpl implements CriteriaBuilder {

	@Override
	public CriteriaQuery<Object> createQuery() {
		return createQuery(Object.class);
	}

	@Override
	public <T> CriteriaQuery<T> createQuery(final Class<T> resultClass) {
		return new CriteriaQueryImpl<T>(resultClass, this);
	}

	@Override
	public CriteriaQuery<Tuple> createTupleQuery() {
		return createQuery(Tuple.class);
	}

	@Override
	public <T> CriteriaUpdate<T> createCriteriaUpdate(final Class<T> targetEntity) {
		return new CriteriaUpdateImpl<T>(targetEntity, this);
	}

	@Override
	public <T> CriteriaDelete<T> createCriteriaDelete(final Class<T> targetEntity) {
		return new CriteriaDeleteImpl<T>(targetEntity, this);
	}

	@Override
	public <Y> CompoundSelection<Y> construct(final Class<Y> resultClass, final Selection<?>... selections) {
		return new CompoundSelectionImpl<>(resultClass, selections);
	}

	@Override
	public CompoundSelection<Tuple> tuple(final Selection<?>... selections) {
		return construct(Tuple.class, selections);
	}

	@Override
	public CompoundSelection<Object[]> array(final Selection<?>... selections) {
		return construct(Object[].class, selections);
	}

	@Override
	public Order asc(final Expression<?> x) {
		return new OrderImpl(x, true);
	}

	@Override
	public Order desc(final Expression<?> x) {
		return new OrderImpl(x, false);
	}

	@Override
	public <N extends Number> Expression<Double> avg(final Expression<N> x) {
		return new FunctionExpressionImpl<Double>("avg", x);
	}

	@Override
	public <N extends Number> Expression<N> sum(final Expression<N> x) {
		return new FunctionExpressionImpl<N>("sum", x);
	}

	@Override
	public Expression<Long> sumAsLong(final Expression<Integer> x) {
		return new FunctionExpressionImpl<Long>(Long.class, "sum", x);
	}

	@Override
	public Expression<Double> sumAsDouble(final Expression<Float> x) {
		return new FunctionExpressionImpl<Double>(Double.class, "sum", x);
	}

	@Override
	public <N extends Number> Expression<N> max(final Expression<N> x) {
		return new FunctionExpressionImpl<N>("max", x);
	}

	@Override
	public <N extends Number> Expression<N> min(final Expression<N> x) {
		return new FunctionExpressionImpl<N>("min", x);
	}

	@Override
	public <X extends Comparable<? super X>> Expression<X> greatest(final Expression<X> x) {
		return new FunctionExpressionImpl<X>("max", x);
	}

	@Override
	public <X extends Comparable<? super X>> Expression<X> least(final Expression<X> x) {
		return new FunctionExpressionImpl<X>("min", x);
	}

	@Override
	public Expression<Long> count(final Expression<?> x) {
		return new FunctionExpressionImpl<Long>(Long.class, "count", x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Expression<Long> countDistinct(final Expression<?> x) {
		return new FunctionExpressionImpl<Long>(Long.class, "countDistinct", (Expression<Object>)x);
	}

	@Override
	public Predicate exists(final Subquery<?> subquery) {
		return new BooleanFunctionExpressionImpl("exists", subquery);
	}

	@Override
	public <Y> Expression<Y> all(final Subquery<Y> subquery) {
		return new FunctionExpressionImpl<Y>("all", subquery);
	}

	@Override
	public <Y> Expression<Y> some(final Subquery<Y> subquery) {
		return new FunctionExpressionImpl<Y>("some", subquery);
	}

	@Override
	public <Y> Expression<Y> any(final Subquery<Y> subquery) {
		return new FunctionExpressionImpl<Y>("any", subquery);
	}

	@Override
	public Predicate and(final Expression<Boolean> x, final Expression<Boolean> y) {
		return new CompoundExpressionImpl(BooleanOperator.AND, x, y);
	}

	@Override
	public Predicate and(final Predicate... restrictions) {
		return new CompoundExpressionImpl(BooleanOperator.AND, restrictions);
	}

	@Override
	public Predicate or(final Expression<Boolean> x, final Expression<Boolean> y) {
		return new CompoundExpressionImpl(BooleanOperator.OR, x, y);
	}

	@Override
	public Predicate or(final Predicate... restrictions) {
		return new CompoundExpressionImpl(BooleanOperator.OR, restrictions);
	}

	@Override
	public Predicate not(final Expression<Boolean> restriction) {
		throw new UnsupportedOperationException("NOT is not supported");
	}

	@Override
	public Predicate conjunction() {
		return new CompoundExpressionImpl(BooleanOperator.AND);
	}

	@Override
	public Predicate disjunction() {
		return new CompoundExpressionImpl(BooleanOperator.OR);
	}

	@Override
	public Predicate isTrue(final Expression<Boolean> x) {
		return new BooleanFunctionExpressionImpl("true", x, new ConstantExpresion<>(true));
	}

	@Override
	public Predicate isFalse(final Expression<Boolean> x) {
		return new BooleanFunctionExpressionImpl("false", x, new ConstantExpresion<>(false));
	}

	@Override
	public Predicate isNull(final Expression<?> x) {
		return new BooleanFunctionExpressionImpl("null", x, new ConstantExpresion<>(null));
	}

	@Override
	public Predicate isNotNull(final Expression<?> x) {
		// TODO should be null but negated.
		return new BooleanFunctionExpressionImpl("not null", x, new ConstantExpresion<>(null));
	}

	@Override
	public Predicate equal(final Expression<?> x, final Expression<?> y) {
		return compare("eq", x, y);
	}

	@Override
	public Predicate equal(final Expression<?> x, final Object y) {
		return equal(x, new ConstantExpresion<Object>(y));
	}

	@Override
	public Predicate notEqual(final Expression<?> x, final Expression<?> y) {
		return compare("ne", x, y);
	}

	@Override
	public Predicate notEqual(final Expression<?> x, final Object y) {
		return notEqual(x, new ConstantExpresion<Object>(y));
	}



	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThan(final Expression<? extends Y> x, final Expression<? extends Y> y) {
		return compare("gt", x, y);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThan(final Expression<? extends Y> x, final Y y) {
		return greaterThan(x, new ConstantExpresion<Y>(y));
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(final Expression<? extends Y> x,
			final Expression<? extends Y> y) {
		return compare("ge", x, y);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(final Expression<? extends Y> x, final Y y) {
		return greaterThanOrEqualTo(x, new ConstantExpresion<Y>(y));
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThan(final Expression<? extends Y> x, final Expression<? extends Y> y) {
		return compare("lt", x, y);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThan(final Expression<? extends Y> x, final Y y) {
		return lessThan(x, new ConstantExpresion<Y>(y));
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(final Expression<? extends Y> x,
			final Expression<? extends Y> y) {
		return compare("le", x, y);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(final Expression<? extends Y> x, final Y y) {
		return lessThanOrEqualTo(x, new ConstantExpresion<Y>(y));
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(final Expression<? extends Y> v, final Expression<? extends Y> x,
			final Expression<? extends Y> y) {
		return new BooleanFunctionExpressionImpl("between", v, x, y);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(final Expression<? extends Y> v, final Y x, final Y y) {
		return between(v, new ConstantExpresion<Y>(x), new ConstantExpresion<Y>(x));
	}

	@Override
	public Predicate gt(final Expression<? extends Number> x, final Expression<? extends Number> y) {
		return compare("gt", x, y);
	}

	@Override
	public Predicate gt(final Expression<? extends Number> x, final Number y) {
		return gt(x, new ConstantExpresion<Number>(y));
	}

	@Override
	public Predicate ge(final Expression<? extends Number> x, final Expression<? extends Number> y) {
		return compare("ge", x, y);
	}

	@Override
	public Predicate ge(final Expression<? extends Number> x, final Number y) {
		return ge(x, new ConstantExpresion<Number>(y));
	}

	@Override
	public Predicate lt(final Expression<? extends Number> x, final Expression<? extends Number> y) {
		return compare("lt", x, y);
	}

	@Override
	public Predicate lt(final Expression<? extends Number> x, final Number y) {
		return lt(x, new ConstantExpresion<Number>(y));
	}

	@Override
	public Predicate le(final Expression<? extends Number> x, final Expression<? extends Number> y) {
		return compare("le", x, y);
	}

	@Override
	public Predicate le(final Expression<? extends Number> x, final Number y) {
		return ge(x, new ConstantExpresion<Number>(y));
	}

	@Override
	public <N extends Number> Expression<N> neg(final Expression<N> x) {
		return new FunctionExpressionImpl<N>("size", x);
	}

	@Override
	public <N extends Number> Expression<N> abs(final Expression<N> x) {
		return new FunctionExpressionImpl<N>("abs", x);
	}

	@Override
	public <N extends Number> Expression<N> sum(final Expression<? extends N> x, final Expression<? extends N> y) {
		return new FunctionExpressionImpl<N>("sum", x, y);
	}

	@Override
	public <N extends Number> Expression<N> sum(final Expression<? extends N> x, final N y) {
		return sum(x, new ConstantExpresion<N>(y));
	}

	@Override
	public <N extends Number> Expression<N> sum(final N x, final Expression<? extends N> y) {
		return sum(new ConstantExpresion<N>(x), y);
	}

	@Override
	public <N extends Number> Expression<N> prod(final Expression<? extends N> x, final Expression<? extends N> y) {
		throw new UnsupportedOperationException("prod is not supported");
	}

	@Override
	public <N extends Number> Expression<N> prod(final Expression<? extends N> x, final N y) {
		throw new UnsupportedOperationException("prod is not supported");
	}

	@Override
	public <N extends Number> Expression<N> prod(final N x, final Expression<? extends N> y) {
		throw new UnsupportedOperationException("prod is not supported");
	}

	@Override
	public <N extends Number> Expression<N> diff(final Expression<? extends N> x, final Expression<? extends N> y) {
		return new FunctionExpressionImpl<>("diff", x, y);
	}

	@Override
	public <N extends Number> Expression<N> diff(final Expression<? extends N> x, final N y) {
		return diff(x, new ConstantExpresion<N>(y));
	}

	@Override
	public <N extends Number> Expression<N> diff(final N x, final Expression<? extends N> y) {
		return diff(new ConstantExpresion<N>(x), y);
	}

	@Override
	public Expression<Number> quot(final Expression<? extends Number> x, final Expression<? extends Number> y) {
		return new FunctionExpressionImpl<>("quot", x, y);
	}

	@Override
	public Expression<Number> quot(final Expression<? extends Number> x, final Number y) {
		return quot(x, new ConstantExpresion<>(y));
	}

	@Override
	public Expression<Number> quot(final Number x, final Expression<? extends Number> y) {
		return quot(new ConstantExpresion<>(x), y);
	}

	@Override
	public Expression<Integer> mod(final Expression<Integer> x, final Expression<Integer> y) {
		return new FunctionExpressionImpl<>("mod", x, y);
	}

	@Override
	public Expression<Integer> mod(final Expression<Integer> x, final Integer y) {
		return mod(x, new ConstantExpresion<>(y));
	}

	@Override
	public Expression<Integer> mod(final Integer x, final Expression<Integer> y) {
		return mod(new ConstantExpresion<>(x), y);
	}

	@Override
	public Expression<Double> sqrt(final Expression<? extends Number> x) {
		return new FunctionExpressionImpl<>("sqrt", x);
	}

	@Override
	public Expression<Long> toLong(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<Integer> toInteger(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<Float> toFloat(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<Double> toDouble(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<BigDecimal> toBigDecimal(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<BigInteger> toBigInteger(final Expression<? extends Number> number) {
		return toType(number);
	}

	@Override
	public Expression<String> toString(final Expression<Character> character) {
		return toType(character);
	}

	@Override
	public <T> Expression<T> literal(final T value) {
		return new ConstantExpresion<T>(value);
	}

	@Override
	public <T> Expression<T> nullLiteral(final Class<T> resultClass) {
		return new ConstantExpresion<T>(resultClass, null);
	}

	@Override
	public <T> ParameterExpression<T> parameter(final Class<T> paramClass) {
		return new ParameterExpressionImpl<T>(paramClass);
	}

	@Override
	public <T> ParameterExpression<T> parameter(final Class<T> paramClass, final String name) {
		return new ParameterExpressionImpl<T>(name, paramClass);
	}

	@Override
	public <C extends Collection<?>> Predicate isEmpty(final Expression<C> collection) {
		return new BooleanFunctionExpressionImpl("empty", collection);
	}

	@Override
	public <C extends Collection<?>> Predicate isNotEmpty(final Expression<C> collection) {
		return new BooleanFunctionExpressionImpl("not empty", collection);
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(final Expression<C> collection) {
		return new FunctionExpressionImpl<Integer>(Integer.class, "size", collection);
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(final C collection) {
		return literal(collection.size());
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(final Expression<E> elem, final Expression<C> collection) {
		throw new UnsupportedOperationException("isMember is unsupported");
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(final E elem, final Expression<C> collection) {
		throw new UnsupportedOperationException("isMember is unsupported");
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(final Expression<E> elem, final Expression<C> collection) {
		throw new UnsupportedOperationException("isNotMember is unsupported");
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(final E elem, final Expression<C> collection) {
		throw new UnsupportedOperationException("isNotMember is unsupported");
	}

	@Override
	public <V, M extends Map<?, V>> Expression<Collection<V>> values(final M map) {
		return new ConstantExpresion<Collection<V>>(null, ((Map<?, V>)map).values());
	}

	@Override
	public <K, M extends Map<K, ?>> Expression<Set<K>> keys(final M map) {
		return new ConstantExpresion<Set<K>>(null, ((Map<K, ?>)map).keySet());
	}

	@Override
	public Predicate like(final Expression<String> x, final Expression<String> pattern) {
		return compare("like", x, pattern);
	}

	@Override
	public Predicate like(final Expression<String> x, final String pattern) {
		return like(x, new ConstantExpresion<String>(pattern));
	}

	@Override
	public Predicate like(final Expression<String> x, final Expression<String> pattern, final Expression<Character> escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate like(final Expression<String> x, final Expression<String> pattern, final char escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate like(final Expression<String> x, final String pattern, final Expression<Character> escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate like(final Expression<String> x, final String pattern, final char escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate notLike(final Expression<String> x, final Expression<String> pattern) {
		return compare("not like", x, pattern);
	}

	@Override
	public Predicate notLike(final Expression<String> x, final String pattern) {
		return notLike(x, new ConstantExpresion<String>(pattern));
	}

	@Override
	public Predicate notLike(final Expression<String> x, final Expression<String> pattern, final Expression<Character> escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate notLike(final Expression<String> x, final Expression<String> pattern, final char escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate notLike(final Expression<String> x, final String pattern, final Expression<Character> escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Predicate notLike(final Expression<String> x, final String pattern, final char escapeChar) {
		throw new UnsupportedOperationException("Like with escape characters are is not supported");
	}

	@Override
	public Expression<String> concat(final Expression<String> x, final Expression<String> y) {
		return new FunctionExpressionImpl<>("concat", x, y);
	}

	@Override
	public Expression<String> concat(final Expression<String> x, final String y) {
		return concat(x, new ConstantExpresion<String>(y));
	}

	@Override
	public Expression<String> concat(final String x, final Expression<String> y) {
		return concat(new ConstantExpresion<String>(x), y);
	}

	@Override
	public Expression<String> substring(final Expression<String> x, final Expression<Integer> from) {
		return new FunctionExpressionImpl<>(String.class, "substring", x, from);
	}

	@Override
	public Expression<String> substring(final Expression<String> x, final int from) {
		return substring(x, new ConstantExpresion<Integer>(from));
	}

	@Override
	public Expression<String> substring(final Expression<String> x, final Expression<Integer> from, final Expression<Integer> len) {
		return new FunctionExpressionImpl<>(String.class, "substring", x, from, len);
	}

	@Override
	public Expression<String> substring(final Expression<String> x, final int from, final int len) {
		return substring(x, new ConstantExpresion<Integer>(from), new ConstantExpresion<Integer>(len));
	}

	@Override
	public Expression<String> trim(final Expression<String> x) {
		return trim(Trimspec.BOTH, x);
	}

	@Override
	public Expression<String> trim(final Trimspec ts, final Expression<String> x) {
		return new FunctionExpressionImpl<String>("trim", new ConstantExpresion<String>(ts.name()), x);
	}

	@Override
	public Expression<String> trim(final Expression<Character> t, final Expression<String> x) {
		return trim(Trimspec.BOTH, t, x);
	}

	@Override
	public Expression<String> trim(final Trimspec ts, final Expression<Character> t, final Expression<String> x) {
		return new FunctionExpressionImpl<String>("trim", new ConstantExpresion<String>(ts.name()), t, x);
	}

	@Override
	public Expression<String> trim(final char t, final Expression<String> x) {
		return trim(new ConstantExpresion<>(new Character(t)), x);
	}

	@Override
	public Expression<String> trim(final Trimspec ts, final char t, final Expression<String> x) {
		return trim(ts, new ConstantExpresion<>(new Character(t)), x);
	}

	@Override
	public Expression<String> lower(final Expression<String> x) {
		return new FunctionExpressionImpl<>("lower", x);
	}

	@Override
	public Expression<String> upper(final Expression<String> x) {
		return new FunctionExpressionImpl<>("upper", x);
	}

	@Override
	public Expression<Integer> length(final Expression<String> x) {
		return new FunctionExpressionImpl<>("length", x);
	}

	@Override
	public Expression<Integer> locate(final Expression<String> x, final Expression<String> pattern) {
		return new FunctionExpressionImpl<>("locate", x, pattern);
	}

	@Override
	public Expression<Integer> locate(final Expression<String> x, final String pattern) {
		return locate(x, new ConstantExpresion<String>(pattern));
	}

	@Override
	public Expression<Integer> locate(final Expression<String> x, final Expression<String> pattern, final Expression<Integer> from) {
		return new FunctionExpressionImpl<>("locate", x, pattern, from);
	}

	@Override
	public Expression<Integer> locate(final Expression<String> x, final String pattern, final int from) {
		return locate(x, new ConstantExpresion<>(pattern), new ConstantExpresion<Integer>(from));
	}

	@Override
	public Expression<Date> currentDate() {
		return new FunctionExpressionImpl<>("date");
	}

	@Override
	public Expression<Timestamp> currentTimestamp() {
		return new FunctionExpressionImpl<>("timestamp");
	}

	@Override
	public Expression<Time> currentTime() {
		return new FunctionExpressionImpl<>("time");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> In<T> in(final Expression<? extends T> expression) {
		return new InImpl<T>((Expression<T>)expression);
	}

	@Override
	public <Y> Expression<Y> coalesce(final Expression<? extends Y> x, final Expression<? extends Y> y) {
		return new FunctionExpressionImpl<>("coalesce", x, y);
	}

	@Override
	public <Y> Expression<Y> coalesce(final Expression<? extends Y> x, final Y y) {
		return coalesce(x, new ConstantExpresion<Y>(y));
	}

	@Override
	public <Y> Expression<Y> nullif(final Expression<Y> x, final Expression<?> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Expression<Y> nullif(final Expression<Y> x, final Y y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Coalesce<T> coalesce() {
		throw new UnsupportedOperationException("Coalence expression builder is not implemented yet");
	}

	@Override
	public <C, R> SimpleCase<C, R> selectCase(final Expression<? extends C> expression) {
		throw new UnsupportedOperationException("Cases are not supported yet");
	}

	@Override
	public <R> Case<R> selectCase() {
		throw new UnsupportedOperationException("Cases are not supported yet");
	}

	@Override
	public <T> Expression<T> function(final String name, final Class<T> type, final Expression<?>... args) {
		return new FunctionExpressionImpl<>(type, name, args);
	}

	@Override
	public <X, T, V extends T> Join<X, V> treat(final Join<X, T> join, final Class<V> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, T, E extends T> CollectionJoin<X, E> treat(final CollectionJoin<X, T> join, final Class<E> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, T, E extends T> SetJoin<X, E> treat(final SetJoin<X, T> join, final Class<E> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, T, E extends T> ListJoin<X, E> treat(final ListJoin<X, T> join, final Class<E> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, K, T, V extends T> MapJoin<X, K, V> treat(final MapJoin<X, K, T> join, final Class<V> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, T extends X> Path<T> treat(final Path<X> path, final Class<T> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, T extends X> Root<T> treat(final Root<X> root, final Class<T> type) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@SuppressWarnings("unchecked")
	private <T, S> Expression<T> toType(final Expression<? extends S> value) {
		return (Expression<T>) value;
	}

	private Predicate compare(final String op, final Expression<?> ... xs) {
		return new BooleanFunctionExpressionImpl(op, xs);
	}
}

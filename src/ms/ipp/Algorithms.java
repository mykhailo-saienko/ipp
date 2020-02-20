package ms.ipp;

import static ms.ipp.Iterables.filtered;
import static ms.ipp.Iterables.list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import ms.ipp.base.KeyValue;

/**
 * A collection of routines that manipulate Java class related to functional
 * programming, such as Function, Predicate, Operator, Consumer, Supplier, etc.
 * 
 * @author mykhailo.saienko
 *
 */
public class Algorithms {
	///////////////////////////////////////////////////////
	/// *********** Function Concatenation ************ ///
	///////////////////////////////////////////////////////

	/**
	 * Creates a new {@code BiPredicate} which returns true if and only if at least
	 * one of the given non-null {@code BiPredicates} returns true. Is equivalent
	 * to:
	 * 
	 * <pre>
	 * reduceNonNulls(BiPredicate::or, Arrays.asList(preds));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param preds a parameter array of {@link BiPredicate}s. Nulls in the array
	 *              are ignored
	 * @return null if all predicates are null or a concatenated BiPredicate
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T, U> BiPredicate<T, U> or(BiPredicate<? super T, ? super U>... preds) {
		return reduceNonNulls(BiPredicate::or, Arrays.asList(preds));
	}

	/**
	 * Creates a new {@code BiPredicate} which returns true if and only if all given
	 * {@code BiPredicates} return true. Is equivalent to:
	 * 
	 * <pre>
	 * reduceNonNulls(BiPredicate::and, Arrays.asList(preds));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param preds a parameter array of {@link BiPredicate}s. Nulls in the array
	 *              are ignored
	 * @return null if all predicates are null or a concatenated BiPredicate
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T, U> BiPredicate<T, U> and(BiPredicate<? super T, ? super U>... preds) {
		return reduceNonNulls(BiPredicate::and, Arrays.asList(preds));
	}

	/**
	 * Creates a new {@code Predicate} which returns true if and only if at least
	 * one of the given non-null {@code Predicates} returns true. Is equivalent to:
	 * 
	 * <pre>
	 * reduceNonNulls(Predicate::or, Arrays.asList(preds));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param preds a parameter array of {@link Predicate}s. Nulls in the array are
	 *              ignored
	 * @return null if all predicates are null or a concatenated Predicate
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T> Predicate<T> or(Predicate<? super T>... preds) {
		return reduceNonNulls(Predicate::or, Arrays.asList(preds));
	}

	/**
	 * Creates a new {@code Predicate} which returns true if and only if all given
	 * {@code Predicates} return true. Is equivalent to:
	 * 
	 * <pre>
	 * reduceNonNulls(Predicate::and, Arrays.asList(preds));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param preds a parameter array of {@link Predicate}s. Nulls in the array are
	 *              ignored
	 * @return null if all predicates are null or a concatenated Predicate
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T> Predicate<T> and(Predicate<? super T>... preds) {
		return reduceNonNulls(Predicate::and, Arrays.asList(preds));
	}

	/**
	 * Creates a new {@code BiConsumer} which calls all given {@code BiConsumer}s in
	 * the order of their appearance as arguments. Is equivalent to:
	 * 
	 * <pre>
	 * reduceNonNulls(BiConsumer::andThen, Arrays.asList(procs));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param procs a parameter array of {@link BiConsumers}s. Nulls in the array
	 *              are ignored
	 * @return null if all BiConsumers are null or a concatenated {@code BiConsumer}
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T, U> BiConsumer<T, U> seq(BiConsumer<? super T, ? super U>... procs) {
		return reduceNonNulls(BiConsumer::andThen, Arrays.asList(procs));
	}

	/**
	 * Creates a new {@code Consumer} which calls all given {@code Consumer}s in the
	 * order of their appearance as arguments. Is equivalent to:
	 * 
	 * <pre>
	 * reduceNonNulls(Consumer::andThen, Arrays.asList(procs));
	 * </pre>
	 * 
	 * @see #reduceNonNulls(BiFunction, Object...)
	 * @param procs a parameter array of {@link Consumers}s. Nulls in the array are
	 *              ignored
	 * @return null if all Consumers are null or a concatenated {@code Consumer}
	 *         otherwise.
	 */
	@SafeVarargs
	public static <T> Consumer<T> seq(Consumer<? super T>... procs) {
		return reduceNonNulls(Consumer::andThen, Arrays.asList(procs));
	}

	/**
	 * Creates a new {@code Function} which returns the result of a given
	 * {@link Function} if a given {@code Predicate} returns true and null otherwise
	 * 
	 * @param pred a {@code Predicate} to use in the new {@code Function}, not null
	 * @param func a {@code Function} to use in the new {@code Function}, not null
	 * @return
	 */
	public static <T, U> Function<T, U> applyIf(Predicate<? super T> pred,
			Function<? super T, U> function) {
		return t -> pred.test(t) ? function.apply(t) : null;
	}

	/**
	 * Creates a new {@code BiFunction} which returns the result of a given
	 * {@link BiFunction} if a given {@code BiPredicate} returns true and null
	 * otherwise
	 * 
	 * @param pred a {@code BiPredicate} to use in the new {@code BiFunction}, not
	 *             null
	 * @param func a {@code BiFunction} to use in the new {@code BiFunction}, not
	 *             null
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> applyIf(BiPredicate<? super T, ? super U> pred,
			BiFunction<? super T, ? super U, V> func) {
		return (t, u) -> pred.test(t, u) ? func.apply(t, u) : null;
	}

	/**
	 * If the {@code source} is not null and a given {@code Predicate} returns true
	 * for it, applies a given {@code Function} to it and returns the result.
	 * Otherwise, returns null.
	 * 
	 * @param source the source value. Null returns null.
	 * @param pred   the Predicate to test the value against. Null disables the
	 *               test.
	 * @param func   the Function to apply, not null.
	 * @return
	 */
	public static <T, U> U ignoreIfNullOrPred(T source, Predicate<? super T> pred,
			Function<? super T, U> func) {
		if (source == null || (pred != null && pred.test(source))) {
			return null;
		}
		return func.apply(source);
	}

	/**
	 * Creates a new {@code BiConsumer} which calls a given {@code BiConsumer} only
	 * if a given {@code BiPredicate} returns true.
	 * 
	 * @param pred a {@code BiPredicate} to use in the new {@code BiConsumer}. If
	 *             null, the original {@code BiConsumer} is always called
	 * @param proc a {@code BiConsumer} to use in the new {@code BiConsumer}. If
	 *             null, the new {@code BiConsumer} only applies the given
	 *             {@code BiPredicate}
	 * @return null if both the {@code BiConsumer} and the {@code BiPredicate} are
	 *         null or a new BiConsumer otherwise
	 */
	public static <T, U> BiConsumer<T, U> callIf(BiPredicate<? super T, ? super U> pred,
			BiConsumer<? super T, ? super U> proc) {
		if (pred == null && proc == null) {
			return null;
		} else if (pred == null) {
			return proc::accept;
		} else if (proc == null) {
			return pred::test;
		}
		return (t, u) -> {
			if (pred.test(t, u)) {
				proc.accept(t, u);
			}
		};
	}

	/**
	 * Creates a new {@code Consumer} which calls a given {@code Consumer} only if a
	 * given {@code Predicate} returns true.
	 * 
	 * @param pred a {@code Predicate} to use in the new {@code Consumer}. If null,
	 *             the original {@code Consumer} is always called
	 * @param proc a {@code Consumer} to use in the new {@code Consumer}. If null,
	 *             the new {@code Consumer} only applies the given {@code Predicate}
	 * @return null if both the {@code Consumer} and the {@code Predicate} are null
	 *         or a new Consumer otherwise
	 */
	public static <T> Consumer<T> callIf(Predicate<? super T> pred, Consumer<? super T> proc) {
		if (pred == null && proc == null) {
			return null;
		} else if (pred == null) {
			return proc::accept;
		} else if (proc == null) {
			return pred::test;
		}
		return t -> {
			if (pred.test(t)) {
				proc.accept(t);
			}
		};
	}

	/**
	 * Creates a {@code BiConsumer} by calling a given {@code Consumer} on the
	 * output of a given {@code BiFunction}.
	 * 
	 * @param func is called first, not null
	 * @param proc is called on {@code func}'s output, not null
	 * @return
	 */
	public static <T, U, V> BiConsumer<T, U> biConcatC(BiFunction<? super T, ? super U, V> func,
			Consumer<? super V> proc) {
		return (t, u) -> proc.accept(func.apply(t, u));
	}

	/**
	 * Creates a {@code Consumer<T>} out of a {@code Consumer<U>} by applying a
	 * given {@code Function<T,U>} first.
	 * 
	 * @param func the converter, not null
	 * @param proc the action, not null
	 * @return
	 */
	public static <T, U> Consumer<T> concatC(Function<? super T, U> func,
			Consumer<? super U> proc) {
		return t -> proc.accept(func.apply(t));
	}

	/**
	 * Creates a {@code Consumer<R>} out of a {@code BiConsumer<T, U>} by applying
	 * given two converters to the input first.
	 * 
	 * @param func1 the converter which produces the first argument in {@code proc},
	 *              not null
	 * @param func2 the converter which produces the second argument in
	 *              {@code proc}, not null
	 * @param proc  the action, not null
	 * @return
	 */
	public static <T, U, R> Consumer<R> concatC(Function<? super R, T> func1,
			Function<? super R, U> func2, BiConsumer<? super T, ? super U> proc) {
		return r -> proc.accept(func1.apply(r), func2.apply(r));
	}

	/**
	 * Creates a {@code Function<R, V>} out of a {@code BiFunction<T, U, V>} by
	 * applying given two converters to the input first.
	 * 
	 * @param func1 the converter which produces the first argument in {@code func},
	 *              not null
	 * @param func2 the converter which produces the second argument in
	 *              {@code func}, not null
	 * @param func  the BiFunction, not null
	 * @return
	 */
	public static <T, U, V, R> Function<R, V> concatF(Function<? super R, T> mod1,
			Function<? super R, U> mod2, BiFunction<? super T, ? super U, V> func) {
		return r -> func.apply(mod1.apply(r), mod2.apply(r));
	}

	/**
	 * Creates a {@code Function<T, V>} by concatenating a {@code Function<T, U>}
	 * and a {@code Function<U, V>}.
	 * 
	 * @param func1 the first function, not null
	 * @param func2 the second function, not null
	 * @return
	 */
	public static <T, U, V> Function<T, V> concatFF(Function<? super T, ? extends U> func1,
			Function<? super U, ? extends V> func2) {
		return func1.andThen(func2)::apply;
	}

	/**
	 * Creates a {@code Function} which calls a given {@code Consumer} prior to
	 * apply a given {@code Function}.
	 * 
	 * @param proc the Consumer. If null, only the Function is called
	 * @param func the Function, not null
	 * @return
	 */
	public static <T, U> Function<T, U> concatF(Consumer<T> proc, Function<T, U> func) {
		return proc == null ? func : t -> {
			proc.accept(t);
			return func.apply(t);
		};
	}

	/**
	 * Creates a {@code BiFunction} which calls a given {@code BiConsumer} prior to
	 * applying a given {@code BiFunction}.
	 * 
	 * @param proc the BiConsumer. If null, only the BiFunction is called
	 * @param func the BiFunction, not null
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> concatF(BiConsumer<T, U> proc,
			BiFunction<T, U, V> func) {
		return proc == null ? func : (t, u) -> {
			proc.accept(t, u);
			return func.apply(t, u);
		};
	}

	///////////////////////////////////////////////////////
	/// ************* Supplier conversion ************* ///
	///////////////////////////////////////////////////////
	/**
	 * Creates a {@code Supplier} which converts a given {@code Supplier}'s value by
	 * applying a given {@code Function} to it. If the return value is null, the
	 * conversion function is ignored.
	 * 
	 * @param src  the Supplier, may be null
	 * @param func the conversion function, not null
	 * @return
	 */
	public static <T, U> Supplier<U> convert(Supplier<T> src, Function<T, U> func) {
		return src == null ? null : () -> applyIf(s -> s != null, func).apply(src.get());
	}

	/**
	 * Creates a {@code Supplier} which calls a given {@code Runnable} prior to
	 * calling a given {@code Supplier}.
	 * 
	 * @param runnable the code to run, may be null
	 * @param src      the Supplier, not null
	 * @return
	 */
	public static <T> Supplier<T> concatS(Runnable r, Supplier<T> src) {
		return r == null ? src : () -> {
			r.run();
			return src.get();
		};
	}

	///////////////////////////////////////////////////////
	/// ************** Unary to Binary **************** ///
	///////////////////////////////////////////////////////
	/**
	 * Creates a {@code BiFunction} by applying a given {@code Function} to the
	 * second argument and totally ignoring the first one.
	 * 
	 * @param func the Function to apply. Null returns null.
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> ignore1(Function<U, V> func) {
		return func == null ? null : (t, u) -> func.apply(u);
	}

	/**
	 * Creates a {@code BiFunction} by applying a given {@code Function} to the
	 * first argument and totally ignoring the second one.
	 * 
	 * @param func the Function to apply. Null returns null.
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> ignore2(Function<T, V> func) {
		return func == null ? null : (t, u) -> func.apply(t);
	}

	/**
	 * Creates a {@code BiConsumer} by applying a given {@code Consumer} to the
	 * second argument and totally ignoring the first one.
	 * 
	 * @param proc the Function to apply. Null returns null.
	 * @return
	 */
	public static <T, U> BiConsumer<T, U> ignore1(Consumer<U> proc) {
		return proc == null ? null : (t, u) -> proc.accept(u);
	}

	/**
	 * Creates a {@code BiConsumer} by applying a given {@code Consumer} to the
	 * first argument and totally ignoring the second one.
	 * 
	 * @param proc the Function to apply. Null returns null.
	 * @return
	 */
	public static <T, U> BiConsumer<T, U> ignore2(Consumer<T> proc) {
		return proc == null ? null : (t, u) -> proc.accept(t);
	}

	/**
	 * Creates a {@code BiPredicate} by applying a given {@code Predicate} to the
	 * second argument and totally ignoring the first one.
	 * 
	 * @param pred the Predicate to apply. Null returns null.
	 * @return
	 */
	public static <T, U> BiPredicate<T, U> ignore1(Predicate<U> pred) {
		return pred == null ? null : (t, u) -> pred.test(u);
	}

	/**
	 * Creates a {@code BiPredicate} by applying a given {@code Predicate} to the
	 * first argument and totally ignoring the second one.
	 * 
	 * @param pred the Predicate to apply. Null returns null.
	 * @return
	 */
	public static <T, U> BiPredicate<T, U> ignore2(Predicate<T> pred) {
		return pred == null ? null : (t, u) -> pred.test(t);
	}

	/**
	 * Creates a {@code BinaryOperator} by applying a given {@code UnaryOperator} to
	 * the second argument and totally ignoring the first one.
	 * 
	 * @param op the UnaryOperator to apply. Null returns null.
	 * @return
	 */
	public static <T> BinaryOperator<T> ignore1(UnaryOperator<T> op) {
		return op == null ? null : (t, u) -> op.apply(u);
	}

	/**
	 * Creates a {@code BinaryOperator} by applying a given {@code UnaryOperator} to
	 * the first argument and totally ignoring the second one.
	 * 
	 * @param op the UnaryOperator to apply. Null returns null.
	 * @return
	 */
	public static <T> BinaryOperator<T> ignore2(UnaryOperator<T> op) {
		return op == null ? null : (t, u) -> op.apply(t);
	}

	///////////////////////////////////////////////////////
	/// ******* Function<Boolean> <-> Predicate ******* ///
	///////////////////////////////////////////////////////

	/**
	 * Makes a {@code Predicate} out of a given {@code Function} returning boolean.
	 * 
	 * @param func the Function to convert. Null returns null
	 * @return
	 */
	public static <T> Predicate<T> toPred(Function<T, Boolean> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Makes a {@code BiPredicate} out of a given {@code BiFunction} returning
	 * boolean.
	 * 
	 * @param func the BiFunction to convert. Null returns null
	 * @return
	 */
	public static <T, U> BiPredicate<T, U> toPred(BiFunction<T, U, Boolean> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Makes a {@code Function} out of a given {@code Predicate}.
	 * 
	 * @param func the Predicate to convert. Null returns null
	 * @return
	 */
	public static <T> Function<T, Boolean> toFunc(Predicate<T> pred) {
		return pred == null ? null : pred::test;
	}

	/**
	 * Makes a {@code BiFunction} out of a given {@code BiPredicate}.
	 * 
	 * @param func the BiPredicate to convert. Null returns null
	 * @return
	 */
	public static <T, U> BiFunction<T, U, Boolean> toFunc(BiPredicate<T, U> pred) {
		return pred == null ? null : pred::test;
	}

	///////////////////////////////////////////////////////
	/// ************ Function <-> Consumer ************ ///
	///////////////////////////////////////////////////////
	/**
	 * Creates a {@code Consumer} out of a given {@code Function} by just ignoring
	 * its return value
	 * 
	 * @param func the Function to convert. Null returns null
	 * @return
	 */
	public static <T, U> Consumer<T> toProc(Function<T, U> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Creates a {@code BiConsumer} out of a given {@code BiFunction} by just
	 * ignoring its return value
	 * 
	 * @param func the BiFunction to convert. Null returns null
	 * @return
	 */
	public static <T, U, V> BiConsumer<T, U> toProc(BiFunction<T, U, V> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Makes a Function which returns a fixed value after calling a given
	 * {@code Consumer}.
	 * 
	 * @param proc  the Consumer to call. If null, just returns a given value
	 * @param value the value to return, may be null
	 * @return
	 */
	public static <T, U> Function<T, U> toFunc(Consumer<T> proc, U value) {
		return concatF(proc, constant(value));
	}

	/**
	 * Makes a BiFunction which returns a fixed value after calling a given
	 * {@code BiConsumer}.
	 * 
	 * @param proc  the BiConsumer to call. If null, just returns a given value
	 * @param value the value to return, may be null
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> toFunc(BiConsumer<T, U> proc, V value) {
		return concatF(proc, biConstant(value));
	}

	/**
	 * Returns a constant {@code Supplier}, i.e., a Supplier which always returns a
	 * given value.
	 * 
	 * @param value the value to return, may be null
	 * @return
	 */
	public static <U> Supplier<U> def(U value) {
		return () -> value;
	}

	/**
	 * Returns a constant {@code Function}, i.e., a Function which always returns a
	 * given value.
	 * 
	 * @param value the value to return, may be null
	 * @return
	 */
	public static <T, U> Function<T, U> constant(U value) {
		return t -> value;
	}

	/**
	 * Returns a constant {@code BiFunction}, i.e., a Function which always returns
	 * a given value.
	 * 
	 * @param value the value to return, may be null
	 * @return
	 */
	public static <T, U, V> BiFunction<T, U, V> biConstant(V value) {
		return (t, u) -> value;
	}

	///////////////////////////////////////////////////////
	/// ************ Function <-> Operator ************ ///
	///////////////////////////////////////////////////////
	/**
	 * Makes an {@code UnaryOperator} out of a given {@code Function}.
	 * 
	 * @param func the Function to convert. Null returns null
	 * @return
	 */
	public static <T> UnaryOperator<T> toOp(Function<T, T> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Makes a {@code BinaryOperator} out of a given {@code BiFunction}.
	 * 
	 * @param func the Function to convert. Null returns null
	 * @return
	 */
	public static <T, U> BinaryOperator<T> toOp(BiFunction<T, T, T> func) {
		return func == null ? null : func::apply;
	}

	/**
	 * Makes a {@code Function} out of a given {@code UnaryOperator}.
	 * 
	 * @param op the UnaryOperator to convert. Null returns null
	 * @return
	 */
	public static <T> Function<T, T> toFunc(UnaryOperator<T> op) {
		return op == null ? null : op::apply;
	}

	/**
	 * Makes a {@code BiFunction} out of a given {@code BinaryOperator}.
	 * 
	 * @param op the BinaryOperator to convert. Null returns null
	 * @return
	 */
	public static <T, U> BiFunction<T, T, T> toFunc(BinaryOperator<T> op) {
		return op == null ? null : op::apply;
	}

	///////////////////////////////////////////////////////
	/// *********** Iterable <-> BiIterable *********** ///
	///////////////////////////////////////////////////////
	/**
	 * Creates a {@code Consumer} accepting {@code Entry<T,U>} out of a given
	 * {@code BiConsumer} accepting {@code T} and {@code U} as arguments.
	 * 
	 * @param proc the BiConsumer to convert. Null returns null
	 * @return
	 */
	public static <T, U> Consumer<Entry<T, U>> toKV(BiConsumer<? super T, ? super U> proc) {
		return proc == null ? null : concatC(Entry::getKey, Entry::getValue, proc);
	}

	/**
	 * Creates a {@code Function} accepting {@code Entry<T,U>} out of a given
	 * {@code BiFunction} accepting {@code T} and {@code U} as arguments.
	 * 
	 * @param func the BiFunction to convert. Null returns null
	 * @return
	 */
	public static <T, U, V> Function<Entry<T, U>, V> toKV(
			BiFunction<? super T, ? super U, V> func) {
		return func == null ? null : concatF(Entry::getKey, Entry::getValue, func);
	}

	/**
	 * Creates a {@code Predicate} accepting {@code Entry<T,U>} out of a given
	 * {@code BiPredicate} accepting {@code T} and {@code U} as arguments.
	 * 
	 * @param pred the BiPredicate to convert. Null returns null
	 * @return
	 */
	public static <T, U, V> Predicate<Entry<T, U>> toKV(BiPredicate<? super T, ? super U> pred) {
		return pred == null ? null : toPred(toKV(toFunc(pred)));
	}

	/**
	 * Creates a {@code Function} returning {@code Entry<T,U>} out of two
	 * {@code Function}s which return {@code T} and {@code U}, respectively.
	 * 
	 * @param func1 the first Function, not null
	 * @param func2 the second Function, not null
	 * @return
	 */
	public static <T, V, W> Function<T, Entry<V, W>> toKV(Function<? super T, V> func1,
			Function<? super T, W> func2) {
		return concatF(func1, func2, KeyValue<V, W>::new);
	}

	///////////////////////////////////////////////////////
	/// ************ Miscellaneous Methods ************ ///
	///////////////////////////////////////////////////////
	/**
	 * Reduces all non-null elements to a single element by means of a given
	 * accumulator. <br>
	 * The documented usage is to join multiple Consumers or BiConsumers.
	 * 
	 * @see Stream#reduce(BinaryOperator)
	 * @param accumulator
	 * @param elems       the elements to join together, null are ignored.
	 * @return null if all elements are null or the first non-null element if it is
	 *         the only one in the array. Otherwise, the result of the last call to
	 *         {@code accumulator}.
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> U reduceNonNulls(BiFunction<U, T, U> accumulator,
			Iterable<? extends T> elems) {
		var filtered = list(filtered(elems, t -> t != null));
		if (filtered.isEmpty()) {
			return null;
		}
		return reduce(t -> (U) t, accumulator, filtered);
	}

	/**
	 * Reduces all non-null elements to a single element by means of a given
	 * initializator (to initialize the result from the first element) and a given
	 * accumulator (to update the result). <br>
	 * Compare to {@link Stream#reduce(Object, BinaryOperator)}. The difference is
	 * that the Stream-version needs an explicit identity element provided and poses
	 * additional constraint on this element (in order to parallelize correctly). We
	 * perform the reduction sequentially and can generate the identity in place.
	 * 
	 * @param initializer
	 * @param accumulator
	 * @param elems
	 * @return
	 */
	public static <T, U> U reduce(Function<T, U> initializer, BiFunction<U, T, U> accumulator,
			Iterable<? extends T> elems) {
		// initialize the first element
		Iterator<? extends T> it = elems.iterator();
		U result = null;

		// initialize with the first element
		if (it.hasNext()) {
			T elem = it.next();
			result = initializer.apply(elem);
		} else {
			error("Cannot pass empty iterable to reduce");
		}

		// ... iterate over the rest
		while (it.hasNext()) {
			T elem = it.next();
			result = accumulator.apply(result, elem);
		}

		return result;
	}

	/**
	 * Throws an {@code IllegalArgumentException} with a given message.
	 * 
	 * @param message
	 * @return
	 */
	public static <T> T error(String message) {
		throw new IllegalArgumentException(message);
	}

	public static <U> U checkNotNull(U value, String name) {
		if (value == null) {
			error("Argument '" + name + "' cannot be null");
		}
		return value;
	}

}

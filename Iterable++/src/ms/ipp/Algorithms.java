package ms.ipp;

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

public class Algorithms {
	/// **** Function concatenators **** ///

	@SafeVarargs
	public static <T, U> BiPredicate<T, U> and(BiPredicate<? super T, ? super U>... predicates) {
		return accumulateNonNulls((p1, p2) -> p1.and(p2), predicates);
	}

	@SafeVarargs
	public static <T> Predicate<T> and(Predicate<? super T>... predicates) {
		return accumulateNonNulls((p1, p2) -> p1.and(p2), predicates);
	}

	@SafeVarargs
	public static <T, U> BiConsumer<T, U> seq(BiConsumer<? super T, ? super U>... predicates) {
		return accumulateNonNulls((p1, p2) -> p1.andThen(p2), predicates);
	}

	@SafeVarargs
	public static <T> Consumer<T> seq(Consumer<? super T>... predicates) {
		return accumulateNonNulls((p1, p2) -> p1.andThen(p2), predicates);
	}

	public static <T, U, V> BiFunction<T, U, V> applyIf(BiPredicate<? super T, ? super U> pred,
			BiFunction<? super T, ? super U, V> function) {
		return (t, u) -> pred.test(t, u) ? function.apply(t, u) : null;
	}

	public static <T, U> Function<T, U> applyIf(Predicate<? super T> pred, Function<? super T, U> function) {
		return t -> pred.test(t) ? function.apply(t) : null;
	}

	public static <T> Consumer<T> callIf(Predicate<? super T> pred, Consumer<? super T> consumer) {
		if (pred == null && consumer == null) {
			return null;
		} else if (pred == null) {
			return consumer::accept;
		} else if (consumer == null) {
			return pred::test;
		}
		return t -> {
			if (pred.test(t)) {
				consumer.accept(t);
			}
		};
	}

	public static <T, U> BiConsumer<T, U> callIf(BiPredicate<? super T, ? super U> pred,
			BiConsumer<? super T, ? super U> consumer) {
		if (pred == null && consumer == null) {
			return null;
		} else if (pred == null) {
			return consumer::accept;
		} else if (consumer == null) {
			return pred::test;
		}
		return (t, u) -> {
			if (pred.test(t, u)) {
				consumer.accept(t, u);
			}
		};
	}

	public static <T, U> Consumer<T> concatC(Function<? super T, U> func, Consumer<? super U> cons) {
		return t -> cons.accept(func.apply(t));
	}

	public static <T, U, R> Consumer<R> concatC(Function<? super R, T> mod1, Function<? super R, U> mod2,
			BiConsumer<? super T, ? super U> proc) {
		return r -> proc.accept(mod1.apply(r), mod2.apply(r));
	}

	public static <T, U, V> BiConsumer<T, U> biConcatC(BiFunction<? super T, ? super U, V> func,
			Consumer<? super V> cons) {
		return (t, u) -> cons.accept(func.apply(t, u));
	}

	public static <T, U, V, R> Function<R, V> concatF(Function<? super R, T> mod1, Function<? super R, U> mod2,
			BiFunction<? super T, ? super U, V> func) {
		return r -> func.apply(mod1.apply(r), mod2.apply(r));
	}

	public static <T, U> Function<T, U> concatF(Consumer<T> proc, Function<T, U> func) {
		return proc == null ? func : t -> {
			proc.accept(t);
			return func.apply(t);
		};
	}

	public static <T, U, V> BiFunction<T, U, V> concatF(BiConsumer<T, U> proc, BiFunction<T, U, V> func) {
		return proc == null ? func : (t, u) -> {
			proc.accept(t, u);
			return func.apply(t, u);
		};
	}

	public static <T, U, R> Predicate<R> concatP(Function<? super R, T> mod1, Function<? super R, U> mod2,
			BiPredicate<? super T, ? super U> pred) {
		return r -> pred.test(mod1.apply(r), mod2.apply(r));
	}

	public static <T, U> U ignoreIfNullOrPred(T source, Predicate<? super T> condition,
			Function<? super T, U> processor) {
		if (source == null || (condition != null && condition.test(source))) {
			return null;
		}
		return processor.apply(source);
	}
	/// **** Augment to Binary functional from Unary functional **** ///

	public static <T, U, V> BiFunction<T, U, V> ignore1(Function<U, V> func) {
		return func == null ? null : (t, u) -> func.apply(u);
	}

	public static <T, U, V> BiFunction<T, U, V> ignore2(Function<T, V> func) {
		return func == null ? null : (t, u) -> func.apply(t);
	}

	public static <T, U> BiConsumer<T, U> ignore1(Consumer<U> func) {
		return func == null ? null : (t, u) -> func.accept(u);
	}

	public static <T, U> BiConsumer<T, U> ignore2(Consumer<T> func) {
		return func == null ? null : (t, u) -> func.accept(t);
	}

	public static <T, U> BiPredicate<T, U> ignore1(Predicate<U> func) {
		return func == null ? null : (t, u) -> func.test(u);
	}

	public static <T, U> BiPredicate<T, U> ignore2(Predicate<T> func) {
		return func == null ? null : (t, u) -> func.test(t);
	}

	public static <T> BinaryOperator<T> ignore1(UnaryOperator<T> func) {
		return func == null ? null : (t, u) -> func.apply(u);
	}

	public static <T> BinaryOperator<T> ignore2(UnaryOperator<T> func) {
		return func == null ? null : (t, u) -> func.apply(t);
	}

	/// **** Natural conversion routines Func<Bool> <-> Pred **** ///
	public static <T> Predicate<T> toPred(Function<T, Boolean> func) {
		return func == null ? null : func::apply;
	}

	public static <T, U> BiPredicate<T, U> toPred(BiFunction<T, U, Boolean> func) {
		return func == null ? null : func::apply;
	}

	public static <T> Function<T, Boolean> toFunc(Predicate<T> pred) {
		return pred == null ? null : pred::test;
	}

	public static <T, U> BiFunction<T, U, Boolean> toFunc(BiPredicate<T, U> pred) {
		return pred == null ? null : pred::test;
	}

	/// **** Natural conversion routines Func -> Consumer **** ///
	public static <T, U> Consumer<T> toProc(Function<T, U> func) {
		return func == null ? null : func::apply;
	}

	public static <T, U, V> BiConsumer<T, U> toProc(BiFunction<T, U, V> func) {
		return func == null ? null : func::apply;
	}

	public static <T, U> Function<T, U> toFunc(Consumer<T> proc, U value) {
		return concatF(proc, constant(value));
	}

	public static <T, U, V> BiFunction<T, U, V> toFunc(BiConsumer<T, U> proc, V value) {
		return concatF(proc, biConstant(value));
	}

	public static <T, U> Function<T, U> constant(U value) {
		return t -> value;
	}

	public static <T, U, V> BiFunction<T, U, V> biConstant(V value) {
		return (t, u) -> value;
	}

	/// **** Natural conversion routines Func <-> Operator **** ///
	public static <T> UnaryOperator<T> toOp(Function<T, T> func) {
		return func == null ? null : func::apply;
	}

	public static <T, U> BinaryOperator<T> toOp(BiFunction<T, T, T> func) {
		return func == null ? null : func::apply;
	}

	public static <T> Function<T, T> toFunc(UnaryOperator<T> op) {
		return op == null ? null : op::apply;
	}

	public static <T, U> BiFunction<T, T, T> toFunc(BinaryOperator<T> op) {
		return op == null ? null : op::apply;
	}

	//// **** Miscellaneous methods **** ///
	public static <T, U> Consumer<Entry<T, U>> toKV(BiConsumer<? super T, ? super U> proc) {
		return proc == null ? null : concatC(Entry::getKey, Entry::getValue, proc);
	}

	public static <T, V, W> Function<T, Entry<V, W>> toKV(Function<? super T, V> func1, Function<? super T, W> func2) {
		return concatF(func1, func2, KeyValue<V, W>::new);
	}

	public static <T, U, V> Function<Entry<T, U>, V> toKV(BiFunction<? super T, ? super U, V> func) {
		return func == null ? null : concatF(Entry::getKey, Entry::getValue, func);
	}

	public static <T, U, V> Predicate<Entry<T, U>> toKV(BiPredicate<? super T, ? super U> func) {
		return func == null ? null : toPred(toKV(toFunc(func)));
	}

	/**
	 * Reduces all non-null elements to a single element by means of a given
	 * accumulator. Compare to {@link Stream#reduce(BinaryOperator)} <br>
	 * The intended usage is to join multiple Consumers or BiConsumers.
	 * 
	 * @param accumulator
	 * @param elems
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> U accumulateNonNulls(BiFunction<U, T, U> accumulator, T... elems) {
		U result = null;
		for (T elem : elems) {
			if (elem == null) {
				continue;
			}
			if (result == null) {
				result = (U) elem;
			} else {
				result = accumulator.apply(result, elem);
			}
		}

		return result;
	}

	public static <U> Supplier<U> def(U value) {
		return () -> value;
	}

	public static <T> T disabled(String message) {
		throw new IllegalArgumentException(message);
	}

}

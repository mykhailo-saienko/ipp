package ms.ipp;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class comprises a collection of most-widely-used queries on Streams,
 * such as finding the first element with given conditions (and automatically
 * checking that the selected element is the only one with those conditions),
 * checking if none, any or all elements satisfy a condition or re-packing
 * elements into a different type of {@code Collection}.
 * 
 * @author mykhailo.saienko
 *
 */
public class Streams {
	/**
	 * Returns true if all elements in the stream satisfy a given {@code Predicate}.
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, true is returned.
	 * @return
	 */
	public static <T> boolean all(Stream<T> st, Predicate<? super T> pred) {
		return pred == null ? true : !any(st, pred.negate());
	}

	/**
	 * Returns true if none of the elements in the stream satisfy a given
	 * {@code Predicate}
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, returns true is the stream is empty
	 * @return
	 */
	public static <T> boolean none(Stream<T> st, Predicate<? super T> pred) {
		return !any(st, pred);
	}

	/**
	 * Returns true if at least one of the elements in the stream satisfies a given
	 * {@code Predicate}
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, returns true if stream is not empty.
	 * @return
	 */
	public static <T> boolean any(Stream<T> it, Predicate<? super T> pred) {
		return first(it, pred) != null;
	}

	/**
	 * If there is exactly one item in the {@code Iterable} which satisfies a given
	 * {@code Predicate}, returns the element. If there are no items satisfying the
	 * {@code Predicate}, returns null. If there are more than one items, throws
	 * {@link IllegalArgumentException}.
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, the original stream is used
	 * @return
	 */
	public static <T> T unique(Stream<T> st, Predicate<? super T> pred) {
		if (st == null) {
			return null;
		}
		return filtered(st, pred).reduce((i1, i2) -> {
			throw new IllegalArgumentException("At least two elements encountered: [" + i1 + ", " + i2 + "]");
		}).orElse(null);
	}

	/**
	 * Returns the first element encountered in the stream which satisfies a given
	 * {@code Predicate}
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate, null is interpreted as a condition which always
	 *             returns true
	 * @return
	 */
	public static <T> T first(Stream<T> st, Predicate<? super T> pred) {
		if (st == null) {
			return null;
		}
		return filtered(st, pred).findAny().orElse(null);
	}

	/**
	 * Performs a given action on all elements from the stream which satisfy a given
	 * {@code Predicate}.
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, the original stream is used
	 * @param proc the action to perform on eligible elements, not null
	 */
	public static <T> void forEach(Stream<T> st, Predicate<? super T> pred, Consumer<? super T> proc) {
		if (st != null) {
			filtered(st, pred).forEach(proc);
		}
	}

	/**
	 * Collects all elements from the stream which satisfy a given {@code Predicate}
	 * by using a given {@link Collector}.
	 * 
	 * @param st        the original stream, not null
	 * @param pred      the predicate. If null, the original stream is used
	 * @param collector the Collector, as it is used by @{code Stream::collect}
	 *                  methods
	 */
	public static <T, U extends T, R> R collect(Stream<U> it, Predicate<? super U> pred, Collector<T, ?, R> collector) {
		if (it == null) {
			return null;
		}
		return filtered(it, pred).collect(collector);
	}

	/**
	 * Collects at most the first {@code n} elements from the stream which satisfy a
	 * given {@code Predicate} by using a given {@link Collector}.
	 * 
	 * @param st        the original stream, not null
	 * @param pred      the predicate. If null, the original stream is used
	 * @param collector the Collector, as it is used by @{code Stream::collect}
	 *                  methods.
	 * @param n         the number of elements to collect
	 */
	public static <T, U extends T, R> R collect(Stream<U> st, Predicate<? super U> pred, Collector<T, ?, R> collector,
			int n) {
		if (st == null) {
			return null;
		}
		return filtered(st, pred).limit(n).collect(collector);
	}

	/**
	 * Collects all elements from the stream which satisfy a given {@code Predicate}
	 * to a {@code List}.
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, the original stream is used
	 * @return
	 */
	public static <T, U extends T> List<T> list(Stream<U> st, Predicate<? super U> pred) {
		return collect(st, pred, Collectors.toList());
	}

	/**
	 * Collects all elements from the stream which satisfy a given {@code Predicate}
	 * to a {@code Set}.
	 * 
	 * @param st   the original stream, not null
	 * @param pred the predicate. If null, the original stream is used.
	 * @return
	 */
	public static <T, U extends T> Set<T> set(Stream<U> st, Predicate<? super U> pred) {
		return collect(st, pred, Collectors.toSet());
	}

	/**
	 * Returns a {@code Stream} which filters out elements that don't satisfy a
	 * given {@code Predicate}.
	 * 
	 * @param st   the original stream, not null.
	 * @param pred the predicate. If null, the original stream is returned.
	 * @return
	 */
	public static <T> Stream<T> filtered(Stream<T> st, Predicate<? super T> pred) {
		return pred == null ? st : st.filter(pred);
	}

	/**
	 * Returns a {@code Stream} which maps elements by means of a given
	 * {@code Function}.
	 * 
	 * @param st  the original stream, not null
	 * @param map the mapper-function, not null
	 * @return
	 */
	public static <T, U> Stream<U> mapped(Stream<T> st, Function<T, U> map) {
		if (map == null) {
			throw new IllegalArgumentException("Mapper cannot be null");
		}
		return st.map(map);
	}

	public static <T> Stream<T> stream(Collection<T> it) {
		return it == null ? null : it.stream();
	}

	public static <T> Stream<T> stream(Iterable<T> it) {
		return it == null ? null : StreamSupport.stream(it.spliterator(), false);
	}
}

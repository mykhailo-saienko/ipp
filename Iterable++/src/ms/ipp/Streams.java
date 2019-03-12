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

public class Streams {
	public static <T> boolean all(Stream<T> st, Predicate<? super T> pred) {
		return !any(st, pred.negate());
	}

	public static <T> boolean none(Stream<T> st, Predicate<? super T> pred) {
		return !any(st, pred);
	}

	public static <T> boolean any(Stream<T> it, Predicate<? super T> pred) {
		return first(it, pred) != null;
	}

	public static <T> T unique(Stream<T> st, Predicate<? super T> pred) {
		if (st == null) {
			return null;
		}
		return filtered(st, pred).reduce((i1, i2) -> {
			throw new IllegalArgumentException("At least two elements encountered: [" + i1 + ", " + i2 + "]");
		}).orElse(null);
	}

	public static <T> T first(Stream<T> st, Predicate<? super T> pred) {
		if (st == null) {
			return null;
		}
		return filtered(st, pred).findAny().orElse(null);
	}

	public static <T> void forEach(Stream<T> st, Predicate<? super T> pred, Consumer<? super T> proc) {
		if (st != null) {
			filtered(st, pred).forEach(proc);
		}
	}

	public static <T, U extends T, R> R collect(Stream<U> it, Predicate<? super U> pred, Collector<T, ?, R> collector) {
		if (it == null) {
			return null;
		}
		return filtered(it, pred).collect(collector);
	}

	public static <T, U extends T, R> R collect(Stream<U> it, Predicate<? super U> pred, Collector<T, ?, R> collector,
			int n) {
		if (it == null) {
			return null;
		}
		return filtered(it, pred).limit(n).collect(collector);
	}

	public static <T, U extends T> List<T> list(Stream<U> it, Predicate<? super U> pred) {
		return collect(it, pred, Collectors.toList());
	}

	public static <T, U extends T> Set<T> set(Stream<U> it, Predicate<? super U> pred) {
		return collect(it, pred, Collectors.toSet());
	}

	public static <T> Stream<T> filtered(Stream<T> st, Predicate<? super T> pred) {
		return pred == null ? st : st.filter(pred);
	}

	public static <T, U> Stream<U> mapped(Stream<T> st, Function<T, U> func) {
		if (func == null) {
			throw new IllegalArgumentException("Mapper cannot be null");
		}
		return st.map(func);
	}

	public static <T> Stream<T> stream(Collection<T> it) {
		return it == null ? null : it.stream();
	}

	public static <T> Stream<T> stream(Iterable<T> it) {
		return it == null ? null : StreamSupport.stream(it.spliterator(), false);
	}
}

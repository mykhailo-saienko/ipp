package ms.ipp.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filtering iterator with the ability to remove filtered elements. <br>
 * Essentially copied from
 * https://commons.apache.org/proper/commons-collections/jacoco/org.apache.commons.collections4.iterators/FilterIterator.java.html
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 */
public class FilteredIterator<T> implements Iterator<T> {
	private final Predicate<? super T> filter;
	private final Iterator<T> source;
	private T nextValid;
	private boolean nextSet;

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> t != null && seen.add(keyExtractor.apply(t));
	}

	public static <T> FilteredIterator<T> distinct(Iterator<T> source, Function<? super T, ?> keyExtractor) {
		return new FilteredIterator<>(source, distinctByKey(keyExtractor));
	}

	public FilteredIterator(Iterator<T> source, Predicate<? super T> filter) {
		if (filter == null) {
			filter = t -> true;
		}
		this.filter = filter;
		this.source = source;
	}

	@Override
	public boolean hasNext() {
		return nextSet || findNextValid();
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		nextSet = false;
		return nextValid;
	}

	/**
	 * Removes from the underlying collection of the base iterator the last element
	 * returned by this iterator. This method can only be called if
	 * <code>next()</code> was called, but not after <code>hasNext()</code>, because
	 * the <code>hasNext()</code> call changes the base iterator.
	 *
	 * @throws IllegalStateException if <code>hasNext()</code> has already been
	 *                               called.
	 */
	@Override
	public void remove() {
		if (nextSet) {
			throw new IllegalStateException("remove() can only be called after next() and before hasNext()");
		}
		source.remove();
	}

	private boolean findNextValid() {
		while (source.hasNext()) {
			T nextTempValid = source.next();
			if (filter.test(nextTempValid)) {
				nextSet = true;
				nextValid = nextTempValid;
				return true;
			}
		}
		return false;
	}
}
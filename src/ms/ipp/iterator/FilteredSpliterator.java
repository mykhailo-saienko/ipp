package ms.ipp.iterator;

import static ms.ipp.Algorithms.callIf;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A <i>Decorator</i> for an {@code Spliterator<T>} which only shows those
 * elements, for which a given filter returns true.
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 */
public class FilteredSpliterator<T> implements Spliterator<T> {

	private final Spliterator<T> source;
	private final Predicate<? super T> filter;

	/**
	 * Creates an instance of {@code FilteredSpliterator<T>} based on another
	 * {@code Spliterator<T>} and a given filter.
	 * 
	 * @param source the original Spliterator, not null
	 * @param filter the filter, not null.
	 */
	public FilteredSpliterator(Spliterator<T> source, Predicate<? super T> filter) {
		this.source = source;
		this.filter = filter;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		return source.tryAdvance(callIf(filter, action));
	}

	@Override
	public Spliterator<T> trySplit() {
		return new FilteredSpliterator<>(source.trySplit(), filter);
	}

	@Override
	public long estimateSize() {
		return source.estimateSize();
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		source.forEachRemaining(callIf(filter, action));
	}

	@Override
	public long getExactSizeIfKnown() {
		return -1; // filter distorts size
	}

	@Override
	public int characteristics() {
		// filter distorts size -> we cannot estimate it
		return source.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED;
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return source.hasCharacteristics(characteristics);
	}

	@Override
	public Comparator<? super T> getComparator() {
		return source.getComparator();
	}
}

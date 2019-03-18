package ms.ipp.iterable;

import static ms.ipp.Algorithms.callIf;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import ms.ipp.iterator.CustomDeleteIterator;
import ms.ipp.iterator.FilteredIterator;
import ms.ipp.iterator.FilteredSpliterator;

/**
 * A <i>Decorator</i> for an {@code Iterable<T>} which only shows those
 * elements, for which a given filter returns true. The class also accepts an
 * onDelete-hook which is passed on to the iterators it creates (see
 * {@link CustomDeleteIterator} for more details).
 * 
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 */
public class FilteredIterable<T> implements Iterable<T> {

	private final Iterable<T> source;

	private Supplier<Predicate<? super T>> filter;
	private Supplier<Consumer<? super T>> onDelete;

	public FilteredIterable(Iterable<T> source) {
		this.source = source;
	}

	/**
	 * Sets the filter supplier.<br>
	 * <b>NOTE:</b> The class does not accept Predicates directly, as filters may be
	 * stateful (for example,
	 * {@link FilteredIterator#distinctByKey(java.util.function.Function)
	 * FilteredIterator::distinctByKey(..)} creates such a filter). Hence, every
	 * created iterator must have its own copy of the filter.
	 * 
	 * @param filter
	 * @return
	 */
	public FilteredIterable<T> setFilter(Supplier<Predicate<? super T>> filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * Sets the onDelete-hook supplier.
	 * 
	 * @param onDelete
	 * @return
	 */
	public FilteredIterable<T> setOnDelete(Supplier<Consumer<? super T>> onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	/**
	 * Sets a simple onDelete-hook shared by all iterators.
	 * 
	 * @param onDelete
	 * @return
	 */
	public FilteredIterable<T> setOnDelete(Consumer<? super T> onDelete) {
		return setOnDelete(() -> onDelete);
	}

	/**
	 * If the filter is not null, returns a {@link FilteredIterator} with the
	 * filter. Additionally, decorates the resulting iterator with the the
	 * onDelete-hook if the latter is not null.
	 */
	@Override
	public Iterator<T> iterator() {
		if (source == null) {
			return null;
		}
		Iterator<T> it = source.iterator();
		if (filter != null) {
			it = new FilteredIterator<>(it, filter.get());
		}
		if (onDelete != null) {
			it = new CustomDeleteIterator<>(it, onDelete.get());
		}
		return it;
	}

	/**
	 * If the filter is not null, returns a {@link FilteredSpliterator} with the
	 * filter. Otherwise, returns the {@link Spliterator} created by the original
	 * {@code Iterable}.
	 */
	@Override
	public Spliterator<T> spliterator() {
		if (source == null) {
			return null;
		}
		Spliterator<T> sp = source.spliterator();
		if (filter != null) {
			sp = new FilteredSpliterator<>(sp, filter.get());
		}
		return sp;
	}

	/**
	 * Performs a given action for all elements for which the filter returns true.
	 */
	@Override
	public void forEach(Consumer<? super T> action) {
		if (source != null) {
			source.forEach(filter == null ? action : callIf(filter.get(), action));
		}
	}
}

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

public class FilteredIterable<T> implements Iterable<T> {

	private final Iterable<T> source;

	Supplier<Predicate<? super T>> filter;
	Supplier<Consumer<? super T>> onDelete;

	public FilteredIterable(Iterable<T> source) {
		this.source = source;
	}

	public FilteredIterable<T> setFilter(Supplier<Predicate<? super T>> filter) {
		this.filter = filter;
		return this;
	}

	public FilteredIterable<T> setOnDelete(Supplier<Consumer<? super T>> onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	public FilteredIterable<T> setOnDelete(Consumer<? super T> onDelete) {
		return setOnDelete(() -> onDelete);
	}

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

	@Override
	public void forEach(Consumer<? super T> action) {
		if (source != null) {
			source.forEach(filter == null ? action : callIf(filter.get(), action));
		}
	}
}

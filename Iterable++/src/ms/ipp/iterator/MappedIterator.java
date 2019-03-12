package ms.ipp.iterator;

import static ms.ipp.Algorithms.concatC;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MappedIterator<T, U> implements Iterator<U> {

	private final Iterator<T> source;
	private final Function<? super T, U> mapper;
	private Supplier<Boolean> onDelete;

	public MappedIterator(Iterator<T> source, Function<? super T, U> mapper) {
		this.source = source;
		this.mapper = mapper;
	}

	public MappedIterator<T, U> setOnDelete(Supplier<Boolean> onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public U next() {
		return mapper.apply(source.next());
	}

	@Override
	public void remove() {
		source.remove();
		if (onDelete != null) {
			onDelete.get();
		}
	}

	@Override
	public void forEachRemaining(Consumer<? super U> action) {
		source.forEachRemaining(concatC(mapper, action));
	}
}

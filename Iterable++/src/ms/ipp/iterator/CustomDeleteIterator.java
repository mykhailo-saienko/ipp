package ms.ipp.iterator;

import java.util.Iterator;
import java.util.function.Consumer;

public class CustomDeleteIterator<T> implements Iterator<T> {

	private final Iterator<T> source;
	private final Consumer<? super T> onDelete;
	private T lastRetrieved;

	public CustomDeleteIterator(Iterator<T> source, Consumer<? super T> onDelete) {
		this.source = source;
		this.onDelete = onDelete;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public T next() {
		return lastRetrieved = source.next();
	}

	@Override
	public void remove() {
		if (lastRetrieved == null) {
			throw new IllegalStateException();
		}
		onDelete.accept(lastRetrieved);
		source.remove();
		lastRetrieved = null;
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		source.forEachRemaining(action);
		lastRetrieved = null;
	}
}
package ms.ipp.iterator;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A <i>Decorator</i> for an {@code Iterator<T>} allowing to call a custom hook
 * immediately before a current element is deleted in the
 * {@code remove()}-method. The element is passed to the hook as the first and
 * the only argument.
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 */
public class CustomDeleteIterator<T> implements Iterator<T> {

	private final Iterator<T> source;
	private final Consumer<? super T> onDelete;
	private T lastRetrieved;

	/**
	 * Creates a Decorator for a given Iterator with a given onDelete-hook.
	 * 
	 * @param source   the original Iterator, not null
	 * @param onDelete the onDelete-hook, not null
	 */
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

	/**
	 * Calls a custom hook immediately before a current element is deleted in the
	 * {@code remove()}-method. The element is passed to the hook as the first and
	 * the only argument.
	 * 
	 */
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
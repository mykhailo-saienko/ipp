package ms.ipp.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator containing exactly one element.
 * 
 * @author mykhailo.saienko
 *
 * @param <F>
 */
public class OneIterator<F> implements Iterator<F> {
	private Runnable deleter;

	private final F elem;
	private boolean retrieved, deleted;

	/**
	 * Creates an iterator based on one element.
	 * 
	 * @param elem the source element, may be null
	 */
	public OneIterator(F elem) {
		this.elem = elem;
		this.retrieved = false;
		this.deleted = false;
	}

	/**
	 * Sets the function called in {@code remove()}.
	 * 
	 * @param deleter the deleter function, may be null. Setting null will make
	 *                {@code remove()} throw an
	 *                {@link UnsupportedOperationException}.
	 * @return
	 */
	public OneIterator<F> setDeleter(Runnable deleter) {
		this.deleter = deleter;
		return this;
	}

	@Override
	public boolean hasNext() {
		return !retrieved;
	}

	@Override
	public F next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		retrieved = true;
		return elem;
	}

	@Override
	public void remove() {
		if (deleter == null) {
			throw new UnsupportedOperationException("Deletion is not supported for " + elem);
		}
		if (deleted || !retrieved) {
			throw new IllegalStateException();
		}
		deleter.run();
		deleted = true;
	}
}
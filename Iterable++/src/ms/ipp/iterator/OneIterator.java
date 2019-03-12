package ms.ipp.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OneIterator<F> implements Iterator<F> {
	private Runnable deleter;

	private final F elem;
	private boolean retrieved;

	public OneIterator(F elem) {
		this.elem = elem;
		this.retrieved = false;
	}

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
			throw new IllegalStateException("Deletion is not supported for " + elem);
		}
		deleter.run();
	}
}
package ms.ipp.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This Iterator has no elements
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 */
public class EmptyIterator<T> implements Iterator<T> {

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		throw new NoSuchElementException();
	}

}

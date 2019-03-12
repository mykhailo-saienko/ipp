package ms.ipp.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class NestedIterator<T, U> implements Iterator<U> {

	private final Iterator<T> root;
	private final Function<T, Iterator<U>> gen;
	private Iterator<U> leaf;

	private U nextValid;
	private boolean nextSet;

	@SafeVarargs
	public static <T> Iterator<T> combined(Iterator<T>... its) {
		return new NestedIterator<>(Arrays.asList(its).iterator(), i -> i);
	}

	public static <T, U> NestedIterator<T, U> array(Iterator<T> it, Function<T, U[]> gen) {
		return new NestedIterator<T, U>(it, t -> Arrays.stream(gen.apply(t)).iterator());
	}

	/**
	 * 
	 * @param it
	 * @param gen Iterator generator for values returned by the primary iterator.
	 *            May return nulls.
	 */
	public NestedIterator(Iterator<T> it, Function<T, Iterator<U>> gen) {
		this.root = it;
		this.gen = gen;
		leaf = null;
	}

	@Override
	public boolean hasNext() {
		return nextSet || findNextValid();
	}

	@Override
	public U next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		nextSet = false;
		return nextValid;
	}

	@Override
	public void remove() {
		if (nextSet || leaf == null) {
			throw new IllegalStateException("remove() can only be called after next() and before hasNext()");
		}
		leaf.remove();
	}

	private boolean findNextValid() {
		while ((leaf == null || !leaf.hasNext()) && root.hasNext()) {
			leaf = gen.apply(root.next());
		}
		// we have encountered the last one
		if (leaf == null || !leaf.hasNext()) {
			return false;
		}
		nextSet = true;
		nextValid = leaf.next();
		return true;
	}
}
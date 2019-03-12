package ms.ipp.iterator;

import static ms.ipp.Algorithms.concatC;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class IteratorDelegate<T, R> implements Iterator<R> {

	private final Iterator<T> it;
	private final Function<? super T, R> convert;

	public IteratorDelegate(Iterator<T> it, Function<? super T, R> convert) {
		this.it = it;
		this.convert = convert;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public R next() {
		return convert.apply(it.next());
	}

	@Override
	public void remove() {
		it.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super R> action) {
		it.forEachRemaining(concatC(convert, action));
	}
}
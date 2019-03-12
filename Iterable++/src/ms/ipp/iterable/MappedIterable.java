package ms.ipp.iterable;

import static ms.ipp.Algorithms.concatC;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import ms.ipp.iterator.MappedIterator;
import ms.ipp.iterator.MappedSpliterator;

public class MappedIterable<T, R> implements Iterable<R> {

	private final Iterable<T> source;
	private final Function<? super T, R> mapper;

	public MappedIterable(Iterable<T> source, Function<? super T, R> mapper) {
		this.source = source;
		this.mapper = mapper;
	}

	@Override
	public Iterator<R> iterator() {
		return new MappedIterator<>(source.iterator(), mapper);
	}

	@Override
	public Spliterator<R> spliterator() {
		return new MappedSpliterator<>(source.spliterator(), mapper);
	}

	@Override
	public void forEach(Consumer<? super R> action) {
		source.forEach(concatC(mapper, action));
	}
}

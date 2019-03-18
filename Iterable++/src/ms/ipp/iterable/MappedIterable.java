package ms.ipp.iterable;

import static ms.ipp.Algorithms.concatC;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import ms.ipp.iterator.MappedIterator;
import ms.ipp.iterator.MappedSpliterator;

/**
 * A <i>Decorator</i> for an {@code Iterable<T>} which maps every element from T
 * to R by means of a given mapping function.
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 * @param <R>
 */
public class MappedIterable<T, R> implements Iterable<R> {

	private final Iterable<T> source;
	private final Function<? super T, R> mapper;

	/**
	 * Creates an instance of {@code MappedIterable<T,R>} based on another
	 * {@code Iterable<T>} and a given mapper from {@code T} to {@code R}.
	 * 
	 * @param source the original Iterable, not null
	 * @param mapper the mapper, not null
	 */
	public MappedIterable(Iterable<T> source, Function<? super T, R> mapper) {
		this.source = source;
		this.mapper = mapper;
	}

	/**
	 * Returns a {@link MappedIterator} with the mapper given in the constructor.
	 */
	@Override
	public Iterator<R> iterator() {
		return new MappedIterator<>(source.iterator(), mapper);
	}

	/**
	 * Returns a {@link MappedSpliterator} with the mapper given in the constructor.
	 */
	@Override
	public Spliterator<R> spliterator() {
		return new MappedSpliterator<>(source.spliterator(), mapper);
	}

	/**
	 * Performs a given action for each element in the original {@code Iterable<T>).
	 * As the action is specified for elements of type {@code R}, the original
	 * elements are first converted by means of the mapper specified in the
	 * constructor.
	 */
	@Override
	public void forEach(Consumer<? super R> action) {
		source.forEach(concatC(mapper, action));
	}
}

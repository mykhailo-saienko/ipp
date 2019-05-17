package ms.ipp.iterator;

import static ms.ipp.Algorithms.concatC;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A <i>Decorator</i> for an {@code Iterator<T>} which maps every element from T
 * to R by means of a given mapping function.
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 * @param <R>
 */
public class MappedIterator<T, R> implements Iterator<R> {

	private final Iterator<T> source;
	private final Function<? super T, R> mapper;
	private Supplier<Boolean> onDelete;

	/**
	 * Creates an instance of {@code MappedIterator<T,R>} based on another
	 * {@code Iterator<T>} and a given mapper from {@code T} to {@code R}.
	 * 
	 * @param source the original Iterator, not null
	 * @param mapper the mapper, not null
	 */
	public MappedIterator(Iterator<T> source, Function<? super T, R> mapper) {
		this.source = source;
		this.mapper = mapper;
	}

	public MappedIterator<T, R> setOnDelete(Supplier<Boolean> onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public R next() {
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
	public void forEachRemaining(Consumer<? super R> action) {
		source.forEachRemaining(concatC(mapper, action));
	}
}

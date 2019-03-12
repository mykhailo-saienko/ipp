package ms.ipp.iterator;

import static ms.ipp.Algorithms.concatC;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class MappedSpliterator<T, R> implements Spliterator<R> {

	private final Spliterator<T> it;
	private final Function<? super T, R> mapper;

	public MappedSpliterator(Spliterator<T> it, Function<? super T, R> mapper) {
		this.it = it;
		this.mapper = mapper;
	}

	@Override
	public boolean tryAdvance(Consumer<? super R> action) {
		return it.tryAdvance(concatC(mapper, action));
	}

	@Override
	public Spliterator<R> trySplit() {
		return new MappedSpliterator<>(it.trySplit(), mapper);
	}

	@Override
	public long estimateSize() {
		return it.estimateSize();
	}

	@Override
	public long getExactSizeIfKnown() {
		return it.getExactSizeIfKnown();
	}

	@Override
	public int characteristics() {
		return it.characteristics();
	}

	@Override
	public void forEachRemaining(Consumer<? super R> action) {
		it.forEachRemaining(concatC(mapper, action));
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return it.hasCharacteristics(characteristics);
	}
	// TODO: Cannot add comparator as it requires a back-transformation from R
	// to T (rather than from T to R as is provided by convert)
}
package ms.ipp.iterable;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ProxyBiIterable<T, U> implements BiIterable<T, U> {

	private final Iterable<Entry<T, U>> source;

	public ProxyBiIterable(Iterable<Entry<T, U>> source) {
		this.source = source;
	}

	@Override
	public Iterator<Entry<T, U>> iterator() {
		return source.iterator();
	}

	@Override
	public Spliterator<Entry<T, U>> spliterator() {
		return source.spliterator();
	}

	@Override
	public void forEach(Consumer<? super Entry<T, U>> action) {
		source.forEach(action);
	}

	public Iterable<Entry<T, U>> getSource() {
		return source;
	};
}

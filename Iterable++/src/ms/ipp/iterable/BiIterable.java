package ms.ipp.iterable;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;

public interface BiIterable<T, U> extends Iterable<Map.Entry<T, U>> {

	public static <T, U> BiIterable<T, U> biIt(Iterable<Map.Entry<T, U>> source) {
		return new BiIterable<T, U>() {
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
		};
	}
}

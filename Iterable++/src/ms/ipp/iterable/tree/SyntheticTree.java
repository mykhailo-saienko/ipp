package ms.ipp.iterable.tree;

import static ms.ipp.Algorithms.applyIf;
import static ms.ipp.Algorithms.disabled;
import static ms.ipp.Algorithms.toKV;
import static ms.ipp.Iterables.mapped;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ms.ipp.Iterables;
import ms.ipp.iterable.BiIterable;

public class SyntheticTree<F> extends AbstractTree<F> {

	private static interface Setter<F> {
		void set(String name, F value, BiConsumer<F, F> updater);
	}

	private final Function<String, F> retriever;
	private BiIterable<String, F> forEach;
	private Predicate<String> deleter;
	private Setter<F> setter;

	public <T> SyntheticTree(Function<T, F> converter, BiConsumer<T, F> setter, Function<T, String> idRetriever,
			Function<String, T> retriever, Iterable<T> forEach, Class<F> clazz) {
		this(retriever.andThen(applyIf(t -> t != null, converter)), mapped(forEach, toKV(idRetriever, converter)),
				clazz);
		// Setter must ret
		this.setter = (s, o, upd) -> {
			T obj = retriever.apply(s);
			if (upd != null) {
				upd.accept(converter.apply(obj), o);
			}
			setter.accept(obj, o);
		};
	}

	/**
	 * 
	 * @param retriever
	 * @param forEach
	 * @param clazz
	 */
	public SyntheticTree(Function<String, F> retriever, Class<F> clazz) {
		this(retriever, disabled("Iterating not supported"), clazz);
	}

	public SyntheticTree(Function<String, F> retriever, Iterable<Entry<String, F>> itGen, Class<F> clazz) {
		super(clazz);
		this.retriever = retriever;
		this.setIterator(itGen);
	}

	public SyntheticTree<F> setIterator(Iterable<Entry<String, F>> itGen) {
		this.forEach = Iterables.toBiIt(itGen);
		return this;
	}

	public SyntheticTree<F> setDeleter(Predicate<String> deleter) {
		this.deleter = deleter;
		return this;
	}

	@Override
	public Iterator<Entry<String, F>> iterator() {
		return forEach.iterator();
	}

	@Override
	public F doPeek(String name) {
		return retriever.apply(name);
	}

	@Override
	public void doDeleteImpl(String name, F value) {
		if (deleter != null) {
			deleter.test(name);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T doSetImpl(String name, T value, Class<T> clazz) {
		if (setter == null) {
			return null;
		}
		setter.set(name, (F) value, getUpdater(clazz));
		return value;
	}

}

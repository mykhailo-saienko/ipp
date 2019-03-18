package ms.ipp.iterable.tree;

import static ms.ipp.Algorithms.applyIf;
import static ms.ipp.Algorithms.error;
import static ms.ipp.Algorithms.toKV;
import static ms.ipp.Iterables.mapped;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ms.ipp.Iterables;
import ms.ipp.iterable.BiIterable;

/**
 * A concrete implementation of <code>Tree</code>, in which the associative and
 * the iterative structures are determined by external methods.
 * 
 * @author mykhailo.saienko
 *
 * @param <F>
 */
public class SyntheticTree<F> extends AbstractTree<F> {

	private static interface Setter<F> {
		void set(String name, F value, BiConsumer<F, F> updater);
	}

	private final Function<String, F> retriever;
	private BiIterable<String, F> iterable;
	private Predicate<String> deleter;
	private Setter<F> setter;

	/**
	 * Constructs a {@link SyntheticTree<F>} out of a given {@code retriever},
	 * {@code idRetriever}, and {@code iterable} adapted to the type {@code T} by
	 * applying a given {@code converter} whenever needed
	 * 
	 * @param converter
	 * @param setter
	 * @param idRetriever
	 * @param retriever
	 * @param iterable
	 * @param clazz
	 */
	public <T> SyntheticTree(Function<T, F> converter, BiConsumer<T, F> setter, Function<T, String> idRetriever,
			Function<String, T> retriever, Iterable<T> iterable, Class<F> clazz) {
		this(retriever.andThen(applyIf(t -> t != null, converter)), mapped(iterable, toKV(idRetriever, converter)),
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
	 * Creates a new Tree which uses a given {@code retriever} in its
	 * {@code set(..)} and {@code doSet(..)} methods.
	 * 
	 * @param retriever
	 * @param iterable
	 * @param clazz
	 */
	public SyntheticTree(Function<String, F> retriever, Class<F> clazz) {
		this(retriever, error("Iterating not supported"), clazz);
	}

	/**
	 * Creates a new Tree which uses a given {@code retriever} in its
	 * {@code doSet(..)} methods and a given {@code Iterable} in its
	 * {@code members(..)} methods.
	 * 
	 * @param retriever
	 * @param iterable
	 * @param clazz
	 */
	public SyntheticTree(Function<String, F> retriever, Iterable<Entry<String, F>> iterable, Class<F> clazz) {
		super(clazz);
		this.retriever = retriever;
		this.iterable = Iterables.toBiIt(iterable);
	}

	/**
	 * Sets a method to use when the {@code doDelete} method is called.
	 * 
	 * @param deleter the method to use for deleting immediate children. If null,
	 *                {@code doDelete} does nothing
	 * @return
	 */
	public SyntheticTree<F> setDeleter(Predicate<String> deleter) {
		this.deleter = deleter;
		return this;
	}

	@Override
	public Iterator<Entry<String, F>> iterator() {
		return iterable.iterator();
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

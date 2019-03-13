package ms.ipp.iterable.tree;

import static ms.ipp.Algorithms.toFunc;
import static ms.ipp.Iterables.removeFrom;
import static ms.ipp.iterable.tree.TreeHelper.processMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import ms.ipp.iterable.tree.path.PathManipulator;
import ms.ipp.iterable.tree.path.StdPathManipulator;

public abstract class AbstractTree<F> implements Tree<F> {
	protected static final Logger logger = Logger.getLogger(AbstractTree.class);

	public static enum Recursion {
		SIMPLE, GREEDY, FLAT;
	}

	private Recursion recursion;
	private PathManipulator manipulator;
	private final Map<Class<?>, BiConsumer<F, F>> updaters;

	/// Used for run-time checks in recursive retrievals.
	private final Class<F> clazz;

	public AbstractTree(Class<F> clazz) {
		this.clazz = clazz;
		setSeparator('.');
		// The default converter assumes a member of type Entity is
		// automatically Entity<F>.
		setRecursion(Recursion.SIMPLE);
		updaters = new HashMap<>();
	}

	/**
	 * Null removes the updater
	 * 
	 * @param key
	 * @param updater
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends F> void setUpdater(Class<T> key, BiConsumer<? extends F, T> updater) {
		if (updater == null) {
			updaters.remove(key);
		} else {
			// we convert it without worries, since it will be only called in
			// set(...) for values of Class<T> which can be converted back to
			// T without problems
			updaters.put(key, (BiConsumer<F, F>) updater);
		}
	}

	protected BiConsumer<F, F> getUpdater(Class<?> key) {
		return updaters.get(key);
	}

	public AbstractTree<F> setSeparator(char separator) {
		this.manipulator = new StdPathManipulator(separator);
		return this;
	}

	public AbstractTree<F> setRecursion(Recursion recursion) {
		this.recursion = recursion;
		return this;
	}

	@Override
	public PathManipulator getPathManipulator() {
		return manipulator;
	}

	@Override
	public Recursion getRecursion() {
		return recursion;
	}

	@Override
	public final <T> T peek(String name, Class<T> clazz) {
		return processMember(this, name, n -> doPeek(n, clazz), (e, n) -> e.peek(n, clazz), null);
	}

	@Override
	public final void delete(String name, Object value) {
		processMember(this, name, toFunc(n -> doDelete(n, value), true), toFunc((e, n) -> e.delete(n, value), true),
				true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void doDelete(String name, Object value) {
		// there is no way this value is in our list.
		if (value != null && !canSet(value.getClass())) {
			return;
		}
		doDeleteImpl(name, (F) value);
	}

	@Override
	public final <T> T set(String name, T value, Class<T> clazz) {
		// The logic of tryGetMember depends on members not being null.
		if (value == null) {
			throw new IllegalArgumentException("Cannot set '" + name + "' to null");
		}
		if (logger.isTraceEnabled()) {
			logger.debug("Setting field '" + name + "' to value " + value + " of type " + clazz);
		}

		return processMember(this, name, n -> doSet(n, value, clazz), (e, n) -> e.set(n, value, clazz), null);
	}

	@Override
	public final <T> T doSet(String name, T value, Class<T> clazz) {
		// we assume that value is not null. See AbstractEntity on more details
		assert (value != null);
		if (!canSet(clazz)) {
			return null;
		}
		return doSetImpl(name, value, clazz);
	}

	@Override
	public Class<F> getBaseClass() {
		return clazz;
	}

	/**
	 * Returns true if instances of a given class are accepted by
	 * {@link #setMember(String, Object)}.
	 * 
	 * @param clazz
	 * @return
	 */
	protected boolean canSet(Class<?> clazz) {
		return getBaseClass().isAssignableFrom(clazz);
	}

	protected void doDeleteImpl(String name, F value) {
		Predicate<Entry<String, F>> pred = value == null ? null : e -> value.equals(e.getValue());
		removeFrom((Iterable<Entry<String, F>>) members(name), pred);
	}

	protected abstract <T> T doSetImpl(String name, T value, Class<T> clazz);
}
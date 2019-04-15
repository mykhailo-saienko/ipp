package ms.ipp.iterable.tree;

import static ms.ipp.Algorithms.toFunc;
import static ms.ipp.Iterables.removeFrom;
import static ms.ipp.iterable.tree.TreeHelper.processMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ms.ipp.iterable.tree.path.PathManipulator;
import ms.ipp.iterable.tree.path.StdPathManipulator;

/**
 * A basic abstract implementation of all features shared by the concrete
 * sub-implementations.
 * 
 * In particular, the following is implemented:
 * <li>recursive methods, such as {@link #get(String, Class) get(..)},
 * {@link #delete(String, Object) delete(..)}, {@link #peek(String, Class)
 * peek(..)}, {@link #set(String, Object, Class) set(..)};
 * <li>some preliminary checks for {@link #doDelete(String, Object)
 * doDelete(..)}, {@link #doPeek(String, Class) doPeek(..)},
 * {@link #doSet(String, Object, Class) doSet(..)};
 * <li>metadata setters, such as {@link #setRecursion(Recursion)},
 * {@link #setPathManipulator(PathManipulator)},
 * {@link #setUpdater(Class, BiConsumer)}, and the corresponding getters.
 * 
 * @author mykhailo.saienko
 *
 * @param <F> as in {@link Tree}.
 */
public abstract class AbstractTree<F> implements Tree<F> {
	// Returns a logger with the name of the calling class.
	protected static final Logger logger = LogManager.getLogger();

	/**
	 * The recursion mode used by <code>Tree</code> in recursive manipulations.
	 * 
	 * While trying to locate an element by its path, the tree asks its
	 * {@link PathManipulator} to give the path' root, i.e., the name of the
	 * immediate child this Tree is supposed to have.
	 * <ul>
	 * <li>If such a child is found (and is a {@link Tree}), it is prompted to
	 * recursively locate an element given by the remaining part of the path until
	 * this remaining part becomes empty.<br>
	 * <li>If such a child is <i>not</i> found, the recursion mode decides on the
	 * behaviour:
	 * <ul>
	 * <li>In the <code>STANDARD</code> mode, the search is interrupted and an error
	 * is signalised.
	 * <li>In the <code>EXHAUSTIVE</code> mode, the tree repeatedly prompts the
	 * <code>PathManipulator</code> to extract a root from the remaining part of the
	 * path and add it to the already used part and looks if there exists a child
	 * with this newly combined name. If there is one, use it and the remaining part
	 * of the path to conclude the search. If the entire path is used up and no
	 * immediate children are found, the search is interrupted and an error is
	 * signalised
	 */
	public static enum Recursion {
		STANDARD, EXHAUSTIVE;
	}

	private Recursion recursion;
	private PathManipulator manipulator;
	private final Map<Class<?>, BiConsumer<F, F>> updaters;

	/// Used for run-time checks in recursive retrievals.
	private final Class<F> clazz;

	public AbstractTree(Class<F> clazz) {
		this.clazz = clazz;
		this.setPathManipulator(new StdPathManipulator('.'));

		// The default converter assumes a member of type Entity is
		// automatically Entity<F>.
		setRecursion(Recursion.STANDARD);
		updaters = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends F> void setUpdater(Class<T> key, BiConsumer<T, T> updater) {
		if (updater == null) {
			updaters.remove(key);
		} else {
			// we convert it without worries, since it will be only called in
			// set(...) for values of Class<T> which can be converted back to
			// F without problems
			updaters.put(key, (BiConsumer<F, F>) updater);
		}
	}

	public AbstractTree<F> setRecursion(Recursion recursion) {
		this.recursion = recursion;
		return this;
	}

	public AbstractTree<F> setPathManipulator(PathManipulator manipulator) {
		this.manipulator = manipulator;
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
		logger.debug("Setting field '{}' to value {} of type {}", name, value, clazz);

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

	protected BiConsumer<F, F> getUpdater(Class<?> key) {
		return updaters.get(key);
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
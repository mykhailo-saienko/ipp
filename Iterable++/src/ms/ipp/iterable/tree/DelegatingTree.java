package ms.ipp.iterable.tree;

import static ms.ipp.Iterables.first;
import static ms.ipp.Iterables.isEqualOrNull;
import static ms.ipp.Iterables.mapped;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import ms.ipp.Iterables;
import ms.ipp.base.KeyValue;
import ms.ipp.iterator.FilteredIterator;
import ms.ipp.iterator.NestedIterator;

public class DelegatingTree<F> extends AbstractTree<F> {
	private final List<KeyValue<Class<?>, Tree<F>>> delegates;
	private boolean distinct;

	public DelegatingTree(Class<F> clazz) {
		super(clazz);
		setDistinct(true);
		delegates = new ArrayList<>();

	}

	@Override
	public <T extends F> void setUpdater(Class<T> key, BiConsumer<T, T> updater) {
		super.setUpdater(key, updater);
		for (Entry<Class<?>, Tree<F>> d : delegates) {
			d.getValue().setUpdater(key, updater);
		}
	}

	/**
	 * Sets the DelegatingTree's behaviour while traversing over all elements. If
	 * true and multiple elements with the same path exist in one of multiple
	 * delegate Trees, only the element encountered first is taken into account.
	 * Otherwise, all elements are returned.
	 * 
	 * @param distinct
	 * @return
	 */
	public DelegatingTree<F> setDistinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	/**
	 * Adds a new delegate {@code Tree} at a given index in the list of all existing
	 * delegate Trees.
	 * 
	 * @param key      the Class to associate the {@code delegate} with. May be
	 *                 null, in which case the tree is not associated with any
	 *                 particular Class.
	 * @param delegate the Tree to insert, not null
	 * @param index    the index, at which to insert the {@code delegate}. Must be
	 *                 non-negative and not greater than this Tree's size
	 */
	@SuppressWarnings("unchecked")
	public <T extends F> void add(Class<T> key, Tree<T> delegate, int index) {
		// either the same key or the same generic delegate.
		// The latter may be the case if we want to shift the delegate to
		// another place.
		Iterables.removeFrom(delegates,
				e -> (key != null && key.equals(e.getKey())) || (key == null && e.getValue().equals(delegate)));
		delegates.add(index, new KeyValue<>(key, (Tree<F>) delegate));
	}

	/**
	 * Adds a new delegate {@code Tree} at a given index in the list of all existing
	 * delegate Trees. Is equivalent to:
	 * 
	 * <pre>
	 * add(null, delegate, insert);
	 * </pre>
	 * 
	 * @see #add(Class, Tree, int)
	 * @param delegate the Tree to insert, not null
	 * @param index    the index, at which to insert the {@code delegate}. Must be
	 *                 non-negative and not greater than this Tree's size
	 */
	public void add(Tree<F> delegate, int insert) {
		add(null, delegate, insert);
	}

	/**
	 * Adds a new delegate {@code Tree} to the end of the list of all existing
	 * delegates and returns the index at which it was added.
	 * 
	 * @param key      the Class to associate the {@code delegate} with. May be
	 *                 null, in which case the tree is not associated with any
	 *                 particular Class.
	 * @param delegate the Tree to insert, not null
	 */
	public <T extends F> int add(Class<T> key, Tree<T> delegate) {
		int i = size();
		add(key, delegate, i);
		return i;
	}

	/**
	 * Adds a new delegate {@code Tree} to the end of the list of all existing
	 * delegates and returns the index at which it was added. The delegate is not
	 * associated with any particular Class. Is equivalent to:
	 * 
	 * <pre>
	 * add(null, delegate);
	 * </pre>
	 * 
	 * @see #add(Class, Tree)
	 * 
	 * @param delegate the Tree to insert, not null
	 */
	public int add(Tree<F> delegate) {
		return add(null, delegate);
	}

	/**
	 * Returns the delegate with a given index.
	 * 
	 * @param index the index, at which to look for delegate. Must be non-negative
	 *              and smaller than this Tree's size
	 * @return
	 */
	public Tree<F> get(int index) {
		return delegates.get(index).getValue();
	}

	/**
	 * Returns the first delegate in the list which is associated with a given Class
	 * or null if none are found.
	 * 
	 * @param clazz the Class, for which to look for the associated delegate.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Tree<T> get(Class<T> clazz) {
		KeyValue<Class<?>, Tree<F>> res = first(delegates, e -> isEqualOrNull(e.getKey(), clazz));
		return res == null ? null : (Tree<T>) res.getValue();
	}

	/**
	 * Removes and returns the delegate with a given index.
	 * 
	 * @param index the index, at which to remove a delegate. Must be non-negative
	 *              and smaller than this Tree's size
	 * @return the delegate just removed
	 */
	public Tree<F> remove(int index) {
		return delegates.remove(index).getValue();
	}

	/**
	 * Removes and returns returns the delegate with the highest index.
	 * 
	 * @return the delegate just removed
	 */
	public Tree<F> remove() {
		return remove(size() - 1);
	}

	/**
	 * Returns the number of delegates this {@code Tree} contains.
	 * 
	 * @return
	 */
	public int size() {
		return delegates.size();
	}

	@Override
	public F doPeek(String name) {
		return first(mapped(delegates, e -> e.getValue().peek(name)), t -> t != null);
	}

	@Override
	public <T> T doSetImpl(String name, T value, Class<T> clazz) {
		return first(mapped(delegates, e -> e.getValue().set(name, value, clazz)), t -> t != null);
	}

	@Override
	public Iterator<Entry<String, F>> iterator() {
		Iterator<Entry<String, F>> it = new NestedIterator<>(delegates.iterator(), e -> e.getValue().iterator());
		if (distinct) {
			it = FilteredIterator.distinct(it, Entry::getKey);
		}
		return it;
	}

}

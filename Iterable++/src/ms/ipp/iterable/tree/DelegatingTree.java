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
	public <T extends F> void setUpdater(Class<T> key, BiConsumer<? extends F, T> updater) {
		super.setUpdater(key, updater);
		for (Entry<Class<?>, Tree<F>> d : delegates) {
			d.getValue().setUpdater(key, updater);
		}
	}

	public DelegatingTree<F> setDistinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends F> void add(Class<T> key, Tree<T> delegate, int insert) {
		// either the same key or the same generic delegate.
		// The latter may be the case if we want to shift the delegate to
		// another place.
		Iterables.removeFrom(delegates,
				e -> (key != null && key.equals(e.getKey())) || (key == null && e.getValue().equals(delegate)));
		delegates.add(insert, new KeyValue<>(key, (Tree<F>) delegate));
	}

	public void add(Tree<F> delegate, int insert) {
		add(null, delegate, insert);
	}

	public <T extends F> int add(Class<T> key, Tree<T> delegate) {
		int i = size();
		add(key, delegate, i);
		return i;
	}

	public int add(Tree<F> delegate) {
		return add(null, delegate);
	}

	public Tree<F> get(int index) {
		return delegates.get(index).getValue();
	}

	@SuppressWarnings("unchecked")
	public <T> Tree<T> get(Class<T> clazz) {
		KeyValue<Class<?>, Tree<F>> res = first(delegates, e -> isEqualOrNull(e.getKey(), clazz));
		return res == null ? null : (Tree<T>) res.getValue();
	}

	public Tree<F> remove(int index) {
		return delegates.remove(index).getValue();
	}

	public Tree<F> pop() {
		return remove(size() - 1);
	}

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

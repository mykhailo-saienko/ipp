package ms.ipp.iterable.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import ms.ipp.Iterables;
import ms.ipp.base.KeyValue;
import ms.ipp.iterator.MappedIterator;
import ms.ipp.iterator.NestedIterator;

/**
 * A {@code Tree}-wrapper around a {@code Map<String, List<F>>} which allows
 * multiple elements with the same name to co-exist.
 * 
 * @author mykhailo.saienko
 *
 * @param <F>
 */
public class StdMultiTree<F> extends AbstractTree<F> {

	private final Map<String, List<F>> members;
	private final BiPredicate<F, F> isEqual;

	/**
	 * Creates a new {@code StdMultiTree} which uses the standard comparator for
	 * duplicate checks when inserting new elements. Is equivalent to:
	 * 
	 * <pre>
	 * new StdMultiTree<>(clazz, Iterables::isEqualOrNull);
	 * </pre>
	 * 
	 * @see Iterables#isEqualOrNull(Object, Object)
	 * @see #StdMultiTree(Class, BiPredicate)
	 * 
	 * @param clazz the base class, not null
	 */
	public StdMultiTree(Class<F> clazz) {
		this(clazz, Iterables::isEqualOrNull);
	}

	/**
	 * Creates a new {@code StdMultiTree} which uses a given {@link BiPredicate} for
	 * duplicate checks when inserting new elements.
	 * 
	 * @param clazz   the base class, not null
	 * @param isEqual the comparator to use for duplicate checks, not null
	 */
	public StdMultiTree(Class<F> clazz, BiPredicate<F, F> isEqual) {
		super(clazz);
		// all objects with the same name should still be unique wrt to this
		// equalizer
		this.isEqual = isEqual;
		members = new HashMap<>();
	}

	@Override
	public Iterator<Entry<String, F>> iterator(String name) {
		List<F> list = check(name);
		return new MappedIterator<>(list.iterator(), e -> new KeyValue<>(name, e));
	}

	@Override
	public Iterator<Entry<String, F>> iterator() {
		return new NestedIterator<>(members.entrySet().iterator(),
				e -> new MappedIterator<>(e.getValue().iterator(), f -> new KeyValue<>(e.getKey(), f)));
	}

	@Override
	public F doPeek(String name) {
		return doPeek(name, getBaseClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T doPeek(String name, Class<T> clazz) {
		List<F> list = check(name);
		if (list.isEmpty()) {
			return null;
		}
		F res = Iterables.unique(list, clazz::isInstance);
		return res == null ? null : (T) res;
	}

	@Override
	protected <T> T doSetImpl(String name, T value, Class<T> clazz) {
		@SuppressWarnings("unchecked")
		F input = (F) value;
		List<F> list = Iterables.getInsert(name, members);
		int index = reallyDelete(list, name, input, isEqual);
		if (index == -1) {
			index = list.size();
		}
		list.add(index, input);
		return value;
	}

	@Override
	public void doDeleteImpl(String name, F value) {
		reallyDelete(check(name), name, value, isEqual);
	}

	private static <T> int reallyDelete(List<T> list, String name, T value, BiPredicate<T, T> isEqual) {
		int index = Iterables.indexOf(list, t -> isEqual.test(t, value));
		if (index != -1) {
			list.remove(index);
		}
		return index;
	}

	private List<F> check(String name) {
		return Iterables.get(name, members, ArrayList::new);
	}

}

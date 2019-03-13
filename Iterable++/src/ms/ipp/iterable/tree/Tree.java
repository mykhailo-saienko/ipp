package ms.ipp.iterable.tree;

import static ms.ipp.Iterables.toBiIt;
import static ms.ipp.iterable.tree.TreeHelper.castIterator;
import static ms.ipp.iterable.tree.TreeHelper.recursiveHead;
import static ms.ipp.iterator.NestedIterator.combined;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import ms.ipp.base.KeyValue;
import ms.ipp.iterable.BiIterable;
import ms.ipp.iterable.tree.AbstractTree.Recursion;
import ms.ipp.iterable.tree.path.PathManipulator;
import ms.ipp.iterator.FilteredIterator;
import ms.ipp.iterator.NestedIterator;
import ms.ipp.iterator.OneIterator;

public interface Tree<F> extends BiIterable<String, F> {
	Class<F> getBaseClass();

	PathManipulator getPathManipulator();

	Recursion getRecursion();

	<T extends F> void setUpdater(Class<T> key, BiConsumer<? extends F, T> updater);

	/**
	 * Sets a member with a given name to a given value. If a given member cannot be
	 * found, reset or added, null is returned. <br>
	 * <b>NOTE:</b> Null-values are not accepted. If a given value is null, an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	<T> T set(String name, T value, Class<T> clazz);

	<T> T peek(String name, Class<T> clazz);

	/**
	 * Recursively deletes all members with a given name (and, optionally, a given
	 * value).
	 * 
	 * @param name
	 * @return
	 */
	void delete(String name, Object value);

	F doPeek(String name);

	<T> T doSet(String name, T value, Class<T> clazz);

	void doDelete(String name, Object value);

	default <T> Iterator<Entry<String, T>> iterator(String name, Class<T> clazz) {
		return castIterator(iterator(name), getBaseClass(), clazz);
	}

	default <T> Iterator<Entry<String, T>> iterator(Class<T> clazz) {
		return castIterator(iterator(), getBaseClass(), clazz);
	}

	default Iterator<Entry<String, F>> iterator(String name) {
		return new FilteredIterator<>(iterator(), e -> e.getKey().equals(name));
	}

	default BiIterable<String, F> members(String name) {
		return toBiIt(() -> iterator(name));
	}

	default <T> BiIterable<String, T> members(String name, Class<T> clazz) {
		return toBiIt(() -> iterator(name, clazz));
	}

	default <T> BiIterable<String, T> members(Class<T> clazz) {
		return toBiIt(() -> iterator(clazz));
	}

	/**
	 * Recursively traverse only leaves of at least the base type (members may be
	 * Entities with different base types which may not be compatible with the
	 * root's base type).
	 * 
	 * @return
	 */
	default BiIterable<String, F> recursive() {
		return recursive(getBaseClass());
	}

	default <T> BiIterable<String, T> recursive(Class<T> clazz) {
		return toBiIt(() -> recursiveHead(this, clazz));
	}

	/**
	 * Retrieves a member with a given name. If there is no such member, throws an
	 * IllegalArgumentException
	 * 
	 * @param name
	 * @return
	 */
	default F get(String name) {
		return get(name, getBaseClass());
	}

	default <T> T get(String name, Class<T> clazz) {
		T member = peek(name, clazz);
		if (member != null) {
			return member;
		}
		throw new IllegalArgumentException("Unknown member '" + name + "'");
	}

	/**
	 * Retrieves a member with a given name. If the name is not simple, the search
	 * is carried recursively. If there is no such member, returns null.
	 * 
	 * @param name
	 * @return
	 */
	default F peek(String name) {
		return peek(name, getBaseClass());
	}

	/**
	 * Use only if name uniquely identifies the member. Otherwise the results are
	 * undefined.
	 * 
	 * @param name
	 */
	default void delete(String name) {
		delete(name, null);
	}

	@SuppressWarnings("unchecked")
	default <T> T set(String name, T value) {
		return set(name, value, (Class<T>) value.getClass());
	}

	/**
	 * Retrieves a member with a given name. By default, if the parameter
	 * {@link #recursion} is set to SIMPLE, the names are always simple. Otherwise,
	 * the simplicity of the passed name is not guaranteed.
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T> T doPeek(String name, Class<T> clazz) {
		F res = doPeek(name);
		// if res is null, isInstance returns false
		return clazz.isInstance(res) ? (T) res : null;
	}

	/**
	 * Use only if name uniquely identifies the member. Otherwise the results are
	 * undefined.
	 * 
	 * @param name
	 */
	default void doDelete(String name) {
		doDelete(name, null);
	}
}

class TreeHelper {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T, U> Iterator<Entry<String, U>> castIterator(Iterator<Entry<String, T>> it, Class<T> source,
			Class<U> target) {
		// if a target class is a superclass of our source class, all instances
		// will be of target class, too, so no need to filter them out
		if (!target.isAssignableFrom(source)) {
			// if it is a subclass or completely unrelated -> some concrete
			// instances may coincidentally still satisfy the condition (for
			// example implement some interfaces)
			it = new FilteredIterator<>(it, e -> target.isInstance(e.getValue()));
		}
		// this cast is safe, as we have only kept instances of Entry<String, ?
		// extends U>
		return (Iterator) it;
	}

	static <T, U> U processMember(Tree<?> source, String name, Function<String, U> doProcessor,
			BiFunction<Tree<?>, String, U> recursiveProcessor, U onError) {
		PathManipulator manipulator = source.getPathManipulator();
		Recursion recursion = source.getRecursion();
		if (name == null || name.isEmpty()) {
			return onError;
		} else if (recursion == Recursion.FLAT || manipulator.isSimple(name)) {
			return doProcessor.apply(name);
		}
		// we are sure that the name is not simple -> there must be a member
		// and it has to have submembers
		// find a member with the first prefix we extract. If the member is not
		// found, return error. Otherwise use recursiveProcessor on the rest of
		// the path
		else if (recursion == Recursion.SIMPLE) {
			KeyValue<String, String> split = manipulator.getRoot(name);
			Tree<?> f = source.doPeek(split.getKey(), Tree.class);
			return (f == null) ? onError : recursiveProcessor.apply(f, split.getValue());
		} else {
			// greedy recursion -> try all prefixes until found a member or
			// exhausted all prefixes. If a member is found, apply
			// recursiveProcessor on the rest of the path
			for (int next = manipulator.nextLevel(name, 0); next != -1; next = manipulator.nextLevel(name, next)) {
				String key = name.substring(0, next);
				Tree<?> f = source.doPeek(key, Tree.class);
				if (f == null) {
					continue;
				}
				return recursiveProcessor.apply(f, name.substring(next + 1));
			}
			// if we are here, we have to try and process the entire string in a
			// FLAT manner
			return doProcessor.apply(name);
		}
	}

	static <T> Iterator<Entry<String, T>> recursiveHead(Tree<?> source, Class<T> clazz) {
		// we have to traverse all members of source (even if they are not of
		// type T) since they might be Entities possessing children of type T
		// Ideally, we would like to have source.iterator(clazz || Entity);
		return new NestedIterator<>(source.iterator(),
				e -> recursive(source, e.getKey(), e.getValue(), deleter(source, e), clazz));
	}

	private static <T> Iterator<Entry<String, T>> recursive(Tree<?> parent, String fullName, Object member,
			Runnable deleter, Class<T> clazz) {
		Iterator<Entry<String, T>> it = null;
		if (clazz.isInstance(member)) {
			Entry<String, T> keyValue = new KeyValue<>(fullName, clazz.cast(member));
			// the deleter deletes
			it = new OneIterator<>(keyValue).setDeleter(deleter);
		}
		if (member instanceof Tree) {
			Tree<?> ent = ((Tree<?>) member);
			it = combined(it, new NestedIterator<>(ent.iterator(), e -> recursive(ent,
					ent.getPathManipulator().combine(fullName, e.getKey()), e.getValue(), deleter(ent, e), clazz)));
		}
		return it;
	}

	private static <T> Runnable deleter(Tree<?> parent, Entry<String, T> member) {
		return () -> parent.delete(member.getKey(), member.getValue());
	}

}
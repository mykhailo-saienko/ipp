package ms.ipp.iterable.tree;

import static ms.ipp.Iterables.toBiIt;
import static ms.ipp.iterable.tree.TreeHelper.castIterator;
import static ms.ipp.iterable.tree.TreeHelper.recursiveHead;
import static ms.ipp.iterator.NestedIterator.merge;

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

/**
 * The API for all Tree-like structures. Its main features are:
 * <li>The <b>iterative structure</b> which offers several methods for
 * traversing the Tree's immediate children, such as {@link #members()},
 * {@link #iterator(Class) iterator(..)}.
 * <li>The <b>associative structure</b> allowing to effectively locate and
 * manipulate members by their names, e.g., {@link #doPeek(String, Class)
 * doPeek(..)}, {@link #doSet(String, Object,Class) doSet(..)},
 * {@link #doDelete(String, Object) doDelete(..)}.
 * <li>The <b>recursive structure</b>, in the sense that an element is
 * recognised to potentially have children if it implements the interface Tree.
 * 
 * The recursive structure is compatible with both the iterative and the
 * associative structures. More precisely, there is a collection of methods,
 * such as {@link #recursive()} which allow traversing over the entire Tree
 * structure, including its children, the children's children, etc. On top of
 * that, there are methods, such as {@link #peek(String, Class) peek(..)},
 * {@link #get(String, Class) get(..)}, {@link #set(String, Object,Class)
 * set(..)}, {@link #delete(String, Object) delete(..)}, for retrieving, setting
 * or deleting elements by their full paths within the recursive structure
 * (rather than just their names as elements of the Tree). How these paths are
 * interpreted depends on the {@link PathManipulator} that the Tree is equipped
 * with.
 * 
 * All iterators and iterables are fully compatible with the methods in the
 * class {@link ms.ipp.Iterables}.
 * 
 * @author mykhailo.saienko
 *
 * @param <F> The superclass that Tree's immediate children must extend. The
 *        children's children are not required to be of type <b>F</b>.
 */
public interface Tree<F> extends BiIterable<String, F> {
	///////////////////////////////////////////////////////
	/// ************ Metadata Manipulation ************ ///
	///////////////////////////////////////////////////////

	/**
	 * Returns the Class-object of the template-parameter F. This is mostly used to
	 * check for compatibility of an input object at run-time.
	 * 
	 * @return
	 */
	Class<F> getBaseClass();

	/**
	 * Returns the {@link PathManipulator} used to break the path into single levels
	 * while recursively searching for elements by their path.
	 * 
	 * @return
	 */
	PathManipulator getPathManipulator();

	/**
	 * Returns the recursion mode.
	 * 
	 * @see Recursion
	 * @return
	 */
	Recursion getRecursion();

	/**
	 * Sets an updater for a given type.
	 * 
	 * If {@link Tree#set(String, Object, Class) set(..)} or
	 * {@link #doSet(String, Object, Class) doSet(..)} are called and there is an
	 * updater for the passed type, it is called immediately before a new value
	 * replaces the old one. In this case, the old value is passed as the first and
	 * the new value - as the second parameter.
	 * 
	 * <b>WARNING:</b> The updater assumes that the old value is of the same type as
	 * the new value. If this cannot be ensured, the affected <code>set</code> and
	 * <code>doSet</code> methods exhibit undefined behaviour.
	 * 
	 * @param type    the type for which a given updater must be called
	 * @param updater a new updater or null in order to remove the updater
	 */
	<T extends F> void setUpdater(Class<T> type, BiConsumer<T, T> updater);

	///////////////////////////////////////////////////////
	/// ************* Iterative Structure ************* ///
	///////////////////////////////////////////////////////
	/**
	 * Returns a (non-recursive) iterator over all immediate children with a given
	 * name which are of a given type.
	 * 
	 * @param name
	 * @param clazz
	 * @return
	 */
	default <T> Iterator<Entry<String, T>> iterator(String name, Class<T> clazz) {
		return castIterator(iterator(name), getBaseClass(), clazz);
	}

	/**
	 * Returns a (non-recursive) iterator over all immediate children which are of a
	 * given type.
	 * 
	 * @param clazz
	 * @return
	 */
	default <T> Iterator<Entry<String, T>> iterator(Class<T> clazz) {
		return castIterator(iterator(), getBaseClass(), clazz);
	}

	/**
	 * Returns a (non-recursive) iterator over all immediate children with a given
	 * name.
	 * 
	 * @param clazz
	 * @return
	 */
	default Iterator<Entry<String, F>> iterator(String name) {
		return new FilteredIterator<>(iterator(), e -> e.getKey().equals(name));
	}

	/**
	 * Returns a (non-recursive) {@link BiIterable} over all immediate children with
	 * a given name which are of a given type.
	 * 
	 * @param name
	 * @param clazz
	 * @return
	 */
	default <T> BiIterable<String, T> members(String name, Class<T> clazz) {
		return toBiIt(() -> iterator(name, clazz));
	}

	/**
	 * Returns a (non-recursive) {@link BiIterable} over all immediate children with
	 * a given name.
	 * 
	 * @param name
	 * @return
	 */
	default BiIterable<String, F> members(String name) {
		return toBiIt(() -> iterator(name));
	}

	/**
	 * Returns a (non-recursive) {@link BiIterable} over all immediate children of a
	 * given type.
	 * 
	 * @param clazz
	 * @return
	 */
	default <T> BiIterable<String, T> members(Class<T> clazz) {
		return toBiIt(() -> iterator(clazz));
	}

	///////////////////////////////////////////////////////
	/// ************ Associative Structure ************ ///
	///////////////////////////////////////////////////////
	/**
	 * Retrieves a member with a given name which is automatically of base type. If
	 * no such member exists, returns null.
	 * 
	 * @param name
	 * @param clazz
	 * @return
	 */
	F doPeek(String name);

	/**
	 * Retrieves a member with a given name and of a given type. If no such member
	 * exists, returns null.
	 * 
	 * @param name
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T> T doPeek(String name, Class<T> clazz) {
		F res = doPeek(name);
		// if res is null, isInstance returns false
		return clazz.isInstance(res) ? (T) res : null;
	}

	/**
	 * Sets a member with a given name to a given value. If the member with a given
	 * path cannot be found, reset or added, null is returned.
	 * 
	 * @param path
	 * @param value a new value to set for a given path. Must not be null.
	 * @param clazz
	 * @return
	 * @throws IllegalArgumentException is value is null.
	 */
	<T> T doSet(String name, T value, Class<T> clazz);

	/**
	 * Equivalent to <code>doDelete(name, null)</code>.
	 * 
	 * @param name
	 */
	default void doDelete(String name) {
		doDelete(name, null);
	}

	/**
	 * The results depend on the content of the parameter <code>value</code>:
	 * <li>If <code>value</code> is not null, deletes all children with a given name
	 * which are equal to a given value.
	 * <li>If the value is null and the path identifies the child, it is deleted.
	 * <li>Otherwise, the result is undefined.
	 * 
	 * 
	 * @param path
	 * @param value
	 * @return
	 */
	void doDelete(String name, Object value);

	///////////////////////////////////////////////////////
	/// ****** Recursive Structure (Associative) ****** ///
	///////////////////////////////////////////////////////

	/**
	 * Sets a member with a given path to a given value. If the path is not simple,
	 * the search is carried recursively. If the member with the path cannot be
	 * found, reset or added, null is returned.
	 * 
	 * @param path
	 * @param value a new value to set for a given path. Must not be null.
	 * @param clazz
	 * @return
	 * @throws IllegalArgumentException is value is null.
	 */
	<T> T set(String path, T value, Class<T> clazz);

	/**
	 * Basically equivalent to: <code>set(path, value, value.getClass())</code>.
	 * 
	 * @see Tree#set(String, Object, Class);
	 * @param path
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T> T set(String path, T value) {
		return set(path, value, (Class<T>) value.getClass());
	}

	/**
	 * Equivalent to: <code>delete(path, null)</code>.
	 * 
	 * @param path
	 */
	default void delete(String path) {
		delete(path, null);
	}

	/**
	 * The results depend on the content of the parameter <code>value</code>:
	 * <li>If <code>value</code> is not null, recursively deletes all elements with
	 * a given path which are equal to a given value.
	 * <li>If the value is null and the path identifies the element, this element is
	 * deleted.
	 * <li>Otherwise, the result is undefined.
	 * 
	 * 
	 * @param path
	 * @param value
	 * @return
	 */
	void delete(String path, Object value);

	/**
	 * Retrieves a member with a given path and which is of a given type. If the
	 * path is not simple, the search is carried recursively. If there is no such
	 * member, returns null.
	 * 
	 * @param path
	 * @return
	 */
	<T> T peek(String path, Class<T> clazz);

	/**
	 * Equivalent to: <code>peek(path, getBaseClass())</code>.
	 * 
	 * @param path
	 * @return
	 */
	default F peek(String path) {
		return peek(path, getBaseClass());
	}

	/**
	 * Retrieves an element with a given path and which is of a given type. If the
	 * path is not simple, the search is carried recursively. In contrast to
	 * {@link #peek(String, Class)}, if there is no such member, throws an
	 * {@link IllegalArgumentException}.
	 * 
	 * @param path
	 * @return
	 */
	default <T> T get(String path, Class<T> clazz) {
		T member = peek(path, clazz);
		if (member != null) {
			return member;
		}
		throw new IllegalArgumentException("Unknown member '" + path + "'");
	}

	/**
	 * Equivalent to: <code>get(path, getBaseClass())</code>.
	 * 
	 * @param path
	 * @return
	 */
	default F get(String path) {
		return get(path, getBaseClass());
	}

	///////////////////////////////////////////////////////
	/// ******* Recursive Structure (Iterative) ******* ///
	///////////////////////////////////////////////////////
	/**
	 * Equivalent to: {@code recursive(getBaseClass());}
	 * 
	 * @see #recursive(Class).
	 * @return
	 */
	default BiIterable<String, F> recursive() {
		return recursive(getBaseClass());
	}

	/**
	 * Creates and returns a {@link BiIterable} which traverses all elements of a
	 * given type. This includes non-leaves if they are subclasses of
	 * <code>T</code>.
	 * 
	 * @param clazz
	 */
	default <T> BiIterable<String, T> recursive(Class<T> clazz) {
		return toBiIt(() -> recursiveHead(this, clazz));
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

	static <T, U> U processMember(Tree<?> source, String path, Function<String, U> doProcessor,
			BiFunction<Tree<?>, String, U> recursiveProcessor, U onError) {
		if (path == null || path.isEmpty()) {
			return onError;
		}

		PathManipulator manipulator = source.getPathManipulator();
		if (manipulator.isSimple(path)) {
			return doProcessor.apply(path);
		}

		// we are sure that the path is not simple -> there must be a member
		// and it has to have submembers
		// find a member with the first prefix we extract. If the member is not
		// found, return error. Otherwise use recursiveProcessor on the rest of
		// the path
		Recursion recursion = source.getRecursion();
		if (recursion == Recursion.STANDARD) {
			KeyValue<String, String> split = manipulator.getRoot(path);
			Tree<?> f = source.doPeek(split.getKey(), Tree.class);
			return (f == null) ? onError : recursiveProcessor.apply(f, split.getValue());
		} else {
			// greedy recursion -> try all prefixes until found a member or
			// exhausted all prefixes. If a member is found, apply
			// recursiveProcessor on the rest of the path
			for (int next = manipulator.nextLevel(path, 0); next != -1; next = manipulator.nextLevel(path, next)) {
				String key = path.substring(0, next);
				Tree<?> f = source.doPeek(key, Tree.class);
				if (f == null) {
					continue;
				}
				return recursiveProcessor.apply(f, path.substring(next + 1));
			}
			// if we are here, we have to try and process the entire string in a
			// FLAT manner
			return doProcessor.apply(path);
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
			it = merge(it, new NestedIterator<>(ent.iterator(), e -> recursive(ent,
					ent.getPathManipulator().combine(fullName, e.getKey()), e.getValue(), deleter(ent, e), clazz)));
		}
		return it;
	}

	private static <T> Runnable deleter(Tree<?> parent, Entry<String, T> member) {
		return () -> parent.delete(member.getKey(), member.getValue());
	}

}
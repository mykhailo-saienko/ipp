package ms.ipp.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import ms.ipp.iterable.tree.Tree;

/**
 * An Iterator which offers a "flattened-up" view on hierarchical structures
 * with two levels. For example, given a {@code List<List<T>> list}, we can
 * define an Iterator which iterates over all elements of {@code list.get(0)},
 * then proceeds to the iterating over {@code list.get(1)}, etc., as follows:
 * 
 * <pre>
 * Iterator<T> it = new NestedIterator<List<T>, T>(list.iterator(), i -> i.iterator());
 * </pre>
 * 
 * The template parameters in {@code <List<T>, T>} are strictly for information
 * purposes and may be dropped. With the convenience method
 * {@link NestedIterator#flatten(Iterator)}, the above code may be written in a
 * more expressive manner:
 * 
 * <pre>
 * Iterator<T> it = NestedIterator.flatten(list.iterator());
 * </pre>
 * 
 * 
 * For another example, imagine you have a {@code List<String> departments} and
 * a member function<br>
 * {@code List<Person> getEmployees(String department)} in the caller class.
 * Then,
 * 
 * <pre>
 * Iterator<Person> it = new NestedIterator<String, Person>(departments.iterator(), s -> getEmployees(s).iterator());
 * </pre>
 * 
 * iterates over all employees in all departments. Again, there is more
 * expressive manner of writing this by using
 * {@link #iterable(Iterator, Function)}:
 * 
 * <pre>
 * Iterator<Person> it = NestedIterator.iterable(departments.iterator(), this::getEmployees);
 * </pre>
 * 
 * The third frequently encountered example is to combine several Iterators into
 * one. Suppose we have {@code Iterator<T> it1, it2, it3}. Then
 * 
 * <pre>
 * Iterator<Person> it = NestedIterator.merge(it1, it2, it3);
 * </pre>
 * 
 * iterates over all elements in {@code it1}, then in {@code it2}, and finally
 * in {@code it3}.
 * 
 * The {@link NestedIterator} can be generalised to an arbitrary number of
 * hierarchy levels. This is done in {@link Tree} and its implementations.
 * 
 * @author mykhailo.saienko
 *
 * @param <T> The type of elements returned by the original Iterator
 * @param <U> The type of elements returned by the created Iterator
 */
public class NestedIterator<T, U> implements Iterator<U> {

	private final Iterator<T> root;
	private final Function<T, Iterator<U>> gen;
	private Iterator<U> leaf;

	private U nextValid;
	private boolean nextSet;

	/**
	 * Glues several instances of {@code Iterator<T>} together and creates an
	 * Iterator<T> which iterates over the elements of the first iterator, then over
	 * those of the second one, etc., until the last element of the last iterator is
	 * encountered.
	 * 
	 * @param its variable number of iterators. Nulls are allowed and will be simply
	 *            ignored while iterating.
	 * @return
	 */
	@SafeVarargs
	public static <T> Iterator<T> merge(Iterator<T>... its) {
		return new NestedIterator<>(Arrays.asList(its).iterator(), i -> i);
	}

	/**
	 * Creates a {@code NestedIterator<T, U>} over an {@code Iterator<T>}, whose
	 * elements are mapped to arrays of type {@code U[]} by a given
	 * generator-function.
	 * 
	 * @param it
	 * @param gen
	 * @return
	 */
	public static <T, U> NestedIterator<T, U> array(Iterator<T> it, Function<T, U[]> gen) {
		return new NestedIterator<>(it, t -> Arrays.stream(gen.apply(t)).iterator());
	}

	/**
	 * Creates a {@code NestedIterator<T, U>} over an {@code Iterator<T>}, whose
	 * elements are mapped to {@code Iterable<U>} by a given generator-function.
	 * 
	 * @param it
	 * @param gen
	 * @return
	 */
	public static <T, U> NestedIterator<T, U> iterable(Iterator<T> it, Function<T, ? extends Iterable<U>> gen) {
		return new NestedIterator<>(it, t -> gen.apply(t).iterator());
	}

	/**
	 * Creates a flattened-up {@code NestedIterator<T, U>} over an
	 * {@code Iterator<T>} whose elements are of type {@code Iterable<U>}.
	 * 
	 * @param it
	 * @param gen
	 * @return
	 */
	public static <T, U extends Iterable<T>> NestedIterator<U, T> flatten(Iterator<U> source) {
		return new NestedIterator<>(source, t -> t.iterator());
	}

	/**
	 * 
	 * @param it  the original iterator
	 * @param gen Iterator generator for values returned by the primary iterator,
	 *            not null. However, may return nulls which are then ignored.
	 */
	public NestedIterator(Iterator<T> it, Function<T, Iterator<U>> gen) {
		this.root = it;
		this.gen = gen;
		leaf = null;
	}

	@Override
	public boolean hasNext() {
		return nextSet || findNextValid();
	}

	@Override
	public U next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		nextSet = false;
		return nextValid;
	}

	@Override
	public void remove() {
		if (nextSet || leaf == null) {
			throw new IllegalStateException("remove() can only be called after next() and before hasNext()");
		}
		leaf.remove();
	}

	private boolean findNextValid() {
		while ((leaf == null || !leaf.hasNext()) && root.hasNext()) {
			leaf = gen.apply(root.next());
		}
		// we have encountered the last one
		if (leaf == null || !leaf.hasNext()) {
			return false;
		}
		nextSet = true;
		nextValid = leaf.next();
		return true;
	}
}
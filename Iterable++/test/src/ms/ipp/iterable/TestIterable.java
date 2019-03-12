package ms.ipp.iterable;

import static java.util.Arrays.asList;
import static ms.ipp.Iterables.count;
import static ms.ipp.Iterables.distinct;
import static ms.ipp.Iterables.toBiIt;
import static ms.ipp.base.KeyValue.KVP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import ms.ipp.base.KeyValue;
import ms.ipp.iterable.BiIterable;
import ms.ipp.iterator.MappedIterator;
import ms.ipp.iterator.NestedIterator;

public class TestIterable {

	@Test(expected = IllegalStateException.class)
	public void testNested() {
		Map<String, List<Integer>> source = new TreeMap<>();
		source.put("a", new ArrayList<>());
		source.put("b", new ArrayList<>(Arrays.asList(1)));
		source.put("c", new ArrayList<>());
		source.put("d", new ArrayList<>(Arrays.asList(1, 2)));
		source.put("e", new ArrayList<>());
		BiIterable<String, Integer> it = createNested(source);

		// Counting
		Assert.assertEquals(3, count(it));

		// Retrieval
		assertIterator(it.iterator(), KVP("b", 1), KVP("d", 1), KVP("d", 2));

		// Remove fist
		Iterator<Entry<String, Integer>> iterator = it.iterator();
		iterator.next();
		iterator.remove();
		assertIterator(iterator, KVP("d", 1), KVP("d", 2));
		assertIterator(it.iterator(), KVP("d", 1), KVP("d", 2));

		// Remove last
		iterator = it.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertIterator(iterator);
		assertIterator(it.iterator(), KVP("d", 1));

		// Remove before calling next should result into an IllegalStateError
		it.iterator().remove();
	}

	private BiIterable<String, Integer> createNested(Map<String, List<Integer>> source) {
		return () -> new NestedIterator<>(source.entrySet().iterator(),
				e -> new MappedIterator<>(e.getValue().iterator(), i -> new KeyValue<>(e.getKey(), i)));
	}

	@Test(expected = IllegalStateException.class)
	public void testEmptyNested() {
		Map<String, List<Integer>> source = new TreeMap<>();
		source.put("a", new ArrayList<>());
		BiIterable<String, Integer> it = createNested(source);
		// Counting
		Assert.assertEquals(0, count(it));
		Assert.assertTrue(!it.iterator().hasNext());
		// Remove before calling next should result into an IllegalStateError
		it.iterator().remove();
	}

	@Test(expected = IllegalStateException.class)
	public void testDistinct() {
		BiIterable<String, Integer> distinct = createTestDistinct();

		// Counting
		Assert.assertEquals(3, count(distinct));

		// Retrieval
		assertIterator(distinct.iterator(), KVP("a", 1), KVP("b", 2), KVP("c", 4));

		// Remove first
		Iterator<Entry<String, Integer>> it = distinct.iterator();
		it.next();
		it.remove();
		// although this iterator has deleted the KVP a=1...
		assertIterator(it, KVP("b", 2), KVP("c", 4));
		// ...globally, we have another KVP with the same key a=3.
		assertIterator(distinct.iterator(), KVP("b", 2), KVP("a", 3), KVP("c", 4));

		// Remove last
		it = distinct.iterator();
		it.next();
		it.next();
		it.next();
		it.remove();
		// no values must remain in the iterator
		assertIterator(it);
		// ... but two values are still in the iterable
		assertIterator(distinct.iterator(), KVP("b", 2), KVP("a", 3));

		// Remove before calling next should result into an IllegalStateError
		distinct.iterator().remove();
	}

	private BiIterable<String, Integer> createTestDistinct() {
		List<Entry<String, Integer>> map = new ArrayList<>(asList(KVP("a", 1), KVP("b", 2), KVP("a", 3), KVP("c", 4)));
		return toBiIt(distinct(map, Entry::getKey));
	}

	@SafeVarargs
	public static <T> void assertIterator(Iterator<T> it, T... entries) {
		for (T t : entries) {
			Assert.assertEquals(it.next(), t);
		}
		Assert.assertTrue(!it.hasNext());
	}

	public static <T, U> void assertKV(T expKey, U expValue, Entry<T, U> entry) {
		Assert.assertEquals(expKey, entry.getKey());
		Assert.assertEquals(expValue, entry.getValue());
	}
}

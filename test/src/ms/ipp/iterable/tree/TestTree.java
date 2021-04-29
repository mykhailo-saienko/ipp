package ms.ipp.iterable.tree;

import static ms.ipp.Iterables.count;
import static ms.ipp.Iterables.mapped;
import static ms.ipp.base.KeyValue.KVP;
import static ms.ipp.iterable.TestIterable.assertIterator;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import ms.ipp.base.KeyValue;
import ms.ipp.iterable.BiIterable;

public class TestTree {
	@Test
	public void testPeeks() {
		DelegatingTree<Number> entity = createSimpleDelegatingTree();

		// normal priority-based retrieval
		Assert.assertEquals(1, entity.peek("a"));
		Assert.assertEquals(2, entity.peek("b"));
		Assert.assertEquals(5.0, entity.peek("c"));

		// class-specific peeks
		Assert.assertEquals((Integer) 1, entity.get(Integer.class).peek("a"));
		Assert.assertEquals((Integer) 2, entity.get(Integer.class).peek("b"));
		Assert.assertEquals(null, entity.get(Integer.class).peek("c"));

		Assert.assertEquals((Double) 3.0, entity.get(Double.class).peek("a"));
		Assert.assertEquals((Double) 4.0, entity.get(Double.class).peek("b"));
		Assert.assertEquals((Double) 5.0, entity.get(Double.class).peek("c"));
	}

	@Test
	public void testMembers() {
		DelegatingTree<Number> entity = createSimpleDelegatingTree();
		BiIterable<String, Number> members = entity.members(Number.class);
		// Implicitly test if DelegatingTree::members(Clazz) produces correct
		// iterators, as we generate one for count and one for assertIterator
		Assert.assertEquals(3, count(members));
		assertIterator(members.iterator(), KVP("a", 1), KVP("b", 2), KVP("c", 5.0));

		entity.setDistinct(false);
		members = entity.members(Number.class);
		Assert.assertEquals(6, count(members));
		assertIterator(members.iterator(), KVP("a", 1), KVP("a", 1), KVP("b", 2), KVP("a", 3.0), KVP("b", 4.0),
				KVP("c", 5.0));
	}

	@Test
	public void testIllegalSet() {
		DelegatingTree<Number> entity = createSimpleDelegatingTree();
		// Cannot set Strings in a Tree with base class Number
		Assert.assertEquals(null, entity.set("x", "b"));
		// Total number of members has not changed
		entity.setDistinct(false);
		Assert.assertEquals(6, count(entity.members(Number.class)));
	}

	@Test
	public void testMultipleContainer() {
		DelegatingTree<Number> d = new DelegatingTree<>(Number.class);
		d.add(Integer.class, new StdTree<>(Integer.class));
		d.add(Double.class, new StdMultiTree<>(Double.class));

		d.get(Double.class).set("d", 3.0);
		d.get(Double.class).set("d", 4.0);
		d.get(Double.class).set("d", 3.0);
		// We expect to have 2 members: d=3.0, d=4.0
		assertIterator(d.get(Double.class).iterator(), KVP("d", 3.0), KVP("d", 4.0));
		// The delegating entity is distinct, it returns only the first 'd'
		assertIterator(d.iterator(Double.class), KVP("d", 3.0));
		assertIterator(d.members(Double.class).iterator(), KVP("d", 3.0));
	}

	@Test
	public void testSimpleRecursion() {
		// T=TestInterface, E=Tree, X=!T & !E. Structure:
		// X a,
		// E b {T b1(1), E b2}
		// T c(2)
		// TE d(3) {X d1, T d2(4)}
		StdTree<Object> root = new StdTree<>(Object.class);
		root.set("a", 30);

		StdTree<Object> b = new StdTree<>(Object.class);
		b.set("b1", new TestLeaf(1));
		b.set("b2", new StdTree<>(String.class));
		root.set("b", b);

		root.set("c", new TestLeaf(2));

		TestContainer<Object> d = new TestContainer<>(Object.class, 3);
		d.set("d1", 31);
		d.set("d2", new TestLeaf(4));
		root.set("d", d);

		// Recursive traversal with filtering
		BiIterable<String, TestInterface> recursive = root.recursive(TestInterface.class);
		Assert.assertEquals(4, count(recursive));
		BiIterable<String, Integer> mapped = mapped(recursive, (t, u) -> new KeyValue<>(t, u.getValue()));
		assertIterator(mapped.iterator(), KVP("b.b1", 1), KVP("c", 2), KVP("d", 3), KVP("d.d2", 4));

		// Recursive retrieval
		Assert.assertEquals(31, root.get("d.d1"));
		Assert.assertEquals(30, root.get("a"));

		// Recursive initialisation
		Assert.assertEquals("test", root.set("b.b2.b11", "test"));
		Assert.assertEquals(null, root.set("b.b2.b12", 3));
		assertIterator(root.recursive(String.class).iterator(), KVP("b.b2.b11", "test"));

		// Recursive deletion
		root.delete("b.b1");
		recursive = root.recursive(TestInterface.class);
		Assert.assertEquals(3, count(recursive));
		mapped = mapped(recursive, (t, u) -> new KeyValue<>(t, u.getValue()));
		assertIterator(mapped.iterator(), KVP("c", 2), KVP("d", 3), KVP("d.d2", 4));
	}

	/**
	 * Creates a DelegatingTree<Number> with the following structure: <br>
	 * 1. (generic): "a"=1<br>
	 * 2. Integer: "a"=1, "b"=2<br>
	 * 3. Double: "a"=3.0, "b"=4.0, "c"=5.0
	 * 
	 * @return
	 */
	private DelegatingTree<Number> createSimpleDelegatingTree() {
		DelegatingTree<Number> tree = new DelegatingTree<>(Number.class);
		tree.add(new StdTree<>(Number.class));
		tree.add(Integer.class, new StdTree<>(Integer.class));
		tree.add(Double.class, new StdTree<>(Double.class));

		tree.set("a", 1); // not class-specific
		tree.get(Integer.class).set("a", 1); // class-specific
		tree.get(Integer.class).set("b", 2);
		tree.get(Double.class).set("a", 3.0);
		tree.get(Double.class).set("b", 4.0);
		tree.get(Double.class).set("c", 5.0);
		return tree;
	}

	private static interface TestInterface {
		Integer getValue();
	}

	private static class TestLeaf implements TestInterface {
		private final Integer value;

		public TestLeaf(Integer value) {
			this.value = value;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "'TL:" + getValue() + "'";
		}
	}

	private static class TestContainer<F> extends StdTree<F> implements TestInterface {
		private final Integer value;

		public TestContainer(Class<F> clazz, Integer value) {
			super(clazz);
			this.value = value;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "'TE:" + getValue() + "'";
		}
	}
}

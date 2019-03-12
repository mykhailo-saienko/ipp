package ms.ipp.iterable.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import ms.ipp.base.KeyValue;
import ms.ipp.iterator.EmptyIterator;
import ms.ipp.iterator.OneIterator;

public class StdTree<F> extends AbstractTree<F> {
	private final HashMap<String, F> entities;

	public StdTree(Class<F> clazz) {
		super(clazz);
		entities = new HashMap<>();
	}

	@Override
	public Iterator<Entry<String, F>> iterator(String name) {
		F value = doPeek(name, getBaseClass());
		// the may be at most one element with a given name (and class)
		if (value != null) {
			Entry<String, F> elem = new KeyValue<>(name, value);
			return new OneIterator<>(elem).setDeleter(() -> entities.remove(name));
		} else {
			return new EmptyIterator<>();
		}
	}

	@Override
	public Iterator<Entry<String, F>> iterator() {
		return entities.entrySet().iterator();
	}

	@Override
	public F doPeek(String name) {
		return entities.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T doSetImpl(String name, T value, Class<T> clazz) {
		entities.put(name, (F) value);
		return value;
	}

	// Overridden for efficiency reasons
	@Override
	public void doDeleteImpl(String name, F value) {
		entities.remove(name);
	}
}

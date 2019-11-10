package ms.ipp.base;

import static ms.ipp.Iterables.isEqualOrNull;

import java.util.Map;

/**
 * A concrete implementation of {@code Map.Entry<T, V>}
 * 
 * @author mykhailo.saienko
 *
 * @see Map.Entry
 * @param <T>
 * @param <V>
 */
public class KeyValue<T, V> implements Map.Entry<T, V> {
	private T key;
	private V value;

	public static <U, W> KeyValue<U, W> KVP(U key, W value) {
		return new KeyValue<U, W>(key, value);
	}

	/**
	 * Creates a new KeyValue with both the key and the value equal to null.
	 */
	public KeyValue() {
		this(null, null);
	}

	/**
	 * Creates a copy of existing {@code Map.Entry}.
	 * 
	 * @param source the source Map.Entry, not null
	 */
	public KeyValue(Map.Entry<T, V> source) {
		this(source.getKey(), source.getValue());
	}

	/**
	 * Creates a new KeyValue with given key and value.
	 * 
	 * @param key
	 * @param value
	 */
	public KeyValue(T key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public V setValue(V value) {
		return this.value = value;
	}

	@Override
	public V getValue() {
		return value;
	}

	/**
	 * Sets a new key.
	 * 
	 * @param key
	 */
	public void setKey(T key) {
		this.key = key;
	}

	@Override
	public T getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyValue)) {
			return false;
		}
		KeyValue<?, ?> k = (KeyValue<?, ?>) obj;
		return isEqualOrNull(key, k.key) && isEqualOrNull(value, k.value);
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}
}

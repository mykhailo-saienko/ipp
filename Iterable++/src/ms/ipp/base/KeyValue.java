package ms.ipp.base;

import static ms.ipp.Iterables.isEqualOrNull;

import java.util.Map;

public class KeyValue<T, V> implements Map.Entry<T, V> {
	private T key;
	private V value;

	public static <U, W> KeyValue<U, W> KVP(U key, W value) {
		return new KeyValue<U, W>(key, value);
	}

	public KeyValue() {
		this(null, null);
	}

	public KeyValue(Map.Entry<T, V> source) {
		this(source.getKey(), source.getValue());
	}

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

	public void setKey(T key) {
		this.key = key;
	}

	@Override
	public T getKey() {
		return key;
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

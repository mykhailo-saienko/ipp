package ms.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ms.ipp.Iterables;

public class HashIndex<T> implements Index<T> {
    private final Map<T, Set<String>> index;
    private final Class<T> clazz;

    public HashIndex(Class<T> clazz) {
        this.clazz = clazz;
        index = new HashMap<>();
    }

    @Override
    public void insert(T key, String id) {
        Iterables.getInsert(key, index, HashSet::new).add(id);
    }

    @Override
    public Set<String> queryEquals(T key) {
        var result = index.get(key);
        return result == null ? new HashSet<>() : result;
    }

    @Override
    public Class<T> getValueClass() {
        return clazz;
    }

    @Override
    public void remove(T value, String id) {
        var set = index.get(value);
        if (set == null || !set.remove(id)) { // This should never happen!
            throw new IllegalArgumentException("Index key '" + value + "' for id '" + id
                                               + "' not found");
        }
        if (set.isEmpty()) {
            index.remove(value);
        }
    }

}

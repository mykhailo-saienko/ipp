package ms.db;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import ms.ipp.Iterables;

public class SortedIndex<T> implements Index<T> {

    private final TreeMap<T, Set<String>> index;
    private final Class<T> clazz;

    public SortedIndex(Comparator<? super T> comparator, Class<T> clazz) {
        this.clazz = clazz;
        index = new TreeMap<>(comparator);
    }

    @Override
    public void insert(T key, String id) {
        Iterables.getInsert(key, index, HashSet::new).add(id);
    }

    public Set<String> queryIntervalUnsafe(Object smallest,
                                           boolean includeSmallest,
                                           Object largest,
                                           boolean includeLargest) {
        return queryInterval(getValueClass().cast(smallest),
                             includeSmallest,
                             getValueClass().cast(largest),
                             includeLargest);
    }

    public Set<String> queryInterval(T smallest,
                                     boolean includeSmallest,
                                     T largest,
                                     boolean includeLargest) {
        if (index.isEmpty()) {
            return new HashSet<>();
        }
        if (smallest == null) {
            smallest = index.firstKey();
            includeSmallest = true;
        }
        if (largest == null) {
            largest = index.lastKey();
            includeLargest = true;
        }
        var result = index.subMap(smallest, includeSmallest, largest, includeLargest);
        return Iterables.union(result.values());
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
        if (set == null) {
            throw new IllegalArgumentException("Index key '" + value + "' for id '" + id
                                               + "' not found");
        }
        if (!set.remove(id)) {
            throw new IllegalArgumentException("Index id '" + id + "' for key '" + value
                                               + "' not found");
        }
        if (set.isEmpty()) {
            index.remove(value);
        }
    }
}

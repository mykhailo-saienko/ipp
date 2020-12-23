package ms.db;

import static ms.ipp.Iterables.filter;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import ms.db.MergedQuery.Type;
import ms.ipp.Iterables;

public class Table<T> {

    public static enum InsertBehaviour {
        OVERWRITE, // Overwrites existing value
        RETURN, // Returns without raising an error
        ERROR // Raises an error
    }

    private final Map<String, T> mainIndex;

    private final Map<String, Index<?>> indexes;
    private final Map<String, SortedIndex<?>> sortedIndexes;

    private final Map<String, Function<T, ?>> valueGenerators;

    public Table(boolean sorted) {
        indexes = new HashMap<>();
        sortedIndexes = new HashMap<>();
        valueGenerators = new HashMap<>();

        mainIndex = sorted ? new TreeMap<>() : new HashMap<>();
    }

    public static void main(String[] args) {
        Query q = Query.less("test", "asdf").and(Query.larger("test2", "baf"));
        Table<Query> t = new Table<>(true);
        t.addSortedIndex("test", q1 -> q1.toString(), String.class);
        t.addSortedIndex("test2", q2 -> q2.toString(), String.class);
        Collection<Query> result = t.query(q);
        System.out.println(result);
    }

    public T queryById(String id) {
        return mainIndex.get(id);
    }

    public Set<String> queryIds(Query query) {
        Set<String> idx = queryIndex(query);
        return filter(mainIndex, (k, v) -> idx.contains(k)).keySet();
    }

    public String queryUniqueId(Query query) {
        var result = queryIds(query);
        if (result.size() != 1) {
            throw new IllegalArgumentException("Expected unique id for query '" + query
                                               + "' but got " + result.size());
        }
        return result.iterator().next();
    }

    public Collection<T> query(Query query) {
        Set<String> idx = queryIndex(query);
        return filter(mainIndex, (k, v) -> idx.contains(k)).values();
    }

    public T queryUnique(Query query) {
        var result = query(query);
        if (result.size() != 1) {
            throw new IllegalArgumentException("Expected unique value for query '" + query
                                               + "' but got " + result.size());
        }
        return result.iterator().next();
    }

    public void insert(Map<String, T> values, InsertBehaviour onDuplicate) {
        // NOTE: This method is not atomic anymore
        for (var pair : values.entrySet()) {
            insert(pair.getKey(), pair.getValue(), onDuplicate);
        }
    }

    public void insert(String id, T value, InsertBehaviour onDuplicate) {
        // Check for duplicates
        boolean shouldRemove = false;
        if (mainIndex.containsKey(id)) {
            if (onDuplicate == InsertBehaviour.ERROR) {
                throw new IllegalArgumentException("Value with id '" + id + "' already exists");
            } else if (onDuplicate == InsertBehaviour.OVERWRITE) {
                // Do not remove yet...
                shouldRemove = true;
            } else {
                return;
            }
        }
        // ... obtain all key values (if any of the functions throws an exception, we are
        // still left with consistent state). Since generators were compiler-checked, we are sure
        // the generated values will be accepted by all indexes
        Map<String, ?> keys = Iterables.mapValues(valueGenerators, f -> f.apply(value));

        // now nothing can go wrong. Perform operation
        if (shouldRemove) {
            remove(id, true); // shouldn't throw (especially since the generators didn't throw)
        }

        // Add new value
        mainIndex.put(id, value);
        for (var pair : indexes.entrySet()) {
            pair.getValue().insertUnsafe(keys.get(pair.getKey()), id);
        }
    }

    public void remove(String id, boolean errorOnMissing) {
        // using 'containsKey' allows support for null values.
        if (!mainIndex.containsKey(id)) {
            if (errorOnMissing) {
                throw new IllegalArgumentException("Unknown id '" + id + "'");
            } else {
                return;
            }
        }
        // don't delete the value yet ...
        T value = mainIndex.get(id);

        // ... first obtain all key values (if any of the functions throws an exception, we are
        // still left with consistent state). Since generators were compiler-checked, we are sure
        // the generated values will be accepted by all indexes
        Map<String, ?> keys = Iterables.mapValues(valueGenerators, f -> f.apply(value));

        // now nothing can go wrong. Delete everything.
        for (var pair : indexes.entrySet()) {
            pair.getValue().removeUnsafe(keys.get(pair.getKey()), id);
        }
        mainIndex.remove(id);
    }

    public <U> void addIndex(String indexName, Function<T, U> generator, Class<U> clazz) {
        doAddIndex(indexName, generator, new HashIndex<>(clazz));
    }

    public <U extends Comparable<U>> void addSortedIndex(String indexName,
                                                         Function<T, U> generator,
                                                         Class<U> clazz) {
        addSortedIndex(indexName, generator, (s1, s2) -> s1.compareTo(s2), clazz);
    }

    public <U> void addSortedIndex(String indexName,
                                   Function<T, U> generator,
                                   Comparator<U> comparator,
                                   Class<U> clazz) {
        SortedIndex<U> index = new SortedIndex<>(comparator, clazz);
        doAddIndex(indexName, generator, index);
        sortedIndexes.put(indexName, index);
    }

    public void removeIndex(String indexName) {
        if (!indexes.containsKey(indexName)) {
            throw new IllegalArgumentException("Unknown index '" + indexName + "'");
        }
        indexes.remove(indexName);
        sortedIndexes.remove(indexName);
        valueGenerators.remove(indexName);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ==================================== PRIVATE MEMBERS ==================================== //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private <U> void doAddIndex(String indexName, Function<T, U> generator, Index<U> index) {
        if (indexes.containsKey(indexName)) {
            throw new IllegalArgumentException("Index '" + indexName + "' already exists");
        }
        indexes.put(indexName, index);
        valueGenerators.put(indexName, generator);

        // fill the index with all entries
        for (var pair : mainIndex.entrySet()) {
            index.insert(generator.apply(pair.getValue()), pair.getKey());
        }
    }

    private Set<String> queryIndex(Query query) {
        if (query instanceof MergedQuery) {
            return queryMerged((MergedQuery) query);
        } else if (query instanceof RangeQuery) {
            return queryRange((RangeQuery<?>) query);
        } else if (query instanceof EqualsQuery) {
            return queryHash((EqualsQuery<?>) query);
        } else {
            throw new IllegalArgumentException("Unsupported query type '"
                                               + query.getClass().getSimpleName() + "'");
        }
    }

    private Set<String> queryRange(RangeQuery<?> query) {
        var index = sortedIndexes.get(query.getIndexName());
        if (index == null) {
            throw new IllegalArgumentException("Unknown sorted index '" + query.getIndexName()
                                               + "'");
        }
        return index.queryIntervalUnsafe(query.getSmallest(),
                                         query.isIncludeSmallest(),
                                         query.getLargest(),
                                         query.isIncludeLargest());
    }

    private Set<String> queryHash(EqualsQuery<?> query) {
        var index = indexes.get(query.getIndexName());
        if (index == null) {
            throw new IllegalArgumentException("Unknown index '" + query.getIndexName() + "'");
        }
        return index.queryEqualsUnsafe(query.getValue());
    }

    private Set<String> queryMerged(MergedQuery query) {
        var idList = Iterables.map(query.getSubqueries(), this::queryIndex);
        if (query.getType() == Type.AND) {
            return Iterables.intersection(idList);
        } else {
            return Iterables.union(idList);
        }
    }
}// Table

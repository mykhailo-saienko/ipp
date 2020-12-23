package ms.db;

import ms.db.MergedQuery.Type;

public interface Query {

    public static <T> RangeQuery<T> between(String indexName,
                                            T smallest,
                                            boolean includeSmallest,
                                            T largest,
                                            boolean includeLargest) {
        return new RangeQuery<>(indexName, smallest, includeSmallest, largest, includeLargest);
    }

    public static <T> RangeQuery<T> less(String indexName, T value) {
        return between(indexName, null, true, value, false);
    }

    public static <T> RangeQuery<T> lessEqual(String indexName, T value) {
        return between(indexName, null, true, value, true);
    }

    public static <T> RangeQuery<T> larger(String indexName, T value) {
        return between(indexName, value, false, null, true);
    }

    public static <T> RangeQuery<T> largerEqual(String indexName, T value) {
        return between(indexName, value, true, null, true);
    }

    public static <T> EqualsQuery<T> equal(String indexName, T value) {
        return new EqualsQuery<>(indexName, value);
    }

    default MergedQuery and(Query... queries) {
        return new MergedQuery(Type.AND, queries);
    }

    default MergedQuery or(Query... queries) {
        return new MergedQuery(Type.OR, queries);
    }
}

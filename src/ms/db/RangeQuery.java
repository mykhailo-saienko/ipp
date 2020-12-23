package ms.db;

public class RangeQuery<T> implements Query {

    private final String indexName;
    private final T smallest;
    private final boolean includeSmallest;
    private final T largest;
    private final boolean includeLargest;

    public RangeQuery(String indexName,
                      T smallest,
                      boolean includeSmallest,
                      T largest,
                      boolean includeLargest) {
        this.indexName = indexName;
        this.smallest = smallest;
        this.includeSmallest = includeSmallest;
        this.largest = largest;
        this.includeLargest = includeLargest;
    }

    public String getIndexName() {
        return indexName;
    }

    public T getSmallest() {
        return smallest;
    }

    public boolean isIncludeSmallest() {
        return includeSmallest;
    }

    public T getLargest() {
        return largest;
    }

    public boolean isIncludeLargest() {
        return includeLargest;
    }
}

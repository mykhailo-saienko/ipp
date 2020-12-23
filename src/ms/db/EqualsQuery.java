package ms.db;

public class EqualsQuery<T> implements Query {

    private final T value;
    private final String indexName;

    public EqualsQuery(String indexName, T value) {
        this.indexName = indexName;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String getIndexName() {
        return indexName;
    }
}

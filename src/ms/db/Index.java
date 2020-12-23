package ms.db;

import java.util.Set;

public interface Index<T> {
    Class<T> getValueClass();

    Set<String> queryEquals(T value);

    void insert(T value, String id);

    void remove(T value, String id);

    default void insertUnsafe(Object value, String id) {
        insert(getValueClass().cast(value), id);
    }

    default void removeUnsafe(Object value, String id) {
        remove(getValueClass().cast(value), id);
    }

    default Set<String> queryEqualsUnsafe(Object value) {
        return queryEquals(getValueClass().cast(value));
    }
}

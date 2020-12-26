package ms.db;

import static ms.db.IndexHelper.cast;

import java.util.Set;

public interface Index<T> {
    Class<T> getValueClass();

    Set<String> queryEquals(T value);

    void insert(T value, String id);

    void remove(T value, String id);

    default void insertUnsafe(Object value, String id) {
        insert(cast(value, getValueClass()), id);
    }

    default void removeUnsafe(Object value, String id) {
        remove(cast(value, getValueClass()), id);
    }

    default Set<String> queryEqualsUnsafe(EqualsQuery<?> query) {
        return queryEquals(cast(query.getIndexName(), query.getValue(), getValueClass()));
    }
}

class IndexHelper {
    public static <T> T cast(Object value, Class<T> clazz) {
        return cast("", value, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(String queryName, Object value, Class<T> clazz) {
        if (value == null || clazz.isInstance(value)) {
            return (T) value;
        } else {
            Object v = queryName + "=" + value;
            throw new IllegalArgumentException("Cannot cast " + v + " from "
                                               + value.getClass().getSimpleName() + " to "
                                               + clazz.getSimpleName());
        }
    }
}
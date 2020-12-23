package ms.db;

import java.util.Arrays;
import java.util.List;

public class MergedQuery implements Query {

    public static enum Type {
        AND, OR
    }

    private final Type type;
    private final List<Query> queries;

    public MergedQuery(Type type, Query... queries) {
        this.type = type;
        this.queries = Arrays.asList(queries);
    }

    public Type getType() {
        return type;
    }

    public List<Query> getSubqueries() {
        return queries;
    }
}

package ms.db;

import static ms.ipp.Iterables.appendList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergedQuery implements Query {

    public static enum Type {
        AND, OR;
    }

    private final Type type;
    private final List<Query> queries;

    public MergedQuery(Type type, Query... queries) {
        this.type = type;
        this.queries = new ArrayList<>(Arrays.asList(queries));
    }

    public MergedQuery add(Query... queries) {
        this.queries.addAll(Arrays.asList(queries));
        return this;
    }

    public Type getType() {
        return type;
    }

    public List<Query> getSubqueries() {
        return queries;
    }

    @Override
    public String toString() {
        return appendList(queries,
                          "(",
                          ")",
                          type.toString(),
                          (q, sb) -> sb.append(" '").append(q).append("' "));
    }
}

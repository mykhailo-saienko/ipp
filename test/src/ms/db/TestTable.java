package ms.db;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static ms.db.Query.between;
import static ms.db.Query.equal;
import static ms.db.Query.larger;
import static ms.db.Query.largerEqual;
import static ms.db.Query.less;
import static ms.db.Query.lessEqual;
import static ms.db.Table.InsertBehaviour.ERROR;
import static ms.db.Table.InsertBehaviour.OVERWRITE;
import static ms.db.Table.InsertBehaviour.RETURN;
import static ms.ipp.Iterables.filterMap;
import static ms.ipp.Iterables.map;
import static ms.ipp.Iterables.mapped;
import static ms.ipp.base.KeyValue.KVP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ms.ipp.base.KeyValue;

public class TestTable {
    // TODO: Idea: Test atomicity on insertion/deletion if generators throw errors
    // TODO: Idea: Test range-query on non-sorted index. Should it generate Exception or just
    // execute a slower exhaustive search/filtering?
    @BeforeEach
    public void setUp() {
        t = new Table<>(true); // for sorted, it is easier to test for elements in the query results
        t.addSortedIndex(AGE, q1 -> q1.age, Integer.class); // no nulls allowed
        // nulls are essentially treated as zeroes.
        t.addSortedIndex(WEIGHT, q1 -> q1.weight, nullsFirst(naturalOrder()), Double.class);

        t.insert(Map.of("1",
                        new Person("John", 20, 70.),
                        "2",
                        new Person("Jane", 25, 55.),
                        "3",
                        new Person("Arny", 28, 104.),
                        "4",
                        new Person("Joe", 32, null)),
                 ERROR);
    }

    @TestFactory
    public Iterable<DynamicTest> testQueries() {
        // Test all basic queries (equality, range, AND, OR).
        return mapped(queryTests,
                      k -> dynamicTest("test Query " + k.getKey(),
                                       () -> testBasicQuery(k.getKey(), k.getValue())));
    }

    @TestFactory
    public List<DynamicTest> testCorrectUniqueQueries() {
        // filter all basic tests with exactly one expected hit
        var filtered = filterMap(queryTests,
                                 q -> q.getValue().size() == 1,
                                 k -> KVP(k.getKey(), k.getValue().get(0)));
        assumeTrue("No correct unique tests detected", !filtered.isEmpty());

        // test for uniqueness
        return map(filtered,
                   k -> dynamicTest("test unique Query " + k.getKey(),
                                    () -> testBasicUniqueQuery(k.getKey(), k.getValue())));
    }

    @TestFactory
    public List<DynamicTest> testWrongUniqueQueries() {
        // filter all basic tests with the number of expected hits != 1 (contrary to
        // testBaseUniqueQueries)
        // filter all basic tests with exactly one expected hit
        var filtered = filterMap(queryTests, q -> q.getValue().size() != 1, k -> k.getKey());
        assumeTrue("No wrong unique tests detected", !filtered.isEmpty());

        // test for uniqueness
        return map(filtered,
                   k -> dynamicTest("test wrong unique Query " + k,
                                    () -> assertThrows(IllegalArgumentException.class,
                                                       () -> t.queryUnique(k))));
    }

    @Test
    public void testWrongValues() {
        // only integers are accepted for integer based indexes
        assertThrows(IllegalArgumentException.class, () -> t.query(equal(AGE, "20")));
    }

    @Test
    public void testQueryById() {
        // this basically ensures the basic integrity of the table
        assertEquals("John", t.queryById("1").name);
        assertEquals("Jane", t.queryById("2").name);
        assertEquals("Arny", t.queryById("3").name);
        assertEquals("Joe", t.queryById("4").name);
    }

    @Test
    public void testInsert() {
        // tests different insertion behaviours
        // throw error on duplicate
        assertThrows(IllegalArgumentException.class,
                     () -> t.insert("1", new Person("x", 5, 10.), ERROR));
        assertEquals(4, t.size()); // nothing has been added
        testBasicQuery(less(WEIGHT, 200.), asList("John", "Jane", "Arny", "Joe"));

        // ignore on duplicate
        t.insert("1", new Person("x", 5, 10.), RETURN);
        assertEquals(4, t.size()); // nothing has been added
        testBasicQuery(less(WEIGHT, 200.), asList("John", "Jane", "Arny", "Joe"));

        // overwrite on duplicate
        t.insert("1", new Person("x", 5, 10.), OVERWRITE);
        testBasicQuery(less(WEIGHT, 200.), asList("x", "Jane", "Arny", "Joe"));
        testBasicQuery(less(WEIGHT, 11.), asList("x", "Joe"));
    }

    @Test
    public void testRemove() {
        // test different insertion behaviours
        t.remove("5", false); // ignore
        assertEquals(4, t.size()); // nothing has been removed
        testBasicQuery(less(WEIGHT, 200.), asList("John", "Jane", "Arny", "Joe"));

        assertThrows(IllegalArgumentException.class, () -> t.remove("5", true)); // error
        assertEquals(4, t.size()); // nothing has been removed
        testBasicQuery(less(WEIGHT, 200.), asList("John", "Jane", "Arny", "Joe"));

        // genuine remove
        t.remove("1", true);
        assertEquals(3, t.size());
        testBasicQuery(lessEqual(WEIGHT, 70.), asList("Jane", "Joe"));
        testBasicQuery(equal(AGE, 20), asList());
    }

    @Test
    public void testAddIndexes() {
        // error on duplicate indexes
        assertThrows(IllegalArgumentException.class, () -> t.addIndex(AGE, p -> null, Date.class));

        // add a new index
        String CATEGORY = "Category";
        t.addIndex(CATEGORY,
                   p -> p.weight == null ? "Strange" : p.weight <= 70. ? "Normal" : "Bulky",
                   String.class);

        // query all three values
        testBasicQuery(equal(CATEGORY, "Strange"), asList("Joe"));
        testBasicQuery(equal(CATEGORY, "Normal"), asList("John", "Jane"));
        testBasicQuery(equal(CATEGORY, "Bulky"), asList("Arny"));

        // remove all normal people
        t.queryIds(equal(CATEGORY, "Normal")).forEach(s -> t.remove(s, true));
        assertEquals(2, t.size());
        testBasicQuery(less(WEIGHT, 200.), asList("Arny", "Joe"));
    }

    @Test
    public void testRemoveIndexes() {
        // error on removing unknown indexes
        assertThrows(IllegalArgumentException.class, () -> t.removeIndex("Unknown"));

        // 1. removing AGE
        t.removeIndex(AGE);

        // weight index still works
        testBasicQuery(largerEqual(WEIGHT, 104.), asList("Arny"));

        // but age doesn't
        assertThrows(IllegalArgumentException.class, () -> t.query(equal(AGE, 20)));
        assertThrows(IllegalArgumentException.class, () -> t.query(larger(AGE, 20)));

        // 2. removing WEIGHT
        t.removeIndex(WEIGHT);

        // weight doesn't work any more either
        assertThrows(IllegalArgumentException.class, () -> t.query(equal(WEIGHT, 20)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ==================================== PRIVATE STUFF ====================================== //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final String WEIGHT = "weight";
    private static final String AGE = "age";
    private Table<Person> t;

    List<KeyValue<Query, List<String>>> queryTests //
            = asList(// basic equality
                     KVP(equal(AGE, 20), asList("John")),
                     KVP(equal(AGE, 34), asList()),
                     KVP(equal(WEIGHT, 55.), asList("Jane")),
                     KVP(equal(WEIGHT, 55.5), asList()),
                     // basic ranges
                     KVP(less(AGE, 20), asList()), //
                     KVP(lessEqual(AGE, 20), asList("John")), //
                     KVP(larger(WEIGHT, 70.), asList("Arny")), //
                     KVP(largerEqual(WEIGHT, 70.), asList("John", "Arny")), //
                     KVP(lessEqual(WEIGHT, 70.), asList("John", "Jane", "Joe")), // nulls are zeroes
                     KVP(between(AGE, 25, true, 28, true), asList("Jane", "Arny")), //
                     // empty intersection
                     KVP(less(AGE, 28).and(larger(WEIGHT, 70.)), asList()),
                     // non-empty intersection
                     KVP(lessEqual(AGE, 28).and(larger(WEIGHT, 70.)), asList("Arny")), //
                     // empty union
                     KVP(less(AGE, 20).or(larger(AGE, 32)), asList()),
                     // non-empty (non-overlapping) union
                     KVP(lessEqual(AGE, 20).or(largerEqual(AGE, 32)), asList("John", "Joe")),
                     // non-empty (overlapping) union
                     KVP(less(AGE, 28).or(lessEqual(WEIGHT, 55.)), asList("John", "Jane", "Joe")));

    private void testBasicQuery(Query query, List<String> expectedNames) {
        assertEquals(expectedNames, map(t.query(query), p -> p.name));
    }

    private void testBasicUniqueQuery(Query query, String expectedName) {
        assertEquals(expectedName, t.queryUnique(query).name);
    }

    /// Just a test class
    private static class Person {
        private final Integer age; // age in years
        private final String name; // name surname
        private final Double weight; // weight in kg

        public Person(String name, Integer age, Double weight) {
            this.name = name;
            this.age = age;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return name + "(age: " + age + ", weight: " + weight + ")";
        }
    }

}

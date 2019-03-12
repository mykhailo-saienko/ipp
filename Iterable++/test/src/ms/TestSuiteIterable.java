package ms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ms.ipp.TestIterables;
import ms.ipp.iterable.TestIterable;
import ms.ipp.iterable.tree.TestEntity;

@RunWith(Suite.class)
@SuiteClasses({ TestIterable.class, TestIterables.class, TestEntity.class })
public class TestSuiteIterable {

}

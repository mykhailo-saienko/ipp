package ms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ms.ipp.TestIterables;
import ms.ipp.iterable.TestIterable;
import ms.ipp.iterable.tree.TestTree;

@RunWith(Suite.class)
@SuiteClasses({ TestIterable.class, TestIterables.class, TestTree.class })
public class TestSuiteIterable {

}

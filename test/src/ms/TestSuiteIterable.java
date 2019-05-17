package ms;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import ms.ipp.TestIterables;
import ms.ipp.iterable.TestIterable;
import ms.ipp.iterable.tree.TestTree;

@RunWith(JUnitPlatform.class)
@SelectClasses({ TestIterable.class, TestIterables.class, TestTree.class })
public class TestSuiteIterable {

}

package ms.ipp.iterable.tree.path;

import ms.ipp.base.KeyValue;

public interface PathManipulator {

	KeyValue<String, String> getRoot(String path);

	int nextLevel(String path, int from);

	String combine(String prefix, String leaf);

	boolean isSimple(String path);
}

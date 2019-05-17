package ms.ipp.iterable.tree.path;

import static ms.ipp.base.KeyValue.KVP;

import ms.ipp.base.KeyValue;

/**
 * This PathManipulator interprets the entire path as a simple name for the
 * immediate child.
 * 
 * @author mykhailo.saienko
 *
 */
public class TrivialPathManipulator extends AbstractPathManipulator {

	/**
	 * Returns the entire name as the root and an empty string as a suffix.
	 */
	@Override
	public KeyValue<String, String> getRoot(String path) {
		return KVP(path, "");
	}

	/**
	 * The entire
	 */
	@Override
	public int nextLevel(String path, int from) {
		return -1;
	}

	@Override
	public boolean isSimple(String path) {
		return true;
	}

}

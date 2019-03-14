package ms.ipp.iterable.tree.path;

import static java.util.Arrays.asList;
import static ms.ipp.Iterables.appendList;

import java.util.List;

import ms.ipp.base.KeyValue;

public class StdPathManipulator extends AbstractPathManipulator {

	public static int nextSep(String path, boolean first, char separator, int fromIndex) {
		return first ? path.indexOf(separator, fromIndex)
				: path.lastIndexOf(separator, path.length() - fromIndex);
	}

	public static KeyValue<String, String> separate(String path, boolean first, char separator) {
		int sep = nextSep(path, first, separator, 0);
		if (sep == -1) {
			return first ? new KeyValue<>(path, "") : new KeyValue<>("", path);
		} else {
			return new KeyValue<>(path.substring(0, sep), path.substring(sep + 1));
		}
	}

	public static KeyValue<String, String> separate(String path, boolean first) {
		return separate(path, first, '.');
	}

	public static String getPackage(String path) {
		return separate(path, false).getKey();
	}

	public static String toSimpleName(String path) {
		return separate(path, false).getValue();
	}

	public static List<String> fromPath(String path) {
		return asList(path.split("\\."));
	}

	public static String toPath(List<String> ids) {
		return appendList(ids, "", "", ".", (t, sb) -> sb.append(t));
	}

	public StdPathManipulator() {
		super();
	}

	public StdPathManipulator(char separator) {
		super(separator);
	}

	@Override
	public KeyValue<String, String> getRoot(String path) {
		return separate(path, true, separator());
	}

	@Override
	public int nextLevel(String path, int from) {
		return nextSep(path, true, separator(), from);
	}

	@Override
	public boolean isSimple(String name) {
		return name != null && !name.contains(separatorAsString());
	}
}

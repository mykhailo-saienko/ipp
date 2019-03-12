package ms.ipp.iterable.tree.path;

import static java.util.Arrays.asList;
import static ms.ipp.Iterables.appendList;

import java.util.List;

import ms.ipp.base.KeyValue;

public class StdPathManipulator implements PathManipulator {

	private final char separator;

	public static int nextSep(String fullName, boolean first, char separator, int fromIndex) {
		return first ? fullName.indexOf(separator, fromIndex)
				: fullName.lastIndexOf(separator, fullName.length() - fromIndex);
	}

	public static KeyValue<String, String> separate(String fullName, boolean first, char separator) {
		int sep = nextSep(fullName, first, separator, 0);
		if (sep == -1) {
			return first ? new KeyValue<>(fullName, "") : new KeyValue<>("", fullName);
		} else {
			return new KeyValue<>(fullName.substring(0, sep), fullName.substring(sep + 1));
		}
	}

	public static KeyValue<String, String> separate(String fullName, boolean first) {
		return separate(fullName, first, '.');
	}

	public static String getPackage(String fullName) {
		return separate(fullName, false).getKey();
	}

	public static String toSimpleName(String fullName) {
		return separate(fullName, false).getValue();
	}

	public static List<String> fromFullName(String fullName) {
		return asList(fullName.split("\\."));
	}

	public static String toFullName(List<String> ids) {
		return appendList(ids, "", "", ".", (t, sb) -> sb.append(t));
	}

	public StdPathManipulator() {
		this('.');
	}

	public StdPathManipulator(char separator) {
		this.separator = separator;
	}

	@Override
	public KeyValue<String, String> getRoot(String name) {
		return separate(name, true, separator);
	}

	@Override
	public int nextLevel(String name, int from) {
		return nextSep(name, true, separator, from);
	}

	@Override
	public String combine(String prefix, String leaf) {
		return prefix == null || prefix.isEmpty() ? leaf : prefix + "." + leaf;
	}

	@Override
	public boolean isSimple(String name) {
		return name != null && !name.contains(".");
	}
}

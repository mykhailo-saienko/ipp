package ms.ipp.iterable.tree.path;

public abstract class AbstractPathManipulator implements PathManipulator {
	private char separator;
	private String sepAsString;

	public AbstractPathManipulator() {
		this('.');
	}

	public AbstractPathManipulator(char separator) {
		setSeparator(separator);
	}

	public void setSeparator(char separator) {
		this.separator = separator;
		sepAsString = String.valueOf(separator);
	}

	public char separator() {
		return separator;
	}

	protected String separatorAsString() {
		return sepAsString;
	}

	@Override
	public String combine(String prefix, String leaf) {
		return prefix == null || prefix.isEmpty() ? leaf : prefix + separator() + leaf;
	}
}

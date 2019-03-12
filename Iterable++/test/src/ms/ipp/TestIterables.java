package ms.ipp;

import static ms.ipp.Iterables.appendList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestIterables {

	@Test
	public void testAppend() {
		List<String> list = Arrays.asList("de", "ma", "goo");
		Assert.assertEquals("", appendList(new ArrayList<>(), "", "", ".", (s, sb) -> sb.append(s)));
		Assert.assertEquals("de", appendList(list.subList(0, 1), "", "", ".", (s, sb) -> sb.append(s)));
		Assert.assertEquals("de.ma", appendList(list.subList(0, 2), "", "", ".", (s, sb) -> sb.append(s)));
		Assert.assertEquals("de.ma.goo", appendList(list.subList(0, 3), "", "", ".", (s, sb) -> sb.append(s)));
	}
}

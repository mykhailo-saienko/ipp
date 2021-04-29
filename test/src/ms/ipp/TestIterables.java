package ms.ipp;

import static ms.ipp.Iterables.appendList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestIterables {

    @Test
    public void testAppend() {
        List<String> list = Arrays.asList("de", "ma", "goo");
        assertEquals("", appendList(new ArrayList<>(), "", "", ".", (s, sb) -> sb.append(s)));
        assertEquals("de", appendList(list.subList(0, 1), "", "", ".", (s, sb) -> sb.append(s)));
        assertEquals("de.ma", appendList(list.subList(0, 2), "", "", ".", (s, sb) -> sb.append(s)));
        assertEquals("de.ma.goo",
                     appendList(list.subList(0, 3), "", "", ".", (s, sb) -> sb.append(s)));
    }

    @Test
    public void testAny() {
        assertTrue(Iterables.any(Arrays.asList(null, "tr"), s -> s == null));
    }
}

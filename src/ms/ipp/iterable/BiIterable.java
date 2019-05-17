package ms.ipp.iterable;

import java.util.Map;

import ms.ipp.Iterables;
import ms.ipp.iterable.tree.Tree;

/**
 * A "convenience" interface for {@code Iterable<Map.Entry<T, U>>}. Introduced
 * for several reasons:
 * <li>It is used extensively by {@link Tree} and its implementations. Besides,
 * the class {@link Iterables} has several methods designed specifically for
 * {@code BiIterable<T,U>}.
 * <li>Typing {@code BiIterable<T,U>} requires less efforts than
 * {@code Iterable<Map.Entry<T,U>>} and is more expressive.
 * 
 * @author mykhailo.saienko
 *
 * @param <T>
 * @param <U>
 */
public interface BiIterable<T, U> extends Iterable<Map.Entry<T, U>> {

}

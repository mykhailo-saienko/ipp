package ms.ipp.base;

/**
 * An abstract functional interface for a {@code Function} which takes three
 * arguments.
 * 
 * @author mykhailo.saienko
 *
 * @param <U>
 * @param <V>
 * @param <W>
 * @param <T>
 */
@FunctionalInterface
public interface TriFunction<U, V, W, T> {
	T apply(U arg0, V arg1, W arg2);
}
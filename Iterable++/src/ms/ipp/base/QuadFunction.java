package ms.ipp.base;

/**
 * An abstract functional interface for a {@code Function} which takes four
 * arguments.
 * 
 * @author mykhailo.saienko
 *
 * @param <U>
 * @param <V>
 * @param <W>
 * @param <X>
 * @param <T>
 */
@FunctionalInterface
public interface QuadFunction<U, V, W, X, T> {
	T apply(U arg0, V arg1, W arg2, X arg3);
}
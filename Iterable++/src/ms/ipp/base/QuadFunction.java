package ms.ipp.base;

public interface QuadFunction<U, V, W, X, T> {
	T apply(U arg0, V arg1, W arg2, X arg3);
}
package ms.ipp.base;

public interface TriFunction<U, V, W, T> {
	T apply(U arg0, V arg1, W arg2);
}
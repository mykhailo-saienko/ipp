package ms.ipp.base;

import static ms.ipp.Iterables.isEqualOrNull;

public class Quartet<T1, T2, T3, T4> {
	private T1 t1;
	private T2 t2;
	private T3 t3;
	private T4 t4;

	public static <U1, U2, U3, U4> Quartet<U1, U2, U3, U4> with(U1 u1, U2 u2, U3 u3, U4 u4) {
		return new Quartet<>(u1, u2, u3, u4);
	}

	public Quartet(T1 t1, T2 t2, T3 t3, T4 t4) {
		this.setT1(t1);
		this.setT2(t2);
		this.setT3(t3);
		this.setT4(t4);
	}

	public T1 getT1() {
		return t1;
	}

	public void setT1(T1 t1) {
		this.t1 = t1;
	}

	public T2 getT2() {
		return t2;
	}

	public void setT2(T2 t2) {
		this.t2 = t2;
	}

	public T3 getT3() {
		return t3;
	}

	public void setT3(T3 t3) {
		this.t3 = t3;
	}

	public T4 getT4() {
		return t4;
	}

	public void setT4(T4 t4) {
		this.t4 = t4;
	}

	@Override
	public int hashCode() {
		return (t1 == null ? 0 : t1.hashCode()) ^ (t2 == null ? 0 : t2.hashCode())
				^ (t3 == null ? 0 : t3.hashCode()) ^ (t4 == null ? 0 : t4.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Quartet)) {
			return false;
		}
		Quartet<?, ?, ?, ?> s = (Quartet<?, ?, ?, ?>) obj;
		return isEqualOrNull(t1, s.t1) && isEqualOrNull(t2, s.t2) && isEqualOrNull(t3, s.t3)
				&& isEqualOrNull(t4, s.t4);
	}

	@Override
	public String toString() {
		return "(" + t1 + ", " + t2 + ", " + t3 + ", " + t4 + ")";
	}
}

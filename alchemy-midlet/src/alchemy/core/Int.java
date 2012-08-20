/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.core;

/**
 * Boxed integer.
 * Works faster then java.lang.Integer due to caching.
 * @author Sergey Basalaev
 */
public class Int {
	private static final Int[] cache;
	
	public static final Int ONE;
	public static final Int ZERO;
	public static final Int M_ONE;
	
	static {
		cache = new Int[256+128];
		for (int i=0; i<cache.length; i++) cache[i] = new Int(i-128);
		M_ONE = cache[127];
		ZERO = cache[128];
		ONE = cache[129];
	}
	
	public final int value;
	
	public Int(int val) { this.value = val; }

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj.getClass() != Int.class) return false;
		return this.value == ((Int)obj).value;
	}

	public int hashCode() { return value; }
	
	public static Int toInt(int i) {
		if (i >= -128 && i < 256) return cache[i+128];
		else return new Int(i);
	}
	
	public String toString() {
		return Integer.toString(value);
	}
}

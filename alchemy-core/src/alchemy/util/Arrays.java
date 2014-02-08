/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013-2014, Sergey Basalaev <sbasalaev@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package alchemy.util;

import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.Process;
import alchemy.system.ProcessKilledException;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;

/**
 * Array-related functions.
 * This class provides functions to work with object arrays
 * and primitive arrays alike.
 *
 * @author Sergey Basalaev
 */
public final class Arrays {

	private Arrays() { }

	/** An instance of zero-size array. */
	public static final Object[] EMPTY = new Object[0];

	/** Constant for array type switches. */
	public static final int AR_OBJECT = 'L';
	/** Constant for array type switches. */
	public static final int AR_BOOLEAN = 'Z';
	/** Constant for array type switches. */
	public static final int AR_BYTE = 'B';
	/** Constant for array type switches. */
	public static final int AR_CHAR = 'C';
	/** Constant for array type switches. */
	public static final int AR_SHORT = 'S';
	/** Constant for array type switches. */
	public static final int AR_INT = 'I';
	/** Constant for array type switches. */
	public static final int AR_LONG = 'J';
	/** Constant for array type switches. */
	public static final int AR_FLOAT = 'F';
	/** Constant for array type switches. */
	public static final int AR_DOUBLE = 'D';

	/**
	 * Copies portion of one array into another.
	 * This method handles the cases when one of arrays
	 * is Object[] and another is array of primitives.
	 */
	public static void arrayCopy(Object src, int srcofs, Object dest, int dstofs, int len) {
		int srctype = src.getClass().getName().charAt(1);
		int desttype = dest.getClass().getName().charAt(1);
		if (srctype == desttype) {
			try {
				System.arraycopy(src, srcofs, dest, dstofs, len);
				return;
			} catch (ArrayStoreException ase) {
				throw new ClassCastException("Arrays of different types");
			}
		} else if (srctype == AR_OBJECT) {
			Object[] array = (Object[]) src;
			switch (desttype) {
				case AR_BOOLEAN: {
					boolean[] za = (boolean[]) dest;
					for (int i=0; i<len; i++) {
						za[dstofs+i] = array[srcofs+i] == Int32.ONE;
					}
					return;
				}
				case AR_BYTE: {
					byte[] ba = (byte[]) dest;
					for (int i=0; i<len; i++) {
						ba[dstofs+i] = (byte) ((Int32)array[srcofs+i]).value;
					}
					return;
				}
				case AR_CHAR: {
					char[] ca = (char[]) dest;
					for (int i=0; i<len; i++) {
						ca[dstofs+i] = (char) ((Int32)array[srcofs+i]).value;
					}
					return;
				}
				case AR_SHORT: {
					short[] sa = (short[]) dest;
					for (int i=0; i<len; i++) {
						sa[dstofs+i] = (short) ((Int32)array[srcofs+i]).value;
					}
					return;
				}
				case AR_INT: {
					int[] ia = (int[]) dest;
					for (int i=0; i<len; i++) {
						ia[dstofs+i] = ((Int32)array[srcofs+i]).value;
					}
					return;
				}
				case AR_LONG: {
					long[] la = (long[]) dest;
					for (int i=0; i<len; i++) {
						la[dstofs+i] = ((Int64)array[srcofs+i]).value;
					}
					return;
				}
				case AR_FLOAT: {
					float[] fa = (float[]) dest;
					for (int i=0; i<len; i++) {
						fa[dstofs+i] = ((Float32)array[srcofs+i]).value;
					}
					return;
				}
				case AR_DOUBLE: {
					double[] da = (double[]) dest;
					for (int i=0; i<len; i++) {
						da[dstofs+i] = ((Float64)array[srcofs+i]).value;
					}
					return;
				}
			}
		} else if (desttype == AR_OBJECT) {
			Object[] array = (Object[]) dest;
			switch (srctype) {
				case AR_BOOLEAN: {
					boolean[] za = (boolean[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = za[i] ? Int32.ONE : Int32.ZERO;
					}
					return;
				}
				case AR_BYTE: {
					byte[] ba = (byte[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = Int32.toInt32(ba[i]);
					}
					return;
				}
				case AR_CHAR: {
					char[] ca = (char[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = Int32.toInt32(ca[i]);
					}
					return;
				}
				case AR_SHORT: {
					short[] sa = (short[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = Int32.toInt32(sa[i]);
					}
					return;
				}
				case AR_INT: {
					int[] ia = (int[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = Int32.toInt32(ia[i]);
					}
					return;
				}
				case AR_LONG: {
					long[] la = (long[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = new Int64(la[i]);
					}
					return;
				}
				case AR_FLOAT: {
					float[] fa = (float[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = new Float32(fa[i]);
					}
					return;
				}
				case AR_DOUBLE: {
					double[] da = (double[]) src;
					for (int i=0; i<len; i++) {
						array[dstofs+i] = new Float64(da[i]);
					}
					return;
				}
			}
		}
		throw new ClassCastException("Not an array");
	}

	/** Returns length of array object. */
	public static int arrayLength(Object array) {
		switch (array.getClass().getName().charAt(1)) {
			case AR_OBJECT:
				return ((Object[])array).length;
			case AR_BOOLEAN:
				return ((boolean[])array).length;
			case AR_BYTE:
				return ((byte[])array).length;
			case AR_CHAR:
				return ((char[])array).length;
			case AR_SHORT:
				return ((short[])array).length;
			case AR_INT:
				return ((int[])array).length;
			case AR_LONG:
				return ((long[])array).length;
			case AR_FLOAT:
				return ((float[])array).length;
			case AR_DOUBLE:
				return ((double[])array).length;
			default:
				throw new ClassCastException("Not an array");
		}
	}

	public static void arraySet(Object array, int index, Object obj) {
		switch (array.getClass().getName().charAt(1)) {
			case AR_OBJECT:
				((Object[])array)[index] = obj;
				return;
			case AR_BOOLEAN:
				((boolean[])array)[index] = obj != Int32.ZERO;
				return;
			case AR_BYTE:
				((byte[])array)[index] = (byte)((Int32)obj).value;
				return;
			case AR_CHAR:
				((char[])array)[index] = (char)((Int32)obj).value;
				return;
			case AR_SHORT:
				((short[])array)[index] = (short)((Int32)obj).value;
				return;
			case AR_INT:
				((int[])array)[index] = ((Int32)obj).value;
				return;
			case AR_LONG:
				((long[])array)[index] = ((Int64)obj).value;
				return;
			case AR_FLOAT:
				((float[])array)[index] = ((Float32)obj).value;
				return;
			case AR_DOUBLE:
				((double[])array)[index] = ((Float64)obj).value;
				return;
			default:
				throw new ClassCastException("Not an array");
		}
	}

	public static Object arrayGet(Object array, int index) {
		switch (array.getClass().getName().charAt(1)) {
			case AR_OBJECT:
				return ((Object[])array)[index];
			case AR_BOOLEAN:
				return ((boolean[])array)[index] ? Int32.ONE : Int32.ZERO;
			case AR_BYTE:
				return Int32.toInt32(((byte[])array)[index]);
			case AR_CHAR:
				return Int32.toInt32(((char[])array)[index]);
			case AR_SHORT:
				return Int32.toInt32(((short[])array)[index]);
			case AR_INT:
				return Int32.toInt32(((int[])array)[index]);
			case AR_LONG:
				return new Int64(((long[])array)[index]);
			case AR_FLOAT:
				return new Float32(((float[])array)[index]);
			case AR_DOUBLE:
				return new Float64(((double[])array)[index]);
			default:
				throw new ClassCastException("Not an array");
		}
	}

	/** Returns new multidimensional array. */
	public static Object newMultiArray(int[] sizes, int type) {
		if (sizes.length == 0) throw new IllegalArgumentException();
		return newMultiArray(sizes, type, 0);
	}

	private static Object newMultiArray(int[] sizes, int type, int level) {
		if (level == sizes.length-1) {
			switch (type) {
				case AR_OBJECT:  return new Object[sizes[level]];
				case AR_BOOLEAN: return new boolean[sizes[level]];
				case AR_BYTE:    return new byte[sizes[level]];
				case AR_CHAR:    return new char[sizes[level]];
				case AR_SHORT:   return new short[sizes[level]];
				case AR_INT:     return new int[sizes[level]];
				case AR_LONG:    return new long[sizes[level]];
				case AR_FLOAT:   return new float[sizes[level]];
				case AR_DOUBLE:  return new double[sizes[level]];
				default: throw new IllegalArgumentException();
			}
		} else {
			Object[] array = new Object[sizes[level]];
			for (int i=0; i<sizes[level]; i++) {
				array[i] = newMultiArray(sizes, type, level+1);
			}
			return array;
		}
	}

	/** Performs quick sort of array in given range using given implementation. */
	public static void qsort(Object[] array, int low, int high, Process p, Function f)
	throws AlchemyException, ProcessKilledException {
		if (low >= high) return;
		int i = low;
		int j = high;
		Object x = array[(low+high)/2];
		while(i <= j) {
			while(((Int32)f.invoke(p, new Object[] {array[i],x})).value < 0) i++;
			while(((Int32)f.invoke(p, new Object[] {array[j],x})).value > 0) j--;
			if (i <= j) {
				Object tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j, p, f);
		if (i < high) qsort(array, i, high, p, f);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(byte[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		byte x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				byte tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(short[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		short x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				short tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(char[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		char x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				char tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}
	/** Performs quick sort of array in given range. */
	public static void qsort(int[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		int x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				int tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(long[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		long x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				long tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(float[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		float x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				float tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}

	/** Performs quick sort of array in given range. */
	public static void qsort(double[] array, int low, int high) {
		if (low >= high) return;
		int i = low;
		int j = high;
		double x = array[(low+high)/2];
		while(i <= j) {
			while(array[i] < x) i++;
			while(array[j] > x) j--;
			if (i <= j) {
				double tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
				i++;
				j--;
			}
		}
		if (low < j)  qsort(array, low, j);
		if (i < high) qsort(array, i, high);
	}
}

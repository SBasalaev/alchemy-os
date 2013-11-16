/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

/**
 * List of elements backed by array.
 * <p>
 * Unlike <code>Vector</code> this class is not synchronized.
 *
 * @author Sergey Basalaev
 */
public final class ArrayList {
	
	private Object[] elements;
	private int size;

	/** Creates new empty ArrayList with default capacity. */
	public ArrayList() {
		this(16);
	}

	/** Creates new empty ArrayList with given capacity. */
	public ArrayList(int capacity) {
		elements = new Object[capacity];
		size = 0;
	}

	private void growBy(int count) {
		int minCapacity = elements.length + count;
		int newCapacity = (elements.length * 3) / 2 + 1;
		if (newCapacity < minCapacity) newCapacity = minCapacity;
		Object[] data = new Object[newCapacity];
		System.arraycopy(elements, 0, data, 0, size);
		elements = data;
	}

	/** Returns number of elements in this list. */
	public int size() {
		return size;
	}

	/** Returns true if this list contains no elements. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Removes all elements from this list. */
	public void clear() {
		for (int i=size-1; i>=0; i--) {
			elements[i] = null;
		}
		size = 0;
	}

	/** Returns element at given index. */
	public Object get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return elements[index];
	}

	/** Returns first element of this list. */
	public Object first() {
		if (size == 0) throw new IndexOutOfBoundsException();
		return elements[0];
	}

	/** Returns last element of this list. */
	public Object last() {
		if (size == 0) throw new IndexOutOfBoundsException();
		return elements[size-1];
	}

	/** Sets new value to the elements of this list. */
	public void set(int index, Object e) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		elements[index] = e;
	}

	/** Inserts element in given position of the list. */
	public void insert(int index, Object e) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException();
		if (elements.length == size)
			growBy(1);
		if (index < size)
			System.arraycopy(elements, index, elements, index+1, size-index);
		elements[index] = e;
		size++;
	}

	/** Inserts elements from given array. */
	public void insertFrom(int index, Object array, int ofs, int len) {
		if (len < 0) len = Arrays.arrayLength(array);
		if (len == 0) return;
		if (elements.length < size + len)
			growBy(len);
		if (index < size)
			System.arraycopy(elements, index, elements, index+len, size-index);
		Arrays.arrayCopy(array, ofs, elements, index, len);
		size += len;
	}

	/** Adds element to the end of the list. */
	public void add(Object e) {
		if (elements.length == size)
			growBy(1);
		elements[size] = e;
		size++;
	}

	/** Adds elements from given array. */
	public void addFrom(Object array, int ofs, int len) {
		if (len < 0) len = Arrays.arrayLength(array);
		if (len == 0) return;
		if (elements.length < size + len)
			growBy(len);
		Arrays.arrayCopy(array, ofs, elements, size, len);
		size += len;
	}

	/** Removes element from given position of the list. */
	public void remove(int index) {
		if (index < 0) index += size;
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
		size--;
		if (index < size)
			System.arraycopy(elements, index+1, elements, index, size-index);
		elements[size] = null;
	}

	/** Finds and removes given element. */
	public boolean remove(Object e) {
		int idx = indexOf(e, 0);
		if (idx >= 0) remove(idx);
		return idx >= 0;
	}

	/** Returns sublist of this list. */
	public ArrayList getRange(int from, int to) {
		if (from < 0) from += size;
		if (to < 0) to += size;
		int len = to-from;
		ArrayList list = new ArrayList(len);
		list.size = len;
		System.arraycopy(elements, from, list.elements, 0, len);
		return list;
	}

	/** Tests whether list contains given element. */
	public boolean contains(Object e) {
		return indexOf(e, 0) >= 0;
	}

	/**
	 * Returns index of the first occurence of given object
	 * after given index.
	 * Returns -1 if the list does not contain it.
	 */
	public int indexOf(Object e, int from) {
		for (int index = from; index < size; index++) {
			if (e == null ? elements[index] == null : e.equals(elements[index])) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Returns index of the first occurence of given object.
	 * Returns -1 if the list does not contain it.
	 */
	public int indexOf(Object e) {
		return indexOf(e, 0);
	}

	/**
	 * Returns index of the last occurence of given object.
	 * Returns -1 if the list does not contain it.
	 */
	public int lastIndexOf(Object e) {
		for (int index = size-1; index >= 0; index--) {
			if (e == null ? elements[index] == null : e.equals(elements[index])) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Copies portion of this list into given array.
	 * 
	 * @param from    index of the first element to copy
	 * @param array   object or promitive array to copy into
	 * @param offset  offset in array to place first elemet
	 * @param len     number of elements to copy
	 */
	public void copyInto(int from, Object array, int offset, int len) {
		if (len < 0) len = Arrays.arrayLength(array);
		if (len == 0) return;
		if (from < 0 || from >= size || len > size || offset < 0 || offset + len > Arrays.arrayLength(array))
			throw new IndexOutOfBoundsException();
		Arrays.arrayCopy(elements, from, array, offset, len);
	}

	/** Copies contents of this list into given array. */
	public void copyInto(Object array) {
		copyInto(0, array, 0, size);
	}

	void buildString(ArrayList dejaVu, StringBuffer buf) {
		if (dejaVu.indexOf(this, 0) >= 0) {
			buf.append("[...]");
		} else {
			dejaVu.add(this);
			buf.append('[');
			for (int i=0; i<size; i++) {
				if (i != 0) buf.append(", ");
				Strings.buildString(elements[i], dejaVu, buf);
			}
			buf.append(']');
			dejaVu.remove(this);
		}
	}

	/** Returns string representation of this list. */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buildString(new ArrayList(), buf);
		return buf.toString();
	}
}

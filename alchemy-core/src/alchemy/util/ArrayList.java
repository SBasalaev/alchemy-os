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
public class ArrayList {
	
	private Object[] elements;
	private int size;
	
	public ArrayList() {
		this(16);
	}
	
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
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public Object get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return elements[index];
	}
	
	public Object first() {
		if (size == 0) throw new IndexOutOfBoundsException();
		return elements[0];
	}
	
	public Object last() {
		if (size == 0) throw new IndexOutOfBoundsException();
		return elements[size-1];
	}
	
	public void set(int index, Object e) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		elements[index] = e;
	}
	
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
	
	public void add(Object e) {
		if (elements.length == size)
			growBy(1);
		elements[size] = e;
		size++;
	}
	
	public void remove(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		size--;
		if (index < size)
			System.arraycopy(elements, index+1, elements, index, size-index);
		elements[size] = null;
	}
	
	public void remove(Object e) {
		int idx = indexOf(e);
		if (idx >= 0) remove(idx);
	}
	
	public int indexOf(Object e) {
		for (int index = 0; index < size; index++) {
			if (e == null ? elements[index] == null : e.equals(elements[index])) {
				return index;
			}
		}
		return -1;
	}

	public int lastIndexOf(Object e) {
		for (int index = size-1; index >= 0; index--) {
			if (e == null ? elements[index] == null : e.equals(elements[index])) {
				return index;
			}
		}
		return -1;
	}
	
	public void copyInto(int from, Object[] array, int offset, int len) {
		if (from < 0 || from >= size || len > size || offset < 0 || offset + len > array.length)
			throw new IndexOutOfBoundsException();
		System.arraycopy(elements, from, array, offset, len);
	}
	
	public void copyInto(Object[] array) {
		copyInto(0, array, 0, size);
	}

	void buildString(ArrayList dejaVu, StringBuffer buf) {
		if (dejaVu.indexOf(this) >= 0) {
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

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buildString(new ArrayList(), buf);
		return buf.toString();
	}
}

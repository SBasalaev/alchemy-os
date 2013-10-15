/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Lock for multithreaded applications.
 *
 * @author Sergey Basalaev
 */
public final class Lock {

	private Thread owner = null;
	private int lockCount = 0;

	public Lock() { }

	public synchronized void lock() throws InterruptedException {
		Thread current = Thread.currentThread();
		if (current == owner) {
			lockCount++;
		} else {
			while (owner != null) wait();
			owner = current;
			lockCount++;
		}
	}

	public synchronized boolean tryLock() {
		Thread current = Thread.currentThread();
		if (owner == null || current == owner) {
			owner = current;
			lockCount++;
			return true;
		} else {
			return false;
		}
	}

	public synchronized void unlock() {
		Thread current = Thread.currentThread();
		if (current == owner) {
			lockCount--;
			if (lockCount == 0) {
				owner = null;
				notify();
			}
		} else {
			throw new IllegalStateException();
		}
	}
}

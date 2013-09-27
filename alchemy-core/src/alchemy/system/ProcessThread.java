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

package alchemy.system;

/**
 * Single thread in a process.
 * @author Sergey Basalaev
 */
public final class ProcessThread extends Thread {

	/** Owner of this thread. */
	private final Process process;
	/** Main function in this thread. */
	private final Function func;
	/** Arguments to be passed to the function. */
	private final Object[] args;
	/** Daemon status of the thread. */
	private final boolean daemon;

	/** Set if thread crashes. */
	private AlchemyException error;
	/** Interrupted status. */
	private boolean interrupted;

	ProcessThread(Process process, Function func, Object[] args, boolean isDaemon) {
		this.process = process;
		this.func = func;
		this.args = args;
		this.daemon = isDaemon;
	}

	public void run() {
		process.threadStarted(this);
		try {
			func.invoke(process, args);
		} catch (AlchemyException ae) {
			error = ae;
		} catch (Throwable t) {
			error = new AlchemyException(t);
			error.addTraceElement(func.name, "uncaught");
		}
		process.threadEnded(this);
	}

	/**
	 * Returns error that caused this thread to stop.
	 * Returns null if thread ended normally or is still running.
	 */
	public AlchemyException getError() {
		return error;
	}

	/** Interrupts this thread. */
	public void interrupt() {
		interrupted = true;
		super.interrupt();
	}

	/** Returns interrupted status of this thread. */
	public boolean isInterrupted() {
		return interrupted;
	}

	/** Returns daemon status of this thread. */
	public boolean isDaemon() {
		return daemon;
	}
}

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

import alchemy.fs.Filesystem;
import alchemy.io.NullInputStream;
import alchemy.io.NullOutputStream;
import alchemy.util.ArrayList;
import alchemy.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connection;

/**
 * Executing process.
 * @author Sergey Basalaev
 */
public final class Process {

	/** Returned by getState() for new process. */
	public static final int NEW = 0;
	/** Returned by getState() for alive running process. */
	public static final int RUNNING = 1;
	/** Returned by getState() for ended process. */
	public static final int ENDED = 5;

	/** Standard input stream. */
	public InputStream stdin;
	/** Standard output stream. */
	public OutputStream stdout;
	/** Standard error stream. */
	public OutputStream stderr;

	/** Parent of this process. */
	private final Process parent;
	/** Environment variables. */
	private HashMap env;
	/** Listeners of this process. */
	private final ArrayList listeners = new ArrayList();
	/** State of this process. */
	private int state = NEW;
	/** Connections owned by this process. */
	private final ArrayList connections = new ArrayList();
	/** Current directory. */
	private String curdir;
	/** Threads of this process. */
	private final ArrayList threads = new ArrayList();
	/** Nondaemon thread count. */
	private int threadcount;

	/** Creates new parentless process. */
	public Process() {
		this.parent = null;
		this.curdir = "";
		this.stdin = new NullInputStream(-1);
		this.stdout = this.stderr = new NullOutputStream();
	}

	/** Creates new process that inherits environment from its parent. */
	public Process(Process parent) {
		this.parent = parent;
		this.curdir = parent.curdir;
		this.stdin = parent.stdin;
		this.stdout = parent.stdout;
		this.stderr = parent.stderr;
	}

	/** Attaches process listener to this process. */
	public void addProcessListener(ProcessListener l) {
		synchronized (listeners) {
			if (!listeners.contains(l)) listeners.add(l);
		}
	}

	/** Detaches process listener from this process. */
	public void removeProcessListener(ProcessListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	/** Returns state of this process. */
	public int getState() {
		return state;
	}

	/**
	 * Returns value of the environment variable.
	 * If variable is not defined then null is returned.
	 */
	public String getEnv(String key) {
		if (env != null) {
			String val = (String)env.get(key);
			if (val != null) return val;
		}
		if (parent != null) {
			return parent.getEnv(key);
		}
		return null;
	}

	/**
	 * Sets new value to the environment variable.
	 * If value is null, resets variable.
	 */
	public void setEnv(String key, String value) {
		if (value == null) {
			// remove variable
			if (env != null) env.remove(key);
		} else {
			// set variable
			if (env == null) env = new HashMap();
			env.set(key, value);
		}
	}

	/**
	 * Registers connection in this process.
	 * All connections registered within process are
	 * closed when the process ends.
	 */
	public void addConnection(Connection conn) {
		synchronized (connections) {
			connections.add(conn);
		}
	}

	/** Removes connection from this process. */
	public void removeConnection(Connection conn) {
		synchronized (connections) {
			connections.remove(conn);
		}
	}

	/** Returns current directory of this process. */
	public String getCurrentDirectory() {
		return curdir;
	}

	/** Sets current directory for this process. */
	public synchronized void setCurrentDirectory(String dir) throws IOException {
		dir = toFile(dir);
		if (!Filesystem.isDirectory(dir))
			throw new IOException("Not a directory: " + dir);
		curdir = dir;
	}

	/** Converts file path to the normalized absolute path. */
	public String toFile(String path) {
		if (path.length() == 0 || path.charAt(0) == '/') {
			return Filesystem.normalize(path);
		} else {
			return Filesystem.normalize(curdir + '/' + path);
		}
	}

	public void kill() {
		synchronized (threads) {
			for (int idx = threads.size()-1; idx >= 0; idx--) {
				((ProcessThread)threads.get(idx)).interrupt();
			}
			threads.clear();
			finalizeProcess();
		}
	}

	/** Called when thread execution starts. */
	void threadStarted(ProcessThread thread) {
		synchronized (threads) {
			threads.add(thread);
			if (!thread.isDaemon()) threadcount++;
		}
	}

	/** Called when thread execution ends. */
	void threadEnded(ProcessThread thread) {
		synchronized (threads) {
			threads.remove(thread);
			if (!thread.isDaemon()) threadcount--;
			if (threadcount == 0) {
				for (int idx = threads.size()-1; idx >= 0; idx--) {
					((ProcessThread)threads.get(idx)).interrupt();
				}
				threads.clear();
			}
			if (threads.isEmpty()) {
				finalizeProcess();
			}
		}
	}

	/**
	 * Flushes output streams and closes all connections.
	 */
	private void finalizeProcess() {
		try { stdout.flush(); } catch (IOException ioe) { }
		try { stderr.flush(); } catch (IOException ioe) { }
		for (int i=connections.size()-1; i >= 0; i--) {
			try {
				((Connection)connections.get(i)).close();
			} catch (IOException ioe) { }
		}
	}
}

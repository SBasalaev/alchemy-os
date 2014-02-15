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

import alchemy.evm.EtherLoader;
import alchemy.fs.Filesystem;
import alchemy.io.NullInputStream;
import alchemy.io.NullOutputStream;
import alchemy.io.UTFReader;
import alchemy.types.Int32;
import alchemy.util.ArrayList;
import alchemy.util.Arrays;
import alchemy.util.HashMap;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connection;

/**
 * Single process in Alchemy OS.
 * Process represents an instance of a program
 * executing in Alchemy environment.
 *
 * <h3>Process lifecycle</h3>
 * Process lifecycle consists of three steps:
 * <ol>
 * <li>
 * <code>NEW</code><br/>
 * This is the state of new process. In this state process
 * environment can be modified using methods
 * <ul>
 * <li><code>setEnv</code>
 * <li><code>setCurrentDirectory</code>
 * <li><code>setPriority</code>
 * </ul>
 * <li>
 * <code>RUNNING</code><br/>
 * This is the state of process which executes program in separate
 * thread. Process becomes <code>RUNNING</code> after executing
 * {@link #start()} method. If the program fails to be loaded then
 * exception is thrown and process remains in <code>NEW</code> state.
 * <li>
 * <code>ENDED</code><br/>
 * The <code>RUNNING</code> process becomes <code>ENDED</code>
 * when the main thread dies. In this state program exit code
 * can be examined with {@link #getExitCode()} method and
 * if program has ended by throwing an exception that exception
 * can be obtained with {@link #getError()} method.
 * </ol>
 *
 * <h3><a name="LibraryLoading"></a>Loading of programs and libraries</h3>
 * Methods that load libraries are <code>start</code>
 * and <code>loadLibrary</code>.
 * Loading of library consists of the following steps:
 * <ol>
 * <li>
 * Library file is resolved from given string argument and
 * searched in a set of paths. If file can not be found then
 * <code>IOException</code> is thrown.
 *   <ul>
 *   <li>
 *   If argument starts with <code>'/'</code> character, then it is regarded
 *   as absolute path.
 *   <li>
 *   If argument contains </code>'/'</code> but not starts with it then it
 *   is regarded as path relative to the {@link #getCurrentDirectory() current directory}.
 *   <li>
 *   If argument does not contain any slashes then search is performed in a
 *   set of paths defined in an environment variables. Method <code>start</code>
 *   reads paths from <code>PATH</code> variable and <code>readLibrary</code>
 *   method reads them from <code>LIBPATH</code>.
 *   These variables should contain colon-separated list of paths.
 *   </ul>
 * <li>
 * If library is already stored in {@link Cache} then it is returned.
 * Caching is based on file name and timestamp. So the cached version is
 * used if file was not changed since the last caching.
 * <li>
 * If cache has no library or has older version of library then file is
 * used to construct new library instance.
 * </ol>
 *
 * @author Sergey Basalaev
 */
public final class Process {

	/** Magic number for Ether libraries. */
	private static final int MAGIC_ETHER = 0xC0DE;
	/** Magic number for native libraries. */
	private static final int MAGIC_NATIVE = ('#' << 8) | '@';
	/** Magic number for interpreter scripts. */
	private static final int MAGIC_INTERPRETER = ('#' << 8) | '!';
	/** Magic number for symbolic links. */
	private static final int MAGIC_LINK = ('#' << 8) | '=';

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
	/**
	 * Killed status of this process.
	 * Read-only, please.
	 */
	public volatile boolean killed;

	/** Parent of this process. */
	private final Process parent;
	/** Command that invoked this process. */
	private final String command;
	/** Command-line arguments passed to this process. */
	private final String[] cmdArgs;
	/**
	 * Environment variables.
	 * <pre>String -&gt; String</pre>.
	 */
	private HashMap env;
	/**
	 * Global variables.
	 * <pre>Library -&gt; String -&gt; Object</pre>
	 */
	private HashMap globals;
	/** Listeners of this process. */
	private final ArrayList listeners = new ArrayList();
	/** Connections owned by this process. */
	private final ArrayList connections = new ArrayList();
	/** Current directory. */
	private String curdir;
	/** Main thread of this process. */
	private ProcessThread mainThread;
	/** Additional threads of this process. */
	private final ArrayList threads = new ArrayList();
	/** Priority of this process. */
	private int priority;

	/**
	 * Creates new parentless process.
	 */
	public Process(String command, String[] args) {
		this.parent = null;
		this.curdir = "";
		this.stdin = new NullInputStream(-1);
		this.stdout = this.stderr = new NullOutputStream();
		this.command = command;
		this.cmdArgs = args;
	}

	/** Creates new process that inherits environment from its parent. */
	public Process(Process parent, String command, String[] args) {
		this.parent = parent;
		this.curdir = parent.curdir;
		this.stdin = parent.stdin;
		this.stdout = parent.stdout;
		this.stderr = parent.stderr;
		this.command = command;
		this.cmdArgs = args;
	}

	public static Process currentProcess() {
		Thread thread = Thread.currentThread();
		if (thread instanceof ProcessThread) {
			return ((ProcessThread)thread).getProcess();
		} else {
			return null;
		}
	}

	/** Returns program name. */
	public String getName() {
		return Filesystem.fileName(command);
	}

	/** Returns command-line arguments. */
	public String[] getArgs() {
		String[] newargs = new String[cmdArgs.length];
		System.arraycopy(cmdArgs, 0, newargs, 0, newargs.length);
		return newargs;
	}

	/** Returns string representation of this process. */
	public String toString() {
		return "Process(" + getName() + ')';
	}

	/**
	 * Returns exit code of this process.
	 * Can be called only in ENDED state.
	 */
	public int getExitCode() {
		if (mainThread == null || mainThread.isAlive()) throw new IllegalStateException();
		return mainThread.getExitCode();
	}

	/**
	 * Returns exception that caused this process to crash.
	 * Returns null if process ended normally.
	 * Can be called only in ENDED state.
	 */
	public AlchemyException getError() {
		if (mainThread == null || mainThread.isAlive()) throw new IllegalStateException();
		return mainThread.getError();
	}

	/**
	 * Starts execution of this process.
	 * Can be called only in NEW state.
	 * If program is successfully instantiated and started,
	 * turns into RUNNING state.
	 */
	public Process start() throws IOException, InstantiationException {
		synchronized (threads) {
			if (mainThread != null) throw new IllegalStateException();
			Library program = loadBinary(command, getEnv("PATH"));
			Function main = program.getFunction("main");
			if (main == null)
				throw new InstantiationException("No main function in " + command);
			mainThread = new ProcessThread(this, main, new Object[] {cmdArgs});
			mainThread.start();
		}
		return this;
	}

	/**
	 * Waits for this process to end and returns its exit code.
	 * Can be called only in RUNNING or ENDED state.
	 */
	public int waitFor() throws InterruptedException {
		if (mainThread == null) throw new IllegalStateException();
		mainThread.join();
		return mainThread.getExitCode();
	}

	/** Searches program or library in given path list and loads it. */
	private Library loadBinary(String libname, String pathlist) throws IOException, InstantiationException {
		// resolve file name and check permissions
		String libfile = resolveFile(libname, pathlist);
		if (libfile == null)
			throw new IOException("File not found: " + libname);
		if (!Filesystem.canExec(libfile))
			throw new SecurityException("Permission denied: " + libfile);

		// search library in cache
		long tstamp = Filesystem.lastModified(libfile);
		Object cachedlib = Cache.get(libfile, tstamp);
		if (cachedlib != null) {
			if (cachedlib instanceof Library)
				return (Library) cachedlib;
			else
				throw new ClassCastException("Unknown library format: " + libfile);
		}

		// read library from file
		InputStream libin = Filesystem.read(libfile);
		Library lib;
		try {
			int magic = (libin.read() << 8) | libin.read();
			switch (magic) {
				case MAGIC_NATIVE: {
					String classname = new UTFReader(libin).readLine();
					try {
						lib = (Library)Class.forName(classname).newInstance();
						break;
					} catch (ClassNotFoundException cnfe) {
						throw new InstantiationException("Not supported in this build of Alchemy OS: " + classname);
					} catch (NoClassDefFoundError cndfe) {
						throw new InstantiationException("Not supported by this device: " + classname);
					} catch (Throwable t) {
						throw new InstantiationException("Not a library class: " + classname);
					}
				}
				case MAGIC_LINK: {
					String filename = new UTFReader(libin).readLine();
					libin.close();
					if (filename.charAt(0) != '/') {
						filename = Filesystem.fileParent(libfile) + '/' + filename;
					}
					lib = loadBinary(filename, pathlist);
					break;
				}
				case MAGIC_INTERPRETER: {
					String intcmd = new UTFReader(libin).readLine();
					String[] intargs = Strings.split(intcmd, ' ', true);
					intcmd = intargs[0];
					System.arraycopy(intargs, 1, intargs, 0, intargs.length-1);
					intargs[intargs.length-1] = libfile;
					lib = new Library();
					lib.putFunction(new InterpreterMain(lib, intcmd, intargs));
					break;
				}
				case MAGIC_ETHER:
					lib = EtherLoader.load(this, libin);
					break;
				default:
					throw new InstantiationException("Unknown library format: " + libfile);
			}
		} finally {
			try { libin.close(); } catch (IOException ioe) { }
		}

		// assign name to the library and put it into the cache
		if (lib != null) {
			if (lib.name == null) lib.name = Filesystem.fileName(libfile);
			Cache.put(libfile, tstamp, lib);
		}
		return lib;
	}

	/**
	 * Loads library with given name.
	 * If library name contains slashes then it is regarded as
	 * absolute or relative path. Otherwise it is searched in
	 * paths specified by LIBPATH environment variable.
	 */
	public Library loadLibrary(String name) throws IOException, InstantiationException {
		return loadBinary(name, getEnv("LIBPATH"));
	}

	/**
	 * Searches file in given list of paths.
	 * If file does not exist then null is returned.
	 *
	 * @param name      file name
	 * @param pathlist  colon separated list of paths
	 */
	public String resolveFile(String name, String pathlist) {
		if (name.length() == 0)
			throw new IllegalArgumentException();
		if (name.indexOf('/') >= 0) {
			name = toFile(name);
			if (Filesystem.exists(name)) return name;
		} else {
			String[] paths = Strings.split(pathlist, ':', true);
			for (int i=0; i<paths.length; i++) {
				String path = paths[i];
				if (path.length() == 0) continue;
				String testname = toFile(path + '/' + name);
				if (Filesystem.exists(testname)) return testname;
			}
		}
		return null;
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
		if (mainThread == null) return NEW;
		if (mainThread.isAlive()) return RUNNING;
		return ENDED;
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
	 * Returns value of the global variable.
	 * If variable is not set, return default value.
	 */
	public Object getGlobal(Library lib, String name, Object dflt) {
		if (globals == null) return dflt;
		Object ret = ((HashMap)globals.get(lib)).get(name);
		if (ret == null) return dflt;
		return ret;
	}

	/**
	 * Sets value to the global variable.
	 */
	public void setGlobal(Library lib, String name, Object value) {
		if (globals == null) globals = new HashMap();
		HashMap vars = (HashMap)globals.get(lib);
		if (vars == null) {
			vars = new HashMap();
			globals.set(lib, vars);
		}
		vars.set(name, value);
	}

	/** Returns current priority of the process. */
	public int getPriority() {
		return priority;
	}

	/** Sets new priority to the process. */
	public void setPriority(int newPriority) {
		if (newPriority < Thread.MIN_PRIORITY || newPriority > Thread.MAX_PRIORITY)
			throw new IllegalArgumentException();
		synchronized (threads) {
			mainThread.setPriority(newPriority);
			for (int i=threads.size()-1; i>=0; i--) {
				((ProcessThread)threads.get(i)).setPriority(newPriority);
			}
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

	/** Creates new thread in this process. */
	public ProcessThread createThread(Function func) {
		return new ProcessThread(this, func, Arrays.EMPTY);
	}

	/** Stops all threads and finalizes process. */
	public void kill() {
		killed = true;
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
		}
	}

	/** Called when thread execution ends. */
	void threadEnded(ProcessThread thread) {
		synchronized (threads) {
			threads.remove(thread);
			if (thread == mainThread) {
				for (int idx = threads.size()-1; idx >= 0; idx--) {
					((ProcessThread)threads.get(idx)).interrupt();
				}
				threads.clear();
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
		for (int i=listeners.size()-1; i >= 0; i--) {
			((ProcessListener)listeners.get(i)).processEnded(this);
		}
	}

	/** Main function of the interpreter script. */
	private static class InterpreterMain extends Function {
		private final String intcmd;
		private final String[] intargs;

		public InterpreterMain(Library owner, String command, String[] args) {
			super(owner, "main");
			this.intcmd = command;
			this.intargs = args;
		}

		public Object invoke(Process p, Object[] args) throws AlchemyException {
			String[] params = new String[intargs.length + args.length];
			System.arraycopy(intargs, 0, params, 0, intargs.length);
			System.arraycopy(args, 0, params, intargs.length, args.length);
			try {
				return Int32.toInt32(new Process(p, intcmd, params).start().waitFor());
			} catch (Throwable t) {
				throw new AlchemyException(t);
			}
		}
	}
}

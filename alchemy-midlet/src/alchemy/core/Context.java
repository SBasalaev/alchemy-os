/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.core;

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.util.I18N;
import alchemy.util.IO;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Program execution context.
 * Context inherits program environment, input/output
 * streams and program thread.
 * <h3><a name="ContextStates">Context states</a></h3>
 * <ul>
 * <li>
 * <code>NEW</code><br/>
 * This is the state of newly created context. Context, created by
 * {@link #Context(Context) Context(parent)} constructor inherits
 * environment from its parent, though it can be modified.
 * <li>
 * <code>RUNNING</code><br/>
 * This is the state of context which executes program in separate
 * thread. Context becomes <code>RUNNING</code> after executing one
 * of methods {@link #start(String, String[]) start()}
 * or {@link #startAndWait(String, String[]) startAndWait()}.
 * These methods can be invoked only in <code>NEW</code> state.
 * If program fails to be loaded then exception is thrown
 * and context remains in <code>NEW</code> state.
 * <li>
 * <code>ENDED</code><br/>
 * The <code>RUNNING</code> context becomes <code>ENDED</code>
 * when program thread dies. In this state program exit code
 * can be examined with {@link #getExitCode()} method and
 * if program has ended by throwing an exception that exception
 * can be obtained with {@link #getError()} method.
 * </ul>
 * <h3><a name="LibraryLoading">Loading of programs and libraries</a></h3>
 * Methods that load libraries are <code>start</code>, <code>startAndWait</code>
 * and <code>loadLibrary</code>. The first two are used on <code>NEW</code>
 * context to start program while the latter can be called in any context state.
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
 *   is regarded as path relative to the {@link #getCurDir() current directory}.
 *   <li>
 *   If argument does not contain any slashes then search is performed in a
 *   set of paths defined in an environment variables. Methods <code>start</code>
 *   and <code>startAndWait</code> read paths from <code>PATH</code> variable
 *   and <code>readLibrary</code> method reads them from <code>LIBPATH</code>.
 *   These variables should contain colon-separated list of paths.
 *   </ul>
 * <li>
 * Library cache is checked whether it already has library loaded from
 * obtained file. If file contents has not been changed since that load
 * then cached library is returned.
 * <li>
 * If cache has no library or has older version of library then file is
 * used to construct new library instance. The first two bytes are read
 * to determine file type and call appropriate <code>LibBuilder</code>.
 * If there is no builder for the given file type then
 * <code>InstantiationException</code> is thrown.
 * </ol>
 *
 * @author Sergey Basalaev
 */
public class Context {

	/* CONTEXT STATES */

	/**
	 * State of the newly created context.
	 * @see <a href="#ContextStates">Context states</a>
	 */
	public static final int NEW = 0;
	/**
	 * State of the running context.
	 * @see <a href="#ContextStates">Context states</a>
	 */
	public static final int RUNNING = 1;
	/**
	 * State of the context that has ended its execution.
	 * @see <a href="#ContextStates">Context states</a>
	 */
	public static final int ENDED = 5;

	/* PUBLIC FIELDS */

	/**
	 * Standard input stream.
	 * By default stream of parent context is used.
	 */
	public InputStream stdin;
	/**
	 * Standard output stream.
	 * By default stream of parent context is used.
	 */
	public OutputStream stdout;
	/**
	 * Standard error stream.
	 * By default stream of parent context is used.
	 */
	public OutputStream stderr;

	/* PACKAGE PRIVATE FIELDS */

	/** Stack of function calls. */
	final Stack callStack = new Stack();

	/* PRIVATE FIELDS */

	/** Context runtime. */
	private Art art;
	/** Context parent, null for root context. */
	private Context parent;
	/** Context environment, initialized on demand. */
	private Hashtable env;
	/** Context storage, initialized on demand. */
	private Hashtable objects;
	/** Current directory. */
	private File curdir;
	/** The thread of this context. */
	private Thread thread;
	/** State of the context. */
	private int state = NEW;
//	/** Lock for methods that affect changing context's state. */
//	private final Object stateLock = new Object();
	/** Result returned by a program. */
	private int exitcode = -1;
	/** Error thrown by a program. */
	private Throwable error;
	/** Streams opened by process. */
	private Vector streams;
	/** Listeners of this context. */
	private Vector listeners;

	/* CONSTRUCTORS */

	/**
	 * Creates root context with given runtime.
	 */
	Context(Art runtime) {
		stdin = new NullInputStream();
		stderr = stdout = System.out;
		curdir = new File("");
		art = runtime;
	}

	/**
	 * Creates new context with given parent.
	 */
	public Context(Context parent) {
		if (parent == null) throw new IllegalArgumentException();
		this.parent = parent;
		art = parent.art;
		stdin = parent.stdin;
		stdout = parent.stdout;
		stderr = parent.stderr;
		curdir = parent.curdir;
	}

	/* CONTEXT METHODS */

	/**
	 * Returns value of environment variable.
	 * If variable is not defined then empty
	 * string is returned.
	 *
	 * @see #setEnv(String, String)
	 */
	public String getEnv(String key) {
		if (env != null) {
			Object val = env.get(key);
			if (val != null) return val.toString();
		}
		if (parent != null) {
			return parent.getEnv(key);
		}
		return "";
	}

	/**
	 * Sets value to the environment variable.
	 * @see #getEnv(String)
	 */
	public void setEnv(String key, String value) {
		if (env == null) env = new Hashtable();
		env.put(key, value);
	}

	/**
	 * Returns current working directory of context.
	 * @see #setCurDir(File)
	 */
	public File getCurDir() {
		return curdir;
	}

	/**
	 * Sets new current working directory to this context.
	 * @throws IOException
	 *   if given file does not represent existing
	 *   directory or if I/O error occurs
	 * @see #getCurDir() 
	 */
	public void setCurDir(File newdir) throws IOException {
		if (!art.fs.isDirectory(newdir)) throw new IOException(I18N._("Not a directory: {0}", newdir));
		curdir = newdir;
	}

	/**
	 * Returns state of this context.
	 */
	public int getState() {
		return state;
	}
	
	/**
	 * Changes state and notifies listeners.
	 */
	private void setState(int state) {
		this.state = state;
		if (state == ENDED && listeners != null) {
			for (Enumeration e = listeners.elements(); e.hasMoreElements(); ) {
				ContextListener l = (ContextListener)e.nextElement();
				l.contextEnded(this);
			}
		}
	}

	/**
	 * Returns program exit code.
	 */
	public int getExitCode() {
		return exitcode;
	}

	/**
	 * If state is <code>ENDED</code> and program execution
	 * resulted in exception this method returns it.
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Starts program in this context, blocks while
	 * it executes and returns program exit code.
	 * This method should be called when context is
	 * in <code>NEW</code> state.
	 *
	 * @param progname  program name
	 * @param cmdArgs   command line arguments
	 * @return program exit code
	 * @throws IOException
	 *    if an I/O error occurs while loading program
	 * @throws InstantiationException
	 *   if no program can be loaded from the file resolved from
	 *   given program name
	 * @throws IllegalStateException
	 *   if context is not in <code>NEW</code> state
	 *
	 * @see <a href="#LibraryLoading">How programs are loaded</a>
	 */
	public int startAndWait(String progname, String[] cmdArgs)
			throws IOException, InstantiationException, IllegalStateException {
		start(progname, cmdArgs);
		try {
			thread.join();
		} catch (InterruptedException e) {
			//return RESULT_INT
		}
		return exitcode;
	}

	/**
	 * Starts program in this context.
	 * This method should be invoked when the context
	 * is in <code>NEW</code> state. It resolves program
	 * file by name, loads it as library and executes its
	 * 'main' function.
	 *
	 * @param progname  program name or path
	 * @param cmdArgs   command-line arguments, may be null
	 *
	 * @throws IOException
	 *    if an I/O error occurs while loading program
	 * @throws InstantiationException
	 *   if program cannot be loaded from the file resolved from
	 *   given program name
	 * @throws IllegalStateException
	 *   if context is not in <code>NEW</code> state
	 *
	 * @see <a href="#LibraryLoading">How programs are loaded</a>
	 */
	public void start(String progname, String[] cmdArgs)
			throws IOException, InstantiationException, IllegalStateException {
		if (state != NEW) throw new IllegalStateException();
		if (cmdArgs == null) cmdArgs = new String[0];
		Library prog = loadLibForPath(progname, getEnv("PATH"));
		Function main = prog.getFunc("main");
		if (main == null) throw new InstantiationException(I18N._("No 'main' function"));
		thread = new ContextThread(progname, main, cmdArgs);
		setState(RUNNING);
		thread.start();
	}

	/**
	 * Loads library with given name.
	 *
	 * @param libname  library name or path
	 * @return <code>Library</code> instance
	 * @throws IOException
	 *   if an I/O error occurs while loading library
	 * @throws InstantiationException
	 *   if library cannot be loaded from the file resolved from
	 *   given library name
	 *
	 * @see <a href="#LibraryLoading">How libraries are loaded</a>
	 */
	public Library loadLibrary(String libname) throws IOException, InstantiationException {
		return loadLibForPath(libname, getEnv("LIBPATH"));
	}

	/**
	 * Searches library in given paths and loads it.
	 * @param libname   library name or path
	 * @param pathlist  colon separated list of paths
	 * @return  <code>Library</code> instance
	 * @throws IOException  if an I/O error occurs
	 * @throws InstantiationException  if library cannot
	 *   be instantiated from given file
	 */
	private Library loadLibForPath(String libname, String pathlist) throws IOException, InstantiationException {
		//resolving file
		File libfile = resolveFile(libname, pathlist);
		//checking permissions
		if (!fs().canExec(libfile))
			throw new InstantiationException(I18N._("Permission denied: {0}", libfile));
		//searching in cache
		long tstamp = fs().lastModified(libfile);
		Library lib = art.cache.getLibrary(libfile, tstamp);
		if (lib != null) return lib;
		//reading magic number and building
		InputStream in = fs().read(libfile);
		try {
			int magic = (in.read() << 8) | in.read();
			if (magic < 0)
				throw new InstantiationException(I18N._("Unknown library format"));
			//parsing link
			if (magic == (short)(('#'<<8)|'=')) {
				String fname = new UTFReader(in).readLine();
				if (fname.charAt(0) != '/') {
					fname = libfile.parent().path()+'/'+fname;
				}
				return loadLibForPath(fname, pathlist);
			} else if (magic == (short)(('#'<<8)|'!')) {
				String[] args = IO.split(new UTFReader(in).readLine(), ' ');
				String progname = args[0];
				System.arraycopy(args, 1, args, 0, args.length-1);
				args[args.length-1] = libfile.toString();
				HashLibrary hl = new HashLibrary();
				hl.putFunc(new ShebangFunction(progname, args));
				hl.lock();
				return hl;
			}
			LibBuilder builder = art.builders.get((short)magic);
			if (builder == null)
				throw new InstantiationException(I18N._("Unknown library format"));
			lib = builder.build(this, in);
			//caching
			art.cache.putLibrary(libfile, tstamp, lib);
		} finally {
			try { in.close(); } catch (IOException ioe) { }
		}
		return lib;
	}

	/**
	 * Resolves file by name and pathlist.
	 *
	 * @param name      file name
	 * @param pathlist  colon separated list of paths
	 * @return file that exists in a file system
	 * @throws IOException  if file is not found
	 */
	public File resolveFile(String name, String pathlist) throws IOException {
		File f;
		if (name.indexOf('/') >= 0) {
			f = toFile(name);
			if (fs().exists(f)) return f;
		} else {
			while (pathlist.length() > 0) {
				String path;
				int colon = pathlist.indexOf(':');
				if (colon >= 0) {
					path = name.substring(0, colon);
					pathlist = pathlist.substring(colon+1);
				} else {
					path = pathlist;
					pathlist = "";
				}
				if (path.length() == 0) continue;
				f = toFile(path+'/'+name);
				if (fs().exists(f)) return f;
			}
		}
		throw new IOException(I18N._("File not found: {0}", name));
	}

	/**
	 * Convenience method to access runtime filesystem.
	 */
	public Filesystem fs() {
		return art.fs;
	}

	/**
	 * Convenience method to convert path in a file.
	 * If path is absolute it is converted to file with
	 * that pathname, if path is relative the pathname
	 * is resolved with respect to the current directory.
	 * @param path path to the file
	 * @return file for given path
	 */
	public File toFile(String path) {
		int l = path.length();
		while (l > 0 && path.charAt(l-1) == '/') l--;
		path = path.substring(0,l);
		if (path.length() == 0 || path.charAt(0) == '/') return new File(path);
		else return new File(curdir, path);
	}

	/**
	 * Adds input or output stream to this context.
	 * All streams owned by context are automatically flushed
	 * and closed when context turns to <code>ENDED</code> state.
	 * @param stream  <code>InputStream</code> or <code>OutputStream</code>
	 */
	public void addStream(Object stream) {
		if (streams == null) streams = new Vector();
		if ((stream instanceof InputStream) || (stream instanceof OutputStream)) {
			streams.addElement(stream);
		}
	}

	/**
	 * Removes stream from owning by this context.
	 *
	 * @param stream  <code>InputStream</code> or <code>OutputStream</code>
	 * @see #addStream(Object)
	 */
	public void removeStream(Object stream) {
		if (streams != null) streams.removeElement(stream);
	}
	
	/**
	 * Returns object stored in context storage.
	 * If there is no object for given name, returns <code>null</code>.
	 */
	public Object get(Object key) {
		if (objects == null) return null;
		return objects.get(key);
	}
	
	/**
	 * Saves object in storage.
	 * If value is <code>null</code> removes key.
	 */
	public void set(Object key, Object value) {
		if (objects == null) {
			objects = new Hashtable();
		}
		if (value == null) {
			objects.remove(key);
		} else {
			objects.put(key, value);
		}
	}
	
	public void addContextListener(ContextListener l) {
		if (listeners == null) listeners = new Vector();
		if (!listeners.contains(l)) listeners.addElement(l);
	}
	
	public void removeContextListener(ContextListener l) {
		if (listeners != null) listeners.removeElement(l);
	}

	/**
	 * Dumps stack of function calls.
	 * @return newline separated list of function calls
	 */
	public String dumpCallStack() {
		StringBuffer sb = new StringBuffer();
		//synchronized (callStack) {
		for (int i=callStack.size()-1; i>=0; i--) {
			sb.append('@').append(callStack.elementAt(i)).append('\n');
		}
		//}
		return sb.toString();
	}

	private class ContextThread extends Thread {

		private final Function main;
		private final String[] cmdArgs;

		public ContextThread(String progname, Function main, String[] cmdArgs) {
			super(progname);
			this.main = main;
			this.cmdArgs = cmdArgs;
		}

		public void run() {
			try {
				Integer r = (Integer)main.call(Context.this, new Object[] {cmdArgs});
				exitcode = r == null ? 0 : r.intValue();
			} catch (Throwable t) {
				error = t;
				t.printStackTrace();
			}
			setState(ENDED);
			if (streams != null) {
				for (Enumeration e = streams.elements(); e.hasMoreElements(); ) {
					Object stream = e.nextElement();
					if (stream instanceof InputStream) {
						try {
							((InputStream)stream).close();
						} catch (IOException ioe) { }
					} else if (stream instanceof OutputStream) {
						try {
							((OutputStream)stream).flush();
						} catch (IOException ioe) { }
						try {
							((OutputStream)stream).close();
						} catch (IOException ioe) { }
					}
				}
			}
		}
	}
	
	private static class ShebangFunction extends Function {
	
		private final String   progname;
		private final String[] args;
	
		private ShebangFunction(String progname, String[] args) {
			super("main");
			this.progname = progname;
			this.args = args;
		}
		
		protected Object exec(Context c, Object[] params) throws Exception {
			String[] givenargs = (String[])(params[0]);
			String[] cmdargs = new String[args.length + givenargs.length];
			System.arraycopy(args, 0, cmdargs, 0, args.length);
			System.arraycopy(givenargs, 0, cmdargs, args.length, givenargs.length);
			return Ival(new Context(c).startAndWait(progname, cmdargs));
		}
	}
}

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
import alchemy.util.UTFReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Stack;

/**
 * Program execution context.
 *
 * @author Sergey Basalaev
 */
public class Context {

	/* CONTEXT STATES */

	/** State of the newly created context. */
	public static final int NEW = 0;
	/** State of the running context. */
	public static final int RUNNING = 1;
	/** State of the context that has ended its execution. */
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
	public PrintStream stdout;
	/**
	 * Standard error stream.
	 * By default stream of parent context is used.
	 */
	public PrintStream stderr;

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
	/** Current directory. */
	private File curdir;
	/** The thread of this context. */
	private Thread thread;
	/** State of the context. */
	private int state = NEW;
//	/** Lock for methods that affect changing context's state. */
//	private final Object stateLock = new Object();
	/** Result returned by a program. */
	private int result = -1;

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
	 */
	public void setEnv(String key, String value) {
		if (env == null) env = new Hashtable();
		env.put(key, value);
	}

	/**
	 * Returns current directory of context.
	 */
	public File getCurDir() {
		return curdir;
	}

	/**
	 * Sets new current directory to this context.
	 * @throws IOException
	 *   if given file does not represent existing
	 *   directory or if I/O error occurs
	 */
	public void setCurDir(File newdir) throws IOException {
		if (!art.fs.isDirectory(newdir)) throw new IOException("Not a directory: "+newdir);
		curdir = newdir;
	}

	/**
	 * Returns state of this context.
	 */
	public int getState() {
		return state;
	}

	public int startAndWait(String progname, String[] cmdArgs) throws IOException, InstantiationException {
		start(progname, cmdArgs);
		try {
			thread.join();
		} catch (InterruptedException e) {
			//return RESULT_INT
		}
		return result;
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
	 */
	public void start(String progname, String[] cmdArgs) throws IOException, InstantiationException {
		if (state != NEW) throw new IllegalStateException();
		if (cmdArgs == null) cmdArgs = new String[0];
		Library prog = loadLibForPath(progname, getEnv("PATH"));
		Function main = prog.getFunc("main");
		if (main == null) throw new InstantiationException("No 'main' function");
		thread = new ContextThread(progname, main, cmdArgs);
		state = RUNNING;
		thread.start();
	}

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
			throw new InstantiationException("Permission denied: "+libfile);
		//searching in cache
		long tstamp = fs().lastModified(libfile);
		Library lib = art.cache.getLibrary(libfile, tstamp);
		if (lib != null) return lib;
		//reading magic number and building
		InputStream in = fs().read(libfile);
		try {
			int magic = (in.read() << 8) | in.read();
			if (magic < 0)
				throw new InstantiationException("Unknown library format");
			//parsing link
			if (magic == (short)(('#'<<8)|'=')) {
				String fname = new UTFReader(in).readLine();
				if (fname.charAt(0) != '/') {
					fname = libfile.parent().path()+'/'+fname;
				}
				return loadLibForPath(fname, pathlist);
			}
			LibBuilder builder = art.builders.get((short)magic);
			if (builder == null)
				throw new InstantiationException("Unknown library format");
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
	private File resolveFile(String name, String pathlist) throws IOException {
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
		throw new IOException("File not found: "+name);
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
	 * Dumps stack of function calls.
	 * @return newline separated list of function calls
	 */
	public String dumpCallStack() {
		StringBuffer sb = new StringBuffer();
		synchronized (callStack) {
			for (int i=callStack.size()-1; i>=0; i--) {
				sb.append('@').append(callStack.elementAt(i)).append('\n');
			}
		}
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
				result = r.intValue();
			} catch (Throwable t) {
				t.printStackTrace();
				//TODO: exception handler
			}
			state = ENDED;
		}
	}
}

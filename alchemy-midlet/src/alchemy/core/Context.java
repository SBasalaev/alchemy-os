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

package alchemy.core;

import alchemy.fs.FSManager;
import alchemy.fs.Filesystem;
import alchemy.fs.devfs.NullInputStream;
import alchemy.util.IO;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connection;

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
	private String curdir;
	/** The thread of this context. */
	private Thread thread;
	/** State of the context. */
	private int state = NEW;
	/** Result returned by a program. */
	private int exitcode = AlchemyException.SUCCESS;
	/** Error thrown by a program. */
	private Throwable error;
	/** Streams opened by process. */
	private Vector streams;
	/** Listeners of this context. */
	private Vector listeners;
	/** Main function. */
	private Function main;
	/** Command-line arguments. */
	private String[] cmdArgs;
	/** Priority hint. */
	private int priority = Thread.NORM_PRIORITY;

	/* CONSTRUCTORS */

	/**
	 * Creates root context with given runtime.
	 */
	Context(Art runtime) {
		stdin = new NullInputStream();
		stderr = stdout = System.out;
		curdir = "";
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
	 * Returns name of the program this context executes.
	 * If context is in NEW state this method return <code>null</code>.
	 */
	public String getName() {
		return (thread == null) ? null : thread.getName();
	}
	
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
	public String getCurDir() {
		return curdir;
	}

	/**
	 * Sets new current working directory to this context.
	 * @throws IOException
	 *   if given file does not represent existing
	 *   directory or if I/O error occurs
	 * @see #getCurDir() 
	 */
	public void setCurDir(String newdir) throws IOException {
		if (!FSManager.fs().isDirectory(newdir)) throw new IOException("Not a directory: "+newdir);
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
	public void start(String progname, String[] args)
			throws IOException, InstantiationException, IllegalStateException {
		if (state != NEW) throw new IllegalStateException();
		Library prog = loadLibForPath(progname, getEnv("PATH"));
		this.main = prog.getFunction("main");
		this.cmdArgs =  (args != null) ? args : new String[0];
		if (main == null) throw new InstantiationException("No 'main' function");
		thread = new ContextThread(progname);
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
		String libfile = resolveFile(libname, pathlist);
		//checking permissions
		if (!FSManager.fs().canExec(libfile))
			throw new InstantiationException("Permission denied: "+libfile);
		//searching in cache
		long tstamp = FSManager.fs().lastModified(libfile);
		Library lib = art.cache.getLibrary(libfile, tstamp);
		if (lib != null) return lib;
		//reading magic number and building
		InputStream in = FSManager.fs().read(libfile);
		try {
			int magic = (in.read() << 8) | in.read();
			switch (magic) {
				case ('#' << 8) | '=': { // link to another library
					String fname = new UTFReader(in).readLine();
					in.close();
					if (fname.charAt(0) != '/') {
						fname = Filesystem.fparent(libfile)+'/'+fname;
					}
					lib = loadLibForPath(fname, pathlist);
					art.cache.putLibrary(libfile, tstamp, lib);
					return lib;
				}
				case ('#' << 8) | '!': { // shebang
					String[] args = IO.split(new UTFReader(in).readLine(), ' ');
					in.close();
					String progname = args[0];
					System.arraycopy(args, 1, args, 0, args.length-1);
					args[args.length-1] = libfile.toString();
					HashLibrary hl = new HashLibrary();
					hl.putFunc(new ShebangFunction(progname, args));
					hl.lock();
					art.cache.putLibrary(libfile, tstamp, hl);
					return hl;
				}
				case ('#' << 8) | '@': { // native library
					UTFReader r = new UTFReader(in);
					String classname = r.readLine();
					try {
						lib = (Library)Class.forName(classname).newInstance();
					} catch (ClassNotFoundException cnfe) {
						throw new InstantiationException("Class not found: "+classname);
					} catch (IllegalAccessException iae) {
						throw new InstantiationException("Class not accessible: "+classname);
					} catch (ClassCastException cce) {
						throw new InstantiationException("Not a library: "+classname);
					} catch (NoClassDefFoundError ncdfe) {
						throw new InstantiationException("Unsupported API: " + ncdfe.getMessage());
					}
					in.close();
					art.cache.putLibrary(libfile, tstamp, lib);
					return lib;
				}
				case 0xC0DE: { // Ether library
					lib = art.etherbuilder.build(this, in);
					in.close();
					art.cache.putLibrary(libfile, tstamp, lib);
					return lib;
				}
				default:
					throw new InstantiationException("Unknown library format");
			}
		} finally {
			try { in.close(); } catch (IOException ioe) { }
		}
	}

	/**
	 * Resolves file by name and pathlist.
	 *
	 * @param name      file name
	 * @param pathlist  colon separated list of paths
	 * @return file that exists in a file system
	 * @throws IOException  if file is not found
	 */
	public String resolveFile(String name, String pathlist) throws IOException {
		String f;
		if (name.indexOf('/') >= 0) {
			f = toFile(name);
			if (FSManager.fs().exists(f)) return f;
		} else {
			String[] paths = IO.split(pathlist, ':');
			for (int i=0; i<paths.length; i++) {
				String path =paths[i];
				if (path.length() == 0) continue;
				f = toFile(path+'/'+name);
				if (FSManager.fs().exists(f)) return f;				
			}
		}
		throw new IOException("File not found: "+name);
	}

	/**
	 * Convenience method to convert path in a file.
	 * If path is absolute it is converted to file with
	 * that pathname, if path is relative the pathname
	 * is resolved with respect to the current directory.
	 * @param path path to the file
	 * @return file for given path
	 */
	public String toFile(String path) {
		if (path.length() == 0 || path.charAt(0) == '/') return FSManager.normalize(path);
		else return FSManager.normalize(curdir + '/' + path);
	}

	/**
	 * Adds input or output stream to this context.
	 * All streams owned by context are automatically flushed
	 * and closed when context turns to <code>ENDED</code> state.
	 * @param stream  <code>InputStream</code> or <code>OutputStream</code>
	 */
	public void addStream(Object stream) {
		if (streams == null) streams = new Vector();
		streams.addElement(stream);
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
	
	public void interrupt() {
		if (thread == null) throw new IllegalStateException("Process is not running");
		thread.interrupt();
	}
	
	public void setPriority(int pr) {
		if (pr < Thread.MIN_PRIORITY || pr > Thread.MAX_PRIORITY)
			throw new IllegalArgumentException("Process priority is out of range: "+pr);
		if (thread == null) priority = pr;
		else thread.setPriority(pr);
	}
	
	public int getPriority() {
		if (thread == null) return priority;
		else return thread.getPriority();
	}

	/** A thread of running Alchemy program. */
	public class ContextThread extends Thread {

		public ContextThread(String progname) {
			super(progname);
		}
		
		public Context context() {
			return Context.this;
		}

		public void run() {
			setState(RUNNING);
			try {
				Object r = main.exec(Context.this, new Object[] {cmdArgs});
				if (r instanceof Int) {
					exitcode = ((Int)r).value;
				}
			} catch (Throwable t) {
				error = t;
				IO.println(stderr, t);
				if (t instanceof AlchemyException) {
					exitcode = ((AlchemyException)t).errcode;
				} else {
					exitcode = AlchemyException.FAIL;
				}
				//t.printStackTrace();
			}
			try { stdout.flush(); } catch (IOException e) { }
			try { stderr.flush(); } catch (IOException e) { }
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
					} else if (stream instanceof Connection) {
						try {
							((Connection)stream).close();
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
		
		public Object exec(Context c, Object[] params) throws AlchemyException {
			String[] givenargs = (String[])(params[0]);
			String[] cmdargs = new String[args.length + givenargs.length];
			System.arraycopy(args, 0, cmdargs, 0, args.length);
			System.arraycopy(givenargs, 0, cmdargs, args.length, givenargs.length);
			try {
				return Ival(new Context(c).startAndWait(progname, cmdargs));
			} catch (Exception e) {
				AlchemyException ae = new AlchemyException(e);
				ae.addTraceElement(this, progname+":1");
				throw ae;
			}
		}
	}
}

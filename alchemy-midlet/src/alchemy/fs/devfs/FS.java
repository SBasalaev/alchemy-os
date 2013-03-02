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

package alchemy.fs.devfs;

import alchemy.core.Context.ContextThread;
import alchemy.fs.Filesystem;
import alchemy.util.IO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;

/**
 * Device file system for Alchemy OS.
 * Typically mounted under {@code /dev}.
 * <p>
 * Implemented nodes:
 * <ul>
 * <li>{@code /null}</li>
 * <li>{@code /stdin}</li>
 * <li>{@code /stdout}</li>
 * <li>{@code /stderr}</li>
 * </ul>
 * </p>
 * 
 * @author Sergey Basalaev
 */
public final class FS extends Filesystem {
	
	private final Hashtable sharedinputs = new Hashtable();
	private final  Hashtable sharedoutputs = new Hashtable();
	private final String[] stddevs = {"stdin", "stdout", "stderr", "null", "zero", "random"};
	private final String[] commdevs;
	
	public FS() {
		SinkOutputStream sink = new SinkOutputStream();
		sharedoutputs.put("zero", sink);
		sharedoutputs.put("null", sink);

		sharedinputs.put("zero", new SinkInputStream(0));
		sharedinputs.put("null", new SinkInputStream(-1));
		sharedinputs.put("random", new RandomInputStream());

		String comm = System.getProperty("microedition.commports");
		commdevs = (comm == null) ? new String[0] : IO.split(comm, ',');
	}

	public InputStream read(String file) throws IOException {
		String name = fname(file);
		if ("stdin".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stdin;
			}
		}
		InputStream input = (InputStream) sharedinputs.get(name);
		if (input != null) {
			return input;
		}
		for (int i=0; i<commdevs.length; i++) {
			if (commdevs[i].equals(name)) {
				return Connector.openInputStream("comm:" + name);
			}
		}
		throw new IOException("No such device");
	}

	public OutputStream write(String file) throws IOException {
		String name = fname(file);
		if ("stdout".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stdout;
			}
		}
		if ("stderr".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stderr;
			}
		}
		OutputStream output = (OutputStream) sharedoutputs.get(name);
		if (output != null) {
			return output;
		}
		for (int i=0; i<commdevs.length; i++) {
			if (commdevs[i].equals(name)) {
				return Connector.openOutputStream("comm:" + name);
			}
		}
		throw new IOException("No such device");
	}

	public OutputStream append(String file) throws IOException {
		return write(file);
	}

	public String[] list(String file) throws IOException {
		String[] alldevs = new String[stddevs.length + commdevs.length];
		System.arraycopy(stddevs, 0, alldevs, 0, stddevs.length);
		System.arraycopy(commdevs, 0, alldevs, stddevs.length, commdevs.length);
		return alldevs;
	}

	public boolean exists(String file) {
		String name = fname(file);
		if (name.length() == 0) return true;
		for (int i=0; i<stddevs.length; i++) {
			if (name.equals(stddevs[i])) return true;
		}
		for (int i=0; i<commdevs.length; i++) {
			if (name.equals(commdevs[i])) return true;
		}
		return false;
	}

	public boolean isDirectory(String file) {
		return file.length() == 0;
	}

	public void create(String file) throws IOException {
		throw new IOException("Cannot create new device");
	}

	public void mkdir(String file) throws IOException {
		throw new IOException("Cannot create new device");
	}

	public void remove(String file) throws IOException {
		throw new IOException("Cannot remove device");
	}

	public long lastModified(String file) throws IOException {
		return System.currentTimeMillis();
	}

	public boolean canRead(String file) {
		String name = fname(file);
		if (name.length() == 0 || name.equals("stdin")) return true;
		if (sharedinputs.contains(name)) return true;
		for (int i=0; i<commdevs.length; i++) {
			if (name.equals(commdevs[i])) return true;
		}
		return false;
	}

	public boolean canWrite(String file) {
		String name = fname(file);
		if (name.equals("stdout") || name.equals("stderr")) return true;
		if (sharedinputs.contains(name)) return true;
		for (int i=0; i<commdevs.length; i++) {
			if (name.equals(commdevs[i])) return true;
		}
		return false;
	}

	public boolean canExec(String file) {
		return false;
	}

	public void setRead(String file, boolean on) throws IOException {
		throw new IOException("Cannot change device permissions");
	}
	
	public void setWrite(String file, boolean on) throws IOException {
		throw new IOException("Cannot change device permissions");
	}
	
	public void setExec(String file, boolean on) throws IOException {
		throw new IOException("Cannot change device permissions");
	}

	public long size(String file) {
		return 0l;
	}
	
	public long spaceTotal(String root) {
		return 0l;
	}
	
	public long spaceFree(String root) {
		return 0l;
	}
	
	public long spaceUsed(String root) {
		return 0l;
	}
}

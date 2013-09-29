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

import alchemy.fs.FSDriver;
import alchemy.fs.Filesystem;
import alchemy.io.NullInputStream;
import alchemy.io.NullOutputStream;
import alchemy.io.RandomInputStream;
import alchemy.system.ProcessThread;
import alchemy.util.HashMap;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;

/**
 * Device file system for Alchemy OS.
 * Typically mounted under {@code /dev}.
 * <p>
 * Implemented nodes:
 * <ul>
 * <li>{@code /null}</li>
 * <li>{@code /zero}</li>
 * <li>{@code /random}</li>
 * <li>{@code /stdin}</li>
 * <li>{@code /stdout}</li>
 * <li>{@code /stderr}</li>
 * <li>COMM ports on mobile platforms</li>
 * </ul>
 * </p>
 * 
 * @author Sergey Basalaev
 */
public final class Driver extends FSDriver {
	
	private final HashMap sharedinputs = new HashMap();
	private final HashMap sharedoutputs = new HashMap();
	private final String[] stddevs = {"stdin", "stdout", "stderr", "null", "zero", "random"};
	private final String[] commdevs;
	
	public Driver() {
		NullOutputStream sink = new NullOutputStream();
		sharedoutputs.set("zero", sink);
		sharedoutputs.set("null", sink);

		sharedinputs.set("zero", new NullInputStream(0));
		sharedinputs.set("null", new NullInputStream(-1));
		sharedinputs.set("random", new RandomInputStream());

		String comm = System.getProperty("microedition.commports");
		commdevs = (comm == null) ? new String[0] : Strings.split(comm, ',');
	}

	public InputStream read(String file) throws IOException {
		String name = Filesystem.fileName(file);
		if ("stdin".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ProcessThread) {
				return ((ProcessThread)th).getProcess().stdin;
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
		String name = Filesystem.fileName(file);
		if ("stdout".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ProcessThread) {
				return ((ProcessThread)th).getProcess().stdout;
			}
		}
		if ("stderr".equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ProcessThread) {
				return ((ProcessThread)th).getProcess().stderr;
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
		String name = Filesystem.fileName(file);
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
		String name = Filesystem.fileName(file);
		if (name.length() == 0 || name.equals("stdin")) return true;
		if (sharedinputs.get(name) != null) return true;
		for (int i=0; i<commdevs.length; i++) {
			if (name.equals(commdevs[i])) return true;
		}
		return false;
	}

	public boolean canWrite(String file) {
		String name = Filesystem.fileName(file);
		if (name.equals("stdout") || name.equals("stderr")) return true;
		if (sharedoutputs.get(name) != null) return true;
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
		return 0L;
	}
	
	public long spaceTotal() {
		return 0L;
	}
	
	public long spaceFree() {
		return 0L;
	}
	
	public long spaceUsed() {
		return 0L;
	}
}

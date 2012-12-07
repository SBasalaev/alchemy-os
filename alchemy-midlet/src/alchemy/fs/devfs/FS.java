/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

	static public final String DEV_NULL = "null";
	static public final String DEV_STDIN = "stdin";
	static public final String DEV_STDOUT = "stdout";
	static public final String DEV_STDERR = "stderr";
	
	public InputStream read(String file) throws IOException {
		String name = file.substring(1);
		if (DEV_NULL.equals(name)) {
			return new NullInputStream();
		} else if (DEV_STDIN.equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stdin;
			}
		}
		throw new IOException("No such device");
	}

	public OutputStream write(String file) throws IOException {
		String name = file.substring(1);
		if (DEV_NULL.equals(name)) {
			return new NullOutputStream();
		} else if (DEV_STDOUT.equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stdout;
			}
		} else if (DEV_STDERR.equals(name)) {
			Thread th = Thread.currentThread();
			if (th instanceof ContextThread) {
				return ((ContextThread)th).context().stderr;
			}
		}
		throw new IOException("No such device");
	}

	public OutputStream append(String file) throws IOException {
		return write(file);
	}

	public String[] list(String file) throws IOException {
		return new String[] { DEV_NULL, DEV_STDIN, DEV_STDOUT, DEV_STDERR };
	}

	public boolean exists(String file) {
		String name = file.substring(1);
		return DEV_NULL.equals(name) || DEV_STDIN.equals(name) || DEV_STDOUT.equals(name) || DEV_STDERR.equals(name);
	}

	public boolean isDirectory(String file) {
		return false;
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
		String name = file.substring(1);
		return DEV_NULL.equals(name) || DEV_STDIN.equals(name);
	}

	public boolean canWrite(String file) {
		String name = file.substring(1);
		return DEV_NULL.equals(name) || DEV_STDOUT.equals(name) || DEV_STDERR.equals(name);
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

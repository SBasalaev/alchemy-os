/*
 * Archiver for Alchemy.
 *  Copyright (C) 2011-2012  Sergey Basalaev <sbasalaev@gmail.com>
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

package org.alchemy.arh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Application main class.
 * @author Sergey Basalaev
 */
public class Main {

	private Main() { }

	static private final String VERSION = "arh 1.1";
	static private final String HELP =
			"Usage:\n" +
			"arh c archive files...    create archive\n" +
			"arh x archive             extract archive\n" +
			"arh t archive             list archive contents\n" +
			"arh h                     print help message and exit\n" +
			"arh v                     print program version and exit";

	/** Directory flag. */
	static private final int A_DIR = 16;
	/** Read flag. */
	static private final int A_READ = 4;
	/** Write flag. */
	static private final int A_WRITE = 2;
	/** Exec flag. */
	static private final int A_EXEC = 1;

	/* Due to file system differences, attribute flags are hardcoded now.
	 * DIRECTORY  drwx
	 * PROGRAM    -rwx
	 * FILE       -rw-
	 */
	
	static private File archiveFile;

	/**
	 * Main entry point.
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
			System.err.println("arh: no command");
			System.err.println(HELP);
			System.exit(1);
		} else if (args[0].startsWith("v") || args[0].startsWith("-v")) {
			System.out.println(VERSION);
		} else if (args[0].startsWith("h") || args[0].startsWith("-h")) {
			System.out.println(HELP);
		} else if (args[0].startsWith("c") || args[0].startsWith("-c")) {
			if (args.length < 3) {
				System.err.println("arh: insufficient arguments");
				System.err.println(HELP);
				System.exit(1);
			}
			archiveFile = new File(args[1]);
			DataOutputStream out = new DataOutputStream(new FileOutputStream(archiveFile));
			for (int i=2; i<args.length; i++) {
				arhFile(out, args[i]);
			}
			out.flush();
			out.close();
		} else if (args[0].startsWith("x") || args[0].startsWith("-x")) {
			if (args.length < 2) {
				System.err.println("arh: insufficient arguments");
				System.err.println(HELP);
				System.exit(1);
			}
			archiveFile = new File(args[1]);
			DataInputStream in = new DataInputStream(new FileInputStream(archiveFile));
			unarh(in);
			in.close();
		} else if (args[0].startsWith("t") || args[0].startsWith("-t")) {
			if (args.length < 2) {
				System.err.println("arh: insufficient arguments");
				System.err.println(HELP);
				System.exit(1);
			}
			archiveFile = new File(args[1]);
			DataInputStream in = new DataInputStream(new FileInputStream(archiveFile));
			arhlist(in);
			in.close();
		}
    }

	/**
	 * Recursively writes file to the archive.
	 */
	static private void arhFile(DataOutputStream out, String fname) throws IOException {
		File f = new File(fname);
		if (!f.exists()) throw new FileNotFoundException(fname);
		if (f.equals(archiveFile)) return;
		out.writeUTF(arhpath(fname));
		out.writeLong(f.lastModified());
		int attrs = A_READ | A_WRITE;
		if (f.isDirectory()) {
			out.writeByte(attrs | A_DIR | A_EXEC);
			String[] subs = f.list();
			for (String sub : subs) {
				arhFile(out, fname+'/'+sub);
			}
		} else {
			InputStream fstream = new FileInputStream(f);
			int magic = (fstream.read() << 8) | fstream.read();
			fstream.close();
			if ((magic == 0xC0DE) || (magic == (('#' << 8) | '!'))
			 || (magic == (('#' << 8) | '@')) || (magic == (('#' << 8) | '='))) {
				attrs |= A_EXEC;
			}
			out.writeByte(attrs);
			long len = f.length();
			if (len > Integer.MAX_VALUE) throw new IOException("File is too long: "+fname);
			out.writeInt((int)len);
			if (len > 0) {
				fstream = new FileInputStream(f);
				byte[] buf = new byte[4096];
				int l = fstream.read(buf);
				do {
					out.write(buf, 0, l);
					l = fstream.read(buf);
				} while (l > 0);
				fstream.close();
			}
		}
	}

	/**
	 * Calculates file path for the archive.
	 * Method changes all '/./' to '/', all
	 * '/name/../' to '/' and removes all leading
	 * '/' and '../'
	 */
	static private String arhpath(String path) {
		int index = path.indexOf('/');
		while (index == 0) {
			path = path.substring(1);
			index = path.indexOf('/');
		}
		if (index < 0) {
			if (path.isEmpty() || path.equals("..")) return ".";
			else return path;
		}
		StringBuilder sb = new StringBuilder();
		do {
			if (index == 0) {
				path = path.substring(1);
			} else {
				String pname = path.substring(0,index);
				if (pname.equals("..")) {
					sb.setLength(0);
				} else {
					if (!pname.equals(".")) {
						sb.append('/').append(pname);
					}
					path = path.substring(index+1);
				}
			}
			index = path.indexOf('/');
		} while (index >= 0);
		if (path.equals("..")) return ".";
		if (!path.equals(".")) sb.append('/').append(path);
		if (sb.length() == 0) return ".";
		return sb.substring(1);
	}

	/**
	 * Unpacks archive.
	 */
	static private void unarh(DataInputStream in) throws IOException {
		while (in.available() > 0) {
			File f = new File(arhpath(in.readUTF()));
			in.skipBytes(8);
			int attrs = in.readUnsignedByte();
			if ((attrs & A_DIR) != 0) {
				f.mkdir();
			} else {
				FileOutputStream out = new FileOutputStream(f);
				int len = in.readInt();
				if (len > 0) {
					byte[] buf = new byte[4096];
					while (len > 4096) {
						in.read(buf);
						out.write(buf);
						len -= 4096;
					}
					in.read(buf, 0, len);
					out.write(buf, 0, len);
				}
				out.flush();
				out.close();
			}
			f.setReadable((attrs & A_READ) != 0);
			f.setWritable((attrs & A_WRITE) != 0);
			f.setExecutable((attrs & A_EXEC) != 0);
		}
	}

	/**
	 * Lists contents of archive.
	 */
	static private void arhlist(DataInputStream in) throws IOException {
		while (in.available() > 0) {
			System.out.print(in.readUTF());
			in.skipBytes(8);
			int attrs = in.readUnsignedByte();
			if ((attrs & A_DIR) != 0) {
				System.out.println('/');
			} else {
				System.out.println();
				in.skipBytes(in.readInt());
			}
		}
	}
}

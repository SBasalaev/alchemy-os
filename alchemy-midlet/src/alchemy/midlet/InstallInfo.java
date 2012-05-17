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

package alchemy.midlet;

import alchemy.fs.Filesystem;
import alchemy.util.Initable;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * Information about Alchemy installation.
 * Information is stored as properties in the record store
 * named "installinfo". Stored properties are:
 * <dl>
 * <dt><code>fs.type</code></dt>
 * <dd>Type of filesystem to use</dd>
 * <dt><code>fs.init</code></dt>
 * <dd>Initialization string for the filesystem, for
 * RMS - name of the recordstore, for others - path to the root
 * directory</dd>
 * <dt><code>alchemy.initcmd</code></dt>
 * <dd>Command that is executed to run Alchemy</dd>
 * <dt><code>alchemy.version</code></dt>
 * <dd>Version of installation</dd>
 * </dl>
 * @author Sergey Basalaev
 */
class InstallInfo {

	private static final String INSTALLINFO = "installinfo";

	private static Properties props;

	private InstallInfo() { }

	/**
	 * Tests whether installation info exists within MIDlet.
	 */
	public static boolean exists() {
		try {
			RecordStore.openRecordStore(INSTALLINFO, false).closeRecordStore();
			return true;
		} catch (RecordStoreNotFoundException rsnfe) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Reads installation information.
	 * Returns empty properties if Alchemy is not installed.
	 * Returned object should then be filled with new information.
	 */
	public static Properties read() {
		if (props != null) return props;
		try {
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, false);
			try {
				byte[] b = rs.getRecord(1);
				UTFReader r = new UTFReader(new ByteArrayInputStream(b));
				return props = Properties.readFrom(r);
			} finally {
				rs.closeRecordStore();
			}
		} catch (RecordStoreNotFoundException rsnfe) {
			return props = new Properties();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Saves installation information that was changed
	 * after the previous call to <code>read()</code>.
	 */
	public static void save() {
		if (props == null) return;
		try {
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, true);
			if (rs.getNextRecordID() == 1) rs.addRecord(null, 0, 0);
			byte[] data = IO.utfEncode(props.toString());
			rs.setRecord(1, data, 0, data.length);
			rs.closeRecordStore();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Removes installation info and invalidates any produced properties.
	 */
	public static void remove() {
		try {
			props = null;
			RecordStore.deleteRecordStore(INSTALLINFO);
		} catch (RecordStoreNotFoundException rsnfe) {
			// already removed
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Returns filesystem set up by installation.
	 * Returns null if Alchemy is not installed
	 */
	public static Filesystem getFilesystem() throws IOException {
		if (props == null && !exists()) return null;
		if (props == null) read();
		String fstype = props.get("fs.type");
		String fsinit = props.get("fs.init");
		try {
			Class fsclass = Class.forName("alchemy.fs."+fstype+".FS");
			Filesystem fs = (Filesystem)fsclass.newInstance();
			if (fs instanceof Initable) {
				((Initable)fs).init(fsinit);
			}
			return fs;
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("FS module not found for fs.type="+fstype);
		} catch (Throwable t) {
			throw new RuntimeException("Error while creating FS: "+t);
		}
	}
}

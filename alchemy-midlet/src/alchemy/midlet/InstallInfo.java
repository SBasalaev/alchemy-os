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

package alchemy.midlet;

import alchemy.fs.Filesystem;
import alchemy.util.I18N;
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
 * @author Sergey Basalaev
 */
class InstallInfo {

	private static final String INSTALLINFO = "installinfo";

	/**
	 * Key for the property that identifies type of used filesystem.
	 */
	static final String FS_TYPE = "fs.type";
	/**
	 * Property that contains initialization string for filesystem.
	 * Value depends on the value for 'fs.type' property.
	 * <ul>
	 *   <li>For RMS it will be name of the used record store</li>
	 *   <li>For JSR75 and SIEMENS it will be path to the root directory</li>
	 * </ul>
	 */
	static final String FS_INIT = "fs.init";

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
		String fstype = props.get(FS_TYPE);
		String fsinit = props.get(FS_INIT);
		try {
			Class fsclass = Class.forName("alchemy.fs."+fstype+".FS");
			Filesystem fs = (Filesystem)fsclass.newInstance();
			if (fs instanceof Initable) {
				((Initable)fs).init(fsinit);
			}
			return fs;
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException(I18N._("FS module not found for ")+FS_TYPE+'='+fstype);
		} catch (Throwable t) {
			throw new RuntimeException(I18N._("Error while creating FS: ")+t);
		}
	}
}

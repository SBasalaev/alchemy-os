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
 *
 */

package alchemy.midlet;

import alchemy.fs.Filesystem;
import alchemy.fs.rms.RSFilesystem;
//#ifdef JSR75
import alchemy.fs.jsr75.JSR75Filesystem;
//#endif
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import alchemy.util.Util;
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
	 * Valid values:
	 *   RMS    for RSFilesystem
	 *   JSR75  for JSR75Filesystem
	 */
	static final String FS_TYPE = "fs.type";
	/**
	 * Property that contains initialization string for filesystem.
	 * Value depends on the value for 'fs.type' property.
	 *   For RMS it will be name of the used record store
	 *   For JSR75 it will be path to the root directory
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
			byte[] data = Util.utfEncode(props.toString());
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
		if (fstype.equals("rms")) return new RSFilesystem(fsinit);
		//#ifdef JSR75
		if (fstype.equals("jsr75")) return new JSR75Filesystem(fsinit);
		//#endif
		throw new RuntimeException("Unknown FS: "+FS_TYPE+'='+fstype);
	}
}

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

package alchemy.platform.j2me;

import alchemy.io.IO;
import alchemy.io.UTFReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
public final class InstallInfo implements alchemy.platform.InstallInfo {

	private static final String INSTALLINFO = "installinfo";
	/** Key for the installed version number. */
	private static final String INST_VERSION = "alchemy.version";
	/** Key for the string used to initialize root file system. */
	private static final String FS_DRIVER = "fs.type";
	/** Key for the type of the root file system. */
	private static final String FS_OPTIONS = "fs.init";
	/** Key for the current name of the record store used as emulated FS. */
	private static final String RMS_NAME = "rms.name";

	private String version;
	private String fsdriver;
	private String fsoptions;

	public InstallInfo() throws IOException {
		try {
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, false);
			try {
				byte[] b = rs.getRecord(1);
				UTFReader r = new UTFReader(new ByteArrayInputStream(b));
				String line;
				while ((line = r.readLine()) != null) {
					int eq = line.indexOf('=');
					if (eq < 0) continue;
					String key = line.substring(0, eq);
					String value = line.substring(eq+1);
					if (key.equals(INST_VERSION)) version = value;
					else if (key.equals(FS_DRIVER)) fsdriver = value;
					else if (key.equals(FS_OPTIONS)) fsoptions = value;
				}
				r.close();
			} finally {
				rs.closeRecordStore();
			}
		} catch (RecordStoreNotFoundException rsnfe) {
			// ok, not installed
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
	}

	public boolean exists() {
		try {
			RecordStore.openRecordStore(INSTALLINFO, false).closeRecordStore();
			return true;
		} catch (RecordStoreNotFoundException rsnfe) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	public String getFilesystemDriver() {
		return fsdriver;
	}

	public String getFilesystemOptions() {
		return fsoptions;
	}

	public String getInstalledVersion() {
		return version;
	}

	public void setFilesystem(String driver, String options) throws IOException {
		fsdriver = driver;
		fsoptions = options;
	}

	public void setInstalledVersion(String version) {
		this.version = version;
	}

	public void save() throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IO.println(out, INST_VERSION + '=' + version);
			IO.println(out, FS_DRIVER + '=' + fsdriver);
			IO.println(out, FS_OPTIONS + '=' + fsoptions);
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, true);
			if (rs.getNextRecordID() == 1) rs.addRecord(null, 0, 0);
			byte[] data = out.toByteArray();
			rs.setRecord(1, data, 0, data.length);
			rs.closeRecordStore();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	public void remove() {
		try {
			RecordStore.deleteRecordStore(INSTALLINFO);
		} catch (RecordStoreNotFoundException rsnfe) {
			// already removed
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}
}

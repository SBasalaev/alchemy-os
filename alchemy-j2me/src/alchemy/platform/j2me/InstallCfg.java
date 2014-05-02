/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.platform.Installer;
import alchemy.util.HashMap;
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
public final class InstallCfg implements alchemy.platform.InstallCfg {

	private static final String INSTALLINFO = "installinfo";

	private HashMap config;
	private boolean isInstalled;

	public InstallCfg() throws IOException {
		try {
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, false);
			try {
				config = Installer.parseConfig(new ByteArrayInputStream(rs.getRecord(1)));
				// for compatibility with Release 2.1
				if (config.get("alchemy.version") != null) {
					config.set(Installer.VERSION, config.get("alchemy.version"));
					config.remove("alchemy.version");
				}
			} finally {
				rs.closeRecordStore();
			}
			isInstalled = true;
		} catch (RecordStoreNotFoundException rsnfe) {
			// ok, not installed
			config = new HashMap();
			isInstalled = false;
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
	}

	public boolean exists() {
		return isInstalled;
	}

	public HashMap getConfig() {
		return config;
	}

	public void save() throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Object[] keys = config.keys();
			for (int i=0; i<keys.length; i++) {
				IO.println(out, "" + keys[i] + '=' + config.get(keys[i]));
			}
			RecordStore rs = RecordStore.openRecordStore(INSTALLINFO, true);
			if (rs.getNextRecordID() == 1) rs.addRecord(null, 0, 0);
			byte[] data = out.toByteArray();
			rs.setRecord(1, data, 0, data.length);
			rs.closeRecordStore();
			isInstalled = true;
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	public void remove() {
		try {
			config.clear();
			RecordStore.deleteRecordStore(INSTALLINFO);
			isInstalled = false;
		} catch (RecordStoreNotFoundException rsnfe) {
			// already removed
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}
}

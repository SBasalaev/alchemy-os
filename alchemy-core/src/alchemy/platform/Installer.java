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

package alchemy.platform;

import alchemy.fs.Filesystem;
import alchemy.io.UTFReader;
import alchemy.util.HashMap;
import alchemy.util.Strings;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common installation routines.
 *
 * @author Sergey Basalaev
 */
public final class Installer {

	/** Configuration of installer. */
	private final HashMap setupCfg;
	/** Platform dependent config methods. */
	private final InstallInfo info;

	public Installer() throws IOException {
		this.setupCfg = getSetupCfg();
		String platform = setupCfg.get("platform").toString();
		try {
			Class infoClz = Class.forName("alchemy.platform." + platform + ".InstallInfo");
			this.info = (InstallInfo) infoClz.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new IOException("No InstallInfo for platform " + platform);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/** Reads setup.cfg embedded in the jar. */
	static HashMap getSetupCfg() throws IOException {
		InputStream cfgin = Installer.class.getResourceAsStream("/setup.cfg");
		if (cfgin == null) throw new IOException("setup.cfg not found");
		HashMap map = readConfig(cfgin);
		cfgin.close();
		return map;
	}

	/** Parses configuration and returns it as key-value pairs. */
	private static HashMap readConfig(InputStream input) throws IOException {
		HashMap map = new HashMap();
		UTFReader r = new UTFReader(input);
		String line;
		while ((line = r.readLine()) != null) {
			int eq = line.indexOf('=');
			if (eq >= 0) {
				map.set(line.substring(0, eq).trim(), line.substring(eq+1).trim());
			}
		}
		return map;
	}

	/** Compares version strings. */
	private int compareVersions(String v1, String v2) {
		String[] v1parts = Strings.split(v1, '.', false);
		String[] v2parts = Strings.split(v2, '.', false);
		int index = 0;
		while (true) {
			if (index < v1parts.length) {
				int i1 = Integer.parseInt(v1parts[index]);
				int i2 = (index < v2parts.length) ? Integer.parseInt(v2parts[index]) : 0;
				if (i1 != i2) return i1-i2;
			} else if (index < v2parts.length) {
				int i2 = Integer.parseInt(v2parts[index]);
				if (i2 != 0) return -i2;
			} else {
				return 0; // two versions are equal
			}
			index++;
		}
	}

	/** Returns true if Alchemy OS is installed. */
	public boolean isInstalled() {
		return info.exists();
	}

	/**
	 * Returns true if Alchemy OS is installed and its
	 * version is older then the current.
	 */
	public boolean isUpdateNeeded() {
		return info.exists() && compareVersions(info.getInstalledVersion(), setupCfg.get("version").toString()) < 0;
	}

	/**
	 * Unpacks installer.
	 * This method mounts root file system, unpacks base files
	 * and then unmounts it.
	 */
	public void install(String fsdriver, String fsoptions) throws IOException {
		Filesystem.mount("", fsdriver, fsoptions);
		unpackBaseSystem();
		Filesystem.unmount("");
		info.setInstalledVersion(setupCfg.get("version").toString());
		info.setFilesystem(fsdriver, fsoptions);
		info.save();
	}

	/**
	 * Unpacks installer and updates configuration.
	 */
	public void update() throws IOException {
		Filesystem.mount("", info.getFilesystemDriver(), info.getFilesystemOptions());
		unpackBaseSystem();
		Filesystem.unmount("");
		info.setInstalledVersion(setupCfg.get("version").toString());
		info.save();
	}

	/**
	 * Installs base files in the file system.
	 * File system must be mounted before this method.
	 */
	private void unpackBaseSystem() throws IOException {
		String[] archives = Strings.split(getSetupCfg().get("install.archives").toString(), ' ', true);
		for (int i=0; i<archives.length; i++) {
			String arh = archives[i];
			DataInputStream datastream = new DataInputStream(getClass().getResourceAsStream("/"+arh));
			while (datastream.available() > 0) {
				String fname = datastream.readUTF();
				String f = '/'+fname;
				datastream.skip(8); //timestamp
				int attrs = datastream.readUnsignedByte();
				if ((attrs & 16) != 0) { //directory
					if (!Filesystem.exists(f)) Filesystem.mkdir(f);
				} else {
					if (!Filesystem.exists(f)) Filesystem.create(f);
					byte[] data = new byte[datastream.readInt()];
					datastream.readFully(data);
					OutputStream out = Filesystem.write(f);
					out.write(data);
					out.flush();
					out.close();
				}
				Filesystem.setExec(f, (attrs & 1) != 0);
			}
			datastream.close();
		}
		if (Filesystem.exists("/PACKAGE")) Filesystem.remove("/PACKAGE");
	}
}

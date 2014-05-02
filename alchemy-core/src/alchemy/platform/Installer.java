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
 * This class is used to acquire configuration
 * and make necessary preparations for boot,
 * install and update.
 *
 * <h4>Setup configuration</h4>
 *
 * Setup configuration is embedded in the program bundle
 * in file /setup.cfg and stores information about platform.
 * The following keys are defined in it:
 * <table border="1">
 * <tr>
 * <th>Key</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td><code>version</code></td>
 * <td>Version of Alchemy OS bundle.</td>
 * </tr>
 * <tr>
 * <td><code>platform</code></td>
 * <td>Target platform for this bundle. Possible values
 * are <code>pc</code>, <code>j2me</code>, <code>android</code>.</td>
 * </tr>
 * <tr>
 * <td><code>install.archives</code></td>
 * <td>List of bundled archives to unpack on install/update.</td>
 * </tr>
 * <tr>
 * <td><code>install.fs.*.name</code></td>
 * <td>Name of the file system driver for the installer.</td>
 * </tr>
 * <tr>
 * <td><code>install.fs.*.init</code></td>
 * <td>Initial options for the file system driver.</td>
 * </tr>
 * <tr>
 * <td>install.fs.*.test</td>
 * <td>Name of the required class for the driver.</td>
 * </tr>
 * <tr>
 * <td>install.fs.*.nav</td>
 * <td>If set to true, the file system is navigable.</td>
 * </tr>
 * </table>
 *
 * <h4>Installation configuration</h4>
 *
 * Installation configuration is written when Alchemy OS
 * is being installed/updated. It contains the following
 * keys:
 *
 * <table border="1">
 * <tr>
 * <th>Key</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td><code>version</code></td>
 * <td>Installed version of Alchemy OS.</td>
 * </tr>
 * <tr>
 * <td><code>fs.driver</code></td>
 * <td>The name of the driver used to mount root file system.</td>
 * </tr>
 * <tr>
 * <td><code>fs.options</code></td>
 * <td>The options to mount root file system.</td>
 * </tr>
 * </table>
 *
 * @author Sergey Basalaev
 */
public final class Installer {

	/** Configuration field that contains current version. */
	public static final String VERSION = "version";
	/** Configuration field that contains target platform. */
	public static final String PLATFORM = "platform";
	/** Configuration field that contains list of archives to unpack. */
	public static final String ARCHIVES = "install.archives";
	/** Configuration field that contains driver name for the root file system. */
	public static final String FS_DRIVER = "fs.driver";
	/** Configuration field that contains options for the root file system. */
	public static final String FS_OPTIONS = "fs.options";

	/** Configuration of installer. */
	private final HashMap setupCfg;
	/** Platform dependent config methods. */
	private final InstallCfg instCfg;

	public Installer() throws IOException {
		setupCfg = getSetupCfg();
		instCfg = Platform.getPlatform().installCfg();
	}

	/** Returns configuration embedded in the current bundle. */
	public HashMap getSetupConfig() {
		return setupCfg;
	}

	/** Returns configuration stored on platform. */
	public HashMap getInstalledConfig() {
		return instCfg.getConfig();
	}

	/** Writes configuration modifications. */
	public void saveInstalledConfig() throws IOException {
		instCfg.save();
	}

	/** Purges configuration turning Alchemy OS in uninstalled state. */
	public void removeInstalledConfig() throws IOException {
		instCfg.remove();
	}

	/** Reads setup.cfg embedded in the jar. */
	static HashMap getSetupCfg() throws IOException {
		InputStream cfgin = Installer.class.getResourceAsStream("/setup.cfg");
		if (cfgin == null) throw new IOException("setup.cfg not found");
		HashMap map = parseConfig(cfgin);
		cfgin.close();
		return map;
	}

	/** Parses configuration and returns it as key-value pairs. */
	public static HashMap parseConfig(InputStream input) throws IOException {
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
	public static int compareVersions(String v1, String v2) {
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
		return instCfg.exists();
	}

	/**
	 * Returns true if Alchemy OS is installed and its
	 * version is older then the current.
	 */
	public boolean isUpdateNeeded() {
		return instCfg.exists() &&
				compareVersions(instCfg.getConfig().get(VERSION).toString(), setupCfg.get(VERSION).toString()) < 0;
	}

	/**
	 * Unpacks installer.
	 * This method mounts root file system, unpacks base
	 * files, unmounts it and writes install configuration.
	 */
	public void install(String fsdriver, String fsoptions) throws IOException {
		Filesystem.mount("", fsdriver, fsoptions);
		unpackBaseSystem();
		Filesystem.unmount("");
		HashMap cfg = instCfg.getConfig();
		cfg.set(VERSION, setupCfg.get(VERSION));
		cfg.set(FS_DRIVER, fsdriver);
		cfg.set(FS_OPTIONS, fsoptions);
		instCfg.save();
	}

	/**
	 * Unpacks installer and updates configuration.
	 */
	public void update() throws IOException {
		HashMap cfg = instCfg.getConfig();
		Filesystem.mount("", cfg.get(FS_DRIVER).toString(), cfg.get(FS_OPTIONS).toString());
		unpackBaseSystem();
		Filesystem.unmount("");
		cfg.set(VERSION, setupCfg.get(VERSION));
		instCfg.save();
	}

	/**
	 * Installs base files in the file system.
	 * File system must be mounted before this method.
	 */
	private void unpackBaseSystem() throws IOException {
		String[] archives = Strings.split(getSetupCfg().get(ARCHIVES).toString(), ' ', true);
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

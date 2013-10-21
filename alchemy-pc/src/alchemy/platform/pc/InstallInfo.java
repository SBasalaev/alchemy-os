/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.platform.pc;

import alchemy.io.UTFReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Sergey Basalaev
 */
public final class InstallInfo implements alchemy.platform.InstallInfo {

	private File configFile;
	private String version;
	private String fsdriver;
	private String fsoptions;

	public InstallInfo() throws IOException {
		configFile = new File("install.cfg");
		if (configFile.exists()) {
			UTFReader r = new UTFReader(new FileInputStream(configFile));
			String line;
			while ((line = r.readLine()) != null) {
				int eq = line.indexOf('=');
				if (eq < 0) continue;
				String key = line.substring(0, eq).trim();
				String value = line.substring(eq+1).trim();
				if (key.equals("fs.driver")) {
					fsdriver = value;
				} else if (key.equals("fs.options")) {
					fsoptions = value;
				} else if (key.equals("version")) {
					version = value;
				}
			}
			r.close();
		}
	}

	@Override
	public boolean exists() {
		return configFile.exists();
	}

	@Override
	public String getFilesystemDriver() {
		return fsdriver;
	}

	@Override
	public String getFilesystemOptions() {
		return fsoptions;
	}

	@Override
	public void setFilesystem(String driver, String options) throws IOException {
		fsdriver = driver;
		fsoptions = options;
	}

	@Override
	public String getInstalledVersion() {
		return version;
	}

	@Override
	public void setInstalledVersion(String version) {
		this.version = version;
	}

	@Override
	public void remove() {
		configFile.delete();
	}

	@Override
	public void save() throws IOException {
		PrintStream out = new PrintStream(configFile, "UTF-8");
		out.println("version=" + version);
		out.println("platform=pc");
		out.println("fs.driver=" + fsdriver);
		out.println("fs.options=" + fsoptions);
		out.close();
	}
}

/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.platform.Installer;
import alchemy.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Sergey Basalaev
 */
public final class InstallCfg implements alchemy.platform.InstallCfg {

	private File configFile;
	private HashMap config;

	public InstallCfg() throws IOException {
		configFile = new File("install.cfg");
		if (configFile.exists()) {
			config = Installer.parseConfig(new FileInputStream(configFile));
		} else {
			config = new HashMap();
		}
	}

	@Override public boolean exists() {
		return configFile.exists();
	}

	@Override public void remove() {
		configFile.delete();
	}

	@Override public void save() throws IOException {
		PrintStream out = new PrintStream(configFile, "UTF-8");
		for (Object key : config.keys()) {
			out.print(key);
			out.print('=');
			out.println(config.get(key));
		}
		out.close();
	}

	@Override public HashMap getConfig() {
		return config;
	}
}

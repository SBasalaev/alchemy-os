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

import java.io.IOException;

/**
 * Platform dependent interfaces.
 *
 * @author Sergey Basalaev
 */
public final class Platform {

	private final Core core;
	private final UI ui;
	private final InstallCfg cfg;

	private Platform() {
		try {
			String name = (String) Installer.getSetupCfg().get("platform");
			core = (Core)       load(name, "Core");
			ui   = (UI)         load(name, "UI");
			cfg  = (InstallCfg) load(name, "InstallCfg");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ioe.toString());
		}
	}

	private static Platform instance;

	public static Platform getPlatform() {
		if (instance == null) instance = new Platform();
		return instance;
	}

	private static Object load(String platform, String name) throws IOException {
		try {
			Class clz = Class.forName("alchemy.platform." + platform + '.' + name);
			return clz.newInstance();
		} catch (Exception e) {
			throw new IOException("Error initializing platform interface " + name);
		}
	}

	public Core getCore() {
		return core;
	}

	public UI getUI() {
		return ui;
	}

	public InstallCfg installCfg() {
		return cfg;
	}
}

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

import java.io.IOException;

/**
 * Platform dependent interfaces.
 *
 * @author Sergey Basalaev
 */
public final class Platform {

	private final UI ui;

	private Platform() throws IOException {
		String name = (String) Installer.getSetupCfg().get("platform");
		ui = (UI) load(name, "UI");
	}

	private static Platform instance;

	public static Platform getPlatform() throws IOException {
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

	public UI getUI() {
		return ui;
	}
}

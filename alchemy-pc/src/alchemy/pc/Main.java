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

package alchemy.pc;

import alchemy.fs.Filesystem;
import alchemy.platform.Installer;
import java.io.File;
import java.io.IOException;

/**
 * Main entry point for PC.
 * @author Sergey Basalaev
 */
public class Main {

	private Main() { }

	public static void main(String[] args) {
		try {
			File root = new File("./root");
			if (!root.exists()) root.mkdirs();
			checkInstall();
			run();
		} catch (Exception e) {
			System.err.println("!!! Alchemy OS crashed !!!");
			e.printStackTrace();
		}
	}

	public static void checkInstall() throws IOException {
		Installer installer = new Installer();
		if (!installer.isInstalled()) {
			System.out.println("Installing Alchemy OS...");
			installer.install("pc", "./root");
		} else if (installer.isUpdateNeeded()) {
			System.out.println("Updating Alchemy OS...");
			installer.update();
		}
	}

	public static void run() throws IOException, InstantiationException, InterruptedException {
		Filesystem.mount("", "pc", "./root");
		Filesystem.mount("/dev", "devfs", "");
		System.out.println("Welcome!");
		alchemy.system.Process ps = new alchemy.system.Process("terminal", new String[0]);
		ps.setEnv("PATH", "/bin");
		ps.setEnv("LIBPATH", "/lib");
		ps.setEnv("INCPATH", "/inc");
		ps.setCurrentDirectory("/home");
		ps.start().waitFor();
		if (ps.getError() != null) {
			System.err.println(ps.getError());
		}
		System.exit(ps.getExitCode());
	}
}

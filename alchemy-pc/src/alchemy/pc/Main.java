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

package alchemy.pc;

import alchemy.fs.Filesystem;
import alchemy.platform.Installer;
import alchemy.system.Process;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

/**
 * Main entry point for PC.
 * @author Sergey Basalaev
 */
public class Main {

	private static final String ROOT_DIR = "root";

	private Main() { }

	public static void main(String[] args) {
		try {
			File root = new File(ROOT_DIR);
			if (!root.exists()) root.mkdirs();
			checkInstall();
			run();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			PrintWriter message = new PrintWriter(str);
			message.print("Alchemy OS crashed. Please report this to developers.\n\n");
			e.printStackTrace(message);
			JOptionPane.showMessageDialog(null, str.toString(), "Alchemy OS", ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static void checkInstall() throws IOException {
		Installer installer = new Installer();
		if (!installer.isInstalled()) {
			WaitFrame w = new WaitFrame("Installing Alchemy OS...");
			w.setVisible(true);
			installer.install("pc", ROOT_DIR);
			w.dispose();
		} else if (installer.isUpdateNeeded()) {
			WaitFrame w = new WaitFrame("Installing Alchemy OS...");
			w.setVisible(true);
			installer.update();
			w.dispose();
		}
	}

	public static void run() throws IOException, InstantiationException, InterruptedException {
		Filesystem.mount("", "pc", ROOT_DIR);
		Filesystem.mount("/dev", "devfs", "");
		Process ps = new Process("terminal", new String[0]);
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

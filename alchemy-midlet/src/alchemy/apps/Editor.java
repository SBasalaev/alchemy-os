/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
 *
 */

package alchemy.apps;

import alchemy.core.Context;
import alchemy.fs.File;
import alchemy.midlet.AlchemyMIDlet;
import alchemy.nlib.NativeApp;
import alchemy.util.Util;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * Simple text editor.
 * @author Sergey Basalaev
 */
public class Editor extends NativeApp {

	private static final String HELP = "Usage: ed <filename>\n";
	private static final String VERSION = "native editor v1.0\n";
	
	private static final Command cmdSave = new Command("Save", Command.OK, 1);
	private static final Command cmdQuit = new Command("Quit", Command.EXIT, 5);

	public int main(Context c, String[] args) {
		if (args.length == 0) {
			c.stderr.print("ed: no file specified\n");
			c.stderr.print(HELP);
			return 1;
		}
		if (args[0].equals("-h")) {
			c.stdout.print(HELP);
			return 0;
		}
		if (args[0].equals("-v")) {
			c.stdout.print(VERSION);
			return 0;
		}
		File docfile = c.toFile(args[0]);
		TextBox box = new TextBox("Editor", null, 65536, TextField.ANY);
		box.addCommand(cmdSave);
		box.addCommand(cmdQuit);
		final NotifyListener l = new NotifyListener();
		box.setCommandListener(l);
		//reading file
		try {
			if (c.fs().exists(docfile)) {
				InputStream in = c.fs().read(docfile);
				byte[] data = Util.readFully(in);
				in.close();
				if (data.length > box.getMaxSize()) {
					c.stderr.println("ned: File is too long");
					return 1;
				}
				box.setString(Util.utfDecode(data));
			}
		} catch (Exception e) {
			c.stderr.println(e);
			return 1;
		}
		AlchemyMIDlet.getInstance().pushScreen(box);
		try {
			while (true) {
				synchronized (l) { l.wait(); }
				if (l.result == cmdQuit) return 0;
				else if (l.result == cmdSave) {
					OutputStream out = c.fs().write(docfile);
					byte[] data = Util.utfEncode(box.getString());
					out.write(data);
					out.close();
					AlchemyMIDlet.getInstance().alert("Editor", "File saved", AlertType.INFO);
				}
			}
		} catch (Exception e) {
			c.stderr.println(e);
			return 1;
		} finally {
			AlchemyMIDlet.getInstance().popScreen();
		}
	}
}

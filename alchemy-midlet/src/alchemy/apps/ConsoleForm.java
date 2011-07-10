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
 */

package alchemy.apps;

import alchemy.util.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * A form for the native console.
 * @author Sergey Basalaev
 */
class ConsoleForm extends Form implements ItemCommandListener {

	final InputStream in = new ConsoleInputStream();
	final PrintStream out = new PrintStream(new ConsoleOutputStream());

	private final TextField input = new TextField(">", null, 1024, TextField.ANY);
	private final Command cmdInput = new Command("Input", Command.OK, 1);
	private boolean inputvisible = false;

	public ConsoleForm() {
		super("Console");
		input.addCommand(cmdInput);
		input.setItemCommandListener(this);
	}

	public void print(String msg) {
		StringItem message = new StringItem(null, msg);
		int pos = size() - (inputvisible? 1:0);
		insert(pos, message);
	}

	/**
	 * Blocks current thread until some text is put
	 * into text field and "input" button is pressed.
	 */
	private String waitForInput() {
		synchronized (input) {
			input.setString("");
			append(input);
			inputvisible = true;
			try {
				input.wait();
			} catch (InterruptedException ie) {
				print("interrupt");
			}
			delete(size()-1);
			append(new StringItem(input.getLabel(), input.getString()+'\n'));
			inputvisible = false;
			return input.getString();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == cmdInput) {
			//means we're finished reading
			//notify thread that awaits for input
			synchronized (input) {
				input.notify();
			}
		}
	}

	private class ConsoleOutputStream extends OutputStream {

		ByteArrayOutputStream buf;

		public ConsoleOutputStream() {
			buf = new ByteArrayOutputStream();
		}

		public synchronized void write(int b) throws IOException {
			buf.write(b);
			if (b == '\n') flush();
		}

		public synchronized void write(byte[] b, int off, int len) throws IOException {
			int flushmark = off+len-1;
			while (flushmark >= off) {
				if (b[flushmark] == '\n') break;
				flushmark--;
			}
			if (flushmark >= off) {
				buf.write(b, off, off+flushmark+1);
				flush();
				len -= flushmark-off+1;
				off = flushmark+1;
			}
			buf.write(b, off, len);
		}

		public synchronized void flush() throws IOException {
			byte[] data = buf.toByteArray();
			buf.reset();
			print(Util.utfDecode(data));
		}
	}

	public class ConsoleInputStream extends InputStream {

		private ByteArrayInputStream buf = null;

		public ConsoleInputStream() { }

		public void setPrompt(String prompt) {
			input.setLabel(prompt);
		}

		public synchronized void clearScreen() {
			deleteAll();
			if (inputvisible) append(input);
		}

		public int read() throws IOException {
			if (buf == null) {
				String data = waitForInput();
				buf = new ByteArrayInputStream(Util.utfEncode(data));
			}
			int b = buf.read();
			if (b == -1) buf = null;
			return b;
		}
	}
}

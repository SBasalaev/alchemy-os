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

package alchemy.apps;

import alchemy.io.TerminalInput;
import alchemy.util.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * A form for the native terminal.
 * @author Sergey Basalaev
 */
class TerminalForm extends Form {

	final InputStream in = new TerminalInputStream();
	final PrintStream out = new PrintStream(new TerminalOutputStream());

	private final TextField input = new TextField(">", null, 1024, TextField.ANY);
	private final Command cmdInput;
	private final Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private boolean inputvisible = false;

	public TerminalForm(Command input) {
		super("Terminal");
		this.input.setInitialInputMode("MIDP_LOWERCASE_LATIN");
		cmdInput = input;
	}

	public void print(String msg) {
		StringItem message = new StringItem(null, msg);
		message.setFont(smallfont);
		int pos = size() - (inputvisible? 1:0);
		insert(pos, message);
	}

	/**
	 * Blocks current thread until some text is put
	 * into text field and "input" button is pressed.
	 */
	private synchronized String waitForInput() {
			input.setString("");
			append(input);
			inputvisible = true;
			addCommand(cmdInput);
			try {
				wait();
			} catch (InterruptedException ie) {
				print("interrupted");
			}
			delete(size()-1);
			append(new StringItem(input.getLabel(), input.getString()+'\n'));
			inputvisible = false;
			removeCommand(cmdInput);
			return input.getString()+'\n';
	}

	private class TerminalOutputStream extends OutputStream {

		ByteArrayOutputStream buf;

		public TerminalOutputStream() {
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
			print(Strings.utfDecode(data));
		}
	}

	private class TerminalInputStream extends InputStream implements TerminalInput {

		private ByteArrayInputStream buf = new ByteArrayInputStream(new byte[0]);

		public TerminalInputStream() { }

		public String getPrompt() {
			return input.getLabel();
		}

		public void setPrompt(String prompt) {
			input.setLabel(prompt);
		}

		public synchronized void clear() {
			deleteAll();
			if (inputvisible) append(input);
		}

		public int available() throws IOException {
			return buf.available();
		}

		public int read() throws IOException {
			int b = buf.read();
			if (b == -1) {
				String data = waitForInput();
				buf = new ByteArrayInputStream(Strings.utfEncode(data));
				b = buf.read();
			}
			return b;
		}
	}
}

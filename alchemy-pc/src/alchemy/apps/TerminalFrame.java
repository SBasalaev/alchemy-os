/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Terminal window.
 * @author Sergey Basalaev
 */
public final class TerminalFrame extends JFrame {

	private final JEditorPane output;
	private final JLabel prompt;
	private final JTextField input;
	private final Box inputBox;
	private final Object sync = new Object();

	private StringBuilder outputContents = new StringBuilder();

	final TerminalInputStream in = new TerminalInputStream();
	final TerminalOutputStream out = new TerminalOutputStream(false);
	final TerminalOutputStream err = new TerminalOutputStream(true);

	public TerminalFrame(String title) {
		super(title);

		output = new JEditorPane();
		output.setEditable(false);
		output.setContentType("text/html");
		JScrollPane outputPane = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outputPane.setPreferredSize(new Dimension(600, 480));

		prompt = new JLabel();

		input = new JTextField();
		input.setEditable(false);
		input.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					synchronized (sync) { sync.notify(); }
				}
			}
		});

		JButton enter = new JButton("Enter");
		enter.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				synchronized (sync) {  sync.notify(); }
			}
		});

		inputBox = Box.createHorizontalBox();
		inputBox.add(prompt);
		inputBox.add(input);
		inputBox.add(enter);

		add(outputPane, BorderLayout.CENTER);
		add(inputBox, BorderLayout.SOUTH);
		pack();
	}

	private String waitForInput() {
		input.setEditable(true);
		input.requestFocus();
		synchronized (sync) {
			try {
				sync.wait();
			} catch (InterruptedException ie) {
				append("interrupted");
				flushText();
			}
		}
		input.setEditable(false);
		String newInput = input.getText() + '\n';
		append("<b>");
		appendEscaped(prompt.getText());
		append("</b> ");
		appendEscaped(newInput);
		flushText();
		input.setText(null);
		return newInput;
	}

	private synchronized void append(String str) {
		outputContents.append(str);
	}

	private synchronized void appendEscaped(String str) {
		StringBuilder sb = outputContents;
		for (int i=0; i<str.length(); i++) {
			char ch = str.charAt(i);
			switch (ch) {
				case '<':  sb.append("&lt;"); break;
				case '>':  sb.append("&gt;"); break;
				case '\n': sb.append("<br>"); break;
				default:   sb.append(ch);     break;
			}
		}
	}

	private void flushText() {
		output.setText(outputContents.toString());
	}

	private class TerminalInputStream extends InputStream implements TerminalInput {

		private ByteArrayInputStream buf = new ByteArrayInputStream(new byte[0]);

		public TerminalInputStream() { }

		@Override
		public int available() throws IOException {
			return buf.available();
		}

		@Override
		public int read() throws IOException {
			int b = buf.read();
			if (b == -1) {
				String data = waitForInput();
				buf = new ByteArrayInputStream(Strings.utfEncode(data));
				b = buf.read();
			}
			return b;
		}

		@Override
		public void clear() {
			synchronized (TerminalFrame.this) {
				outputContents = new StringBuilder();
			}
			flushText();
		}

		@Override
		public String getPrompt() {
			return prompt.getText();
		}

		@Override
		public void setPrompt(String text) {
			prompt.setText(text);
		}
	}

	private class TerminalOutputStream extends OutputStream {

		private ByteArrayOutputStream buf;
		private final boolean errColored;

		public TerminalOutputStream(boolean errColored) {
			buf = new ByteArrayOutputStream();
			this.errColored = errColored;
		}

		@Override
		public synchronized void write(int b) throws IOException {
			buf.write(b);
			if (b == '\n') flush();
		}

		@Override
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

		@Override
		public synchronized void flush() throws IOException {
			byte[] data = buf.toByteArray();
			buf.reset();
			if (errColored) append("<font color=\"red\">");
			appendEscaped(Strings.utfDecode(data));
			if (errColored) append("</font>");
			flushText();
		}
	}
}

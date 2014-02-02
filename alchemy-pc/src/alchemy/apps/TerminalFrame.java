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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;

/**
 * Terminal window.
 * @author Sergey Basalaev
 */
public final class TerminalFrame extends JFrame {

	private final JTextArea output;
	private final JLabel prompt;
	private final JTextField input;
	private final Box inputBox;
	private final Object sync = new Object();
	private final JButton enter;

	final TerminalInputStream in = new TerminalInputStream();
	final TerminalOutputStream out = new TerminalOutputStream();
	final TerminalOutputStream err = out;

	public TerminalFrame(String title) {
		super(title);

		output = new JTextArea();
		output.setEditable(false);
		output.setLineWrap(true);
		JScrollPane outputPane = new JScrollPane(output, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputPane.setPreferredSize(new Dimension(640, 480));

		prompt = new JLabel();

		input = new JTextField();
		input.setEditable(false);
		input.setText("Running...");
		input.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					synchronized (sync) { sync.notify(); }
				}
			}
		});

		enter = new JButton("Enter");
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
		input.setText(null);
		input.setEditable(true);
		enter.setVisible(true);
		input.requestFocus();
		synchronized (sync) {
			try {
				sync.wait();
			} catch (InterruptedException ie) {
				output.append("interrupted\n");
			}
		}
		input.setEditable(false);
		String newInput = input.getText() + '\n';
		input.setText("Running...");
		enter.setVisible(false);
		synchronized (output) {
			output.append(prompt.getText());
			output.append(" ");
			output.append(newInput);
		}
		return newInput;
	}

	void end(String name) {
		input.setEditable(false);
		input.setText("Process '" + name + "' ended.");
		enter.setText("Close");
		enter.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				TerminalFrame.this.setVisible(false);
			}
		});
		enter.setVisible(true);
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
			synchronized (output) {
				output.setText(null);
			}
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

		public TerminalOutputStream() {
			buf = new ByteArrayOutputStream();
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
			synchronized (output) {
				output.append(Strings.utfDecode(data));
			}
		}
	}
}

/*
 * Copyright (C) 2014 Sergey Basalaev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package alchemy.libs.ui;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * PC implementation of EditBox.
 * @author Sergey Basalaev
 */
public final class UiEditBox extends UiScreen {

	private final JTextArea textArea;
	private final JScrollPane scrollPane;

	private int maxsize = Short.MAX_VALUE;

	public UiEditBox(String title, String text) {
		super(title);
		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setFont(FontManager.getFont(FontManager.DEFAULT_FONT));

		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.add(textArea);

		setText(text);
	}

	@Override public JComponent getWidget() {
		return scrollPane;
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		if (text != null && text.length() > this.maxsize)
			throw new IllegalArgumentException("Text size exceeds maxSize");
		textArea.setText(text);
	}

	public int getMaxSize() {
		return maxsize;
	}

	public void setMaxSize(int size) {
		if (size <= 0)
			throw new IllegalArgumentException("maxSize must be positive");
		String text = getText();
		if (text.length() > size) {
			setText(text.substring(0, size));
		}
		maxsize = size;
	}

	public int getSize() {
		return textArea.getText().length();
	}

	public int getCaret() {
		return textArea.getCaretPosition();
	}
}

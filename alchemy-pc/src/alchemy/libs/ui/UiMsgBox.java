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

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author Sergey Basalaev
 */
public final class UiMsgBox extends UiScreen {

	private final Box widget;
	private final JLabel imageLabel;
	private final JLabel textLabel;

	private UiImage image;
	private int font;

	public UiMsgBox(String title, String text, UiImage image) {
		super(title);
		font = FontManager.DEFAULT_FONT;

		imageLabel = new JLabel();
		textLabel = new JLabel();
		textLabel.setFont(FontManager.getFont(font));

		widget = Box.createVerticalBox();
		widget.add(imageLabel);
		widget.add(textLabel);

		setText(text);
		setImage(image);
	}

	public String getText() {
		return textLabel.getText();
	}

	public void setText(String msg) {
		textLabel.setText(msg);
	}

	public UiImage getImage() {
		return image;
	}

	public void setImage(UiImage image) {
		this.image = image;
		this.imageLabel.setIcon(image == null ? null : new ImageIcon(image.image));
	}

	public int getFont() {
		return font;
	}

	public void setFont(int font) {
		this.font = font;
		this.textLabel.setFont(FontManager.getFont(font));
	}

	@Override public JComponent getWidget() {
		return widget;
	}
}

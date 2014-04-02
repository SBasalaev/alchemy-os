/*
 * Copyright (C) 2014 <copyright-holder>
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

import alchemy.libs.LibUI2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * Implementation of image abstraction.
 * @author Sergey Basalaev
 */
public final class UiImage {

	public final Image image;
	public final boolean isMutable;

	public UiImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image = img;
		Graphics2D graphics = img.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setFont(FontManager.getFont(FontManager.DEFAULT_FONT));
		graphics.setStroke(LibUI2.SOLID);
		isMutable = true;
	}

	public UiImage(Image img) {
		while (!Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, null)) {
			Thread.yield();
		}
		image = img;
		isMutable = false;
	}

	public Graphics2D getGraphics() {
		if (!isMutable) throw new IllegalStateException("Immutable image");
		return ((BufferedImage)image).createGraphics();
	}
}

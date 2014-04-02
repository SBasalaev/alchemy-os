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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Font manager for j2se UI library.
 * @author Sergey Basalaev
 */
public final class FontManager {
	public static final int FACE_SYSTEM = 0;
	public static final int FACE_MONOSPACE = 32;
	public static final int FACE_PROPORTIONAL = 64;

	public static final int SIZE_SMALL = 8;
	public static final int SIZE_MEDIUM = 0;
	public static final int SIZE_LARGE = 16;

	public static final int STYLE_PLAIN = 0;
	public static final int STYLE_BOLD = 1;
	public static final int STYLE_ITALIC = 2;
	public static final int STYLE_UNDERLINED = 4;

	public static final String IMPL_SYSTEM = Font.SANS_SERIF;
	public static final String IMPL_MONOSPACE = Font.MONOSPACED;
	public static final String IMPL_PROPORTIONAL = Font.SANS_SERIF;

	public static final int IMPL_SMALL = 9;
	public static final int IMPL_MEDIUM = 11;
	public static final int IMPL_LARGE = 13;

	public static final int DEFAULT_FONT = FACE_SYSTEM | SIZE_MEDIUM | STYLE_PLAIN;

	private FontManager() { }

	private static HashMap<Integer, FontMetrics> fonts = new HashMap<Integer, FontMetrics>();

	/** Graphics to acquire font metrics. */
	private final static Graphics graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();

	public static FontMetrics getFontMetrics(int mask) {
		if ((mask & (FACE_MONOSPACE | FACE_PROPORTIONAL)) == (FACE_MONOSPACE | FACE_PROPORTIONAL))
			throw new IllegalArgumentException("Bad font face specifier");
		if ((mask & (SIZE_SMALL | SIZE_LARGE)) == (SIZE_SMALL | SIZE_LARGE))
			throw new IllegalArgumentException("Bad font size specifier");

		FontMetrics fm = fonts.get(mask);
		if (fm == null) {
			String name = IMPL_SYSTEM;
			if ((mask & FACE_MONOSPACE) != 0) {
				name = IMPL_MONOSPACE;
			} else if ((mask & FACE_PROPORTIONAL) != 0) {
				name = IMPL_PROPORTIONAL;
			}

			int style = 0;
			if ((mask & STYLE_BOLD) != 0) {
				style |= Font.BOLD;
			}
			if ((mask & STYLE_ITALIC) != 0) {
				style |= Font.ITALIC;
			}

			int size = IMPL_MEDIUM;
			if ((mask & SIZE_LARGE) != 0) {
				size = IMPL_LARGE;
			} else if ((mask & SIZE_SMALL) != 0) {
				size = IMPL_SMALL;
			}
			// TODO: what to do with underlined ??

			Font font = new Font(name, style, size);
			fm = graphics.getFontMetrics(font);
			fonts.put(mask, fm);
		}
		return fm;
	}

	public static Font getFont(int mask) {
		return getFontMetrics(mask).getFont();
	}

	public static int getFontMask(Font f) {
		int mask = 0;
		if (f.isBold()) mask |= STYLE_BOLD;
		if (f.isItalic()) mask |= STYLE_ITALIC;

		int size = f.getSize();
		if (size < IMPL_MEDIUM) mask |= SIZE_SMALL;
		else if (size > IMPL_MEDIUM) mask |= SIZE_LARGE;

		if (f.getName().contains("Mono")) mask |= FACE_MONOSPACE;

		return mask;
	}
}

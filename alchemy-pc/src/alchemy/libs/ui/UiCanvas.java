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

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;

/**
 * PC implementation of the canvas.
 * @author Sergey Basalaev
 */
public final class UiCanvas extends UiScreen {

	private final CanvasImpl impl;

	public UiCanvas(boolean fullscreen) {
		super(null);
		impl = new CanvasImpl(this, UiPlatform.DEFAULT_WIDTH, UiPlatform.DEFAULT_HEIGHT);
	}

	public @Override JComponent getWidget() {
		return impl;
	}

	/* Action codes. */
	public static final int UP    = 1;
	public static final int DOWN  = 6;
	public static final int LEFT  = 2;
	public static final int RIGHT = 5;
	public static final int FIRE  = 8;
	public static final int ACT_A = 9;
	public static final int ACT_B = 10;
	public static final int ACT_C = 11;
	public static final int ACT_D = 12;

	public Graphics2D getGraphics() {
		return impl.getDrawingTarget();
	}

	public void repaint(int x, int y, int w, int h) {
		impl.repaint(x, y, w, h);
	}

	public void refresh() {
		impl.repaint();
	}

	public int actionCode(int key) {
		switch (key) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
			case KeyEvent.VK_NUMPAD8:
				return UP;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
			case KeyEvent.VK_NUMPAD2:
				return DOWN;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A:
			case KeyEvent.VK_NUMPAD4:
				return LEFT;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D:
			case KeyEvent.VK_NUMPAD6:
				return RIGHT;
			case KeyEvent.VK_NUMPAD5:
				return FIRE;
			default:
				return 0;
		}
	}

	public String keyName(int key) {
		return KeyEvent.getKeyText(key);
	}

	public boolean hasPtrEvents() {
		return true;
	}

	public boolean hasPtrDragEvent() {
		return true;
	}

	public boolean hasHoldEvent() {
		return false;
	}
}

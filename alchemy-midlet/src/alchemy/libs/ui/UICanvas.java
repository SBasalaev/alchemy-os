/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.libs.ui;

import alchemy.core.Int;
import alchemy.midlet.UIServer;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Double buffered canvas implementation for Alchemy UI.
 * @author Sergey Basalaev
 */
public class UICanvas extends Canvas {
	
	private Image buffer;
	
	public UICanvas(boolean fullscreen) {
		setFullScreenMode(fullscreen);
		if (buffer == null) {
			buffer = Image.createImage(getWidth(), getHeight());
		}
	}
	
	protected void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, 0);
	}
	
	/** Returns graphics of buffer. */
	public Graphics getGraphics() {
		return buffer.getGraphics();
	}

	public boolean isDoubleBuffered() {
		return true;
	}

	/** Generates key event. */
	protected void keyPressed(int keyCode) {
		UIServer.pushEvent(this, UIServer.EVENT_KEY_PRESS, new Int(keyCode));
	}

	/** Generates pointer press event. */
	protected void pointerPressed(int x, int y) {
		UIServer.pushEvent(this, UIServer.EVENT_PTR_PRESS, new Object[] {new Int(x), new Int(y)});
	}
	
	/** Generates pointer release event. */
	protected void pointerReleased(int x, int y) {
		UIServer.pushEvent(this, UIServer.EVENT_PTR_RELEASE, new Object[] {new Int(x), new Int(y)});
	}

	/** Generates pointer drag event. */
	protected void pointerDragged(int x, int y) {
		UIServer.pushEvent(this, UIServer.EVENT_PTR_DRAG, new Object[] {new Int(x), new Int(y)});
	}

	protected synchronized void sizeChanged(int w, int h) {
		Image oldbuf = buffer;
		buffer = Image.createImage(getWidth(), getHeight());
		if (oldbuf != null) {
			buffer.getGraphics().drawImage(oldbuf, 0, 0, Graphics.TOP | Graphics.LEFT);
		}
	}
}

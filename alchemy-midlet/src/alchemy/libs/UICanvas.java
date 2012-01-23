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

package alchemy.libs;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Double buffered canvas implementation for Alchemy UI.
 * @author Sergey Basalaev
 */
class UICanvas extends Canvas implements CommandListener {
	
	private final Image buffer;
	
	/** Queue of pressed keys. */
	private final int[] keyqueue =  new int[16];
	/** Index of first key code in the queue. */
	private int keyfirst = 0;
	/** Total number of key codes available in the queue. */
	private int keycount = 0;
	
	public UICanvas(boolean fullscreen) {
		setCommandListener(this);
		setFullScreenMode(fullscreen);
		buffer = Image.createImage(getWidth(), getHeight());
	}
	
	protected void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, 0);
	}
	
	/** Returns graphics of buffer. */
	public Graphics getGraphics() {
		return buffer.getGraphics();
	}

	public void commandAction(Command c, Displayable d) {
	}

	/** Adds key code in the queue. */
	protected void keyPressed(int keyCode) {
		synchronized (keyqueue) {
			if (keycount == keyqueue.length) return;
			int nextindex = (keyfirst + keycount) % keyqueue.length;
			keyqueue[nextindex] = keyCode;
			keycount++;
		}
	}
	
	/**
	 * Returns next key code from the queue.
	 * If there are no key presses in the queue this
	 * method returns <code>null</code>.
	 */
	public int readKeyCode() {
		synchronized (keyqueue) {
			if (keycount == 0) return 0;
			int code = keyqueue[keyfirst];
			keyfirst = (keyfirst + 1) % keyqueue.length;
			keycount--;
			return code;
		}
	}
}

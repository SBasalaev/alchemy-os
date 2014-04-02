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

import alchemy.system.UIServer;
import alchemy.types.Int32;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Canvas implementation.
 * @author Sergey Basalaev
 */
final class CanvasImpl extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

	private final BufferedImage buffer;
	private final UiCanvas owner;

	public CanvasImpl(UiCanvas owner, int width, int height) {
		this.owner = owner;
		setPreferredSize(new Dimension(width, height));
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@Override public void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, null);
	}

	@Override public void update(Graphics g) {
		this.paint(g);
	}

	public Graphics2D getDrawingTarget() {
		return buffer.createGraphics();
	}

	@Override public void keyPressed(KeyEvent e) {
		UIServer.pushEvent(UIServer.EVENT_KEY_PRESS, owner, Int32.toInt32(e.getKeyCode()));
	}

	@Override public void keyReleased(KeyEvent e) {
		UIServer.pushEvent(UIServer.EVENT_KEY_RELEASE, owner, Int32.toInt32(e.getKeyCode()));
	}

	@Override public void keyTyped(KeyEvent e) {
		// ignore, press/release sequence is generated anyway
	}

	@Override public void mousePressed(MouseEvent e) {
		UIServer.pushEvent(UIServer.EVENT_PTR_PRESS, owner,
				new Int32[] { Int32.toInt32(e.getX()), Int32.toInt32(e.getY()) });
	}

	@Override public void mouseReleased(MouseEvent e) {
		UIServer.pushEvent(UIServer.EVENT_PTR_RELEASE, owner,
				new Int32[] { Int32.toInt32(e.getX()), Int32.toInt32(e.getY()) });
	}

	@Override public void mouseClicked(MouseEvent e) {
		// ignore, press/release sequence is generated anyway
	}

	@Override public void mouseEntered(MouseEvent e) {
		// ignore, we are not interested in these events
	}

	@Override public void mouseExited(MouseEvent e) {
		// ignore, we are not interested in these events
	}

	@Override public void mouseMoved(MouseEvent e) {
		// ignore, we are not interested in these events
	}

	@Override public void mouseDragged(MouseEvent e) {
		UIServer.pushEvent(UIServer.EVENT_PTR_DRAG, owner,
				new Int32[] { Int32.toInt32(e.getX()), Int32.toInt32(e.getY()) });
	}
}

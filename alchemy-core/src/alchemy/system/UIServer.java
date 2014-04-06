/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.system;

import alchemy.platform.Platform;
import alchemy.platform.UI;
import alchemy.types.Int32;
import alchemy.util.ArrayList;

/**
 * UI server for Alchemy OS.
 * <h3>UI layout</h3>
 * The UI of the MIDlet is a stack of screens.
 * Each process can be mapped to at most one screen.
 * The screen that is on top of stack is presented to
 * the user and corresponding screen is able to generate
 * events. When top process dies its screen is removed
 * from stack and the next screen is shown.
 *
 * <h3>Event dispatching</h3>
 * <code>UIServer</code> holds queue of events for each mapped process.
 *
 * @author Sergey Basalaev
 */
public final class UIServer {
	/** Event type for gaining focus. */
	public static final Int32 EVENT_SHOW = Int32.M_ONE;
	/** Event type for losing focus. */
	public static final Int32 EVENT_HIDE = Int32.toInt32(-2);
	/** Event type for on-screen menu. */
	public static final Int32 EVENT_MENU = Int32.ONE;
	/** Event type for on-item menu. */
	public static final Int32 EVENT_HYPERLINK = Int32.toInt32(2);
	/** Event type for canvas key press. */
	public static final Int32 EVENT_KEY_PRESS = Int32.toInt32(3);
	/** Event type for canvas key hold. */
	public static final Int32 EVENT_KEY_HOLD = Int32.toInt32(4);
	/** Event type for canvas key release. */
	public static final Int32 EVENT_KEY_RELEASE = Int32.toInt32(5);
	/** Event type for canvas pointer press. */
	public static final Int32 EVENT_PTR_PRESS = Int32.toInt32(6);
	/** Event type for canvas pointer release. */
	public static final Int32 EVENT_PTR_RELEASE = Int32.toInt32(7);
	/** Event type for canvas pointer drag. */
	public static final Int32 EVENT_PTR_DRAG = Int32.toInt32(8);
	/** Event type for changed state of item. */
	public static final Int32 EVENT_ITEM_STATE = Int32.toInt32(9);

	private UIServer() { }

	private static final ArrayList frameStack = new ArrayList();

	private static final UI ui = Platform.getPlatform().getUI();

	private static final UIProcessListener uiListener = new UIProcessListener();

	/** Finds frame in the frame stack by its process or screen. */
	private static int frameIndex(Object obj) {
		for (int index = frameStack.size()-1; index >= 0; index--) {
			UIFrame frame = (UIFrame)frameStack.get(index);
			if (frame.process == obj || frame.screen == obj) return index;
		}
		return -1;
	}

	/** Adds given event to the event queue. */
	public static synchronized void pushEvent(Int32 kind, Object screen, Object value) {
		int idx = frameIndex(screen);
		if (idx >= 0) {
			final UIFrame frame = (UIFrame)frameStack.get(idx);
			synchronized (frame) {
				frame.push(new Object[] {kind, screen, value});
				frame.notify();
			}
		}
	}

	/** Shows screen that is currently on top of the stack and pushes SHOW/HIDE events. */
	public static void displayCurrent() {
		if (frameStack.isEmpty()) {
			ui.setCurrentScreen(null);
			return;
		}
		UIFrame frame = ((UIFrame)frameStack.last());
		Object oldScreen = ui.getCurrentScreen();
		Object newScreen = frame.screen;
		if (oldScreen != newScreen) {
			if (oldScreen != null) pushEvent(EVENT_HIDE, oldScreen, null);
			if (newScreen != null) pushEvent(EVENT_SHOW, newScreen, null);
		}
		ui.setCurrentScreen(newScreen);
		ui.setIcon(frame.process.getGlobal(null, "ui.icon", null));
	}

	/** Assigns screen to the process. */
	public static synchronized void setScreen(Process p, Object screen) {
		int idx = frameIndex(p);
		if (idx >= 0) {
			((UIFrame)frameStack.get(idx)).screen = screen;
		} else {
			frameStack.add(new UIFrame(p, screen));
			p.addProcessListener(uiListener);
		}
		displayCurrent();
	}

	/** Removes screen of given process. */
	public static synchronized void removeScreen(Process p) {
		int idx = frameIndex(p);
		if (idx >= 0) {
			UIFrame frame = (UIFrame)frameStack.get(idx);
			frame.process.removeProcessListener(uiListener);
			frameStack.remove(idx);
			displayCurrent();
		}
	}

	/** Returns screen this process mapped to. */
	public static synchronized Object getScreen(Process p) {
		int idx = frameIndex(p);
		if (idx >= 0) {
			UIFrame frame = (UIFrame)frameStack.get(idx);
			return frame.screen;
		} else {
			return null;
		}
	}

	/**
	 * Reads next event object for given process.
	 * If the event queue is empty and wait is <code>true</code>
	 * this method blocks until an event is available, otherwise
	 * it returns <code>null</code> on empty queue.
	 *
	 * @throws IllegalStateException  if process is not mapped to a screen
	 * @throws InterruptedException  if process was interrupted
	 */
	public static Object[] readEvent(Process p, boolean wait) throws IllegalStateException, InterruptedException {
		UIFrame frame = null;
		synchronized (UIServer.class) {
			int idx = frameIndex(p);
			if (idx >= 0) frame = ((UIFrame)frameStack.get(idx));
		}
		if (frame == null) throw new IllegalStateException("Screen is not set");
		synchronized (frame) {
			Object[] e = frame.pop();
			if (e == null && wait) {
				frame.wait();
				e = frame.pop();
			}
			return e;
		}
	}

	private static final class UIProcessListener implements ProcessListener {

		public void processEnded(Process p) {
			synchronized (UIServer.class) {
				int idx = frameIndex(p);
				if (idx >= 0) {
					frameStack.remove(idx);
				}
			}
			displayCurrent();
		}
	}

	/** Holds process screen and event queue. */
	private static final class UIFrame {
		private static final int QUEUE_SIZE = 32;

		public final Process process;
		public Object screen;
		private Object[][] queue;
		private int qFirst;
		private int qCount;

		public UIFrame(Process p, Object screen) {
			this.process = p;
			this.screen = screen;
			this.queue = new Object[QUEUE_SIZE][];
		}

		public boolean push(Object[] event) {
			if (qCount == QUEUE_SIZE) return false;
			queue[(qFirst + qCount) % QUEUE_SIZE] = event;
			qCount++;
			return true;
		}

		public Object[] pop() {
			if (qCount == 0) return null;
			Object[] event = queue[qFirst];
			qFirst = (qFirst+1) % QUEUE_SIZE;
			qCount--;
			return event;
		}
	}
}

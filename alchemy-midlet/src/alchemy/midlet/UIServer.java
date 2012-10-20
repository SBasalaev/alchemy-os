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

package alchemy.midlet;

import alchemy.core.Context;
import alchemy.core.ContextListener;
import alchemy.core.Int;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;

/**
 * UI server for Alchemy.
 * <h3>UI layout</h3>
 * The UI of the MIDlet is a stack of screens.
 * Each context can be mapped to at most one screen.
 * The screen that is on top of stack is presented to
 * the user and corresponding screen is able to generate
 * events. When top context dies its screen is removed
 * from stack and the next screen is shown.
 * 
 * <h3>Event dispatching</h3>
 * <code>UIServer</code> holds queue of events for each mapped context.
 * 
 * @author Sergey Basalaev
 */
public final class UIServer {
	/** Event type for gaining focus. */
	public static final Int EVENT_SHOW = Int.M_ONE;
	/** Event type for losing focus. */
	public static final Int EVENT_HIDE = Int.toInt(-2);
	/** Event type for on-screen menu. */
	public static final Int EVENT_MENU = Int.ONE;
	/** Event type for on-item menu. */
	public static final Int EVENT_ITEM_MENU = Int.toInt(2);
	/** Event type for canvas key press. */
	public static final Int EVENT_KEY_PRESS = Int.toInt(3);
	/** Event type for canvas pointer press. */
	public static final Int EVENT_PTR_PRESS = Int.toInt(6);
	/** Event type for canvas pointer release. */
	public static final Int EVENT_PTR_RELEASE = Int.toInt(7);
	/** Event type for canvas pointer drag. */
	public static final Int EVENT_PTR_DRAG = Int.toInt(8);
	
	/** Display set by Alchemy MIDlet. */
	static Display display;

	/** Holds all contexts which have associated screens. */
	private static final Vector frames = new Vector();
	
	private static final UIContextListener l = new UIContextListener();
	private static final UICommandListener cl = new UICommandListener();
	private static final UIItemCommandListener icl = new UIItemCommandListener();
	
	private static final List appList = new List("Applications", Choice.IMPLICIT);
	private static final Command appCommand = new Command("Apps...", Command.SCREEN, 100);
	
	static {
		appList.setCommandListener(new AppListCommandListener());
	}
	
	private UIServer() { }
	
	/** Finds frame in a vector by its Context or Displayable. */
	private static int frameIndex(Object obj) {
		int index;
		for (index=frames.size()-1; index>=0; index--) {
			UIFrame frame = (UIFrame)frames.elementAt(index);
			if (frame.c == obj || frame.d == obj) break;
		}
		return index;
	}

	/**
	 * Maps specified context to given screen.
	 * If context is already mapped then its screen is replaced.
	 * If context is not mapped then new mapping is added and
	 * corresponding screen is placed on top of the screen stack.
	 */
	public static synchronized void setScreen(Context c, Displayable d) {
		int i = frameIndex(c);
		if (i >= 0) {
			((UIFrame)frames.elementAt(i)).d = d;
		} else {
			frames.addElement(new UIFrame(c, d));
			c.addContextListener(l);
			appList.append(c.getName(), null);
		}
		d.addCommand(appCommand);
		d.setCommandListener(cl);
		displayCurrent();
	}
	
	/**
	 * Unmaps context mapping and removes corresponding screen
	 * from the screen stack.
	 */
	public static synchronized void removeScreen(Context c) {
		int index = frameIndex(c);
		if (index >= 0) {
			UIFrame frame = (UIFrame)frames.elementAt(index);
			frame.c.removeContextListener(l);
			frame.d.setCommandListener(null);
			frame.d.removeCommand(appCommand);
			frames.removeElementAt(index);
			appList.delete(index);
			displayCurrent();
		}
	}
	
	/**
	 * Returns screen this context mapped to or <code>null</code>.
	 */
	public static synchronized Displayable getScreen(Context c) {
		int index = frameIndex(c);
		if (index >= 0) {
			UIFrame frame = (UIFrame)frames.elementAt(index);
			return frame.d;
		} else {
			return null;
		}
	}

	public static void alert(String title, String text, AlertType type) {
		Alert a = new Alert(title, text, null, type);
		display.setCurrent(a);
	}
	
	public static Displayable currentScreen() {
		if (frames.isEmpty()) return null;
		return ((UIFrame)frames.lastElement()).d;
	}
	
	public static void displayCurrent() {
		Displayable oldscr = display.getCurrent();
		Displayable newscr = currentScreen();
		if (oldscr != newscr) {
			if (oldscr != null) pushEvent(oldscr, EVENT_HIDE, null);
			if (newscr != null) pushEvent(newscr, EVENT_SHOW, null);
		}
		display.setCurrent(newscr);
	}
	
	/** Adds given event to the event queue. */
	public static synchronized void pushEvent(Displayable d, Int kind, Object value) {
		int i = frameIndex(d);
		if (i >= 0) {
			final UIFrame frame = (UIFrame)frames.elementAt(i);
			synchronized (frame) {
				frame.queue.push(new Object[] {kind, d, value});
				frame.notify();
			}
		}
	}
	
	/**
	 * Reads next event object for given context.
	 * If the event queue is empty and wait is <code>true</code>
	 * this method blocks until an event is available, otherwise
	 * it returns <code>null</code> on empty queue.
	 *
	 * @throws IllegalStateException  if context is not mapped to a screen
	 */
	public static Object readEvent(Context c, boolean wait) throws IllegalStateException {
		UIFrame frame = null;
		synchronized (UIServer.class) {
			int i = frameIndex(c);
			if (i >= 0) frame = ((UIFrame)frames.elementAt(i));
		}
		if (frame == null) throw new IllegalStateException("Screen is not set.");
		synchronized (frame) {
			Object e = frame.queue.pop();
			if (e == null && wait) {
				try { frame.wait(); }
				catch (InterruptedException ie) { }
				e = frame.queue.pop();
			}
			return e;
		}
	}
	
	/** Registers UI server as listener for item events. */
	public static void receiveItemEvents(Item item) {
		item.setItemCommandListener(icl);
	}
		
	public static String getDefaultTitle(Context c) {
		Object title = String.valueOf(c.get("ui.title"));
		return (title != null) ? title.toString() : c.getName();
	}
	
	public static void setDefaultTitle(Context c, String title) {
		c.set("ui.title", title);
		synchronized (UIServer.class) {
			int index = frameIndex(c);
			if (index >= 0) {
				appList.set(index, title, appList.getImage(index));
			}
		}
	}

	public static Image getDefaultIcon(Context c) {
		return (Image)c.get("ui.icon");
	}
	
	public static void setDefaultIcon(Context c, Image img) {
		c.set("ui.icon", img);
		synchronized (UIServer.class) {
			int index = frameIndex(c);
			if (index >= 0) {
				appList.set(index, appList.getString(index), img);
			}
		}
	}
	
	public static boolean vibrate(int millis) {
		return display.vibrate(millis);
	}
	
	public static boolean flash(int millis) {
		return display.flashBacklight(millis);
	}
	
	/** Removes screen mapping when context ends. */
	private static class UIContextListener implements ContextListener {
		public void contextEnded(Context c) {
			removeScreen(c);
		}
	}
	
	/** Generates screen menu events. */
	private static class UICommandListener implements CommandListener {
		public void commandAction(Command command, Displayable d) {
			if (command == appCommand) { // show app selection screen
				appList.setSelectedIndex(0, true);
				pushEvent(currentScreen(), EVENT_HIDE, null);
				display.setCurrent(appList);
			} else {
				pushEvent(d, EVENT_MENU, command);
			}
		}
	}
	
	/** Generates item menu events. */
	private static class UIItemCommandListener implements ItemCommandListener {
		public void commandAction(Command command, Item item) {
			pushEvent(null, EVENT_ITEM_MENU, item);
		}
	}
	
	private static class AppListCommandListener implements CommandListener {
		public void commandAction(Command c, Displayable d) {
			synchronized (UIServer.class) {
				int index = appList.getSelectedIndex();
				if (index != appList.size()-1) {
					UIFrame frame = (UIFrame)frames.elementAt(index);
					frames.removeElementAt(index);
					frames.addElement(frame);
					appList.delete(index);
					appList.append(getDefaultTitle(frame.c), getDefaultIcon(frame.c));
				}
				displayCurrent();
			}
		}
	}
	
	/** Context event queue. */
	private static class Queue {
		private static final int QUEUE_SIZE = 10;
		
		/** Event queue. */
		private final Object[] events = new Object[QUEUE_SIZE];
		/** Index of the first element in queue. */
		private int ev_start = 0;
		/** Count of elements in queue. */
		private int ev_count = 0;
		
		/** Clears event queue. */
		public void clear() {
			ev_count = 0;
		}
		
		/** Adds new event to the event queue. */
		public void push(Object e) {
			events[(ev_start + ev_count) % QUEUE_SIZE] = e;
			if (ev_count < QUEUE_SIZE) {
				ev_count++;
			} else {
				// if queue is full, throw away first element
				ev_start = (ev_start+1) % QUEUE_SIZE;
			}
		}
		
		/** Reads event from the event queue. */
		public Object pop() {
			if (ev_count == 0) return null;
			Object ret = events[ev_start];
			ev_start = (ev_start+1) % QUEUE_SIZE;
			ev_count--;
			return ret;
		}
	}

	private static class UIFrame {
		final Context c;
		Displayable d;
		final Queue queue;

		public UIFrame(Context c, Displayable d) {
			this.c = c;
			this.d = d;
			queue = new Queue();
		}
	}
}

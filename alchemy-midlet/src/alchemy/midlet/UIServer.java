/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012  Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.core.Function;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

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
	public static final Integer EVENT_SHOW = new Integer(-1);
	/** Event type for losing focus. */
	public static final Integer EVENT_HIDE = new Integer(-2);
	/** Event type for command activation. */
	public static final Integer EVENT_MENU = new Integer(1);
	/** Event type for canvas key press. */
	public static final Integer EVENT_KEY_PRESS = new Integer(3);
	/** Event type for canvas pointer press. */
	public static final Integer EVENT_PTR_PRESS = new Integer(6);
	/** Event type for canvas pointer release. */
	public static final Integer EVENT_PTR_RELEASE = new Integer(7);
	/** Event type for canvas pointer drag. */
	public static final Integer EVENT_PTR_DRAG = new Integer(8);
	
	/** Display set by Alchemy MIDlet. */
	static Display display;

	private static final Vector contexts = new Vector();
	private static final Vector screens = new Vector();
	private static final Vector queues = new Vector();
	
	private static final UIContextListener l = new UIContextListener();
	private static final UICommandListener cl = new UICommandListener();
	
	private UIServer() { }
	
	/**
	 * Maps specified context to given screen.
	 * If context is already mapped then its screen is replaced.
	 * If context is not mapped then new mapping is added and
	 * corresponding screen is placed on top of the screen stack.
	 */
	public static synchronized void mapContext(Context c, Displayable d) {
		d.setCommandListener(cl);
		int index = contexts.indexOf(c);
		if (index < 0) {
			c.addContextListener(l);
			contexts.addElement(c);
			screens.addElement(d);
			queues.addElement(new Queue());
		} else {
			screens.setElementAt(d, index);
			((Queue)queues.elementAt(index)).clearEvents();
		}
		displayCurrent();
	}
	
	/**
	 * Unmaps context mapping and removes corresponding screen
	 * from the screen stack.
	 */
	public static synchronized void unmapContext(Context c) {
		int index = contexts.indexOf(c);
		if (index >= 0) {
			c.removeContextListener(l);
			((Displayable)screens.elementAt(index)).setCommandListener(null);
			contexts.removeElementAt(index);
			screens.removeElementAt(index);
			queues.removeElementAt(index);
			displayCurrent();
		}
	}
	
	public static void alert(String title, String text, AlertType type) {
		Alert a = new Alert(title, text, null, type);
		display.setCurrent(a);
	}
	
	public static synchronized Displayable currentScreen() {
		int top = contexts.size()-1;
		return (top >= 0) ? ((Displayable)screens.elementAt(top)) : null;
	}
	
	public static void displayCurrent() {
		Displayable oldscr = display.getCurrent();
		Displayable newscr = currentScreen();
		if (oldscr != newscr) {
			if (oldscr != null) addEvent(oldscr, EVENT_HIDE, Function.ZERO, Function.ZERO);
			if (newscr != null) addEvent(newscr, EVENT_SHOW, Function.ZERO, Function.ZERO);
		}
		display.setCurrent(newscr);
	}
	
	/** Adds given event to the event queue. */
	public static synchronized void addEvent(Displayable d, Integer kind, Integer val1, Integer val2) {
		int index = screens.indexOf(d);
		if (index < 0) return;
		((Queue)queues.elementAt(index)).pushEvent(new Object[] {d, kind, val1, val2 });
	}
	
	/** Reads next event object for given context. */
	public static synchronized Object readEvent(Context c) {
		int index = contexts.indexOf(c);
		if (index < 0) return null;
		return ((Queue)queues.elementAt(index)).popEvent();
	}
	
	/** Removes screen mapping when context ends. */
	private static class UIContextListener implements ContextListener {
		public void contextEnded(Context c) {
			unmapContext(c);
		}
	}
	
	/** Generates command events. */
	private static class UICommandListener implements CommandListener {
		public void commandAction(Command c, Displayable d) {
			addEvent(d, EVENT_MENU, new Integer(c.getPriority()), Function.ZERO);
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
		public void clearEvents() {
			ev_count = 0;
		}
		
		/** Adds new event to the event queue. */
		public void pushEvent(Object e) {
			events[(ev_start + ev_count) % QUEUE_SIZE] = e;
			if (ev_count < QUEUE_SIZE) {
				ev_count++;
			} else {
				// if queue is full, throw away first element
				ev_start = (ev_start+1) % QUEUE_SIZE;
			}
		}
		
		/** Reads event from the event queue. */
		public Object popEvent() {
			if (ev_count == 0) return null;
			Object ret = events[ev_start];
			ev_start = (ev_start+1) % QUEUE_SIZE;
			ev_count--;
			return ret;
		}
	}
}

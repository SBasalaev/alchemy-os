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

/**
 * PC implementation of the attachable screen menu.
 * @author Sergey Basalaev
 */
public final class UiMenu {

	public static final int MT_SCREEN = 1;
	public static final int MT_BACK = 2;
	public static final int MT_CANCEL = 3;
	public static final int MT_OK = 4;
	public static final int MT_HELP = 5;
	public static final int MT_STOP = 6;
	public static final int MT_EXIT = 7;

	private final String label;
	private final int priority;
	private final int type;

	public UiMenu(String label, int priority, int type) {
		if (type < MT_SCREEN || type > MT_EXIT) throw new IllegalArgumentException();
		this.label = label;
		this.priority = priority;
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public int getPriority() {
		return priority;
	}

	public int getType() {
		return type;
	}
}

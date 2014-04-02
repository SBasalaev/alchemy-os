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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;

/**
 * Implementation of Ether Screen type.
 * @author Sergey Basalaev
 */
public abstract class UiScreen {

	private String title;
	private Set<UiMenu> menus;

	protected UiScreen() {
		this.menus = new HashSet<UiMenu>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWidth() {
		return getWidget().getWidth();
	}

	public int getHeight() {
		return getWidget().getHeight();
	}

	public final void addMenu(UiMenu menu) {
		menus.add(menu);
	}

	public final void removeMenu(UiMenu menu) {
		menus.remove(menu);
	}

	/** For the platform UI. */
	public abstract JComponent getWidget();

	/** For the platform UI. */
	final Set<UiMenu> getMenus() {
		return Collections.unmodifiableSet(menus);
	}
}

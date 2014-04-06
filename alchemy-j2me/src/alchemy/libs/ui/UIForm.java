/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.system.UIServer;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;

/**
 *
 * @author sbasalaev
 */
public final class UIForm extends Form implements ItemCommandListener, ItemStateListener {

	public UIForm(String title) {
		super(title);
	}

	public int append(Item item) {
		item.setItemCommandListener(this);
		return super.append(item);
	}

	public void insert(int itemNum, Item item) {
		item.setItemCommandListener(this);
		super.insert(itemNum, item);
	}

	public void set(int itemNum, Item item) {
		item.setItemCommandListener(this);
		super.set(itemNum, item);
	}

	public void commandAction(Command c, Item item) {
		UIServer.pushEvent(UIServer.EVENT_HYPERLINK, this, item);
	}

	public void itemStateChanged(Item item) {
		UIServer.pushEvent(UIServer.EVENT_ITEM_STATE, this, item);
	}
}

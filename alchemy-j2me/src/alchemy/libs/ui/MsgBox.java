/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 * MsgBox implementation based on Form.
 * MsgBox was created because I could not integrate Alerts
 * in Alchemy. They behave incorrectly within UI stack.
 * 
 * @author Sergey Basalaev
 */
public class MsgBox extends Form {
	
	private final StringItem text;
	private final ImageItem image;
	
	public MsgBox(String title, String msg, Image img) {
		super(title);
		this.text = new StringItem(null, msg);
		this.image = new ImageItem(null, img, Item.LAYOUT_NEWLINE_AFTER, "Icon");
		append(image);
		append(text);
	}
	
	public void setString(String msg) {
		text.setText(msg);
	}
	
	public String getString() {
		return text.getText();
	}
	
	public void setImage(Image img) {
		image.setImage(img);
	}
	
	public Image getImage() {
		return image.getImage();
	}
}

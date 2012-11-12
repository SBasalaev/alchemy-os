/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

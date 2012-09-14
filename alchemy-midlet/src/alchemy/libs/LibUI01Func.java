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

package alchemy.libs;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.midlet.UIServer;
import java.io.InputStream;
import java.util.Date;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * libui.0.1.so functions.
 * @author Sergey Basalaev
 */
class LibUI01Func extends Function {
	
	private final int index;

	public LibUI01Func(String name, int index) {
		super(name);
		this.index = index;
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // new_image(w: Int, h: Int): Image
				return Image.createImage(ival(args[0]), ival(args[1]));
			case 1: // image_graphics(im: Image): Graphics
				return ((Image)args[0]).getGraphics();
			case 2: { // image_from_argb(argb: Array, w: Int, h: Int, alpha: Bool): Image
				Object[] data = (Object[])args[0];
				final int[] argb = new int[data.length];
				for (int i=argb.length-1; i>=0; i--) {
					argb[i] = ival(data[i]);
				}
				return Image.createRGBImage(argb, ival(args[1]), ival(args[2]), true);
			}
			case 3: // image_from_stream(in: IStream): Image
				return Image.createImage((InputStream)args[0]);
			case 4: { // image_from_data(data: BArray): Image
				final byte[] data = (byte[])args[0];
				return Image.createImage(data, 0, data.length);
			}
			case 5: // image_from_image(im: Image, x: Int, y: Int, w: Int, h: Int): Image
				return Image.createImage((Image)args[0], ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), 0);
			case 6: { // get_image_argb(im: Image, argb: Array, ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int)
				Object[] data = (Object[])args[1];
				int ofs = ival(args[2]);
				int scanlen = ival(args[3]);
				int x = ival(args[4]);
				int y = ival(args[5]);
				int w = ival(args[6]);
				int h = ival(args[7]);
				int[] argb = new int[scanlen * h];
				((Image)args[0]).getRGB(argb, 0, scanlen, x, y, w, h);
				for (int i=argb.length-1; i>=0; i--) {
					data[ofs+i] = Ival(argb[i]);
				}
				return null;
			}
			case 7: // get_color(g: Graphics): Int
				return Ival(((Graphics)args[0]).getColor());
			case 8: // set_color(g: Graphics, rgb: Int)
				((Graphics)args[0]).setColor(ival(args[1]));
				return null;
			case 9: // get_font(g: Graphics): Int
				return Ival(font2int(((Graphics)args[0]).getFont()));
			case 10: // set_font(g: Graphics, font: Int)
				((Graphics)args[0]).setFont(int2font(ival(args[1])));
				return null;
			case 11: // str_width(font: Int, str: String): Int
				return Ival(int2font(ival(args[0])).stringWidth((String)args[1]));
			case 12: // font_height(font: Int): Int
				return Ival(int2font(ival(args[0])).getHeight());
			case 13: // draw_line(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int)
				((Graphics)args[0]).drawLine(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 14: // draw_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).drawRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 15: // fill_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).fillRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 16: // draw_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).drawRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 17: // fill_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).fillRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 18: // draw_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).drawArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 19: // fill_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).fillArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 20: // fill_triangle(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int)
				((Graphics)args[0]).fillTriangle(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 21: // draw_string(g: Graphics, str: String, x: Int, y: Int)
				((Graphics)args[0]).drawString((String)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 22: // draw_image(g: Graphics, im: Image, x: Int, y: Int)
				((Graphics)args[0]).drawImage((Image)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 23: // copy_area(g: Graphics, xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int)
				((Graphics)args[0]).copyArea(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), 0);
				return null;
			case 24: // new_canvas(full: Bool): Screen
				return new UICanvas(bval(args[0]));
			case 25: { // canvas_graphics(cnv: Screen): Graphics
				return ((UICanvas)args[0]).getGraphics();
			}
			case 26: { // canvas_read_key(cnv: Screen): Int
				// compatibility function with previous non-event canvas behavior
				Object[] event = (Object[])UIServer.readEvent(c, false);
				if (event != null && event[0] == UIServer.EVENT_KEY_PRESS && event[1] == args[0]) {
					return event[2];
				} else {
					return Function.ZERO;
				}
			}
			case 27: { // ui_set_screen(scr: Screen)
				if (args[0] != null) {
					UIServer.setScreen(c, (Displayable)args[0]);
				} else {
					UIServer.removeScreen(c);
				}
				return null;
			}
			case 28: // screen_height(scr: Screen): Int
				return Ival(((Displayable)args[0]).getHeight());
			case 29: // screen_width(scr: Screen): Int
				return Ival(((Displayable)args[0]).getWidth());
			case 30: // screen_get_title(scr: Screen): String
				return ((Displayable)args[0]).getTitle();
			case 31: { // screen_set_title(scr: Screen, title: String)
				((Displayable)args[0]).setTitle((String)args[1]);
				return null;
			}
			case 32: // screen_shown(scr: Screen): Bool
				return Ival(((Displayable)args[0]).isShown());
			case 33: // canvas_refresh(cnv: Screen)
				((UICanvas)args[0]).repaint();
				return null;
			case 34: // ui_read_event(): UIEvent
				return UIServer.readEvent(c, false);
			case 35: // ui_wait_event(): UIEvent
				return UIServer.readEvent(c, true);
			case 36: // new_editbox(mode: Int): Screen
				return new TextBox(null, null, Short.MAX_VALUE, ival(args[0]));
			case 37: // editbox_get_text(box: Screen): String
				return ((TextBox)args[0]).getString();
			case 38: // editbox_set_text(box: Screen, text: String)
				((TextBox)args[0]).setString((String)args[1]);
				return null;
			case 39: { // new_listbox(strings: Array, images: Array, select: Menu): Screen
				Object[] str = (Object[])args[0];
				String[] strings = new String[str.length];
				System.arraycopy(str, 0, strings, 0, str.length);
				Object[] img = (Object[])args[1];
				Image[] images = null;
				if (img != null) {
					images = new Image[img.length];
					System.arraycopy(img, 0, images, 0, img.length);
				}
				List list = new List(null, Choice.IMPLICIT, strings, images);
				list.setSelectCommand((Command)args[2]);
				return list;
			}
			case 40: // listbox_get_index(list: Screen): Int
				return Ival(((List)args[0]).getSelectedIndex());
			case 41: // listbox_set_index(list: Screen, index: Int)
				((List)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 42: // ui_get_screen(): Screen
				return UIServer.getScreen(c);
			case 43: // new_menu(text: String, priority: Int): Menu
				return new Command((String)args[0], Command.SCREEN, ival(args[1]));
			case 44: // menu_get_text(menu: Menu): String
				return ((Command)args[0]).getLabel();
			case 45: // menu_get_priority(menu: Menu): Int
				return Ival(((Command)args[0]).getPriority());
			case 46: // screen_add_menu(scr: Screen, menu: Menu)
				((Displayable)args[0]).addCommand((Command)args[1]);
				return null;
			case 47: // screen_remove_menu(scr: Screen, menu: Menu)
				((Displayable)args[0]).removeCommand((Command)args[1]);
				return null;
			case 48: // new_form(): Screen
				return new Form(null);
			case 49: // form_add(form: Screen, item: Item)
				((Form)args[0]).append((Item)args[1]);
				return null;
			case 50: // form_get(form: Screen, at: Int): Item
				return ((Form)args[0]).get(ival(args[1]));
			case 51: // form_set(form: Screen, at: Int, item: Item)
				((Form)args[0]).set(ival(args[1]), (Item)args[2]);
				return null;
			case 52: // form_insert(form: Screen, at: Int, item: Item)
				((Form)args[0]).insert(ival(args[1]), (Item)args[2]);
				return null;
			case 53: // form_remove(form: Screen, at: Int)
				((Form)args[0]).delete(ival(args[1]));
				return null;
			case 54: // form_size(form: Screen): Int
				return Ival(((Form)args[0]).size());
			case 55: // form_clear(form: Screen)
				((Form)args[0]).deleteAll();
				return null;
			case 56: // item_get_label(item: Item): String
				return ((Item)args[0]).getLabel();
			case 57: // item_set_label(item: Item, label: String)
				((Item)args[0]).setLabel((String)args[1]);
				return null;
			case 58: // new_textitem(label: String, text: String): Item
				return new StringItem((String)args[0], String.valueOf(args[1])+'\n');
			case 59: // textitem_get_text(item: Item): String
				return ((StringItem)args[0]).getText();
			case 60: // textitem_set_text(item: Item, text: String)
				((StringItem)args[0]).setText(String.valueOf(args[1])+'\n');
				return null;
			case 61: // textitem_get_font(item: Item): Int
				return Ival(font2int(((StringItem)args[0]).getFont()));
			case 62: // textitem_set_font(item: Item, font: Int)
				((StringItem)args[0]).setFont(int2font(ival(args[1])));
				return null;
			case 63: // new_imageitem(label: String, img: Image): Item
				return new ImageItem((String)args[0], (Image)args[1], Item.LAYOUT_NEWLINE_AFTER, null);
			case 64: // imageitem_get_image(item: Item): Image
				return ((ImageItem)args[0]).getImage();
			case 65: // imageitem_set_image(item: Item, img: Image)
				((ImageItem)args[0]).setImage((Image)args[1]);
				return null;
			case 66: // new_edititem(label: String, text: String, mode: Int, size: Int): Item
				return new TextField((String)args[0], (String)args[1], ival(args[3]), ival(args[2]));
			case 67: // edititem_get_text(item: Item): String
				return ((TextField)args[0]).getString();
			case 68: // edititem_set_text(item: Item, text: String)
				((TextField)args[0]).setString((String)args[1]);
				return null;
			case 69: // new_gaugeitem(label: String, max: Int, init: Int): Item
				return new Gauge((String)args[0], true, ival(args[1]), ival(args[2]));
			case 70: // gaugeitem_get_value(item: Item): Int
				return Ival(((Gauge)args[0]).getValue());
			case 71: // gaugeitem_set_value(item: Item, val: Int)
				((Gauge)args[0]).setValue(ival(args[1]));
				return null;
			case 72: // gaugeitem_get_maxvalue(item: Item): Int
				return Ival(((Gauge)args[0]).getMaxValue());
			case 73: // gaugeitem_set_maxvalue(item: Item, val: Int)
				((Gauge)args[0]).setMaxValue(ival(args[1]));
				return null;
			case 74: // new_dateitem(label: String, mode: Int): Item
				return new DateField((String)args[0], ival(args[1]));
			case 75: { // dateitem_get_date(item: Item): Long
				Date date = ((DateField)args[0]).getDate();
				return (date == null) ? null : Lval(date.getTime());
			}
			case 76: // dateitem_set_date(item: Item, date: Long)
				((DateField)args[0]).setDate(new Date(lval(args[1])));
				return null;
			case 77: { // new_checkitem(label: String, text: String, checked: Bool): Item
				ChoiceGroup check = new ChoiceGroup((String)args[0], Choice.MULTIPLE);
				check.append((String)args[1], null);
				check.setSelectedIndex(0, bval(args[2]));
				return check;
			}
			case 78: { // checkitem_get_checked(item: Item): Bool
				boolean[] checked = new boolean[1];
				((ChoiceGroup)args[0]).getSelectedFlags(checked);
				return Ival(checked[0]);
			}
			case 79: // checkitem_set_checked(item: Item, checked: Bool)
				((ChoiceGroup)args[0]).setSelectedIndex(0, bval(args[1]));
				return null;
			case 80: // checkitem_get_text(item: Item): String
				return ((ChoiceGroup)args[0]).getString(0);
			case 81: // checkitem_set_text(item: Item, text: String)
				((ChoiceGroup)args[0]).set(0, (String)args[1], null);
				return null;
			case 82: { // new_radioitem(label: String, strings: Array): Item
				Object[] array = (Object[])args[1];
				String[] strings = new String[array.length];
				System.arraycopy(array, 0, strings, 0, array.length);
				return new ChoiceGroup((String)args[0], Choice.EXCLUSIVE, strings, null);
			}
			case 83: // radioitem_get_index(item: Item): Int
				return Ival(((ChoiceGroup)args[0]).getSelectedIndex());
			case 84: // radioitem_set_index(item: Item, index: String)
				((ChoiceGroup)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 85: { // new_textbox(text: String): Screen
				Form box = new Form(null);
				box.append(new StringItem(null, (String)args[0]));
				return box;
			}
			case 86: // textbox_get_text(box: Screen): String
				return ((StringItem)((Form)args[0]).get(0)).getText();
			case 87: // textbox_set_text(box: Screen, text: String)
				((StringItem)((Form)args[0]).get(0)).setText((String)args[1]);
				return null;
			default:
				return null;
		}
	}
	
	private static int font2int(Font f) {
		return f.getFace() | f.getSize() | f.getStyle();
	}
	
	private static Font int2font(int mask) {
		return Font.getFont(mask & 0x60, mask & 0x7, mask & 0x18);
	}

	public String toString() {
		return "libui0.1.so:"+signature;
	}
}

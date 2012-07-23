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

import alchemy.libs.ui.UICanvas;
import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.midlet.UIServer;
import alchemy.nlib.NativeFunction;
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
 * libui.1.so functions.
 * @author Sergey Basalaev
 */
class LibUI1Func extends NativeFunction {

	public LibUI1Func(String name, int index) {
		super(name, index);
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // new_image(w: Int, h: Int): Image
				return Image.createImage(ival(args[0]), ival(args[1]));
			case 1: // Image.graphics(): Graphics
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
			case 6: { // Image.get_argb(argb: Array, ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int)
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
			case 7: // Graphics.get_color(): Int
				return Ival(((Graphics)args[0]).getColor());
			case 8: // Graphics.set_color(rgb: Int)
				((Graphics)args[0]).setColor(ival(args[1]));
				return null;
			case 9: // Graphics.get_font(): Int
				return Ival(font2int(((Graphics)args[0]).getFont()));
			case 10: // Graphics.set_font(font: Int)
				((Graphics)args[0]).setFont(int2font(ival(args[1])));
				return null;
			case 11: // str_width(font: Int, str: String): Int
				return Ival(int2font(ival(args[0])).stringWidth((String)args[1]));
			case 12: // font_height(font: Int): Int
				return Ival(int2font(ival(args[0])).getHeight());
			case 13: // Graphics.draw_line(x1: Int, y1: Int, x2: Int, y2: Int)
				((Graphics)args[0]).drawLine(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 14: // Graphics.draw_rect(x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).drawRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 15: // Graphics.fill_rect(x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).fillRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 16: // Graphics.draw_roundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).drawRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 17: // Graphics.fill_roundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).fillRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 18: // Graphics.draw_arc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).drawArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 19: // Graphics.fill_arc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).fillArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 20: // Graphics.fill_triangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int)
				((Graphics)args[0]).fillTriangle(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 21: // Graphics.draw_string(str: String, x: Int, y: Int)
				((Graphics)args[0]).drawString((String)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 22: // Graphics.draw_image(im: Image, x: Int, y: Int)
				((Graphics)args[0]).drawImage((Image)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 23: // Graphics.copy_area(xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int)
				((Graphics)args[0]).copyArea(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), 0);
				return null;
			case 24: // new_canvas(full: Bool): Canvas
				return new UICanvas(bval(args[0]));
			case 25: { // Canvas.graphics(): Graphics
				return ((UICanvas)args[0]).getGraphics();
			}
			case 26: { // Canvas.read_key(): Int
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
			case 28: // Screen.get_height(): Int
				return Ival(((Displayable)args[0]).getHeight());
			case 29: // Screen.get_width(): Int
				return Ival(((Displayable)args[0]).getWidth());
			case 30: // Screen.get_title(): String
				return ((Displayable)args[0]).getTitle();
			case 31: { // Screen.set_title(title: String)
				((Displayable)args[0]).setTitle((String)args[1]);
				return null;
			}
			case 32: // Screen.is_shown(): Bool
				return Ival(((Displayable)args[0]).isShown());
			case 33: // Canvas.refresh()
				((UICanvas)args[0]).repaint();
				return null;
			case 34: // ui_read_event(): UIEvent
				return UIServer.readEvent(c, false);
			case 35: // ui_wait_event(): UIEvent
				return UIServer.readEvent(c, true);
			case 36: // new_editbox(mode: Int): EditBox
				return new TextBox(null, null, Short.MAX_VALUE, ival(args[0]));
			case 37: // EditBox.get_text(): String
				return ((TextBox)args[0]).getString();
			case 38: // EditBox.set_text(text: String)
				((TextBox)args[0]).setString((String)args[1]);
				return null;
			case 39: { // new_listbox(strings: [String], images: [Image], select: Menu): ListBox
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
			case 40: // ListBox.get_index(): Int
				return Ival(((List)args[0]).getSelectedIndex());
			case 41: // ListBox.set_index(index: Int)
				((List)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 42: // ui_get_screen(): Screen
				return UIServer.getScreen(c);
			case 43: // new_menu(text: String, priority: Int): Menu
				return new Command((String)args[0], Command.SCREEN, ival(args[1]));
			case 44: // Menu.get_text(): String
				return ((Command)args[0]).getLabel();
			case 45: // Menu.get_priority(): Int
				return Ival(((Command)args[0]).getPriority());
			case 46: // Screen.add_menu(menu: Menu)
				((Displayable)args[0]).addCommand((Command)args[1]);
				return null;
			case 47: // Screen.remove_menu(menu: Menu)
				((Displayable)args[0]).removeCommand((Command)args[1]);
				return null;
			case 48: // new_form(): Form
				return new Form(null);
			case 49: // Form.add(item: Item)
				((Form)args[0]).append((Item)args[1]);
				return null;
			case 50: // Form.get(at: Int): Item
				return ((Form)args[0]).get(ival(args[1]));
			case 51: // Form.set(at: Int, item: Item)
				((Form)args[0]).set(ival(args[1]), (Item)args[2]);
				return null;
			case 52: // Form.insert(at: Int, item: Item)
				((Form)args[0]).insert(ival(args[1]), (Item)args[2]);
				return null;
			case 53: // Form.remove(at: Int)
				((Form)args[0]).delete(ival(args[1]));
				return null;
			case 54: // Form.size(): Int
				return Ival(((Form)args[0]).size());
			case 55: // Form.clear()
				((Form)args[0]).deleteAll();
				return null;
			case 56: // Item.get_label(): String
				return ((Item)args[0]).getLabel();
			case 57: // Item.set_label(label: String)
				((Item)args[0]).setLabel((String)args[1]);
				return null;
			case 58: // new_textitem(label: String, text: String): TextItem
				return new StringItem((String)args[0], String.valueOf(args[1])+'\n');
			case 59: // TextItem.get_text(): String
				return ((StringItem)args[0]).getText();
			case 60: // TextItem.set_text(text: String)
				((StringItem)args[0]).setText(String.valueOf(args[1])+'\n');
				return null;
			case 61: // TextItem.get_font(): Int
				return Ival(font2int(((StringItem)args[0]).getFont()));
			case 62: // TextItem.set_font(font: Int)
				((StringItem)args[0]).setFont(int2font(ival(args[1])));
				return null;
			case 63: // new_imageitem(label: String, img: Image): ImageItem
				return new ImageItem((String)args[0], (Image)args[1], Item.LAYOUT_NEWLINE_AFTER, null);
			case 64: // ImageItem.get_image(): Image
				return ((ImageItem)args[0]).getImage();
			case 65: // ImageItem.set_image(img: Image)
				((ImageItem)args[0]).setImage((Image)args[1]);
				return null;
			case 66: // new_edititem(label: String, text: String, mode: Int, size: Int): EditItem
				return new TextField((String)args[0], (String)args[1], ival(args[3]), ival(args[2]));
			case 67: // EditItem.get_text(): String
				return ((TextField)args[0]).getString();
			case 68: // EditItem.set_text(text: String)
				((TextField)args[0]).setString((String)args[0]);
				return null;
			case 69: // new_gaugeitem(label: String, max: Int, init: Int): GaugeItem
				return new Gauge((String)args[0], true, ival(args[1]), ival(args[2]));
			case 70: // GaugeItem.get_value(): Int
				return Ival(((Gauge)args[0]).getValue());
			case 71: // GaugeItem.set_value(val: Int)
				((Gauge)args[0]).setValue(ival(args[1]));
				return null;
			case 72: // GaugeItem.get_maxvalue(): Int
				return Ival(((Gauge)args[0]).getMaxValue());
			case 73: // GaugeItem.set_maxvalue(val: Int)
				((Gauge)args[0]).setMaxValue(ival(args[1]));
				return null;
			case 74: // new_dateitem(label: String, mode: Int): DateItem
				return new DateField((String)args[0], ival(args[1]));
			case 75: { // DateItem.get_date(item: Item): Long
				Date date = ((DateField)args[0]).getDate();
				return (date == null) ? null : Lval(date.getTime());
			}
			case 76: // DateItem.set_date(date: Long)
				((DateField)args[0]).setDate(new Date(lval(args[1])));
				return null;
			case 77: { // new_checkitem(label: String, text: String, checked: Bool): CheckItem
				ChoiceGroup check = new ChoiceGroup((String)args[0], Choice.MULTIPLE);
				check.append((String)args[1], null);
				check.setSelectedIndex(0, bval(args[2]));
				return check;
			}
			case 78: { // CheckItem.get_checked(): Bool
				boolean[] checked = new boolean[1];
				((ChoiceGroup)args[0]).getSelectedFlags(checked);
				return Ival(checked[0]);
			}
			case 79: // CheckItem.set_checked(checked: Bool)
				((ChoiceGroup)args[0]).setSelectedIndex(0, bval(args[1]));
				return null;
			case 80: // CheckItem.get_text(): String
				return ((ChoiceGroup)args[0]).getString(0);
			case 81: // CheckItem.set_text(text: String)
				((ChoiceGroup)args[0]).set(0, (String)args[1], null);
				return null;
			case 82: { // new_radioitem(label: String, strings: [String]): RadioItem
				Object[] array = (Object[])args[1];
				String[] strings = new String[array.length];
				System.arraycopy(array, 0, strings, 0, array.length);
				return new ChoiceGroup((String)args[0], Choice.EXCLUSIVE, strings, null);
			}
			case 83: // RadioItem.get_index(): Int
				return Ival(((ChoiceGroup)args[0]).getSelectedIndex());
			case 84: // RadioItem.set_index(index: String)
				((ChoiceGroup)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 85: { // new_textbox(text: String): TextBox
				Form box = new Form(null);
				box.append(new StringItem(null, (String)args[0]));
				return box;
			}
			case 86: // TextBox.get_text(): String
				return ((StringItem)((Form)args[0]).get(0)).getText();
			case 87: // TextBox.set_text(text: String)
				((StringItem)((Form)args[0]).get(0)).setText((String)args[1]);
				return null;
			case 88: // font_baseline(font: Int): Int
				return Ival(int2font(ival(args[0])).getBaselinePosition());
			case 89: // Graphics.get_stroke(): Int
				return Ival(((Graphics)args[0]).getStrokeStyle());
			case 90: // Graphics.set_stroke(stroke: Int)
				((Graphics)args[0]).setStrokeStyle(ival(args[1]));
				return null;
			case 91: // Graphics.draw_region(im: Image, xsrc: Int, ysrc: Int, w: Int, h: Int, trans: Int, xdst: Int, ydst: Int)
				((Graphics)args[0]).drawRegion((Image)args[1], ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), ival(args[8]), 0);
				return null;
			case 92: { // Graphics.draw_rgb(rgb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int, alpha: Bool)
				Object[] rgbInts = (Object[])args[1];
				int[] rgb = new int[rgbInts.length];
				for (int i=rgb.length-1; i>=0; i--) {
					rgb[i] = ival(rgbInts[i]);
				}
				((Graphics)args[0]).drawRGB(rgb, ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), bval(args[8]));
				return null;
			}
			case 93: // Canvas.action_code(key: Int): Int
				return Ival(((UICanvas)args[0]).getGameAction(ival(args[1])));
			case 94: // Canvas.has_ptr_events(): Bool
				return Ival(((UICanvas)args[0]).hasPointerEvents());
			case 95: // Canvas.has_ptrdrag_event(): Bool
				return Ival(((UICanvas)args[0]).hasPointerMotionEvents());
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
	
	private static String titleFor(Context c) {
		Object title = String.valueOf(c.get("ui.title"));
		return (title != null) ? title.toString() : c.getName();
	}

	public String soname() {
		return "libui.1.so";
	}
}

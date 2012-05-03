/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;

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
				Object[] event = (Object[])UIServer.readEvent(c);
				if (event != null && event[0] == args[0] && event[1] == UIServer.EVENT_KEY_PRESS) {
					return event[2];
				} else {
					return Function.ZERO;
				}
			}
			case 27: { // ui_set_screen(scr: Screen)
				if (args[0] != null) {
					UIServer.mapContext(c, (Displayable)args[0]);
				} else {
					UIServer.unmapContext(c);
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
				return UIServer.readEvent(c);
			case 35: // new_textbox(mode: Int): Screen
				return new TextBox(null, null, Short.MAX_VALUE, ival(args[0]));
			case 36: // textbox_get_text(box: Screen): String
				return ((TextBox)args[0]).getString();
			case 37: // textbox_set_text(box: Screen, text: String)
				((TextBox)args[0]).setString((String)args[1]);
				return null;
			case 38: { // new_listbox(strings: Array, images: Array): Screen
				Object[] str = (Object[])args[0];
				String[] strings = new String[str.length];
				System.arraycopy(str, 0, strings, 0, str.length);
				Object[] img = (Object[])args[1];
				Image[] images = null;
				if (img != null) {
					images = new Image[img.length];
					System.arraycopy(img, 0, images, 0, img.length);
				}
				return new List(null, Choice.IMPLICIT, strings, images);
			}
			case 39: // listbox_get_index(list: Screen): Int
				return Ival(((List)args[0]).getSelectedIndex());
			case 40: // listbox_set_index(list: Screen, index: Int)
				((List)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 41: // listbox_default_menu(): Menu
				return List.SELECT_COMMAND;
			case 42: // new_menu(text: String, priority: Int): Menu
				return new Command((String)args[0], Command.SCREEN, ival(args[1]));
			case 43: // menu_get_text(menu: Menu): String
				return ((Command)args[0]).getLabel();
			case 44: // menu_get_priority(menu: Menu): Int
				return Ival(((Command)args[0]).getPriority());
			case 45: // screen_add_menu(scr: Screen, menu: Menu)
				((Displayable)args[0]).addCommand((Command)args[1]);
				return null;
			case 46: // screen_remove_menu(scr: Screen, menu: Menu)
				((Displayable)args[0]).removeCommand((Command)args[1]);
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
}

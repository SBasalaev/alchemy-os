/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.libs.ui.MsgBox;
import alchemy.libs.ui.UICanvas;
import alchemy.platform.Platform;
import alchemy.platform.UI;
import alchemy.system.Cache;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.system.UIServer;
import alchemy.types.Int32;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;

/**
 * User interface library for Alchemy OS.
 * This is j2me implementation.
 *
 * Classes are mapped as follows:
 * <table border="1">
 * <tr>
 * <th>Ether type</th>
 * <th>Java class</th>
 * </tr>
 * <tr>
 * <td><code>Graphics</code></td>
 * <td><code>javax.microedition.lcdui.Graphics</code></td>
 * </tr>
 * <tr>
 * <td><code>Image</code></td>
 * <td><code>javax.microedition.lcdui.Image</code></td>
 * </tr>
 * <tr>
 * <td><code>Menu</code></td>
 * <td><code>javax.microedition.lcdui.Command</code></td>
 * </tr>
 * <tr>
 * <td><code>Screen</code></td>
 * <td><code>javax.microedition.lcdui.Displayable</code></td>
 * </tr>
 * <tr>
 * <td><code>Canvas</code></td>
 * <td><code>alchemy.libs.ui.UICanvas</code></td>
 * </tr>
 * </table>
 *
 * @author Sergey Basalaev
 */
public final class LibUI2 extends NativeLibrary {

	public LibUI2() throws IOException {
		load("/symbols/ui2");
		name = "libui.2.so";
	}

	private final UI ui = Platform.getPlatform().getUI();

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			/* == Header: font.eh == */
			case 0: // stringWidth(font: Int, str: String): Int
				return Ival(int2font((Int32)args[0]).stringWidth((String)args[1]));
			case 1: // fontHeight(font: Int): Int
				return Ival(int2font((Int32)args[0]).getHeight());
			case 2: // fontBaseline(font: Int): Int
				return Ival(int2font((Int32)args[0]).getBaselinePosition());

			/* == Header: graphics.eh == */
			case 3: // Graphics.getColor(): Int
				return Ival(((Graphics)args[0]).getColor());
			case 4: // Graphics.setColor(rgb: Int)
				((Graphics)args[0]).setColor(ival(args[1]));
				return null;
			case 5: // Graphics.getStroke(): Int
				return Ival(((Graphics)args[0]).getStrokeStyle());
			case 6: // Graphics.setStroke(stroke: Int)
				((Graphics)args[0]).setStrokeStyle(ival(args[1]));
				return null;
			case 7: // Graphics.getFont(): Int
				return font2int(((Graphics)args[0]).getFont());
			case 8: // Graphics.setFont(font: Int)
				((Graphics)args[0]).setFont(int2font((Int32)args[1]));
				return null;
			case 9: // Graphics.drawLine(x1: Int, y1: Int, x2: Int, y2: Int)
				((Graphics)args[0]).drawLine(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 10: // Graphics.drawRect(x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).drawRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 11: // Graphics.fillRect(x: Int, y: Int, w: Int, h: Int)
				((Graphics)args[0]).fillRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 12: // Graphics.drawRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).drawRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 13: // Graphics.fillRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics)args[0]).fillRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 14: // Graphics.drawArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).drawArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 15: // Graphics.fillArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics)args[0]).fillArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 16: // Graphics.fillTriangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int)
				((Graphics)args[0]).fillTriangle(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 17: // Graphics.drawString(str: String, x: Int, y: Int)
				((Graphics)args[0]).drawString((String)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 18: // Graphics.drawImage(im: Image, x: Int, y: Int)
				((Graphics)args[0]).drawImage((Image)args[1], ival(args[2]), ival(args[3]), 0);
				return null;
			case 19: // Graphics.drawRGB(rgb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int, alpha: Bool)
				((Graphics)args[0]).drawRGB((int[])args[1], ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), bval(args[8]));
				return null;
			case 20: // Graphics.copyArea(xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int)
				((Graphics)args[0]).copyArea(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), 0);
				return null;
			case 21: // Graphics.drawRegion(im: Image, xsrc: Int, ysrc: Int, w: Int, h: Int, trans: Int, xdst: Int, ydst: Int)
				((Graphics)args[0]).drawRegion((Image)args[1], ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), ival(args[8]), 0);
				return null;

			/* == Header: image.eh == */
			case 22: // Image.new(w: Int, h: Int)
				return Image.createImage(ival(args[0]), ival(args[1]));
			case 23: // Image.graphics(): Graphics
				return ((Image)args[0]).getGraphics();
			case 24: // imageFromARGB(argb: [Int], w: Int, h: Int, alpha: Bool): Image
				return Image.createRGBImage((int[])args[0], ival(args[1]), ival(args[2]), bval(args[3]));
			case 25: { // imageFromFile(file: String): Image
				String filename = p.toFile((String)args[0]);
				long tstamp = Filesystem.lastModified(filename);
				Image img = (Image) Cache.get(filename, tstamp);
				if (img == null) {
					ConnectionInputStream in = new ConnectionInputStream(Filesystem.read(filename));
					p.addConnection(in);
					img = Image.createImage(in);
					in.close();
					Cache.put(filename, tstamp, img);
					p.removeConnection(in);
				}
				return img;
			}
			case 26: // imageFromStream(in: IStream): Image
				return Image.createImage((InputStream)args[0]);
			case 27: { // imageFromData(data: [Byte], ofs: Int = 0, len: Int = -1): Image
				final byte[] buf = (byte[])args[0];
				int ofs = ival(args[1]);
				int len = ival(args[2]);
				if (len < 0) len = buf.length - ofs;
				return Image.createImage(buf, ofs, len);
			}
			case 28: // imageFromImage(im: Image, x: Int, y: Int, w: Int, h: Int): Image
				return Image.createImage((Image)args[0], ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), 0);
			case 29: // Image.getARGB(argb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int)
				((Image)args[0]).getRGB((int[])args[1], ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]));
				return null;
			case 30: // Image.getWidth(): Int
				return Ival(((Image)args[0]).getWidth());
			case 31: // Image.getHeight(): Int
				return Ival(((Image)args[0]).getHeight());
			case 32: // Image.isMutable(): Bool
				return Ival(((Image)args[0]).isMutable());

			/*  == Header: ui.eh == */
			case 33: // Menu.new(text: String, priority: Int, mtype: Int = MT_SCREEN): Menu
				return new Command((String)args[0], ival(args[1]), ival(args[2]));
			case 34: // Menu.getText(): String
				return ((Command)args[0]).getLabel();
			case 35: // Menu.getPriority(): Int
				return Ival(((Command)args[0]).getPriority());
			case 36: // Menu.getType(): Int
				return Ival(((Command)args[0]).getCommandType());
			case 37: // Screen.isShown(): Bool
				return Ival(((Displayable)args[0]).isShown());
			case 38: // Screen.getHeight(): Int
				return Ival(((Displayable)args[0]).getHeight());
			case 39: // Screen.getWidth(): Int
				return Ival(((Displayable)args[0]).getWidth());
			case 40: // Screen.getTitle(): String
				return ((Displayable)args[0]).getTitle();
			case 41: { // Screen.setTitle(title: String)
				Displayable screen = (Displayable) args[0];
				String title = (String) args[1];
				screen.setTitle(title);
				ui.screenTitleChanged(screen, title);
				return null;
			}
			case 42: { // Screen.addMenu(menu: Menu)
				Displayable screen = (Displayable) args[0];
				Command menu = (Command) args[1];
				screen.addCommand(menu);
				ui.screenMenuAdded(screen, menu);
				return null;
			}
			case 43: { // Screen.removeMenu(menu: Menu)
				Displayable screen = (Displayable) args[0];
				Command menu = (Command) args[1];
				screen.removeCommand(menu);
				ui.screenMenuRemoved(screen, menu);
				return null;
			}
			case 44: // uiReadEvent(): UIEvent
				return UIServer.readEvent(p, false);
			case 45: // uiWaitEvent(): UIEvent
				return UIServer.readEvent(p, true);
			case 46: // uiVibrate(millis: Int): Bool
				return Ival(ui.vibrate(ival(args[0])));
			case 47: // uiFlash(millis: Int): Bool
				return Ival(ui.flash(ival(args[0])));
			case 48: // uiGetScreen(): Screen
				return UIServer.getScreen(p);
			case 49: // uiSetScreen(scr: Screen)
				UIServer.setScreen(p, args[0]);
				return null;
			case 50: // uiSetDefaultTitle(title: String)
				p.setGlobal(null, "ui.title", (String)args[0]);
				return null;
			case 51: { // uiSetIcon(icon: Image)
				p.setGlobal(null, "ui.icon", (Image)args[0]);
				UIServer.displayCurrent();
				return null;
			}

			/* == Header: canvas.eh == */
			case 52: // Canvas.new(full: Bool = false): Canvas
				return new UICanvas(bval(args[0]));
			case 53: // Canvas.graphics(): Graphics
				return ((UICanvas)args[0]).getGraphics();
			case 54: // Canvas.repaint(x: Int, y: Int, w: Int, h: Int)
				((UICanvas)args[0]).repaint(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 55: // Canvas.refresh()
				((UICanvas)args[0]).repaint();
				return null;
			case 56: // Canvas.actionCode(key: Int): Int
				return Ival(((UICanvas)args[0]).getGameAction(ival(args[1])));
			case 57: // Canvas.keyName(key: Int): String
				return ((UICanvas)args[0]).getKeyName(ival(args[1]));
			case 58: // Canvas.hasPtrEvents(): Bool
				return Ival(((UICanvas)args[0]).hasPointerEvents());
			case 59: // Canvas.hasPtrDragEvent(): Bool
				return Ival(((UICanvas)args[0]).hasPointerMotionEvents());
			case 60: // Canvas.hasHoldEvent(): Bool
				return Ival(((UICanvas)args[0]).hasRepeatEvents());

			/* == Header: msgbox.eh == */
			case 61: // MsgBox.new(text: String = "", image: Image = null)
				return new MsgBox((String)p.getGlobal(null, "ui.title", p.getName()), (String)args[0], (Image)args[1]);
			case 62: // MsgBox.getText(): String
				return ((MsgBox)args[0]).getString();
			case 63: // MsgBox.setText(text: String)
				((MsgBox)args[0]).setString((String)args[1]);
				return null;
			case 64: // MsgBox.getImage(): Image
				return ((MsgBox)args[0]).getImage();
			case 65: // MsgBox.setImage(img: Image)
				((MsgBox)args[0]).setImage((Image)args[1]);
				return null;
			case 66: // MsgBox.getFont(): Int
				return font2int(((MsgBox)args[0]).getFont());
			case 67: // MsgBox.setFont(font: Int)
				((MsgBox)args[0]).setFont(int2font((Int32)args[1]));
				return null;

			/* == Header: editbox.eh == */
			case 68: // EditBox.new(mode: Int = EDIT_ANY): EditBox
				return new TextBox((String)p.getGlobal(null, "ui.title", p.getName()), (String)args[1], Short.MAX_VALUE, ival(args[0]));
			case 69: // EditBox.getText(): String
				return ((TextBox)args[0]).getString();
			case 70: // EditBox.setText(text: String)
				((TextBox)args[0]).setString((String)args[1]);
				return null;
			case 71: // EditBox.getMaxSize(): Int
				return Ival(((TextBox)args[0]).getMaxSize());
			case 72: // EditBox.setMaxSize(size: Int)
				((TextBox)args[0]).setMaxSize(ival(args[1]));
				return null;
			case 73: // EditBox.getSize(): Int
				return Ival(((TextBox)args[0]).size());
			case 74: // EditBox.getCaret(): Int
				return Ival(((TextBox)args[0]).getCaretPosition());

			/* == Header: listbox.eh == */
			case 75: { // ListBox.new(strings: [String], images: [Image], select: Menu): ListBox
				Object[] objStrings = (Object[])args[0];
				String[] strings = null;
				if (objStrings != null) {
					strings = new String[objStrings.length];
					System.arraycopy(objStrings, 0, strings, 0, objStrings.length);
				}
				Object[] objImages = (Object[])args[1];
				Image[] images = null;
				if (objImages != null) {
					images = new Image[objImages.length];
					System.arraycopy(objImages, 0, images, 0, objImages.length);
				}
				List list = new List((String)p.getGlobal(null, "ui.title", p.getName()), List.IMPLICIT, strings, images);
				list.setSelectCommand((Command)args[2]);
				return list;
			}
			case 76: // ListBox.getIndex(): Int
				return Ival(((List)args[0]).getSelectedIndex());
			case 77: // ListBox.setIndex(index: Int)
				((List)args[0]).setSelectedIndex(ival(args[1]), true);
				return null;
			case 78: // ListBox.add(str: String, img: Image = null)
				((List)args[0]).append((String)args[1], (Image)args[2]);
				return null;
			case 79: // ListBox.insert(at: Int, str: String, img: Image = null)
				((List)args[0]).insert(ival(args[1]), (String)args[2], (Image)args[3]);
				return null;
			case 80: // ListBox.set(at: Int, str: String, img: Image = null)
				((List)args[0]).set(ival(args[1]), (String)args[2], (Image)args[3]);
				return null;
			case 81: // ListBox.delete(at: Int)
				((List)args[0]).delete(ival(args[1]));
				return null;
			case 82: // ListBox.getString(at: Int): String
				return ((List)args[0]).getString(ival(args[1]));
			case 83: // ListBox.getImage(at: Int): Image
				return ((List)args[0]).getImage(ival(args[1]));
			case 84: // ListBox.getFont(at: Int): Font
				return font2int(((List)args[0]).getFont(ival(args[1])));
			case 85: // ListBox.setFont(at: Int, font: Int)
				((List)args[0]).setFont(ival(args[1]), int2font((Int32)args[2]));
				return null;
			case 86: // ListBox.clear()
				((List)args[0]).deleteAll();
				return null;
			case 87: // ListBox.len(): Int
				return Ival(((List)args[0]).size());

			default:
				return null;
		}
	}

	private static Int32 font2int(Font f) {
		return Int32.toInt32(f.getFace() | f.getSize() | f.getStyle());
	}
	
	private static Font int2font(Int32 mask) {
		return Font.getFont(mask.value & 0x60, mask.value & 0x7, mask.value & 0x18);
	}
}

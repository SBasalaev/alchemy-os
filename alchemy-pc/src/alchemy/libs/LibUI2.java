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
package alchemy.libs;

import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.io.IO;
import alchemy.libs.ui.FontManager;
import alchemy.libs.ui.UiCanvas;
import alchemy.libs.ui.UiEditBox;
import alchemy.libs.ui.UiImage;
import alchemy.libs.ui.UiListBox;
import alchemy.libs.ui.UiMenu;
import alchemy.libs.ui.UiMsgBox;
import alchemy.libs.ui.UiScreen;
import alchemy.platform.Platform;
import alchemy.platform.UI;
import alchemy.system.Cache;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.system.UIServer;
import alchemy.types.Int32;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * User interface library, pc implementation.
 *
 * Ether classes are mapped as follows:
 *
 * <table border="1">
 * <tr>
 * <th>Ether</th>
 * <th>Java</th>
 * </tr>
 * <tr>
 * <td>Graphics</td>
 * <td>java.awt.Graphics2D</td>
 * </tr>
 * <tr>
 * <td>Image</td>
 * <td>alchemy.libs.ui.ImageImpl</td>
 * </tr>
 * </table>
 *
 * @author Sergey Basalaev
 */
public final class LibUI2 extends NativeLibrary {

	// transform constants
	private static final int TRANS_NONE = 0;
	private static final int TRANS_MIRROR_ROT180 = 1;
	private static final int TRANS_MIRROR = 2;
	private static final int TRANS_ROT180 = 3;
	private static final int TRANS_MIRROR_ROT270 = 4;
	private static final int TRANS_ROT90 = 5;
	private static final int TRANS_ROT270 = 6;
	private static final int TRANS_MIRROR_ROT90 = 7;

	// stroke styles
	public static final Stroke SOLID = new BasicStroke();
	public static final Stroke DOTTED = new BasicStroke(
			1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
			10.0f, new float[] { 0.0f, 1.0f }, 0.0f
	);

	public LibUI2() throws IOException {
		load("/symbols/ui2");
		name = "libui.2.so";
	}

	private final UI ui = Platform.getPlatform().getUI();

	private HashMap<Int32, Color> colorCache = new HashMap<Int32, Color>();

	private Color color(Int32 rgb) {
		Color c = colorCache.get(rgb);
		if (c == null) {
			c = new Color(rgb.value);
			colorCache.put(rgb, c);
		}
		return c;
	}

	private void drawRGB(Graphics2D g, int[] data, int offset, int scanlen, int xofs, int yofs, int w, int h, boolean alpha) {
		for (int x = 0; x < w; x++)
		for (int y = 0; y < h; y++) {
			int rgb = data[offset + y*scanlen + x];
			if (!alpha) rgb |= 0xFF000000;
			g.setColor(color(Ival(rgb)));
			g.fillRect(x + xofs, y + yofs, 1, 1);
		}
	}

	@Override
	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			/* == Header: font.eh == */
			case 0: // stringWidth(font: Int, str: String): Int
				return Ival(FontManager.getFontMetrics(ival(args[0])).stringWidth((String)args[1]));
			case 1: // fontHeight(font: Int): Int
				return Ival(FontManager.getFontMetrics(ival(args[0])).getHeight());
			case 2: // fontBaseline(font: Int): Int
				return Ival(FontManager.getFontMetrics(ival(args[0])).getAscent());

			/* == Header: graphics.eh == */
			case 3: // Graphics.getColor(): Int
				return Ival(((Graphics2D)args[0]).getColor().getRGB());
			case 4: // Graphics.setColor(rgb: Int)
				((Graphics2D)args[0]).setColor(color((Int32)args[1]));
				return null;
			case 5: // Graphics.getStroke(): Int
				return Ival(((Graphics2D)args[0]).getStroke() == DOTTED);
			case 6: // Graphics.setStroke(stroke: Int)
				((Graphics2D)args[0]).setStroke(bval(args[1]) ? DOTTED : SOLID);
				return null;
			case 7: // Graphics.getFont(): Int
				return FontManager.getFontMask(((Graphics2D)args[0]).getFont());
			case 8: // Graphics.setFont(font: Int)
				((Graphics2D)args[0]).setFont(FontManager.getFont(ival(args[1])));
				return null;
			case 9: // Graphics.drawLine(x1: Int, y1: Int, x2: Int, y2: Int)
				((Graphics2D)args[0]).drawLine(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 10: // Graphics.drawRect(x: Int, y: Int, w: Int, h: Int)
				((Graphics2D)args[0]).drawRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 11: // Graphics.fillRect(x: Int, y: Int, w: Int, h: Int)
				((Graphics2D)args[0]).fillRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 12: // Graphics.drawRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics2D)args[0]).drawRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 13: // Graphics.fillRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
				((Graphics2D)args[0]).fillRoundRect(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 14: // Graphics.drawArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics2D)args[0]).drawArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 15: // Graphics.fillArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
				((Graphics2D)args[0]).fillArc(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]));
				return null;
			case 16: { // Graphics.fillTriangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int)
				int[] xpoints = { ival(args[1]), ival(args[3]), ival(args[5]) };
				int[] ypoints = { ival(args[2]), ival(args[4]), ival(args[6]) };
				((Graphics2D)args[0]).fillPolygon(xpoints, ypoints, 3);
				return null;
			}
			case 17: { // Graphics.drawString(str: String, x: Int, y: Int)
				Graphics2D g = (Graphics2D) args[0];
				int x = ival(args[2]);
				int y = ival(args[3]) + g.getFontMetrics().getAscent();
				g.drawString((String)args[1], x, y);
				return null;
			}
			case 18: // Graphics.drawImage(im: Image, x: Int, y: Int)
				((Graphics2D)args[0]).drawImage(((UiImage)args[1]).image, ival(args[2]), ival(args[3]), null);
				return null;
			case 19: // Graphics.drawRGB(rgb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int, alpha: Bool)
				drawRGB(((Graphics2D)args[0]), (int[])args[1], ival(args[2]), ival(args[3]), ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), bval(args[8]));
				return null;
			case 20: { // Graphics.copyArea(xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int)
				int x = ival(args[1]);
				int y = ival(args[2]);
				int dx = x - ival(args[5]);
				int dy = x - ival(args[6]);
				((Graphics2D)args[0]).copyArea(x, y, ival(args[3]), ival(args[4]), dx, dy);
				return null;
			}
			case 21: { // Graphics.drawRegion(im: Image, xsrc: Int, ysrc: Int, w: Int, h: Int, trans: Int, xdst: Int, ydst: Int)
				// based on Microemu implementation
				// author: Andres Navarro
				// license: LGPL 2.1+

				// read arguments
				Graphics2D g = (Graphics2D) args[0];
				Image img = ((UiImage)args[1]).image;
				int x_src = ival(args[2]);
				int y_src = ival(args[3]);
				int width = ival(args[4]);
				int height = ival(args[5]);
				int transform = ival(args[6]);
				int x_dst = ival(args[7]);
				int y_dst = ival(args[8]);

				// prepare transformation
				AffineTransform t = new AffineTransform();
				int dW = width;
				int dH = height;
		        switch (transform) {
					case TRANS_NONE: {
						break;
					}
					case TRANS_ROT90: {
						t.translate((double) height, 0);
					    t.rotate(Math.PI / 2);
					    dW = height;
					    dH = width;
						break;
					}
					case TRANS_ROT180: {
						t.translate(width, height);
				        t.rotate(Math.PI);
						break;
					}
					case TRANS_ROT270: {
						t.translate(0, width);
						t.rotate(Math.PI * 3 / 2);
						dW = height;
						dH = width;
						break;
				    }
					case TRANS_MIRROR: {
						t.translate(width, 0);
						t.scale(-1, 1);
						break;
					}
					case TRANS_MIRROR_ROT90: {
						t.translate((double) height, 0);
						t.rotate(Math.PI / 2);
						t.translate((double) width, 0);
						t.scale(-1, 1);
						dW = height;
						dH = width;
						break;
					}
					case TRANS_MIRROR_ROT180: {
						t.translate(width, 0);
						t.scale(-1, 1);
						t.translate(width, height);
						t.rotate(Math.PI);
						break;
					}
					case TRANS_MIRROR_ROT270: {
						t.rotate(Math.PI * 3 / 2);
						t.scale(-1, 1);
						dW = height;
						dH = width;
						break;
					}
					default:
						throw new IllegalArgumentException("Bad transform");
				}

				AffineTransform savedT = g.getTransform();
				g.translate(x_dst, y_dst);
				g.transform(t);
				g.drawImage(img, 0, 0, width, height, x_src, y_src, x_src + width, y_src + height, null);

				// return to saved
				g.setTransform(savedT);
				return null;
			}

			/* == Header: image.eh == */
			case 22: // Image.new(w: Int, h: Int)
				return new UiImage(ival(args[0]), ival(args[1]));
			case 23: // Image.graphics(): Graphics
				return ((UiImage)args[0]).getGraphics();
			case 24: { // imageFromARGB(argb: [Int], w: Int, h: Int, alpha: Bool): Image
				int[] data = (int[]) args[0];
				int w = ival(args[1]);
				int h = ival(args[2]);
				boolean alpha = bval(args[3]);
				BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				drawRGB(img.createGraphics(), data, 0, w, 0, 0, w, h, alpha);
				return new UiImage(img);
			}
			case 25: { // imageFromFile(file: String): Image
				String filename = p.toFile((String)args[0]);
				long tstamp = Filesystem.lastModified(filename);
				UiImage img = (UiImage) Cache.get(filename, tstamp);
				if (img == null) {
					String url = Filesystem.getNativeURL(filename);
					if (url != null) {
						img = new UiImage(Toolkit.getDefaultToolkit().createImage(new URL(url)));
					} else {
						ConnectionInputStream in = new ConnectionInputStream(Filesystem.read(filename));
						p.addConnection(in);
						byte[] buf = IO.readFully(in);
						in.close();
						p.removeConnection(in);
						img = new UiImage(Toolkit.getDefaultToolkit().createImage(buf));
					}
					Cache.put(filename, tstamp, img);
				}
				return img;
			}
			case 26: { // imageFromStream(input: IStream): Image
				byte[] buf = IO.readFully((InputStream) args[0]);
				return new UiImage(Toolkit.getDefaultToolkit().createImage(buf));
			}
			case 27: { // imageFromData(data: [Byte], ofs: Int = 0, len: Int = -1): Image
				final byte[] buf = (byte[])args[0];
				int ofs = ival(args[1]);
				int len = ival(args[2]);
				if (len < 0) len = buf.length - ofs;
				return new UiImage(Toolkit.getDefaultToolkit().createImage(buf, ofs, len));
			}
			case 28: { // imageFromImage(im: Image, x: Int, y: Int, w: Int, h: Int): Image
				UiImage img = (UiImage) args[0];
				int x = ival(args[1]);
				int y = ival(args[2]);
				int w = ival(args[3]);
				int h = ival(args[4]);
				BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				newimg.getGraphics().drawImage(img.image, x, y, null);
				return new UiImage(newimg);
			}
			case 29: // Image.getARGB(argb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int)
				new PixelGrabber(((UiImage)args[0]).image, ival(args[4]), ival(args[5]), ival(args[6]), ival(args[7]), (int[])args[1], ival(args[2]), ival(args[3])).grabPixels();	
				return null;
			case 30: // Image.getWidth(): Int
				return Ival(((UiImage)args[0]).image.getWidth(null));
			case 31: // Image.getHeight(): Int
				return Ival(((UiImage)args[0]).image.getHeight(null));
			case 32: // Image.isMutable(): Bool
				return Ival(((UiImage)args[0]).isMutable);

			/*  == Header: ui.eh == */
			case 33: // Menu.new(text: String, priority: Int, mtype: Int = MT_SCREEN): Menu
				return new UiMenu((String)args[0], ival(args[1]), ival(args[2]));
			case 34: // Menu.getLabel(): String
				return ((UiMenu)args[0]).getLabel();
			case 35: // Menu.getPriority(): Int
				return Ival(((UiMenu)args[0]).getPriority());
			case 36: // Menu.getType(): Int
				return Ival(((UiMenu)args[0]).getType());
			case 37: // Screen.isShown(): Bool
				return Ival(args[0] == ui.getCurrentScreen());
			case 38: // Screen.getHeight(): Int
				return Ival(((UiScreen)args[0]).getHeight());
			case 39: // Screen.getWidth(): Int
				return Ival(((UiScreen)args[0]).getWidth());
			case 40: // Screen.getTitle(): String
				return ((UiScreen)args[0]).getTitle();
			case 41: { // Screen.setTitle(title: String)
				UiScreen screen = (UiScreen) args[0];
				String title = (String) args[1];
				screen.setTitle(title);
				ui.screenTitleChanged(screen, title);
				return null;
			}
			case 42: { // Screen.addMenu(menu: Menu)
				UiScreen screen = (UiScreen) args[0];
				UiMenu menu = (UiMenu) args[1];
				screen.addMenu(menu);
				ui.screenMenuAdded(screen, menu);
				return null;
			}
			case 43: { // Screen.removeMenu(menu: Menu)
				UiScreen screen = (UiScreen) args[0];
				UiMenu menu = (UiMenu) args[1];
				screen.removeMenu(menu);
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
				p.setGlobal(null, "ui.icon", (UiImage)args[0]);
				UIServer.displayCurrent();
				return null;
			}

			/* == Header: canvas.eh == */
			case 52: // Canvas.new(full: Bool = false): Canvas
				return new UiCanvas(bval(args[0]));
			case 53: // Canvas.graphics(): Graphics
				return ((UiCanvas)args[0]).getGraphics();
			case 54: // Canvas.repaint(x: Int, y: Int, w: Int, h: Int)
				((UiCanvas)args[0]).repaint(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 55: // Canvas.refresh()
				((UiCanvas)args[0]).refresh();
				return null;
			case 56: // Canvas.actionCode(key: Int): Int
				return Ival(((UiCanvas)args[0]).actionCode(ival(args[1])));
			case 57: // Canvas.keyName(key: Int): String
				return ((UiCanvas)args[0]).keyName(ival(args[1]));
			case 58: // Canvas.hasPtrEvents(): Bool
				return Ival(((UiCanvas)args[0]).hasPtrEvents());
			case 59: // Canvas.hasPtrDragEvent(): Bool
				return Ival(((UiCanvas)args[0]).hasPtrDragEvent());
			case 60: // Canvas.hasHoldEvent(): Bool
				return Ival(((UiCanvas)args[0]).hasHoldEvent());

			/* == Header: msgbox.eh == */
			case 61: // MsgBox.new(title: String, text: String = null, image: Image = null)
				return new UiMsgBox((String)args[0], (String)args[1], (UiImage)args[2]);
			case 62: // MsgBox.getText(): String
				return ((UiMsgBox)args[0]).getText();
			case 63: // MsgBox.setText(text: String)
				((UiMsgBox)args[0]).setText((String)args[1]);
				return null;
			case 64: // MsgBox.getImage(): Image
				return ((UiMsgBox)args[0]).getImage();
			case 65: // MsgBox.setImage(img: Image)
				((UiMsgBox)args[0]).setImage((UiImage)args[1]);
				return null;
			case 66: // MsgBox.getFont(): Int
				return Ival(((UiMsgBox)args[0]).getFont());
			case 67: // MsgBox.setFont(font: Int)
				((UiMsgBox)args[0]).setFont(ival(args[1]));
				return null;

			/* == Header: editbox.eh == */
			case 68: // EditBox.new(title: String, text: String = null): EditBox
				return new UiEditBox((String)args[0], (String)args[1]);
			case 69: // EditBox.getText(): String
				return ((UiEditBox)args[0]).getText();
			case 70: // EditBox.setText(text: String)
				((UiEditBox)args[0]).setText((String)args[1]);
				return null;
			case 71: // EditBox.getMaxSize(): Int
				return Ival(((UiEditBox)args[0]).getMaxSize());
			case 72: // EditBox.setMaxSize(size: Int)
				((UiEditBox)args[0]).setMaxSize(ival(args[1]));
				return null;
			case 73: // EditBox.getSize(): Int
				return Ival(((UiEditBox)args[0]).getSize());
			case 74: // EditBox.getCaret(): Int
				return Ival(((UiEditBox)args[0]).getCaret());

			/* == Header: listbox.eh == */
			case 75: { // ListBox.new(title: String, strings: [String], images: [Image], select: Menu): ListBox
				Object[] objStrings = (Object[])args[1];
				String[] strings = null;
				if (objStrings != null) {
					strings = new String[objStrings.length];
					System.arraycopy(objStrings, 0, strings, 0, objStrings.length);
				}
				Object[] objImages = (Object[])args[2];
				UiImage[] images = null;
				if (objImages != null) {
					images = new UiImage[objImages.length];
					System.arraycopy(objImages, 0, images, 0, objImages.length);
				}
				return new UiListBox((String)args[0], strings, images, (UiMenu)args[3]);
			}
			case 76: // ListBox.getIndex(): Int
				return Ival(((UiListBox)args[0]).getIndex());
			case 77: // ListBox.setIndex(index: Int)
				((UiListBox)args[0]).setIndex(ival(args[1]));
				return null;
			case 78: // ListBox.add(str: String, img: Image = null)
				((UiListBox)args[0]).add((String)args[1], (UiImage)args[2]);
				return null;
			case 79: // ListBox.insert(at: Int, str: String, img: Image = null)
				((UiListBox)args[0]).insert(ival(args[1]), (String)args[2], (UiImage)args[3]);
				return null;
			case 80: // ListBox.set(at: Int, str: String, img: Image = null)
				((UiListBox)args[0]).set(ival(args[1]), (String)args[2], (UiImage)args[3]);
				return null;
			case 81: // ListBox.delete(at: Int)
				((UiListBox)args[0]).delete(ival(args[1]));
				return null;
			case 82: // ListBox.getString(at: Int): String
				return ((UiListBox)args[0]).getString(ival(args[1]));
			case 83: // ListBox.getImage(at: Int): Image
				return ((UiListBox)args[0]).getImage(ival(args[1]));
			case 84: // ListBox.getFont(at: Int): Font
				return FontManager.getFontMask(((UiListBox)args[0]).getFont(ival(args[1])));
			case 85: // ListBox.setFont(at: Int, font: Int)
				((UiListBox)args[0]).setFont(ival(args[1]), FontManager.getFont(ival(args[2])));
				return null;
			case 86: // ListBox.clear()
				((UiListBox)args[0]).clear();
				return null;
			case 87: // ListBox.len(): Int
				return Ival(((UiListBox)args[0]).size());

			default:
				throw new RuntimeException("Not implemented");
		}
	}
}

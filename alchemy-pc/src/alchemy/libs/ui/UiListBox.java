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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * PC implementation of ListBox.
 * DOES NOT WORK
 * @author Sergey Basalaev
 */
public final class UiListBox extends UiScreen {

	private DefaultListModel<JLabel> listModel;
	private JList<JLabel> list;

	private final ArrayList<String> stringParts;
	private final ArrayList<UiImage> imageParts;

	public UiListBox(String title, String[] strings, UiImage[] images, UiMenu select) {
		super(title);
		stringParts = new ArrayList<String>(Arrays.asList(strings));
		imageParts = new ArrayList<UiImage>(Arrays.asList(images));

		listModel = new DefaultListModel<JLabel>();
		list = new JList<JLabel>(listModel);
	}

	public int getIndex() {
		return list.getSelectedIndex();
	}

	public void setIndex(int index) {
		list.setSelectedIndex(index);
	}

	public synchronized void add(String str, UiImage img) {
		insert(listModel.size(), str, img);
	}

	public void insert(int at, String str, UiImage img) {
		stringParts.add(at, str);
		imageParts.add(at, img);
		listModel.add(at, new JLabel(str, new ImageIcon(img.image), JLabel.LEFT));
	}

	public void set(int at, String str, UiImage img) {
		stringParts.set(at, str);
		imageParts.set(at, img);
		listModel.set(at, new JLabel(str, new ImageIcon(img.image), JLabel.LEFT));
	}

	public void delete(int at) {
		stringParts.remove(at);
		imageParts.remove(at);
		listModel.remove(at);
	}

	public String getString(int at) {
		return stringParts.get(at);
	}

	public UiImage getImage(int at) {
		return imageParts.get(at);
	}

	public Font getFont(int at) {
		return null;
	}

	public void setFont(int at, Font font) {
		
	}

	public void clear() {
		stringParts.clear();
		imageParts.clear();
		listModel.clear();
	}

	public int size() {
		return listModel.size();
	}

	@Override public JComponent getWidget() {
		return list;
	}
}

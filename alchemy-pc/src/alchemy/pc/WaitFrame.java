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

package alchemy.pc;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Simple frame to show backround job progress.
 * @author Sergey Basalaev
 */
final class WaitFrame extends JFrame {

	public WaitFrame(String caption) {
		super("Alchemy OS");
		JLabel label = new JLabel(caption);
		JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);
		bar.setIndeterminate(true);
		Box box = Box.createVerticalBox();
		box.add(label);
		box.add(bar);
		add(box);
		pack();
	}
}

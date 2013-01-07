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

package alchemy.fs;

import java.io.IOException;

/**
 * Helper for FSNavigator.
 * Actual implementation is loaded through the native interface
 * to avoid static linking and crashing with NoClassDefFoundError.
 * @author Sergey Basalaev
 */
public interface NavigatorHelper {
	/** Lists roots of the file system. */
	String[] listRoots();
	/** Lists contents of given directory. */
	String[] list(String dir) throws IOException;
	/** Creates new directory in the file system. */
	void mkdir(String dir) throws IOException;
}

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

package alchemy.platform;

import alchemy.util.HashMap;
import java.io.IOException;

/**
 * Current installation configuration.
 * Implementations of this interface load and store
 * installation information in platform-dependent
 * manner. The HashMap returned by getConfig() should
 * be used to read/write configuration and then save()
 * should be called.
 *
 * @author Sergey Basalaev
 */
public interface InstallCfg {
	/** Tests if install information exists. */
	boolean exists();
	/** Returns install configuration to read/write. */
	HashMap getConfig();
	/** Flushes changes to the installation config. */
	void save() throws IOException;
	/** Removes install information. */
	void remove() throws IOException;
}

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

package alchemy.platform;

import java.io.IOException;

/**
 * Information about current installation.
 *
 * @author Sergey Basalaev
 */
public interface InstallInfo {
	/** Tests if install information exists. */
	boolean exists();
	/** Returns name of the driver of the root filesystem. */
	String getFilesystemDriver();
	/** Returns options passed to the driver of the root filesystem. */
	String getFilesystemOptions();
	/** Writes filesystem info in the installation config. */
	void setFilesystem(String driver, String options) throws IOException;
	/** Returns version of the installed system. */
	String getInstalledVersion();
	/** Assigns new version to the installed system. */
	void setInstalledVersion(String version);
	/** Flushes changes to the installation config. */
	void save() throws IOException;
	/** Removes install information. */
	void remove() throws IOException;
}

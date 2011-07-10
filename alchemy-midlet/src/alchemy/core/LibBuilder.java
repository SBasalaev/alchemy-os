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

package alchemy.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * Library builder.
 * 
 * @author Sergey Basalaev
 */
public interface LibBuilder {

	/**
	 * Builds library from the given input stream.
	 * Be aware that the first two bytes are already read
	 * (FIXME see LibraryLoadingMechanism).
	 *
	 * @param c   context in which this library is loaded
	 * @param in  input stream
	 */
	Library build(Context c, InputStream in) throws IOException, InstantiationException;
}

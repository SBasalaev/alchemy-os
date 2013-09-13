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

package alchemy.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Input stream that generates pseudorandom data.
 *
 * @author Sergey Basalaev
 */
public class RandomInputStream extends InputStream {
	private final Random rnd = new Random();

	public RandomInputStream() { }

	public int read() throws IOException {
		return rnd.nextInt(256);
	}
}

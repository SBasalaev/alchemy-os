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

package alchemy.platform.j2me;

import alchemy.midlet.AlchemyMIDlet;
import java.io.IOException;

/**
 * j2me implementation of platform dependent core functions.
 * @author Sergey Basalaev
 */
public final class Core implements alchemy.platform.Core {

	public Core() { }

	public boolean platformRequest(String url) throws IOException {
		return AlchemyMIDlet.instance.platformRequest(url);
	}
}

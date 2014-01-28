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

package alchemy.libs;

import alchemy.core.Process;
import alchemy.core.Int;
import alchemy.nlib.NativeLibrary;
import alchemy.util.IO;
import java.io.IOException;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

/**
 * Serial port library for Alchemy OS.
 * @author Sergey Basalaev
 * @version 1.0
 */
public class LibComm1 extends NativeLibrary {

	public LibComm1() throws IOException {
		load("/libcomm1.symbols");
	}
	
	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: { // list_commports(): [String]
				String ports = System.getProperty("microedition.commports");
				if (ports == null) ports = "";
				return IO.split(ports, ',');
			}
			case 1: { // new_comm(port: String, cfg: CommCfg): Comm
				StringBuffer url = new StringBuffer("comm:");
				url.append((String)args[0]);
				Object[] params = (Object[])args[1];
				if (params[0] != Int.ZERO) url.append(";baudrate=").append(ival(params[0]));
				if (params[1] != null) url.append(";bitsperchar=").append(ival(params[1]));
				if (params[2] != null) url.append(";stopbits=").append(ival(params[2]));
				if (params[3] != null) url.append(";parity=").append((String)params[3]);
				if (params[4] != null) url.append(";blocking=").append(bval(params[4]) ? "on" : "off");
				if (params[5] != null) url.append(";autocts=").append(bval(params[5]) ? "on" : "off");
				if (params[6] != null) url.append(";autorts=").append(bval(params[6]) ? "on" : "off");
				Connection conn = Connector.open(url.toString());
				p.addStream(conn);
				return conn;
			}
			case 2: // Comm.get_baudrate(): Int
				return Ival(((CommConnection)args[0]).getBaudRate());
			case 3: // Comm.set_baudrate(baudrate: Int)
				((CommConnection)args[0]).setBaudRate(ival(args[1]));
				return null;
			default:
				return null;
		}
	}

	public String soname() {
		return "libcomm.1.so";
	}
}

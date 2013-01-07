//#condition DEBUGLOG=="true"
//# 
//# /*
//#  * This file is a part of Alchemy OS project.
//#  *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
//#  *
//#  *  This program is free software: you can redistribute it and/or modify
//#  *  it under the terms of the GNU General Public License as published by
//#  *  the Free Software Foundation, either version 3 of the License, or
//#  *  (at your option) any later version.
//#  *
//#  *  This program is distributed in the hope that it will be useful,
//#  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
//#  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//#  *  GNU General Public License for more details.
//#  *
//#  *  You should have received a copy of the GNU General Public License
//#  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//#  */
//# 
//# package alchemy.midlet;
//# 
//# import alchemy.util.IO;
//# import java.io.UTFDataFormatException;
//# import javax.microedition.rms.RecordStore;
//# import javax.microedition.rms.RecordStoreException;
//# 
//# /**
//#  * Logger for debug versions of Alchemy OS.
//#  * @author Sergey Basalaev
//#  */
//# public class Logger {
//# 	
//# 	private Logger() { }
//# 	
//# 	public static void log(String message) {
//# 		try {
//# 			RecordStore logstore = RecordStore.openRecordStore("logs", true);
//# 			if (logstore.getNumRecords() == 0) logstore.addRecord(null, 0, 0);
//# 			StringBuffer msglog = new StringBuffer();
//# 			byte[] buf;
//# 			try {
//# 				buf = logstore.getRecord(1);
//# 				if (buf != null) msglog.append(IO.utfDecode(buf));
//# 			} catch (UTFDataFormatException ue) {
//# 				msglog.append("<Corrupted data>\n");
//# 			}
//# 			msglog.append(message).append('\n');
//# 			try {
//# 				buf = IO.utfEncode(msglog.toString());
//# 			} catch (UTFDataFormatException ue) {
//# 				buf = new byte[0];
//# 			}
//# 			logstore.setRecord(1, buf, 0, buf.length);
//# 			logstore.closeRecordStore();
//# 		} catch (RecordStoreException rse) { }
//# 	}
//# 	
//# 	public static String getLog() {
//# 		try {
//# 			RecordStore logstore = RecordStore.openRecordStore("logs", true);
//# 			if (logstore.getNumRecords() == 0) logstore.addRecord(null, 0, 0);
//# 			byte[] buf = logstore.getRecord(1);
//# 			logstore.closeRecordStore();
//# 			if (buf == null) return "";
//# 			try {
//# 				return IO.utfDecode(buf);
//# 			} catch (UTFDataFormatException ue) {
//# 				return "<Corrupted data>\n";
//# 			}
//# 		} catch (RecordStoreException rse) {
//# 			return "";
//# 		}
//# 	}
//# 	
//# 	public static void clearLog() {
//# 		try {
//# 			RecordStore.deleteRecordStore("logs");
//# 		} catch (RecordStoreException rse) { }
//# 	}
//# }
//# 
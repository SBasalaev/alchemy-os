//#condition DEBUGLOG

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.midlet;

import alchemy.util.IO;
import java.io.UTFDataFormatException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * Logger for debug versions of Alchemy OS.
 * @author Sergey Basalaev
 */
public class Logger {
	
	private Logger() { }
	
	public static void log(String message) {
		try {
			RecordStore logstore = RecordStore.openRecordStore("logs", true);
			if (logstore.getNumRecords() == 0) logstore.addRecord(null, 0, 0);
			StringBuffer msglog = new StringBuffer();
			byte[] buf;
			try {
				buf = logstore.getRecord(1);
				if (buf != null) msglog.append(IO.utfDecode(buf));
			} catch (UTFDataFormatException ue) {
				msglog.append("<Corrupted data>\n");
			}
			msglog.append(message).append('\n');
			try {
				buf = IO.utfEncode(msglog.toString());
			} catch (UTFDataFormatException ue) {
				buf = new byte[0];
			}
			logstore.setRecord(1, buf, 0, buf.length);
			logstore.closeRecordStore();
		} catch (RecordStoreException rse) { }
	}
	
	public static String getLog() {
		try {
			RecordStore logstore = RecordStore.openRecordStore("logs", true);
			if (logstore.getNumRecords() == 0) logstore.addRecord(null, 0, 0);
			byte[] buf = logstore.getRecord(1);
			logstore.closeRecordStore();
			if (buf == null) return "";
			try {
				return IO.utfDecode(buf);
			} catch (UTFDataFormatException ue) {
				return "<Corrupted data>\n";
			}
		} catch (RecordStoreException rse) {
			return "";
		}
	}
	
	public static void clearLog() {
		try {
			RecordStore.deleteRecordStore("logs");
		} catch (RecordStoreException rse) { }
	}
}

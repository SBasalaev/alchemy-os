package alchemy.nec.asm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Code writer for version 2.0
 * @author Sergey Basalaev
 */
public class UnitWriter {
	
	private int vmversion;
	private Vector objects = new Vector();
	
	public UnitWriter() { }
	
	public void visitVersion(int version) {
		this.vmversion = version;
	}
	
	public void visitSymbol(String symbol) {
		ExtFunc f = new ExtFunc(symbol);
		// if function is already in object, just skip it.
		if (!objects.contains(f)) objects.addElement(f);
	}
	
	public FunctionWriter visitFunction(String name, int arglen) {
		int index = objects.indexOf(new ExtFunc(name));
		AsmFunc func = new AsmFunc(name);
		if (index < 0) {
			objects.addElement(func);
		} else {
			if (objects.elementAt(index) instanceof AsmFunc) {
				throw new IllegalStateException("Function already visited: "+name);
			}
			objects.setElementAt(func, index);
		}
		return new FunctionWriter(func, objects, arglen);
	}
	
	public void writeTo(OutputStream stream) throws IOException {
		DataOutputStream out = new DataOutputStream(stream);
		out.writeShort(0xC0DE);
		out.writeShort(vmversion);
		out.writeByte(0); // flags for relocatable object
		out.writeShort(objects.size());
		for (int i=0; i<objects.size(); i++) {
			Object obj = objects.elementAt(i);
			if (obj.getClass() == Integer.class) {
				out.writeByte('i');
				out.writeInt(((Integer)obj).intValue());
			} else if (obj.getClass() == Long.class) {
				out.writeByte('l');
				out.writeLong(((Long)obj).longValue());
			} else if (obj.getClass() == Float.class) {
				out.writeByte('f');
				out.writeFloat(((Float)obj).floatValue());
			} else if (obj.getClass() == Double.class) {
				out.writeByte('d');
				out.writeDouble(((Double)obj).doubleValue());
			} else if (obj.getClass() == String.class) {
				out.writeByte('S');
				out.writeUTF((String)obj);
			} else if (obj.getClass() == ExtFunc.class) {
				out.writeByte('U');
				out.writeUTF(((ExtFunc)obj).value);
			} else if (obj.getClass() == AsmFunc.class) {
				AsmFunc f = (AsmFunc)obj;
				out.writeByte(f.shared ? 'P' : 'H');
				out.writeUTF(f.value);
				out.writeByte(f.stacksize);
				out.writeByte(f.varcount);
				out.writeShort(f.code.length);
				out.write(f.code);
				out.writeShort(f.relocs.length);
				for (int ri=0; ri<f.relocs.length; ri++) {
					out.writeChar(f.relocs[ri]);
				}
			}
		}
	}
}
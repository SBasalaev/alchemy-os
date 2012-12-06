package alchemy.nec.asm;

import alchemy.core.types.Int;
import alchemy.evm.Opcodes;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Code writer for version 2.1
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
		FuncObject f = new FuncObject(symbol);
		// if function is already in object, just skip it.
		if (!objects.contains(f)) objects.addElement(f);
	}
	
	public FunctionWriter visitFunction(String name, boolean shared, int arglen) {
		int index = objects.indexOf(new FuncObject(name));
		AsmFunc func = new AsmFunc(name);
		func.shared = shared;
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
		out.writeByte(0);
		out.writeShort(objects.size());
		for (int i=0; i<objects.size(); i++) {
			Object obj = objects.elementAt(i);
			if (obj.getClass() == Int.class) {
				out.writeByte('i');
				out.writeInt(((Int)obj).value);
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
			} else if (obj.getClass() == FuncObject.class) {
				out.writeByte('U');
				out.writeUTF(((FuncObject)obj).value);
			} else if (obj.getClass() == AsmFunc.class) {
				AsmFunc f = (AsmFunc)obj;
				out.writeByte('P');
				out.writeUTF(f.value);
				int fflags = 0;
				if (f.shared) fflags |= Opcodes.FFLAG_SHARED;
				if (f.relocs != null) fflags |= Opcodes.FFLAG_RELOCS;
				if (f.dbgtable != null) fflags |= Opcodes.FFLAG_LNUM;
				if (f.errtable != null) fflags |= Opcodes.FFLAG_ERRTBL;
				out.writeByte(fflags);
				out.writeByte(f.stacksize);
				out.writeByte(f.varcount);
				out.writeShort(f.code.length);
				out.write(f.code);
				if (f.relocs != null) writeChars(out, f.relocs);
				if (f.dbgtable != null) writeChars(out, f.dbgtable);
				if (f.errtable != null) writeChars(out, f.errtable);
			}
		}
	}
	
	private static void writeChars(DataOutputStream out, char[] chars) throws IOException {
		out.writeShort(chars.length);
		for (int i=0; i<chars.length; i++) {
			out.writeChar(chars[i]);
		}
	}
}
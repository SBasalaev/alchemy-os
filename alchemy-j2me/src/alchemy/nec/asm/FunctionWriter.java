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

package alchemy.nec.asm;

import alchemy.evm.Opcodes;
import alchemy.nec.tree.Null;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;
import alchemy.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Writes function bytecode.
 * @author Sergey Basalaev
 */
public class FunctionWriter implements Opcodes {

	private ByteArrayOutputStream data = new ByteArrayOutputStream();
	private StringBuffer relocdata = new StringBuffer();
	private StringBuffer dbgtable = new StringBuffer();
	/** Keeps sequence of labels (from, to, handle). */
	private ArrayList errdata = new ArrayList();
	private Hashtable labeldata = new Hashtable();
	private int stackpos = 0;
	private int stackmax = 0;
	private int varcount;
	private ArrayList objects;
	private AsmFunc func;
	
	FunctionWriter(AsmFunc func, ArrayList objects, int arglen) {
		this.objects = objects;
		this.varcount = arglen;
		this.func = func;
	}
	
	private void visitStack(int inc) {
		stackpos += inc;
		if (inc > 0 && stackmax < stackpos) stackmax = stackpos;
		if (stackpos < 0) throw new IllegalStateException("Stack inconsistency: empty stack");
	}
	
	/** Visit name of the source file. */
	public void visitSource(String name) {
		if (dbgtable.length() > 0) throw new IllegalStateException("Source already visited");
		int index = objects.indexOf(name);
		if (index < 0) {
			index = objects.size();
			objects.add(name);
		}
		dbgtable.append((char)index);
	}
	
	/** Visit number of the line in the source file. */
	public void visitLine(int num) {
		int len = dbgtable.length();
		if (len == 0) {
			throw new IllegalStateException("Source is not visited");
		}
		if (num >= 0 && (len < 2 || dbgtable.charAt(len-2) != num)) {
			dbgtable.append((char)num).append((char)data.size());
		}
	}
	
	/** Visit instruction without arguments. */
	public void visitInsn(int opcode) {
		data.write(opcode);
		switch (opcode) {
			case AASTORE:
			case BASTORE:
			case CASTORE:
			case SASTORE:
			case ZASTORE:
			case IASTORE:
			case LASTORE:
			case FASTORE:
			case DASTORE:
				visitStack(-3);
				break;
			case ACMP:
			case AALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
			case ZALOAD:
			case IALOAD:
			case LALOAD:
			case FALOAD:
			case DALOAD:
			case DADD:
			case DCMP:
			case DDIV:
			case DMOD:
			case DMUL:
			case DSUB:
			case FADD:
			case FCMP:
			case FDIV:
			case FMOD:
			case FMUL:
			case FSUB:
			case IADD:
			case IAND:
			case ICMP:
			case IDIV:
			case IMOD:
			case IMUL:
			case IOR:
			case ISHL:
			case ISHR:
			case ISUB:
			case IUSHR:
			case IXOR:
			case LADD:
			case LAND:
			case LCMP:
			case LDIV:
			case LMOD:
			case LMUL:
			case LOR:
			case LSUB:
			case LSHL:
			case LSHR:
			case LUSHR:
			case LXOR:
			case POP:
			case RETURN:
				visitStack(-1);
				break;
			case AALEN:
			case BALEN:
			case CALEN:
			case SALEN:
			case ZALEN:
			case IALEN:
			case LALEN:
			case FALEN:
			case DALEN:
			case D2F:
			case D2I:
			case D2L:
			case DNEG:
			case F2D:
			case F2I:
			case F2L:
			case FNEG:
			case I2D:
			case I2F:
			case I2L:
			case INEG:
			case L2D:
			case L2F:
			case L2I:
			case LNEG:
			case NEWAA:
			case NEWBA:
			case NEWCA:
			case NEWSA:
			case NEWZA:
			case NEWIA:
			case NEWLA:
			case NEWFA:
			case NEWDA:
			case NOP:
			case RET_NULL:
			case SWAP:
			case I2C:
			case I2B:
			case I2S:
				break;
			case DUP:
				visitStack(1);
				break;
			case DUP2:
				visitStack(2);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	/** Visit CALL or CALV instruction. */
	public void visitCallInsn(int opcode, int arglen) {
		if (arglen < 0 || arglen > 255) throw new IllegalArgumentException();
		if (arglen < 8) {
			if (opcode == CALL) data.write(CALL_0 + arglen);
			else data.write(CALV_0 + arglen);
		} else {
			data.write(opcode);
			data.write(arglen);
		}
		visitStack(opcode == CALL ? -arglen : -arglen-1);
	}
	
	/** Visit LOAD or STORE instruction. */
	public void visitVarInsn(int opcode, int var) {
		if (var < 0 || var > 255) throw new IllegalArgumentException();
		if (var >= varcount) varcount = var+1;
		if (var < 8) {
			if (opcode == LOAD) data.write(LOAD_0 + var);
			else data.write(STORE_0 + var);
		} else {
			data.write(opcode);
			data.write(var);
		}
		visitStack(opcode == LOAD ? 1 : -1);
	}
	
	public void visitIincInsn(int var, int incr) {
		if (var < 0 || var > 255) throw new IllegalArgumentException();
		if (var >= varcount) varcount = var+1;
		if (incr < Byte.MIN_VALUE || incr > Byte.MAX_VALUE) throw new IllegalArgumentException();
		data.write(IINC);
		data.write(var);
		data.write(incr);
	}
	
	/** Visit LDC instruction with given object.
	 * The class of argument must be one of Null,
	 * Boolean, Int32, Int64, Float32, Float64,
	 * String, FuncObject.
	 */
	public void visitLdcInsn(Object cnst) {
		boolean written = false;
		if (cnst instanceof Boolean) {
			if (cnst.equals(Boolean.TRUE)) data.write(ICONST_1);
			else data.write(ICONST_0);
			written = true;
		} else if (cnst instanceof Int32) {
			int i = ((Int32)cnst).value;
			if (i >= -1 && i <= 5) {
				data.write(ICONST_0 + i);
				written = true;
			} else if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				data.write(BIPUSH);
				data.write(i);
				written = true;
			} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				data.write(SIPUSH);
				data.write(i >> 8);
				data.write(i);
				written = true;
			}
		} else if (cnst instanceof Int64) {
			long l = ((Int64)cnst).value;
			if (l == 0l) {
				data.write(LCONST_0);
				written = true;
			} else if (l == 1l) {
				data.write(LCONST_1);
				written = true;
			}
		} else if (cnst instanceof Float32) {
			float f = ((Float32)cnst).value;
			if (f == 0f) {
				data.write(FCONST_0);
				written = true;
			} else if (f == 1f) {
				data.write(FCONST_1);
				written = true;
			} else if (f == 2f) {
				data.write(FCONST_2);
				written = true;
			}
		} else if (cnst instanceof Float64) {
			double d = ((Float64)cnst).value;
			if (d == 0d) {
				data.write(DCONST_0);
				written = true;
			} else if (d == 1d) {
				data.write(DCONST_1);
				written = true;
			}
		} else if (cnst == Null.NULL) {
			data.write(ACONST_NULL);
			written = true;
		}
		if (!written) {
			int index = objects.indexOf(cnst);
			if (index < 0) {
				index = objects.size();
				objects.add(cnst);
			}
			data.write(LDC);
			relocdata.append((char)data.size());
			data.write(index >> 8);
			data.write(index);
		}
		visitStack(1);
	}
	
	/**
	 * Marks current code position as pointer with label address.
	 */
	private void visitLabelPtr(Label label) {
		if (label.addr >= 0) {
			data.write(label.addr >> 8);
			data.write(label.addr);
		} else {
			StringBuffer sb = (StringBuffer)labeldata.get(label);
			if (sb == null) {
				sb = new StringBuffer();
				labeldata.put(label, sb);
			}
			sb.append((char)data.size());
			data.write(-1);
			data.write(-1);
		}
		if (label.stackpos < 0) {
			label.stackpos = stackpos;
		} else if (label.stackpos != stackpos) {
			throw new IllegalStateException("Stack inconsistency: other jump expects different stack");
		}
	}
	
	public void visitJumpInsn(int opcode, Label label) {
		data.write(opcode);
		switch (opcode) {
			case GOTO:
				break;
			case IFEQ:
			case IFGE:
			case IFGT:
			case IFLE:
			case IFLT:
			case IFNE:
			case IFNULL:
			case IFNNULL:
				visitStack(-1);
				break;
			case IF_ICMPGE:
			case IF_ICMPGT:
			case IF_ICMPLE:
			case IF_ICMPLT:
			case IF_ACMPEQ:
			case IF_ACMPNE:
				visitStack(-2);
				break;
			default:
				throw new IllegalArgumentException();
		}
		visitLabelPtr(label);
	}
	
	public void visitTableSwitch(int min, int max, Label dflt, Label[] jumps) {
		data.write(Opcodes.TABLESWITCH);
		visitStack(-1);
		visitLabelPtr(dflt);
		data.write(min >> 24);
		data.write(min >> 16);
		data.write(min >> 8);
		data.write(min);
		data.write(max >> 24);
		data.write(max >> 16);
		data.write(max >> 8);
		data.write(max);
		for (int i=0; i<=max-min; i++) {
			visitLabelPtr(jumps[i]);
		}
	}
	
	public void visitLookupSwitch(Label dflt, int[] cases, Label[] jumps) {
		data.write(Opcodes.LOOKUPSWITCH);
		visitStack(-1);
		visitLabelPtr(dflt);
		data.write(cases.length >> 8);
		data.write(cases.length);
		for (int i=0; i<cases.length; i++) {
			data.write(cases[i] >> 24);
			data.write(cases[i] >> 16);
			data.write(cases[i] >> 8);
			data.write(cases[i]);
			visitLabelPtr(jumps[i]);
		}
	}
	
	public void visitLabel(Label label) {
		if (label.addr >= 0) throw new IllegalStateException("Label already visited");
		if (label.stackpos < 0) {
			label.stackpos = stackpos;
		} else {
			stackpos = label.stackpos;
		}
		label.addr = data.size();
	}
	
	public void visitTryCatchHandler(Label from, Label to) {
		if (from.stackpos < 0)
			throw new IllegalStateException("Try/Catch handler must be visited after corresponding block");
		Label handler = new Label();
		handler.stackpos = from.stackpos + 1;
		visitLabel(handler);
		errdata.add(from);
		errdata.add(to);
		errdata.add(handler);
	}
	
	public void visitEnd() {
		byte[] code = data.toByteArray();
		for (Enumeration e = labeldata.keys(); e.hasMoreElements(); ) {
			Label label = (Label)e.nextElement();
			if (label.addr < 0) throw new IllegalStateException("Label not visited");
			StringBuffer sb = (StringBuffer)labeldata.get(label);
			for (int i=0; i<sb.length(); i++) {
				int addr = sb.charAt(i);
				code[addr] = (byte)(label.addr >> 8);
				code[addr+1] = (byte)label.addr;
			}
		}
		func.code = code;
		func.stacksize = this.stackmax;
		func.varcount = this.varcount;
		func.relocs = new char[relocdata.length()];
		relocdata.getChars(0, relocdata.length(), func.relocs, 0);
		if (dbgtable.length() > 0) {
			func.dbgtable = new char[dbgtable.length()];
			dbgtable.getChars(0, dbgtable.length(), func.dbgtable, 0);
		}
		if (errdata.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<errdata.size(); i += 3) {
				Label from = (Label)errdata.get(i);
				if (from.addr < 0) throw new IllegalStateException("Label not visited");
				Label to = (Label)errdata.get(i+1);
				if (to.addr < 0) throw new IllegalStateException("Label not visited");
				Label handle = (Label)errdata.get(i+2);
				if (handle.addr < 0) throw new IllegalStateException("Label not visited");
				sb.append((char)from.addr).append((char)to.addr).append((char)handle.addr).append((char)from.stackpos);
			}
			func.errtable = new char[errdata.size() / 3 * 4];
			sb.getChars(0, sb.length(), func.errtable, 0);
		}
	}
}

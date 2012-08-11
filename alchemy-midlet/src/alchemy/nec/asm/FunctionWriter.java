package alchemy.nec.asm;

import alchemy.evm.Opcodes;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Sergey Basalaev
 */
public class FunctionWriter implements Opcodes {

	private ByteArrayOutputStream data = new ByteArrayOutputStream();
	private StringBuffer relocdata = new StringBuffer();
	private Hashtable labeldata = new Hashtable();
	private int stackpos = 0;
	private int stackmax = 0;
	private int varcount;
	private Vector objects;
	private AsmFunc func;
	
	FunctionWriter(AsmFunc func, Vector objects, int arglen) {
		this.objects = objects;
		this.varcount = arglen;
		this.func = func;
	}
	
	private void visitStack(int inc) {
		stackpos += inc;
		if (inc > 0 && stackmax < stackpos) stackmax = stackpos;
		if (stackpos < 0) throw new IllegalStateException("Stack inconsistency: empty stack");
	}
	
	/** Visit instruction without arguments. */
	public void visitInsn(int opcode) {
		data.write(opcode);
		switch (opcode) {
			case ASTORE:
			case BASTORE:
			case CASTORE:
				visitStack(-3);
				break;
			case ACMP:
			case ALOAD:
			case BALOAD:
			case CALOAD:
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
			case ALEN:
			case BALEN:
			case CALEN:
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
			case NEWARRAY:
			case NEWBA:
			case NEWCA:
			case NOP:
			case RET_NULL:
			case SWAP:
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
	
	/** Visit LDC instruction with given object.
	 * The class of argument must be one of null,
	 * Boolean, Integer, Long, Float, Double,
	 * String, FuncObject.
	 */
	public void visitLdcInsn(Object cnst) {
		boolean written = false;
		if (cnst == null) {
			data.write(ACONST_NULL);
			written = true;
		} else if (cnst instanceof Boolean) {
			if (cnst.equals(Boolean.TRUE)) data.write(ICONST_1);
			else data.write(ICONST_0);
			written = true;
		} else if (cnst instanceof Integer) {
			int i = ((Integer)cnst).intValue();
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
		} else if (cnst instanceof Long) {
			long l = ((Long)cnst).longValue();
			if (l == 0l) {
				data.write(LCONST_0);
				written = true;
			} else if (l == 1l) {
				data.write(LCONST_1);
				written = true;
			}
		} else if (cnst instanceof Float) {
			float f = ((Float)cnst).floatValue();
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
		} else if (cnst instanceof Double) {
			double d = ((Double)cnst).doubleValue();
			if (d == 0d) {
				data.write(DCONST_0);
				written = true;
			} else if (d == 1d) {
				data.write(DCONST_1);
				written = true;
			}
		}
		if (!written) {
			int index = objects.indexOf(cnst);
			if (index < 0) {
				objects.addElement(cnst);
				index = objects.size()-1;
			}
			data.write(LDC);
			relocdata.append((char)data.size());
			data.write(index >> 8);
			data.write(index);
		}
		visitStack(1);
	}
	
	/**
	 * Marks current code position as containing label address.
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
		int reloclen = relocdata.length();
		func.relocs = new char[reloclen];
		relocdata.getChars(0, reloclen, func.relocs, 0);
	}
}

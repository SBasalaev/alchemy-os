/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.evm;

import alchemy.core.AlchemyException;
import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.types.Int;

/**
 * Embedded Virtual Machine.
 * @author Sergey Basalaev
 */
class EFunction extends Function {

	private final int stacksize;
	private final int localsize;
	private final byte[] bcode;
	private final char[] dbgtable;
	private final char[] errtable;
	private final Object[] cpool;
	private final String libname;

	public EFunction(String libname, String funcname, Object[] cpool, int stacksize, int localsize, byte[] code, char[] dbgtable, char[] errtable) {
		super(funcname);
		this.libname = libname;
		this.stacksize = stacksize;
		this.localsize = localsize;
		this.bcode = code;
		this.cpool = cpool;
		this.dbgtable = dbgtable;
		this.errtable = errtable;
	}

	public Object exec(Context c, Object[] args) throws AlchemyException {
		//initializing
		final Object[] stack = new Object[stacksize];
		int head = -1;
		final byte[] code = this.bcode;
		Object[] locals;
		if (args.length == localsize) {
			locals = args;
		} else {
			locals = new Object[localsize];
			System.arraycopy(args, 0, locals, 0, args.length);
		}
		int ct = 0;
		while (true) {
		try {
			int instr = code[ct];
			ct++;
			switch (instr) {
			// CONSTANTS
				case Opcodes.ACONST_NULL: {
					head++;
					stack[head] = null;
					break;
				}
				case Opcodes.ICONST_M1: {
					head++;
					stack[head] = Int.M_ONE;
					break;
				}
				case Opcodes.ICONST_0: {
					head++;
					stack[head] = Int.ZERO;
					break;
				}
				case Opcodes.ICONST_1: {
					head++;
					stack[head] = Int.ONE;
					break;
				}
				case Opcodes.ICONST_2: {
					head++;
					stack[head] = Int.toInt(2);
					break;
				}
				case Opcodes.ICONST_3: {
					head++;
					stack[head] = Int.toInt(3);
					break;
				}
				case Opcodes.ICONST_4: {
					head++;
					stack[head] = Int.toInt(4);
					break;
				}
				case Opcodes.ICONST_5: {
					head++;
					stack[head] = Int.toInt(5);
					break;
				}
				case Opcodes.LCONST_0: {
					head++;
					stack[head] = Lval(0l);
					break;
				}
				case Opcodes.LCONST_1: {
					head++;
					stack[head] = Lval(1l);
					break;
				}
				case Opcodes.FCONST_0: {
					head++;
					stack[head] = Fval(0f);
					break;
				}
				case Opcodes.FCONST_1: {
					head++;
					stack[head] = Fval(1f);
					break;
				}
				case Opcodes.FCONST_2: {
					head++;
					stack[head] = Fval(2f);
					break;
				}
				case Opcodes.DCONST_0: {
					head++;
					stack[head] = Dval(0d);
					break;
				}
				case Opcodes.DCONST_1: {
					head++;
					stack[head] = Dval(1d);
					break;
				}
				
			//CONVERSIONS
				case Opcodes.I2L: {
					stack[head] = Lval(ival(stack[head]));
					break;
				}
				case Opcodes.I2F: {
					stack[head] = Fval(ival(stack[head]));
					break;
				}
				case Opcodes.I2D: {
					stack[head] = Dval(ival(stack[head]));
					break;
				}
				case Opcodes.L2F: {
					stack[head] = Fval(lval(stack[head]));
					break;
				}
				case Opcodes.L2D: {
					stack[head] = Dval(lval(stack[head]));
					break;
				}
				case Opcodes.L2I: {
					stack[head] = Ival((int)lval(stack[head]));
					break;
				}
				case Opcodes.F2D: {
					stack[head] = Dval(fval(stack[head]));
					break;
				}
				case Opcodes.F2I: {
					stack[head] = Ival((int)fval(stack[head]));
					break;
				}
				case Opcodes.F2L: {
					stack[head] = Lval((long)fval(stack[head]));
					break;
				}
				case Opcodes.D2I: {
					stack[head] = Ival((int)dval(stack[head]));
					break;
				}
				case Opcodes.D2L: {
					stack[head] = Lval((long)dval(stack[head]));
					break;
				}
				case Opcodes.D2F: {
					stack[head] = Fval((float)dval(stack[head]));
					break;
				}
				case Opcodes.I2C: {
					stack[head] = ((Int)stack[head]).toChar();
					break;
				}

			//INTEGER ARITHMETICS
				case Opcodes.IADD: {
					head--;
					stack[head] = Ival(ival(stack[head]) + ival(stack[head+1]));
					break;
				}
				case Opcodes.ISUB: {
					head--;
					stack[head] = Ival(ival(stack[head]) - ival(stack[head+1]));
					break;
				}
				case Opcodes.IMUL: {
					head--;
					stack[head] = Ival(ival(stack[head]) * ival(stack[head+1]));
					break;
				}
				case Opcodes.IDIV: {
					head--;
					stack[head] = Ival(ival(stack[head]) / ival(stack[head+1]));
					break;
				}
				case Opcodes.IMOD: {
					head--;
					stack[head] = Ival(ival(stack[head]) % ival(stack[head+1]));
					break;
				}
				case Opcodes.INEG: {
					stack[head] = Ival(-ival(stack[head]));
					break;
				}
				case Opcodes.ICMP: {
					head--;
					int itmp = ival(stack[head]) - ival(stack[head+1]);
					stack[head] = (itmp > 0) ? Int.ONE : (itmp == 0 ? Int.ZERO : Int.M_ONE);
					break;
				}
				case Opcodes.ISHL: {
					head--;
					stack[head] = Ival(ival(stack[head]) << ival(stack[head+1]));
					break;
				}
				case Opcodes.ISHR: {
					head--;
					stack[head] = Ival(ival(stack[head]) >> ival(stack[head+1]));
					break;
				}
				case Opcodes.IUSHR: {
					head--;
					stack[head] = Ival(ival(stack[head]) >>> ival(stack[head+1]));
					break;
				}
				case Opcodes.IAND: {
					head--;
					stack[head] = Ival(ival(stack[head]) & ival(stack[head+1]));
					break;
				}
				case Opcodes.IOR: {
					head--;
					stack[head] = Ival(ival(stack[head]) | ival(stack[head+1]));
					break;
				}
				case Opcodes.IXOR: {
					head--;
					stack[head] = Ival(ival(stack[head]) ^ ival(stack[head+1]));
					break;
				}

			//LONG ARITHMETICS
				case Opcodes.LADD: {
					head--;
					stack[head] = Lval(lval(stack[head]) + lval(stack[head+1]));
					break;
				}
				case Opcodes.LSUB: {
					head--;
					stack[head] = Lval(lval(stack[head]) - lval(stack[head+1]));
					break;
				}
				case Opcodes.LMUL: {
					head--;
					stack[head] = Lval(lval(stack[head]) * lval(stack[head+1]));
					break;
				}
				case Opcodes.LDIV: {
					head--;
					stack[head] = Lval(lval(stack[head]) / lval(stack[head+1]));
					break;
				}
				case Opcodes.LMOD:  {
					head--;
					stack[head] = Lval(lval(stack[head]) % lval(stack[head+1]));
					break;
				}
				case Opcodes.LNEG: {
					stack[head] = Lval(-lval(stack[head]));
					break;
				}
				case Opcodes.LCMP: {
					head--;
					long ltmp = lval(stack[head]) - lval(stack[head+1]);
					stack[head] = (ltmp > 0L) ? Int.ONE : (ltmp == 0L ? Int.ZERO : Int.M_ONE);
					break;
				}
				case Opcodes.LSHL: {
					head--;
					stack[head] = Lval(lval(stack[head]) << ival(stack[head+1]));
					break;
				}
				case Opcodes.LSHR: {
					head--;
					stack[head] = Lval(lval(stack[head]) >> ival(stack[head+1]));
					break;
				}
				case Opcodes.LUSHR: {
					head--;
					stack[head] = Lval(lval(stack[head]) >>> ival(stack[head+1]));
					break;
				}
				case Opcodes.LAND: {
					head--;
					stack[head] = Lval(lval(stack[head]) & lval(stack[head+1]));
					break;
				}
				case Opcodes.LOR: {
					head--;
					stack[head] = Lval(lval(stack[head]) | lval(stack[head+1]));
					break;
				}
				case Opcodes.LXOR: {
					head--;
					stack[head] = Lval(lval(stack[head]) ^ lval(stack[head+1]));
					break;
				}

			//FLOAT ARITHMETICS
				case Opcodes.FADD: {
					head--;
					stack[head] = Fval(fval(stack[head]) + fval(stack[head+1]));
					break;
				}
				case Opcodes.FSUB: {
					head--;
					stack[head] = Fval(fval(stack[head]) - fval(stack[head+1]));
					break;
				}
				case Opcodes.FMUL: {
					head--;
					stack[head] = Fval(fval(stack[head]) * fval(stack[head+1]));
					break;
				}
				case Opcodes.FDIV: {
					head--;
					stack[head] = Fval(fval(stack[head]) / fval(stack[head+1]));
					break;
				}
				case Opcodes.FMOD: {
					head--;
					stack[head] = Fval(fval(stack[head]) % fval(stack[head+1]));
					break;
				}
				case Opcodes.FNEG: {
					stack[head] = Fval(-fval(stack[head]));
					break;
				}
				case Opcodes.FCMP: {
					head--;
					float ftmp = fval(stack[head]) - fval(stack[head+1]);
					stack[head] = (ftmp > 0) ? Int.ONE : (ftmp == 0 ? Int.ZERO : Int.M_ONE);
					break;
				}

			//DOUBLE ARITHMETICS
				case Opcodes.DADD: {
					head--;
					stack[head] = Dval(dval(stack[head]) + dval(stack[head+1]));
					break;
				}
				case Opcodes.DSUB: {
					head--;
					stack[head] = Dval(dval(stack[head]) - dval(stack[head+1]));
					break;
				}
				case Opcodes.DMUL: {
					head--;
					stack[head] = Dval(dval(stack[head]) * dval(stack[head+1]));
					break;
				}
				case Opcodes.DDIV: {
					head--;
					stack[head] = Dval(dval(stack[head]) / dval(stack[head+1]));
					break;
				}
				case Opcodes.DMOD: {
					head--;
					stack[head] = Dval(dval(stack[head]) % dval(stack[head+1]));
					break;
				}
				case Opcodes.DNEG: {
					stack[head] = Dval(-dval(stack[head]));
					break;
				}
				case Opcodes.DCMP: {
					head--;
					double dtmp = dval(stack[head]) - dval(stack[head+1]);
					stack[head] = (dtmp > 0) ? Int.ONE : (dtmp == 0 ? Int.ZERO : Int.M_ONE);
					break;
				}

			//LOCALS LOADERS AND SAVERS
				case Opcodes.LOAD_0:
				case Opcodes.LOAD_1:
				case Opcodes.LOAD_2:
				case Opcodes.LOAD_3:
				case Opcodes.LOAD_4:
				case Opcodes.LOAD_5:
				case Opcodes.LOAD_6:
				case Opcodes.LOAD_7: {
					head++;
					stack[head] = locals[instr & 7];
					break;
				}
				case Opcodes.LOAD: { //load <ubyte>
					head++;
					stack[head] = locals[code[ct] & 0xff];
					ct++;
					break;
				}
				//variable savers
				case Opcodes.STORE_0:
				case Opcodes.STORE_1:
				case Opcodes.STORE_2:
				case Opcodes.STORE_3:
				case Opcodes.STORE_4:
				case Opcodes.STORE_5:
				case Opcodes.STORE_6:
				case Opcodes.STORE_7: {
					locals[instr & 7] = stack[head];
					head--;
					break;
				}
				case Opcodes.STORE: { //store <ubyte>
					locals[code[ct] & 0xff] = stack[head];
					ct++;
					head--;
					break;
				}

			//BRANCHING
				case Opcodes.IFEQ: { //ifeq <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) == 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFNE: { //ifne <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) != 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFLT: { //iflt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) < 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFGE: { //ifge <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) >= 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFGT: { //ifgt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) > 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFLE: { //ifle <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head]) <= 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.GOTO: { //goto <ushort>
					ct = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					break;
				}
				case Opcodes.IFNULL: { //ifnull <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (stack[head] == null) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFNNULL: { //ifnnull <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (stack[head] != null) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IF_ICMPLT: { //if_icmplt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head-1]) < ival(stack[head])) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPGE: { //if_icmpge <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head-1]) >= ival(stack[head])) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPGT: { //if_icmpgt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head-1]) > ival(stack[head])) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPLE: { //if_icmple <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (ival(stack[head-1]) <= ival(stack[head])) ct = itmp;
					head -= 2;
					break;
				}
			//FUNCTION CALLS
				case Opcodes.CALL_0:
				case Opcodes.CALL_1:
				case Opcodes.CALL_2:
				case Opcodes.CALL_3:
				case Opcodes.CALL_4:
				case Opcodes.CALL_5:
				case Opcodes.CALL_6:
				case Opcodes.CALL_7:
				case Opcodes.CALV_0:
				case Opcodes.CALV_1:
				case Opcodes.CALV_2:
				case Opcodes.CALV_3:
				case Opcodes.CALV_4:
				case Opcodes.CALV_5:
				case Opcodes.CALV_6:
				case Opcodes.CALV_7: {
					int paramlen = instr & 7;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					stack[head] = ((Function)stack[head]).exec(c, params);
					if ((instr & 8) != 0) head--;
					break;
				}
				case Opcodes.CALL: {//call <ubyte>
					int paramlen = code[ct] & 0xff;
					ct++;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					stack[head] = ((Function)stack[head]).exec(c, params);
					break;
				}
				case Opcodes.CALV: {//calv <ubyte>
					int paramlen = code[ct] & 0xff;
					ct++;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					((Function)stack[head]).exec(c, params);
					head--;
					break;
				}

			//ARRAY INSTRUCTIONS
				case Opcodes.NEWAA: {
					stack[head] = new Object[ival(stack[head])];
					break;
				}
				case Opcodes.AALOAD: {
					int at = ival(stack[head]);
					head--;
					stack[head] = ((Object[])stack[head])[at];
					break;
				}
				case Opcodes.AASTORE: {
					Object val = stack[head];
					int at = ival(stack[head-1]);
					Object[] array = (Object[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.AALEN: {
					stack[head] = Ival(((Object[])stack[head]).length);
					break;
				}
				case Opcodes.NEWBA: {
					stack[head] = new byte[ival(stack[head])];
					break;
				}
				case Opcodes.BALOAD: {
					int at = ival(stack[head]);
					head--;
					stack[head] = Ival( ((byte[])stack[head])[at] );
					break;
				}
				case Opcodes.BASTORE: {
					int val = ival(stack[head]);
					int at = ival(stack[head-1]);
					byte[] array = (byte[])stack[head-2];
					array[at] = (byte)val;
					head -= 3;
					break;
				}
				case Opcodes.BALEN: {
					stack[head] = Ival(((byte[])stack[head]).length);
					break;
				}
				case Opcodes.NEWCA: {
					stack[head] = new char[ival(stack[head])];
					break;
				}
				case Opcodes.CALOAD: {
					int at = ival(stack[head]);
					head--;
					stack[head] = Cval( ((char[])stack[head])[at] );
					break;
				}
				case Opcodes.CASTORE: {
					int val = ival(stack[head]);
					int at = ival(stack[head-1]);
					char[] array = (char[])stack[head-2];
					array[at] = (char)val;
					head -= 3;
					break;
				}
				case Opcodes.CALEN: {
					stack[head] = Ival(((char[])stack[head]).length);
					break;
				}

			//SWITCH BRANCHING
				case Opcodes.TABLESWITCH: {
					int dflt = ((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff);
					ct += 2;
					int min = (code[ct] << 24)
					        | ((code[ct+1] & 0xff) << 16)
					        | ((code[ct+2] & 0xff) << 8)
					        | (code[ct+3] & 0xff);
					ct += 4;
					int max = (code[ct] << 24)
					        | ((code[ct+1] & 0xff) << 16)
					        | ((code[ct+2] & 0xff) << 8)
					        | (code[ct+3] & 0xff);
					ct += 4;
					int val = ival(stack[head]);
					head--;
					if (val >= min && val <= max) {
						ct += (val-min)*2;
						ct = ((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff);
					} else {
						ct = dflt;
					}
					break;
				}
				case Opcodes.LOOKUPSWITCH: {
					int dflt = ((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff);
					ct += 2;
					int count = ((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff);
					ct += 2;
					int val = ival(stack[head]);
					head--;
					boolean matched = false;
					for (int i=0; i<count && !matched; i++) {
						int cand = (code[ct] << 24)
					             | ((code[ct+1] & 0xff) << 16)
					             | ((code[ct+2] & 0xff) << 8)
					             | (code[ct+3] & 0xff);
						ct += 4;
						if (val == cand) {
							ct = ((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff);
							matched = true;
						} else {
							ct += 2;
						}
					}
					if (!matched) ct = dflt;
					break;
				}
			//OTHERS
				case Opcodes.ACMP: {
					head--;
					boolean eq = (stack[head] == null) ? stack[head+1] == null : stack[head].equals(stack[head+1]);
					stack[head] = Ival(!eq);
					break;
				}
				case Opcodes.RET_NULL:
					return null;
				case Opcodes.RETURN:
					return stack[head];
				case Opcodes.DUP: {
					stack[head+1] = stack[head];
					head++;
					break;
				}
				case Opcodes.DUP2: {
					stack[head+2] = stack[head+1] = stack[head];
					head += 2;
					break;
				}
				case Opcodes.SWAP: {
					Object atmp = stack[head-1];
					stack[head-1] = stack[head];
					stack[head] = atmp;
					break;
				}
				case Opcodes.LDC: { //ldc <ushort>
					head++;
					stack[head] = cpool[((code[ct] & 0xff) << 8) | (code[ct+1] & 0xff)];
					ct += 2;
					break;
				}
				case Opcodes.POP:
					head--;
					break;
				case Opcodes.BIPUSH: //bipush <byte>
					head++;
					stack[head] = Ival(code[ct]);
					ct++;
					break;
				case Opcodes.SIPUSH: //sipush <short>
					head++;
					stack[head] = Ival((code[ct] << 8) | (code[ct+1]& 0xff));
					ct += 2;
					break;
			} /* the big switch */
		} catch (Throwable e) {
			// the instruction on which error occured
			ct--;
			// filling exception with debug info
			AlchemyException ae = (e instanceof AlchemyException) ? (AlchemyException)e : new AlchemyException(e);
			if (dbgtable != null) {
				int srcline = 0;
				for (int i=1; i<dbgtable.length; i += 2) {
					if (dbgtable[i+1] <= ct) srcline = dbgtable[i];
				}
				ae.addTraceElement(this, cpool[dbgtable[0]]+":"+srcline);
			} else {
				ae.addTraceElement(this, "+"+ct);
			}
			// catching or rethrowing
			int jumpto = -1;
			if (errtable != null) {
				for (int i=0; i < errtable.length && jumpto < 0; i += 4)
				if (ct >= errtable[i] && ct <= errtable[i+1]) {
					jumpto = errtable[i+2];
					head = errtable[i+3];
				}
			}
			if (jumpto >= 0) {
				stack[head] = ae;
				ct = jumpto;
			} else {
				throw ae;
			}
		}
		} /* the great while */
	}

	public String toString() {
		if (libname != null) return libname+':'+signature;
		else return signature;
	}
}

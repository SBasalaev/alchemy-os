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

import alchemy.core.Context;
import alchemy.core.Function;

/**
 * Embedded Virtual Machine.
 * @author Sergey Basalaev
 */
class EFunction extends Function {

	private final int stacksize;
	private final int localsize;
	private final byte[] bcode;
	private final Object[] cpool;
	private final String libname;

	public EFunction(String libname,
			          String funcname,
					  Object[] cpool,
					  int stacksize,
					  int localsize,
					  byte[] code
					  ) {
		super(funcname);
		this.libname = libname;
		this.stacksize = stacksize;
		this.localsize = localsize;
		this.bcode = code;
		this.cpool = cpool;
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		//initializing
		final Object[] stack = new Object[stacksize];
		int head = -1;
		byte[] code = this.bcode;
		final Object[] locals = new Object[localsize];
		System.arraycopy(args, 0, locals, 0, args.length);
		int ct = 0;
		while (true) {
			int instr = code[ct++];
			switch (instr) {
			// CONSTANTS
				case (byte) 0x01: { // aconst_null
					stack[++head] = null;
					break;
				}
				case (byte) 0x02: { // iconst_m1
					stack[++head] = Function.M_ONE;
					break;
				}
				case (byte) 0x03: { // iconst_0
					stack[++head] = Function.ZERO;
					break;
				}
				case (byte) 0x04: { // iconst_1
					stack[++head] = Function.ONE;
					break;
				}
				case (byte) 0x05: { // iconst_2
					stack[++head] = Ival(2);
					break;
				}
				case (byte) 0x06: { // iconst_3
					stack[++head] = Ival(3);
					break;
				}
				case (byte) 0x07: { // iconst_4
					stack[++head] = Ival(4);
					break;
				}
				case (byte) 0x08: { // iconst_5
					stack[++head] = Ival(5);
					break;
				}
				case (byte) 0x09: { // lconst_0
					stack[++head] = Lval(0l);
					break;
				}
				case (byte) 0x0A: { // lconst_1
					stack[++head] = Lval(1l);
					break;
				}
				case (byte) 0x0B: { // fconst_0
					stack[++head] = Fval(0f);
					break;
				}
				case (byte) 0x0C: { // fconst_1
					stack[++head] = Fval(1f);
					break;
				}
				case (byte) 0x0D: { // fconst_2
					stack[++head] = Fval(2f);
					break;
				}
				case (byte) 0x0E: { // dconst_0
					stack[++head] = Dval(0d);
					break;
				}
				case (byte) 0x0F: { // dconst_1
					stack[++head] = Dval(1d);
					break;
				}
				
			//CONVERSIONS
				case (byte) 0x37: { // i2l
					stack[head] = Lval(ival(stack[head]));
					break;
				}
				case (byte) 0x38: { // i2f
					stack[head] = Fval(ival(stack[head]));
					break;
				}
				case (byte) 0x39: { // i2d
					stack[head] = Dval(ival(stack[head]));
					break;
				}
				case (byte) 0x3A: { // l2f
					stack[head] = Fval(lval(stack[head]));
					break;
				}
				case (byte) 0x3B: { // l2d
					stack[head] = Dval(lval(stack[head]));
					break;
				}
				case (byte) 0x3C: { // l2i
					stack[head] = Ival((int)lval(stack[head]));
					break;
				}
				case (byte) 0x47: { // f2d
					stack[head] = Dval(fval(stack[head]));
					break;
				}
				case (byte) 0x48: { // f2i
					stack[head] = Ival((int)fval(stack[head]));
					break;
				}
				case (byte) 0x49: { // f2l
					stack[head] = Lval((long)fval(stack[head]));
					break;
				}
				case (byte) 0x4A: { // d2i
					stack[head] = Ival((int)dval(stack[head]));
					break;
				}
				case (byte) 0x4B: { // d2l
					stack[head] = Lval((long)dval(stack[head]));
					break;
				}
				case (byte) 0x4C: { // d2f
					stack[head] = Fval((float)dval(stack[head]));
					break;
				}

			//INTEGER ARITHMETICS
				case (byte) 0x10: { // iadd
					Object atmp = Ival(ival(stack[--head]) + ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x11: { // isub
					Object atmp = Ival(ival(stack[--head]) - ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x12: { // imul
					Object atmp = Ival(ival(stack[--head]) * ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x13: { // idiv
					Object atmp = Ival(ival(stack[--head]) / ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x14: { // imod
					Object atmp = Ival(ival(stack[--head]) % ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x15: { // ineg
					stack[head] = Ival(-ival(stack[head]));
					break;
				}
				case (byte) 0x16: { // icmp
					int itmp = ival(stack[--head]) - ival(stack[++head]);
					stack[--head] = (itmp > 0) ? Function.ONE : (itmp == 0 ? Function.ZERO : Function.M_ONE);
					break;
				}
				case (byte) 0x17: { // ishl
					Object atmp = Ival(ival(stack[--head]) << ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x18: { // ishr
					Object atmp = Ival(ival(stack[--head]) >> ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x19: { //iushr
					Object atmp = Ival(ival(stack[--head]) >>> ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x1A: { // iand
					Object atmp = Ival(ival(stack[--head]) & ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x1B: { // ior
					Object atmp = Ival(ival(stack[--head]) | ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x1C: { // ixor
					Object atmp = Ival(ival(stack[--head]) ^ ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}

			//LONG ARITHMETICS
				case (byte) 0x20: { // ladd
					Object atmp = Lval(lval(stack[--head]) + lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x21: { // lsub
					Object atmp = Lval(lval(stack[--head]) - lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x22: { // lmul
					Object atmp = Lval(lval(stack[--head]) * lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x23: { // ldiv
					Object atmp = Lval(lval(stack[--head]) / lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x24:  { // lmod
					Object atmp = Lval(lval(stack[--head]) % lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x25: { // lneg
					stack[head] = Lval(-lval(stack[head]));
					break;
				}
				case (byte) 0x26: { // lcmp
					long ltmp = lval(stack[--head]) - lval(stack[++head]);
					stack[--head] = (ltmp > 0) ? Function.ONE : (ltmp == 0 ? Function.ZERO : Function.M_ONE);
					break;
				}
				case (byte) 0x27: { // lshl
					Object atmp = Lval(lval(stack[--head]) << ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x28: { // lshr
					Object atmp = Lval(lval(stack[--head]) >> ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x29: { // lushr
					Object atmp = Lval(lval(stack[--head]) >>> ival(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x2A: { // land
					Object atmp = Lval(lval(stack[--head]) & lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x2B: { // lor
					Object atmp = Lval(lval(stack[--head]) | lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x2C: { // lxor
					Object atmp = Lval(lval(stack[--head]) ^ lval(stack[++head]));
					stack[--head] = atmp;
					break;
				}

			//FLOAT ARITHMETICS
				case (byte) 0x30: { // fadd
					Object atmp = Fval(fval(stack[--head]) + fval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x31: { // fsub
					Object atmp = Fval(fval(stack[--head]) - fval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x32: { // fmul
					Object atmp = Fval(fval(stack[--head]) * fval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x33: { // fdiv
					Object atmp = Fval(fval(stack[--head]) / fval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x34: { // fmod
					Object atmp = Fval(fval(stack[--head]) % fval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x35: { // fneg
					stack[head] = Fval(-fval(stack[head]));
					break;
				}
				case (byte) 0x36: { // fcmp
					float ftmp = fval(stack[--head]) - fval(stack[++head]);
					stack[--head] = (ftmp > 0) ? Function.ONE : (ftmp == 0 ? Function.ZERO : Function.M_ONE);
					break;
				}

			//DOUBLE ARITHMETICS
				case (byte) 0x40: { // dadd
					Object atmp = Dval(dval(stack[--head]) + dval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x41: { // dsub
					Object atmp = Dval(dval(stack[--head]) - dval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x42: { // dmul
					Object atmp = Dval(dval(stack[--head]) * dval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x43: { // ddiv
					Object atmp = Dval(dval(stack[--head]) / dval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x44: { // dmod
					Object atmp = Dval(dval(stack[--head]) % dval(stack[++head]));
					stack[--head] = atmp;
					break;
				}
				case (byte) 0x45: { // dneg
					stack[head] = Dval(-dval(stack[head]));
					break;
				}
				case (byte) 0x46: { // dcmp
					double dtmp = dval(stack[--head]) - dval(stack[++head]);
					stack[--head] = (dtmp > 0) ? Function.ONE : (dtmp == 0 ? Function.ZERO : Function.M_ONE);
					break;
				}

			//LOCALS LOADERS AND SAVERS
				case (byte) 0x50: // load_0
				case (byte) 0x51: // load_1
				case (byte) 0x52: // load_2
				case (byte) 0x53: // load_3
				case (byte) 0x54: // load_4
				case (byte) 0x55: // load_5
				case (byte) 0x56: // load_6
				case (byte) 0x57: // load_7
					stack[++head] = locals[instr & 7]; break;
				case (byte) 0x3D: { //load <ubyte>
					stack[++head] = locals[code[ct++] & 0xff];
					break;
				}
				//variable savers
				case (byte) 0x58: // store_0
				case (byte) 0x59: // store_1
				case (byte) 0x5A: // store_2
				case (byte) 0x5B: // store_3
				case (byte) 0x5C: // store_4
				case (byte) 0x5D: // store_5
				case (byte) 0x5E: // store_6
				case (byte) 0x5F:{// store_7
					locals[instr & 7] = stack[head];
					head--;
					break;
				}
				case (byte) 0x3E: { //store <ubyte>
					locals[code[ct++] & 0xff] = stack[head];
					head--;
					break;
				}

			//BRANCHING
				case (byte) 0x61: { //ifeq <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) == 0) ct += itmp;
					break;
				}
				case (byte) 0x62: { //ifne <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) != 0) ct += itmp;
					break;
				}
				case (byte) 0x63: { //iflt <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) < 0) ct += itmp;
					break;
				}
				case (byte) 0x64: { //ifge <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) >= 0) ct += itmp;
					break;
				}
				case (byte) 0x65: { //ifgt <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) > 0) ct += itmp;
					break;
				}
				case (byte) 0x66: { //ifle <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (ival(stack[head--]) <= 0) ct += itmp;
					break;
				}
				case (byte) 0x67: { //goto <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					ct += itmp;
					break;
				}
				case (byte) 0x68: { //ifnull <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (stack[head--] == null) ct += itmp;
					break;
				}
				case (byte) 0x69: { //ifnnull <short>
					int itmp = (code[ct++]) << 8 | (code[ct++] & 0xff);
					if (stack[head--] != null) ct += itmp;
					break;
				}

			//FUNCTION CALLS
				case (byte) 0x70: // call_0
				case (byte) 0x71: // call_1
				case (byte) 0x72: // call_2
				case (byte) 0x73: // call_3
				case (byte) 0x74: // call_4
				case (byte) 0x75: // call_5
				case (byte) 0x76: // call_6
				case (byte) 0x77: // call_7
				case (byte) 0x78: // calv_0
				case (byte) 0x79: // calv_1
				case (byte) 0x7A: // calv_2
				case (byte) 0x7B: // calv_3
				case (byte) 0x7C: // calv_4
				case (byte) 0x7D: // calv_5
				case (byte) 0x7E: // calv_6
				case (byte) 0x7F: {//calv_7
					int paramlen = instr & 7;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					stack[head] = ((Function)stack[head]).call(c, params);
					if ((instr & 8) != 0) head--;
					break;
				}
				case (byte) 0x4D: {//call <ubyte>
					int paramlen = code[ct++] & 0xff;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					stack[head] = ((Function)stack[head]).call(c, params);
					break;
				}
				case (byte) 0x4E: {//calv <ubyte>
					int paramlen = code[ct++] & 0xff;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					((Function)stack[head]).call(c, params);
					head--;
					break;
				}

			//ARRAY INSTRUCTIONS
				case (byte) 0xF0: {// newarray
					stack[head] = new Object[ival(stack[head])];
					break;
				}
				case (byte) 0xF1: {// aload
					int at = ival(stack[head]);
					head--;
					stack[head] = ((Object[])stack[head])[at];
					break;
				}
				case (byte) 0xF2: {// astore
					Object val = stack[head];
					int at = ival(stack[head-1]);
					Object[] array = (Object[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case (byte) 0xF3: {// alen
					stack[head] = Ival(((Object[])stack[head]).length);
					break;
				}
				case (byte) 0xF4: {// newba
					stack[head] = new byte[ival(stack[head])];
					break;
				}
				case (byte) 0xF5: {// baload
					int at = ival(stack[head]);
					head--;
					stack[head] = Ival( ((byte[])stack[head])[at] );
					break;
				}
				case (byte) 0xF6: {// bastore
					int val = ival(stack[head]);
					int at = ival(stack[head-1]);
					byte[] array = (byte[])stack[head-2];
					array[at] = (byte)val;
					head -= 3;
					break;
				}
				case (byte) 0xF7: {// balen
					stack[head] = Ival(((byte[])stack[head]).length);
					break;
				}
				case (byte) 0xF8: {// newca
					stack[head] = new char[ival(stack[head])];
					break;
				}
				case (byte) 0xF9: {// caload
					int at = ival(stack[head]);
					head--;
					stack[head] = Ival( ((char[])stack[head])[at] );
					break;
				}
				case (byte) 0xFA: {// castore
					int val = ival(stack[head]);
					int at = ival(stack[head-1]);
					char[] array = (char[])stack[head-2];
					array[at] = (char)val;
					head -= 3;
					break;
				}
				case (byte) 0xFB: {// calen
					stack[head] = Ival(((char[])stack[head]).length);
					break;
				}

			//OTHERS
				case (byte) 0x4F: {// acmp
					boolean btmp = stack[head] == null ? stack[head-1] == null : stack[head-1].equals(stack[head]);
					stack[--head] = Ival(!btmp);
					break;
				}
				case (byte) 0x1D: // throw
					throw (Exception)stack[head];
				case (byte) 0x1E: // ret_null
					return null;
				case (byte) 0x1F: // return
					return stack[head];
				case (byte) 0x2D: { //dup
					stack[head+1] = stack[head];
					head++;
					break;
				}
				case (byte) 0x2E: { //dup2
					stack[head+2] = stack[head+1] = stack[head];
					head += 2;
					break;
				}
				case (byte) 0x2F: { //swap
					Object atmp = stack[head-1];
					stack[head-1] = stack[head];
					stack[head] = atmp;
					break;
				}
				case (byte) 0x3F: { //ldc <ushort>
					stack[++head] = cpool[((code[ct++] & 0xff) << 8) | (code[ct++] & 0xff)];
					break;
				}
				case (byte) 0x60: //pop
					head--;
					break;
				case (byte) 0x6E: //bipush <byte>
					stack[++head] = Ival(code[ct++]);
					break;
				case (byte) 0x6F: //sipush <short>
					stack[++head] = Ival((code[ct++] << 8) | (code[ct++]& 0xff));
					break;
			} /* the big switch */
		} /* the great while */
	}

	public String toString() {
		if (libname != null) return libname+':'+signature;
		else return signature;
	}
}

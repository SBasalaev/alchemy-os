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

package alchemy.evm;

import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.Process;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;

/**
 * Ether Virtual Machine.
 * @author Sergey Basalaev
 */
class EtherFunction extends Function {

	private final int stacksize;
	private final int localsize;
	private final byte[] bcode;
	private final char[] dbgtable;
	private final char[] errtable;
	private final Object[] cpool;
	private final String libname;

	public EtherFunction(String libname, String funcname, Object[] cpool, int stacksize, int localsize, byte[] code, char[] dbgtable, char[] errtable) {
		super(funcname);
		this.libname = libname;
		this.stacksize = stacksize;
		this.localsize = localsize;
		this.bcode = code;
		this.cpool = cpool;
		this.dbgtable = dbgtable;
		this.errtable = errtable;
	}

	public Object invoke(Process p, Object[] args) throws AlchemyException {
		//initializing
		final Object[] stack = new Object[localsize+stacksize];
		System.arraycopy(args, 0, stack, 0, args.length);
		int head = localsize-1;
		final byte[] code = this.bcode;
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
					stack[head] = Int32.M_ONE;
					break;
				}
				case Opcodes.ICONST_0: {
					head++;
					stack[head] = Int32.ZERO;
					break;
				}
				case Opcodes.ICONST_1: {
					head++;
					stack[head] = Int32.ONE;
					break;
				}
				case Opcodes.ICONST_2: {
					head++;
					stack[head] = Int32.toInt32(2);
					break;
				}
				case Opcodes.ICONST_3: {
					head++;
					stack[head] = Int32.toInt32(3);
					break;
				}
				case Opcodes.ICONST_4: {
					head++;
					stack[head] = Int32.toInt32(4);
					break;
				}
				case Opcodes.ICONST_5: {
					head++;
					stack[head] = Int32.toInt32(5);
					break;
				}
				case Opcodes.LCONST_0: {
					head++;
					stack[head] = new Int64(0L);
					break;
				}
				case Opcodes.LCONST_1: {
					head++;
					stack[head] = new Int64(1L);
					break;
				}
				case Opcodes.FCONST_0: {
					head++;
					stack[head] = new Float32(0f);
					break;
				}
				case Opcodes.FCONST_1: {
					head++;
					stack[head] = new Float32(1f);
					break;
				}
				case Opcodes.FCONST_2: {
					head++;
					stack[head] = new Float32(2f);
					break;
				}
				case Opcodes.DCONST_0: {
					head++;
					stack[head] = new Float64(0d);
					break;
				}
				case Opcodes.DCONST_1: {
					head++;
					stack[head] = new Float64(1d);
					break;
				}
				
			//CONVERSIONS
				case Opcodes.I2L: {
					stack[head] = new Int64(((Int32)(stack[head])).value);
					break;
				}
				case Opcodes.I2F: {
					stack[head] = new Float32(((Int32)stack[head]).value);
					break;
				}
				case Opcodes.I2D: {
					stack[head] = new Float64(((Int32)stack[head]).value);
					break;
				}
				case Opcodes.L2F: {
					stack[head] = new Float32(((Int64)stack[head]).value);
					break;
				}
				case Opcodes.L2D: {
					stack[head] = new Float64(((Int64)stack[head]).value);
					break;
				}
				case Opcodes.L2I: {
					stack[head] = Int32.toInt32((int)((Int64)stack[head]).value);
					break;
				}
				case Opcodes.F2D: {
					stack[head] = new Float64(((Float32)stack[head]).value);
					break;
				}
				case Opcodes.F2I: {
					stack[head] = Int32.toInt32((int)((Float32)stack[head]).value);
					break;
				}
				case Opcodes.F2L: {
					stack[head] = new Int64((long)((Float32)stack[head]).value);
					break;
				}
				case Opcodes.D2I: {
					stack[head] = Int32.toInt32((int)((Float64)stack[head]).value);
					break;
				}
				case Opcodes.D2L: {
					stack[head] = new Int64((long)((Float64)stack[head]).value);
					break;
				}
				case Opcodes.D2F: {
					stack[head] = new Float32((float)((Float64)stack[head]).value);
					break;
				}
				case Opcodes.I2C: {
					stack[head] = Int32.toInt32((char)((Int32)stack[head]).value);
					break;
				}
				case Opcodes.I2B: {
					stack[head] = Int32.toInt32((byte)((Int32)stack[head]).value);
					break;
				}
				case Opcodes.I2S: {
					stack[head] = Int32.toInt32((short)((Int32)stack[head]).value);
					break;
				}

			//INTEGER ARITHMETICS
				case Opcodes.IADD: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value + ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.ISUB: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value - ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IMUL: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value * ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IDIV: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value / ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IMOD: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value % ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.INEG: {
					stack[head] = Int32.toInt32(-((Int32)stack[head]).value);
					break;
				}
				case Opcodes.ICMP: {
					head--;
					int itmp = ((Int32)stack[head]).value - ((Int32)stack[head+1]).value;
					stack[head] = (itmp > 0) ? Int32.ONE : (itmp == 0 ? Int32.ZERO : Int32.M_ONE);
					break;
				}
				case Opcodes.ISHL: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value << ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.ISHR: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value >> ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IUSHR: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value >>> ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IAND: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value & ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IOR: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value | ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.IXOR: {
					head--;
					stack[head] = Int32.toInt32(((Int32)stack[head]).value ^ ((Int32)stack[head+1]).value);
					break;
				}

			//LONG ARITHMETICS
				case Opcodes.LADD: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value + ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LSUB: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value - ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LMUL: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value * ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LDIV: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value / ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LMOD:  {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value % ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LNEG: {
					stack[head] = new Int64(-((Int64)stack[head]).value);
					break;
				}
				case Opcodes.LCMP: {
					head--;
					long ltmp = ((Int64)stack[head]).value - ((Int64)stack[head+1]).value;
					stack[head] = (ltmp > 0L) ? Int32.ONE : (ltmp == 0L ? Int32.ZERO : Int32.M_ONE);
					break;
				}
				case Opcodes.LSHL: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value << ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.LSHR: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value >> ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.LUSHR: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value >>> ((Int32)stack[head+1]).value);
					break;
				}
				case Opcodes.LAND: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value & ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LOR: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value | ((Int64)stack[head+1]).value);
					break;
				}
				case Opcodes.LXOR: {
					head--;
					stack[head] = new Int64(((Int64)stack[head]).value ^ ((Int64)stack[head+1]).value);
					break;
				}

			//FLOAT ARITHMETICS
				case Opcodes.FADD: {
					head--;
					stack[head] = new Float32(((Float32)stack[head]).value + ((Float32)stack[head+1]).value);
					break;
				}
				case Opcodes.FSUB: {
					head--;
					stack[head] = new Float32(((Float32)stack[head]).value - ((Float32)stack[head+1]).value);
					break;
				}
				case Opcodes.FMUL: {
					head--;
					stack[head] = new Float32(((Float32)stack[head]).value * ((Float32)stack[head+1]).value);
					break;
				}
				case Opcodes.FDIV: {
					head--;
					stack[head] = new Float32(((Float32)stack[head]).value / ((Float32)stack[head+1]).value);
					break;
				}
				case Opcodes.FMOD: {
					head--;
					stack[head] = new Float32(((Float32)stack[head]).value % ((Float32)stack[head+1]).value);
					break;
				}
				case Opcodes.FNEG: {
					stack[head] = new Float32(-((Float32)stack[head]).value);
					break;
				}
				case Opcodes.FCMP: {
					head--;
					float ftmp = ((Float32)stack[head]).value - ((Float32)stack[head+1]).value;
					stack[head] = (ftmp > 0) ? Int32.ONE : (ftmp == 0 ? Int32.ZERO : Int32.M_ONE);
					break;
				}

			//DOUBLE ARITHMETICS
				case Opcodes.DADD: {
					head--;
					stack[head] = new Float64(((Float64)stack[head]).value + ((Float64)stack[head+1]).value);
					break;
				}
				case Opcodes.DSUB: {
					head--;
					stack[head] = new Float64(((Float64)stack[head]).value - ((Float64)stack[head+1]).value);
					break;
				}
				case Opcodes.DMUL: {
					head--;
					stack[head] = new Float64(((Float64)stack[head]).value * ((Float64)stack[head+1]).value);
					break;
				}
				case Opcodes.DDIV: {
					head--;
					stack[head] = new Float64(((Float64)stack[head]).value / ((Float64)stack[head+1]).value);
					break;
				}
				case Opcodes.DMOD: {
					head--;
					stack[head] = new Float64(((Float64)stack[head]).value % ((Float64)stack[head+1]).value);
					break;
				}
				case Opcodes.DNEG: {
					stack[head] = new Float64(-((Float64)stack[head]).value);
					break;
				}
				case Opcodes.DCMP: {
					head--;
					double dtmp = ((Float64)stack[head]).value - ((Float64)(stack[head+1])).value;
					stack[head] = (dtmp > 0) ? Int32.ONE : (dtmp == 0 ? Int32.ZERO : Int32.M_ONE);
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
					stack[head] = stack[instr & 7];
					break;
				}
				case Opcodes.LOAD: { //load <ubyte>
					head++;
					stack[head] = stack[code[ct] & 0xff];
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
					stack[instr & 7] = stack[head];
					head--;
					break;
				}
				case Opcodes.STORE: { //store <ubyte>
					stack[code[ct] & 0xff] = stack[head];
					ct++;
					head--;
					break;
				}

			//BRANCHING
				case Opcodes.IFEQ: { //ifeq <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value == 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFNE: { //ifne <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value != 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFLT: { //iflt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value < 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFGE: { //ifge <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value >= 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFGT: { //ifgt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value > 0) ct = itmp;
					head--;
					break;
				}
				case Opcodes.IFLE: { //ifle <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head]).value <= 0) ct = itmp;
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
					if (((Int32)stack[head-1]).value < ((Int32)stack[head]).value) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPGE: { //if_icmpge <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head-1]).value >= ((Int32)stack[head]).value) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPGT: { //if_icmpgt <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head-1]).value > ((Int32)stack[head]).value) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ICMPLE: { //if_icmple <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (((Int32)stack[head-1]).value <= ((Int32)stack[head]).value) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ACMPEQ: { //if_acmpeq <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (stack[head-1] != null
							? stack[head-1].equals(stack[head])
							: stack[head] == null) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.IF_ACMPNE: { //if_acmpne <ushort>
					int itmp = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					ct += 2;
					if (stack[head-1] != null
							? !stack[head-1].equals(stack[head])
							: stack[head] != null) ct = itmp;
					head -= 2;
					break;
				}
				case Opcodes.JSR: { //jsr <ushort>
					head++;
					stack[head] = Int32.toInt32(ct+2);
					ct = (code[ct] & 0xff) << 8 | (code[ct+1] & 0xff);
					break;					
				}
				case Opcodes.RET: { //ret
					ct = ((Int32)stack[head]).value;
					head--;
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
					stack[head] = ((Function)stack[head]).invoke(p, params);
					if ((instr & 8) != 0) head--;
					break;
				}
				case Opcodes.CALL: {//call <ubyte>
					int paramlen = code[ct] & 0xff;
					ct++;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					stack[head] = ((Function)stack[head]).invoke(p, params);
					break;
				}
				case Opcodes.CALV: {//calv <ubyte>
					int paramlen = code[ct] & 0xff;
					ct++;
					Object[] params = new Object[paramlen];
					head -= paramlen;
					System.arraycopy(stack, head+1, params, 0, paramlen);
					((Function)stack[head]).invoke(p, params);
					head--;
					break;
				}

			//ARRAY INSTRUCTIONS
				case Opcodes.NEWAA: {
					stack[head] = new Object[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWBA: {
					stack[head] = new byte[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWCA: {
					stack[head] = new char[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWZA: {
					stack[head] = new boolean[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWSA: {
					stack[head] = new short[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWIA: {
					stack[head] = new int[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWLA: {
					stack[head] = new long[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWFA: {
					stack[head] = new float[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.NEWDA: {
					stack[head] = new double[((Int32)stack[head]).value];
					break;
				}
				case Opcodes.AALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = ((Object[])stack[head])[at];
					break;
				}
				case Opcodes.BALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = Int32.toInt32( ((byte[])stack[head])[at] );
					break;
				}
				case Opcodes.CALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = Int32.toInt32( ((char[])stack[head])[at] );
					break;
				}
				case Opcodes.ZALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = ((boolean[])stack[head])[at] ? Int32.ONE : Int32.ZERO;
					break;
				}
				case Opcodes.SALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = Int32.toInt32( ((short[])stack[head])[at] );
					break;
				}
				case Opcodes.IALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = Int32.toInt32( ((int[])stack[head])[at] );
					break;
				}
				case Opcodes.LALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = new Int64( ((long[])stack[head])[at] );
					break;
				}
				case Opcodes.FALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = new Float32( ((float[])stack[head])[at] );
					break;
				}
				case Opcodes.DALOAD: {
					int at = ((Int32)stack[head]).value;
					head--;
					stack[head] = new Float64( ((double[])stack[head])[at] );
					break;
				}
				case Opcodes.AASTORE: {
					Object val = stack[head];
					int at = ((Int32)stack[head-1]).value;
					Object[] array = (Object[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.BASTORE: {
					int val = ((Int32)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					byte[] array = (byte[])stack[head-2];
					array[at] = (byte)val;
					head -= 3;
					break;
				}
				case Opcodes.CASTORE: {
					char val = (char) ((Int32)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					char[] array = (char[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.ZASTORE: {
					boolean val = stack[head] != Int32.ZERO;
					int at = ((Int32)stack[head-1]).value;
					boolean[] array = (boolean[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.SASTORE: {
					short val = (short)((Int32)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					short[] array = (short[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.IASTORE: {
					int val = ((Int32)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					int[] array = (int[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.FASTORE: {
					float val = ((Float32)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					float[] array = (float[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.DASTORE: {
					double val = ((Float64)stack[head]).value;
					int at = ((Int32)stack[head-1]).value;
					double[] array = (double[])stack[head-2];
					array[at] = val;
					head -= 3;
					break;
				}
				case Opcodes.AALEN: {
					stack[head] = Int32.toInt32(((Object[])stack[head]).length);
					break;
				}
				case Opcodes.BALEN: {
					stack[head] = Int32.toInt32(((byte[])stack[head]).length);
					break;
				}
				case Opcodes.CALEN: {
					stack[head] = Int32.toInt32(((char[])stack[head]).length);
					break;
				}
				case Opcodes.ZALEN: {
					stack[head] = Int32.toInt32(((boolean[])stack[head]).length);
					break;
				}
				case Opcodes.SALEN: {
					stack[head] = Int32.toInt32(((short[])stack[head]).length);
					break;
				}
				case Opcodes.IALEN: {
					stack[head] = Int32.toInt32(((int[])stack[head]).length);
					break;
				}
				case Opcodes.LALEN: {
					stack[head] = Int32.toInt32(((long[])stack[head]).length);
					break;
				}
				case Opcodes.FALEN: {
					stack[head] = Int32.toInt32(((float[])stack[head]).length);
					break;
				}
				case Opcodes.DALEN: {
					stack[head] = Int32.toInt32(((double[])stack[head]).length);
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
					int val = ((Int32)stack[head]).value;
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
					int val = ((Int32)stack[head]).value;
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
					stack[head] = eq ? Int32.ZERO : Int32.ONE;
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
					stack[head+2] = stack[head];
					stack[head+1] = stack[head-1];
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
				case Opcodes.POP: {
					head--;
					break;
				}
				case Opcodes.BIPUSH: { //bipush <byte>
					head++;
					stack[head] = Int32.toInt32(code[ct]);
					ct++;
					break;
				}
				case Opcodes.SIPUSH: { //sipush <short>
					head++;
					stack[head] = Int32.toInt32((code[ct] << 8) | (code[ct+1]& 0xff));
					ct += 2;
					break;
				}
				case Opcodes.IINC: { //iinc <ubyte> <byte>
					int idx = code[ct] & 0xff;
					ct++;
					int inc = code[ct];
					ct++;
					stack[idx] = Int32.toInt32(((Int32)stack[idx]).value + inc);
				}
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
				ae.addTraceElement(this.toString(), cpool[dbgtable[0]]+":"+srcline);
			} else {
				ae.addTraceElement(this.toString(), "+"+ct);
			}
			// catching or rethrowing
			int jumpto = -1;
			if (errtable != null) {
				for (int i=0; i < errtable.length && jumpto < 0; i += 4)
				if (ct >= errtable[i] && ct <= errtable[i+1]) {
					jumpto = errtable[i+2];
					head = localsize + errtable[i+3];
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
		if (libname != null) return libname+':'+name;
		else return name;
	}
}

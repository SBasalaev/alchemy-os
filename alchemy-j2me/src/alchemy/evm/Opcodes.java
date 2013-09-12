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

/**
 * Assembler opcodes.
 * @author Sergey Basalaev
 */
public interface Opcodes {
	int LFLAG_SONAME = 1;  /* Library has soname. */
	int LFLAG_DEPS = 2;    /* Library has dependencies. */
	
	int FFLAG_SHARED = 1;  /* Function is shared. */
	int FFLAG_RELOCS = 2;  /* Function has relocation table. */
	int FFLAG_LNUM   = 4;  /* Function has line number table. */
	int FFLAG_ERRTBL = 8;  /* Function has error table. */
	
	byte NOP         = (byte)0x00;
	byte ACONST_NULL = (byte)0x01;
	byte ICONST_M1   = (byte)0x02;
	byte ICONST_0    = (byte)0x03;
	byte ICONST_1    = (byte)0x04;
	byte ICONST_2    = (byte)0x05;
	byte ICONST_3    = (byte)0x06;
	byte ICONST_4    = (byte)0x07;
	byte ICONST_5    = (byte)0x08;
	byte LCONST_0    = (byte)0x09;
	byte LCONST_1    = (byte)0x0A;
	byte FCONST_0    = (byte)0x0B;
	byte FCONST_1    = (byte)0x0C;
	byte FCONST_2    = (byte)0x0D;
	byte DCONST_0    = (byte)0x0E;
	byte DCONST_1    = (byte)0x0F;
	byte IADD        = (byte)0x10;
	byte ISUB        = (byte)0x11;
	byte IMUL        = (byte)0x12;
	byte IDIV        = (byte)0x13;
	byte IMOD        = (byte)0x14;
	byte INEG        = (byte)0x15;
	byte ICMP        = (byte)0x16;
	byte ISHL        = (byte)0x17;
	byte ISHR        = (byte)0x18;
	byte IUSHR       = (byte)0x19;
	byte IAND        = (byte)0x1A;
	byte IOR         = (byte)0x1B;
	byte IXOR        = (byte)0x1C;
	byte I2B         = (byte)0x1D;
	byte RET_NULL    = (byte)0x1E;
	byte RETURN      = (byte)0x1F;
	byte LADD        = (byte)0x20;
	byte LSUB        = (byte)0x21;
	byte LMUL        = (byte)0x22;
	byte LDIV        = (byte)0x23;
	byte LMOD        = (byte)0x24;
	byte LNEG        = (byte)0x25;
	byte LCMP        = (byte)0x26;
	byte LSHL        = (byte)0x27;
	byte LSHR        = (byte)0x28;
	byte LUSHR       = (byte)0x29;
	byte LAND        = (byte)0x2A;
	byte LOR         = (byte)0x2B;
	byte LXOR        = (byte)0x2C;
	byte DUP         = (byte)0x2D;
	byte DUP2        = (byte)0x2E;
	byte SWAP        = (byte)0x2F;
	byte FADD        = (byte)0x30;
	byte FSUB        = (byte)0x31;
	byte FMUL        = (byte)0x32;
	byte FDIV        = (byte)0x33;
	byte FMOD        = (byte)0x34;
	byte FNEG        = (byte)0x35;
	byte FCMP        = (byte)0x36;
	byte I2L         = (byte)0x37;
	byte I2F         = (byte)0x38;
	byte I2D         = (byte)0x39;
	byte L2F         = (byte)0x3A;
	byte L2D         = (byte)0x3B;
	byte L2I         = (byte)0x3C;
	byte LOAD        = (byte)0x3D;
	byte STORE       = (byte)0x3E;
	byte LDC         = (byte)0x3F;
	byte DADD        = (byte)0x40;
	byte DSUB        = (byte)0x41;
	byte DMUL        = (byte)0x42;
	byte DDIV        = (byte)0x43;
	byte DMOD        = (byte)0x44;
	byte DNEG        = (byte)0x45;
	byte DCMP        = (byte)0x46;
	byte F2D         = (byte)0x47;
	byte F2I         = (byte)0x48;
	byte F2L         = (byte)0x49;
	byte D2I         = (byte)0x4A;
	byte D2L         = (byte)0x4B;
	byte D2F         = (byte)0x4C;
	byte CALL        = (byte)0x4D;
	byte CALV        = (byte)0x4E;
	byte ACMP        = (byte)0x4F;
	byte LOAD_0      = (byte)0x50;
	byte LOAD_1      = (byte)0x51;
	byte LOAD_2      = (byte)0x52;
	byte LOAD_3      = (byte)0x53;
	byte LOAD_4      = (byte)0x54;
	byte LOAD_5      = (byte)0x55;
	byte LOAD_6      = (byte)0x56;
	byte LOAD_7      = (byte)0x57;
	byte STORE_0     = (byte)0x58;
	byte STORE_1     = (byte)0x59;
	byte STORE_2     = (byte)0x5A;
	byte STORE_3     = (byte)0x5B;
	byte STORE_4     = (byte)0x5C;
	byte STORE_5     = (byte)0x5D;
	byte STORE_6     = (byte)0x5E;
	byte STORE_7     = (byte)0x5F;
	byte POP         = (byte)0x60;
	byte IFEQ        = (byte)0x61;
	byte IFNE        = (byte)0x62;
	byte IFLT        = (byte)0x63;
	byte IFGE        = (byte)0x64;
	byte IFGT        = (byte)0x65;
	byte IFLE        = (byte)0x66;
	byte GOTO        = (byte)0x67;
	byte IFNULL      = (byte)0x68;
	byte IFNNULL     = (byte)0x69;
	byte IF_ICMPLT   = (byte)0x6A;
	byte IF_ICMPGE   = (byte)0x6B;
	byte IF_ICMPGT   = (byte)0x6C;
	byte IF_ICMPLE   = (byte)0x6D;
	byte BIPUSH      = (byte)0x6E;
	byte SIPUSH      = (byte)0x6F;
	byte CALL_0      = (byte)0x70;
	byte CALL_1      = (byte)0x71;
	byte CALL_2      = (byte)0x72;
	byte CALL_3      = (byte)0x73;
	byte CALL_4      = (byte)0x74;
	byte CALL_5      = (byte)0x75;
	byte CALL_6      = (byte)0x76;
	byte CALL_7      = (byte)0x77;
	byte CALV_0      = (byte)0x78;
	byte CALV_1      = (byte)0x79;
	byte CALV_2      = (byte)0x7A;
	byte CALV_3      = (byte)0x7B;
	byte CALV_4      = (byte)0x7C;
	byte CALV_5      = (byte)0x7D;
	byte CALV_6      = (byte)0x7E;
	byte CALV_7      = (byte)0x7F;
	
	byte IINC        = (byte)0xD3;
	byte JSR         = (byte)0xD4;
	byte RET         = (byte)0xD5;
	byte IF_ACMPEQ   = (byte)0xD6;
	byte IF_ACMPNE   = (byte)0xD7;
	byte NEWZA       = (byte)0xD8;
	byte ZALOAD      = (byte)0xD9;
	byte ZASTORE     = (byte)0xDA;
	byte ZALEN       = (byte)0xDB;
	byte NEWSA       = (byte)0xDC;
	byte SALOAD      = (byte)0xDD;
	byte SASTORE     = (byte)0xDE;
	byte SALEN       = (byte)0xDF;
	byte NEWIA       = (byte)0xE0;
	byte IALOAD      = (byte)0xE1;
	byte IASTORE     = (byte)0xE2;
	byte IALEN       = (byte)0xE3;
	byte NEWLA       = (byte)0xE4;
	byte LALOAD      = (byte)0xE5;
	byte LASTORE     = (byte)0xE6;
	byte LALEN       = (byte)0xE7;
	byte NEWFA       = (byte)0xE8;
	byte FALOAD      = (byte)0xE9;
	byte FASTORE     = (byte)0xEA;
	byte FALEN       = (byte)0xEB;
	byte NEWDA       = (byte)0xEC;
	byte DALOAD      = (byte)0xED;
	byte DASTORE     = (byte)0xEE;
	byte DALEN       = (byte)0xEF;
	byte NEWAA       = (byte)0xF0;
	byte AALOAD      = (byte)0xF1;
	byte AASTORE     = (byte)0xF2;
	byte AALEN       = (byte)0xF3;
	byte NEWBA       = (byte)0xF4;
	byte BALOAD      = (byte)0xF5;
	byte BASTORE     = (byte)0xF6;
	byte BALEN       = (byte)0xF7;
	byte NEWCA       = (byte)0xF8;
	byte CALOAD      = (byte)0xF9;
	byte CASTORE     = (byte)0xFA;
	byte CALEN       = (byte)0xFB;
	byte TABLESWITCH = (byte)0xFC;
	byte LOOKUPSWITCH= (byte)0xFD;
	byte I2C         = (byte)0xFE;
	byte I2S         = (byte)0xFF;
}
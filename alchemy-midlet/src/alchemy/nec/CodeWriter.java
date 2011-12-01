/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec;

import alchemy.core.Context;
import alchemy.l10n.I18N;
import alchemy.nec.tree.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Sergey Basalaev
 */
class CodeWriter implements ExprVisitor {

	private final Context c;
	private Vector objects = new Vector();

	/**
	 * Address of instruction we currently write.
	 * Used to collect relocations.
	 */
	private int addr;

	private Vector relocs = new Vector();

	public CodeWriter(Context c, Unit u) {
		this.c = c;
		objects = new Vector();
		//collect constants
		ConstCollector cc = new ConstCollector();
		for (Enumeration e = u.funcs.elements(); e.hasMoreElements(); ) {
			Func f = (Func)e.nextElement();
			cc.visitFunc(f, objects);
		}
		for (Enumeration e = u.funcs.elements(); e.hasMoreElements(); ) {
			objects.addElement(e.nextElement());
		}
	}

	public void write(OutputStream outstream) throws Exception {
		DataOutputStream out = new DataOutputStream(outstream);
		out.writeInt(0xC0DE0101);
		out.writeByte(0);
		int size = objects.size();
		out.writeShort(size);
		for (int i=0; i<size; i++) {
			Object obj = objects.elementAt(i);
			if (obj == null) {
				out.writeByte('0');
			} else if (obj instanceof Integer) {
				out.writeByte('i');
				out.writeInt(((Integer)obj).intValue());
			} else if (obj instanceof Long) {
				out.writeByte('l');
				out.writeLong(((Long)obj).longValue());
			} else if (obj instanceof Float) {
				out.writeByte('f');
				out.writeFloat(((Float)obj).floatValue());
			} else if (obj instanceof Double) {
				out.writeByte('d');
				out.writeDouble(((Double)obj).doubleValue());
			} else if (obj instanceof String) {
				out.writeByte('S');
				out.writeUTF((String)obj);
			} else if (obj instanceof Func) {
				Func func = (Func)obj;
				if (func.body != null) {
					visitFunc(func, out);
				} else {
					visitUndef(func, out);
				}
			}
		}
		out.flush();
	}

	public void visitFunc(Func f, DataOutputStream out) throws Exception {
		FuncData data = new FuncComputer(true).visitFunc(f);
		if (data.stackmax > 255 || data.localmax > 255 || data.codesize > 65535)
			throw new Exception(I18N._("Function {0} can not be compiled, too complicated", f.asVar.name));
		out.writeByte(f.asVar.name.charAt(0) == '_' ? 'H' : 'P');
		out.writeShort(objects.indexOf(f.asVar.name));
		out.writeByte(1); //has relocations
		out.writeByte(data.stackmax);
		out.writeByte(data.localmax);
		out.writeShort(data.codesize);
		addr = 0;
		relocs.removeAllElements();
		f.body.accept(this, out);
		if (f.body.rettype().equals(BuiltinType.typeNone)) {
			out.writeByte(0x1e);  // ret_nul
		} else {
			out.writeByte(0x1f);  // return
		}
		out.writeShort(relocs.size());
		for (int i=0; i<relocs.size(); i++) {
			out.writeShort(((Integer)relocs.elementAt(i)).intValue());
		}
	}
	
	public void visitUndef(Func f, DataOutputStream out) throws IOException {
		out.writeByte('U');
		out.writeShort(objects.indexOf(f.asVar.name));
	}

	public void visitALen(ALenExpr alen, Object data) {
		DataOutputStream out = (DataOutputStream)data;
		alen.arrayexpr.accept(this, data);
		try {
			Type type = alen.arrayexpr.rettype();
			if (type.equals(BuiltinType.typeBArray)) {
				out.writeByte(0xf7); //balen
			} else if (type.equals(BuiltinType.typeCArray)) {
				out.writeByte(0xfb); //calen
			} else {
				out.write(0xf3); //alen
			}
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitALoad(ALoadExpr aload, Object data) {
		DataOutputStream out = (DataOutputStream)data;
		aload.arrayexpr.accept(this, data);
		aload.indexexpr.accept(this, data);
		try {
			Type type = aload.arrayexpr.rettype();
			if (type.equals(BuiltinType.typeBArray)) {
				out.writeByte(0xf5); //baload
			} else if (type.equals(BuiltinType.typeCArray)) {
				out.writeByte(0xf9); //caload
			} else {
				out.write(0xf1); //aload
			}
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitAStore(AStoreExpr astore, Object data) {
		DataOutputStream out = (DataOutputStream)data;
		astore.arrayexpr.accept(this, data);
		astore.indexexpr.accept(this, data);
		astore.assignexpr.accept(this, data);
		try {
			Type type = astore.arrayexpr.rettype();
			if (type.equals(BuiltinType.typeBArray)) {
				out.writeByte(0xf6); //bastore
			} else if (type.equals(BuiltinType.typeCArray)) {
				out.writeByte(0xfa); //castore
			} else {
				out.write(0xf2); //astore
			}
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitAssign(AssignExpr assign, Object outobj) {
		DataOutputStream out = (DataOutputStream)outobj;
		assign.expr.accept(this, out);
		try {
			if (assign.var.index < 8) {
				out.writeByte(0x58+assign.var.index); // store_x
				addr++;
			} else {
				out.writeByte(0x3e); //store
				out.writeByte(assign.var.index);
				addr += 2;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	private static int[] binops
			= {'+', '-', '*', '/', '%', 0, 0, Tokenizer.TT_LTLT,
			Tokenizer.TT_GTGT, Tokenizer.TT_GTGTGT, '&', '|', '^'};

	public void visitBinary(BinaryExpr binary, Object data) {
		binary.lvalue.accept(this, data);
		binary.rvalue.accept(this, data);
		try {
			int opcode = 0;
			while (binary.operator != binops[opcode]) opcode++;
			Type type = binary.rettype();
			if (type.equals(BuiltinType.typeInt) || type.equals(BuiltinType.typeBool))
				opcode += 0x10;
			else if (type.equals(BuiltinType.typeLong)) opcode += 0x20;
			else if (type.equals(BuiltinType.typeFloat)) opcode += 0x30;
			else if (type.equals(BuiltinType.typeDouble)) opcode += 0x40;
			DataOutputStream out = (DataOutputStream)data;
			out.writeByte(opcode);
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitBlock(BlockExpr block, Object out) {
		for (Enumeration e = block.exprs.elements(); e.hasMoreElements(); ) {
			Expr expr = (Expr)e.nextElement();
			expr.accept(this, out);
		}
	}

	public void visitCast(CastExpr cast, Object out) {
		cast.expr.accept(this, out);
	}

	public void visitCastPrimitive(CastPrimitiveExpr cast, Object out) {
		cast.expr.accept(this, out);
		try {
			((DataOutputStream)out).writeByte(cast.casttype);
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitComparison(ComparisonExpr comp, Object data) {
		comp.lvalue.accept(this, data);
		comp.rvalue.accept(this, data);
		try {
			DataOutputStream out = (DataOutputStream)data;
			Type type = comp.lvalue.rettype();
			if (type.equals(BuiltinType.typeBool) || type.equals(BuiltinType.typeInt))
				out.writeByte(0x16); //icmp
			else if (type.equals(BuiltinType.typeLong))
				out.writeByte(0x26); //lcmp
			else if (type.equals(BuiltinType.typeFloat))
				out.writeByte(0x36); //fcmp
			else if (type.equals(BuiltinType.typeDouble))
				out.writeByte(0x46); //dcmp
			else
				out.writeByte(0x4f); //acmp
			switch (comp.operator) {
				case Tokenizer.TT_NOTEQ:
					out.writeLong(0x6100040467000103l);
					break;
				case Tokenizer.TT_EQEQ:
					out.writeLong(0x6200040467000103l);
					break;
				case Tokenizer.TT_GTEQ:
					out.writeLong(0x6300040467000103l);
					break;
				case '<':
					out.writeLong(0x6400040467000103l);
					break;
				case Tokenizer.TT_LTEQ:
					out.writeLong(0x6500040467000103l);
					break;
				case '>':
					out.writeLong(0x6600040467000103l);
					break;
			}
			addr += 9;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitConst(ConstExpr cexpr, Object data) {
		try {
			DataOutputStream out = (DataOutputStream)data;
			Object value = cexpr.value;
			boolean inline = false;
			if (value == null) {
				out.writeByte(0x01); // aconst_null
				addr++;
				inline = true;
			} else if (value instanceof Boolean) {
				if (value.equals(Boolean.TRUE))
					out.writeByte(0x04); // iconst_1
				else out.writeByte(0x03); // iconst_0
				addr++;
				inline = true;
			} else if (value instanceof Integer) {
				int ival = ((Integer)value).intValue();
				if (ival >= -1 && ival <= 5) {
					out.writeByte(0x03+ival); //iconst_X
					addr++;
					inline = true;
				} else if (ival >= Byte.MIN_VALUE && ival <= Byte.MAX_VALUE) {
					out.writeByte(0x6E); // bipush
					out.writeByte(ival);
					addr += 2;
					inline = true;
				} else if (ival >= Short.MIN_VALUE && ival <= Short.MAX_VALUE) {
					out.writeByte(0x6F); // sipush
					out.writeShort(ival);
					addr += 3;
					inline = true;
				}
			} else if (value instanceof Long) {
				long lval = ((Long)value).longValue();
				if (lval == 0l) {
					out.writeByte(0x09); // lconst_0
					addr++;
					inline = true;
				} else if (lval == 1l) {
					out.writeByte(0x0A); // lconst_1
					addr++;
					inline = true;
				}
			} else if (value instanceof Float) {
				float fval = ((Float)value).floatValue();
				if (fval == 0f) {
					out.writeByte(0x0B); // fconst_0
					addr++;
					inline = true;
				} else if (fval == 1f) {
					out.writeByte(0x0C); // fconst_1
					addr++;
					inline = true;
				} else if (fval == 2f) {
					out.writeByte(0x0D); // fconst_2
					addr++;
					inline = true;
				}
			} else if (value instanceof Double) {
				double dval = ((Double)value).doubleValue();
				if (dval == 0d) {
					out.writeByte(0x0E); // dconst_0
					addr++;
					inline = true;
				} else if (dval == 1d) {
					out.writeByte(0x0F); // dconst_1
					addr++;
					inline = true;
				}
			}
			if (!inline) {
				out.writeByte(0x3f); // ldc
				addr++;
				relocs.addElement(new Integer(addr));
				out.writeShort(objects.indexOf(cexpr.value));
				addr += 2;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitDiscard(DiscardExpr disc, Object data) {
		disc.expr.accept(this, data);
		try {
			((DataOutputStream)data).writeByte(0x60); //pop
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitFCall(FCallExpr fcall, Object data) {
		try {
			DataOutputStream out = (DataOutputStream)data;
			fcall.fload.accept(this, data);
			for (int i=0; i<fcall.args.length; i++) {
				fcall.args[i].accept(this, data);
			}
			if (fcall.rettype().equals(BuiltinType.typeNone)) {
				out.writeByte(0x78+fcall.args.length); // calv
			} else {
				out.writeByte(0x70+fcall.args.length); // call
			}
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitIf(IfExpr ifexpr, Object data) {
		//  cond;
		// ifeq +(ifexpr+3)
		//  ifexpr;
		// goto +(elseexpr)
		//  elseexpr;
		FuncComputer fc = new FuncComputer(false);
		FuncData fdata;
		ifexpr.ifexpr.accept(fc, fdata = new FuncData());
		int ifsize = fdata.codesize;
		ifexpr.elseexpr.accept(fc, fdata = new FuncData());
		int elsesize = fdata.codesize;
		try {
			DataOutputStream out = (DataOutputStream)data;
			ifexpr.condition.accept(this, data);
			out.writeByte(0x61); //ifeq
			out.writeShort(ifsize+3);
			addr += 3;
			ifexpr.ifexpr.accept(this, data);
			out.writeByte(0x67); //goto
			out.writeShort(elsesize);
			addr += 3;
			ifexpr.elseexpr.accept(this, data);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitNewArray(NewArrayExpr newarray, Object data) {
		newarray.lengthexpr.accept(this, data);
		try {
			DataOutputStream out = (DataOutputStream)data;
			Type type = newarray.rettype();
			if (type.equals(BuiltinType.typeBArray)) {
				out.writeByte(0xf4); //newba
			} else if (type.equals(BuiltinType.typeCArray)) {
				out.writeByte(0xf8); //newca
			} else {
				out.write(0xf0); //newarray
			}
			addr++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitNone(NoneExpr none, Object out) {
		//nothing to write
	}

	public void visitUnary(UnaryExpr unary, Object data) {
		try {
			DataOutputStream out = (DataOutputStream)data;
			unary.expr.accept(this, data);
			if (unary.operator  == '!') {
				// ifeq +4
				// iconst_0
				// goto +1
				// iconst_1
				out.writeLong(0x6100040367000104l);
				addr += 8;
			} else if (unary.operator == '-') {
				Type type = unary.rettype();
				if (type.equals(BuiltinType.typeInt))
					out.writeByte(0x15); //ineg
				else if (type.equals(BuiltinType.typeLong))
					out.writeByte(0x25); //lneg
				else if (type.equals(BuiltinType.typeFloat))
					out.writeByte(0x35); //fneg
				else if (type.equals(BuiltinType.typeDouble))
					out.writeByte(0x45); //dneg
				addr++;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitVar(VarExpr vexpr, Object data) {
		try {
			DataOutputStream out = (DataOutputStream)data;
			if (vexpr.var.index < 8) {
				out.writeByte(0x50+vexpr.var.index); // load_x
				addr++;
			} else {
				out.writeByte(0x3d); //load
				out.writeByte(vexpr.var.index);
				addr += 2;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}

	public void visitWhile(WhileExpr wexpr, Object data) {
		//calculating sizes
		FuncComputer fc = new FuncComputer(false);
		FuncData fdata;
		wexpr.condition.accept(fc, fdata = new FuncData());
		int condsize = fdata.codesize;
		wexpr.body.accept(fc, fdata = new FuncData());
		int bodysize = fdata.codesize;
		try {
			//  cond;
			// ifeq +(body+3)
			//  body;
			// goto -(body+cond+6)
			DataOutputStream out = (DataOutputStream)data;
			wexpr.condition.accept(this, data);
			out.writeByte(0x61); //ifeq
			out.writeShort(bodysize+3);
			addr += 3;
			wexpr.body.accept(this, data);
			out.writeByte(0x67); //goto
			out.writeShort(-6-bodysize-condsize);
			addr += 3;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}
}

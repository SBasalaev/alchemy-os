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

package alchemy.nec;

import alchemy.nec.asm.FuncObject;
import alchemy.nec.asm.FunctionWriter;
import alchemy.nec.asm.Label;
import alchemy.nec.asm.Opcodes;
import alchemy.nec.asm.UnitWriter;
import alchemy.nec.tree.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

// FIXME: variable indices should be assigned first

/**
 *
 * @author Sergey Basalaev
 */
public class EAsmWriter implements ExprVisitor {
	private FunctionWriter writer;
	
	public EAsmWriter() {
	}
	
	public void writeTo(Unit unit, OutputStream out) throws IOException {
		UnitWriter uw = new UnitWriter();
		uw.visitVersion(0x0200);
		Vector funcs = unit.funcs;
		for (int i=0; i<funcs.size(); i++) {
			Func f = (Func)funcs.elementAt(i);
			if (f.body != null && f.hits > 0) {
				writer = uw.visitFunction(f.signature, true, f.type.args.length);
				f.body.accept(this, null);
				if (f.type.rettype.equals(BuiltinType.NONE)) {
					writer.visitInsn(Opcodes.RET_NULL);
				} else {
					writer.visitInsn(Opcodes.RETURN);
				}
				writer.visitEnd();
			}
		}
		uw.writeTo(out);
	}
	
	public Object visitALen(ALenExpr alen, Object unused) {
		alen.arrayexpr.accept(this, unused);
		Type artype = alen.arrayexpr.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.BALEN);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.CALEN);
		} else {
			writer.visitInsn(Opcodes.ALEN);
		}
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object unused) {
		aload.arrayexpr.accept(this, unused);
		aload.indexexpr.accept(this, unused);
		Type artype = aload.arrayexpr.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.BALOAD);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.CALOAD);
		} else {
			writer.visitInsn(Opcodes.ALOAD);
		}
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object unused) {
		astore.arrayexpr.accept(this, unused);
		astore.indexexpr.accept(this, unused);
		astore.assignexpr.accept(this, unused);
		Type artype = astore.arrayexpr.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.BASTORE);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.CASTORE);
		} else {
			writer.visitInsn(Opcodes.ASTORE);
		}
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object unused) {
		assign.expr.accept(this, unused);
		writer.visitVarInsn(Opcodes.STORE, assign.var.index);
		return null;
	}
	
	public Object visitBinary(BinaryExpr binary, Object unused) {
		binary.lvalue.accept(this, unused);
		binary.rvalue.accept(this, unused);
		Type type = binary.rettype();
		switch (binary.operator) {
			case '+':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IADD);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LADD);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FADD);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DADD);
				break;
			case '-':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.ISUB);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LSUB);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FSUB);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DSUB);
				break;
			case '*':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IMUL);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LMUL);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FMUL);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DMUL);
				break;
			case '/':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IDIV);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LDIV);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FDIV);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DDIV);
				break;
			case '%':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IMOD);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LMOD);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FMOD);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DMOD);
				break;
			case '&':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IAND);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LAND);
				break;
			case '|':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IOR);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LOR);
				break;
			case '^':
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IXOR);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LXOR);
				break;
			case Tokenizer.TT_LTLT:
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.ISHL);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LSHL);
				break;
			case Tokenizer.TT_GTGT:
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.ISHR);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LSHR);
				break;
			case Tokenizer.TT_GTGTGT:
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.IUSHR);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LUSHR);
				break;
		}
		return null;
	}

	public Object visitBlock(BlockExpr block, Object unused) {
		for (int i=0; i<block.exprs.size(); i++) {
			Expr expr = (Expr)block.exprs.elementAt(i);
			expr.accept(this, unused);
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object unused) {
		cast.expr.accept(this, unused);
		Type from = cast.expr.rettype();
		Type to = cast.rettype();
		if (from.isSubtypeOf(BuiltinType.INT)) {
			if (to.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.I2L);
			else if (to.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.I2F);
			else if (to.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.I2D);
		} else if (from.isSubtypeOf(BuiltinType.LONG)) {
			if (to.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.L2I);
			else if (to.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.L2F);
			else if (to.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.L2D);
		} else if (from.isSubtypeOf(BuiltinType.FLOAT)) {
			if (to.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.F2L);
			else if (to.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.F2I);
			else if (to.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.F2D);
		} else if (from.isSubtypeOf(BuiltinType.DOUBLE)) {
			if (to.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.D2L);
			else if (to.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.D2F);
			else if (to.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.D2I);
		}
		return null;
	}

	public Object visitComparison(ComparisonExpr cmp, Object unused) {
		cmp.lvalue.accept(this, unused);
		cmp.rvalue.accept(this, unused);
		Type type = Type.commonSupertype(cmp.lvalue.rettype(), cmp.rvalue.rettype());
		if (type.isSubtypeOf(BuiltinType.INT)) {
			writer.visitInsn(Opcodes.ICMP);
		} else if (type.isSubtypeOf(BuiltinType.LONG)) {
			writer.visitInsn(Opcodes.LCMP);
		} else if (type.isSubtypeOf(BuiltinType.FLOAT)) {
			writer.visitInsn(Opcodes.FCMP);
		} else if (type.isSubtypeOf(BuiltinType.DOUBLE)) {
			writer.visitInsn(Opcodes.DCMP);
		} else {
			writer.visitInsn(Opcodes.ACMP);
		}
		Label lfalse = new Label();
		Label lafter = new Label();
		switch (cmp.operator) {
			case '<':
				writer.visitJumpInsn(Opcodes.IFGE, lfalse);
				break;
			case '>':
				writer.visitJumpInsn(Opcodes.IFLE, lfalse);
				break;
			case Tokenizer.TT_LTEQ:
				writer.visitJumpInsn(Opcodes.IFGT, lfalse);
				break;
			case Tokenizer.TT_GTEQ:
				writer.visitJumpInsn(Opcodes.IFLT, lfalse);
				break;
			case Tokenizer.TT_EQEQ:
				writer.visitJumpInsn(Opcodes.IFNE, lfalse);
				break;
			case Tokenizer.TT_NOTEQ:
				writer.visitJumpInsn(Opcodes.IFEQ, lfalse);
				break;
		}
		writer.visitLdcInsn(Boolean.TRUE);
		writer.visitJumpInsn(Opcodes.GOTO, lafter);
		writer.visitLabel(lfalse);
		writer.visitLdcInsn(Boolean.FALSE);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitConcat(ConcatExpr concat, Object unused) {
		if (concat.exprs.size() == 1) {
			Expr str1 = (Expr)concat.exprs.elementAt(0);
			str1.accept(this, unused);
		} else if (concat.exprs.size() == 2) {
			Expr str1 = (Expr)concat.exprs.elementAt(0);
			Expr str2 = (Expr)concat.exprs.elementAt(1);
			writer.visitLdcInsn(new FuncObject("String.concat"));
			str1.accept(this, unused);
			str2.accept(this, unused);
			writer.visitCallInsn(Opcodes.CALL, 2);
		} else {
			writer.visitLdcInsn(new FuncObject("Any.tostr"));
			writer.visitLdcInsn(new FuncObject("StrBuf.append"));
			writer.visitInsn(Opcodes.DUP);
			int l = concat.exprs.size()-2;
			while (l > 1) {
				writer.visitInsn(Opcodes.DUP2);
				l -= 2;
			}
			if (l > 0) {
				writer.visitInsn(Opcodes.DUP);
			}
			writer.visitLdcInsn(new FuncObject("new_strbuf"));
			writer.visitCallInsn(Opcodes.CALL, 0);
			for (int i=0; i<concat.exprs.size(); i++) {
				((Expr)concat.exprs.elementAt(i)).accept(this, unused);
				writer.visitCallInsn(Opcodes.CALL, 2);
			}
			writer.visitCallInsn(Opcodes.CALL, 0);
		}
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object unused) {
		Object obj = cexpr.value;
		if (obj instanceof Func) {
			obj = new FuncObject(((Func)obj).signature);
		}
		writer.visitLdcInsn(obj);
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object unused) {
		disc.expr.accept(this, unused);
		writer.visitInsn(Opcodes.POP);
		return null;
	}

	public Object visitDoWhile(DoWhileExpr wexpr, Object unused) {
		// TODO: comparison do-while
		// TODO: do-while true
		Label lstart = new Label();
		Label lafter = new Label();
		writer.visitLabel(lstart);
		wexpr.body.accept(this, unused);
		wexpr.condition.accept(this, unused);
		writer.visitJumpInsn(Opcodes.IFNE, lstart);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object unused) {
		fcall.fload.accept(this, unused);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, unused);
		}
		if (fcall.rettype().equals(BuiltinType.NONE)) {
			writer.visitCallInsn(Opcodes.CALV, fcall.args.length);
		} else {
			writer.visitCallInsn(Opcodes.CALL, fcall.args.length);
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object unused) {
		// TODO: comparison if
		// TODO: if without else
		Label lelse = new Label();
		Label lafter = new Label();
		ifexpr.condition.accept(this, unused);
		writer.visitJumpInsn(Opcodes.IFEQ, lelse);
		ifexpr.ifexpr.accept(this, unused);
		writer.visitJumpInsn(Opcodes.GOTO, lafter);
		writer.visitLabel(lelse);
		ifexpr.elseexpr.accept(this, unused);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object unused) {
		newarray.lengthexpr.accept(this, unused);
		Type artype = newarray.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.NEWBA);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.NEWCA);
		} else if (artype.isSubtypeOf(BuiltinType.ARRAY)) {
			writer.visitInsn(Opcodes.NEWARRAY);
		}
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object unused) {
		writer.visitLdcInsn(new Integer(newarray.initializers.length));
		Type artype = newarray.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.NEWBA);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.NEWCA);
		} else if (artype.isSubtypeOf(BuiltinType.ARRAY)) {
			writer.visitInsn(Opcodes.NEWARRAY);
		}
		for (int i=0; i<newarray.initializers.length; i++) {
			Expr e = newarray.initializers[i];
			if (e != null) {
				writer.visitInsn(Opcodes.DUP);
				writer.visitLdcInsn(new Integer(i));
				e.accept(this, unused);
				if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
					writer.visitInsn(Opcodes.BASTORE);
				} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
					writer.visitInsn(Opcodes.CASTORE);
				} else if (artype.isSubtypeOf(BuiltinType.ARRAY)) {
					writer.visitInsn(Opcodes.ASTORE);
				}
			}
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object unused) {
		return null;
	}

	public Object visitUnary(UnaryExpr unary, Object unused) {
		Type type = unary.rettype();
		switch (unary.operator) {
			case '+':
				unary.expr.accept(this, unused);
				break;
			case '-':
				unary.expr.accept(this, unused);
				if (type.isSubtypeOf(BuiltinType.INT)) writer.visitInsn(Opcodes.INEG);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LNEG);
				else if (type.isSubtypeOf(BuiltinType.FLOAT)) writer.visitInsn(Opcodes.FNEG);
				else if (type.isSubtypeOf(BuiltinType.DOUBLE)) writer.visitInsn(Opcodes.DNEG);
				break;
			case '~':
				unary.expr.accept(this, unused);
				if (type.isSubtypeOf(BuiltinType.INT)) {
					writer.visitLdcInsn(new Integer(-1));
					writer.visitInsn(Opcodes.IXOR);
				} else if (type.isSubtypeOf(BuiltinType.LONG)) {
					writer.visitLdcInsn(new Long(-1));
					writer.visitInsn(Opcodes.LXOR);
				}
				break;
			case '!':
				writer.visitLdcInsn(new Integer(1));
				unary.expr.accept(this, unused);
				writer.visitInsn(Opcodes.ISUB);
				break;
		}
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object unused) {
		writer.visitVarInsn(Opcodes.LOAD, vexpr.var.index);
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object unused) {
		// TODO: comparison while
		// TODO: while true
		Label lstart = new Label();
		Label lafter = new Label();
		writer.visitLabel(lstart);
		wexpr.condition.accept(this, unused);
		writer.visitJumpInsn(Opcodes.IFEQ, lafter);
		wexpr.body.accept(this, unused);
		writer.visitJumpInsn(Opcodes.GOTO, lstart);
		writer.visitLabel(lafter);
		return null;
	}
}

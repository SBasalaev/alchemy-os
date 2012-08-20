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

import alchemy.core.Function;
import alchemy.core.Int;
import alchemy.evm.Opcodes;
import alchemy.nec.asm.FuncObject;
import alchemy.nec.asm.FunctionWriter;
import alchemy.nec.asm.Label;
import alchemy.nec.asm.UnitWriter;
import alchemy.nec.tree.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

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
				if (type.isSubtypeOf(BuiltinType.INT) || type.equals(BuiltinType.BOOL)) writer.visitInsn(Opcodes.IAND);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LAND);
				break;
			case '|':
				if (type.isSubtypeOf(BuiltinType.INT) || type.equals(BuiltinType.BOOL)) writer.visitInsn(Opcodes.IOR);
				else if (type.isSubtypeOf(BuiltinType.LONG)) writer.visitInsn(Opcodes.LOR);
				break;
			case '^':
				if (type.isSubtypeOf(BuiltinType.INT) || type.equals(BuiltinType.BOOL)) writer.visitInsn(Opcodes.IXOR);
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
	
	/** 
	 * Writes comparison with subsequent jump.
	 * If cond is <code>true</code>, jump is performed when condition
	 * is fulfilled, otherwise jump is performed when condition fails.
	 */
	private void visitCmpInIf(ComparisonExpr cmp, Label jumpto, boolean cond) {
		if (cmp.rvalue instanceof ConstExpr && ((ConstExpr)cmp.rvalue).value == null) {
			// comparison with null
			cmp.lvalue.accept(this, null);
			if (cmp.operator == Tokenizer.TT_EQEQ) {
				writer.visitJumpInsn(cond ? Opcodes.IFNULL : Opcodes.IFNNULL, jumpto);
			} else {
				writer.visitJumpInsn(cond ? Opcodes.IFNNULL : Opcodes.IFNULL, jumpto);
			}
		} else if (cmp.rvalue instanceof ConstExpr && ((ConstExpr)cmp.rvalue).value.equals(Int.ZERO)
		        && cmp.lvalue.rettype().isSubtypeOf(BuiltinType.INT)) {
			// integer comparison with zero
			cmp.lvalue.accept(this, null);
			switch (cmp.operator) {
				case '<':
					writer.visitJumpInsn(cond ? Opcodes.IFLT : Opcodes.IFGE, jumpto);
					break;
				case '>':
					writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLE, jumpto);
					break;
				case Tokenizer.TT_LTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFLE : Opcodes.IFGT, jumpto);
					break;
				case Tokenizer.TT_GTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLT, jumpto);
					break;
				case Tokenizer.TT_EQEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFEQ : Opcodes.IFNE, jumpto);
					break;
				case Tokenizer.TT_NOTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
					break;
			}
		} else if ((cmp.lvalue.rettype().isSubtypeOf(BuiltinType.INT) || cmp.rvalue.rettype().isSubtypeOf(BuiltinType.INT))
				&& cmp.operator != Tokenizer.TT_EQEQ && cmp.operator != Tokenizer.TT_NOTEQ) {
			// integer comparison
			cmp.lvalue.accept(this, null);
			cmp.rvalue.accept(this, null);
			switch (cmp.operator) {
				case '<':
					writer.visitJumpInsn(cond ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE, jumpto);
					break;
				case '>':
					writer.visitJumpInsn(cond ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE, jumpto);
					break;
				case Tokenizer.TT_LTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT, jumpto);
					break;
				case Tokenizer.TT_GTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT, jumpto);
					break;
			}
		} else {
			// general comparison
			cmp.lvalue.accept(this, null);
			cmp.rvalue.accept(this, null);
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
			switch (cmp.operator) {
				case '<':
					writer.visitJumpInsn(cond ? Opcodes.IFLT : Opcodes.IFGE, jumpto);
					break;
				case '>':
					writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLE, jumpto);
					break;
				case Tokenizer.TT_LTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFLE : Opcodes.IFGT, jumpto);
					break;
				case Tokenizer.TT_GTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLT, jumpto);
					break;
				case Tokenizer.TT_EQEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFEQ : Opcodes.IFNE, jumpto);
					break;
				case Tokenizer.TT_NOTEQ:
					writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
					break;
			}
		}
	}

	public Object visitComparison(ComparisonExpr cmp, Object unused) {
		Label lfalse = new Label();
		Label lafter = new Label();
		visitCmpInIf(cmp, lfalse, false);
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
			if (str2.rettype().equals(BuiltinType.STRING)) {
				str2.accept(this, unused);
			} else {
				writer.visitLdcInsn(new FuncObject("Any.tostr"));
				str2.accept(this, unused);
				writer.visitCallInsn(Opcodes.CALL, 1);
			}
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
			writer.visitCallInsn(Opcodes.CALL, 1);
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
		Label lstart = new Label();
		Label lafter = new Label();
		// writing body
		writer.visitLabel(lstart);
		wexpr.body.accept(this, unused);
		// writing condition
		if (wexpr.condition instanceof ConstExpr && ((ConstExpr)wexpr.condition).value.equals(Boolean.TRUE)) {
			writer.visitJumpInsn(Opcodes.GOTO, lstart);
		} else if (wexpr.condition instanceof ComparisonExpr) {
			visitCmpInIf((ComparisonExpr)wexpr.condition, lstart, true);
		} else {
			wexpr.condition.accept(this, unused);
			writer.visitJumpInsn(Opcodes.IFNE, lstart);
		}
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
		Label lelse = new Label();
		Label lafter = new Label();
		// writing if condition
		if (ifexpr.condition instanceof ComparisonExpr) {
			visitCmpInIf((ComparisonExpr)ifexpr.condition, lelse, false);
		} else {
			ifexpr.condition.accept(this, unused);
			writer.visitJumpInsn(Opcodes.IFEQ, lelse);
		}
		// writing if body
		ifexpr.ifexpr.accept(this, unused);
		// writing else body
		if (ifexpr.elseexpr instanceof NoneExpr) {
			writer.visitLabel(lelse);
		} else {
			writer.visitJumpInsn(Opcodes.GOTO, lafter);
			writer.visitLabel(lelse);
			ifexpr.elseexpr.accept(this, unused);
		}
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
		} else {
			writer.visitInsn(Opcodes.NEWARRAY);
		}
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object unused) {
		writer.visitLdcInsn(new Int(newarray.initializers.length));
		Type artype = newarray.rettype();
		if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
			writer.visitInsn(Opcodes.NEWBA);
		} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
			writer.visitInsn(Opcodes.NEWCA);
		} else {
			writer.visitInsn(Opcodes.NEWARRAY);
		}
		for (int i=0; i<newarray.initializers.length; i++) {
			Expr e = newarray.initializers[i];
			if (e != null) {
				writer.visitInsn(Opcodes.DUP);
				writer.visitLdcInsn(new Int(i));
				e.accept(this, unused);
				if (artype.isSubtypeOf(BuiltinType.BARRAY)) {
					writer.visitInsn(Opcodes.BASTORE);
				} else if (artype.isSubtypeOf(BuiltinType.CARRAY)) {
					writer.visitInsn(Opcodes.CASTORE);
				} else {
					writer.visitInsn(Opcodes.ASTORE);
				}
			}
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object unused) {
		return null;
	}

	public Object visitSwitch(SwitchExpr swexpr, Object unused) {
		swexpr.indexexpr.accept(this, unused);
		// computing count of numbers, min, max
		int count = 0;
		int min = 0;
		int max = 0;
		Vector vkeys = swexpr.keys;
		for (int i=0; i<vkeys.size(); i++) {
			int[] ik = (int[])vkeys.elementAt(i);
			if (i==0) {
				min = ik[0];
				max = ik[0];
			}
			for (int j=0; j<ik.length; j++) {
				min = Math.min(min, ik[j]);
				max = Math.max(max, ik[j]);
			}
			count += ik.length;
		}
		// preparing labels
		Label lafter = new Label();
		Label ldflt = new Label();
		Label[] labelsunique = new Label[vkeys.size()];
		for (int i=0; i<labelsunique.length; i++) {
			labelsunique[i] = new Label();
		}
		// writing switch instruction.
		int tablelen = 4 + 4 + 2*(max-min+1);
		int lookuplen = 2 + count*6;
		if (tablelen <= lookuplen) {
			// do tableswitch
			Label[] labels = new Label[max-min+1];
			for (int i=0; i<labels.length; i++) {
				labels[i] = (swexpr.elseexpr != null) ? ldflt : lafter;
			}
			for (int ei=0; ei<labelsunique.length; ei++) {
				int[] ik = (int[])vkeys.elementAt(ei);
				for (int i=0; i<ik.length; i++) {
					labels[ik[i]-min] = labelsunique[ei];
				}
			}
			// write it
			if (swexpr.elseexpr != null) {
				writer.visitTableSwitch(min, max, ldflt, labels);
				writer.visitLabel(ldflt);
				swexpr.elseexpr.accept(this, unused);
				writer.visitJumpInsn(Opcodes.GOTO, lafter);
			} else {
				writer.visitTableSwitch(min, max, lafter, labels);
			}
		} else {
			// do lookupswitch
			int[] keys = new int[count];
			Label[] labels = new Label[count];
			int ofs = 0;
			for (int ei=0; ei<labelsunique.length; ei++) {
				int[] ik = (int[])vkeys.elementAt(ei);
				System.arraycopy(ik, 0, keys, ofs, ik.length);
				for (int j=0; j<ik.length; j++) {
					labels[ofs+j] = labelsunique[ei];
				}
				ofs += ik.length;
			}
			// write it
			if (swexpr.elseexpr != null) {
				writer.visitLookupSwitch(ldflt, keys, labels);
				writer.visitLabel(ldflt);
				swexpr.elseexpr.accept(this, unused);
				writer.visitJumpInsn(Opcodes.GOTO, lafter);
			} else {
				writer.visitLookupSwitch(lafter, keys, labels);
			}
		}
		// write expressions
		for (int i=0; i<labelsunique.length; i++) {
			Expr e = (Expr)swexpr.exprs.elementAt(i);
			writer.visitLabel(labelsunique[i]);
			e.accept(this, unused);
			if (i != labelsunique.length-1) {
				writer.visitJumpInsn(Opcodes.GOTO, lafter);
			}
		}
		writer.visitLabel(lafter);
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
					writer.visitLdcInsn(new Int(-1));
					writer.visitInsn(Opcodes.IXOR);
				} else if (type.isSubtypeOf(BuiltinType.LONG)) {
					writer.visitLdcInsn(new Long(-1));
					writer.visitInsn(Opcodes.LXOR);
				}
				break;
			case '!':
				writer.visitLdcInsn(new Int(1));
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
		Label lcond = new Label();
		Label lstart = new Label();
		// writing body
		writer.visitJumpInsn(Opcodes.GOTO, lcond);
		writer.visitLabel(lstart);
		wexpr.body.accept(this, unused);
		// writing condition
		writer.visitLabel(lcond);
		if (wexpr.condition instanceof ConstExpr && ((ConstExpr)wexpr.condition).value.equals(Boolean.TRUE)) {
			writer.visitJumpInsn(Opcodes.GOTO, lstart);
		} else if (wexpr.condition instanceof ComparisonExpr) {
			visitCmpInIf((ComparisonExpr)wexpr.condition, lstart, true);
		} else {
			wexpr.condition.accept(this, unused);
			writer.visitJumpInsn(Opcodes.IFNE, lstart);
		}
		return null;
	}
}

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

import alchemy.nec.tree.*;
import java.util.Enumeration;


/**
 * Computes code size, size of stack and number of locals for
 * Func. Optionally also assigns indices to local variables.
 * 
 * @author Sergey Basalaev
 */
class FuncComputer implements ExprVisitor {

	private final boolean calcVars;

	public FuncComputer(boolean calcVars) {
		this.calcVars = calcVars;
	}

	public FuncData visitFunc(Func f) {
		FuncData data = new FuncData();
		if (calcVars)
		for (int i=0; i < f.locals.size(); i++) {
			Var v = (Var)f.locals.elementAt(i);
			v.index = i;
		}
		data.localcount += f.locals.size();
		data.updateMax();
		f.body.accept(this, data);
		data.codesize++; // return instruction
		return data;
	}

	public Object visitALen(ALenExpr alen, Object data) {
		FuncData fdata = (FuncData)data;
		alen.arrayexpr.accept(this, data);
		fdata.codesize++;
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object data) {
		FuncData fdata = (FuncData)data;
		aload.arrayexpr.accept(this, data);
		aload.indexexpr.accept(this, data);
		fdata.codesize++;
		fdata.stackhead--;
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object data) {
		FuncData fdata = (FuncData)data;
		astore.arrayexpr.accept(this, data);
		astore.indexexpr.accept(this, data);
		astore.assignexpr.accept(this, data);
		fdata.codesize++;
		fdata.stackhead -= 3;
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object data) {
		FuncData fdata = (FuncData)data;
		assign.expr.accept(this, data);
		if (assign.var.index < 8) {
			fdata.codesize++;
		} else {
			fdata.codesize += 2;
		}
		fdata.stackhead--;
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object data) {
		binary.lvalue.accept(this, data);
		binary.rvalue.accept(this, data);
		FuncData fdata = (FuncData)data;
		fdata.codesize++;
		fdata.stackhead--;
		return null;
	}

	public Object visitBlock(BlockExpr block, Object data) {
		FuncData fdata = (FuncData)data;
		if (calcVars)
		for (int i=0; i < block.locals.size(); i++) {
			Var v = (Var)block.locals.elementAt(i);
			v.index = fdata.localcount + i;
		}
		fdata.localcount += block.locals.size();
		fdata.updateMax();
		for (Enumeration e = block.exprs.elements(); e.hasMoreElements(); ) {
			Expr sub = (Expr)e.nextElement();
			sub.accept(this, data);
		}
		fdata.localcount -= block.locals.size();
		return null;
	}

	public Object visitCast(CastExpr cast, Object data) {
		cast.expr.accept(this, data);
		return null;
	}

	public Object visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
		cast.expr.accept(this, data);
		((FuncData)data).codesize++;
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object data) {
		FuncData fdata = (FuncData)data;
		Object value = cexpr.value;
		boolean inline = false;
		if (value == null || value.getClass() == Boolean.class) {
			fdata.codesize++; // aconst_null, iconst_{0,1}
			inline = true;
		} else if (value.getClass() == Integer.class) {
			int ival = ((Integer)value).intValue();
			if (ival >= -1 && ival <= 5) {
				fdata.codesize++;     // iconst_x
				inline = true;
			} else if (ival >= -0x80 && ival < 0x80) {
				fdata.codesize += 2;  // bipush <byte>
				inline = true;
			}
		} else if (value.getClass() == Long.class) {
			long lval = ((Long)value).longValue();
			if (lval == 0l || lval == 1l) {
				fdata.codesize++; // lconst_X
				inline = true;
			}
		} else if (value.getClass() == Float.class) {
			float fval = ((Float)value).floatValue();
			if (fval == 0f || fval == 1f || fval == 2f) {
				fdata.codesize++; // fconst_X
				inline = true;
			}
		} else if (value.getClass() == Double.class) {
			double dval = ((Double)value).doubleValue();
			if (dval == 0d || dval == 1d) {
				fdata.codesize++; // dconst_X
				inline = true;
			}
		}
		if (!inline) {
			fdata.codesize += 3; // ldc <ushort>
		}
		fdata.stackhead++;
		fdata.updateMax();
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object data) {
		disc.expr.accept(this, data);
		FuncData fdata = (FuncData)data;
		fdata.codesize++;
		fdata.stackhead--;
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object data) {
		fcall.fload.accept(this, data);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, data);
		}
		FuncData fdata = (FuncData)data;
		fdata.codesize++; //call / calv
		fdata.stackhead -= fcall.args.length;
		if (fcall.rettype().equals(BuiltinType.NONE)) {
			fdata.stackhead--;
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object data) {
		FuncData fdata = (FuncData)data;
		ifexpr.condition.accept(this, data);
		fdata.codesize += 3;
		fdata.stackhead--;
		ifexpr.ifexpr.accept(this, data);
		fdata.codesize += 3;
		ifexpr.elseexpr.accept(this, data);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object data) {
		FuncData fdata = (FuncData)data;
		newarray.lengthexpr.accept(this, data);
		fdata.codesize++;
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object data) {
		FuncData fdata = (FuncData)data;
		new ConstExpr(new Integer(newarray.initializers.length)).accept(this, data);
		fdata.codesize++; // newarray
		for (int i=0; i < newarray.initializers.length; i++) {
			if (newarray.initializers[i] != null) {
				fdata.codesize++; // dup
				fdata.stackhead++;
				fdata.updateMax();
				new ConstExpr(new Integer(i)).accept(this, data);
				newarray.initializers[i].accept(this, data);
				fdata.codesize++; // astore
				fdata.stackhead -= 3;
			}
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object data) {
		return null;
	}

	public Object visitUnary(UnaryExpr unary, Object data) {
		FuncData fdata = (FuncData)data;
		unary.expr.accept(this, data);
		if (unary.operator == '!') {
			// ifeq +4
			// iconst_0
			// goto +1
			// iconst_1
			fdata.codesize += 8;
		} else {
			fdata.codesize++;
		}
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object data) {
		FuncData fdata = (FuncData)data;
		if (vexpr.var.index < 8) {
			fdata.codesize++;
		} else {
			fdata.codesize += 2;
		}
		fdata.stackhead++;
		fdata.updateMax();
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object data) {
		FuncData fdata = (FuncData)data;
		wexpr.condition.accept(this, data);
		fdata.codesize += 3; //ifeq
		fdata.stackhead--;
		wexpr.body.accept(this, data);
		fdata.codesize += 3; //goto
		return null;
	}
}

class FuncData {
	int codesize = 0;
	int stackhead = 0;
	int stackmax = 0;
	int localcount = 0;
	int localmax = 0;

	void updateMax() {
		if (stackhead > stackmax) stackmax = stackhead;
		if (localcount > localmax) localmax = localcount;
	}
}

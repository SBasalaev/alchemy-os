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

import alchemy.nec.tree.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Gathers all literals in a vector.
 * @author Sergey Basalaev
 */
class ConstCollector implements ExprVisitor {

	public ConstCollector() { }

	public void visitFunc(Func f, Vector v) {
		if (!v.contains(f.asVar.name)) {
			v.addElement(f.asVar.name);
		}
		if (f.body != null) {
			f.body.accept(this, v);
		}
	}

	public Object visitALen(ALenExpr alen, Object data) {
		alen.arrayexpr.accept(this, data);
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object data) {
		aload.arrayexpr.accept(this, data);
		aload.indexexpr.accept(this, data);
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object data) {
		astore.arrayexpr.accept(this, data);
		astore.indexexpr.accept(this, data);
		astore.assignexpr.accept(this, data);
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object data) {
		assign.expr.accept(this, data);
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object data) {
		binary.lvalue.accept(this, data);
		binary.rvalue.accept(this, data);
		return null;
	}

	public Object visitBlock(BlockExpr block, Object data) {
		for (Enumeration e = block.exprs.elements(); e.hasMoreElements(); ) {
			((Expr)e.nextElement()).accept(this, data);
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object data) {
		cast.expr.accept(this, data);
		return null;
	}

	public Object visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
		cast.expr.accept(this, data);
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object data) {
		if (cexpr.rettype() instanceof BuiltinType) {
			Object val = cexpr.value;
			// do not collect null and short numbers - will be inlined
			if (val == null || val instanceof Boolean) return null;
			if (val instanceof Integer) {
				int ival = ((Integer)val).intValue();
				if (ival >= -0x8000 && ival < 0x8000) return null;
			}
			if (val instanceof Long) {
				long lval = ((Long)val).longValue();
				if (lval == 0l || lval == 1l) return null;
			}
			if (val instanceof Float) {
				float fval = ((Float)val).floatValue();
				if (fval == 0f || fval == 1f || fval == 2f) return null;
			}
			if (val instanceof Double) {
				double dval = ((Double)val).doubleValue();
				if (dval == 0d || dval == 1d) return null;
			}
			Vector v = (Vector)data;
			if (!v.contains(cexpr.value)) {
				v.addElement(cexpr.value);
			}
		}
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object data) {
		disc.expr.accept(this, data);
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object data) {
		fcall.fload.accept(this, data);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, data);
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object data) {
		ifexpr.condition.accept(this, data);
		ifexpr.ifexpr.accept(this, data);
		ifexpr.elseexpr.accept(this, data);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object data) {
		newarray.lengthexpr.accept(this, data);
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object data) {
		for (int i=0; i<newarray.initializers.length; i++) {
			if (newarray.initializers[i] != null)
			newarray.initializers[i].accept(this, data);
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object data) {
		return null;
	}

	public Object visitUnary(UnaryExpr unary, Object data) {
		unary.expr.accept(this, data);
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object data) {
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object data) {
		wexpr.condition.accept(this, data);
		wexpr.body.accept(this, data);
		return null;
	}
}

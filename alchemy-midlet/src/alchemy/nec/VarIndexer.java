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

package alchemy.nec;

import alchemy.core.Int;
import alchemy.nec.tree.*;
import java.util.Vector;

/**
 * Assigns indices to local variables.
 * Index to start with is sent as parameter.
 * 
 * @author Sergey Basalaev
 */
public class VarIndexer implements ExprVisitor {
	
	public VarIndexer() { }
	
	public void visitUnit(Unit u) {
		Vector funcs = u.funcs;
		for (int i=0; i<funcs.size(); i++) {
			Func f = (Func)funcs.elementAt(i);
			if (f.body != null) {
				for (int vi=0;  vi<f.locals.size(); vi++) {
					Var v = (Var)f.locals.elementAt(vi);
					v.index = vi;
				}
				f.body.accept(this, Int.toInt(f.locals.size()));
			}
		}
	}

	public Object visitAChange(AChangeExpr achange, Object offset) {
		achange.arrayexpr.accept(this, offset);
		achange.indexexpr.accept(this, offset);
		achange.rvalue.accept(this, offset);
		return null;
	}
	
	public Object visitALen(ALenExpr alen, Object offset) {
		alen.arrayexpr.accept(this, offset);
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object offset) {
		aload.arrayexpr.accept(this, offset);
		aload.indexexpr.accept(this, offset);
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object offset) {
		astore.arrayexpr.accept(this, offset);
		astore.indexexpr.accept(this, offset);
		astore.assignexpr.accept(this, offset);
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object offset) {
		assign.expr.accept(this, offset);
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object offset) {
		binary.lvalue.accept(this, offset);
		binary.rvalue.accept(this, offset);
		return null;
	}

	public Object visitBlock(BlockExpr block, Object offset) {
		int start = ((Int)offset).value;
		int size = block.locals.size();
		for (int vi=0; vi<size; vi++) {
			Var v = (Var)block.locals.elementAt(vi);
			v.index = start+vi;
		}
		for (int ei=0; ei<block.exprs.size(); ei++) {
			((Expr)block.exprs.elementAt(ei)).accept(this, Int.toInt(start+size));
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object offset) {
		cast.expr.accept(this, offset);
		return null;
	}

	public Object visitComparison(ComparisonExpr cmp, Object offset) {
		cmp.lvalue.accept(this, offset);
		cmp.rvalue.accept(this, offset);
		return null;
	}

	public Object visitConcat(ConcatExpr concat, Object offset) {
		for (int ei=0; ei<concat.exprs.size(); ei++) {
			((Expr)concat.exprs.elementAt(ei)).accept(this, offset);
		}
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object offset) {
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object offset) {
		disc.expr.accept(this, offset);
		return null;
	}

	public Object visitDoWhile(DoWhileExpr wexpr, Object offset) {
		wexpr.condition.accept(this, offset);
		wexpr.body.accept(this, offset);
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object offset) {
		fcall.fload.accept(this, offset);
		for (int ei=0; ei<fcall.args.length; ei++) {
			fcall.args[ei].accept(this, offset);
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object offset) {
		ifexpr.condition.accept(this, offset);
		ifexpr.ifexpr.accept(this, offset);
		ifexpr.elseexpr.accept(this, offset);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object offset) {
		newarray.lengthexpr.accept(this, offset);
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object offset) {
		for (int ei=0; ei<newarray.initializers.length; ei++) {
			if (newarray.initializers[ei] != null)
				newarray.initializers[ei].accept(this, offset);
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object offset) {
		return null;
	}

	public Object visitSwitch(SwitchExpr swexpr, Object offset) {
		swexpr.indexexpr.accept(this, offset);
		for (int vi=0; vi<swexpr.exprs.size(); vi++) {
			((Expr)swexpr.exprs.elementAt(vi)).accept(this, offset);
		}
		if (swexpr.elseexpr != null) swexpr.elseexpr.accept(this, offset);
		return null;
	}

	public Object visitTryCatch(TryCatchExpr trycatch, Object offset) {
		trycatch.tryexpr.accept(this, offset);
		trycatch.catchexpr.accept(this, offset);
		return null;
	}
	
	public Object visitUnary(UnaryExpr unary, Object offset) {
		unary.expr.accept(this, offset);
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object offset) {
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object offset) {
		wexpr.condition.accept(this, offset);
		wexpr.body.accept(this, offset);
		return null;
	}
}

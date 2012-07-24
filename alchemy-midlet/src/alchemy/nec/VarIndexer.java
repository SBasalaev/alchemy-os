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
			for (int vi=0;  vi<f.locals.size(); vi++) {
				Var v = (Var)f.locals.elementAt(vi);
				v.index = i;
			}
			f.body.accept(this, new Integer(f.locals.size()));
		}
	}

	public Object visitALen(ALenExpr alen, Object i) {
		alen.arrayexpr.accept(this, i);
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object i) {
		aload.arrayexpr.accept(this, i);
		aload.indexexpr.accept(this, i);
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object i) {
		astore.arrayexpr.accept(this, i);
		astore.assignexpr.accept(this, i);
		astore.indexexpr.accept(this, i);
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object i) {
		assign.expr.accept(this, i);
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object i) {
		binary.lvalue.accept(this, i);
		binary.rvalue.accept(this, i);
		return null;
	}

	public Object visitBlock(BlockExpr block, Object i) {
		int start = ((Integer)i).intValue();
		int size = block.locals.size();
		for (int vi=0; vi<size; vi++) {
			Var v = (Var)block.locals.elementAt(vi);
			v.index = start+vi;
		}
		for (int ei=0; ei<block.exprs.size(); ei++) {
			((Expr)block.exprs.elementAt(ei)).accept(this, new Integer(start+size));
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object i) {
		cast.expr.accept(this, i);
		return null;
	}

	public Object visitComparison(ComparisonExpr cmp, Object i) {
		cmp.lvalue.accept(this, i);
		cmp.rvalue.accept(this, i);
		return null;
	}

	public Object visitConcat(ConcatExpr concat, Object i) {
		for (int ei=0; ei<concat.exprs.size(); ei++) {
			((Expr)concat.exprs.elementAt(ei)).accept(this, i);
		}
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object i) {
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object i) {
		disc.expr.accept(this, i);
		return null;
	}

	public Object visitDoWhile(DoWhileExpr wexpr, Object i) {
		wexpr.condition.accept(this, i);
		wexpr.body.accept(this, i);
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object i) {
		fcall.fload.accept(this, i);
		for (int ei=0; ei<fcall.args.length; ei++) {
			fcall.args[ei].accept(this, new Integer(ei));
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object i) {
		ifexpr.condition.accept(this, i);
		ifexpr.ifexpr.accept(this, i);
		ifexpr.elseexpr.accept(this, i);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object i) {
		newarray.lengthexpr.accept(this, i);
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object i) {
		for (int ei=0; ei<newarray.initializers.length; ei++) {
			if (newarray.initializers[ei] != null)
				newarray.initializers[ei].accept(this, i);
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object i) {
		return null;
	}

	public Object visitUnary(UnaryExpr unary, Object i) {
		unary.expr.accept(this, i);
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object i) {
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object i) {
		wexpr.condition.accept(this, i);
		wexpr.body.accept(this, i);
		return null;
	}
}

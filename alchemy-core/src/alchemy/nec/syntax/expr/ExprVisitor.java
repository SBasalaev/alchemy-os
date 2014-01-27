/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.syntax.expr;

/**
 * Visitor for expression trees.
 * @author Sergey Basalaev
 */
public interface ExprVisitor {
	Object visitApply(ApplyExpr expr, Object args);
	Object visitArrayElement(ArrayElementExpr expr, Object args);
	Object visitArrayLen(ArrayLenExpr expr, Object args);
	Object visitBinary(BinaryExpr expr, Object args);
	Object visitCall(CallExpr expr, Object args);
	Object visitCast(CastExpr expr, Object args);
	Object visitComparison(ComparisonExpr expr, Object args);
	Object visitConcat(ConcatExpr expr, Object args);
	Object visitConst(ConstExpr expr, Object args);
	Object visitIfElse(IfElseExpr expr, Object args);
	Object visitNewArray(NewArrayExpr expr, Object args);
	Object visitNewArrayInit(NewArrayInitExpr expr, Object args);
	Object visitRange(RangeExpr expr, Object args);
	Object visitSequential(SequentialExpr expr, Object args);
	Object visitSwitch(SwitchExpr expr, Object args);
	Object visitTryCatch(TryCatchExpr expr, Object args);
	Object visitUnary(UnaryExpr expr, Object args);
	Object visitVar(VarExpr expr, Object args);
}

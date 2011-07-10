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

package alchemy.nec.tree;

/**
 * Visitor for expression trees.
 * @author Sergey Basalaev
 */
public interface ExprVisitor {
	void visitAssign(AssignExpr assign, Object data);
	void visitBinary(BinaryExpr binary, Object data);
	void visitBlock(BlockExpr block, Object data);
	void visitCast(CastExpr cast, Object data);
	void visitCastPrimitive(CastPrimitiveExpr cast, Object data);
	void visitComparison(ComparisonExpr comp, Object data);
	void visitConst(ConstExpr cexpr, Object data);
	void visitDiscard(DiscardExpr disc, Object data);
	void visitFCall(FCallExpr fcall, Object data);
	void visitIf(IfExpr ifexpr, Object data);
	void visitNone(NoneExpr none, Object data);
	void visitUnary(UnaryExpr expr, Object data);
	void visitVar(VarExpr vexpr, Object data);
	void visitWhile(WhileExpr wexpr, Object data);
}

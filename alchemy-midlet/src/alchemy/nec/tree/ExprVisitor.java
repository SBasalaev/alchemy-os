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
	Object visitALen(ALenExpr alen, Object data);
	Object visitALoad(ALoadExpr aload, Object data);
	Object visitAStore(AStoreExpr astore, Object data);
	Object visitAssign(AssignExpr assign, Object data);
	Object visitBinary(BinaryExpr binary, Object data);
	Object visitBlock(BlockExpr block, Object data);
	Object visitCast(CastExpr cast, Object data);
	Object visitCastPrimitive(CastPrimitiveExpr cast, Object data);
	Object visitConst(ConstExpr cexpr, Object data);
	Object visitDiscard(DiscardExpr disc, Object data);
	Object visitFCall(FCallExpr fcall, Object data);
	Object visitIf(IfExpr ifexpr, Object data);
	Object visitNewArray(NewArrayExpr newarray, Object data);
	Object visitNone(NoneExpr none, Object data);
	Object visitUnary(UnaryExpr expr, Object data);
	Object visitVar(VarExpr vexpr, Object data);
	Object visitWhile(WhileExpr wexpr, Object data);
}

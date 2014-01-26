/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.syntax.statement;

/**
 *
 * @author Sergey Basalaev
 */
public interface StatementVisitor {
	Object visitArraySetStatement(ArraySetStatement stat, Object args);
	Object visitAssignStatement(AssignStatement stat, Object args);
	Object visitBlockStatement(BlockStatement stat, Object args);
	Object visitBreakStatement(BreakStatement stat, Object args);
	Object visitCompoundAssignStatement(CompoundAssignStatement stat, Object args);
	Object visitContinueStatement(ContinueStatement stat, Object args);
	Object visitEmptyStatement(EmptyStatement stat, Object args);
	Object visitExprStatement(ExprStatement stat, Object args);
	Object visitForLoopStatement(ForLoopStatement stat, Object args);
	Object visitIfStatement(IfStatement stat, Object args);
	Object visitLoopStatement(LoopStatement stat, Object args);
	Object visitReturnStatement(ReturnStatement stat, Object args);
	Object visitSwitchStatement(SwitchStatement stat, Object args);
	Object visitThrowStatement(ThrowStatement stat, Object args);
	Object visitTryCatchStatement(TryCatchStatement stat, Object args);
}

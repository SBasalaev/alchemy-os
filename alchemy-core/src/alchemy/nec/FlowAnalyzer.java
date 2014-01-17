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

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.Unit;
import alchemy.nec.syntax.expr.ConstExpr;
import alchemy.nec.syntax.expr.Expr;
import alchemy.nec.syntax.statement.*;
import alchemy.nec.syntax.type.BuiltinType;

/**
 * Checks control flow.
 * @author Sergey Basalaev
 */
public final class FlowAnalyzer implements StatementVisitor {

	/* == Return states for visitor methods. == */

	/** Indicates that execution continues after this statement. */
	public final Object NEXT = new Object();
	/** Indicates that function returns normally after this statement. */
	public final Object RETURN = new Object();
	/** Indicates that control breaks outside innermost loop after this statement. */
	public final Object BREAK = new Object();
	/** Indicates that error is thrown in this statement. */
	public final Object THROW = new Object();

	private final CompilerEnv env;

	private Function function;
	private int loopcount;

	public FlowAnalyzer(CompilerEnv env) {
		this.env = env;
	}

	public void visitUnit(Unit u) {
		
	}

	public void visitFunction(Function f) {
		if (f.body == null) return;
		this.function = f;
		this.loopcount = 0;
		Object result = f.body.accept(this, null);
		if (result != RETURN && result != THROW) {
			if (f.type.returnType == BuiltinType.NONE) {
				BlockStatement block = new BlockStatement(f);
				block.statements.add(f.body);
				block.statements.add(new ReturnStatement(null));
				f.body = block;
				return;
			}
			if (env.hasOption(CompilerEnv.F_COMPAT21) && f.body.kind == Statement.STAT_BLOCK) {
				BlockStatement block = (BlockStatement) f.body;
				if (block.statements.size() > 0) {
					Statement last = (Statement) block.statements.last();
					if (last.kind == Statement.STAT_EXPR) {
						Expr expr = ((ExprStatement)last).expr;
						if (expr.returnType().safeToCastTo(f.type.returnType)) {
							env.warn(f.source, expr.lineNumber(), CompilerEnv.W_DEPRECATED,
									"In Ether 2.2 'return' keyword should be used to return function result");
							block.statements.set(block.statements.size()-1, new ReturnStatement(expr));
							return;
						}
					}
				}
			}
			env.warn(f.source, f.body.lineNumber(), CompilerEnv.W_ERROR, "Missing return statement");
		}
	}

	public Object visitArraySetStatement(ArraySetStatement stat, Object args) {
		return NEXT;
	}

	public Object visitAssignStatement(AssignStatement assign, Object args) {
		return NEXT;
	}

	public Object visitBlockStatement(BlockStatement block, Object args) {
		Object result = NEXT;
		for (int i=0; i<block.statements.size(); i++) {
			Statement stat = (Statement) block.statements.get(i);
			if (result != NEXT) {
				env.warn(function.source, stat.lineNumber(), CompilerEnv.W_ERROR, "Unreachable statement");
				return result;
			}
			result = stat.accept(this, args);
		}
		return result;
	}

	public Object visitBreakStatement(BreakStatement brk, Object args) {
		if (loopcount == 0) {
			env.warn(function.source, brk.lineNumber(), CompilerEnv.W_ERROR, "'break' outside of loop");
		}
		return BREAK;
	}

	public Object visitCompoundAssignStatement(CompoundAssignStatement stat, Object args) {
		return NEXT;
	}

	public Object visitContinueStatement(ContinueStatement cnt, Object args) {
		if (loopcount == 0) {
			env.warn(function.source, cnt.lineNumber(), CompilerEnv.W_ERROR, "'continue' outside of loop");
		}
		return BREAK;
	}

	public Object visitEmptyStatement(EmptyStatement stat, Object args) {
		return NEXT;
	}

	public Object visitExprStatement(ExprStatement stat, Object args) {
		return NEXT;
	}

	public Object visitIfStatement(IfStatement stat, Object args) {
		Object ifResult = stat.ifstat.accept(this, args);
		Object elseResult = stat.elsestat.accept(this, args);
		if (ifResult == NEXT || elseResult == NEXT) {
			return NEXT;
		} else if (ifResult == BREAK || elseResult == BREAK) {
			return BREAK;
		} else if (ifResult == THROW || elseResult == THROW) {
			return THROW;
		} else {
			return RETURN;
		}
	}

	public Object visitLoopStatement(LoopStatement stat, Object args) {
		loopcount++;
		Object preResult = stat.preBody.accept(this, args);
		Object postResult = stat.postBody.accept(this, args);
		loopcount--;
		if (preResult == RETURN && postResult == RETURN) {
			return RETURN;
		} else if (preResult == THROW && postResult == THROW) {
			return THROW;
		} else if (stat.condition.kind == Expr.EXPR_CONST
		           && ((ConstExpr)stat.condition).value == Boolean.TRUE
		           && preResult != BREAK && postResult != BREAK) {
			// unconditional loop
			if (preResult == THROW || postResult == THROW) {
				return THROW;
			} else {
				return RETURN;
			}
		} else {
			return NEXT;
		}
	}

	public Object visitReturnStatement(ReturnStatement stat, Object args) {
		return RETURN;
	}

	public Object visitThrowStatement(ThrowStatement stat, Object args) {
		return THROW;
	}

	public Object visitTryCatchStatement(TryCatchStatement stat, Object args) {
		Object tryResult = stat.tryStat.accept(this, args);
		Object catchResult = stat.catchStat.accept(this, args);
		if (tryResult == THROW) {
			return catchResult;
		}
		if (tryResult == NEXT || catchResult == NEXT) {
			return NEXT;
		} else if (tryResult == BREAK || catchResult == BREAK) {
			return BREAK;
		} else {
			return catchResult;
		}
	}
}

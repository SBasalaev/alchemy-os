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

package alchemy.nec;

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.statement.*;

/**
 * Returns flow status of the statement.
 * Prints errors if statement defines erroneous flow.
 * Argument is Boolean value.
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

	public FlowAnalyzer(CompilerEnv env) {
		this.env = env;
	}

	public Object visitFunction(Function f) {
		this.function = f;
		Object status = f.body.accept(this, Boolean.FALSE);
		this.function = null;
		return status;
	}

	private Object common(Object flow1, Object flow2) {
		if (flow1 == NEXT || flow2 == NEXT) {
			return NEXT;
		} else if (flow1 == BREAK || flow2 == BREAK) {
			return BREAK;
		} else if (flow1 == THROW || flow2 == THROW) {
			return THROW;
		} else {
			return RETURN;
		}
	}

	public Object visitArraySetStatement(ArraySetStatement stat, Object inLoop) {
		return NEXT;
	}

	public Object visitAssignStatement(AssignStatement assign, Object inLoop) {
		return NEXT;
	}

	public Object visitBlockStatement(BlockStatement block, Object inLoop) {
		Object result = NEXT;
		for (int i=0; i<block.statements.size(); i++) {
			Statement stat = (Statement) block.statements.get(i);
			if (result != NEXT && function != null) {
				env.warn(function.source, stat.lineNumber(), CompilerEnv.W_ERROR, "Unreachable statement");
				return result;
			}
			result = stat.accept(this, inLoop);
		}
		return result;
	}

	public Object visitBreakStatement(BreakStatement brk, Object inLoop) {
		if (inLoop != Boolean.TRUE && function != null) {
			env.warn(function.source, brk.lineNumber(), CompilerEnv.W_ERROR, "'break' outside of loop");
		}
		return BREAK;
	}

	public Object visitCompoundAssignStatement(CompoundAssignStatement stat, Object inLoop) {
		return NEXT;
	}

	public Object visitContinueStatement(ContinueStatement cnt, Object inLoop) {
		if (inLoop != Boolean.TRUE && function != null) {
			env.warn(function.source, cnt.lineNumber(), CompilerEnv.W_ERROR, "'continue' outside of loop");
		}
		return BREAK;
	}

	public Object visitEmptyStatement(EmptyStatement stat, Object inLoop) {
		return NEXT;
	}

	public Object visitExprStatement(ExprStatement stat, Object inLoop) {
		return NEXT;
	}

	public Object visitForLoopStatement(ForLoopStatement stat, Object inLoop) {
		Object incrResult = stat.increment.accept(this, Boolean.TRUE);
		Object bodyResult = stat.body.accept(this, Boolean.TRUE);
		if (incrResult == RETURN && bodyResult == RETURN) {
			return RETURN;
		} else if (incrResult == THROW && bodyResult == THROW) {
			return THROW;
		} else {
			return NEXT;
		}
	}

	public Object visitIfStatement(IfStatement stat, Object inLoop) {
		Object ifResult = stat.ifstat.accept(this, inLoop);
		Object elseResult = stat.elsestat.accept(this, inLoop);
		return common(ifResult, elseResult);
	}

	public Object visitLoopStatement(LoopStatement stat, Object inLoop) {
		Object preResult = stat.preBody.accept(this, Boolean.TRUE);
		Object postResult = stat.postBody.accept(this, Boolean.TRUE);
		if (preResult == RETURN && postResult == RETURN) {
			return RETURN;
		} else if (preResult == THROW && postResult == THROW) {
			return THROW;
		} else {
			return NEXT;
		}
	}

	public Object visitReturnStatement(ReturnStatement stat, Object inLoop) {
		return RETURN;
	}

	public Object visitSwitchStatement(SwitchStatement stat, Object inLoop) {
		Object result = stat.elseStat.accept(this, inLoop);
		for (int i=0; i<stat.statements.length; i++) {
			result = common(result, stat.statements[i].accept(this, inLoop));
		}
		return result;
	}

	public Object visitThrowStatement(ThrowStatement stat, Object inLoop) {
		return THROW;
	}

	public Object visitTryCatchStatement(TryCatchStatement stat, Object inLoop) {
		Object tryResult = stat.tryStat.accept(this, inLoop);
		Object catchResult = stat.catchStat.accept(this, inLoop);
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

/* TODO: get rid of this
 */

package alchemy.nec;

import alchemy.nec.tree.*;

/**
 *
 * @author 
 */
public class ExprPrinter implements ExprVisitor {

	public ExprPrinter() { }

	public void visitUnit(Unit u) {
		for (int i=0; i<u.funcs.size(); i++) {
			Func f = (Func)u.funcs.elementAt(i);
			visitFunc(f);
		}
	}

	public void visitFunc(Func f) {
		System.err.print("def "+f.asVar.name+'(');
		for (int i=0; i<f.locals.size(); i++) {
			if (i != 0) System.out.print(", ");
			Var v = (Var)f.locals.elementAt(i);
			System.out.print(v.name+" : "+v.type);
		}
		System.err.print(')');
		if (f.body == null) System.out.println(';');
		else {
			System.out.print(" = ");
			f.body.accept(this, f);
		}
		System.out.println();
	}

	public void visitAssign(AssignExpr assign, Object data) {
		System.out.print(assign.var.name+" = ");
		assign.expr.accept(this, data);
	}

	public void visitBinary(BinaryExpr binary, Object data) {
		System.out.print('(');
		binary.lvalue.accept(this, data);
		switch (binary.operator) {
			case Tokenizer.TT_GTGT:
				System.out.print(" >> ");
				break;
			case Tokenizer.TT_GTGTGT:
				System.out.print(" >>> ");
				break;
			case Tokenizer.TT_LTLT:
				System.out.print(" << ");
				break;
			default:
				System.out.print(" "+(char)binary.operator+" ");
		}
		binary.rvalue.accept(this, data);
		System.out.print(')');
	}

	public void visitBlock(BlockExpr block, Object data) {
		System.out.println('{');
		for (int i=0; i<block.locals.size(); i++) {
			Var v = (Var)block.locals.elementAt(i);
			System.out.println("var "+v.name+" : "+v.type+';');
		}
		for (int i=0; i<block.exprs.size(); i++) {
			((Expr)block.exprs.elementAt(i)).accept(this, data);
		}
		System.out.println('}');
	}

	public void visitCast(CastExpr cast, Object data) {
		System.out.print("cast ("+cast.toType+") ");
		cast.expr.accept(this, data);
	}

	public void visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
		cast.expr.accept(this, data);
	}

	public void visitComparison(ComparisonExpr comp, Object data) {
		System.out.print('(');
		comp.lvalue.accept(this, data);
		switch (comp.operator) {
			case Tokenizer.TT_GTEQ:
				System.out.print(" >= ");
				break;
			case Tokenizer.TT_LTEQ:
				System.out.print(" <= ");
				break;
			case Tokenizer.TT_EQEQ:
				System.out.print(" == ");
				break;
			case Tokenizer.TT_NOTEQ:
				System.out.print(" != ");
				break;
			default:
				System.out.print(" "+(char)comp.operator+" ");
		}
		comp.rvalue.accept(this, data);
		System.out.print(')');
	}

	public void visitConst(ConstExpr cexpr, Object data) {
		if (cexpr.value instanceof Func) {
			Func f = (Func)cexpr.value;
			System.out.print(f.asVar.name);
		} else {
			System.out.print(cexpr.value);
		}
	}

	public void visitDiscard(DiscardExpr disc, Object data) {
		disc.expr.accept(this, data);
		System.out.println(';');
	}

	public void visitFCall(FCallExpr fcall, Object data) {
		fcall.fload.accept(this, data);
		System.out.print('(');
		for (int i=0; i < fcall.args.length; i++) {
			if (i != 0) System.out.print(", ");
			fcall.args[i].accept(this, data);
		}
		System.out.print(')');
	}

	public void visitIf(IfExpr ifexpr, Object data) {
		System.out.print("if (");
		ifexpr.condition.accept(this, data);
		System.out.print(") ");
		ifexpr.ifexpr.accept(this, data);
		System.out.print(" else ");
		ifexpr.elseexpr.accept(this, data);
	}

	public void visitNone(NoneExpr none, Object data) {
		System.out.print("{}");
	}

	public void visitUnary(UnaryExpr expr, Object data) {
		System.out.print((char)expr.operator);
		expr.expr.accept(this, data);
	}

	public void visitVar(VarExpr vexpr, Object data) {
		System.out.print(vexpr.var.name);
	}

	public void visitWhile(WhileExpr wexpr, Object data) {
		System.out.print("while (");
		wexpr.condition.accept(this, data);
		System.out.print(") ");
		wexpr.body.accept(this, data);
	}
}

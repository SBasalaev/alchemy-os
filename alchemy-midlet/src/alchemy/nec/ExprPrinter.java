package alchemy.nec;

/*
import alchemy.nec.tree.ALenExpr;
import alchemy.nec.tree.ALoadExpr;
import alchemy.nec.tree.AStoreExpr;
import alchemy.nec.tree.AssignExpr;
import alchemy.nec.tree.BinaryExpr;
import alchemy.nec.tree.BlockExpr;
import alchemy.nec.tree.BuiltinType;
import alchemy.nec.tree.CastExpr;
import alchemy.nec.tree.CastPrimitiveExpr;
import alchemy.nec.tree.ConstExpr;
import alchemy.nec.tree.DiscardExpr;
import alchemy.nec.tree.Expr;
import alchemy.nec.tree.ExprVisitor;
import alchemy.nec.tree.FCallExpr;
import alchemy.nec.tree.Func;
import alchemy.nec.tree.IfExpr;
import alchemy.nec.tree.NewArrayByEnumExpr;
import alchemy.nec.tree.NewArrayExpr;
import alchemy.nec.tree.NoneExpr;
import alchemy.nec.tree.UnaryExpr;
import alchemy.nec.tree.Unit;
import alchemy.nec.tree.Var;
import alchemy.nec.tree.VarExpr;
import alchemy.nec.tree.WhileExpr;


public class ExprPrinter implements ExprVisitor {

	public ExprPrinter() { }
	
	public void visitUnit(Unit u) {
		for (int i=0; i<u.funcs.size(); i=i+1) {
			this.visitFunc((Func)u.funcs.elementAt(i));
			System.out.println();
		}
	}
	
	public void visitFunc(Func f) {
		System.out.print("def "+f.signature+"(");
		for (int i=0; i<f.type.args.length; i=i+1) {
			if (i != 0) System.out.print(", ");
			Var v = (Var)f.locals.elementAt(i);
			System.out.print(v.name+": "+v.type);
		}
		System.out.print(')');
		if(f.type.rettype != BuiltinType.typeNone) {
			System.out.print(": "+f.type.rettype);
		}
		if (f.body != null) {
			System.out.print(" = ");
			f.body.accept(this, null);
			System.out.println();
		} else {
			System.out.println(';');
		}
	}
	
	public Object visitALen(ALenExpr alen, Object data) {
		alen.arrayexpr.accept(this, data);
		System.out.print(".len");
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object data) {
		aload.arrayexpr.accept(this, data);
		System.out.print('[');
		aload.indexexpr.accept(this, data);
		System.out.print(']');
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object data) {
		astore.arrayexpr.accept(this, data);
		System.out.print('[');
		astore.indexexpr.accept(this, data);
		System.out.print("] = ");
		astore.assignexpr.accept(this, data);
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object data) {
		System.out.print(assign.var.name);
		System.out.print(" = ");
		assign.expr.accept(this, data);
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object data) {
		System.out.print('(');
		binary.lvalue.accept(this, data);
		System.out.print((char)binary.operator);
		binary.rvalue.accept(this, data);
		System.out.print(')');
		return null;
	}

	public Object visitBlock(BlockExpr block, Object data) {
		System.out.println('{');
		for (int i=0; i<block.locals.size(); i++) {
			Var v = (Var)block.locals.elementAt(i);
			System.out.print(v.name);
			System.out.print(": ");
			System.out.print(v.type);
			System.out.println(';');
		}
		for (int i=0; i<block.exprs.size(); i++) {
			Expr e = (Expr)block.exprs.elementAt(i);
			e.accept(this, data);
			System.out.println();
		}
		System.out.println('}');
		return null;
	}

	public Object visitCast(CastExpr cast, Object data) {
		System.out.print("cast ("+cast.toType+") ");
		cast.expr.accept(this, data);
		return null;
	}

	public Object visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
		System.out.print("cast ("+cast.casttype+") ");
		cast.expr.accept(this, data);
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object data) {
		if (cexpr.value instanceof Func) {
			System.out.print(((Func)cexpr.value).signature);
		} else {
			System.out.print(cexpr.value);
		}
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object data) {
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object data) {
		fcall.fload.accept(this, data);
		System.out.print('(');
		for (int i=0; i<fcall.args.length; i++) {
			if (i != 0) System.out.print(", ");
			fcall.args[i].accept(this, data);
		}
		System.out.print(')');
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object data) {
		switch (ifexpr.type) {
			case IfExpr.FALSE: System.out.print("ifnot ("); break;
			case IfExpr.NEG: System.out.print("iflt ("); break;
			case IfExpr.NOTNEG: System.out.print("ifge ("); break;
			case IfExpr.NOTNULL: System.out.print("ifnnul ("); break;
			case IfExpr.NOTPOS: System.out.print("ifle ("); break;
			case IfExpr.NOTZERO: System.out.print("ifne ("); break;
			case IfExpr.NULL: System.out.print("ifnul ("); break;
			case IfExpr.POS: System.out.print("ifgt ("); break;
			case IfExpr.TRUE: System.out.print("if ("); break;
			case IfExpr.ZERO: System.out.print("ifeq (");
		}
		ifexpr.condition.accept(this, data);
		System.out.print(") ");
		ifexpr.ifexpr.accept(this, data);
		System.out.print(" else ");
		ifexpr.elseexpr.accept(this, data);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object data) {
		System.out.print("new "+newarray.rettype()+"(");
		newarray.lengthexpr.accept(this, data);
		System.out.print(")");
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object data) {
		System.out.print("new "+newarray.rettype()+"{");
		for (int i=0; i<newarray.initializers.length; i=i+1) {
			if (i != 0) System.out.print(", ");
			newarray.initializers[i].accept(this, data);
		}
		System.out.print("}");
		return null;
	}

	public Object visitNone(NoneExpr none, Object data) {
		System.out.print(";");
		return null;
	}

	public Object visitUnary(UnaryExpr expr, Object data) {
		System.out.print((char)expr.operator);
		expr.expr.accept(this, data);
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object data) {
		System.out.print(vexpr.var.name);
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object data) {
		System.out.print("while (");
		wexpr.condition.accept(this, data);
		System.out.print(") ");
		wexpr.body.accept(this, data);
		return null;
	}
	
}
*/
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
import alchemy.evm.Opcodes;
import alchemy.nec.asm.FuncObject;
import alchemy.nec.asm.FunctionWriter;
import alchemy.nec.asm.Label;
import alchemy.nec.asm.UnitWriter;
import alchemy.nec.tree.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Writes bytecode of the parsed unit.
 * Argument of {@code visit*} methods specifies
 * whether the expression should also write return
 * from function or not.
 * @author Sergey Basalaev
 */
public class EAsmWriter implements ExprVisitor {
	private FunctionWriter writer;
	private final boolean debug;
	
	public EAsmWriter(boolean dbg) {
		debug = dbg;
	}

	public void writeTo(Unit unit, OutputStream out) throws IOException {
		UnitWriter uw = new UnitWriter();
		uw.visitVersion(0x0201);
		Vector funcs = unit.funcs;
		for (int i=0; i<funcs.size(); i++) {
			Func f = (Func)funcs.elementAt(i);
			if (f.body != null && f.hits > 0) {
				writer = uw.visitFunction(f.signature, true, f.type.args.length);
				if (debug) writer.visitSource(f.source);
				f.body.accept(this, Boolean.TRUE);
				writer.visitEnd();
			}
		}
		uw.writeTo(out);
	}

	/**
	 * Visits condition of conditional expressions.
	 * Argument <i>cond</i> specifies value of <i>expr</i>
	 * on which jump should be performed.
	 */
	private void visitCondition(Expr expr, Label jumpto, boolean cond) {
		if (expr instanceof ConstExpr) {
			// true or false, jump if matches cond
			ConstExpr cnst = (ConstExpr) expr;
			if (cnst.value != Boolean.TRUE ^ cond) {
				writer.visitJumpInsn(Opcodes.GOTO, jumpto);
			}
		} else if (expr instanceof UnaryExpr) {
			// may be only !expr
			UnaryExpr unary = (UnaryExpr) expr;
			visitCondition(unary.expr, jumpto, !cond);
		} else if (expr instanceof ComparisonExpr) {
			ComparisonExpr cmp = (ComparisonExpr) expr;
			if (cmp.rvalue instanceof ConstExpr && ((ConstExpr)cmp.rvalue).value == Null.NULL) {
				// comparison with null
				cmp.lvalue.accept(this, null);
				if (cmp.operator == Token.EQEQ) {
					writer.visitJumpInsn(cond ? Opcodes.IFNULL : Opcodes.IFNNULL, jumpto);
				} else {
					writer.visitJumpInsn(cond ? Opcodes.IFNNULL : Opcodes.IFNULL, jumpto);
				}
			} else if (cmp.rvalue instanceof ConstExpr && ((ConstExpr)cmp.rvalue).value.equals(Int.ZERO)
			        && cmp.lvalue.rettype() == BuiltinType.INT) {
				// integer comparison with zero
				cmp.lvalue.accept(this, null);
				switch (cmp.operator) {
					case '<':
						writer.visitJumpInsn(cond ? Opcodes.IFLT : Opcodes.IFGE, jumpto);
						break;
					case '>':
						writer.visitJumpInsn(cond ? Opcodes.IFGT : Opcodes.IFLE, jumpto);
						break;
					case Token.LTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFLE : Opcodes.IFGT, jumpto);
						break;
					case Token.GTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLT, jumpto);
						break;
					case Token.EQEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFEQ : Opcodes.IFNE, jumpto);
						break;
					case Token.NOTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
						break;
				}
			} else if ((cmp.lvalue.rettype() == BuiltinType.INT || cmp.rvalue.rettype() == BuiltinType.INT)
					&& cmp.operator != Token.EQEQ && cmp.operator != Token.NOTEQ) {
				// integer comparison
				cmp.lvalue.accept(this, null);
				cmp.rvalue.accept(this, null);
				switch (cmp.operator) {
					case '<':
						writer.visitJumpInsn(cond ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE, jumpto);
						break;
					case '>':
						writer.visitJumpInsn(cond ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE, jumpto);
						break;
					case Token.LTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT, jumpto);
						break;
					case Token.GTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT, jumpto);
						break;
				}
			} else if (cmp.operator == Token.EQEQ || cmp.operator == Token.NOTEQ) {
				// object comparison
				cmp.lvalue.accept(this, null);
				cmp.rvalue.accept(this, null);
				if (cmp.operator == Token.EQEQ) {
					writer.visitJumpInsn(cond ? Opcodes.IF_ACMPEQ : Opcodes.IF_ACMPNE, jumpto);
				} else {
					writer.visitJumpInsn(cond ? Opcodes.IF_ACMPNE : Opcodes.IF_ACMPEQ, jumpto);
				}
			} else {
				// general comparison
				cmp.lvalue.accept(this, null);
				cmp.rvalue.accept(this, null);
				Type type = Type.commonSupertype(cmp.lvalue.rettype(), cmp.rvalue.rettype());
				if (type == BuiltinType.INT) {
					writer.visitInsn(Opcodes.ICMP);
				} else if (type == BuiltinType.LONG) {
					writer.visitInsn(Opcodes.LCMP);
				} else if (type == BuiltinType.FLOAT) {
					writer.visitInsn(Opcodes.FCMP);
				} else if (type == BuiltinType.DOUBLE) {
					writer.visitInsn(Opcodes.DCMP);
				} else {
					writer.visitInsn(Opcodes.ACMP);
				}
				switch (cmp.operator) {
					case '<':
						writer.visitJumpInsn(cond ? Opcodes.IFLT : Opcodes.IFGE, jumpto);
						break;
					case '>':
						writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLE, jumpto);
						break;
					case Token.LTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFLE : Opcodes.IFGT, jumpto);
						break;
					case Token.GTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFGE : Opcodes.IFLT, jumpto);
						break;
					case Token.EQEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFEQ : Opcodes.IFNE, jumpto);
						break;
					case Token.NOTEQ:
						writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
						break;
				}
			}
		} else if (expr instanceof IfExpr) {
			IfExpr ifexpr = (IfExpr) expr;
			Label elsebranch = new Label();
			Label afterif = new Label();
			if (ifexpr.ifexpr instanceof ConstExpr) {
				Object cnst = ((ConstExpr)ifexpr.ifexpr).value;
				if (cnst == Boolean.TRUE ^ cond) {
					visitCondition(ifexpr.condition, afterif, true);
					visitCondition(ifexpr.elseexpr, jumpto, cond);
					writer.visitLabel(afterif);
				} else {
					visitCondition(ifexpr.condition, jumpto, true);
					visitCondition(ifexpr.elseexpr, jumpto, cond);
					writer.visitLabel(afterif);
				}
			} else if (ifexpr.elseexpr instanceof ConstExpr) {
				Object cnst = ((ConstExpr)ifexpr.elseexpr).value;
				if (cnst == Boolean.TRUE ^ cond) {
					visitCondition(ifexpr.condition, afterif, false);
					visitCondition(ifexpr.ifexpr, jumpto, cond);
					writer.visitLabel(afterif);
				} else {
					visitCondition(ifexpr.condition, jumpto, false);
					visitCondition(ifexpr.ifexpr, jumpto, cond);
					writer.visitLabel(afterif);
				}
			} else {
				visitCondition(ifexpr.condition, elsebranch, false);
				visitCondition(ifexpr.ifexpr, jumpto, cond);
				writer.visitJumpInsn(Opcodes.GOTO, afterif);
				writer.visitLabel(elsebranch);
				visitCondition(ifexpr.elseexpr, jumpto, cond);
				writer.visitLabel(afterif);
			}
		} else {
			expr.accept(this, Boolean.FALSE);
			writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
		}
	}
	
	public Object visitALen(ALenExpr alen, Object isReturn) {
		alen.arrayexpr.accept(this, Boolean.FALSE);
		Type artype = (ArrayType)alen.arrayexpr.rettype();
		Type eltype = null;
		if (artype instanceof ArrayType)
			eltype = ((ArrayType)artype).elementType();
		if (eltype == BuiltinType.BYTE) {
			writer.visitInsn(Opcodes.BALEN);
		} else if (eltype == BuiltinType.CHAR) {
			writer.visitInsn(Opcodes.CALEN);
		} else if (eltype == BuiltinType.SHORT) {
			writer.visitInsn(Opcodes.SALEN);
		} else if (eltype == BuiltinType.BOOL) {
			writer.visitInsn(Opcodes.ZALEN);
		} else if (eltype == BuiltinType.INT) {
			writer.visitInsn(Opcodes.IALEN);
		} else if (eltype == BuiltinType.LONG) {
			writer.visitInsn(Opcodes.LALEN);
		} else if (eltype == BuiltinType.FLOAT) {
			writer.visitInsn(Opcodes.FALEN);
		} else if (eltype == BuiltinType.DOUBLE) {
			writer.visitInsn(Opcodes.DALEN);
		} else {
			writer.visitInsn(Opcodes.AALEN);
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitALoad(ALoadExpr aload, Object isReturn) {
		aload.arrayexpr.accept(this, Boolean.FALSE);
		aload.indexexpr.accept(this, Boolean.FALSE);
		Type artype = aload.arrayexpr.rettype();
		Type eltype = null;
		if (artype instanceof ArrayType)
			eltype = ((ArrayType)artype).elementType();
		if (eltype == BuiltinType.BYTE) {
			writer.visitInsn(Opcodes.BALOAD);
		} else if (eltype == BuiltinType.CHAR) {
			writer.visitInsn(Opcodes.CALOAD);
		} else if (eltype == BuiltinType.SHORT) {
			writer.visitInsn(Opcodes.SALOAD);
		} else if (eltype == BuiltinType.BOOL) {
			writer.visitInsn(Opcodes.ZALOAD);
		} else if (eltype == BuiltinType.INT) {
			writer.visitInsn(Opcodes.IALOAD);
		} else if (eltype == BuiltinType.LONG) {
			writer.visitInsn(Opcodes.LALOAD);
		} else if (eltype == BuiltinType.FLOAT) {
			writer.visitInsn(Opcodes.FALOAD);
		} else if (eltype == BuiltinType.DOUBLE) {
			writer.visitInsn(Opcodes.DALOAD);
		} else {
			writer.visitInsn(Opcodes.AALOAD);
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitAStore(AStoreExpr astore, Object isReturn) {
		astore.arrayexpr.accept(this, Boolean.FALSE);
		astore.indexexpr.accept(this, Boolean.FALSE);
		astore.assignexpr.accept(this, Boolean.FALSE);
		Type artype = astore.arrayexpr.rettype();
		Type eltype = null;
		if (artype instanceof ArrayType)
			eltype = ((ArrayType)artype).elementType();
		if (eltype == BuiltinType.BYTE) {
			writer.visitInsn(Opcodes.BASTORE);
		} else if (eltype == BuiltinType.CHAR) {
			writer.visitInsn(Opcodes.CASTORE);
		} else if (eltype == BuiltinType.SHORT) {
			writer.visitInsn(Opcodes.SASTORE);
		} else if (eltype == BuiltinType.BOOL) {
			writer.visitInsn(Opcodes.ZASTORE);
		} else if (eltype == BuiltinType.INT) {
			writer.visitInsn(Opcodes.IASTORE);
		} else if (eltype == BuiltinType.LONG) {
			writer.visitInsn(Opcodes.LASTORE);
		} else if (eltype == BuiltinType.FLOAT) {
			writer.visitInsn(Opcodes.FASTORE);
		} else if (eltype == BuiltinType.DOUBLE) {
			writer.visitInsn(Opcodes.DASTORE);
		} else {
			writer.visitInsn(Opcodes.AASTORE);
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}

	public Object visitAssign(AssignExpr assign, Object isReturn) {
		assign.expr.accept(this, Boolean.FALSE);
		writer.visitVarInsn(Opcodes.STORE, assign.var.index);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}
	
	public Object visitBinary(BinaryExpr binary, Object isReturn) {
		binary.lvalue.accept(this, Boolean.FALSE);
		binary.rvalue.accept(this, Boolean.FALSE);
		Type type = binary.rettype();
		switch (binary.operator) {
			case '+':
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.IADD);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LADD);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FADD);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DADD);
				break;
			case '-':
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.ISUB);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LSUB);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FSUB);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DSUB);
				break;
			case '*':
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.IMUL);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LMUL);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FMUL);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DMUL);
				break;
			case '/':
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.IDIV);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LDIV);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FDIV);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DDIV);
				break;
			case '%':
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.IMOD);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LMOD);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FMOD);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DMOD);
				break;
			case '&':
				if (type == BuiltinType.INT || type == BuiltinType.BOOL) writer.visitInsn(Opcodes.IAND);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LAND);
				break;
			case '|':
				if (type == BuiltinType.INT || type == BuiltinType.BOOL) writer.visitInsn(Opcodes.IOR);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LOR);
				break;
			case '^':
				if (type == BuiltinType.INT || type == BuiltinType.BOOL) writer.visitInsn(Opcodes.IXOR);
				else if (type.equals(BuiltinType.LONG)) writer.visitInsn(Opcodes.LXOR);
				break;
			case Token.LTLT:
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.ISHL);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LSHL);
				break;
			case Token.GTGT:
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.ISHR);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LSHR);
				break;
			case Token.GTGTGT:
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.IUSHR);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LUSHR);
				break;
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitBlock(BlockExpr block, Object isReturn) {
		for (int i=0; i<block.exprs.size(); i++) {
			Expr expr = (Expr)block.exprs.elementAt(i);
			expr.accept(this, (isReturn == Boolean.TRUE && i == block.exprs.size()-1) ? Boolean.TRUE : Boolean.FALSE);
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object isReturn) {
		cast.expr.accept(this, Boolean.FALSE);
		Type from = cast.expr.rettype();
		Type to = cast.rettype();
		if (from == BuiltinType.BYTE) {
			if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.LONG) {
				writer.visitInsn(Opcodes.I2L);
			} else if (to == BuiltinType.FLOAT) {
				writer.visitInsn(Opcodes.I2F);
			} else if (to == BuiltinType.DOUBLE) {
				writer.visitInsn(Opcodes.I2D);
			}
		} else if (from == BuiltinType.SHORT) {
			if (to == BuiltinType.BYTE) {
				writer.visitInsn(Opcodes.I2B);
			} else if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.LONG) {
				writer.visitInsn(Opcodes.I2L);
			} else if (to == BuiltinType.FLOAT) {
				writer.visitInsn(Opcodes.I2F);
			} else if (to == BuiltinType.DOUBLE) {
				writer.visitInsn(Opcodes.I2D);
			}
		} else if (from == BuiltinType.CHAR || from == BuiltinType.INT) {
			if (to == BuiltinType.BYTE) {
				writer.visitInsn(Opcodes.I2B);
			} else if (to == BuiltinType.SHORT) {
				writer.visitInsn(Opcodes.I2S);
			} else if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.LONG) {
				writer.visitInsn(Opcodes.I2L);
			} else if (to == BuiltinType.FLOAT) {
				writer.visitInsn(Opcodes.I2F);
			} else if (to == BuiltinType.DOUBLE) {
				writer.visitInsn(Opcodes.I2D);
			}
		} else if (from == BuiltinType.LONG) {
			if (to == BuiltinType.BYTE) {
				writer.visitInsn(Opcodes.L2I);
				writer.visitInsn(Opcodes.I2B);
			} else if (to == BuiltinType.SHORT) {
				writer.visitInsn(Opcodes.L2I);
				writer.visitInsn(Opcodes.I2S);
			} else if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.L2I);
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.INT) {
				writer.visitInsn(Opcodes.L2I);
			} else if (to == BuiltinType.FLOAT) {
				writer.visitInsn(Opcodes.L2F);
			} else if (to == BuiltinType.DOUBLE) {
				writer.visitInsn(Opcodes.L2D);
			}
		} else if (from == BuiltinType.FLOAT) {			
			if (to == BuiltinType.BYTE) {
				writer.visitInsn(Opcodes.F2I);
				writer.visitInsn(Opcodes.I2B);
			} else if (to == BuiltinType.SHORT) {
				writer.visitInsn(Opcodes.F2I);
				writer.visitInsn(Opcodes.I2S);
			} else if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.F2I);
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.INT) {
				writer.visitInsn(Opcodes.F2I);
			} else if (to == BuiltinType.LONG) {
				writer.visitInsn(Opcodes.F2L);
			} else if (to == BuiltinType.DOUBLE) {
				writer.visitInsn(Opcodes.F2D);
			}
		} else if (from == BuiltinType.DOUBLE) {			
			if (to == BuiltinType.BYTE) {
				writer.visitInsn(Opcodes.D2I);
				writer.visitInsn(Opcodes.I2B);
			} else if (to == BuiltinType.SHORT) {
				writer.visitInsn(Opcodes.D2I);
				writer.visitInsn(Opcodes.I2S);
			} else if (to == BuiltinType.CHAR) {
				writer.visitInsn(Opcodes.D2I);
				writer.visitInsn(Opcodes.I2C);
			} else if (to == BuiltinType.INT) {
				writer.visitInsn(Opcodes.D2I);
			} else if (to == BuiltinType.LONG) {
				writer.visitInsn(Opcodes.D2L);
			} else if (to == BuiltinType.FLOAT) {
				writer.visitInsn(Opcodes.D2F);
			}
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}
	
	public Object visitComparison(ComparisonExpr cmp, Object isReturn) {
		Label lfalse = new Label();
		Label lafter = new Label();
		visitCondition(cmp, lfalse, false);
		writer.visitLdcInsn(Boolean.TRUE);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		} else {
			writer.visitJumpInsn(Opcodes.GOTO, lafter);
		}
		writer.visitLabel(lfalse);
		writer.visitLdcInsn(Boolean.FALSE);
		writer.visitLabel(lafter);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitConcat(ConcatExpr concat, Object isReturn) {
		if (concat.exprs.size() == 1) {
			Expr str1 = (Expr)concat.exprs.elementAt(0);
			str1.accept(this, Boolean.FALSE);
		} else {
			writer.visitLdcInsn(new FuncObject("Any.tostr"));
			writer.visitLdcInsn(new FuncObject("new_strbuf"));
			writer.visitCallInsn(Opcodes.CALL, 0);
			for (int i=0; i<concat.exprs.size(); i++) {
				Expr expr = (Expr)concat.exprs.elementAt(i);
				if (expr.rettype() == BuiltinType.CHAR) {
					writer.visitLdcInsn(new FuncObject("StrBuf.addch"));
				} else {
					writer.visitLdcInsn(new FuncObject("StrBuf.append"));
				}
				writer.visitInsn(Opcodes.SWAP);
				expr.accept(this, Boolean.FALSE);
				writer.visitCallInsn(Opcodes.CALL, 2);
			}
			writer.visitCallInsn(Opcodes.CALL, 1);
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object isReturn) {
		if (debug) writer.visitLine(cexpr.lineNumber());
		Object obj = cexpr.value;
		if (isReturn == Boolean.TRUE && obj == Null.NULL) {
			writer.visitInsn(Opcodes.RET_NULL);
			return null;
		}
		if (obj instanceof Func) {
			obj = new FuncObject(((Func)obj).signature);
		}
		writer.visitLdcInsn(obj);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitDiscard(DiscardExpr disc, Object isReturn) {
		disc.expr.accept(this, Boolean.FALSE);
		writer.visitInsn(Opcodes.POP);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}

	public Object visitDoWhile(DoWhileExpr wexpr, Object isReturn) {
		Label lstart = new Label();
		Label lafter = new Label();
		// writing body
		writer.visitLabel(lstart);
		wexpr.body.accept(this, Boolean.FALSE);
		// writing condition
		visitCondition(wexpr.condition, lstart, true);
		writer.visitLabel(lafter);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}

	public Object visitFCall(FCallExpr fcall, Object isReturn) {
		fcall.fload.accept(this, Boolean.FALSE);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, Boolean.FALSE);
		}
		if (fcall.rettype() == BuiltinType.NONE) {
			writer.visitCallInsn(Opcodes.CALV, fcall.args.length);
			if (isReturn == Boolean.TRUE) {
				writer.visitInsn(Opcodes.RET_NULL);
			}
		} else {
			writer.visitCallInsn(Opcodes.CALL, fcall.args.length);
			if (isReturn == Boolean.TRUE) {
				writer.visitInsn(Opcodes.RETURN);
			}
		}
		return null;
	}

	public Object visitIf(IfExpr ifexpr, Object isReturn) {
		Label lelse = new Label();
		Label lafter = new Label();
		// writing if condition
		visitCondition(ifexpr.condition, lelse, false);
		// writing if body
		ifexpr.ifexpr.accept(this, isReturn);
		// writing else body
		if (!(ifexpr.elseexpr instanceof NoneExpr) && isReturn != Boolean.TRUE) {
			writer.visitJumpInsn(Opcodes.GOTO, lafter);
		}
		writer.visitLabel(lelse);
		ifexpr.elseexpr.accept(this, isReturn);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object isReturn) {
		if (debug) writer.visitLine(newarray.lineNumber());
		newarray.lengthexpr.accept(this, Boolean.FALSE);
		Type artype = newarray.rettype();
		Type eltype = null;
		if (artype instanceof ArrayType)
			eltype = ((ArrayType)artype).elementType();
		if (eltype == BuiltinType.BYTE) {
			writer.visitInsn(Opcodes.NEWBA);
		} else if (eltype == BuiltinType.CHAR) {
			writer.visitInsn(Opcodes.NEWCA);
		} else if (eltype == BuiltinType.SHORT) {
			writer.visitInsn(Opcodes.NEWSA);
		} else if (eltype == BuiltinType.BOOL) {
			writer.visitInsn(Opcodes.NEWZA);
		} else if (eltype == BuiltinType.INT) {
			writer.visitInsn(Opcodes.NEWIA);
		} else if (eltype == BuiltinType.LONG) {
			writer.visitInsn(Opcodes.NEWLA);
		} else if (eltype == BuiltinType.FLOAT) {
			writer.visitInsn(Opcodes.NEWFA);
		} else if (eltype == BuiltinType.DOUBLE) {
			writer.visitInsn(Opcodes.NEWDA);
		} else {
			writer.visitInsn(Opcodes.NEWAA);
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object isReturn) {
		if (debug) writer.visitLine(newarray.lineNumber());
		writer.visitLdcInsn(Int.toInt(newarray.initializers.length));
		Type artype = newarray.rettype();
		Type eltype = null;
		if (artype instanceof ArrayType)
			eltype = ((ArrayType)artype).elementType();
		if (eltype == BuiltinType.BYTE) {
			writer.visitInsn(Opcodes.NEWBA);
		} else if (eltype == BuiltinType.CHAR) {
			writer.visitInsn(Opcodes.NEWCA);
		} else if (eltype == BuiltinType.SHORT) {
			writer.visitInsn(Opcodes.NEWSA);
		} else if (eltype == BuiltinType.BOOL) {
			writer.visitInsn(Opcodes.NEWZA);
		} else if (eltype == BuiltinType.INT) {
			writer.visitInsn(Opcodes.NEWIA);
		} else if (eltype == BuiltinType.LONG) {
			writer.visitInsn(Opcodes.NEWLA);
		} else if (eltype == BuiltinType.FLOAT) {
			writer.visitInsn(Opcodes.NEWFA);
		} else if (eltype == BuiltinType.DOUBLE) {
			writer.visitInsn(Opcodes.NEWDA);
		} else {
			writer.visitInsn(Opcodes.NEWAA);
		}
		for (int i=0; i<newarray.initializers.length; i++) {
			Expr e = newarray.initializers[i];
			if (e != null) {
				writer.visitInsn(Opcodes.DUP);
				writer.visitLdcInsn(Int.toInt(i));
				e.accept(this, Boolean.FALSE);
				if (eltype == BuiltinType.BYTE) {
					writer.visitInsn(Opcodes.BASTORE);
				} else if (eltype == BuiltinType.CHAR) {
					writer.visitInsn(Opcodes.CASTORE);
				} else if (eltype == BuiltinType.SHORT) {
					writer.visitInsn(Opcodes.SASTORE);
				} else if (eltype == BuiltinType.BOOL) {
					writer.visitInsn(Opcodes.ZASTORE);
				} else if (eltype == BuiltinType.INT) {
					writer.visitInsn(Opcodes.IASTORE);
				} else if (eltype == BuiltinType.LONG) {
					writer.visitInsn(Opcodes.LASTORE);
				} else if (eltype == BuiltinType.FLOAT) {
					writer.visitInsn(Opcodes.FASTORE);
				} else if (eltype == BuiltinType.DOUBLE) {
					writer.visitInsn(Opcodes.DASTORE);
				} else {
					writer.visitInsn(Opcodes.AASTORE);
				}
			}
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitNone(NoneExpr none, Object isReturn) {
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}

	public Object visitSwitch(SwitchExpr swexpr, Object isReturn) {
		swexpr.indexexpr.accept(this, Boolean.FALSE);
		// computing count of numbers, min, max
		int count = 0;
		int min = 0;
		int max = 0;
		Vector vkeys = swexpr.keys;
		for (int i=0; i<vkeys.size(); i++) {
			int[] ik = (int[])vkeys.elementAt(i);
			if (i==0) {
				min = ik[0];
				max = ik[0];
			}
			for (int j=0; j<ik.length; j++) {
				min = Math.min(min, ik[j]);
				max = Math.max(max, ik[j]);
			}
			count += ik.length;
		}
		// preparing labels
		Label lafter = new Label();
		Label ldflt = new Label();
		Label[] labelsunique = new Label[vkeys.size()];
		for (int i=0; i<labelsunique.length; i++) {
			labelsunique[i] = new Label();
		}
		// writing switch instruction.
		int tablelen = 4 + 4 + 2*(max-min+1);
		int lookuplen = 2 + count*6;
		if (tablelen <= lookuplen) {
			// do tableswitch
			Label[] labels = new Label[max-min+1];
			for (int i=0; i<labels.length; i++) {
				labels[i] = (swexpr.elseexpr != null) ? ldflt : lafter;
			}
			for (int ei=0; ei<labelsunique.length; ei++) {
				int[] ik = (int[])vkeys.elementAt(ei);
				for (int i=0; i<ik.length; i++) {
					labels[ik[i]-min] = labelsunique[ei];
				}
			}
			// write it
			if (swexpr.elseexpr != null) {
				writer.visitTableSwitch(min, max, ldflt, labels);
				writer.visitLabel(ldflt);
				swexpr.elseexpr.accept(this, isReturn);
				if (isReturn != Boolean.TRUE) {
					writer.visitJumpInsn(Opcodes.GOTO, lafter);
				}
			} else {
				writer.visitTableSwitch(min, max, lafter, labels);
			}
		} else {
			// do lookupswitch
			int[] keys = new int[count];
			Label[] labels = new Label[count];
			int ofs = 0;
			for (int ei=0; ei<labelsunique.length; ei++) {
				int[] ik = (int[])vkeys.elementAt(ei);
				System.arraycopy(ik, 0, keys, ofs, ik.length);
				for (int j=0; j<ik.length; j++) {
					labels[ofs+j] = labelsunique[ei];
				}
				ofs += ik.length;
			}
			// write it
			if (swexpr.elseexpr != null) {
				writer.visitLookupSwitch(ldflt, keys, labels);
				writer.visitLabel(ldflt);
				swexpr.elseexpr.accept(this, isReturn);
				if (isReturn != Boolean.TRUE) {
					writer.visitJumpInsn(Opcodes.GOTO, lafter);
				}
			} else {
				writer.visitLookupSwitch(lafter, keys, labels);
			}
		}
		// write expressions
		for (int i=0; i<labelsunique.length; i++) {
			Expr e = (Expr)swexpr.exprs.elementAt(i);
			writer.visitLabel(labelsunique[i]);
			e.accept(this, isReturn);
			if (isReturn != Boolean.TRUE && i != labelsunique.length-1) {
				writer.visitJumpInsn(Opcodes.GOTO, lafter);
			}
		}
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitTryCatch(TryCatchExpr trycatch, Object isReturn) {
		Label trystart = new Label();
		Label tryend = new Label();
		Label aftertry = new Label();
		writer.visitLabel(trystart);
		trycatch.tryexpr.accept(this, isReturn);
		if (isReturn != Boolean.TRUE) {
			writer.visitJumpInsn(Opcodes.GOTO, aftertry);
		}
		writer.visitLabel(tryend);
		writer.visitTryCatchHandler(trystart, tryend);
		if (trycatch.catchvar == null) {
			writer.visitInsn(Opcodes.POP);
		} else {
			writer.visitVarInsn(Opcodes.STORE, trycatch.catchvar.index);
		}
		trycatch.catchexpr.accept(this, isReturn);
		writer.visitLabel(aftertry);
		return null;
	}
	
	public Object visitUnary(UnaryExpr unary, Object isReturn) {
		Type type = unary.rettype();
		switch (unary.operator) {
			case '+':
				unary.expr.accept(this, isReturn);
				return null;
			case '-':
				unary.expr.accept(this, Boolean.FALSE);
				if (type == BuiltinType.INT) writer.visitInsn(Opcodes.INEG);
				else if (type == BuiltinType.LONG) writer.visitInsn(Opcodes.LNEG);
				else if (type == BuiltinType.FLOAT) writer.visitInsn(Opcodes.FNEG);
				else if (type == BuiltinType.DOUBLE) writer.visitInsn(Opcodes.DNEG);
				break;
			case '~':
				unary.expr.accept(this, Boolean.FALSE);
				if (type == BuiltinType.INT) {
					writer.visitLdcInsn(Int.M_ONE);
					writer.visitInsn(Opcodes.IXOR);
				} else if (type == BuiltinType.LONG) {
					writer.visitLdcInsn(new Long(-1));
					writer.visitInsn(Opcodes.LXOR);
				}
				break;
			case '!':
				writer.visitLdcInsn(Int.ONE);
				unary.expr.accept(this, Boolean.FALSE);
				writer.visitInsn(Opcodes.ISUB);
				break;
		}
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitVar(VarExpr vexpr, Object isReturn) {
		if (debug) writer.visitLine(vexpr.lineNumber());
		writer.visitVarInsn(Opcodes.LOAD, vexpr.var.index);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitWhile(WhileExpr wexpr, Object isReturn) {
		Label lstart = new Label();
		Label lafter = new Label();
		// writing condition
		writer.visitLabel(lstart);
		visitCondition(wexpr.condition, lafter, false);
		// writing body
		wexpr.body.accept(this, Boolean.FALSE);
		writer.visitJumpInsn(Opcodes.GOTO, lstart);
		writer.visitLabel(lafter);
		if (isReturn == Boolean.TRUE) {
			writer.visitInsn(Opcodes.RET_NULL);
		}
		return null;
	}
}

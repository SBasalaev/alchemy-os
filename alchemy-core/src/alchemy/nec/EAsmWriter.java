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

package alchemy.nec;

import alchemy.evm.EtherLoader;
import alchemy.evm.Opcodes;
import alchemy.nec.asm.FunctionWriter;
import alchemy.nec.asm.Label;
import alchemy.nec.asm.UnitWriter;
import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.Null;
import alchemy.nec.syntax.Unit;
import alchemy.nec.syntax.Var;
import alchemy.nec.syntax.expr.*;
import alchemy.nec.syntax.statement.*;
import alchemy.nec.syntax.type.ArrayType;
import alchemy.nec.syntax.type.Type;
import alchemy.types.Int32;
import alchemy.types.Int64;
import alchemy.util.ArrayList;
import alchemy.util.Arrays;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes bytecode of the parsed unit.
 * @author Sergey Basalaev
 */
public final class EAsmWriter implements ExprVisitor, StatementVisitor {

	private final CompilerEnv env;
	private final FlowAnalyzer flow;
	
	private Unit unit;
	private FunctionWriter writer;

	private Label loopStart;
	private Label loopEnd;

	private Var[] localVars = new Var[256];

	public EAsmWriter(CompilerEnv env) {
		env.suppressWarnings();
		this.env = env;
		this.flow = new FlowAnalyzer(env);
	}

	private int getVarIndex(Var var) {
		for (int i=0; i<256; i++) {
			if (localVars[i] == var) return i;
		}
		return -1;
	}

	private int addVar(Var var) {
		for (int i=0; i<256; i++) {
			if (localVars[i] == null) {
				localVars[i] = var;
				return i;
			}
		}
		throw new RuntimeException("Too many variables");
	}

	private void removeVar(Var var) {
		for (int i=0; i<256; i++) {
			if (localVars[i] == var) {
				localVars[i] = null;
			}
		}
	}

	public void writeTo(Unit unit, OutputStream out) throws IOException {
		this.unit = unit;
		UnitWriter uw = new UnitWriter();
		uw.visitVersion(EtherLoader.VERSION);
		ArrayList funcs = unit.implementedFunctions;
		for (int i=0; i<funcs.size(); i++) {
			Function f = (Function) funcs.get(i);
			writer = uw.visitFunction(f.signature, true, f.type.argtypes.length);
			if (env.debug) writer.visitSource(f.source);
			for (int vi=0; vi<f.args.length; vi++) addVar(f.args[vi]);
			f.body.accept(this, null);
			for (int vi=0; vi<f.args.length; vi++) removeVar(f.args[vi]);
			writer.visitEnd();
		}
		uw.writeTo(out);
	}

	/**
	 * Visits condition of conditional expressions.
	 * Argument <i>cond</i> specifies value of <i>expr</i>
	 * on which jump should be performed.
	 */
	private void visitCondition(Expr expr, Label jumpto, boolean cond) {
		switch (expr.kind) {
			case Expr.EXPR_CONST: {
				// true or false, jump if matches cond
				Object value = ((ConstExpr)expr).value;
				if (value != Boolean.TRUE ^ cond) {
					writer.visitJumpInsn(Opcodes.GOTO, jumpto);
				}
				break;
			}
			case Expr.EXPR_UNARY: {
				// can be only !expr
				UnaryExpr unary = (UnaryExpr) expr;
				visitCondition(unary.expr, jumpto, !cond);
				break;
			}
			case Expr.EXPR_IF: {
				IfElseExpr ifelse = (IfElseExpr) expr;
				Label elsebranch = new Label();
				Label afterif = new Label();
				if (ifelse.ifexpr.kind == Expr.EXPR_CONST) {
					Object cnst = ((ConstExpr)ifelse.ifexpr).value;
					if (cnst == Boolean.TRUE ^ cond) {
						visitCondition(ifelse.condition, afterif, true);
						visitCondition(ifelse.elseexpr, jumpto, cond);
						writer.visitLabel(afterif);
					} else {
						visitCondition(ifelse.condition, jumpto, true);
						visitCondition(ifelse.elseexpr, jumpto, cond);
						writer.visitLabel(afterif);
					}
				} else if (ifelse.elseexpr.kind == Expr.EXPR_CONST) {
					Object cnst = ((ConstExpr)ifelse.elseexpr).value;
					if (cnst == Boolean.TRUE ^ cond) {
						visitCondition(ifelse.condition, afterif, false);
						visitCondition(ifelse.ifexpr, jumpto, cond);
						writer.visitLabel(afterif);
					} else {
						visitCondition(ifelse.condition, jumpto, false);
						visitCondition(ifelse.ifexpr, jumpto, cond);
						writer.visitLabel(afterif);
					}
				} else {
					visitCondition(ifelse.condition, elsebranch, false);
					visitCondition(ifelse.ifexpr, jumpto, cond);
					writer.visitJumpInsn(Opcodes.GOTO, afterif);
					writer.visitLabel(elsebranch);
					visitCondition(ifelse.elseexpr, jumpto, cond);
					writer.visitLabel(afterif);
				}
				break;
			}
			case Expr.EXPR_COMPARISON: {
				ComparisonExpr cmp = (ComparisonExpr) expr;
				if (cmp.rhs.kind == Expr.EXPR_CONST && ((ConstExpr)cmp.rhs).value == Null.NULL) {
					// comparison with null
					cmp.lhs.accept(this, null);
					if (cmp.operator == Token.EQEQ) {
						writer.visitJumpInsn(cond ? Opcodes.IFNULL : Opcodes.IFNNULL, jumpto);
					} else {
						writer.visitJumpInsn(cond ? Opcodes.IFNNULL : Opcodes.IFNULL, jumpto);
					}
				} else if (cmp.rhs.kind == Expr.EXPR_CONST && ((ConstExpr)cmp.rhs).value.equals(Int32.ZERO)
						&& cmp.lhs.returnType().kind == Type.TYPE_INT) {
					// integer comparison with zero
					cmp.lhs.accept(this, null);
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
				} else if ((cmp.lhs.returnType().kind == Type.TYPE_INT || cmp.rhs.returnType().kind == Type.TYPE_INT)
						&& cmp.operator != Token.EQEQ && cmp.operator != Token.NOTEQ) {
					// integer comparison
					cmp.lhs.accept(this, null);
					cmp.rhs.accept(this, null);
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
					cmp.lhs.accept(this, null);
					cmp.rhs.accept(this, null);
					if (cmp.operator == Token.EQEQ) {
						writer.visitJumpInsn(cond ? Opcodes.IF_ACMPEQ : Opcodes.IF_ACMPNE, jumpto);
					} else {
						writer.visitJumpInsn(cond ? Opcodes.IF_ACMPNE : Opcodes.IF_ACMPEQ, jumpto);
					}
				} else {
					// general comparison
					cmp.lhs.accept(this, null);
					cmp.rhs.accept(this, null);
					Type type = Type.commonSuperType(cmp.lhs.returnType(), cmp.rhs.returnType());
					switch (type.kind) {
						case Type.TYPE_INT:
							writer.visitInsn(Opcodes.ICMP); break;
						case Type.TYPE_LONG:
							writer.visitInsn(Opcodes.LCMP); break;
						case Type.TYPE_FLOAT:
							writer.visitInsn(Opcodes.FCMP); break;
						case Type.TYPE_DOUBLE:
							writer.visitInsn(Opcodes.DCMP); break;
						default:
							writer.visitInsn(Opcodes.ACMP); break;
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
				break;
			}
			default:
				expr.accept(this, null);
				writer.visitJumpInsn(cond ? Opcodes.IFNE : Opcodes.IFEQ, jumpto);
		}
	}

	private int binaryOperatorInsn(Type type, int operator) {
		int kind = type.kind;
		switch (operator) {
			case '+':
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.IADD;
					case Type.TYPE_LONG: return Opcodes.LADD;
					case Type.TYPE_FLOAT: return Opcodes.FADD;
					case Type.TYPE_DOUBLE: return Opcodes.DADD;
				}
				break;
			case '-':
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.ISUB;
					case Type.TYPE_LONG: return Opcodes.LSUB;
					case Type.TYPE_FLOAT: return Opcodes.FSUB;
					case Type.TYPE_DOUBLE: return Opcodes.DSUB;
				}
				break;
			case '*':
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.IMUL;
					case Type.TYPE_LONG: return Opcodes.LMUL;
					case Type.TYPE_FLOAT: return Opcodes.FMUL;
					case Type.TYPE_DOUBLE: return Opcodes.DMUL;
				}
				break;
			case '/':
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.IDIV;
					case Type.TYPE_LONG: return Opcodes.LDIV;
					case Type.TYPE_FLOAT: return Opcodes.FDIV;
					case Type.TYPE_DOUBLE: return Opcodes.DDIV;
				}
				break;
			case '%':
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.IMOD;
					case Type.TYPE_LONG: return Opcodes.LMOD;
					case Type.TYPE_FLOAT: return Opcodes.FMOD;
					case Type.TYPE_DOUBLE: return Opcodes.DMOD;
				}
				break;
			case '&':
				switch (kind) {
					case Type.TYPE_INT:
					case Type.TYPE_BOOL: return Opcodes.IAND;
					case Type.TYPE_LONG: return Opcodes.LAND;
				}
				break;
			case '|':
				switch (kind) {
					case Type.TYPE_INT:
					case Type.TYPE_BOOL: return Opcodes.IOR;
					case Type.TYPE_LONG: return Opcodes.LOR;
				}
				break;
			case '^':
				switch (kind) {
					case Type.TYPE_INT:
					case Type.TYPE_BOOL: return Opcodes.IXOR;
					case Type.TYPE_LONG: return Opcodes.LXOR;
				}
				break;
			case Token.LTLT:
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.ISHL;
					case Type.TYPE_LONG: return Opcodes.LSHL;
				}
				break;
			case Token.GTGT:
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.ISHR;
					case Type.TYPE_LONG: return Opcodes.LSHR;
				}
				break;
			case Token.GTGTGT:
				switch (kind) {
					case Type.TYPE_INT: return Opcodes.IUSHR;
					case Type.TYPE_LONG: return Opcodes.LUSHR;
				}
				break;
		}
		return Opcodes.NOP;
	}

//	public Object visitSwitch(SwitchExpr swexpr, Object isReturn) {
//		swexpr.indexexpr.accept(this, Boolean.FALSE);
//		// computing count of numbers, min, max
//		int count = 0;
//		int min = 0;
//		int max = 0;
//		ArrayList vkeys = swexpr.keys;
//		for (int i=0; i<vkeys.size(); i++) {
//			int[] ik = (int[])vkeys.get(i);
//			if (i==0) {
//				min = ik[0];
//				max = ik[0];
//			}
//			for (int j=0; j<ik.length; j++) {
//				min = Math.min(min, ik[j]);
//				max = Math.max(max, ik[j]);
//			}
//			count += ik.length;
//		}
//		// preparing labels
//		Label lafter = new Label();
//		Label ldflt = new Label();
//		Label[] labelsunique = new Label[vkeys.size()];
//		for (int i=0; i<labelsunique.length; i++) {
//			labelsunique[i] = new Label();
//		}
//		// writing switch instruction.
//		int tablelen = 4 + 4 + 2*(max-min+1);
//		int lookuplen = 2 + count*6;
//		if (tablelen <= lookuplen) {
//			// do tableswitch
//			Label[] labels = new Label[max-min+1];
//			for (int i=0; i<labels.length; i++) {
//				labels[i] = (swexpr.elseexpr != null) ? ldflt : lafter;
//			}
//			for (int ei=0; ei<labelsunique.length; ei++) {
//				int[] ik = (int[])vkeys.get(ei);
//				for (int i=0; i<ik.length; i++) {
//					labels[ik[i]-min] = labelsunique[ei];
//				}
//			}
//			// write it
//			if (swexpr.elseexpr != null) {
//				writer.visitTableSwitch(min, max, ldflt, labels);
//				writer.visitLabel(ldflt);
//				swexpr.elseexpr.accept(this, isReturn);
//				if (isReturn != Boolean.TRUE) {
//					writer.visitJumpInsn(Opcodes.GOTO, lafter);
//				}
//			} else {
//				writer.visitTableSwitch(min, max, lafter, labels);
//			}
//		} else {
//			// do lookupswitch
//			int[] keys = new int[count];
//			Label[] labels = new Label[count];
//			int ofs = 0;
//			for (int ei=0; ei<labelsunique.length; ei++) {
//				int[] ik = (int[])vkeys.get(ei);
//				System.arraycopy(ik, 0, keys, ofs, ik.length);
//				for (int j=0; j<ik.length; j++) {
//					labels[ofs+j] = labelsunique[ei];
//				}
//				ofs += ik.length;
//			}
//			// write it
//			if (swexpr.elseexpr != null) {
//				writer.visitLookupSwitch(ldflt, keys, labels);
//				writer.visitLabel(ldflt);
//				swexpr.elseexpr.accept(this, isReturn);
//				if (isReturn != Boolean.TRUE) {
//					writer.visitJumpInsn(Opcodes.GOTO, lafter);
//				}
//			} else {
//				writer.visitLookupSwitch(lafter, keys, labels);
//			}
//		}
//		// write expressions
//		for (int i=0; i<labelsunique.length; i++) {
//			Expr e = (Expr)swexpr.exprs.get(i);
//			writer.visitLabel(labelsunique[i]);
//			e.accept(this, isReturn);
//			if (isReturn != Boolean.TRUE && i != labelsunique.length-1) {
//				writer.visitJumpInsn(Opcodes.GOTO, lafter);
//			}
//		}
//		writer.visitLabel(lafter);
//		return null;
//	}

	public Object visitApply(ApplyExpr expr, Object args) {
		if (env.debug) writer.visitLine(expr.lineNumber());
		writer.visitLdFunc("Function.apply");
		expr.funcExpr.accept(this, args);
		// create and fill array
		writer.visitLdcInsn(Int32.toInt32(expr.args.length));
		writer.visitInsn(Opcodes.NEWAA);
		for (int i=0; i<expr.args.length; i++) {
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(Int32.toInt32(i));
			expr.args[i].accept(this, args);
			writer.visitInsn(Opcodes.AASTORE);
		}
		writer.visitCallInsn(Opcodes.CALL, 2);
		return null;
	}

	public Object visitArrayElement(ArrayElementExpr expr, Object args) {
		expr.arrayExpr.accept(this, args);
		expr.indexExpr.accept(this, args);
		int loadInsn = Opcodes.AALOAD;
		Type arrayType = expr.arrayExpr.returnType();
		switch (arrayType.kind) {
			case Type.TYPE_ARRAY:
				switch (((ArrayType)arrayType).elementType.kind) {
					case Type.TYPE_BOOL:   loadInsn = Opcodes.ZALOAD; break;
					case Type.TYPE_BYTE:   loadInsn = Opcodes.BALOAD; break;
					case Type.TYPE_CHAR:   loadInsn = Opcodes.CALOAD; break;
					case Type.TYPE_SHORT:  loadInsn = Opcodes.SALOAD; break;
					case Type.TYPE_INT:    loadInsn = Opcodes.IALOAD; break;
					case Type.TYPE_LONG:   loadInsn = Opcodes.LALOAD; break;
					case Type.TYPE_FLOAT:  loadInsn = Opcodes.FALOAD; break;
					case Type.TYPE_DOUBLE: loadInsn = Opcodes.DALOAD; break;
				}
				break;
			case Type.TYPE_INTRANGE:
				loadInsn = Opcodes.IALOAD;
				break;
			case Type.TYPE_LONGRANGE:
				loadInsn = Opcodes.LALOAD;
				break;
		}
		writer.visitInsn(loadInsn);
		return null;
	}

	public Object visitArrayLen(ArrayLenExpr expr, Object args) {
		expr.arrayExpr.accept(this, args);
		int lenInsn = Opcodes.AALEN;
		Type arrayType = expr.arrayExpr.returnType();
		if (arrayType.kind == Type.TYPE_ARRAY) {
			switch (((ArrayType)arrayType).elementType.kind) {
				case Type.TYPE_BOOL:    lenInsn = Opcodes.ZALEN; break;
				case Type.TYPE_BYTE:    lenInsn = Opcodes.BALEN; break;
				case Type.TYPE_CHAR:    lenInsn = Opcodes.CALEN; break;
				case Type.TYPE_SHORT:   lenInsn = Opcodes.SALEN; break;
				case Type.TYPE_INT:     lenInsn = Opcodes.IALEN; break;
				case Type.TYPE_LONG:    lenInsn = Opcodes.LALEN; break;
				case Type.TYPE_FLOAT:   lenInsn = Opcodes.FALEN; break;
				case Type.TYPE_DOUBLE:  lenInsn = Opcodes.DALEN; break;
			}
		}
		writer.visitInsn(lenInsn);
		return null;
	}

	public Object visitBinary(BinaryExpr binary, Object args) {
		binary.lhs.accept(this, args);
		binary.rhs.accept(this, args);
		writer.visitInsn(binaryOperatorInsn(binary.lhs.returnType(), binary.operator));
		return null;
	}

	public Object visitCall(CallExpr fcall, Object args) {
		fcall.fload.accept(this, args);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, args);
		}
		if (fcall.returnType().kind == Type.TYPE_NONE) {
			writer.visitCallInsn(Opcodes.CALV, fcall.args.length);
		} else {
			writer.visitCallInsn(Opcodes.CALL, fcall.args.length);
		}
		return null;
	}

	public Object visitCast(CastExpr cast, Object args) {
		cast.expr.accept(this, args);
		Type from = cast.expr.returnType();
		Type to = cast.returnType();
		switch (from.kind) {
			case Type.TYPE_BYTE:
				switch (to.kind) {
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.I2L);
						break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.I2F);
						break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.I2D);
						break;
				}
				break;
			case Type.TYPE_SHORT:
				switch (to.kind) {
					case Type.TYPE_BYTE:
						writer.visitInsn(Opcodes.I2B);
						break;
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.I2L);
						break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.I2F);
						break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.I2D);
						break;
				}
				break;
			case Type.TYPE_CHAR:
			case Type.TYPE_INT:
				switch (to.kind) {
					case Type.TYPE_BYTE:
						writer.visitInsn(Opcodes.I2B);
						break;
					case Type.TYPE_SHORT:
						writer.visitInsn(Opcodes.I2S);
						break;
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.I2L);
						break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.I2F);
						break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.I2D);
						break;
				}
				break;
			case Type.TYPE_LONG:
				switch (to.kind) {
					case Type.TYPE_BYTE:
						writer.visitInsn(Opcodes.L2I);
						writer.visitInsn(Opcodes.I2B);
						break;
					case Type.TYPE_SHORT:
						writer.visitInsn(Opcodes.L2I);
						writer.visitInsn(Opcodes.I2S);
						break;
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.L2I);
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_INT:
						writer.visitInsn(Opcodes.L2I);
						break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.L2F);
						break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.L2D);
						break;
				}
				break;
			case Type.TYPE_FLOAT:
				switch (to.kind) {
					case Type.TYPE_BYTE:
						writer.visitInsn(Opcodes.F2I);
						writer.visitInsn(Opcodes.I2B);
						break;
					case Type.TYPE_SHORT:
						writer.visitInsn(Opcodes.F2I);
						writer.visitInsn(Opcodes.I2S);
						break;
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.F2I);
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_INT:
						writer.visitInsn(Opcodes.F2I);
						break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.F2L);
						break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.F2D);
						break;
				}
				break;
			case Type.TYPE_DOUBLE:
				switch (to.kind) {
					case Type.TYPE_BYTE:
						writer.visitInsn(Opcodes.D2I);
						writer.visitInsn(Opcodes.I2B);
						break;
					case Type.TYPE_SHORT:
						writer.visitInsn(Opcodes.D2I);
						writer.visitInsn(Opcodes.I2S);
						break;
					case Type.TYPE_CHAR:
						writer.visitInsn(Opcodes.D2I);
						writer.visitInsn(Opcodes.I2C);
						break;
					case Type.TYPE_INT:
						writer.visitInsn(Opcodes.D2I);
						break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.D2L);
						break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.D2F);
						break;
				}
				break;
		}
		return null;
	}

	public Object visitComparison(ComparisonExpr cmp, Object args) {
		Label lfalse = new Label();
		Label lafter = new Label();
		visitCondition(cmp, lfalse, false);
		writer.visitLdcInsn(Boolean.TRUE);
		writer.visitJumpInsn(Opcodes.GOTO, lafter);
		writer.visitLabel(lfalse);
		writer.visitLdcInsn(Boolean.FALSE);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitConst(ConstExpr cexpr, Object args) {
		if (env.debug) writer.visitLine(cexpr.lineNumber());
		Object obj = cexpr.value;
		if (obj instanceof Function) {
			writer.visitLdFunc(((Function)obj).signature);
		} else {
			writer.visitLdcInsn(obj);
		}
		return null;
	}

	public Object visitConcat(ConcatExpr concat, Object args) {
		if (concat.exprs.size() == 1) {
			Expr str1 = (Expr)concat.exprs.get(0);
			str1.accept(this, args);
		} else {
			writer.visitLdFunc("Any.tostr");
			writer.visitLdFunc("StrBuf.new");
			writer.visitCallInsn(Opcodes.CALL, 0);
			for (int i=0; i<concat.exprs.size(); i++) {
				Expr expr = (Expr)concat.exprs.get(i);
				if (expr.returnType().kind == Type.TYPE_CHAR) {
					writer.visitLdcInsn("StrBuf.addch");
				} else {
					writer.visitLdcInsn("StrBuf.append");
				}
				writer.visitInsn(Opcodes.SWAP);
				expr.accept(this, args);
				writer.visitCallInsn(Opcodes.CALL, 2);
			}
			writer.visitCallInsn(Opcodes.CALL, 1);
		}
		return null;
	}

	public Object visitIfElse(IfElseExpr ifelse, Object args) {
		Label lelse = new Label();
		Label lafter = new Label();
		// writing if condition
		visitCondition(ifelse.condition, lelse, false);
		// writing if body
		ifelse.ifexpr.accept(this, args);
		// writing else body
		writer.visitJumpInsn(Opcodes.GOTO, lafter);
		writer.visitLabel(lelse);
		ifelse.elseexpr.accept(this, args);
		writer.visitLabel(lafter);
		return null;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object args) {
		if (env.debug) writer.visitLine(newarray.lineNumber());
		for (int i=0; i<newarray.lengthExprs.length; i++) {
			newarray.lengthExprs[i].accept(this, args);
		}
		Type type = newarray.returnType();
		for (int i=0; i<newarray.lengthExprs.length; i++) {
			type = ((ArrayType)type).elementType;
		}
		int typebyte;
		switch (type.kind) {
			case Type.TYPE_BOOL:   typebyte = Arrays.AR_BOOLEAN; break;
			case Type.TYPE_BYTE:   typebyte = Arrays.AR_BYTE;    break;
			case Type.TYPE_CHAR:   typebyte = Arrays.AR_CHAR;    break;
			case Type.TYPE_SHORT:  typebyte = Arrays.AR_SHORT;   break;
			case Type.TYPE_INT:    typebyte = Arrays.AR_INT;     break;
			case Type.TYPE_LONG:   typebyte = Arrays.AR_LONG;    break;
			case Type.TYPE_FLOAT:  typebyte = Arrays.AR_FLOAT;   break;
			case Type.TYPE_DOUBLE: typebyte = Arrays.AR_DOUBLE;  break;
			default:               typebyte = Arrays.AR_OBJECT;  break;
		}
		writer.visitNewMultiArray(newarray.lengthExprs.length, typebyte);
		return null;
	}

	public Object visitNewArrayInit(NewArrayInitExpr newarray, Object args) {
		if (env.debug) writer.visitLine(newarray.lineNumber());
		writer.visitLdcInsn(Int32.toInt32(newarray.initializers.length));
		Type objType = newarray.returnType();
		int newArrayInsn = Opcodes.NEWAA;
		int storeInsn = Opcodes.AASTORE;
		if (objType.kind == Type.TYPE_ARRAY) {
			switch (((ArrayType)objType).elementType.kind) {
				case Type.TYPE_BOOL:
					newArrayInsn = Opcodes.NEWZA;
					storeInsn = Opcodes.ZASTORE;
					break;
				case Type.TYPE_BYTE:
					newArrayInsn = Opcodes.NEWBA;
					storeInsn = Opcodes.BASTORE;
					break;
				case Type.TYPE_CHAR:
					newArrayInsn = Opcodes.NEWCA;
					storeInsn = Opcodes.CASTORE;
					break;
				case Type.TYPE_SHORT:
					newArrayInsn = Opcodes.NEWSA;
					storeInsn = Opcodes.SASTORE;
					break;
				case Type.TYPE_INT:
					newArrayInsn = Opcodes.NEWIA;
					storeInsn = Opcodes.IASTORE;
					break;
				case Type.TYPE_LONG:
					newArrayInsn = Opcodes.NEWLA;
					storeInsn = Opcodes.LASTORE;
					break;
				case Type.TYPE_FLOAT:
					newArrayInsn = Opcodes.NEWFA;
					storeInsn = Opcodes.FASTORE;
					break;
				case Type.TYPE_DOUBLE:
					newArrayInsn = Opcodes.NEWDA;
					storeInsn = Opcodes.DASTORE;
					break;
			}
		}
		writer.visitInsn(newArrayInsn);
		for (int i=0; i<newarray.initializers.length; i++) {
			Expr e = newarray.initializers[i];
			if (e != null) {
				writer.visitInsn(Opcodes.DUP);
				writer.visitLdcInsn(Int32.toInt32(i));
				e.accept(this, args);
				writer.visitInsn(storeInsn);
			}
		}
		return null;
	}

	public Object visitRange(RangeExpr expr, Object args) {
		boolean isInt = expr.returnType().kind == Type.TYPE_INTRANGE;
		writer.visitLdcInsn(Int32.toInt32(2));
		writer.visitInsn(isInt ? Opcodes.NEWIA : Opcodes.NEWLA);
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(Int32.ZERO);
		expr.fromExpr.accept(this, args);
		writer.visitInsn(isInt ? Opcodes.IASTORE : Opcodes.LASTORE);
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(Int32.ONE);
		expr.toExpr.accept(this, args);
		writer.visitInsn(isInt ? Opcodes.IASTORE : Opcodes.LASTORE);
		return null;
	}

	public Object visitSequential(SequentialExpr expr, Object args) {
		for (int vi=0; vi<expr.seqVars.length; vi++) addVar(expr.seqVars[vi]);
		for (int i=0; i < expr.seqExprs.length; i++) {
			expr.seqExprs[i].accept(this, args);
			writer.visitVarInsn(Opcodes.STORE, getVarIndex(expr.seqVars[i]));
		}
		expr.lastExpr.accept(this, args);
		for (int vi=0; vi<expr.seqVars.length; vi++) removeVar(expr.seqVars[vi]);
		return null;
	}

	public Object visitTryCatch(TryCatchExpr trycatch, Object args) {
		Label trystart = new Label();
		Label tryend = new Label();
		Label aftertry = new Label();
		writer.visitLabel(trystart);
		trycatch.tryExpr.accept(this, args);
		writer.visitJumpInsn(Opcodes.GOTO, aftertry);
		writer.visitLabel(tryend);
		writer.visitTryCatchHandler(trystart, tryend);
		writer.visitInsn(Opcodes.POP);
		trycatch.catchExpr.accept(this, args);
		writer.visitLabel(aftertry);
		return null;
	}

	public Object visitUnary(UnaryExpr unary, Object args) {
		int typekind = unary.returnType().kind;
		switch (unary.operator) {
			case '+':
				unary.expr.accept(this, args);
				break;
			case '-':
				unary.expr.accept(this, args);
				switch (typekind) {
					case Type.TYPE_INT:
						writer.visitInsn(Opcodes.INEG); break;
					case Type.TYPE_LONG:
						writer.visitInsn(Opcodes.LNEG); break;
					case Type.TYPE_FLOAT:
						writer.visitInsn(Opcodes.FNEG); break;
					case Type.TYPE_DOUBLE:
						writer.visitInsn(Opcodes.DNEG); break;
				}
				break;
			case '~':
				unary.expr.accept(this, args);
				if (typekind == Type.TYPE_INT) {
					writer.visitLdcInsn(Int32.M_ONE);
					writer.visitInsn(Opcodes.IXOR);
				} else if (typekind == Type.TYPE_LONG) {
					writer.visitLdcInsn(new Int64(-1));
					writer.visitInsn(Opcodes.LXOR);
				}
				break;
			case '!':
				writer.visitLdcInsn(Int32.ONE);
				unary.expr.accept(this, args);
				writer.visitInsn(Opcodes.ISUB);
				break;
		}
		return null;
	}

	public Object visitVar(VarExpr expr, Object args) {
		Var var = expr.var;
		if (unit.getVar(var.name) == var) {
			// global var
			writer.visitLdcInsn(var.name);
			if (var.defaultValue != null) {
				writer.visitLdcInsn(var.defaultValue);
				writer.visitInsn(Opcodes.GETGLOBALDEF);
			} else {
				writer.visitInsn(Opcodes.GETGLOBAL);
			}
		} else {
			// local var
			writer.visitVarInsn(Opcodes.LOAD, getVarIndex(var));
		}
		return null;
	}

	/* STATEMENT VISITING METHODS */

	public Object visitArraySetStatement(ArraySetStatement stat, Object args) {
		stat.arrayExpr.accept(this, args);
		stat.indexExpr.accept(this, args);
		stat.assignExpr.accept(this, args);
		int storeInsn = Opcodes.AASTORE;
		Type arrayType = stat.arrayExpr.returnType();
		if (arrayType.kind == Type.TYPE_ARRAY) {
			switch (((ArrayType)arrayType).elementType.kind) {
				case Type.TYPE_BOOL:   storeInsn = Opcodes.ZASTORE; break;
				case Type.TYPE_BYTE:   storeInsn = Opcodes.BASTORE; break;
				case Type.TYPE_CHAR:   storeInsn = Opcodes.CASTORE; break;
				case Type.TYPE_SHORT:  storeInsn = Opcodes.SASTORE; break;
				case Type.TYPE_INT:    storeInsn = Opcodes.IASTORE; break;
				case Type.TYPE_LONG:   storeInsn = Opcodes.LASTORE; break;
				case Type.TYPE_FLOAT:  storeInsn = Opcodes.FASTORE; break;
				case Type.TYPE_DOUBLE: storeInsn = Opcodes.DASTORE; break;
			}
		}
		writer.visitInsn(storeInsn);
		return null;
	}

	public Object visitAssignStatement(AssignStatement stat, Object args) {
		Var var = stat.var;
		if (unit.getVar(var.name) == var) {
			// global var
			writer.visitLdcInsn(var.name);
			stat.assignExpr.accept(this, args);
			writer.visitInsn(Opcodes.SETGLOBAL);
		} else {
			// local var
			stat.assignExpr.accept(this, args);
			writer.visitVarInsn(Opcodes.STORE, getVarIndex(var));
		}
		return null;
	}

	public Object visitBlockStatement(BlockStatement block, Object args) {
		Object[] varnames = block.vars.keys();
		for (int vi=0; vi<varnames.length; vi++) addVar((Var)block.vars.get(varnames[vi]));
		for (int i=0; i<block.statements.size(); i++) {
			Statement stat = (Statement)block.statements.get(i);
			stat.accept(this, args);
		}
		for (int vi=0; vi<varnames.length; vi++) removeVar((Var)block.vars.get(varnames[vi]));
		return null;
	}

	public Object visitBreakStatement(BreakStatement stat, Object args) {
		if (env.debug) writer.visitLine(stat.lineNumber());
		writer.visitJumpInsn(Opcodes.GOTO, loopEnd);
		return null;
	}

	public Object visitCompoundAssignStatement(CompoundAssignStatement stat, Object args) {
		Var var = stat.var;
		if (unit.getVar(var.name) == var) {
			// global var
			writer.visitLdcInsn(var.name);
			writer.visitInsn(Opcodes.DUP);
			if (var.defaultValue != null) {
				writer.visitLdcInsn(var.defaultValue);
				writer.visitInsn(Opcodes.GETGLOBALDEF);
			} else {
				writer.visitInsn(Opcodes.GETGLOBAL);
			}
			stat.assignExpr.accept(this, args);
			writer.visitInsn(binaryOperatorInsn(var.type, Token.getBinaryOperator(stat.assignOperator)));
			writer.visitInsn(Opcodes.SETGLOBAL);
		} else {
			// local var
			if (stat.assignExpr.kind == Expr.EXPR_CONST && stat.var.type.kind == Type.TYPE_INT
					&& (stat.assignOperator == Token.PLUSEQ || stat.assignOperator == Token.MINUSEQ)) {
				int incr = ((Int32)((ConstExpr)stat.assignExpr).value).value;
				if (stat.assignOperator == Token.MINUSEQ) incr = -incr;
				if (incr >= Byte.MIN_VALUE && incr <= Byte.MAX_VALUE) {
					if (env.debug) writer.visitLine(stat.lineNumber());
					writer.visitIincInsn(getVarIndex(var), incr);
					return null;
				}
			}
			writer.visitVarInsn(Opcodes.LOAD, getVarIndex(var));
			stat.assignExpr.accept(this, args);
			writer.visitInsn(binaryOperatorInsn(stat.var.type, Token.getBinaryOperator(stat.assignOperator)));
			writer.visitVarInsn(Opcodes.STORE, getVarIndex(var));
		}
		return null;
	}

	public Object visitContinueStatement(ContinueStatement stat, Object args) {
		if (env.debug) writer.visitLine(stat.lineNumber());
		writer.visitJumpInsn(Opcodes.GOTO, loopStart);
		return null;
	}

	public Object visitEmptyStatement(EmptyStatement stat, Object args) {
		return null;
	}

	public Object visitExprStatement(ExprStatement stat, Object args) {
		stat.expr.accept(this, args);
		if (stat.expr.returnType().kind != Type.TYPE_NONE) {
			writer.visitInsn(Opcodes.POP);
		}
		return null;
	}

	public Object visitForLoopStatement(ForLoopStatement stat, Object args) {
		Label outerLoopStart = loopStart;
		Label outerLoopEnd = loopEnd;
		loopStart = new Label();
		loopEnd = new Label();
		// at first iteration we jump over increment
		Label afterIncr = new Label();
		writer.visitJumpInsn(Opcodes.GOTO, afterIncr);
		writer.visitLabel(loopStart);
		stat.increment.accept(this, args);
		writer.visitLabel(afterIncr);
		if (stat.body.kind == Statement.STAT_EMPTY) {
			visitCondition(stat.condition, loopStart, true);
		} else {
			visitCondition(stat.condition, loopEnd, false);
			stat.body.accept(this, args);
			writer.visitJumpInsn(Opcodes.GOTO, loopStart);
		}
		writer.visitLabel(loopEnd);
		loopStart = outerLoopStart;
		loopEnd = outerLoopEnd;
		return null;
	}

	public Object visitIfStatement(IfStatement stat, Object args) {
		Label elseBranch = new Label();
		Label afterIf = new Label();
		visitCondition(stat.condition, elseBranch, false);
		stat.ifstat.accept(this, args);
		if (stat.elsestat.kind != Statement.STAT_EMPTY && stat.elsestat.accept(flow, null) == flow.NEXT) {
			writer.visitJumpInsn(Opcodes.GOTO, afterIf);
		}
		writer.visitLabel(elseBranch);
		stat.elsestat.accept(this, args);
		writer.visitLabel(afterIf);
		return null;
	}

	public Object visitLoopStatement(LoopStatement stat, Object args) {
		Label outerLoopStart = loopStart;
		Label outerLoopEnd = loopEnd;
		loopStart = new Label();
		loopEnd = new Label();
		writer.visitLabel(loopStart);
		stat.preBody.accept(this, args);
		if (stat.postBody.kind == Statement.STAT_EMPTY) {
			visitCondition(stat.condition, loopStart, true);
		} else {
			visitCondition(stat.condition, loopEnd, false);
			stat.postBody.accept(this, args);
			writer.visitJumpInsn(Opcodes.GOTO, loopStart);
		}
		writer.visitLabel(loopEnd);
		loopStart = outerLoopStart;
		loopEnd = outerLoopEnd;
		return null;
	}

	public Object visitReturnStatement(ReturnStatement stat, Object args) {
		if (stat.expr.kind == Expr.EXPR_CONST && ((ConstExpr)stat.expr).value == Null.NULL) {
			if (env.debug) writer.visitLine(stat.lineNumber());
			writer.visitInsn(Opcodes.RET_NULL);
		} else {
			stat.expr.accept(this, args);
			writer.visitInsn(Opcodes.RETURN);
		}
		return null;
	}

	public Object visitThrowStatement(ThrowStatement stat, Object args) {
		stat.errCodeExpr.accept(this, args);
		stat.errMsgExpr.accept(this, args);
		writer.visitInsn(Opcodes.THROW);
		return null;
	}

	public Object visitTryCatchStatement(TryCatchStatement trycatch, Object args) {
		Label tryStart = new Label();
		Label tryEnd = new Label();
		Label afterTry = new Label();
		writer.visitLabel(tryStart);
		trycatch.tryStat.accept(this, args);
		if (trycatch.tryStat.accept(flow, null) == flow.NEXT) {
			writer.visitJumpInsn(Opcodes.GOTO, afterTry);
		}
		writer.visitLabel(tryEnd);
		writer.visitTryCatchHandler(tryStart, tryEnd);
		if (trycatch.catchVar == null) {
			writer.visitInsn(Opcodes.POP);
		} else {
			addVar(trycatch.catchVar);
			writer.visitVarInsn(Opcodes.STORE, getVarIndex(trycatch.catchVar));
		}
		trycatch.catchStat.accept(this, args);
		if (trycatch.catchVar != null) {
			removeVar(trycatch.catchVar);
		}
		writer.visitLabel(afterTry);
		return null;
	}
}

// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.

public class StaticTypeCheck {

	public static TypeMap typing(Declarations d) {
		TypeMap map = new TypeMap();
		for (Declaration di : d)
			map.put(di.v, di.t);
		return map;
	}

	public static void check(boolean test, String msg) {
		if (test)
			return; // 식이 참이면 ret
		System.err.println(msg); // 거짓이면 error
		System.exit(1); // 종료
	}

	// V : Validity Check
	public static void V(Declarations d) { // 선언들에 대한 Validity
		for (int i = 0; i < d.size() - 1; i++)
			for (int j = i + 1; j < d.size(); j++) {
				Declaration di = d.get(i);
				Declaration dj = d.get(j);
				check(!(di.v.equals(dj.v)), // dec이 중복되면 error
						"duplicate declaration: " + dj.v);
			}
	}

	public static void V(Program p) { // Program에 대한 Validity
		V(p.decpart); // Dec part가 Valid
		V(p.body, typing(p.decpart)); // Body가 TypeMap에 대해 Valid
	}

	// 수식에 대한 Type을 돌려줌
	public static Type typeOf(Expression e, TypeMap tm) {
		if (e instanceof Value)
			return ((Value) e).type; // value면 값의 type ret
		if (e instanceof Variable) {
			Variable v = (Variable) e; // 변수면 Variable로 바꾸고
			check(tm.containsKey(v), "undefined variable: " + v); // 선언되어있는지
			// 중복Check임, Validity에서 check하기 때문에
			return (Type) tm.get(v);
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			if (b.op.ArithmeticOp()) // 산술연산자
				if (typeOf(b.term1, tm) == Type.FLOAT) // term1이 float
					return (Type.FLOAT);
				else
					return (Type.INT);
			if (b.op.RelationalOp() || b.op.BooleanOp())
				return (Type.BOOL);
		}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			if (u.op.NotOp())
				return (Type.BOOL);
			else if (u.op.NegateOp())
				return typeOf(u.term, tm); // -int = int, -float = float
			else if (u.op.intOp())
				return (Type.INT); // Casting 연산자
			else if (u.op.floatOp())
				return (Type.FLOAT);
			else if (u.op.charOp())
				return (Type.CHAR);
		}
		if (e instanceof GetInt) {
			GetInt i = (GetInt) e;
//			return i.intvalue.type;
			return (Type.INT);
		}
		if (e instanceof GetFloat) {
			GetFloat i = (GetFloat) e;
//			return i.floatvalue.type;
			return (Type.FLOAT);
		}
		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Expression e, TypeMap tm) { // 식의 validity, 그림 6.3
		if (e instanceof Value || e instanceof GetInt || e instanceof GetFloat)
			return;
		if (e instanceof Variable) {
			Variable v = (Variable) e;
			check(tm.containsKey(v), "undeclared variable: " + v);
			return;
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			Type typ1 = typeOf(b.term1, tm);
			Type typ2 = typeOf(b.term2, tm);
			V(b.term1, tm);
			V(b.term2, tm);
			if (b.op.ArithmeticOp())
				check(typ1 == typ2 && (typ1 == Type.INT || typ1 == Type.FLOAT), "type error for " + b.op);
			// 타입이 둘 다 INT, FLOAT로 같아야됨, int + float는 허용x
			else if (b.op.RelationalOp())
				check(typ1 == typ2, "type error for " + b.op);
			else if (b.op.BooleanOp())
				check(typ1 == Type.BOOL && typ2 == Type.BOOL, b.op + ": non-bool operand");
			else
				throw new IllegalArgumentException("should never reach here");
			return;
		}
		// student exercise
		else if (e instanceof Unary) {
			Unary u = (Unary) e;
			Type type = typeOf(u.term, tm);
			V(u.term, tm);
			if (u.op.NotOp()) {
				check(type == Type.BOOL, "type error for " + u.op);
			} else if (u.op.NegateOp()) {
				check(type == Type.INT || type == Type.FLOAT, "type error for " + u.op);
			}
		}
		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Statement s, TypeMap tm) {
		if (s == null)
			throw new IllegalArgumentException("AST error: null statement");
		if (s instanceof Skip)
			return;
		if (s instanceof Assignment) {
			Assignment a = (Assignment) s;
			check(tm.containsKey(a.target), " undefined target in assignment: " + a.target);
			V(a.source, tm);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = typeOf(a.source, tm);
			if (ttype != srctype) {
				if (ttype == Type.FLOAT)
					check(srctype == Type.INT, "mixed mode assignment to " + a.target);
				else if (ttype == Type.INT)
					check(srctype == Type.CHAR, "mixed mode assignment to " + a.target);
				else
					check(false, "mixed mode assignment to " + a.target);
			}
			return;
		}
		// student exercise
		if (s instanceof Conditional) {
			Conditional c = (Conditional) s;
			V(c.test, tm);
			Type type = typeOf(c.test, tm);
			if (type == Type.BOOL) {
				V(c.thenbranch, tm);
				V(c.elsebranch, tm);
			} else
				check(false, "Condition type is not Bool, input : " + c.test);
			return;
		}
		if (s instanceof Loop) {
			Loop l = (Loop) s;
			V(l.test, tm);
			Type type = typeOf(l.test, tm);
			if (type == Type.BOOL) {
				V(l.body, tm);
			} else
				check(false, "Loop condition type is not Bool, input : " + l.test);
			return;
		}
		if (s instanceof Block) {
			Block b = (Block) s;
			for (Statement statement : b.members) {
				V(statement, tm);
			}
			return;
		}
		if (s instanceof Put) {
			Put p = (Put) s;
			V(p.term, tm);
			return;
		}
		throw new IllegalArgumentException("should never reach here");
	}

	// TODO
	public static void main(String args[]) {
		Parser parser = new Parser(new Lexer("LexerExample.txt"));
		Program prog = parser.program();
		prog.display(); // student exercise
		System.out.println("\nBegin type checking...");
		System.out.print("Type map: ");
		TypeMap map = typing(prog.decpart);
		map.display(); // student exercise
		V(prog);
	} // main

} // class StaticTypeCheck

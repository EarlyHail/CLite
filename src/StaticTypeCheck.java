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
			return; // ���� ���̸� ret
		System.err.println(msg); // �����̸� error
		System.exit(1); // ����
	}

	// V : Validity Check
	public static void V(Declarations d) { // ����鿡 ���� Validity
		for (int i = 0; i < d.size() - 1; i++)
			for (int j = i + 1; j < d.size(); j++) {
				Declaration di = d.get(i);
				Declaration dj = d.get(j);
				check(!(di.v.equals(dj.v)), // dec�� �ߺ��Ǹ� error
						"duplicate declaration: " + dj.v);
			}
	}

	public static void V(Program p) { // Program�� ���� Validity
		V(p.decpart); // Dec part�� Valid
		V(p.body, typing(p.decpart)); // Body�� TypeMap�� ���� Valid
	}

	// ���Ŀ� ���� Type�� ������
	public static Type typeOf(Expression e, TypeMap tm) {
		if (e instanceof Value)
			return ((Value) e).type; // value�� ���� type ret
		if (e instanceof Variable) {
			Variable v = (Variable) e; // ������ Variable�� �ٲٰ�
			check(tm.containsKey(v), "undefined variable: " + v); // ����Ǿ��ִ���
			// �ߺ�Check��, Validity���� check�ϱ� ������
			return (Type) tm.get(v);
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			if (b.op.ArithmeticOp()) // ���������
				if (typeOf(b.term1, tm) == Type.FLOAT) // term1�� float
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
				return (Type.INT); // Casting ������
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

	public static void V(Expression e, TypeMap tm) { // ���� validity, �׸� 6.3
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
			// Ÿ���� �� �� INT, FLOAT�� ���ƾߵ�, int + float�� ���x
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

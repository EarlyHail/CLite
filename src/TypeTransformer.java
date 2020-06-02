import java.util.*;

//float = int일 때 float = (i2f) int
//+인 경우 int+, float+일 수도 있음, type에 맞게 +를 고쳐줘야됨.

public class TypeTransformer {

	public static Program T(Program p, TypeMap tm) { // Transform된 Program return
		Block body = (Block) T(p.body, tm);
		return new Program(p.decpart, body);
	}

	public static Expression T(Expression e, TypeMap tm) {
		if (e instanceof Value)
			return e;
		if (e instanceof Variable)
			return e;
		if (e instanceof Binary) { // 이항식이면
			Binary b = (Binary) e;
			Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);
			Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);
			Expression t1 = T(b.term1, tm);
			Expression t2 = T(b.term2, tm);
			if (typ1 == Type.INT)
				return new Binary(b.op.intMap(b.op.val), t1, t2); // t1 - t2일 때, -를 int-로
			// 현재 op.val을 가지고 intMap을 찾아서
			// intMap은 AbstractSyntax.java에 intMap[]
			else if (typ1 == Type.FLOAT)
				return new Binary(b.op.floatMap(b.op.val), t1, t2);
			else if (typ1 == Type.CHAR)
				return new Binary(b.op.charMap(b.op.val), t1, t2);
			else if (typ1 == Type.BOOL)
				return new Binary(b.op.boolMap(b.op.val), t1, t2);
			throw new IllegalArgumentException("should never reach here");
		}
		// student exercise
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			Type type = StaticTypeCheck.typeOf(u.term, tm);
			Expression t = T(u.term, tm);
			if (type == Type.INT) // op가 negateOp인지 확인 해야하나.....????
				return new Unary(u.op.intMap(u.op.val), t);
			if (type == Type.FLOAT)
				return new Unary(u.op.floatMap(u.op.val), t);
			if (type == Type.BOOL)
				return new Unary(u.op.boolMap(u.op.val), t);
		}
		if (e instanceof GetInt) {
			return e;
		}
		if (e instanceof GetFloat) {
			return e;
		}
		//TODO
		throw new IllegalArgumentException("should never reach here");
	}

	public static Statement T(Statement s, TypeMap tm) {
		if (s instanceof Skip)
			return s;
		if (s instanceof Assignment) {
			Assignment a = (Assignment) s;
			Variable target = a.target;
			Expression src = T(a.source, tm);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = StaticTypeCheck.typeOf(a.source, tm);
			if (ttype == Type.FLOAT) {
				if (srctype == Type.INT) {
					src = new Unary(new Operator(Operator.I2F), src);
					srctype = Type.FLOAT;
				}
			} else if (ttype == Type.INT) {
				if (srctype == Type.CHAR) {
					src = new Unary(new Operator(Operator.C2I), src);
					srctype = Type.INT;
				}
			}
			StaticTypeCheck.check(ttype == srctype, "bug in assignment to " + target);
			return new Assignment(target, src);
		}
		if (s instanceof Conditional) {
			Conditional c = (Conditional) s;
			Expression test = T(c.test, tm);
			Statement tbr = T(c.thenbranch, tm);
			Statement ebr = T(c.elsebranch, tm);
			return new Conditional(test, tbr, ebr);
		}
		if (s instanceof Loop) {
			Loop l = (Loop) s;
			Expression test = T(l.test, tm);
			Statement body = T(l.body, tm);
			return new Loop(test, body);
		}
		if (s instanceof Block) {
			Block b = (Block) s;
			Block out = new Block();
			for (Statement stmt : b.members)
				out.members.add(T(stmt, tm));
			return out;
		}
		if (s instanceof Put) {
			Put p = (Put) s;
			Expression term = T(p.term, tm);
			return new Put(term);
		}
		throw new IllegalArgumentException("should never reach here");
	}

	public static void main(String args[]) {
		Parser parser = new Parser(new Lexer("LexerExample.txt"));
		Program prog = parser.program();
		prog.display(); // student exercise
		System.out.println("\nBegin type checking...");
		System.out.print("Type map: ");
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		map.display(); // student exercise
		StaticTypeCheck.V(prog);
		Program out = T(prog, map);
		System.out.println("Output AST");
        out.display();    // student exercise
	} // main
} // class TypeTransformer

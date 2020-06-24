// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.

public class StaticTypeCheck {
	public static TypeMap typing(Declarations global) {
		TypeMap map = new TypeMap();
		for (Declaration di : global)
			map.put(di.v, di.t);
		return map;
	}

	public static TypeMap typing(Declarations global, Function func) {
		TypeMap map = new TypeMap();
		for (Declaration pi : func.params)
			map.put(pi.v, pi.t);
		for (Declaration li : func.locals)
			map.put(li.v, li.t);
		return map;
	}

	public static TypeMap typing(Declarations global, Functions funcs) {
		TypeMap map = new TypeMap();
		for (int i = 0; i < global.size(); i++) {
			Declaration di = (Declaration) global.get(i);
			map.put(di.v, di.t);
		}
		for (int i = 0; i < funcs.size(); i++) {
			Function fi = (Function) funcs.get(i);
			map.put(new Variable(fi.id), new ProtoType(fi.type, fi.params));
		}
		/*
		 * for (Declaration pi : func.params) map.put(pi.v, pi.t); for (Declaration li :
		 * func.locals) map.put(li.v, li.t);
		 */
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
				if (di.t == Type.VOID || dj.t == Type.VOID) {
					check(false, "void can be variable's type!");
				}
				check(!(di.v.equals(dj.v)), // dec이 중복되면 error
						"duplicate declaration: " + dj.v);
			}
	}

	public static void V(Program p) { // Program에 대한 Validity
		V(p.globals);
		TypeMap tmg = typing(p.globals, p.functions);
		boolean foundMain = false;

//		ArrayList<TypeMap> funcs_map = new ArrayList<TypeMap>();
		/*
		 * for (Function func : p.functions) { funcs_map.add(typing(p.globals, func)); }
		 */
		int i = 0;
		for (Function func : p.functions) {
			if (func.id.equals("main")) {
				if (foundMain) {
					check(false, "Duplicate main function");
				} else {
					foundMain = true;
				}
			}
			V(func.params, func.locals);
			TypeMap tmf = typing(func.params).onion(typing(func.locals));
			tmf = tmg.onion(tmf);
			V(func.body, tmf, p.functions);
			i++;
		}
		if (!foundMain)
			check(false, "Main not found");
		/*
		 * for (int i = 0; i < p.functions.size(); i++) { V(p.functions.get(i).params);
		 * V(p.functions.get(i).locals); // 각 함수들의 param과 local이 valid, unique id } for
		 * (int i = 0; i < p.functions.size(); i++) { V(p.functions.get(i).body,
		 * typing(p.globals, p.functions.get(i)), p.functions); // 각 함수들의 body가 valid }
		 */
		// V(p.functions, typing(p.globals)); // Body가 TypeMap에 대해 Valid
	}

	private static void V(Declarations params, Declarations locals) {
		// TODO Auto-generated method stub
		V(params);
		V(locals);
		for (int i = 0; i < params.size(); i++) {
			for (int j = 0; j < locals.size(); j++) {
				Declaration d1 = params.get(i);
				Declaration d2 = locals.get(i);
				if (d1.t == Type.VOID || d2.t == Type.VOID) {
					check(false, "Variable type Can't be VOID");
				}
				check(!d1.v.toString().equals(d2.v.toString()), "variable duplicated");
			}
		}
	}

	public static void V(Declarations ds, Functions fs) {
		// TODO Auto-generated method stub

	}

	public static void V(Function func, TypeMap tm, Functions funcs) {
		V(func.params); // 파라미터에 대한 validity
		V(func.locals); // 내부 선언에 대한 validity
		boolean hasReturn = false;
		for (Statement statement : func.body.members) {
			if (statement instanceof Return) {
				hasReturn = true;
				V(statement, tm, funcs);
				break;
			}
			V(statement, tm, funcs);
		}
		if (!func.id.equals("main")) {
			if (func.type == Type.VOID && hasReturn) {
				System.err.println("void function can't return");
				System.exit(1);
			} else if (func.type != Type.VOID && hasReturn == false) {
				System.err.println("not void function must return");
				System.exit(1);
			}
		}
	}

	// 수식에 대한 Type을 돌려줌
	public static Type typeOf(Expression e, TypeMap tm, Functions funcs) {
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
				if (typeOf(b.term1, tm, funcs) == Type.FLOAT) // term1이 float
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
				return typeOf(u.term, tm, funcs); // -int = int, -float = float
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
		if (e instanceof Call) {
			// call한 함수의 return type을 찾아야 한다.
			Call c = (Call) e;
			String name = c.name;
			for (Function func : funcs) {
				if (func.id.equals(name)) {
					return func.type;
				}
			}
		}
		if (e instanceof Return) {
			Return r = (Return) e;
			String name = r.target.toString();
			for (Function func : funcs) {
				if (func.id.equals(name)) {
					return func.type;
				}
			}
			return typeOf(r.result, tm, funcs);
		}
		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Expression e, TypeMap tm, Functions funcs) { // 식의 validity, 그림 6.3
		if (e instanceof Value || e instanceof GetInt || e instanceof GetFloat)
			return;
		if (e instanceof Variable) {
			Variable v = (Variable) e;
			check(tm.containsKey(v), "undeclared variable: " + v);
			return;
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			Type typ1 = typeOf(b.term1, tm, funcs);
			Type typ2 = typeOf(b.term2, tm, funcs);
			V(b.term1, tm, funcs);
			V(b.term2, tm, funcs);
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
			Type type = typeOf(u.term, tm, funcs);
			V(u.term, tm, funcs);
			if (u.op.NotOp()) {
				check(type == Type.BOOL, "type error for " + u.op);
			} else if (u.op.NegateOp()) {
				check(type == Type.INT || type == Type.FLOAT, "type error for " + u.op);
			}
			return;
		} else if (e instanceof Call) {
			Call c = (Call) e;
			V(c, tm, funcs);
			/*
			 * for (Expression expression : c.args) { V(expression, tm, funcs); } String
			 * name = c.name; for (Function func : funcs) { if (func.id.equals(name)) { if
			 * (func.params.size() != c.args.size()) { System.err.
			 * println("Function's Arguments numbers and Call's Param numbers are not same"
			 * ); System.exit(1); } } }
			 */
			return;
		}
		throw new IllegalArgumentException("should never reach here");
	}

	private static void V(Call c, TypeMap tm, Functions funcs) {
		if (c.args != null) {
			for (Expression expression : c.args) {
				V(expression, tm, funcs);
			}
			String name = c.name;
			for (Function func : funcs) {
				if (func.id.equals(name)) {
					if (func.params.size() != c.args.size()) {
						System.err.println("Function's Arguments numbers and Call's Param numbers are not same");
						System.exit(1);
					}
				}
			}
		}
	}

	public static void V(Statement s, TypeMap tm, Functions funcs) {
		if (s == null)
			throw new IllegalArgumentException("AST error: null statement");
		if (s instanceof Skip)
			return;
		if (s instanceof Assignment) {
			Assignment a = (Assignment) s;
			check(tm.containsKey(a.target), " undefined target in assignment: " + a.target);
			V(a.source, tm, funcs);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = typeOf(a.source, tm, funcs);
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
			V(c.test, tm, funcs);
			Type type = typeOf(c.test, tm, funcs);
			if (type == Type.BOOL) {
				V(c.thenbranch, tm, funcs);
				V(c.elsebranch, tm, funcs);
			} else
				check(false, "Condition type is not Bool, input : " + c.test);
			return;
		}
		if (s instanceof Loop) {
			Loop l = (Loop) s;
			V(l.test, tm, funcs);
			Type type = typeOf(l.test, tm, funcs);
			if (type == Type.BOOL) {
				V(l.body, tm, funcs);
			} else
				check(false, "Loop condition type is not Bool, input : " + l.test);
			return;
		}
		if (s instanceof Block) {
			Block b = (Block) s;
			for (Statement statement : b.members) {
				V(statement, tm, funcs);
			}
			return;
		}
		if (s instanceof Put) {
			Put p = (Put) s;
			V(p.term, tm, funcs);
			return;
		}
		if (s instanceof Call) {
			Call c = (Call) s;
			V(c, tm, funcs);
			return;
		}
		if (s instanceof Return) {
			Variable fid = ((Return) s).target;
			check(tm.containsKey(fid), "undefined function: " + fid);
			V(((Return) s).result, tm, funcs);
			check((tm.get(fid).type).equals(typeOf(((Return) s).result, tm, funcs)), "incorrect return type" + fid);
			/*
			 * Return r = (Return) s; V(r.result, tm, funcs);
			 */
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
		TypeMap global = StaticTypeCheck.typing(prog.globals, prog.functions);
		global.display();
		ArrayList<TypeMap> map = new ArrayList<>();
		for (Function func : prog.functions) {
			map.add(StaticTypeCheck.typing(prog.globals, func));
		}
		for (TypeMap m : map) {
			m.display();
		}
		System.out.println("--------------");
		V(prog);
	} // main

} // class StaticTypeCheck

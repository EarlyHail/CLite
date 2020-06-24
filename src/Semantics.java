import java.util.ArrayList;
import java.util.Scanner;

// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

public class Semantics {
	Scanner sc = new Scanner(System.in);

	State addFrame(State current, Call c, Function f, Functions fs, State global) {
		State s = new State(current);
		s = s.minus(current.a - current.dlink);
		s = s.onion(global);
		s = s.allocate(f.params);
		for (int i = 0; i < f.params.size(); i++) {
			Expression e = (Expression) c.args.get(i);// args의 변수
			Declaration d = (Declaration) f.params.get(i);
			Variable v = (Variable) d.v;
			Value temp = M(e, current, fs, global);
			State tempState = new State(v, temp);
			s = s.onion(tempState);
			// param의 값을 args의 값으로 지정
		}
		s = s.allocate(f.locals);
		Declarations ds = new Declarations();
		ds.add(new Declaration(new Variable(f.id), f.type));
		s = s.allocate(ds);
		s.dlink = current.a;
		s.display();
		return s;
	}

	State removeFrame(State current, Call c, Function f, Functions fs, State global) {
		State s = new State(current);
		s.dlink = current.dlink;
		for (int i = 0; i < s.a - s.dlink; i++) {
			s.deallocate(f.locals);
		}
		//s.a = current.dlink;
		return s;
	}

	State M(Program p) {
		State sigmag = new State();
		sigmag = sigmag.allocate(p.globals);
		sigmag.dlink = sigmag.slink = sigmag.a;
		return M(p.functions, sigmag);
		// return M(p.body, initialState(p.decpart));
	}

	State M(Functions fs, State sigmag) {
		Function main = findFunction("main", fs);
		State sigma = new State(sigmag);
		sigma.dlink = sigmag.a;
		sigma = sigma.allocate(main.locals);
		System.out.println("-------Global Declaration State-------");
		sigma.display();
		sigma = M(main.body, sigma, fs, sigmag);
		sigma = sigma.deallocate(main.locals);
		sigma = sigmag.onion(sigma);

		return sigma;
	}
	/*
	 * State initialState(Declarations d) { State state = new State(); Value
	 * intUndef = new IntValue(); for (Declaration decl : d) state.put(decl.v,
	 * Value.mkValue(decl.t)); return state; }
	 */

	private Function findFunction(String string, Functions fs) {
		for (Function f : fs) {
			if (f.id.contentEquals("main"))
				return f;
		}
		return null;
	}

	State M(Statement s, State state, Functions fs, State global) {
		if (s instanceof Skip)
			return M((Skip) s, state, global);
		if (s instanceof Assignment)
			return M((Assignment) s, state, fs, global);
		if (s instanceof Conditional)
			return M((Conditional) s, state, fs, global);
		if (s instanceof Loop)
			return M((Loop) s, state, fs, global);
		if (s instanceof Block)
			return M((Block) s, state, fs, global);
		if (s instanceof Put) {
			return M((Put) s, state, fs, global);
		}
		if (s instanceof Return) {
			return M((Return) s, state, fs, global);
		}
		if (s instanceof Call) {
			return M((Call) s, state, fs, global);
		}
		throw new IllegalArgumentException("should never reach here");
	}

	State M(Skip s, State state, State global) {
		return state;
	}

	State M(Assignment a, State state, Functions fs, State global) {
		Value arg = M(a.source, state, fs, global);
		State test = state.onion(a.target, arg);
		return test;
	}

	State M(Block b, State sigma, Functions fs, State global) {
		int n = b.members.size();
		Statement s;
		for (int i = 0; i < n; i++) {
			s = (Statement) b.members.get(i);
			sigma = M(s, sigma, fs, global);
			if (s instanceof Return)
				return sigma;
		}
		/*
		 * for (Statement s : b.members) state = M(s, state);
		 */
		return sigma;
	}

	State M(Return r, State sigma, Functions fs, State global) {
		return sigma.onion(new State(r.target, M(r.result, sigma, fs, global)));
	}

	State M(Call c, State state, Functions fs, State global) {
		for (Function f : fs) {
			if (f.id.contentEquals(c.name)) {
				System.out.println("\n-------Before Function Call-------");
				state.display();
				System.out.println();
				System.out.println("-------"+f.id+ " Function Call-------");
				
				state = addFrame(state, c, f, fs, global);
				for (Statement s : f.body.members) {
					M(s, state, fs, global);

				}
				System.out.println("\n-------end Function-------");
				state.display();
				state = removeFrame(state, c, f, fs, global);
			}
		}
		return state;
	}

	State M(Conditional c, State state, Functions fs, State global) {
		if (M(c.test, state, fs, global).boolValue())
			return M(c.thenbranch, state, fs, global);
		else
			return M(c.elsebranch, state, fs, global);
	}

	State M(Loop l, State state, Functions fs, State global) {
		if (M(l.test, state, fs, global).boolValue())
			return M(l, M(l.body, state, fs, global), fs, global);
		else
			return state;
	}

	State M(Put p, State state, Functions fs) {
		return state;
	}

	Value applyBinary(Operator op, Value v1, Value v2) {
		StaticTypeCheck.check(!v1.isUndef() && !v2.isUndef(), "reference to undef value");
		if (op.val.equals(Operator.INT_PLUS))
			return new IntValue(v1.intValue() + v2.intValue());
		if (op.val.equals(Operator.INT_MINUS))
			return new IntValue(v1.intValue() - v2.intValue());
		if (op.val.equals(Operator.INT_TIMES))
			return new IntValue(v1.intValue() * v2.intValue());
		if (op.val.equals(Operator.INT_DIV))
			return new IntValue(v1.intValue() / v2.intValue());
		// student exercise
		if (op.val.equals(Operator.INT_LT))
			return new BoolValue(v1.intValue() < v2.intValue());
		if (op.val.equals(Operator.INT_LE))
			return new BoolValue(v1.intValue() <= v2.intValue());
		if (op.val.equals(Operator.INT_EQ))
			return new BoolValue(v1.intValue() == v2.intValue());
		if (op.val.equals(Operator.INT_NE))
			return new BoolValue(v1.intValue() != v2.intValue());
		if (op.val.equals(Operator.INT_GT))
			return new BoolValue(v1.intValue() > v2.intValue());
		if (op.val.equals(Operator.INT_GE))
			return new BoolValue(v1.intValue() >= v2.intValue());

		if (op.val.equals(Operator.FLOAT_LT))
			return new BoolValue(v1.floatValue() < v2.floatValue());
		if (op.val.equals(Operator.FLOAT_LE))
			return new BoolValue(v1.floatValue() <= v2.floatValue());
		if (op.val.equals(Operator.FLOAT_EQ))
			return new BoolValue(v1.floatValue() == v2.floatValue());
		if (op.val.equals(Operator.FLOAT_NE))
			return new BoolValue(v1.floatValue() != v2.floatValue());
		if (op.val.equals(Operator.FLOAT_GT))
			return new BoolValue(v1.floatValue() > v2.floatValue());
		if (op.val.equals(Operator.FLOAT_GE))
			return new BoolValue(v1.floatValue() >= v2.floatValue());

		if (op.val.equals(Operator.FLOAT_PLUS))
			return new FloatValue(v1.floatValue() + v2.floatValue());
		if (op.val.equals(Operator.FLOAT_MINUS))
			return new FloatValue(v1.floatValue() - v2.floatValue());
		if (op.val.equals(Operator.FLOAT_TIMES))
			return new FloatValue(v1.floatValue() * v2.floatValue());
		if (op.val.equals(Operator.FLOAT_DIV))
			return new FloatValue(v1.floatValue() / v2.floatValue());

		if (op.val.equals(Operator.CHAR_LT))
			return new BoolValue(v1.charValue() < v2.charValue());
		if (op.val.equals(Operator.CHAR_LE))
			return new BoolValue(v1.charValue() <= v2.charValue());
		if (op.val.equals(Operator.CHAR_EQ))
			return new BoolValue(v1.charValue() == v2.charValue());
		if (op.val.equals(Operator.CHAR_NE))
			return new BoolValue(v1.charValue() != v2.charValue());
		if (op.val.equals(Operator.CHAR_GT))
			return new BoolValue(v1.charValue() > v2.charValue());
		if (op.val.equals(Operator.CHAR_GE))
			return new BoolValue(v1.charValue() >= v2.charValue());

		if (op.val.equals(Operator.BOOL_EQ))
			return new BoolValue(v1.boolValue() == v2.boolValue());
		if (op.val.equals(Operator.BOOL_NE))
			return new BoolValue(v1.boolValue() != v2.boolValue());

		if (op.val.equals(Operator.AND))
			return new BoolValue(v1.boolValue() && v2.boolValue());
		if (op.val.equals(Operator.OR))
			return new BoolValue(v1.boolValue() || v2.boolValue());
		throw new IllegalArgumentException("should never reach here");
	}

	Value applyUnary(Operator op, Value v) {
		StaticTypeCheck.check(!v.isUndef(), "reference to undef value");
		if (op.val.equals(Operator.NOT))
			return new BoolValue(!v.boolValue());
		else if (op.val.equals(Operator.INT_NEG))
			return new IntValue(-v.intValue());
		else if (op.val.equals(Operator.FLOAT_NEG))
			return new FloatValue(-v.floatValue());
		else if (op.val.equals(Operator.I2F))
			return new FloatValue((float) (v.intValue()));
		else if (op.val.equals(Operator.F2I))
			return new IntValue((int) (v.floatValue()));
		else if (op.val.equals(Operator.C2I))
			return new IntValue((int) (v.charValue()));
		else if (op.val.equals(Operator.I2C))
			return new CharValue((char) (v.intValue()));
		throw new IllegalArgumentException("should never reach here");
	}

	Value M(Expression e, State state, Functions fs, State global) {
		if (e instanceof Value)
			return (Value) e;
		if (e instanceof Variable) {
			int addr = state.findAddrByVar((Variable) e);
			return (Value) (state.mu.get(addr));
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			return applyBinary(b.op, M(b.term1, state, fs, global), M(b.term2, state, fs, global));
		}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			return applyUnary(u.op, M(u.term, state, fs, global));
		}
		if (e instanceof GetInt) {
			GetInt i = (GetInt) e;
			i.intvalue = new IntValue(sc.nextInt());
			return i.intvalue;
		}
		if (e instanceof GetFloat) {
			GetFloat f = (GetFloat) e;
			f.floatvalue = new FloatValue(sc.nextFloat());
			return f.floatvalue;
		}
		if (e instanceof Call) {
			Call c = (Call) e;
			for (Function f : fs) {
				if (f.id.contentEquals(c.name)) {
					state = addFrame(state, c, f, fs, global);
					Variable ret = null;
					for (Statement s : f.body.members) {
						System.out.print("Statement Loop : ");
						state.display();
						M(s, state, fs, global);
						if (s instanceof Return) {
							ret = ((Return) s).target;
						}
					}
					int addr = state.findAddrByVar((Variable) e);
					Value retVal = (Value) (state.mu.get(addr));
					state = removeFrame(state, c, f, fs, global);
					
					return retVal;
				}
			}
		}
		System.out.println(e);
		throw new IllegalArgumentException("should never reach here");
	}

	public static void main(String args[]) {
		Parser parser = new Parser(new Lexer("LexerExample.txt"));
		Program prog = parser.program();
		prog.display(); // student exercise
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		TypeMap global = StaticTypeCheck.typing(prog.globals, prog.functions);
		global.display();
		ArrayList<TypeMap> map = new ArrayList<>();
		for (Function func : prog.functions) {
			map.add(StaticTypeCheck.typing(prog.globals, func));
		}
		for (TypeMap m : map) {
			m.display();
		}
		StaticTypeCheck.V(prog);
		Program out = TypeTransformer.T(prog);
		System.out.println("Output AST");
//		out.display(); // student exercise
		Semantics semantics = new Semantics();
		State state = semantics.M(out);
		System.out.println("Final State");
		state.display(); // student exercise
	}
}

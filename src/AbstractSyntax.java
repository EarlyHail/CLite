// Abstract syntax for the language C++Lite,
// exactly as it appears in Appendix B.

import java.util.*;

class Program {
	// Program = Declarations globals; Functions functions
	// Old Program = Declarations decpart ; Block body
	Declarations globals;
	Functions functions;

	Program(Declarations g, Functions f) {
		globals = g;
		functions = f;
	}

	public void display() {
		System.out.println("Program ");
		globals.display(1);
		functions.display(1);
	}
}

class Functions extends ArrayList<Function> {
	// TODO
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("Functions");
		for (int i = 0; i < size(); i++)
			get(i).display(tap + 1);

	}
}

class Function {
	// Function = Type t; String id; Declarations params, locals; Block body
	Type type;
	String id;
	Declarations params;
	Declarations locals;
	Block body;

	Function(Type t, String id, Declarations params, Declarations locals, Block body) {
		this.type = t;
		this.id = id;
		this.locals = locals;
		this.params = params;
		this.body = body;
	}

	// TODO
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println(type.toString() + " " + id);
		params.display(tap + 1);
		locals.display(tap + 1);
		body.display(tap + 1);
	}
}

class Declarations extends ArrayList<Declaration> {
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("Declarations");
		for (int i = 0; i < size(); i++)
			get(i).display(tap + 1);
	}
	// Declarations = Declaration*
	// (a list of declarations d1, d2, ..., dn)

	public void toString2() {
		for (int i = 0; i < size(); i++) {
			get(i).display2();
			System.out.print(" ");
		}
	}
}

class Declaration {
// Declaration = Variable v; Type t
	Variable v;
	Type t;

	Declaration(Variable var, Type type) {
		v = var;
		t = type;
	} // declaration */

	public void display2() {
		t.display2();
		System.out.print("=");
		v.display2();
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		v.display(0);
	}
}

class Type extends ProtoType{
	// Type = int | bool | char | float | void
	final static Type INT = new Type("int");
	final static Type BOOL = new Type("bool");
	final static Type CHAR = new Type("char");
	final static Type FLOAT = new Type("float");
	final static Type VOID = new Type("void");
	// final static Type UNDEFINED = new Type("undef");
	final static Type UNUSED = new Type("unu");

	private String id;
	public Type() {}
	public void display2() {
		System.out.print(id);
	}
	private Type(String t) {
		id = t;
	}

	public String toString() {
		return id;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.print(id);
	}
}

abstract interface Statement {
	// Statement = Skip | Block | Assignment | Conditional | Loop
	// | Put | Call | Return
	public abstract void display(int tap);
}

class Skip implements Statement {
	public void display(int tap) {
	}
}

class Block implements Statement {
	// Block = Statement*
	// (a Vector of members)
	public ArrayList<Statement> members = new ArrayList<Statement>();

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Block");
		for (int i = 0; i < members.size(); i++) {
			members.get(i).display(tap + 1);
		}
	}
}

class Assignment implements Statement {
	// Assignment = Variable target; Expression source
	Variable target;
	Expression source;

	Assignment(Variable t, Expression e) {
		target = t;
		source = e;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("Assignment\t");
		target.display(tap + 1);
		source.display(tap + 1);
	}
}

class Conditional implements Statement {
// Conditional = Expression test; Statement thenbranch, elsebranch
	Expression test;
	Statement thenbranch, elsebranch;
	// elsebranch == null means "if... then"

	Conditional(Expression t, Statement tp) {
		test = t;
		thenbranch = tp;
		elsebranch = new Skip();
	}

	Conditional(Expression t, Statement tp, Statement ep) {
		test = t;
		thenbranch = tp;
		elsebranch = ep;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Conditional");
		test.display(tap + 1);
		thenbranch.display(tap + 1);
		elsebranch.display(tap + 1);
	}
}

class Loop implements Statement {
// Loop = Expression test; Statement body
	Expression test;
	Statement body;

	Loop(Expression t, Statement b) {
		test = t;
		body = b;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Loop");
		test.display(tap + 1);
		body.display(tap + 1);
	}
}

class Put implements Statement {
	Expression term;

	Put(Expression term) {
		this.term = term;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Put");
		term.display(tap + 1);
	}
}

class Call implements Statement, Expression {
	String name;
	ArrayList<Expression> args;	

	Call(String n, ArrayList<Expression> a) {
		name = n;
		args = a;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Call\t" + name);
		if (args != null) {
			for (int i = 0; i < tap+1; i++)
				System.out.print('\t');
			System.out.println("Args");
			for (int i = 0; i < args.size(); i++) {
				args.get(i).display(tap + 2);
			}
		}
	}
}

class Return implements Statement {
	Variable target;	//function name
	Expression result;	//return value

	Return(Variable t, Expression r) {
		target = t;
		result = r;
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("Return ");
		result.display(tap + 1);
	}
}

interface Expression {
	public abstract void display(int tap);
	// Expression = Variable | Value | Binary | Unary | GetInt | GetFloat | Call
}

class GetInt implements Expression {
	IntValue intvalue;

	GetInt() {
//		Scanner sc = new Scanner(System.in);
//		intvalue = new IntValue(sc.nextInt());
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("GetInt");
//		intvalue.display(tap + 1);
	}
}

class GetFloat implements Expression {
	FloatValue floatvalue;

	GetFloat() {
		Scanner sc = new Scanner(System.in);
		floatvalue = new FloatValue(sc.nextFloat());
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print('\t');
		System.out.println("GetFloat");
		floatvalue.display(tap + 1);
	}
}

class Variable implements Expression {
	// Variable = String id
	private String id;

	Variable(String s) {
		id = s;
	}

	public void display2() {
		System.out.print(id);
	}

	public String toString() {
		return id;
	}

	public boolean equals(Object obj) {
		String s = ((Variable) obj).id;
		return id.equals(s); // case-sensitive identifiers
	}

	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println(id);
	}
}

abstract class Value implements Expression {
	// Value = IntValue | BoolValue |
	// CharValue | FloatValue
	protected Type type;
	protected boolean undef = true;

	int intValue() {
		assert false : "should never reach here";
		return 0;
	}

	boolean boolValue() {
		assert false : "should never reach here";
		return false;
	}

	char charValue() {
		assert false : "should never reach here";
		return ' ';
	}

	float floatValue() {
		assert false : "should never reach here";
		return 0.0f;
	}

	boolean isUndef() {
		return undef;
	}

	Type type() {
		return type;
	}

	static Value mkValue(Type type) {
		if (type == Type.INT)
			return new IntValue();
		if (type == Type.BOOL)
			return new BoolValue();
		if (type == Type.CHAR)
			return new CharValue();
		if (type == Type.FLOAT)
			return new FloatValue();
		if (type == Type.UNUSED)
			return new FloatValue();
		throw new IllegalArgumentException("Illegal type in mkValue");
	}
}

class IntValue extends Value {
	private int value = 0;

	IntValue() {
		type = Type.INT;
	}

	IntValue(int v) {
		this();
		value = v;
		undef = false;
	}

	int intValue() {
		assert !undef : "reference to undefined int value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("IntValue\t" + value);
	}
}

class BoolValue extends Value {
	private boolean value = false;

	BoolValue() {
		type = Type.BOOL;
	}

	BoolValue(boolean v) {
		this();
		value = v;
		undef = false;
	}

	boolean boolValue() {
		assert !undef : "reference to undefined bool value";
		return value;
	}

	int intValue() {
		assert !undef : "reference to undefined bool value";
		return value ? 1 : 0;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("BoolValue\t" + value);
	}

}

class CharValue extends Value {
	private char value = ' ';

	CharValue() {
		type = Type.CHAR;
	}

	CharValue(char v) {
		this();
		value = v;
		undef = false;
	}

	char charValue() {
		assert !undef : "reference to undefined char value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("CharValue\t" + value);
	}
}

class FloatValue extends Value {
	private float value = 0;

	FloatValue() {
		type = Type.FLOAT;
	}

	FloatValue(float v) {
		this();
		value = v;
		undef = false;
	}

	float floatValue() {
		assert !undef : "reference to undefined float value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("FloatValue\t" + value);
	}
}

class Binary implements Expression {
// Binary = Operator op; Expression term1, term2
	Operator op;
	Expression term1, term2;

	Binary(Operator o, Expression l, Expression r) {
		op = o;
		term1 = l;
		term2 = r;
	} // binary

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("Binary");
		op.display(tap + 1);
		term1.display(tap + 1);
		term2.display(tap + 1);
	}
}

class Unary implements Expression {
	// Unary = Operator op; Expression term
	Operator op;
	Expression term;

	Unary(Operator o, Expression e) {
		op = o;
		term = e;
	} // unary

	@Override
	public void display(int tap) {
		for (int i = 0; i < tap; i++)
			System.out.print("\t");
		System.out.println("Unary");
		op.display(tap + 1);
		term.display(tap + 1);
	}

}

class Operator {
	// Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
	// BooleanOp = && | ||
	final static String AND = "&&";
	final static String OR = "||";
	// RelationalOp = < | <= | == | != | >= | >
	final static String LT = "<";
	final static String LE = "<=";
	final static String EQ = "==";
	final static String NE = "!=";
	final static String GT = ">";
	final static String GE = ">=";
	// ArithmeticOp = + | - | * | /
	final static String PLUS = "+";
	final static String MINUS = "-";
	final static String TIMES = "*";
	final static String DIV = "/";
	// UnaryOp = !
	final static String NOT = "!";
	final static String NEG = "-";
	// CastOp = int | float | char
	final static String INT = "int";
	final static String FLOAT = "float";
	final static String CHAR = "char";
	// Typed Operators
	// RelationalOp = < | <= | == | != | >= | >
	final static String INT_LT = "INT<";
	final static String INT_LE = "INT<=";
	final static String INT_EQ = "INT==";
	final static String INT_NE = "INT!=";
	final static String INT_GT = "INT>";
	final static String INT_GE = "INT>=";
	// ArithmeticOp = + | - | * | /
	final static String INT_PLUS = "INT+";
	final static String INT_MINUS = "INT-";
	final static String INT_TIMES = "INT*";
	final static String INT_DIV = "INT/";
	// UnaryOp = !
	final static String INT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String FLOAT_LT = "FLOAT<";
	final static String FLOAT_LE = "FLOAT<=";
	final static String FLOAT_EQ = "FLOAT==";
	final static String FLOAT_NE = "FLOAT!=";
	final static String FLOAT_GT = "FLOAT>";
	final static String FLOAT_GE = "FLOAT>=";
	// ArithmeticOp = + | - | * | /
	final static String FLOAT_PLUS = "FLOAT+";
	final static String FLOAT_MINUS = "FLOAT-";
	final static String FLOAT_TIMES = "FLOAT*";
	final static String FLOAT_DIV = "FLOAT/";
	// UnaryOp = !
	final static String FLOAT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String CHAR_LT = "CHAR<";
	final static String CHAR_LE = "CHAR<=";
	final static String CHAR_EQ = "CHAR==";
	final static String CHAR_NE = "CHAR!=";
	final static String CHAR_GT = "CHAR>";
	final static String CHAR_GE = "CHAR>=";
	// RelationalOp = < | <= | == | != | >= | >
	final static String BOOL_LT = "BOOL<";
	final static String BOOL_LE = "BOOL<=";
	final static String BOOL_EQ = "BOOL==";
	final static String BOOL_NE = "BOOL!=";
	final static String BOOL_GT = "BOOL>";
	final static String BOOL_GE = "BOOL>=";
	// Type specific cast
	final static String I2F = "I2F";
	final static String F2I = "F2I";
	final static String C2I = "C2I";
	final static String I2C = "I2C";

	String val;

	Operator(String s) {
		val = s;
	}
	public String toString() {
		return val;
	}

	public boolean equals(Object obj) {
		return val.equals(obj);
	}

	boolean BooleanOp() {
		return val.equals(AND) || val.equals(OR);
	}

	boolean RelationalOp() {
		return val.equals(LT) || val.equals(LE) || val.equals(EQ) || val.equals(NE) || val.equals(GT) || val.equals(GE);
	}

	boolean ArithmeticOp() {
		return val.equals(PLUS) || val.equals(MINUS) || val.equals(TIMES) || val.equals(DIV);
	}

	boolean NotOp() {
		return val.equals(NOT);
	}

	boolean NegateOp() {
		return val.equals(NEG);
	}

	boolean intOp() {
		return val.equals(INT);
	}

	boolean floatOp() {
		return val.equals(FLOAT);
	}

	boolean charOp() {
		return val.equals(CHAR);
	}

	final static String intMap[][] = { { PLUS, INT_PLUS }, { MINUS, INT_MINUS }, { TIMES, INT_TIMES }, { DIV, INT_DIV },
			{ EQ, INT_EQ }, { NE, INT_NE }, { LT, INT_LT }, { LE, INT_LE }, { GT, INT_GT }, { GE, INT_GE },
			{ NEG, INT_NEG }, { FLOAT, I2F }, { CHAR, I2C } };

	final static String floatMap[][] = { { PLUS, FLOAT_PLUS }, { MINUS, FLOAT_MINUS }, { TIMES, FLOAT_TIMES },
			{ DIV, FLOAT_DIV }, { EQ, FLOAT_EQ }, { NE, FLOAT_NE }, { LT, FLOAT_LT }, { LE, FLOAT_LE },
			{ GT, FLOAT_GT }, { GE, FLOAT_GE }, { NEG, FLOAT_NEG }, { INT, F2I } };

	final static String charMap[][] = { { EQ, CHAR_EQ }, { NE, CHAR_NE }, { LT, CHAR_LT }, { LE, CHAR_LE },
			{ GT, CHAR_GT }, { GE, CHAR_GE }, { INT, C2I } };

	final static String boolMap[][] = { { EQ, BOOL_EQ }, { NE, BOOL_NE }, { LT, BOOL_LT }, { LE, BOOL_LE },
			{ GT, BOOL_GT }, { GE, BOOL_GE }, };

	final static private Operator map(String[][] tmap, String op) {
		for (int i = 0; i < tmap.length; i++)
			if (tmap[i][0].equals(op))
				return new Operator(tmap[i][1]);
		assert false : "should never reach here";
		return null;
	}

	final static public Operator intMap(String op) {
		return map(intMap, op);
	}

	final static public Operator floatMap(String op) {
		return map(floatMap, op);
	}

	final static public Operator charMap(String op) {
		return map(charMap, op);
	}

	final static public Operator boolMap(String op) {
		return map(boolMap, op);
	}

	public void display(int tap) {
		for (int i = 0; i < tap; i++) {
			System.out.print('\t');
		}
		System.out.print("Operator\t");
		System.out.print(val);
		System.out.println();
	}
}

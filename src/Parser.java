import java.util.*;

public class Parser {
	// Recursive descent parser that inputs a C++Lite program and
	// generates its abstract syntax. Each method corresponds to
	// a concrete syntax grammar rule, which appears as a comment
	// at the beginning of the method.

	Token token; // current token from the input stream
	Lexer lexer;
	Variable funcName;

	public Parser(Lexer ts) { // Open the C++Lite source program
		lexer = ts; // as a token stream, and
		token = lexer.next(); // retrieve its first Token
	}

	private String match(TokenType t) {
		String value = token.value();
		if (token.type().equals(t))
			token = lexer.next();
		else
			error(t);
		return value;
	}

	private void error(TokenType tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	private void error(String tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	public Program program() {
		// Program --> { Type Identifier FunctionOrGlobal } MainFunction
		// Old Program --> void main ( ) '{' Declarations Statements '}'
//		TokenType[] header = { TokenType.Int, TokenType.Main, TokenType.LeftParen, TokenType.RightParen };
//		for (int i = 0; i < header.length; i++) // bypass "int main ( )"
//			match(header[i]);
		Declarations globals = new Declarations();
		Functions functions = new Functions();
		getGlobals(globals, functions);

		return new Program(globals, functions);
	}

	private void getGlobals(Declarations gs, Functions fs) {
		Type t;
		Variable v;
		while (!token.type().equals(TokenType.Eof)) {
			t = type();
			if (token.type().equals(TokenType.Identifier)) {
				v = new Variable(match(TokenType.Identifier));
				if (token.type().equals(TokenType.LeftParen)) {
					// function
					funcName = v;
					function(t, v, fs);
				} else if (token.type().equals(TokenType.Comma) || token.type().equals(TokenType.Semicolon)) {
					// declarations
					Declaration dec = new Declaration(v, t);
					gs.add(dec);
					while (token.type().equals(TokenType.Comma)) {
						token = lexer.next();
						v = new Variable(match(TokenType.Identifier));
						dec = new Declaration(v, t);
						gs.add(dec);
					}
					match(TokenType.Semicolon);
				}
			} else if (token.type().equals(TokenType.Main)) {
				// main
				funcName = new Variable("main");
				token = lexer.next();
				function(t, funcName, fs);
			}
		}
	}

	private void function(Type t, Variable v, Functions fs) {
		Type type = t;
		String id = v.toString();
		Declarations params;
		Declarations locals;
		Block body;

		match(TokenType.LeftParen);
		params = parameters();
		match(TokenType.RightParen);
		match(TokenType.LeftBrace);
		locals = declarations();
		body = statements();
		Function func = new Function(type, id, params, locals, body);
		fs.add(func);
		match(TokenType.RightBrace);
//		func.display(0);
	}

	private Declarations parameters() {
		// Parameters -> [Parameter {, Parameter } ]
		Variable v;
		Type t;
		Declarations decs = new Declarations();
		if (isType()) {
			t = type();
			v = new Variable(match(TokenType.Identifier));
			decs.add(new Declaration(v, t));
			while (token.type().equals(TokenType.Comma)) {
				token = lexer.next();
				t = type();
				v = new Variable(match(TokenType.Identifier));
				decs.add(new Declaration(v, t));
			}
		}
		return decs;
	}

	private Declarations declarations() { // Declarations --> { Declaration }
		Declarations decs = new Declarations();
		while (isType()) {
			declaration(decs);
		}
		return decs; // student exercise
	}

	private void declaration(Declarations ds) { // Declaration --> Type Identifier { , Identifier } ;
		Type type;
		Variable var;
		Declaration dec;
		type = type();
		var = new Variable(match(TokenType.Identifier));
		dec = new Declaration(var, type);
		ds.add(dec);

		while (token.type().equals(TokenType.Comma)) {
			token = lexer.next();
			var = new Variable(match(TokenType.Identifier));
			dec = new Declaration(var, type);
			ds.add(dec);
		}
		match(TokenType.Semicolon);
	}

	private Type type() {
		// Type --> int | bool | float | char
		Type t = null;
		if (token.type().equals(TokenType.Int)) { // TokenÀÇ typeÀº TokenType
			t = Type.INT;
		} else if (token.type().equals(TokenType.Bool)) {
			t = Type.BOOL;
		} else if (token.type().equals(TokenType.Float)) {
			t = Type.FLOAT;
		} else if (token.type().equals(TokenType.Char)) {
			t = Type.CHAR;
		} else if (token.type().equals(TokenType.Void)) {
			t = Type.VOID;
		} else {
			error("Invalid Type : " + token);
		}
		token = lexer.next();
		// student exercise
		return t;
	}

	private Statement statement() {
		// Statement --> ; | Block | Assignment | IfStatement | WhileStatement |
		// PutStatement | CallStatement | ReturnStatement;
		Statement s = new Skip();
		if (token.type().equals(TokenType.LeftBrace)) {
			s = statements();
		} else if (token.type().equals(TokenType.Identifier)) {
			Token temp = token;
			token = lexer.next();
			if (token.type().equals(TokenType.LeftParen)) {
				s = callStatement(temp);
			} else {
				s = assignment(temp);
			}
		} else if (token.type().equals(TokenType.If)) {
			s = ifStatement();
		} else if (token.type().equals(TokenType.While)) {
			s = whileStatement();
		} else if (token.type().equals(TokenType.Put)) {
			s = putStatement();
		} else if (token.type().equals(TokenType.Return)) {
			s = returnStatement();
		}
		// student exercise
		return s;
	}

	private boolean isStatement() {
		if (token.type().equals(TokenType.Semicolon)) {
			return true;
		} else if (token.type().equals(TokenType.LeftBrace)) {
			return true;
		} else if (token.type().equals(TokenType.Identifier)) {
			return true;
		} else if (token.type().equals(TokenType.If)) {
			return true;
		} else if (token.type().equals(TokenType.While)) {
			return true;
		} else if (token.type().equals(TokenType.Put)) {
			return true;
		} else if (token.type().equals(TokenType.Return)) {
			return true;
		}
		return false;
	}

	private Block statements() { // Block --> '{' Statements '}'
		Block b = new Block();
		Statement s;
		boolean bracket = false;
		if (token.type().equals(TokenType.LeftBrace)) {
			bracket = true;
			token = lexer.next();
		}
		while (isStatement()) { // check if token is statement
			s = statement();
			b.members.add(s);
		}
		if (bracket == true)
			match(TokenType.RightBrace);
		return b;// student exercise
	}

	private Assignment assignment(Token prevToken) { // Assignment --> Identifier = (Get | Expression) ;
//		Variable target = new Variable(match(TokenType.Identifier));
		Variable target = new Variable(prevToken.value());
		match(TokenType.Assign);
		Expression source = expression();
		match(TokenType.Semicolon);
		return new Assignment(target, source);// student exercise
	}

	private Conditional ifStatement() { // IfStatement --> if ( Expression ) Statement [ else Statement ]
		match(TokenType.If);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement thenbranch = statement();
		Statement elsebranch = null;
		if (token.type().equals(TokenType.Else)) {
			token = lexer.next();
			elsebranch = statement();
			return new Conditional(test, thenbranch, elsebranch); // student exercise
		}
		return new Conditional(test, thenbranch); // student exercise
	}

	private Loop whileStatement() { // WhileStatement --> while ( Expression ) Statement
		match(TokenType.While);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement body = statement();

		return new Loop(test, body); // student exercise
	}

	private Put putStatement() { // PutStatement --> put(Expression)
		match(TokenType.Put);
		match(TokenType.LeftParen);
		Expression term = expression();
		match(TokenType.RightParen);
		match(TokenType.Semicolon);
		return new Put(term);
	}

	private Return returnStatement() { // ReturnStatement --> return Expression;
		match(TokenType.Return);
		Expression result = expression();
		match(TokenType.Semicolon);
		return new Return(funcName, result);
	}

	private Call callStatement(Token prevToken) {
		Call c = call(prevToken);
		match(TokenType.Semicolon);
		return c;
	}

	private Call call(Token prevToken) {
		String name = prevToken.value();
		ArrayList<Expression> args = null;
		match(TokenType.LeftParen);
		if (!token.type().equals(TokenType.RightParen)) {
			args = arguments();
		}
		match(TokenType.RightParen);
		return new Call(name, args);
	}

	private ArrayList<Expression> arguments() {
		ArrayList<Expression> args = new ArrayList<Expression>();
		args.add(expression());
		while (token.type().equals(TokenType.Comma)) {
			token = lexer.next();
			args.add(expression());
		}
		return args;
	}

	private Expression expression() { // Expression --> Conjunction { || Conjunction }
		Expression e = conjunction();
		while (token.type().equals(TokenType.Or)) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = conjunction();
			e = new Binary(op, e, term2);
		}
		return e; // student exercise
	}

	private Expression conjunction() { // Conjunction --> Equality { && Equality }
		Expression e = equality();
		while (token.type().equals(TokenType.And)) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = equality();
			e = new Binary(op, e, term2);
		}
		return e; // student exercise
	}

	private Expression equality() { // Equality --> Relation [ EquOp Relation ]
		Expression e = relation();
		if (isEqualityOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = relation();
			e = new Binary(op, e, term2);
		}
		return e; // student exercise
	}

	private Expression relation() { // Relation --> Addition [RelOp Addition]
		Expression e = addition();
		if (isRelationalOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = addition();
			e = new Binary(op, e, term2);
		}
		return e; // student exercise
	}

	private Expression addition() { // Addition --> Term { AddOp Term }
		Expression e = term();
		while (isAddOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = term();
			e = new Binary(op, e, term2);
		}
		return e;
	}

	private Expression term() {
		// Term --> Factor { MultiplyOp Factor }
		Expression e = factor();
		while (isMultiplyOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = factor();
			e = new Binary(op, e, term2);
		}
		return e;
	}

	private Expression factor() {
		// Factor --> [ UnaryOp ] Primary
		if (isUnaryOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term = primary();
			return new Unary(op, term);
		} else
			return primary();
	}

	private Expression primary() {
		// Primary --> Identifier | Literal | ( Expression )
		// | Type ( Expression ) | getInt() | getFloat() | Call
		Expression e = null;
		if (token.type().equals(TokenType.Identifier)) {
			Token temp = token;
			token = lexer.next();
			if (token.type().equals(TokenType.LeftParen)) {
				e = call(temp);
			} else {
				e = new Variable(temp.value());
			}
		} else if (isLiteral()) {
			e = literal();
		} else if (token.type().equals(TokenType.LeftParen)) {
			token = lexer.next();
			e = expression();
			match(TokenType.RightParen);
		} else if (isType()) {
			Operator op = new Operator(match(token.type()));
			match(TokenType.LeftParen);
			Expression term = expression();
			match(TokenType.RightParen);
			e = new Unary(op, term);
		} else if (token.type().equals(TokenType.GetInt)) {
			token = lexer.next();
			e = new GetInt();
			match(TokenType.LeftParen);
			match(TokenType.RightParen);
		} else if (token.type().equals(TokenType.GetFloat)) {
			token = lexer.next();
			e = new GetFloat();
			match(TokenType.LeftParen);
			match(TokenType.RightParen);
		} else
			error("Identifier | Literal | ( | Type | getInt() | getFloat()");
		return e;
		
		//x = a;
		//y = getInt();
	}

	private Value literal() {
		Value val = null;
		String tok_val = token.value();
		if (token.type().equals(TokenType.IntLiteral)) {
			val = new IntValue(Integer.parseInt(tok_val));
		} else if (token.type().equals(TokenType.True)) {
			val = new BoolValue(true);
		} else if (token.type().equals(TokenType.False)) {
			val = new BoolValue(false);
		} else if (token.type().equals(TokenType.CharLiteral)) {
			val = new CharValue(tok_val.charAt(0));
		} else if (token.type().equals(TokenType.FloatLiteral)) {
			val = new FloatValue(Float.parseFloat(tok_val));
		} else {
			error("Invalid Value : " + tok_val);
		}
		token = lexer.next();
		return val; // student exercise
	}

	private boolean isAddOp() {
		return token.type().equals(TokenType.Plus) || token.type().equals(TokenType.Minus);
	}

	private boolean isMultiplyOp() {
		return token.type().equals(TokenType.Multiply) || token.type().equals(TokenType.Divide);
	}

	private boolean isUnaryOp() {
		return token.type().equals(TokenType.Not) || token.type().equals(TokenType.Minus);
	}

	private boolean isEqualityOp() {
		return token.type().equals(TokenType.Equals) || token.type().equals(TokenType.NotEqual);
	}

	private boolean isRelationalOp() {
		return token.type().equals(TokenType.Less) || token.type().equals(TokenType.LessEqual)
				|| token.type().equals(TokenType.Greater) || token.type().equals(TokenType.GreaterEqual);
	}

	private boolean isType() {
		return token.type().equals(TokenType.Int) || token.type().equals(TokenType.Bool)
				|| token.type().equals(TokenType.Float) || token.type().equals(TokenType.Char)
				|| token.type().equals(TokenType.Void);
	}

	private boolean isLiteral() {
		return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
				|| token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.CharLiteral);
	}

	private boolean isBooleanLiteral() {
		return token.type().equals(TokenType.True) || token.type().equals(TokenType.False);
	}

	public static void main(String args[]) {
		Parser parser = new Parser(new Lexer("LexerExample.txt"));
		Program prog = parser.program();
		prog.display(); // display abstract syntax tree
	} // main

} // Parser


// File:   MH_Lexer.java
// Date:   October 2015

// Java template file for lexer component of Informatics 2A Assignment 1 (2015).
// Concerns lexical classes and lexer for the language MH (`Micro-Haskell').

import java.io.*;

class MH_Lexer extends GenLexer implements LEX_TOKEN_STREAM {

	static class VarAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "VAR";
		}

		@Override
		public int numberOfStates() {
			return 3;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (CharTypes.isSmall(c)) {
					return 1;
				} else
					return 2;

			case 1:
				if (CharTypes.isSmall(c) || CharTypes.isLarge(c) || CharTypes.isDigit(c) || c == '\'') {
					return 1;
				} else
					return 2;

			default:
				return 2;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 1);
		}

		@Override
		boolean dead(int state) {
			return (state == 2);
		}
	}

	static class NumAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "NUM";
		}

		@Override
		public int numberOfStates() {
			return 3;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (CharTypes.isDigit(c)) {
					return 1;
				} else
					return 2;

			case 1:
				if (CharTypes.isDigit(c)) {
					return 1;
				} else
					return 2;

			default:
				return 2;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 1);
		}

		@Override
		boolean dead(int state) {
			return (state == 2);
		}
	}

	static class BooleanAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "BOOLEAN";
		}

		@Override
		public int numberOfStates() {
			return 9;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (c == 'T') {
					return 1;
				} else if (c == 'F') {
					return 5;
				} else
					return 8;

			case 1:
				if (c == 'r') {
					return 2;
				} else
					return 8;

			case 2:
				if (c == 'u') {
					return 3;
				} else
					return 8;

			case 3:
				if (c == 'e') {
					return 4;
				} else
					return 8;

			case 5:
				if (c == 'a') {
					return 6;
				} else
					return 8;

			case 6:
				if (c == 'l') {
					return 7;
				} else
					return 8;

			case 7:
				if (c == 's') {
					return 3;
				} else
					return 8;

			case 4:
				return 8;
			default:
				return 8;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 4);
		}

		@Override
		boolean dead(int state) {
			return (state == 8);
		}
	}

	static class SymAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "SYM";
		}

		@Override
		public int numberOfStates() {
			return 3;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (CharTypes.isSymbolic(c)) {
					return 1;
				} else
					return 2;

			case 1:
				if (CharTypes.isSymbolic(c)) {
					return 1;
				} else
					return 2;

			default:
				return 2;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 1);
		}

		@Override
		boolean dead(int state) {
			return (state == 2);
		}
	}

	static class WhitespaceAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "";
		}

		@Override
		public int numberOfStates() {
			return 3;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (CharTypes.isWhitespace(c)) {
					return 1;
				} else
					return 2;

			case 1:
				if (CharTypes.isWhitespace(c)) {
					return 1;
				} else
					return 2;

			default:
				return 2;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 1);
		}

		@Override
		boolean dead(int state) {
			return (state == 2);
		}
	}

	static class CommentAcceptor extends GenAcceptor implements DFA {

		@Override
		public String lexClass() {
			return "";
		}

		@Override
		public int numberOfStates() {
			return 5;
		}

		@Override
		int nextState(int state, char c) {
			switch (state) {
			case 0:
				if (c == '-') {
					return 1;
				} else
					return 4;

			case 1:
				if (c == '-') {
					return 2;
				} else
					return 4;

			case 2:
				if (c == '-') {
					return 2;
				} else if (CharTypes.isSymbolic(c) || CharTypes.isNewline(c)) {
					return 4;
				} else
					return 3;

			case 3:
				if (CharTypes.isNewline(c)) {
					return 4;
				} else
					return 3;

			default:
				return 4;
			}
		}

		@Override
		boolean accepting(int state) {
			return (state == 3);
		}

		@Override
		boolean dead(int state) {
			return (state == 4);
		}
	}

	static class TokAcceptor extends GenAcceptor implements DFA {

		String tok;
		int tokLen;

		TokAcceptor(String tok) {
			this.tok = tok;
			tokLen = tok.length();
		}

		public String lexClass() {
			return tok;
		}

		public int numberOfStates() {
			return tokLen + 2;
		}

		int nextState(int state, char c) {
			if (state < tokLen) {
				if (tok.charAt(state) == c) {
					return state + 1;
				} else
					return tokLen + 1;
			} else
				return tokLen + 1;
		}

		boolean accepting(int state) {
			// return tokLen == state;
			if (tokLen == state) {
				return true;
			} else
				return false;
		}

		boolean dead(int state) {
			// return tokLen+1 == state;
			if (tokLen + 1 == state) {
				return true;
			} else
				return false;
		}
	}

	static class IntegerAcceptor {
		private IntegerAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("Integer");
		}
	}

	static class BoolAcceptor {
		private BoolAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("Bool");
		}
	}

	static class ifAcceptor {
		private ifAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("if");
		}
	}

	static class thenAcceptor {
		private thenAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("then");
		}
	}

	static class elseAcceptor {
		private elseAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("else");
		}
	}

	static class LeftBracketAcceptor {
		private LeftBracketAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor("(");
		}
	}

	static class RightBracketAcceptor {
		private RightBracketAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor(")");
		}
	}

	static class SemiColonAcceptor {
		private SemiColonAcceptor() {
		}

		public static DFA acc() {
			return new TokAcceptor(";");
		}
	}

	static DFA commentAccepted = new CommentAcceptor();
	static DFA ifAccepted = ifAcceptor.acc();
	static DFA thenAccepted = thenAcceptor.acc();
	static DFA elseAccepted = elseAcceptor.acc();
	static DFA integerDecAccepted = IntegerAcceptor.acc();
	static DFA booleanDecAccepted = BoolAcceptor.acc();
	static DFA leftBracketAccepted = LeftBracketAcceptor.acc();
	static DFA rightBracketAccepted = RightBracketAcceptor.acc();
	static DFA semiColonAccepted = SemiColonAcceptor.acc();
	static DFA whiteSpaceAccepted = new WhitespaceAcceptor();
	static DFA boolAccepted = new BooleanAcceptor();
	static DFA symAccepted = new SymAcceptor();
	static DFA varAccepted = new VarAcceptor();
	static DFA numAccepted = new NumAcceptor();

	static DFA[] MH_acceptors = new DFA[] { commentAccepted, ifAccepted, thenAccepted, elseAccepted, integerDecAccepted,
			booleanDecAccepted, leftBracketAccepted, rightBracketAccepted, semiColonAccepted, whiteSpaceAccepted,
			boolAccepted, symAccepted, varAccepted, numAccepted };

	MH_Lexer(Reader reader) {
		super(reader, MH_acceptors);
	}
}

class Demo {

	public static void main(String[] args) throws LexError, StateOutOfRange, IOException {
		System.out.print("Lexer> ");
		Reader reader = new BufferedReader(new InputStreamReader(System.in));
		GenLexer demoLexer = new MH_Lexer(reader);
		LexToken currTok = demoLexer.pullProperToken();
		while (currTok != null) {
			System.out.println(currTok.value() + " \t" + currTok.lexClass());
			currTok = demoLexer.pullProperToken();
		}
		;
	}
}

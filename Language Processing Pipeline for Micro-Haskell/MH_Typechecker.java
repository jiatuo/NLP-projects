
// File:   MH_Typechecker.java
// Date:   October 2015

// Java template file for typechecker component of Informatics 2A Assignment 1.
// Provides infrastructure for Micro-Haskell typechecking:
// the core typechecking operation is to be implemented by students.

import java.util.*;
import java.io.*;

class MH_Typechecker {

	static MH_Parser MH_Parser = MH_Type_Impl.MH_Parser;

	// The core of the typechecker:
	// Computing the MH_TYPE of a given MH_EXP in a given TYPE_ENV.
	// Should raise TypeError if MH_EXP isn't well-typed

	static MH_TYPE IntegerType = MH_Type_Impl.IntegerType;
	static MH_TYPE BoolType = MH_Type_Impl.BoolType;

	static MH_TYPE computeType(MH_EXP exp, TYPE_ENV env) throws TypeError, UnknownVariable {

		if (exp.isNUM()) {
			return MH_Type_Impl.IntegerType;
		}

		if (exp.isBOOLEAN()) {
			return MH_Type_Impl.BoolType;
		}

		if (exp.isINFIX()) {
			if (exp.infixOp().equals("==") || exp.infixOp().equals("<=")) {
				return BoolType;
			} else if (exp.infixOp().equals("+") || exp.infixOp().equals("-")) {
				return IntegerType;
			}
		}

		if (exp.isAPP()) {
			if ((computeType(exp.first(), env)).left().equals(computeType(exp.second(), env))) {
				return computeType(exp.first(), env).right();
			}
		}

		if (exp.isIF()) {
			if (computeType(exp.first(), env).equals(BoolType)) {
				if (computeType(exp.second(), env).equals(computeType(exp.third(), env))) {
					return computeType(exp.third(), env);
				} else
					throw new TypeError("Second and third value in the IF statement are not of the same type!");
			} else
				throw new TypeError("The first parameter in the IF statement is not of the type boolean!");
		}

		if (exp.isVAR()) {
			if (!(env.typeOf(exp.value()) == null)) {
				return env.typeOf(exp.value());

			} else
				throw new UnknownVariable(exp.value());
		} else
			throw new TypeError("Invalid expression!");
	}

	// Type environments:

	interface TYPE_ENV {
		MH_TYPE typeOf(String var) throws UnknownVariable;
	}

	static class MH_Type_Env implements TYPE_ENV {

		TreeMap env;

		public MH_TYPE typeOf(String var) throws UnknownVariable {
			MH_TYPE t = (MH_TYPE) (env.get(var));
			if (t == null)
				throw new UnknownVariable(var);
			else
				return t;
		}

		// Constructor for cloning a type env
		MH_Type_Env(MH_Type_Env given) {
			this.env = (TreeMap) given.env.clone();
		}

		// Constructor for building a type env from the type decls
		// appearing in a program
		MH_Type_Env(TREE prog) throws DuplicatedVariable {
			this.env = new TreeMap();
			TREE prog1 = prog;
			while (prog1.getRhs() != MH_Parser.epsilon) {
				TREE typeDecl = prog1.getChildren()[0].getChildren()[0];
				String var = typeDecl.getChildren()[0].getValue();
				MH_TYPE theType = MH_Type_Impl.convertType(typeDecl.getChildren()[2]);
				if (env.containsKey(var))
					throw new DuplicatedVariable(var);
				else
					env.put(var, theType);
				prog1 = prog1.getChildren()[1];
			}
			System.out.println("Type conversions successful.");
		}

		// Augmenting a type env with a list of function arguments.
		// Takes the type of the function, returns the result type.
		MH_TYPE addArgBindings(TREE args, MH_TYPE theType) throws DuplicatedVariable, TypeError {
			TREE args1 = args;
			MH_TYPE theType1 = theType;
			while (args1.getRhs() != MH_Parser.epsilon) {
				if (theType1.isArrow()) {
					String var = args1.getChildren()[0].getValue();
					if (env.containsKey(var)) {
						throw new DuplicatedVariable(var);
					} else {
						this.env.put(var, theType1.left());
						theType1 = theType1.right();
						args1 = args1.getChildren()[1];
					}
				} else
					throw new TypeError("Too many function arguments");
			}
			;
			return theType1;
		}
	}

	static MH_Type_Env compileTypeEnv(TREE prog) throws DuplicatedVariable {
		return new MH_Type_Env(prog);
	}

	// Building a closure (using lambda) from argument list and body
	static MH_EXP buildClosure(TREE args, MH_EXP exp) {
		if (args.getRhs() == MH_Parser.epsilon)
			return exp;
		else {
			MH_EXP exp1 = buildClosure(args.getChildren()[1], exp);
			String var = args.getChildren()[0].getValue();
			return new MH_Exp_Impl(var, exp1);
		}
	}

	// Name-closure pairs (result of processing a TermDecl).
	static class Named_MH_EXP {
		String name;
		MH_EXP exp;

		Named_MH_EXP(String name, MH_EXP exp) {
			this.name = name;
			this.exp = exp;
		}
	}

	static Named_MH_EXP typecheckDecl(TREE decl, MH_Type_Env env)
			throws TypeError, UnknownVariable, DuplicatedVariable, NameMismatchError {
		// typechecks the given decl against the env,
		// and returns a name-closure pair for the entity declared.
		String theVar = decl.getChildren()[0].getChildren()[0].getValue();
		String theVar1 = decl.getChildren()[1].getChildren()[0].getValue();
		if (!theVar.equals(theVar1))
			throw new NameMismatchError(theVar, theVar1);
		MH_TYPE theType = MH_Type_Impl.convertType(decl.getChildren()[0].getChildren()[2]);
		MH_EXP theExp = MH_Exp_Impl.convertExp(decl.getChildren()[1].getChildren()[3]);
		TREE theArgs = decl.getChildren()[1].getChildren()[1];
		MH_Type_Env theEnv = new MH_Type_Env(env);
		MH_TYPE resultType = theEnv.addArgBindings(theArgs, theType);
		MH_TYPE expType = computeType(theExp, theEnv);
		if (expType.equals(resultType)) {
			return new Named_MH_EXP(theVar, buildClosure(theArgs, theExp));
		} else
			throw new TypeError("RHS of declaration of " + theVar + " has wrong type");
	}

	static MH_Exp_Env typecheckProg(TREE prog, MH_Type_Env env)
			throws TypeError, UnknownVariable, DuplicatedVariable, NameMismatchError {
		TREE prog1 = prog;
		TreeMap treeMap = new TreeMap();
		while (prog1.getRhs() != MH_Parser.epsilon) {
			TREE theDecl = prog1.getChildren()[0];
			Named_MH_EXP binding = typecheckDecl(theDecl, env);
			treeMap.put(binding.name, binding.exp);
			prog1 = prog1.getChildren()[1];
		}
		System.out.println("Typecheck successful.");
		return new MH_Exp_Env(treeMap);
	}

	// For testing:

	public static void main(String[] args) throws Exception {
		Reader reader = new BufferedReader(new FileReader(args[0]));
		// try {
		LEX_TOKEN_STREAM MH_Lexer = new CheckedSymbolLexer(new MH_Lexer(reader));
		TREE prog = MH_Parser.parseTokenStream(MH_Lexer);
		MH_Type_Env typeEnv = compileTypeEnv(prog);
		MH_Exp_Env runEnv = typecheckProg(prog, typeEnv);
		// } catch (Exception x) {
		// System.out.println ("MH Error: " + x.getMessage()) ;
		// }
	}
}

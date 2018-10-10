package tsl.jlisp;

import java.util.Vector;

public class LispFunction extends Function {
	Symbol fsym = null;
	Vector<Symbol> params = null;
	Sexp body = null;
	boolean doEval = true;

	public static int SEXPPARAM = 1;
	public static int OBJECTPARAM = 2;
	public static Class[] sexpParamTypes = new Class[] { Sexp.class };
	public static Class[] objectParamTypes = new Class[] { Object.class };

	public LispFunction(Symbol sym, Vector params, Sexp body, boolean doeval) {
		super(null);
		this.fsym = sym;
		this.params = params;
		this.body = body;
		this.doEval = doeval;
	}

	public static boolean isLFunction(Object o) {
		return o instanceof LispFunction;
	}

	public static JLispObject applyLFunctionSymbol(Sexp exp) {
		JLispObject rv = null;
		Symbol s = (Symbol) exp.getFirst();
		LispFunction lf = (LispFunction) s.getValue();
		Sexp args = (Sexp) exp.getCdr();
		if (s.isDoEval()) {
			args = (Sexp) JLisp.evList(args);
		}
		rv = applyLFunctionObject(lf, args);
		return rv;
	}

	public static JLispObject applyLFunctionObject(LispFunction lf, Sexp args) {
		JLispObject rv = null;
		SymbolTable st = new SymbolTable();
		st.pushParameters(lf.params, args, 0);
		rv = JLisp.eval(lf.body);
		SymbolTable.popParameters();
		return rv;
	}

	public String toString() {
		if (this.fsym != null) {
			return this.fsym.getName();
		} else {
			return "<AnonymousLFunction>";
		}
	}

}

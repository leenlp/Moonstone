package tsl.jlisp;

public class Function extends JLispObject {
	public Symbol sym = null;

	public static int SEXPPARAM = 1;
	public static int OBJECTPARAM = 2;
	public static Class[] SexpParamTypes = new Class[] { Sexp.class };
	public static Class[] ObjectParamTypes = new Class[] { Object.class };
	public static Boolean TRUTH = new Boolean(true);
	public static Boolean FALSITY = new Boolean(false);

	public Function() {
	}

	public Function(Symbol sym, boolean doeval) {
		if (sym != null) {
			this.sym = sym;
			sym.setType(Symbol.FUNCTIONTYPE);
			sym.setValue(this);
			sym.setDoEval(doeval);
		}
	}

	public Function(Symbol sym) {
		sym.setValue(this);
	}

	public String toString() {
		return "[Function:  " + this.sym.getName() + "]";
	}

	public static boolean isTrue(Boolean b) {
		return b.booleanValue();
	}

}

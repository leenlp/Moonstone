package tsl.tsllisp;

public class TLMethod extends Function {
	private Symbol param = null;
	private TLClass mclass = null;
	private TLObject body = null;

	public static Class[] sexpParamTypes = new Class[] { Sexp.class };

	public TLMethod(Symbol sym, Symbol param, TLClass mclass, TLObject body) {
		super(null);
		this.sym = sym;
		this.param = param;
		this.mclass = mclass;
		this.body = body;
		sym.setValue(this);
	}

	public static boolean isMethod(Object o) {
		return o instanceof TLMethod;
	}

	public static TLObject applyMethodSymbol(Sexp exp, boolean isEval)
			throws Exception {
		Symbol s = (Symbol) exp.getFirst();
		if (!(s.getValue() instanceof TLMethod)) {
			throw new Exception("Symbol " + s + "not bound to Method");
		}
		return applyMethodObject((TLMethod) s.getValue(), (Sexp) exp.getCdr());
	}

	public static TLObject applyMethodObject(TLMethod method, Sexp args)
			throws Exception {
		TLObject rv = null;
		TLisp tl = TLisp.tLisp;
		tl.pushSymbolTable();
		SymbolTable st = tl.getCurrentSymbolTable();
		TLObject ito = TLisp.eval(args.getFirst());
		if (!(ito instanceof TLClassInstance)) {
			throw new Exception(method
					+ ": Argument not an instance of TLClassInstance");
		}
		TLClassInstance cinst = (TLClassInstance) ito;
		st.pushParameter(method.param, cinst);
		rv = TLisp.eval(method.body);
		tl.popSymbolTable();
		return rv;
	}

	public static TLObject expandMethodObject(TLMethod method, Sexp args)
			throws Exception {
		TLObject ito = TLisp.eval(args.getFirst());
		if (!(ito instanceof TLClassInstance)) {
			throw new Exception(method
					+ ": Argument not an instance of TLClassInstance");
		}
		return Sexp.doCons(method.sym, TLisp.expand(args));
	}

	public Symbol getParam() {
		return param;
	}

	public TLClass getMclass() {
		return mclass;
	}

	public TLObject getBody() {
		return body;
	}
	
	public String toString() {
		return "<Method: " + this.sym + ">";
	}

}

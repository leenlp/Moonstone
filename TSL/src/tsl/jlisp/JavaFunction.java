package tsl.jlisp;

import java.lang.reflect.Method;

public class JavaFunction extends Function {
	private int paramType = -1;
	private Class[] paramTypes = null;
	private Method method = null;

	public JavaFunction(Symbol sym, String methodname, int ptype, boolean doeval) {
		super(sym, doeval);
		try {
			this.paramType = ptype;
			this.paramTypes = (ptype == SEXPPARAM ? SexpParamTypes
					: ObjectParamTypes);
			this.method = LispJFunctions.class.getMethod(methodname,
					this.paramTypes);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static boolean isJFunction(JLispObject o) {
		return o instanceof JavaFunction;
	}

	public static JLispObject applyJFunctionSymbol(Sexp exp) {
		JLispObject rv = null;
		try {
			Symbol s = (Symbol) exp.getFirst();
			JavaFunction jf = (JavaFunction) s.getValue();
			Sexp args = (Sexp) exp.getCdr();
			if (s.isDoEval()) {
				args = (Sexp) JLisp.evList(args);
			}
			Object farg = (jf.paramType == SEXPPARAM ? (Object) args
					: (Object) JLUtils.convertSexpToLVector(args));
			rv = (JLispObject) jf.method.invoke(LispJFunctions.staticObject,
					new Object[] { farg });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

}

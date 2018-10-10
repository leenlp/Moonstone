package tsl.jlisp;

public class JLispObject {

	public JLispObject() {
	}

	public boolean isSexp() {
		return this instanceof Sexp;
	}

	public boolean isJavaObject() {
		return this instanceof JavaObject;
	}

	public boolean isSymbol() {
		return this instanceof Symbol;
	}

	public boolean isString() {
		return this instanceof JavaObject && ((JavaObject) this).isString();
	}

	public boolean isFloat() {
		return this instanceof JavaObject && ((JavaObject) this).isFloat();
	}

}

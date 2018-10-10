package tsl.jlisp;

public class JLispException extends Exception {
	public static final long serialVersionUID = 0;
	public String message = "Poorly formed lisp expression";
	
	public JLispException(String message, Token token) {
		this.message = message + ", Token=" + token;
	}
	
	public JLispException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}

}

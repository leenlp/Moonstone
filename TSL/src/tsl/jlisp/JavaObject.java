package tsl.jlisp;

public class JavaObject extends JLispObject {
	private Object object = null;

	public JavaObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return this.object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public boolean isString() {
		return this.object instanceof String;
	}

	public boolean isFloat() {
		return this.object instanceof Float;
	}

	public String toString() {
		String str = "<JavaObject: Object=" + this.object.toString() + ">";
		return str;
	}

}

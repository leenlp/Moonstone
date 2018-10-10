package tsl.tsllisp;

import java.util.Vector;

public class JavaObject extends TLObject {
	private Object object = null;

	public JavaObject() {
	}
	
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
	
	public boolean isVector() {
		return this.object instanceof Vector;
	}
	
	public boolean equals(Object o) {
		if (o instanceof JavaObject) {
			JavaObject jo = (JavaObject) o;
			return this.getObject().equals(jo.getObject());
		}
		return false;
	}
	
	public String toString() {
		return this.object.toString();
	}

}

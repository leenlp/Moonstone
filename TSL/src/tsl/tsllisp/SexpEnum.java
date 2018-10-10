package tsl.tsllisp;

import java.util.Enumeration;

public class SexpEnum implements Enumeration {
	Object object = null;
	
	SexpEnum(Object o) {
		this.object = o;
	}
	
	public boolean hasMoreElements() {
		return (TLUtils.isCons(this.object));
	}
	
	public Object nextElement() {
		Sexp s = (Sexp) this.object;
		Object o = s.getCar();
		this.object = s.getCdr();
		return o;
	}

}


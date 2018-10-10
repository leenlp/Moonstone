package workbench.api.annotation;

import java.util.Vector;

import tsl.utilities.VUtils;

public class Annotator {

	private Vector<String> names = null;
	private boolean isPrimary = false;

	public Annotator(String nstr, boolean isPrimary) {
		String[] nlst = nstr.split(",");
		this.names = VUtils.arrayToVector(nlst);
		this.isPrimary = isPrimary;
	}
	
	// Before 11/10/2015
//	public Annotator(String name, boolean isPrimary) {
//		this.name = name;
//		this.isPrimary = isPrimary;
//	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	public Vector<String> getNames() {
		return this.names;
	}
	
	public boolean containsName(String name) {
		return this.names.contains(name);
	}
	
	public String getName() {
		return this.names.firstElement();
	}

	public String toString() {
		String str = "<" + this.names + "="
				+ (this.isPrimary ? "primary" : "secondary") + ">";
		return str;
	}


}

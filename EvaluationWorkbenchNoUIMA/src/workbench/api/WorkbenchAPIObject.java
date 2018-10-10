package workbench.api;

import java.util.Vector;

public abstract class WorkbenchAPIObject {
	
	private String name = null;
	
	public WorkbenchAPIObject(String name) {
		this.name = name;
	}
	
	public Vector getAlternativeValues() {
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

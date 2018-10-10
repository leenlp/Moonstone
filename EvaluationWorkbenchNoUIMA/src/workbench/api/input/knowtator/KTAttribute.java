package workbench.api.input.knowtator;

import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;


public class KTAttribute {

	String attribute = null;
	String value = null;

	public KTAttribute(String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
	}
	
	public void addToTypeSystem(Type parent) throws Exception {
		new Attribute(parent, this.getAttribute());
	}

	// Before 9/18/2014
//	public void addToTypeSystem(TypeObject parent) throws Exception {
//		Attribute attribute = new Attribute(parent.getTypeSystem(),
//				this.getAttribute(), this.getAttribute(), null);
//		attribute.setParentTypeObject(parent);
//		parent.addAttribute(attribute);
//		TypeSystem.addTypeObjectByID(attribute);
//		TypeSystem.addTypeObjectByUima(attribute);
//	}

	public String toString() {
		return "<Attribute:" + this.attribute + "=" + this.value + ">";
	}

	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}
	
	public String toXML() {
		return null;
	}

}

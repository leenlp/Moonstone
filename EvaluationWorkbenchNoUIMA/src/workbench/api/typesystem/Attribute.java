package workbench.api.typesystem;

import java.util.Vector;

import tsl.utilities.VUtils;
import workbench.api.WorkbenchAPIObject;

public class Attribute extends WorkbenchAPIObject {
	protected TypeSystem typesystem = null;
	protected String fullName = null;
	protected Type parent = null;
	protected Vector values = null;
	protected int attributeID = -1;
	
	public Attribute(Type parent, String aname) {
		this(-1, parent, aname);
	}
	
	public Attribute(int aid, Type parent, String aname) {
		super(aname);
		this.attributeID = aid;
		this.typesystem = parent.getTypeSystem();
		this.parent = parent;
		this.fullName = parent.getFullname() + ":" + aname;
		parent.getTypeSystem().putObjectHash(this.fullName, this);
		parent.addAttribute(this);
	}
	
	public static Attribute createAttribute(Type parent, String aname) {
		return createAttribute(-1, parent, aname);
	}
	
	public static Attribute createAttribute(int aid, Type parent, String aname) {
		Attribute attr = parent.getAttribute(aname);
		if (attr == null) {
			attr = new Attribute(aid, parent, aname);
		}
		return attr;
	}
	
	public void addValues(Vector values) {
		this.values = VUtils.appendIfNot(this.values, values);
	}
	
	public void addValue(Object value) {
		this.values = VUtils.addIfNot(this.values, value);
	}

	public Vector<String> getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}

	public String getFullName() {
		return this.fullName;
	}
	
	public Type getParent() {
		return parent;
	}
	
	public void setParent(Type parent) {
		this.parent = parent;
		
		// 10/12/2015
		this.fullName = parent.getFullname() + ":" + this.getName();
	}

	public boolean isClassification() {
		return this instanceof Classification;
	}
	
	public String toString() {
		String str = "<Attribute: Name=" + this.fullName + ">";
		return str;
	}
	
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("  <attribute name=\"" + this.getName() + "\">\n");
		if (this.getValues() != null) {
			for (Object value : this.getValues()) {
				sb.append("    <value> " + value + "</value>\n");
			}
		}
		sb.append("  </attribute>\n");
		return sb.toString();
	}
	
	public Vector getAlternativeValues() {
		if (this.values != null) {
			return new Vector(this.values);
		}
		return null;
	}

	public TypeSystem getTypesystem() {
		return typesystem;
	}

	public int getAttributeID() {
		return attributeID;
	}

	public void setAttributeID(int attributeID) {
		this.attributeID = attributeID;
	}


}

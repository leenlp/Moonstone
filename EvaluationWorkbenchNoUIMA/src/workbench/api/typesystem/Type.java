package workbench.api.typesystem;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.VUtils;
import workbench.api.WorkbenchAPIObject;
import workbench.api.input.knowtator.KTClass;

public class Type extends WorkbenchAPIObject {
	private String fullName;
	private Type parent = null;
	private Vector<Type> children = null;
	private TypeSystem typeSystem = null;
	private Hashtable<Object, Attribute> attributeHash = new Hashtable();
	private Vector<Attribute> attributes = null;
	private Vector<Attribute> classifications = null;
	private KTClass ktClass = null;
	private Color color = null;
	private int typeID = -1;

	public Type(TypeSystem ts, Type parent, String name, Color color) {
		this(-1, ts, parent, name, color);
	}
	
	public Type(int typeid, TypeSystem ts, Type parent, String name, Color color) {
		super(name);
		this.typeID = typeid;
		this.typeSystem = ts;
		this.setParent(parent);
		this.color = color;
		if (ts != null) {
			this.fullName = this.getFullname();
			ts.addType(this);
		}
	}
	
	public Vector<String> getAlternativeValues() {
		return this.getAllNestedClassificationValues();
	}

	public Vector<String> getAlternativeValues_BEFORE_8_25_2017() {
		Attribute classification = this.getFirstClassification();
		if (classification != null && classification.getValues() != null) {
			return new Vector(classification.getValues());
		}
		return null;
	}

	public Attribute getAttribute(String aname) {
		return this.attributeHash.get(aname);
	}
	
	public Attribute getAttribute(int id) {
		return this.attributeHash.get(id);
	}
	
	public void addAttribute(String aname) {
		addAttribute(-1, aname);
	}

	public void addAttribute(int aid, String aname) {
		if (this.attributeHash.get(aname) == null) {
			Attribute attribute = new Attribute(aid, this, aname);
			addAttribute(attribute);
		}
	}

	public void addAttribute(Attribute attribute) {

		// 10/12/2015
		this.removeAttribute(attribute);

		attribute.setParent(this);
		this.attributeHash.put(attribute.getFullName(), attribute);
		this.attributeHash.put(attribute.getName(), attribute);
		if (attribute.getAttributeID() >= 0) {
			this.attributeHash.put(attribute.getAttributeID(), attribute);
		}
		this.attributes = VUtils.addIfNot(this.attributes, attribute);
		if (attribute.isClassification()) {
			this.classifications = VUtils.addIfNot(this.classifications, attribute);
		}
	}

	// 10/12/2015
	public void removeAttribute(Attribute attribute) {
		attribute.getParent().getTypeSystem().removeObjectHash(attribute.getFullName());
		this.attributeHash.remove(attribute.getFullName());
		this.attributeHash.remove(attribute.getName());
		if (attribute.getAttributeID() >= 0) {
			this.attributeHash.remove(attribute.getAttributeID());
		}
		if (this.attributes != null) {
			this.attributes.remove(attribute);
		}
		if (this.classifications != null) {
			this.classifications.remove(attribute);
		}
	}

	public String getFullname() {
		// 8/19/2017 TEST
		this.fullName = null;
		
		if (this.fullName == null) {
			String fname = "";
			Vector<Type> path = this.getPathToRoot();
			for (int i = 0; i < path.size(); i++) {
				Type type = path.elementAt(i);
				if (type.getName() != null) {
					fname += type.getName();
					if (i < path.size() - 1) {
						fname += ":";
					}
				}
			}
			this.fullName = fname;
		}
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Vector<Type> getPathToRoot() {
		if (this.isRoot()) {
			return VUtils.listify(this);
		}
		Vector<Type> path = this.parent.getPathToRoot();
		return VUtils.add(path, this);
	}

	public boolean isRoot() {
		return this.parent == null;
	}

	public Type getParent() {
		return parent;
	}

	public void setParent(Type parent) {
		if (parent != null) {
			this.parent = parent;
			parent.children = VUtils.addIfNot(parent.children, this);
		}
	}

	public Vector<Type> getChildren() {
		return children;
	}

	public Vector<Attribute> getAttributes() {
		return attributes;
	}

	public Vector<String> getAttributeStrings() {
		Vector<String> strs = null;
		if (this.attributes != null) {
			for (Attribute attr : this.attributes) {
				strs = VUtils.add(strs, attr.getName());
			}
		}
		return strs;
	}

	public int getNumberOfAttributes() {
		return this.attributeHash.keySet().size();
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<workbench_type name=\"" + this.getName() + "\">\n");
		if (this.parent != null) {
			sb.append("  <parent> " + this.parent.getName() + " </parent>\n");
		}
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				sb.append(attribute.toXML());
			}
		}
		sb.append("</workbench_type>\n");
		return sb.toString();
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public String toString() {
		String str = "<Type:Fullname=" + this.fullName + ">";
		return str;
	}

	public Attribute getFirstClassification() {
		if (this.classifications != null) {
			return this.classifications.firstElement();
		}
		return null;
	}
	
	// 8/25/2017
	public Vector<String> getAllNestedClassificationValues() {
		Vector<String> nested = null;
		if (this.classifications != null) {
			nested = new Vector(0);
			for (Attribute a : this.classifications) {
				nested = VUtils.append(nested, a.getValues());
			}
		}
		if (this.children != null) {
			for (Type child : this.children) {
				nested = VUtils.appendNew(nested, child.getAllNestedClassificationValues());
			}
		}
		return nested;
	}


	public KTClass getKtClass() {
		return ktClass;
	}

	public void setKtClass(KTClass ktClass) {
		this.ktClass = ktClass;
	}

	// 9/27/2014
	public boolean isDocumentType() {
		return this.getName().toLowerCase().contains("document");
	}

	public Color getColor() {
		return color;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	
	
	
	

}

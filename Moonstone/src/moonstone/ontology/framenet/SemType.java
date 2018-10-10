package moonstone.ontology.framenet;

import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import tsl.expression.term.type.TypeConstant;
import tsl.utilities.JDomUtils;

public class SemType {

	FramenetOntology ontologyCreation = null;
	SemType parent = null;
	Vector<SemType> children = null;
	Vector<Frame> frames = null;
	Vector<LexicalUnit> lexicalUnits = null;
	String pid = null;
	String id = null;
	String name = null;
	TypeConstant typeConstant = null;

	public SemType(FramenetOntology oc, Element ele) {
		this.ontologyCreation = oc;
		this.name = ele.getAttributeValue("name");
		this.id = ele.getAttributeValue("ID");
		Element pnode = JDomUtils.getElementByName(ele, "superType");
		if (pnode != null) {
			this.pid = pnode.getAttributeValue("supID");
		}
		this.ontologyCreation.semTypeIDHash.put(this.id, this);
	}
	
	public TypeConstant createTypeConstant() {
		return TypeConstant.createTypeConstant(this.name);
	}

	public FramenetOntology getOntologyCreation() {
		return ontologyCreation;
	}

	public void setOntologyCreation(FramenetOntology ontologyCreation) {
		this.ontologyCreation = ontologyCreation;
	}

	public SemType getParent() {
		return parent;
	}

	public void setParent(SemType parent) {
		this.parent = parent;
	}

	public Vector<SemType> getChildren() {
		return children;
	}

	public void setChildren(Vector<SemType> children) {
		this.children = children;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		String str = "<SemType: Name=" + this.name + ", ID=" + this.id + ", ParentID=" + this.pid + ">";
		return str;
	}

}

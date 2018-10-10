package moonstone.ontology.framenet;

import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import moonstone.rule.Rule;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.tsllisp.Sexp;
import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class LexicalUnit {
	String id = null;
	String frameID = null;
	String partOfSpeech = null;
	String name = null;
	Frame frame = null;
	private Vector<SemType> semTypes = null;
	Hashtable<String, String> frameRoleHash = new Hashtable();

	public LexicalUnit(FramenetOntology oc, String fpath) {
		try {
			org.jdom.Document jdoc = new SAXBuilder().build(fpath);
			Element root = jdoc.getRootElement();
			Element luNode = JDomUtils.getElementByName(root, "lexUnit");
			if (luNode != null) {
				this.id = luNode.getAttributeValue("ID");
				this.partOfSpeech = luNode.getAttributeValue("POS");
				this.frameID = luNode.getAttributeValue("frameID");
				this.name = luNode.getAttributeValue("name");
				int index = this.name.lastIndexOf('.');
				if (index > 0) {
					this.name = this.name.substring(0, index);
				}
				Vector<Element> fes = JDomUtils.getElementsByName(luNode, "FE");
				if (fes != null) {
					for (Element fe : fes) {
						String fename = fe.getAttributeValue("name");
						String fetype = fe.getAttributeValue("type");
						if (fename != null && fetype != null) {
							this.frameRoleHash.put(fetype, fename);
						}
					}
				}
				Vector<Element> stnodes = JDomUtils.getElementsByName(luNode, "semType");
				if (stnodes != null) {
					for (Element stnode : stnodes) {
						String stid = stnode.getAttributeValue("ID");
						if (stid != null) {
							SemType st = oc.getSemType(stid);
							this.semTypes = VUtils.add(this.semTypes, st);
						} else {
							int x = 1;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// From Frame XML -- Not used
	public LexicalUnit(Frame frame, Element luNode) {
		this.frame = frame;
		this.id = luNode.getAttributeValue("ID");
		this.partOfSpeech = luNode.getAttributeValue("POS");
		this.name = luNode.getAttributeValue("name");
		Vector<Element> stnodes = JDomUtils.getElementsByName(luNode, "semType");
		if (stnodes != null) {
			for (Element stnode : stnodes) {
				String stid = stnode.getAttributeValue("ID");
				SemType st = frame.ontologyCreation.getSemType(stid);
				this.semTypes = VUtils.add(this.semTypes, st);
			}
		}
	}
	
	public Rule extractGrammarRule() {
		if (this.getSemTypes() != null) {
			TypeConstant tc = this.getSemTypes().firstElement().typeConstant;
			String word = this.getName();
			Vector<Vector> wlsts = VUtils.listify(VUtils.listify(word));
			String ruleid = "FN-" + word + "-" + this.getPartOfSpeech() + "-rule";
			
			Rule newrule = new moonstone.rule.Rule();
			newrule.setRuleID(ruleid);
			newrule.setType(tc);
			SyntacticTypeConstant pos = SyntacticTypeConstant.createSyntacticTypeConstant(this.getPartOfSpeech(), this.getPartOfSpeech());
			newrule.setPhraseType(pos);
			newrule.setPatternLists(wlsts);
			Sexp sexp = newrule.toSexp(false);
			newrule.setSexp(sexp);
			return newrule;
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public String getFrameID() {
		return frameID;
	}

	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public String getName() {
		return name;
	}

	public String getFrameRole(String role) {
		return this.getFrameRole(role);
	}

	public Vector<SemType> getSemTypes() {
		return semTypes;
	}

	public String toString() {
		String stname = "*";
		String str = "<LexicalUnit: ID=" + this.id + ", Name=" + this.name + ", Frame=" + this.frame.getName()
				+ ", SemTypes=" + this.semTypes + ">";
		return str;
	}

}

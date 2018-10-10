package moonstone.ontology.framenet;

import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import moonstone.rule.Rule;
import tsl.expression.term.type.TypeConstant;
import tsl.tsllisp.Sexp;
import tsl.utilities.HUtils;
import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class Frame {
	FramenetOntology ontologyCreation = null;
	String id = null;
	String name = null;
	Frame parent = null;
	Vector<SemType> semTypes = null;
	Hashtable<String, String> relatedFrameIDHash = new Hashtable();
	Hashtable<String, Frame> relatedFrameHash = new Hashtable();
	Vector<LexicalUnit> lexicalUnits = null;
	Hashtable<String, FrameElement> frameElementRoleHash = new Hashtable();
	Vector<FrameElement> allFrameElements = null;

	private static Hashtable<String, String> SeenFrameElementIDHash = new Hashtable();

	public Frame(FramenetOntology oc, String fpath) {
		this.ontologyCreation = oc;
		try {
			org.jdom.Document jdoc = new SAXBuilder().build(fpath);
			Element root = jdoc.getRootElement();
			Element frameNode = JDomUtils.getElementByName(root, "frame");
			if (frameNode != null) {
				String frameID = frameNode.getAttributeValue("ID");
				String frameName = frameNode.getAttributeValue("name");
				this.id = frameID;
				this.name = frameName;
				Vector<Element> FENodes = JDomUtils.getElementsByName(root, "FE");
				for (Element FENode : FENodes) {
					FrameElement fe = new FrameElement(this, FENode);
					this.frameElementRoleHash.put(fe.getThemeName(), fe);
					this.allFrameElements = VUtils.add(this.allFrameElements, fe);
				}
				Vector<Element> FRNodes = JDomUtils.getElementsByName(root, "frameRelation");
				for (Element FRNode : FRNodes) {
					String rname = FRNode.getAttributeValue("type");
					Element rfnode = JDomUtils.getElementByName(FRNode, "relatedFrame");
					if (rfnode != null) {
						String rfid = rfnode.getAttributeValue("ID");
						this.relatedFrameIDHash.put(rname, rfid);
					}
				}
				Vector<Element> LUNodes = JDomUtils.getElementsByName(root, "lexUnit");
				if (LUNodes != null) {
					for (Element LUNode : LUNodes) {
						String luid = LUNode.getAttributeValue("ID");
						LexicalUnit lu = this.ontologyCreation.getLexicalUnit(luid);
						this.lexicalUnits = VUtils.add(this.lexicalUnits, lu);
					}
				}

				Vector<Element> stnodes = JDomUtils.getSingleElementsByName(frameNode, "semType");
				if (stnodes != null) {
					for (Element stnode : stnodes) {
						String stid = stnode.getAttributeValue("ID");
						if (stid != null) {
							SemType st = oc.getSemType(stid);
							this.semTypes = VUtils.add(this.semTypes, st);
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resolveRelatedFrames() {
		Vector<String> rnames = HUtils.getKeys(this.relatedFrameIDHash);
		if (rnames != null) {
			for (String rname : rnames) {
				String rfid = this.relatedFrameIDHash.get(rname);
				Frame rframe = this.ontologyCreation.getFrame(rfid);
				this.relatedFrameHash.put(rname, rframe);
				if ("Inherits from".equals(rname)) {
					this.parent = rframe;
				}
			}
		}
	}

	public Rule extractGrammarRule() {
		Rule newrule = null;
		if (this.getSemTypes() != null && this.getLexicalUnitWords() != null) {
			TypeConstant tc = this.getSemTypes().firstElement().typeConstant;
			String ruleid = "FN-" + this.getName() + "-rule";
			Vector wlsts = null;
			Vector<Vector> plists = null;
			int pindex = 1;

			Vector<String> luwords = this.getLexicalUnitWords();
			wlsts = VUtils.add(wlsts, luwords);
			
			Vector<String> plst = VUtils.listify("trigger-word", "?0");
			plists = VUtils.add(plists, plst);

			for (String role : this.frameElementRoleHash.keySet()) {
				FrameElement fe = this.frameElementRoleHash.get(role);
				if (fe.getSemTypes() != null && fe.isCore()) {
					String vname = "?" + pindex++;
					plst = VUtils.listify(fe.getThemeName(), vname);
					plists = VUtils.add(plists, plst);
					TypeConstant fetc = fe.getSemTypes().firstElement().typeConstant;
					Vector wlst = VUtils.listify(fetc.getFormalName());
					wlsts = VUtils.add(wlsts, wlst);
				}
			}
			newrule = new moonstone.rule.Rule();
			newrule.setRuleID(ruleid);
			newrule.setType(tc);
			newrule.setPatternLists(wlsts);
			newrule.setPropertyPredicates(plists);
			Sexp sexp = newrule.toSexp(false);
			newrule.setSexp(sexp);
		}
		return newrule;
	}

	public Vector<String> getLexicalUnitWords() {
		Vector<String> words = null;
		if (this.lexicalUnits != null) {
			for (LexicalUnit lu : this.lexicalUnits) {
				String word = lu.getName();
				int index = word.lastIndexOf('.');
				if (index > 0) {
					word = word.substring(0, index);
				}
				words = VUtils.addIfNot(words, word);
			}
		}
		return words;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Frame getParent() {
		return parent;
	}

	public Vector<SemType> getSemTypes() {
		return semTypes;
	}

	public String toString() {
		String str = "<Frame: ID=" + this.id + ", Name=" + this.name + ">";
		return str;
	}

}

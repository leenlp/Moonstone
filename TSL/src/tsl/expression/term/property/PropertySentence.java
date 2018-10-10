package tsl.expression.term.property;

import java.util.Vector;

import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.BinaryRelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class PropertySentence extends BinaryRelationSentence {
	
	public PropertySentence(PropertyConstant pc, Term domain,
			Object range) {
		super(pc);
		if (!(range instanceof ObjectConstant)) {
			range = new ObjectConstant(range);
		}
		this.addTerm(domain);
		this.addTerm(range);
		
		// 4/12/2013:  This is redundant:  Storing the property as a straight-up 
		// Java Object in the Information.property table.  Do I need this?
		domain.setProperty(pc.getName(), range);
		this.getSubject();
		this.getModifier();
	}
	
	public static PropertySentence createPropertySentence(PropertyConstant pc,
			Term domain, Object range) {
		if (pc.testAssignments(domain, range)) {
			return new PropertySentence(pc, domain, range);
		}
		return null;
	}
	
	public ObjectConstant getRange() {
		if (this.getTerms() != null) {
			ObjectConstant oc = (ObjectConstant) this.getTerms().elementAt(1);
			return oc;
		}
		return null;
	}
	
	public Object getRangeValue() {
		ObjectConstant range = this.getRange();
		return (range != null ? range.getObject() : null);
	}
	
	// 5/6/2017
	/**
	public static PropertySentence createPropertySentence(Vector v) {
		if (v != null && "defpropertysentence".equals(v.firstElement())) {
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			kb.getTypeConstant(tname)
			String pname = (String) v.elementAt(1);
			String subjecttypename = (String) v.elementAt(2);
			String rangevaluestr = (String) v.elementAt(3);
			PropertyConstant pconst = PropertyConstant.
			TypeConstant subjecttype = TypeConstant.findByName(subjecttypename);
			TypeConstant modifiertype = TypeConstant.findByName(modifiertypename);
			TypeConstant type = new TypeConstant(name);
			type.defv = v;
			return type;
		}
		return null;
	}
	**/

}

package tsl.expression.term.relation;

import java.util.Vector;

import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class JavaRelationSentence extends RelationSentence {

	public JavaRelationSentence(Vector v) {
		super();
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		// Before 3/24/2014
//		KnowledgeBase kb = this.getKnowledgeBase();
		String mname = (String) v.firstElement();
		JavaRelationConstant jrc = JavaRelationConstant
				.createJavaRelationConstant(mname);
		this.setRelation(jrc);
		for (int i = 1; i < v.size(); i++) {
			Object o = v.elementAt(i);
			Object t = kb.getTerm(this, o);
			this.addTerm(t);
		}
		kb.addRelationSentenceList(this);
	}

	public static JavaRelationSentence createJavaRelationSentence(Vector v) {
		if (v.firstElement() instanceof String) {
			String mname = (String) v.firstElement();
			JavaRelationConstant jrc = JavaRelationConstant
					.createJavaRelationConstant(mname);
			if (jrc != null) {
				return new JavaRelationSentence(v);
			}
		}
		return null;
	}
	
	public Boolean eval() {
		boolean result = false;
		try {
			if (this.argumentArray == null) {
				this.argumentArray = new Object[this.getTermCount()];
			}
			if (!Term.unpack(this.getTerms(), this.argumentArray)) {
				return false;
			}
			JavaRelationConstant jrc = (JavaRelationConstant) this.getRelation();
			result = (Boolean) jrc.getMethod().invoke(null, this.argumentArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}

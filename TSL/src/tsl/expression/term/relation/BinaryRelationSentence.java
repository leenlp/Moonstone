package tsl.expression.term.relation;

import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.NameSpace;

public class BinaryRelationSentence extends RelationSentence {

	public BinaryRelationSentence() {
		super();
	}

	public BinaryRelationSentence(RelationConstant rc) {
		super(rc);
	}

	public BinaryRelationSentence(RelationConstant rc, Term subject,
			Term modifier) {
		super(rc);
		this.setSubject(subject);
		this.setModifier(modifier);
	}
	
	public static BinaryRelationSentence createBinaryRelationSentence(
			Object relation, Term subject, Term modifier) {
		RelationConstant rc = null;
		if (relation instanceof RelationConstant) {
			rc = (RelationConstant) relation;
		} else if (relation instanceof String) {
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			NameSpace ns = kb.getNameSpace();
			rc = ns.getRelationConstant((String) relation);
		}
		if (rc != null && rc.testAssignments(subject, modifier)) {
			BinaryRelationSentence rs = new BinaryRelationSentence(rc, subject,
					modifier);
			return rs;
		}
		return null;
	}

}

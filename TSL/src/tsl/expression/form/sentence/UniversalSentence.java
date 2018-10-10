package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.knowledge.knowledgebase.KnowledgeBase;

// (forall (?x) (and (condition ?x "fever") (polarity ?x "present")))

public class UniversalSentence extends QuantifierSentence {
	
	public UniversalSentence() {
		super();
	}
	
	public UniversalSentence(Vector v) {
		super(v);
	}

	public static UniversalSentence createUniversalSentence(Vector v) {
		if (v != null && "forall".equals(v.firstElement())) {
			return new UniversalSentence(v);
		}
		return null;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		this.setKnowledgeBase(kb);
		this.sentence.setContainingKBExpression(containingKBExpression);
	}
	
}

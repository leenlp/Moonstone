package tsl.expression.term.type;

import java.util.Vector;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class TypePredicate extends Sentence {
	private TypeConstant type = null;
	private Term argument = null;
	
	public TypePredicate(String tname, String aname) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.type = kb.getNameSpace().getTypeConstant(tname);
		this.argument = (Term) kb.getTerm(this, aname);
	}
	
	public static TypePredicate createTypePredicate(Vector v) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (v.size() == 2) {
			String tname = v.elementAt(0).toString();
			String aname = v.elementAt(1).toString();
			TypeConstant type = kb.getNameSpace().getTypeConstant(tname);
			if (type != null) {
				return new TypePredicate(tname, aname);
			}
		}
		return null;
	}

	public TypeConstant getType() {
		return type;
	}

	public Term getArgument() {
		return argument;
	}

}

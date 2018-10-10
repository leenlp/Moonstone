package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

// (exists (?x) (and (condition ?x "fever") (polarity ?x "present")))

public class ExistentialSentence extends QuantifierSentence {
	
	public ExistentialSentence() {
		
	}
	
	public ExistentialSentence(Vector v) {
		super(v);
	}

	public static ExistentialSentence createExistentialSentence(Vector v) {
		if (v != null && "exists".equals(v.firstElement())) {
			return new ExistentialSentence(v);
		}
		return null;
	}
	
	public String toString() {
		String str = "(exists (";
		for (int i = 0; i < this.getVariableCount(); i++) {
			Variable var = this.getVariable(i);
			str += var.getName();
			if (i < this.getVariableCount()-1) {
				str += " ";
			}
		}
		str += ") ";
		str += this.getSentence().toString();
		str += ")";
		return str;
	}

}

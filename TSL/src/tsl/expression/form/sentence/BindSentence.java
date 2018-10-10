package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;

public class BindSentence extends RelationSentence {
	
	public BindSentence(Vector v) {
		super(v);
	}

	public static BindSentence createBindSentence(Vector v) {
		BindSentence sent = null;
		if ("bind".equals(v.firstElement())) {
			sent = new BindSentence(v);
		}
		return sent;
	}

}

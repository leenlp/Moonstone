package tsl.expression.term;

import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.utilities.VUtils;

public class SituationTerm extends Term {
	private Sentence sentence = null;
	private Vector<Sentence> facts = null;
	
	public SituationTerm(Sentence sentence) {
		this.sentence = sentence;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	
	public Vector<Sentence> getFacts() {
		return this.facts;
	}
	
	public void addFact(Sentence fact) {
		this.facts = VUtils.add(this.facts, fact);
	}

}

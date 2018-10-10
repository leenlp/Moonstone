package tsl.expression.term;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.Sentence;

// Permits a sentence to be used as an argument to another sentence.

public class SentenceTerm extends Term {
	private Sentence sentence = null;
	
	public SentenceTerm(String name) {
		super(name);
	}
	
	public SentenceTerm(String name, Sentence sentence) {
		super(name);
		this.sentence = sentence;
	}
	
	public SentenceTerm(Sentence sentence) {
		this.sentence = sentence;
	}
	
	// 3/30/2014:  Create SentenceTerm representing an object containing all the RelSents
	// that describe a particular Term.
	public static SentenceTerm createSentenceTerm(Term subject) {
		AndSentence as = AndSentence.createAndSentence(subject);
		SentenceTerm st = null;
		if (as != null) {
			st = new SentenceTerm(as);
		}
		return st;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

}

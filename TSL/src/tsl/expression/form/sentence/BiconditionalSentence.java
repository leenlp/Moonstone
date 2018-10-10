package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.Symbol;
import tsl.tsllisp.TLUtils;

public class BiconditionalSentence extends ImplicationSentence {

	
	public BiconditionalSentence() {
	}

	public BiconditionalSentence(String name) {
		super(name);
	}

	public BiconditionalSentence(Vector v) {
		super();
		if (v.size() == 3) {
			this.antecedent = Sentence.createSentence((Vector) v.elementAt(1));
			this.consequent = Sentence.createSentence((Vector) v.elementAt(2));
		}
	}

	public static BiconditionalSentence createBiconditionalSentence(Vector v) {
		if (v != null && "<->".equals(v.firstElement())) {
			return new BiconditionalSentence(v);
		}
		return null;
	}
	
	public static boolean isBiconditionalSentence(Sexp sexp) {
		return (sexp.getLength() == 3
				&& TLUtils.isSymbol(sexp.getFirst())
				&& "<->".equals(((Symbol) sexp.getFirst()).getName())
				&& TLUtils.isCons(sexp.getSecond())
				&& TLUtils.isCons(sexp.getThird()));
	}

	
}

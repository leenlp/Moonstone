package moonstone.rule;

import java.util.Vector;

import tsl.tsllisp.Sexp;
import moonstone.grammar.Grammar;
import moonstone.grammar.GrammarModule;

public class LabelRule extends moonstone.rule.Rule {
	
	public LabelRule(Grammar grammar, Sexp sexp, Vector pattern, Vector wordLists) {
		super(grammar, sexp, pattern, wordLists);
	}

}

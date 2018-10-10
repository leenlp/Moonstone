package moonstone.annotation;

import java.util.Vector;
import moonstone.rule.Rule;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;

public class TagAnnotation extends TerminalAnnotation {

	public TagAnnotation(WordSequenceAnnotation sentence, String cui,
			String concept, SyntacticTypeConstant ptype, String string, int tokenStart,
			int tokenEnd, int textStart, int textEnd, int wordTokenStart,
			int wordTokenEnd, Object value, TypeConstant tc) {
		super(sentence, cui, concept, ptype, string, tokenStart, tokenEnd,
				textStart, textEnd, wordTokenStart, wordTokenEnd, value, tc);
	}

	// 12/5/2014
	public Vector<Rule> getTagRules() {
		return this.getSentenceAnnotation().getGrammar()
				.getRulesByIndexToken(this.getString());
	}
	
	// 6/28/2015
	private void applyConTextModifiersForward(Vector<Annotation> annotations) {
		WordSequenceAnnotation sa = this.getSentenceAnnotation();
		Rule trule = this.getRule();
		String mdir = trule.getModifierDirection();
		for (Annotation annotation : annotations) {
			
		}
	}


}

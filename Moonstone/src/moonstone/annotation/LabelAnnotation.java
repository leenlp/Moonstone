package moonstone.annotation;

import moonstone.rule.Rule;

public class LabelAnnotation extends TerminalAnnotation {

	public LabelAnnotation(WordSequenceAnnotation sentence, Rule rule,
			String string, int tokenStart, int tokenEnd, int textStart,
			int textEnd, int wordTokenStart, int wordTokenEnd) {
		super(sentence, string);
		sentence.addStopWordLabel(this);
	}

}

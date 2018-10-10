package moonstone.annotation;

import java.util.Vector;

import moonstone.syntactic.Syntax;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.VUtils;

public class StringAnnotation extends TerminalAnnotation {

	public StringAnnotation(WordSequenceAnnotation sentence, String string,
			int tokenStart, int tokenEnd, int textStart, int textEnd,
			int wordTokenStart, int wordTokenEnd, Object value) {
		super(sentence, null, null, Syntax.NUMBER, string, tokenStart,
				tokenEnd, textStart, textEnd, wordTokenStart, wordTokenEnd,
				value, TypeConstant.createTypeConstant("NUMBER"));
	}

	public void generateIndexTokens() {
		Vector<String> tokens = VUtils.listify(Syntax.NUMBER);
		tokens.add(this.getTypeIndex());
		this.indexTokens = tokens;
	}

}

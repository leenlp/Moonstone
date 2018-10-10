package moonstone.annotation;

import java.util.Vector;

import moonstone.syntactic.Syntax;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.VUtils;

public class NumberAnnotation extends TerminalAnnotation {

	private static TypeConstant NumberType = TypeConstant.createTypeConstant("number");

	public NumberAnnotation() {
		super();
	}

	public NumberAnnotation(WordSequenceAnnotation sentence, String string, int tokenStart, int tokenEnd, int textStart,
			int textEnd, int wordTokenStart, int wordTokenEnd, Object value) {
		super(sentence, null, null, Syntax.NUMBER, string, tokenStart, tokenEnd, textStart, textEnd, wordTokenStart,
				wordTokenEnd, value, NumberType);
	}

	public void generateIndexTokens() {
		Vector tokens = VUtils.listify(Syntax.NUMBER);
		tokens.add(this.getTypeIndex());
		
		// 2/23/2018
		tokens.add(SyntacticTypeConstant.NUMBER_SYNTACTIC_TYPE_CONSTANT );

		this.indexTokens = tokens;

	}

}

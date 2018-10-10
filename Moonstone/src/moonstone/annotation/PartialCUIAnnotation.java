package moonstone.annotation;

import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.Ontology;

public class PartialCUIAnnotation extends Annotation {

	public PartialCUIAnnotation(WordSequenceAnnotation sentence, String type,
			String string, int tokenStart, int tokenEnd, int textStart,
			int textEnd, Object value, TypeConstant tc, Ontology ontology) {
//		super(sentence, type, null, null, string, tokenStart, tokenEnd,
//				textStart, textEnd, value, null, tc, ontology);
	}
	
	// Not indexing these yet...
//	public void finishUp(Rule rule, Object result) {
//		int x = 1;
//		x = x;
//	}

}

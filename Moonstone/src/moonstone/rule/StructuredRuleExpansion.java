package moonstone.rule;

import moonstone.annotation.Annotation;

public class StructuredRuleExpansion extends RuleExpansion {
	private int lastMatchedIndex = 0;

	public StructuredRuleExpansion(Rule rule, Annotation matched) {
		super(rule, matched, 0);
	}

	public StructuredRuleExpansion(StructuredRuleExpansion expansion,
			Annotation matched) {
		super(expansion, matched, (expansion.lastMatchedIndex + 1));
		this.lastMatchedIndex = (expansion.lastMatchedIndex + 1);
	}

	public int getLastMatchedIndex() {
		return lastMatchedIndex;
	}

//	public boolean checkIsFullyMatched() {
//		return this.lastMatchedIndex == this.getRule().getWordListCount() - 1;
//	}
	
//	public boolean checkIsValid() {
//		return true;
//	}

	public Annotation getLastMatchedAnnotation() {
		return this.matchedAnnotations[this.lastMatchedIndex];
	}
	
	public Annotation getFirstMatchedAnnotation() {
		return this.matchedAnnotations[0];
	}
	
	public int getStartToken() {
		Annotation annotation = this.getFirstMatchedAnnotation();
		return annotation.getTokenStart();
	}
	
	public int getEndToken() {
		Annotation annotation = this.getLastMatchedAnnotation();
		return annotation.getTokenEnd();
	}

}

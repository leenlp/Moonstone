package moonstone.annotation;

import java.util.Vector;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.utilities.SeqUtils;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;

public class DocumentAnnotation extends Annotation {

	public DocumentAnnotation(WordSequenceAnnotation sentenceAnnotation,
			Rule rule, Vector<Annotation> sources, boolean doExpand) {
		super(sentenceAnnotation, rule, sources, doExpand);
		if (this.getSemanticInterpretation() != null) {
			// this.getSemanticInterpretation().getRelationSentences();
		}
	}

	// Before 4/17/2015
	// public void assignRelativeOffsets() {
	// Annotation start = this.textuallySortedSourceAnnotations.firstElement();
	// Annotation end = this.textuallySortedSourceAnnotations.lastElement();
	// this.relativeTokenStart = start.tokenStart;
	// this.relativeTokenEnd = end.tokenEnd;
	// }

	public boolean checkIsValid() {
		try {
			if (this.getTextlength() > 1000) {
				int x = 1;
			}
			Vector<Annotation> children = this.getTextuallySortedSourceAnnotations();
			if (this.rule.isDoDebug()) {
				int x = 1;
			}
			
			Rule rule = this.rule;
			float depth = this.getDepth();
			if (this.getDepth() > 20) {
				this.setInvalidReason(Annotation.FailDepth);
				return false;
			}
			if (this.typeLoopCheck(this)) {
				this.setInvalidReason(Annotation.FailTypeCheck);
				return false;
			}
			for (int i = 0; i < this.textuallySortedSourceAnnotations.size() - 1; i++) {
				Annotation a1 = this.textuallySortedSourceAnnotations
						.elementAt(i);
				Annotation a2 = this.textuallySortedSourceAnnotations
						.elementAt(i + 1);
				if (!SeqUtils.disjoint(a1.getTokenStart(), a1.getTokenEnd(),
						a2.getTokenStart(), a2.getTokenEnd())) {
					this.setInvalidReason(RuleExpansion.HasOverlap);
					return false;
				}
				
				// 1/15/2016:  JUST FOR READMISSION:  I DON'T NEED MULTI-SENTENCE
				// ANNOTATIONS
				// 2/26/2016:  When I discard document-level overlapping annotations, 
				// invalid multi-sentence annotations with low probability can swallow 
				// more valid ones.  Need to think of a good solution.
				if (!a1.getSentenceAnnotation().equals(a2.getSentenceAnnotation())) {
					this.setInvalidReason("DifferentSentences");
					return false;
				}
			}
			if (this.getRule().isOrdered()) {
				for (int i = 0; i < this.lexicallySortedSourceAnnotations
						.size() - 1; i++) {
					Annotation a1 = this.lexicallySortedSourceAnnotations
							.elementAt(i);
					Annotation a2 = this.lexicallySortedSourceAnnotations
							.elementAt(i + 1);
					if (!(a1.getTokenEnd() < a2.getTokenStart())) {
						this.setInvalidReason(RuleExpansion.NotOrdered);
						return false;
					}
				}
			}

			if (this.getRule().getTestPredicates() != null) {
				if (this.rule.isDoDebug()) {
					int x = 1;
				}
				for (Constraint c : this.getRule().getTestPredicates()) {
					Boolean rv = (Boolean) c
							.evalConstraint(this.getVariables());
					if (rv == null || !rv) {
						this.setInvalidReason(Annotation.FailTest);
						
						if (this.getConcept().toString().toLowerCase().contains("support")) {
							int x = 1;
						}
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}

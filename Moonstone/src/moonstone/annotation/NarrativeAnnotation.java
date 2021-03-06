package moonstone.annotation;

import java.util.Vector;

import tsl.utilities.SeqUtils;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;

public class NarrativeAnnotation extends Annotation {

	public NarrativeAnnotation(WordSequenceAnnotation sentenceAnnotation, Rule rule, Vector<Annotation> sources,
			boolean doExpand) {
		super(sentenceAnnotation, rule, sources, doExpand);
	}

	// ADD VINCI
	public boolean checkIsValid() {
		try {
			Rule rule = this.getRule();
			
			if (rule != null && rule.isDoDebug()) {
				System.out.println("Debug annotation: Text\"" + this.getText() + "\", Rule=" + rule.getName());
				int x = 0;
			}

			if (!this.validateRuleSubpattern()) {
				this.setInvalidReason(Annotation.FailSpecialization);
				return false;
			}

			if (this.getDepth() > 10) {
				this.setInvalidReason(Annotation.FailDepth);
				return false;
			}


			// 4/23/2018:  Is this causing difficulties?
//			if (this.getDepth() > 20) {
//				this.setInvalidReason(Annotation.FailDepth;
//				return false;
//			}

			if (this.typeLoopCheck(this)) {
				this.setInvalidReason(Annotation.FailTypeCheck);
				return false;
			}

			// 8/4/2016
			if (rule != null && rule.getPhraseType() != null && this.getPhraseType() != null
					&& !rule.getPhraseType().equals(this.getPhraseType())) {
				this.setInvalidReason(FailPhraseTypeCheck);
				return false;
			}

			// NEW: 3/11/2016
			int totalAnnotationTokenCount = this.textuallySortedSourceAnnotations.firstElement().getTokenLength();

			// 6/23/2016
			boolean childContainsTarget = false;

			for (int i = 0; i < this.textuallySortedSourceAnnotations.size() - 1; i++) {
				Annotation a1 = this.textuallySortedSourceAnnotations.elementAt(i);
				Annotation a2 = this.textuallySortedSourceAnnotations.elementAt(i + 1);

				if (!childContainsTarget) {
					if (a1.containsTargetConcept()) {
						childContainsTarget = true;
					}
				}

				totalAnnotationTokenCount += a2.getTokenLength();
				int rt1start = a1.getRelativeTokenStart();
				int rt1end = a1.getRelativeTokenEnd();
				int rt2start = a2.getRelativeTokenStart();
				int rt2end = a2.getRelativeTokenEnd();

				int interstitialsize = (a2.getWordTokenStart() - 1) - (a1.getWordTokenEnd() + 1);
				if (interstitialsize > 4) {
					int index1 = this.getLexicallySortedSourceAnnotations().indexOf(a1);
					int index2 = this.getLexicallySortedSourceAnnotations().indexOf(a2);
					if (index1 == index2 - 1) {

						// 1/14/2016: IDEA: Limit the amount of interstitial
						// space
						// PROBLEM: This eliminates valid sibling annotations.
						// Need
						// to fix this...

						this.setInvalidReason("InterstitialTooLarge");
						return false;
					}
				}

				if (!SeqUtils.disjoint(rt1start, rt1end, rt2start, rt2end)) {
					this.setInvalidReason(RuleExpansion.HasOverlap);
					return false;
				}

				// 3/11/2016
				if (totalAnnotationTokenCount > rule.getWindow()) {
					this.setInvalidReason(RuleExpansion.TooLarge);
					return false;
				}

				if (!this.getRule().isPermitInterstitialAnnotations()
						&& grammar.interpretedAnnotationsExist(rt1end + 1, rt2start - 1)) {
					this.setInvalidReason(RuleExpansion.HasInterstitial);
					return false;
				}

				if (grammar.containsUniversalStopword(rt1end + 1, rt2start - 1)) {
					// this.setInvalidReason(Annotation.ContainsStopword;
					// return false;
				}
			}
			if (this.getRule().isOrdered() || this.getRule().isJuxtaposed()) {
				for (int i = 0; i < this.lexicallySortedSourceAnnotations.size() - 1; i++) {
					Annotation a1 = this.lexicallySortedSourceAnnotations.elementAt(i);
					Annotation a2 = this.lexicallySortedSourceAnnotations.elementAt(i + 1);
					int rt1start = a1.getRelativeTokenStart();
					int rt1end = a1.getRelativeTokenEnd();
					int rt2start = a2.getRelativeTokenStart();
					int rt2end = a2.getRelativeTokenEnd();

					if (!(rt1end < rt2start)) {
						this.setInvalidReason(RuleExpansion.NotOrdered);
						return false;
					}
					// 3/4/2016: Took out stopword failure. This interferes
					// with all legitimate phrases containing "and"...
					if (this.getRule().isJuxtaposed()) {
						if (a1.getWordTokenEnd() != a2.getWordTokenStart() - 1) {
							this.setInvalidReason(RuleExpansion.NotJuxtaposed);
							return false;
						}
					}
				}
			}
			if (this.getRule().getStopWords() != null && this.getSentenceAnnotation().getStopWordLabels() != null) {
				for (LabelAnnotation la : this.getSentenceAnnotation().getStopWordLabels()) {
					if (la.getRule() == this.getRule()
							&& this.textuallySortedSourceAnnotations.firstElement().getRelativeTokenStart() <= la
									.getTokenStart()
							&& la.getTokenEnd() <= this.textuallySortedSourceAnnotations.lastElement()
									.getRelativeTokenEnd()) {
						// this.setInvalidReason(Annotation.ContainsStopword;
						// return false;
					}
				}
			}
			if (rule != null && rule.isDoDebug()) {
				int x = 0;
			}
			if (!this.isValidPerRuleConstraints()) {
				return false;
			}

			if (!this.isValidPerTrainingOffsets()) {
				return false;
			}

			// TEST: 6/23/2016 NOT SURE IF THIS HELPS!
			// 7/26/2016 decision: Handle this within RuleInterface. Gather top
			// target-containing
			// annotations into input to document grammar.
			if (childContainsTarget && !this.containsTargetConcept()) {
				// this.setInvalidReason("LostTargetConcept";

				// 7/22/2016: Problem: This fails when a concept is produced,
				// but not by the target concept rule (which comes
				// after). False artifact.
				// return false;
			}

			// TEST: 6/24/2016
			if (this.getSentenceAnnotation().containsPunctuation(this)) {
				// this.setInvalidReason("ContainsPunctuation";
				// return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}

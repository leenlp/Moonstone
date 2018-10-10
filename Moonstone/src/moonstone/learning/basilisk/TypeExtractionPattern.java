package moonstone.learning.basilisk;

import java.util.Vector;

import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;

public class TypeExtractionPattern extends ExtractionPattern {

	public TypeExtractionPattern(Annotation annotation,
			Vector<Annotation> anchors) {
		super(annotation, anchors);
	}

	public static boolean canExtractPattern(Annotation annotation) {
		if (annotation.getRule() != null
				&& !annotation.isTerminal()
				&& !annotation.getRule().isVisited()
				&& annotation.getRule().getRuleID() != null
				&& ExtractionPattern.ValidRuleIDs.contains(annotation.getRule()
						.getRuleID()) && annotation.getChildren().size() >= 2
				&& !annotationContainsConjunct(annotation)) {
			boolean foundanchor = false;
			boolean foundcontext = false;
			for (Annotation cannotation : annotation.getChildAnnotations()) {
				if (cannotation.getTokenLength() > 4) {
					return false;
				}
				if (canUseAsAnchor(cannotation)) {
					if (!foundanchor) {
						foundanchor = true;
					} else {
						foundcontext = true;
					}
				}
			}
			return foundanchor && foundcontext;
		}
		return false;
	}

	private static String[] ConjunctStrings = { "and", "or" };

	private static boolean annotationContainsConjunct(Annotation annotation) {
		for (int i = 0; i < ConjunctStrings.length; i++) {
			String cstr = ConjunctStrings[i];
			if (annotation.getString().contains(cstr)) {
				return true;
			}
		}
		return false;
	}

	public static Vector<Annotation> gatherCandidateAnnotations(
			Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.getRule() != null && !annotation.isTerminal()) {
			if (ExtractionPattern.canExtractTypePattern(annotation)) {
				annotation.getRule().setVisited(true);
				annotations = VUtils.listify(annotation);
			}
			for (Annotation ca : annotation
					.getLexicallySortedSourceAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherCandidateAnnotations(ca));
			}
			if (annotation.getRule() != null
					&& annotation.getRule().isVisited()) {
				annotation.getRule().setVisited(false);
			}
		}
		return annotations;
	}

}

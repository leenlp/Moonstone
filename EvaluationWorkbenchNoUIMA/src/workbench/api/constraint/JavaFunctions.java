package workbench.api.constraint;

import java.util.Vector;

import tsl.expression.term.type.TypeConstant;
import workbench.api.annotation.Annotation;
import workbench.api.typesystem.Attribute;

public class JavaFunctions {
	
	public static boolean adjudicationEHostVsMoonstone(Annotation annotation,
			String value, String loop) {
		if (annotationHasClassification(annotation, value)) {
			String aname = annotation.getKtAnnotation().getAnnotatorName()
					.toLowerCase();
			boolean isMoonstone = aname.contains("moonstone");
			boolean hasValidationCorrect = annotationHasAttributeValue(
					annotation, "validation", "correct");
			boolean hasValidationIncorrect = annotationHasAttributeValue(
					annotation, "validation", "incorrect");

			boolean isValid = true;
			if (hasValidationCorrect) {
				isValid = true;
			} else if (hasValidationIncorrect) {
				isValid = false;
			} else if (!isMoonstone && !hasValidationIncorrect) {
				isValid = true;
			}

			if ("TP-FN-PRIMARY".equals(loop)) {
				return isValid;
			}
			if ("TP-FN-SECONDARY".equals(loop)) {
				return isValid && isMoonstone;
			}
			if ("FP".equals(loop)) {
				return isMoonstone && !isValid;
			}
		}
		return false;
	}

	public static boolean annotationPairHasSameType(Annotation a1, Annotation a2) {
		return a1 != null && a2 != null && a1.getType().equals(a2.getType());
	}

	public static boolean annotationHasClassification(Annotation annotation,
			Object value) {
		Object cvalue = annotation.getClassificationValue();
		boolean rv = value.equals(cvalue);
		if (rv) {
			int x = 1;
		}
 		return rv;
	}
	
	public static boolean annotationHasValue(Annotation annotation, Object value) {
		Vector<Attribute> attributes = annotation.getAllAttributes();
		if (attributes != null) {
			for (Attribute attr : attributes) {
				Object o = annotation.getAttributeValue(attr);
				if (value.equals(o)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean sameSemanticType(Annotation a1, Annotation a2) {
		return (a1.getType() != null && a1.getType().equals(a2.getType()));
	}

	public static boolean hasSemanticType(Annotation a, String tname) {
		return (a.getType() != null && tname != null && tname.equals(a
				.getType().getName()));
	}
	
	// 8/19/2017
	public static boolean annotationIsUnifiable(Annotation annotation, String tname) {
		boolean rv = annotation.getType().getFullname().contains(tname);
		if (rv) {
			int x = 1;
		}
		return rv;
	}
	
	public static boolean annotationHasAttributeValue(Annotation annotation,
			String aname, Object value) {
		Object o = annotation.getAttributeValue(aname);
		boolean result = value.equals(o);
		return result;
	}

	public static boolean annotationHasAttribute(Annotation annotation,
			String aname) {
		Attribute attr = annotation.getType().getAttribute(aname);
		return attr != null;
	}

}

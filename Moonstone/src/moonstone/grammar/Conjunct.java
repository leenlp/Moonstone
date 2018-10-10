package moonstone.grammar;

import java.util.Vector;

import tsl.utilities.SetUtils;
import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;

public class Conjunct {

	public static Vector<Annotation> getExpandedConjuncts(
			Vector<Annotation> annotations) {
		Vector<Annotation> expanded = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.containsConjunct()) {
					expanded = VUtils.append(expanded,
							expandConjuncts(annotation));
				}
			}
		}
		return expanded;
	}

	public static Vector<Annotation> expandConjuncts(Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.isConjunct()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				annotations = VUtils
						.append(annotations, expandConjuncts(child));
			}
		}
		if (annotations == null && annotation.containsConjunct()) {
			annotations = distributeConjuncts(annotation);
		}
		if (annotations == null && annotation.isInterpreted()) {
			annotations = VUtils.listify(annotation);
		}
		return annotations;
	}

	public static Vector<Annotation> distributeConjuncts(Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.containsConjunct()) {
			Vector<Vector<Annotation>> children = null;
			for (Annotation child : annotation.getChildAnnotations()) {
				if (child.isConjunct()) {
					Vector<Annotation> conjuncts = expandConjuncts(child);
					children = VUtils.add(children, conjuncts);
				} else {
					children = VUtils.add(children, VUtils.listify(child));
				}
			}
			Vector<Vector<Annotation>> csets = SetUtils
					.cartesianProduct(children);
			for (Vector<Annotation> cset : csets) {
				Annotation ca = new Annotation(annotation, cset);
				annotations = VUtils.add(annotations, ca);
			}
		}
		return annotations;
	}

}

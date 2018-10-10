package workbench.api.annotation;

import java.util.Comparator;
import java.util.Vector;

import tsl.expression.term.variable.Variable;
import tsl.utilities.VUtils;

public class OverlappingAnnotationPair {

	private Annotation primaryAnnotation = null;
	private Annotation secondaryAnnotation = null;
	String documentName = null;
	private Vector<Variable> variables = null;
	private boolean isStrict = false;
	private int nonOverlappingDistance = 0;

	public OverlappingAnnotationPair(Annotation a1, Annotation a2,
			boolean strict) {
		this.primaryAnnotation = a1;
		this.secondaryAnnotation = a2;
		this.documentName = a1.getAnnotationCollection().getAnnotationEvent()
				.getDocumentName();
		this.addVariable("?annotation1", a1);
		this.addVariable("?annotation2", a2);
		this.isStrict = strict;
		if (!strict) {
			this.nonOverlappingDistance = Math.abs((a1.getStart() - a2
					.getStart()) - Math.abs(a1.getEnd() - a2.getEnd()));
		}
		a1.addOverlappingAnnotationPair(this);
		a2.addOverlappingAnnotationPair(this);
	}

	public boolean isStrict() {
		return isStrict;
	}

	public Annotation getPrimaryAnnotation() {
		return primaryAnnotation;
	}

	public Annotation getSecondaryAnnotation() {
		return secondaryAnnotation;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public String toString() {
		String str = "<Pair: " + this.primaryAnnotation + ":"
				+ this.secondaryAnnotation + ">";
		return str;
	}

	public Vector<Variable> getVariables() {
		return variables;
	}

	public void addVariable(String vname, Object value) {
		if (Variable.find(this.variables, vname) == null) {
			Variable var = new Variable(vname);
			var.bind(value);
			this.variables = VUtils.add(this.variables, var);
		}
	}

	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			OverlappingAnnotationPair pair1 = (OverlappingAnnotationPair) o1;
			OverlappingAnnotationPair pair2 = (OverlappingAnnotationPair) o2;
			Annotation a1 = (Annotation) pair1.getPrimaryAnnotation();
			Annotation a2 = (Annotation) pair2.getSecondaryAnnotation();
			if (a1.getStart() < a2.getStart()) {
				return -1;
			}
			if (a2.getStart() < a1.getStart()) {
				return 1;
			}
			if (a1.getEnd() > a2.getEnd()) {
				return -1;
			}
			if (a2.getEnd() > a1.getEnd()) {
				return 1;
			}
			return 0;
		}
	}
	
	public static class LengthSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			OverlappingAnnotationPair pair1 = (OverlappingAnnotationPair) o1;
			OverlappingAnnotationPair pair2 = (OverlappingAnnotationPair) o2;
			if (pair1.getNonOverlappingDistance() < pair2.getNonOverlappingDistance()) {
				return -1;
			}
			if (pair1.getNonOverlappingDistance() > pair2.getNonOverlappingDistance()) {
				return 1;
			}
			return 0;
		}
	}

	public int getNonOverlappingDistance() {
		return nonOverlappingDistance;
	}

}

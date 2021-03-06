package workbench.api.constraint;

import java.util.Vector;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import workbench.api.Analysis;

public class ConstraintPacket {

	private Constraint constraint = null;
	private Analysis analysis = null;
	private String name = null;
	private String constraintExpression = null;
	public static String DefaultClassificationConstraintName = "AnnotationHasClassification";
	public static String DefaultAttributeValueConstraintName = "AnnotationHasValue";
	

	public ConstraintPacket(String name, String expr, Constraint constraint,
			Analysis analysis) {
		this.name = name;
		this.constraintExpression = expr;
		this.constraint = constraint;
		this.analysis = analysis;
		this.analysis.addConstraintPacket(this);
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public String getName() {
		return name;
	}

	public String getConstraintExpression() {
		return constraintExpression;
	}

	public String toString() {
		return "<Constraint: Name=" + this.name + ">";
	}
	
	// 10/20/2014
	public static ConstraintPacket createConstraintPacket(KnowledgeBase kb,
			String name, Vector v, String cstr, Analysis analysis) throws Exception {
		ConstraintPacket cp = null;
		Constraint constraint = Constraint.createConstraint(kb, v);
		if (constraint != null) {
			cp = new ConstraintPacket(name, cstr, constraint, analysis);
		}
		return cp;
	}

	public static ConstraintPacket createConstraintPacket(KnowledgeBase kb,
			String name, String expr, Analysis analysis) throws Exception {
		ConstraintPacket cp = null;
		if (expr.charAt(0) != '\'') {
			expr = '\'' + expr;
		}
		Constraint constraint = Constraint.createConstraint(kb, expr);
		if (constraint != null) {
			cp = new ConstraintPacket(name, expr, constraint, analysis);
		}
		return cp;
	}

}

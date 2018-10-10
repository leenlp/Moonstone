package workbench.api.typesystem;

import workbench.api.annotation.Annotation;

public class Relation {
	private String relation = null;
	private Annotation subject = null;
	private Annotation modifier = null;

	public Relation(String relation, Annotation subject, Annotation modifier) {
		this.relation = relation;
		this.subject = subject;
		this.modifier = modifier;
	}

	public String getRelation() {
		return relation;
	}

	public Annotation getSubject() {
		return subject;
	}

	public Annotation getModifier() {
		return modifier;
	}

	public String toString() {
		String str = this.relation + "(\"" + "\"" + this.modifier.getText() + "\"="
				+ this.modifier.getClassificationValue() + ")";
		return str;
	}

}

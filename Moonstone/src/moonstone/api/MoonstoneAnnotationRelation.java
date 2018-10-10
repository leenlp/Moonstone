package moonstone.api;

public class MoonstoneAnnotationRelation {
	private MoonstoneAnnotation annotation = null;
	private String relation = null;
	private MoonstoneAnnotation subject = null;
	private MoonstoneAnnotation modifier = null;

	public MoonstoneAnnotationRelation(MoonstoneAnnotation annotation,
			String relation, MoonstoneAnnotation subject,
			MoonstoneAnnotation modifier) {
		this.annotation = annotation;
		this.relation = relation;
		this.subject = subject;
		this.modifier = modifier;
	}

	public String toString() {
		String str = this.relation + "(" + this.subject.getId() + ","
				+ this.modifier.getId() + ")";
		return str;
	}

	public MoonstoneAnnotation getAnnotation() {
		return annotation;
	}

	public String getRelation() {
		return relation;
	}

	public MoonstoneAnnotation getSubject() {
		return subject;
	}

	public MoonstoneAnnotation getModifier() {
		return modifier;
	}

}

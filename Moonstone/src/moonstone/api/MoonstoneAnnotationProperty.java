package moonstone.api;

public class MoonstoneAnnotationProperty {
	private MoonstoneAnnotation annotation = null;
	private String property = null;
	private Object value = null;

	public MoonstoneAnnotationProperty(MoonstoneAnnotation annotation,
			String property, Object value) {
		this.annotation = annotation;
		this.property = property;
		this.value = value;
	}

	public MoonstoneAnnotation getAnnotation() {
		return annotation;
	}

	public String getProperty() {
		return property;
	}

	public Object getValue() {
		return value;
	}
	
	public String toString() {
		String str = "<" + this.property + "=" + this.value + ">";
		return str;
	}
	

}

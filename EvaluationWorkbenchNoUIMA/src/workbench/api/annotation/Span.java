package workbench.api.annotation;

import java.util.Comparator;

public class Span {

	private Annotation annotation = null;
	private int start = -1;
	private int end = -1;
	
	public Span(Annotation annotation, int start, int end) {
		this.annotation = annotation;
		this.start = start;
		this.end = end;
	}
	
	public Span(Annotation annotation, annotation.Span aspan) {
		this.annotation = annotation;
		this.setStart(aspan.getTextStart());
		this.setEnd(aspan.getTextEnd());
	}
	

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public int getLength() {
		return this.end - this.start + 1;
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	
	public String toString() {
		String str = "<" + this.start + "-" + this.end + ">";
		return str;
	}
	
	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Span s1 = (Span) o1;
			Span s2 = (Span) o2;
			if (s1.getStart() < s2.getStart()) {
				return -1;
			}
			if (s2.getStart() < s1.getStart()) {
				return 1;
			}
			if (s1.getEnd() > s2.getEnd()) {
				return -1;
			}
			if (s2.getEnd() > s1.getEnd()) {
				return 1;
			}
			return 0;
		}
	}
}

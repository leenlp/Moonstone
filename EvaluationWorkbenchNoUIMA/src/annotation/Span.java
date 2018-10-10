package annotation;

import java.util.Comparator;

import tsl.utilities.SeqUtils;

public class Span extends EVAnnotation {
	EVAnnotation annotation = null;
	int textStart = 0;
	int textEnd = 0;
	String text = null;
	String id = null;
	
	public Span(int start, int end) {
		this(null, start, end);
	}
	
	public Span(EVAnnotation annotation, int start, int end) {
		this.annotation = annotation;
		this.textStart = start;
		this.textEnd = end;
		this.setId(null);
	}
	
	public int getTextStart() {
		return textStart;
	}

	public void setTextStart(int textStart) {
		this.textStart = textStart;
	}
	
	public int getTextEnd() {
		return textEnd;
	}
	
	public void setTextEnd(int textEnd) {
		this.textEnd = textEnd;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public int getLength() {
		return this.textEnd - this.textStart;
	}
	
	public String toString() {
		return "<" + this.textStart + "-" + this.textEnd + ">";
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		if (id == null && this.annotation != null) {
			int num = this.annotation.getAnnotationCollection().getNumberOfSpans();
			id = "span_" + num;
			this.annotation.getAnnotationCollection().setNumberOfSpans(num+1);
		}
		this.id = id;
	}
	
	public static int getOverlap(Span s1, Span s2) {
		int overlap = SeqUtils.amountOverlap(s1.getTextStart(),
				s1.getTextEnd(), s2.getTextStart(), 
				s2.getTextEnd());
		return overlap;
	}
	
	// 2/23/2013
	public boolean coversPosition(int position) {
		return this.textStart <= position && position <= this.textEnd;
	}
	
	public static class PositionSorter implements Comparator {
		
		public int compare(Object o1, Object o2) {
			Span s1 = (Span) o1;
			Span s2 = (Span) o2;
			if (s1.getTextStart() < s2.getTextStart()) {
				return -1;
			}
			if (s1.getTextStart() > s2.getTextStart()) {
				return 1;
			}
			return 0;
		}
	}

	
}

package edu.utah.blulab.nlp;

import moonstone.annotation.Annotation;

/**
 * Created by Bill on 10/11/2017.
 */
public class AnnotationNLP {
	
	private Annotation annotation = null;
	private String documentName = null;
	private int textStart = -1;
	private int textEnd = -1;
	private String concept = null;
	
	public AnnotationNLP(Annotation annotation) {
		this.documentName = annotation.getDocument().getName();
		this.textStart = annotation.getTextStart();
		this.textEnd = annotation.getTextEnd();
		if (annotation.hasConcept()) {
			this.concept = annotation.getConcept().toString();
		}
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public String getDocumentName() {
		return documentName;
	}

	public int getTextStart() {
		return textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}

	public String getConcept() {
		return concept;
	}
	
	
}


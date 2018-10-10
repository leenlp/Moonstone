package annotation;

import tsl.documentanalysis.document.Document;

/**
 * Document-level annotation.
 */
public class DocumentAnnotation extends EVAnnotation {

	public DocumentAnnotation() {

	}

	public DocumentAnnotation(typesystem.Annotation type, Document document)
			throws Exception {
		super(type, document);
	}

	/*
	 * Used in "toString()" methods of different annotation levels.
	 */
	public String getLevelPrefix() {
		return "D";
	}
}

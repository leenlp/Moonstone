package annotation;

import tsl.documentanalysis.document.Document;

public class SnippetAnnotation extends EVAnnotation {

	public SnippetAnnotation() {
		super();
	}

	public SnippetAnnotation(typesystem.Annotation type, Document document)
			throws Exception {
		super(type, document);
	}

	public String getLevelPrefix() {
		return "S";
	}

}

package annotation;

import tsl.documentanalysis.document.Document;

// TODO: Auto-generated Javadoc
/**
 * Word-level subclass of EVAnnotation.
 */
public class WordAnnotation extends EVAnnotation {

	/**
	 * Instantiates a new word annotation.
	 * 
	 * @param document
	 *            the document
	 */
	public WordAnnotation(typesystem.Annotation type, Document document)
			throws Exception {
		super(type, document);
	}
}

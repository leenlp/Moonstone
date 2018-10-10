package annotation;

import tsl.documentanalysis.document.Document;

// TODO: Auto-generated Javadoc
/**
 * Token-level subclass of EVAnnotation
 */
public class TokenAnnotation extends EVAnnotation {

	/**
	 * Instantiates a new token annotation.
	 * 
	 * @param document
	 *            the document
	 */
	public TokenAnnotation(typesystem.Annotation type, Document document)
			throws Exception {
		super(type, document);
	}
}

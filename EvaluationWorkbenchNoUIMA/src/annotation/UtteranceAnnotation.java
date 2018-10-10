package annotation;

import tsl.documentanalysis.document.Document;


// TODO: Auto-generated Javadoc
/**
 * Utterance-level subclass of EVAnnotation.
 */
public class UtteranceAnnotation extends EVAnnotation {

	/**
	 * Instantiates a new utterance annotation.
	 * 
	 * @param document
	 *            the document
	 */
	public UtteranceAnnotation(typesystem.Annotation type, Document document)
			throws Exception {
		super(type, document);
	}
}

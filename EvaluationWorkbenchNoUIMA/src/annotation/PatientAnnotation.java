package annotation;

import tsl.documentanalysis.document.Document;

// TODO: Auto-generated Javadoc
/**
 * Subclass for Patient level.
 */
public class PatientAnnotation extends EVAnnotation {
	
	/**
	 * Instantiates a new patient annotation.
	 *
	 * @param document the document
	 */
	public PatientAnnotation(typesystem.Annotation type, Document document) throws Exception {
		super(type, document);
	}

}

package annotation;

import tsl.documentanalysis.document.Document;

/**
 * The EVAnnotation subclass for Group level.
 */
public class GroupAnnotation extends EVAnnotation {
	
	/**
	 * Instantiates a new group annotation.
	 */
	public GroupAnnotation() {
		
	}
	
	/**
	 * Instantiates a new group annotation.
	 *
	 * @param document the document
	 */
	public GroupAnnotation(typesystem.Annotation type, Document document) throws Exception {
		super(type, document);
	}
	// SPM 12/21/2011 Add group level tagging 
	/* (non-Javadoc)
	 * @see annotation.EVAnnotation#getLevelPrefix()
	 */
	public String getLevelPrefix() {
		return "G";
	}
}

package workbench.arr;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tsl.utilities.HUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ARRDocument.
 */
public class ARRDocument {
	
	/** The analysis. */
	public AnnotationAnalysis analysis = null;
	
	/** The filename. */
	public String filename = null;
	
	/** The text. */
	public String text = null;
	
	/** The document map. */
	public static Map<String, ARRDocument> documentMap = new HashMap<String, ARRDocument>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[" + this.filename + "]";
	}
	
	/**
	 * Gets the document.
	 *
	 * @param filename the filename
	 * @return the document
	 */
	public static ARRDocument getDocument(String filename) {
		return documentMap.get(filename);
	}
	
	/**
	 * Gets the documents.
	 *
	 * @return the documents
	 */
	public static Vector getDocuments() {
		Vector v = HUtils.getElements(documentMap);
		Collections.sort(v, new NameSorter());
		return v;
	}
	
	/**
	 * Gets the document names.
	 *
	 * @return the document names
	 */
	public static Vector getDocumentNames() {
		Vector v = HUtils.getKeys(documentMap);
		Collections.sort(v);
		return v;
	}
	
	/**
	 * The Class NameSorter.
	 */
	public static class NameSorter implements Comparator {
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			ARRDocument d1 = (ARRDocument) o1;
			ARRDocument d2 = (ARRDocument) o2;
			return d1.filename.compareTo(d2.filename);
		}
	}

}

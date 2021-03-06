package workbench.api.annotation;

import java.util.Comparator;

import tsl.documentanalysis.document.Document;
import workbench.api.Analysis;

public class AnnotationEvent {

	private Analysis analysis = null;
	private AnnotationCollection primaryAnnotationCollection = null;
	private AnnotationCollection secondaryAnnotationCollection = null;
	private AnnotationCollection selectedAnnotationCollection = null;
	private Document document = null;
	private String documentName = null;
	private int documentID = -1;

	public AnnotationEvent(Analysis analysis, Document document, String documentName) {
		this.analysis = analysis;
		this.document = document;
		this.documentName = (document != null ? document.getName() : documentName);
	}

	// For using the old WB as a scaffold for building the new WB.
	public AnnotationEvent(Analysis analysis, workbench.arr.AnnotationEvent ae) {
		this.analysis = analysis;
		this.document = ae.getDocument();
		this.primaryAnnotationCollection = new AnnotationCollection(
				ae.getPrimaryAnnotationCollection(), this, true);
		this.secondaryAnnotationCollection = new AnnotationCollection(
				ae.getSecondaryAnnotationCollection(), this, false);
	}

	public AnnotationCollection getAnnotationCollection(Annotator annotator) {
		return annotator.isPrimary() ? this.primaryAnnotationCollection
				: this.secondaryAnnotationCollection;
	}

	public AnnotationCollection getPrimaryAnnotationCollection() {
		return primaryAnnotationCollection;
	}

	public void setPrimaryAnnotationCollection(
			AnnotationCollection primaryAnnotationCollection) {
		this.primaryAnnotationCollection = primaryAnnotationCollection;
		this.primaryAnnotationCollection.setAnnotationEvent(this);
	}

	public AnnotationCollection getSecondaryAnnotationCollection() {
		return secondaryAnnotationCollection;
	}

	public void setSecondaryAnnotationCollection(
			AnnotationCollection secondaryAnnotationCollection) {
		Annotator annotator = secondaryAnnotationCollection.getAnnotator();
		this.secondaryAnnotationCollection = secondaryAnnotationCollection;
		this.secondaryAnnotationCollection.setAnnotationEvent(this);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public String toString() {
		String str = "<AE: Document=" + this.getDocumentName() + ">";
		return str;
	}
	
	public String getDocumentName() {
		if (this.documentName == null && this.document != null) {
			this.documentName = this.document.getName();
		}
		return documentName;
	}

	public static class DocumentNameSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			AnnotationEvent ae1 = (AnnotationEvent) o1;
			AnnotationEvent ae2 = (AnnotationEvent) o2;
			return ae1.documentName.compareTo(ae2.documentName);
		}
	}

	public int getDocumentID() {
		return documentID;
	}

	public void setDocumentID(int documentID) {
		this.documentID = documentID;
	}
	
	

}

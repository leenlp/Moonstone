package tsl.dbaccess.mysql;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.DocumentItemConstant;
import tsl.documentanalysis.document.Sentence;
import tsl.utilities.MathUtils;
import tsl.utilities.VUtils;

public class DocumentItemInfoWrapper {

	private DocumentItemConstant documentItem = null;
	private double score = 0d;
	private double[] sweights = null;
	private Vector<String> matchingTerms = null;

	public DocumentItemInfoWrapper(DocumentItemConstant documentItem,
			double[] sweights, double[] tweights, Vector<String> queryTerms) {
		this.documentItem = documentItem;
		this.sweights = sweights;
		for (int i = 0; i < sweights.length; i++) {
			if (sweights[i] > 0) {
				matchingTerms = VUtils.add(matchingTerms,
						queryTerms.elementAt(i));
			}
		}
		this.score = MathUtils.cosineSimilarity(sweights, tweights);
	}

	public DocumentItemConstant getDocumentItem() {
		return this.documentItem;
	}

	public void setDocumentItem(DocumentItemConstant documentItem) {
		this.documentItem = documentItem;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getText() {
		return null;
	}

	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		String str = "<\"" + this.getText() + "\",Score="
				+ df.format(this.score) + ",MatchingTerms="
				+ this.matchingTerms + ",Weights=" + this.getSweights() + ">";
		return str;
	}

	public static Vector<DocumentItemConstant> getDocumentItems(
			Vector<DocumentItemInfoWrapper> wrappers) {
		Vector<DocumentItemConstant> dis = null;
		if (wrappers != null) {
			for (DocumentItemInfoWrapper wrapper : wrappers) {
				dis = VUtils.add(dis,
						(DocumentItemConstant) wrapper.documentItem);
			}
		}
		return dis;
	}

	public static Vector<Sentence> getSentences(
			Vector<DocumentItemInfoWrapper> wrappers) {
		Vector<Sentence> sentences = null;
		if (wrappers != null
				&& wrappers.firstElement().documentItem instanceof Sentence) {
			for (DocumentItemInfoWrapper wrapper : wrappers) {
				sentences = VUtils.add(sentences,
						(Sentence) wrapper.documentItem);
			}
		}
		return sentences;
	}

	public static Vector<Document> getDocuments(
			Vector<DocumentItemInfoWrapper> wrappers) {
		Vector<Document> documents = null;
		if (wrappers != null
				&& wrappers.firstElement().documentItem instanceof Document) {
			for (DocumentItemInfoWrapper wrapper : wrappers) {
				documents = VUtils.add(documents,
						(Document) wrapper.documentItem);
			}
		}
		return documents;
	}

	public static class ScoreSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			DocumentItemInfoWrapper s1 = (DocumentItemInfoWrapper) o1;
			DocumentItemInfoWrapper s2 = (DocumentItemInfoWrapper) o2;
			if (s1.score > s2.score) {
				return -1;
			}
			if (s1.score < s2.score) {
				return 1;
			}
			if (s1.documentItem.getTextLength() < s2.documentItem
					.getTextLength()) {
				return -1;
			}
			return 1;
		}
	}

	public String getSweights() {
		String str = "[";
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		for (int i = 0; i < this.sweights.length; i++) {
			str += df.format(this.sweights[i]);
			if (i < this.sweights.length - 1) {
				str += ",";
			}
		}
		str += "]";
		return str;
	}

}

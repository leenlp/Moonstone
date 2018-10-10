package moonstone.learning.feature;

import tsl.documentanalysis.document.Document;
import tsl.utilities.VUtils;
import moonstone.annotation.Annotation;
import moonstone.io.readmission.ReadmissionPatientResults;
import moonstone.javafunction.JavaFunctions;

public class ARFFPatientVector {

	private ReadmissionPatientResults results = null;
	private String pname = null;
	private int[] featureCounts = null;
	private int featureArraySize = 0;

	public static int NumDiscretizedDays = ARFFPatientVectorVariable.DiscretizedDayStrings.length;
	public static int NumPolarities = ARFFPatientVectorVariable.PolarityStrings.length;
	public static int NumDocumentTypes = ARFFPatientVectorVariable.DocumentTypeStrings.length;

	public ARFFPatientVector(ReadmissionPatientResults results, String pname) {
		this.results = results;
		this.pname = pname;
		int fcount = results.processor.featureSet.featureDefinitionVector
				.getNumberOfFeatures();
		this.featureArraySize = fcount * NumDiscretizedDays * NumDocumentTypes * NumPolarities;
		this.featureCounts = new int[this.featureArraySize];
		this.results.processor.ARFFPatientVectorHash.put(pname, this);
	}

	public void addFeature(Annotation annotation, String content) {
		// Before 5/15/2017
		FeatureDefinitionVector fdv = this.results.processor.featureSet.featureDefinitionVector;
		Feature feature = fdv.getFeature(content);
		if (feature == null) {
			System.out.println("\n\tARFFPatientVector:  Not storing content "
					+ content);
			return;
		}
		int dayIndex = getDayOffset(annotation);
		int polarityIndex = getPolarityOffset(annotation);
		int typeIndex = getDocumentTypeOffset(annotation);
		int fcount = fdv.getNumberOfFeatures();
		int findex = feature.getIndex();
		int stepsize = NumDiscretizedDays * NumDocumentTypes * NumPolarities;
		int baseFeatureOffset = stepsize * findex;
		// inset = ((NumDayIndices * NumTypeIndices) * dayIndex) + (NumTypeIndices * polarIndex) + polarIndex;
		int inset = (4 * dayIndex) + (2 * typeIndex) + polarityIndex;
		int featureIndex = baseFeatureOffset + inset;
		if (featureIndex <= this.featureArraySize) {
			this.featureCounts[featureIndex]++;
		} else {
			System.out.println("\n\tARFFPatientVector:  Not storing content "
					+ content);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.featureArraySize; i++) {
			int fcount = this.featureCounts[i];
			String fcountstr = String.valueOf(fcount);
			sb.append(fcountstr);
			if (i < this.featureArraySize - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static int getDocumentTypeOffset(Annotation annotation) {
		int dtype = 1;
		Document d = annotation.getDocument();
		String lcf = d.getName().toLowerCase();
		if (lcf.contains("social") || lcf.contains("sws")
				|| lcf.contains("mental") || lcf.contains("psych")) {
			dtype = 0;
		}
		return dtype;
	}

	public static int getPolarityOffset(Annotation annotation) {
		int polarity = JavaFunctions.isAffirmed(annotation) ? 0 : 1;
		return polarity;
	}

	public static int getDayOffset(Annotation annotation) {
		int val = 5;
		Document d = annotation.getDocument();
		if (d != null) {
			int diff = d.getAdmitDictationDayDifference();
			if (diff < 0) {
				val = 0;
			} else if (diff < 7) {
				val = 1;
			} else if (diff < 30) {
				val = 2;
			} else if (diff < 90) {
				val = 3;
			} else if (diff < 180) {
				val = 4;
			} else {
				val = 5;
			}
		}
		return val;
	}

}

package moonstone.learning.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.io.readmission.Readmission;
import moonstone.javafunction.JavaFunctions;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;

public class FeaturePatientVector {

	protected FeatureSet featureSet = null;
	protected String patientName = null;
	protected int[] featureCounts = null;

	public FeaturePatientVector(FeatureSet fs, String pname) {
		this.featureSet = fs;
		this.patientName = pname;
		this.featureCounts = new int[fs.featureDefinitionVector.getNumberOfFeatures()];
		fs.featurePatientVectorHash.put(pname, this);
	}

	public void writePatientVectorFile(boolean isTraining) {
		String fname = (isTraining ? this.featureSet.patientTrainingVectorDirectoryName
				: this.featureSet.patientTestVectorDirectoryName);
		String fpath = fname + File.separatorChar + this.patientName;
		FUtils.deleteFileIfExists(fpath);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.getNumberOfFeatures(); i++) {
			sb.append(i + " " + this.featureCounts[i] + "\n");
		}
		FUtils.writeFile(fpath, sb.toString());
	}

	public void extractFeaturesFromReport(String tsetname, Document doc) {
		System.out.println("Processing " + tsetname + ":" + doc.getName() + "...");
		doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
		Vector<Annotation> annotations = this.featureSet.moonstoneRuleInterface.getControl()
				.applyNarrativeGrammarToText(doc, true, true, true);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<String> allcontent = FeatureSet.getRelevantPropositionalContent(annotation);
				if (allcontent != null) {
					for (String content : allcontent) {
						this.incrementCount(content);
					}
				}
			}
		}
		this.featureSet.moonstoneRuleInterface.releaseAnnotations();
	}

	public void readPatientFeatureVectorFile(File file, String pname) {
		try {
			if (file.exists() && file.isFile()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = null;
				int numlines = 0;
				while ((line = in.readLine()) != null) {
					String[] strs = line.split(" ");
					if (strs.length >= 2 && Character.isDigit(strs[1].charAt(0))) {
						numlines++;
						int index = Integer.parseInt(strs[0]);
						int count = Integer.parseInt(strs[1]);
						this.featureCounts[index] = count;
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getCount(int index) {
		if (index >= 0 && index < this.getNumberOfFeatures()) {
			return this.featureCounts[index];
		}
		return -1;
	}

	public void incrementCount(String content) {
		Feature f = this.featureSet.featureDefinitionVector.getFeature(content);
		if (f != null && f.index >= 0) {
			this.featureCounts[f.index]++;
		} else {
			System.out.println("FeaturePatientVector:  Feature not found: " + content);
		}
	}

	public int incrementCount(int index) {
		if (index >= 0 && index < this.getNumberOfFeatures()) {
			return this.featureCounts[++index];
		}
		return -1;
	}

	public int getNumberOfFeatures() {
		return this.featureSet.featureDefinitionVector.getNumberOfFeatures();
	}

}

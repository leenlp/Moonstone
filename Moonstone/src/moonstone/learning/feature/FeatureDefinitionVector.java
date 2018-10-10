package moonstone.learning.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.io.readmission.ReadmissionSnippetResult;
import moonstone.javafunction.JavaFunctions;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class FeatureDefinitionVector {
	protected FeatureSet featureSet = null;
	protected Vector<Feature> featureArray = new Vector(1000000);
	protected Hashtable<Object, Feature> featureHash = new Hashtable();

	// 12/6/2016 WEKA

	protected Hashtable<String, Hashtable<String, Integer>> patientConceptCountHash = new Hashtable();

	public FeatureDefinitionVector(FeatureSet fs) {
		this.featureSet = fs;
		this.readDefinitionFile();
	}

	public Feature getFeature(Object content) {
		return this.featureHash.get(content);
	}

	public Feature getFeature(int index) {
		if (index >= 0 && index < this.getNumberOfFeatures()) {
			return this.featureArray.elementAt(index);
		}
		return null;
	}

	public int[] getCountArray() {
		int[] array = new int[this.getNumberOfFeatures()];
		for (int i = 0; i < this.getNumberOfFeatures(); i++) {
			array[i] = this.featureArray.size();
		}
		return array;
	}

	public void incrementFeatureCount(Object content) {
		Feature f = this.getFeature(content);
		if (f != null) {
			f.incrementCount();
		}
	}

	public Feature addFeature(String content) {
		Feature pf = (Feature) this.featureHash.get(content);
		if (pf != null) {
			pf.incrementCount();
		} else {
			int index = this.featureArray.size();
			pf = new Feature(index, content, 1);
			this.featureArray.add(pf);
			this.featureHash.put(content, pf);
		}
		return pf;
	}

	public void extractFeatureDefinitions(String tsetname, Document doc) {
		System.out.println("Processing " + tsetname + ":" + doc.getName() + "...");
		MoonstoneRuleInterface msri = this.featureSet.moonstoneRuleInterface;
		Vector<Annotation> annotations = msri.getControl().applyNarrativeGrammarToText(doc, false, false,
				false);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Document d = annotation.getDocument();
				Vector<String> allcontent = FeatureSet.getRelevantPropositionalContent(annotation);
				if (allcontent != null) {
					for (String content : allcontent) {
						this.addFeature(content);
					}
				}
			}
		}
		msri.releaseAnnotations();
	}

	public void readDefinitionFile() {
		String fname = this.featureSet.featureDefinitionFileName;
		File file = new File(fname);
		try {
			if (file.exists() && file.isFile()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = null;
				int numlines = 0;
				while ((line = in.readLine()) != null) {
					String[] strs = line.split(" ");
					if (strs.length >= 2 && Character.isDigit(strs[0].charAt(0))) {
						numlines++;
						int index = Integer.parseInt(strs[0].trim());
						String content = strs[1].trim();
						Feature pf = new Feature(index, content, 0);
						this.featureArray.add(pf);
						this.featureHash.put(content, pf);
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeFeatureDefinitionFile() {
		int x = 1;
		// String fname = this.getDefinitionFileName();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.getNumberOfFeatures(); i++) {
			Feature f = this.getFeature(i);
			sb.append(f.getIndex() + " " + f.getContent() + "\n");
		}
		FUtils.writeFile(this.featureSet.featureDefinitionFileName, sb.toString());
	}

	public int getNumberOfFeatures() {
		if (this.featureArray != null) {
			return this.featureArray.size();
		}
		return 0;
	}

	// 10/10/2016 Not used...
	public Vector<Feature> getOrderedFeatures() {
		Vector<Feature> v = (Vector<Feature>) HUtils.getElements(this.featureHash);
		Collections.sort(v, new Feature.FeatureIndexSorter());
		this.featureArray = v;
		return v;
	}

}

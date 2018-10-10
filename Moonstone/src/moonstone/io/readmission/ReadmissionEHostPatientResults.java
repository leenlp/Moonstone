package moonstone.io.readmission;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.knowledge.engine.StartupParameters;
import tsl.utilities.FUtils;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;

public class ReadmissionEHostPatientResults {

	MoonstoneRuleInterface moonstone = null;
	ReadmissionPatientResults patientResults = null;
	KnowtatorIO kio = null;
	Hashtable<String, Boolean> patientSummaryHash = new Hashtable();
	Hashtable<String, String> patientEHostClassificationHash = new Hashtable();
	Hashtable<String, String> patientVariableClassificationHash = new Hashtable();

	public static String EHostPatientResultsDirectory = "EHostPatientResultsDirectory";

	public ReadmissionEHostPatientResults(ReadmissionPatientResults rpr) {
		try {
			this.patientResults = rpr;
			this.moonstone = rpr.moonstone;
			System.out.println("ReadmissionEHostPatientResults: Reading files...");
			this.readAnnotationFiles();
			if (this.kio != null && kio.getAnnotations() != null) {
				this.kio.createWorkbenchAnnotations(kio.getAnalysis());
				for (AnnotationCollection ac : this.kio
						.getAnnotationCollections()) {
					for (Annotation annotation : ac.getAnnotations()) {
						String classification = (String) annotation
								.getClassificationValue();
						String docname = annotation.getAnnotationCollection()
								.getSourceTextName();
						String pname = getPatientNameFromFileName(docname);
						if (classification != null) {
							
							// 5/11/2017
							if ("lives in a group setting".equals(classification)) {
								classification = "does not live alone";
							}
							
							classification = rpr
									.convertOldToNewEHostValue(classification);
							String key = pname + ":" + classification;
							this.patientSummaryHash.put(key, new Boolean(true));
			
							String variable = this.moonstone.getReadmission()
									.getAttributeValueType(classification);
							if (variable == null) {
								variable = annotation.getName();
							}
							key = pname + ":" + variable;
							this.patientVariableClassificationHash.put(key,
									classification);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean patientHasClassification(String pname, String classification) {
		String key = pname + ":" + classification;
		Boolean istrue = this.patientSummaryHash.get(key);
		if (istrue == null) {
			key = pname + "@@" + classification;
			istrue = this.patientSummaryHash.get(key);
			if (istrue != null) {
				int x = 1;
			}
		}
		return istrue != null;
	}

	public String getPatientClassification(String pname, String variable) {
		String key = pname + ":" + variable;
		String classification = this.patientVariableClassificationHash.get(key);
		return classification;
	}

	public void readAnnotationFiles() {
		try {
			if (this.patientResults != null
					&& this.patientResults.moonstone.getWorkbench() != null) {
				StartupParameters sp = this.patientResults.moonstone.getStartupParameters();
				String dname = sp
						.getPropertyValue(EHostPatientResultsDirectory);
				// 4/13/2017: Added name substring so I can use large containing
				// directories
				
				String reportConstraint = sp.getPropertyValue("ReadmissionEHostPathnameAnnotationConstraintString");
				if (reportConstraint == null) {
					reportConstraint = sp.getPropertyValue("AnnotationPathnameConstraintString");
				}
				Vector<File> files = FUtils.readFilesFromDirectory(dname,
						reportConstraint);
				if (files != null) {
					this.kio = new KnowtatorIO();
					Analysis oldanalysis = this.patientResults.moonstone
							.getWorkbench().getAnalysis();
					Analysis newanalysis = new Analysis(
							oldanalysis.getTypeSystem());
					kio.setAnalysis(newanalysis);
					kio.setTypeSystem(newanalysis.getTypeSystem());
					for (File file : files) {
						String fname = file.getName().toLowerCase();
						if (fname.contains("knowtator")) {
							String pname = getPatientNameFromFileName(file
									.getName());
							String fstr = FUtils.readFile(file);
							Knowtator.readAnnotationFile(kio, null, fstr, true);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getPatientNameFromFileName(String fname) {
		int index = fname.indexOf("_");
		String pname = null;
		if (index > 0) {
			pname = "";
			for (int i = index + 1; i < fname.length(); i++) {
				char c = fname.charAt(i);
				if (Character.isDigit(c)) {
					pname += c;
				} else {
					break;
				}
			}
		}
		return pname;
	}
	
	

}

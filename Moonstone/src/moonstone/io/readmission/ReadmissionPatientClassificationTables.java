package moonstone.io.readmission;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class ReadmissionPatientClassificationTables {

	private MoonstoneRuleInterface moonstone = null;
	private Hashtable<String, ReadmissionPatientClassificationTable> tableHash = new Hashtable();
	public static String ReadmissionPatientTableDirectory = "ReadmissionPatientTableDirectory";
	public static String AnnotationSetDescription = "AnnotationSetDescription";
	public static String Separator = ":";
	public static String PrintSeparator = "|";

	public ReadmissionPatientClassificationTables(MoonstoneRuleInterface msri) {
		this.moonstone = msri;
		// this.readTableFiles();
	}

	public void printClassificationStatistics() {
		StringBuffer sb = new StringBuffer();
		this.printClassificationStatistics("Phase2_Tiffany",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printClassificationStatistics("Phase2_Kristi",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		
		this.printVariableStatistics("Phase2_Kristi",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printClassificationStatistics("Phase2_Holly",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		
		
		
		this.printClassificationStatistics("Phase2_Tiffany",
				"Phase2_Holly", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany",
				"Phase2_Holly", sb);
		sb.append("\n\n");
		
		this.printClassificationStatistics("Phase2_Tiffany",
				"Phase2_Kristi", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany",
				"Phase2_Kristi", sb);
		sb.append("\n\n");
		
		this.printClassificationStatistics("Phase2_Holly",
				"Phase2_Tiffany", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly",
				"Phase2_Tiffany", sb);
		sb.append("\n\n");
		
		this.printClassificationStatistics("Phase2_Holly",
				"Phase2_Kristi", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly",
				"Phase2_Kristi", sb);
		sb.append("\n\n");
		
		this.printClassificationStatistics("Phase2_Kristi",
				"Phase2_Tiffany", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Kristi",
				"Phase2_Tiffany", sb);
		sb.append("\n\n");
		
		this.printClassificationStatistics("Phase2_Kristi",
				"Phase2_Holly", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Kristi",
				"Phase2_Holly", sb);
		sb.append("\n\n");
		
		
		String fname = this.moonstone.getResourceDirectoryName() + File.separatorChar +
				"ReadmissionClassificationStatistics.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printClassificationStatistics(String a1, String a2,
			StringBuffer sb) {
		ReadmissionPatientClassificationTable t1 = this.getTable(a1);
		ReadmissionPatientClassificationTable t2 = this.getTable(a2);
		Hashtable<String, Integer> chash = new Hashtable();
		String line = "\n\n" + a1 + " vs " + a2 + "\n";
		line += "Variable|Concept|TP|FP|TN|FN|F-Measure\n";
		sb.append(line);
		boolean withMoonstone = a2.toLowerCase().contains("moonstone");
		this.getMatchCounts(t1, t2, chash, withMoonstone);
		for (String vname : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			Vector<String> classifications = this.moonstone.getReadmission()
					.getTypeAttributeValues(vname);
			for (String c : classifications) {
				String key = vname + Separator + c + Separator + "TP";
				float tp = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "FP";
				float fp = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "TN";
				float tn = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "FN";
				float fn = HUtils.getCount(chash, key);
				float num = 0;
				float den = 0;
				float accuracy = 0;
				float sensitivity = 0;
				float specificity = 0;
				num = tp + tn;
				den = tp + fp + tn + fn;
				accuracy = num / den;
				num = tp;
				den = tp + fn;
				if (den > 0) {
					sensitivity = num / den;
				}
				num = tn;
				den = tn + fp;
				if (den > 0) {
					specificity = num / den;
				}
				float fmeasure = getFmeasure(tp, tn, fp, fn);
				float kappa = getCohensKappa(tp, tn, fp, fn);
				line = vname + PrintSeparator + c + PrintSeparator + (int) tp
						+ PrintSeparator + (int) fp + PrintSeparator + (int) tn
						+ PrintSeparator + (int) fn + PrintSeparator + fmeasure
						+ "\n";
				sb.append(line);
			}
		}
	}

	public void printVariableStatistics(String a1, String a2, StringBuffer sb) {
		ReadmissionPatientClassificationTable t1 = this.getTable(a1);
		ReadmissionPatientClassificationTable t2 = this.getTable(a2);
		Hashtable<String, Integer> chash = new Hashtable();
		String line = a1 + " vs " + a2 + "\n";
		line += "Variable|TP|FP|TN|FN|F-Measure\n";
		sb.append(line);
		boolean withMoonstone = a2.toLowerCase().contains("moonstone");
		this.getMatchCounts(t1, t2, chash, withMoonstone);
		for (String vname : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			String key = vname + Separator + "TP";
			float tp = HUtils.getCount(chash, key);
			key = vname + Separator + "FP";
			float fp = HUtils.getCount(chash, key);
			key = vname + Separator + "TN";
			float tn = HUtils.getCount(chash, key);
			key = vname + Separator + "FN";
			float fn = HUtils.getCount(chash, key);

			float num = 0;
			float den = 0;
			float accuracy = 0;
			float sensitivity = 0;
			float specificity = 0;
			num = tp + tn;
			den = tp + fp + tn + fn;
			accuracy = num / den;
			num = tp;
			den = tp + fn;
			if (den > 0) {
				sensitivity = num / den;
			}
			num = tn;
			den = tn + fp;
			if (den > 0) {
				specificity = num / den;
			}
			float fmeasure = getFmeasure(tp, tn, fp, fn);
			float kappa = getCohensKappa(tp, tn, fp, fn);
			line = vname + PrintSeparator + (int) tp + PrintSeparator
					+ (int) fp + PrintSeparator + (int) tn + PrintSeparator
					+ (int) fn + PrintSeparator + fmeasure + "\n";
			sb.append(line);
		}
	}

	public void getMatchCounts(ReadmissionPatientClassificationTable t1,
			ReadmissionPatientClassificationTable t2,
			Hashtable<String, Integer> chash, boolean withMoonstone) {
		Vector<String> patients = t1.getPatients();
		Vector<String> variables = t1.getVariables();
		if (patients != null && variables != null) {
			for (String pname : patients) {
				for (String vname : variables) {
					String cstr = null;
					String c1 = t1.getClassification(pname, vname);
					String c2 = t2.getClassification(pname, vname);
					
					if (c1 == null || c2 == null) {
						continue;
					}
					boolean isNoMention = c1.toLowerCase().contains("mention");
					// Note: I didn't train on "no mention"; therefore
					// it artificially lowers results to include these
					// cases...
					if (withMoonstone && isNoMention) {
						continue;
					}
					
					if (isNoMention) {
						int x = 1;
					}

					if (c1.equals(c2)) {
						String key = vname + Separator + "TP";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + c1 + Separator + "TP";
						HUtils.incrementCount(chash, key);
					} else {
						String key = vname + Separator + "FN";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + "FP";
						HUtils.incrementCount(chash, key);

						key = vname + Separator + c1 + Separator + "FN";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + c2 + Separator + "FP";
						HUtils.incrementCount(chash, key);

					}
				}
			}
		}
	}

	// Before 4/27/2017
	// public void getMatchCounts(ReadmissionPatientClassificationTable t1,
	// ReadmissionPatientClassificationTable t2,
	// Hashtable<String, Integer> chash) {
	// Vector<String> patients = t1.getPatients();
	// Vector<String> variables = t1.getVariables();
	// if (patients != null && variables != null) {
	// for (String pname : patients) {
	// for (String vname : variables) {
	// String cstr = null;
	// String c1 = t1.getClassification(pname, vname);
	// String c2 = t2.getClassification(pname, vname);
	// if (c1 != null) {
	// if (c1.equals(c2)) {
	// cstr = "TP";
	// } else if (c2 != null) {
	// cstr = "FP";
	// } else {
	// cstr = "FN";
	// }
	// } else if (c2 != null) {
	// cstr = "FP";
	// } else {
	// cstr = "TN";
	// }
	// if (cstr != null) {
	// String key = vname + Separator + cstr;
	// HUtils.incrementCount(chash, key);
	// }
	// }
	// }
	// }
	// }

	public void addTableFromEHost() {
		String aname = this.moonstone.getStartupParameters().getPropertyValue(
				AnnotationSetDescription);
		if (aname != null) {
			ReadmissionCorpusProcessor rcp = new ReadmissionCorpusProcessor(
					this.moonstone, false);
			ReadmissionPatientResults rpr = new ReadmissionPatientResults(rcp,
					this.moonstone);
			ReadmissionEHostPatientResults repr = new ReadmissionEHostPatientResults(
					rpr);
			this.addTableFromEHost(aname, repr);
			this.writeTableFiles();
		}
	}

	public void addTableFromEHost(String aname,
			ReadmissionEHostPatientResults repr) {
		ReadmissionPatientClassificationTable t = this.getTable(aname);
		t.extractTableObjectsFromEHost(repr);
	}

	public void addTableFromFile(File f) {
		String aname = f.getName();
		ReadmissionPatientClassificationTable t = this.getTable(aname);
		t.readTableFile(f);
	}

	public void addTablesFromWEKA() {
		ReadmissionCorpusProcessor rpc = new ReadmissionCorpusProcessor(
				this.moonstone, false);
		String aname = "moonstone";
		ReadmissionPatientClassificationTable t = this.getTable(aname, true);
		Hashtable<String, Integer> pvhash = new Hashtable();
		Hashtable<Integer, String> phash = new Hashtable();
		Hashtable<String, String> pchash = new Hashtable();
		for (String variable : ReadmissionCorpusProcessor.SalomehVariables) {
			rpc.generateSalomehFormatResultsViaWEKA(variable, pvhash, phash,
					pchash);
		}
		for (Enumeration<String> e = pchash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			String[] strs = key.split(":");
			String vname = strs[0];
			String pname = strs[1];
			String cname = pchash.get(key);
			if (cname != null) {
				t.addTableObject(pname, vname, cname);
			}
		}
		this.writeTableFiles();
	}

	public void readTableFiles() {
		try {
			String dname = this.moonstone.getStartupParameters()
					.getPropertyValue(ReadmissionPatientTableDirectory);
			String dpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + dname;
			Vector<File> files = FUtils.readFilesFromDirectory(dpath);
			if (files != null) {
				for (File f : files) {
					this.addTableFromFile(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTableFiles() {
		String dname = this.moonstone.getStartupParameters().getPropertyValue(
				ReadmissionPatientTableDirectory);
		dname = ReadmissionPatientTableDirectory; // temp

		if (dname != null) {
			int x = 1;
			String dpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + dname;
			for (ReadmissionPatientClassificationTable t : this.tableHash
					.values()) {
				t.sortTableObjects();
				String aname = t.getAnnotator();
				String fpath = dpath + File.separatorChar + aname;
				FUtils.writeFile(fpath, t.toString());
			}
		}
	}

	public ReadmissionPatientClassificationTable getTable(String aname) {
		return this.getTable(aname, true);
	}

	public ReadmissionPatientClassificationTable getTable(String aname,
			boolean create) {
		ReadmissionPatientClassificationTable t = this.tableHash.get(aname);
		if (t == null && create) {
			t = new ReadmissionPatientClassificationTable(this, aname);
			this.tableHash.put(aname, t);
		}
		return t;
	}

	public static float getFmeasure(float tp, float tn, float fp, float fn) {
		float Bwt = 1;
		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);
		if (den > 0) {
			return num / den;
		}
		return 0;
	}

	public static float getCohensKappa(float tp, float tn, float fp, float fn) {
		float totalobs = tp + tn + fp + fn;
		float marg4p2_pos = (tp + fp);
		float marg4p1_pos = (tp + fn);
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);
		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			E_tp = (marg4p1_pos / totalobs) * (marg4p2_pos / totalobs);
			E_tn = (marg4p1_neg / totalobs) * (marg4p2_neg / totalobs);
			Ao = (tp + tn) / totalobs;
			Ae = (E_tp + E_tn);
		}
		float num = Ao - Ae;
		float den = 1 - Ae;
		if (den > 0) {
			return num / den;
		}
		return 0;
	}

}

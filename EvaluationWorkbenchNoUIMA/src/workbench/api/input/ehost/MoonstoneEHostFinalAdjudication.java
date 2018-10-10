package workbench.api.input.ehost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import workbench.api.constraint.ConstraintMatch;

public class MoonstoneEHostFinalAdjudication {

	private static String dirname = "/Users/leechristensen/Desktop/READMISSION";
	private static String VisibleMismatchFileName = "MentionSpreadsheetMatchHiddenMatchVisible_2_20_2018 TAB.txt";
	private static String InvisibleMismatchFileName = "Clean MentionSpreadsheetMatchHidden 2_20_2018 TAB.txt";
	private static String MoonstoneErrorFileName = "MoonstoneMatchErrorFile";
	private static String UnadjudicatedMatchFileName = "UnadjudicatedMatchFile.txt";

	public static void main(String[] args) {
		getMoonstoneEHostFinalAdjudicationCounts();
	}

	public static void getMoonstoneEHostFinalAdjudicationCounts() {
		Hashtable<String, Integer> MoonstoneCorrectHash = new Hashtable();
		Hashtable<String, Integer> ClassFalsePositiveHash = new Hashtable();
		Hashtable<String, Integer> ClassFalseNegativeHash = new Hashtable();
		Hashtable<String, Integer> ClassCorrectedFalsePositiveHash = new Hashtable();
		Hashtable<String, Integer> ClassCorrectedFalseNegativeHash = new Hashtable();
		Hashtable<String, Integer> UnadjustedClassCountHash = new Hashtable();

		String vfs = dirname + "/" + VisibleMismatchFileName;
		String ivfs = dirname + "/" + InvisibleMismatchFileName;
		String matchfilename = dirname + "/" + MoonstoneErrorFileName;
		String unadjudicatedMatchFilename = dirname + "/" + UnadjudicatedMatchFileName;

		File umf = new File(unadjudicatedMatchFilename);

		if (umf.exists()) {
			try {
				BufferedReader umin = new BufferedReader(new FileReader(umf));
				String line = null;
				while ((line = umin.readLine()) != null) {
					if (line.length() < 4) {
						continue;
					}
					String[] strs = line.split(",");
					if (strs.length != 5) {
						int x = 1;
					}
					String cname = strs[0];
					int tpcount = Integer.valueOf(strs[1]);
					int fpcount = Integer.valueOf(strs[2]);
					int tncount = Integer.valueOf(strs[3]);
					int fncount = Integer.valueOf(strs[4]);
					UnadjustedClassCountHash.put(cname + ":TP", new Integer(tpcount));
					UnadjustedClassCountHash.put(cname + ":FP", new Integer(fpcount));
					UnadjustedClassCountHash.put(cname + ":TN", new Integer(tncount));
					UnadjustedClassCountHash.put(cname + ":FN", new Integer(fncount));
				}
				umin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File vf = new File(vfs);
		File ivf = new File(ivfs);

		Hashtable<String, Integer> AllAnnotationHash = new Hashtable();
		StringBuffer MoonstoneErrorSB = new StringBuffer();
		Hashtable<String, String> MoonstoneMentionErrorHash = new Hashtable();
		try {
			if (vf.exists() && ivf.exists()) {
				BufferedReader vin = new BufferedReader(new FileReader(vf));
				BufferedReader ivin = new BufferedReader(new FileReader(ivf));
				String vline = null;
				String ivline = null;
				while (true) {
					vline = vin.readLine();
					ivline = ivin.readLine();
					if (vline == null || ivline == null) {
						break;
					}

					String[] strs1 = vline.split("\t");
					String[] strs2 = ivline.split("\t");
					if ("CLASSIFICATION".equals(strs1[0])) {
						continue;
					}
					String classification = strs1[0];
					String matchtype = strs1[1];
					String mention = strs2[3];
					String context = strs2[4];
					int adjudication = Integer.valueOf(strs2[1]);
					int MoonstoneCorrect = 0;
					boolean isFP = false;
					if ("FP".equals(matchtype)) {
						isFP = true;
						MoonstoneCorrect = adjudication;
						HUtils.incrementCount(ClassFalsePositiveHash, classification);
					} else {
						MoonstoneCorrect = (adjudication == 1 ? 0 : 1);
						HUtils.incrementCount(ClassFalseNegativeHash, classification);
					}
					HUtils.incrementCount(AllAnnotationHash, classification);
					if (MoonstoneCorrect == 1) {
						HUtils.incrementCount(MoonstoneCorrectHash, classification);
						if (isFP) {
							HUtils.incrementCount(ClassCorrectedFalsePositiveHash, classification);
						} else {
							HUtils.incrementCount(ClassCorrectedFalseNegativeHash, classification);
						}
					} else {
						int existing = HUtils.getCount(MoonstoneCorrectHash, classification);
						if (existing == 0) {
							HUtils.setCount(MoonstoneCorrectHash, classification, 0);
						}
						if (MoonstoneMentionErrorHash.get(mention) == null) {
							MoonstoneMentionErrorHash.put(mention, mention);
							String line = classification + " (" + matchtype + ") \" MENTION=" + mention
									+ "\" CONTEXT= \"" + context;
							MoonstoneErrorSB.append(line + "\n\n");
						}
					}
				}
				vin.close();
				ivin.close();
				float allTotals = 0;
				float allMoonstoneValidations = 0;
				int allCorrectedFPCount = 0;
				int allCorrectedFNCount = 0;
				for (Object key : HUtils.getKeys(MoonstoneCorrectHash)) {
					String classification = (String) key;
					int fpcount = HUtils.getCount(ClassFalsePositiveHash, classification);
					int fncount = HUtils.getCount(ClassFalseNegativeHash, classification);

					int cfpcount = HUtils.getCount(ClassCorrectedFalsePositiveHash, classification);
					int cfncount = HUtils.getCount(ClassCorrectedFalseNegativeHash, classification);

					allCorrectedFPCount += cfpcount;
					allCorrectedFNCount += cfncount;

					int value = HUtils.getCount(MoonstoneCorrectHash, key);
					allMoonstoneValidations += value;
					int total = HUtils.getCount(AllAnnotationHash, key);
					allTotals += total;
					float improvement = (float) value / (float) total;

					int unadjustedTP = UnadjustedClassCountHash.get(classification + ":TP");
					int unadjustedFP = UnadjustedClassCountHash.get(classification + ":FP");
					int unadjustedTN = UnadjustedClassCountHash.get(classification + ":TN");
					int unadjustedFN = UnadjustedClassCountHash.get(classification + ":FN");

					int adjustedTP = unadjustedTP + cfpcount;
					int adjustedFP = unadjustedFP - cfpcount;
					int adjustedTN = unadjustedTN + cfncount;
					int adjustedFN = unadjustedFN - cfncount;

					String precisionString = getPrecision(adjustedFP, adjustedTN);
					String recallString = getRecall(adjustedTP, adjustedFN);
					String FmeasureString = getFmeasure(adjustedTP, adjustedFP, adjustedTN, adjustedFN);
					
					System.out.println("Classification=" + classification + " ,AdjustedPrecision=" + precisionString + " ,AdjustedRecall=" + recallString
							+ " ,AdjustedFMeasure=" + FmeasureString + "\n");

//					System.out.println("Classification=" + classification + ",TotalMismatches=" + total
//							+ ",FalsePositives=" + fpcount + ",FalseNegatives=" + fncount
//
//							+ ",CorrectedFalsePositives=" + cfpcount + ",CorrectedFalseNegatives=" + cfncount
//
//							+ " ,MoonstoneValidated=" + value + ",PercentMoonstoneValidation=" + improvement
//
//							+ " ,AdjustedPrecision=" + precisionString + " ,AdjustedRecall=" + recallString
//							+ " ,AdjustedFMeasure=" + FmeasureString
//
//							+ "\n");
				}
				float percentMoonstoneValidation = allMoonstoneValidations / allTotals;
				System.out.println(
						"AllTotals=" + allTotals + ",AllCorrectedFPs=" + allCorrectedFPCount + ",AllCorrectedFNs="
								+ allCorrectedFNCount + ",PercentMoonstoneValidation=" + percentMoonstoneValidation);

				FUtils.writeFile(matchfilename, MoonstoneErrorSB.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getRecall(float tp, float fn) {
		float num = tp;
		float den = tp + fn;
		return getResultString(num, den);
	}

	public static String getPrecision(float fp, float tn) {
		float num = tn;
		float den = tn + fp;
		return getResultString(num, den);
	}

	public static String getFmeasure(float tp, float fp, float tn, float fn) {

		/* F=(1+B^2)*recall*precision/ B^2*precision + recall */
		float Bwt = 1;
		/*
		 * B weight of recall vs precsn, B>1,or<1, means weight recall, orprecn, more
		 */

		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		/* else let recall be 0 */

		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		/* else let precsn be 0 */

		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);

		return getResultString(num, den);
	}

	private static String getResultString(float numerator, float denominator) {
		float result = 0;
		String resultString = "*";
		if (denominator > 0) {
			result = numerator / denominator;
			resultString = String.valueOf(result);
			if (resultString.length() > 4) {
				resultString = resultString.substring(0, 4);
			}
		}
		return resultString;
	}

}

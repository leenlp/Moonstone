package tsl.utilities;

import java.util.Vector;


public class MUtils {
	
	public static float wordVectorLength(int[] counts) {
		int countssquared = 0;
		for (int i = 0; i < counts.length; i++) {
			countssquared += counts[i] * counts[i];
		}
		double d = Math.sqrt(new Float(countssquared).floatValue());
		return new Float(d).floatValue();
	}
	
	public static float objectCountWrapperVectorLength(Vector wrappers) {
		int countssquared = 0;
		for (int i = 0; i < wrappers.size(); i++) {
			int count = ((ObjectInfoWrapper) wrappers.elementAt(i)).count;
			countssquared += count * count;
		}
		double d = Math.sqrt(new Float(countssquared).floatValue());
		return new Float(d).floatValue();
	}
	
	public static float dotProduct(int[] v1, int[] v2) {
		int prod = 0;
		for (int i = 0; i < v1.length; i++) {
			prod += v1[i] * v2[i];
		}
		return new Float(prod).floatValue();
	}
	
	public static float objectCountWrapperdotProduct(Vector w1, Vector w2) {
		int prod = 0;
		for (int i = 0; i < w1.size(); i++) {
			prod += ((ObjectInfoWrapper) w1.elementAt(i)).count
					* ((ObjectInfoWrapper) w2.elementAt(i)).count;
		}
		return new Float(prod).floatValue();
	}
	
	public static float objectCountWrapperVectorSimilarity(Vector w1, Vector w2) {
		float dp = objectCountWrapperdotProduct(w1, w2);
		float l1 = objectCountWrapperVectorLength(w1);
		float l2 = objectCountWrapperVectorLength(w2);
		float lengthprod = l1 * l2;
		float cosinetheta = (lengthprod == 0f ? 0f : dp / lengthprod);
		return cosinetheta;
	}
	

}

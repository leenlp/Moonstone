package tsl.utilities;

public class MathUtils {
	
	// From http://stackoverflow.com/questions/520241/how-do-i-calculate-the-cosine-similarity-of-two-vectors
	
	public static double length(double[] array) {
		double sum = 0d;
		for (int i = 0; i < array.length; i++) {
			sum += array[i] * array[i];
		}
		return Math.sqrt(sum);
	}
	
	public static double dotProduct(double[] a, double[] b) {
		double dp = 0d;
		if (a != null && b != null && a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
				dp += a[i] * b[i];
			}
		}
		return dp;
	}
	
	public static double cosineSimilarity(double[] a, double[] b) {
		double dp = dotProduct(a, b);
		double ma = length(a);
		double mb = length(b);
		double coss = dp / (ma * mb);
		return coss;
	}

}

package tsl.utilities;

import java.util.Comparator;

public class NumUtils {
	
	// e.g. "5/6/2014"
	public static boolean isDateString(String text) {
		if (text != null) {
			int slashcount = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!(Character.isDigit(c) || c == '/')) {
					return false;
				}
				if (c == '/') {
					slashcount++;
				}
			}
			return slashcount == 1;
		}
		return false;
	}

	public static class InverseIntegerSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Integer i1 = (Integer) o1;
			Integer i2 = (Integer) o2;
			if (i1 > i2) {
				return -1;
			}
			if (i1 < i2) {
				return 1;
			}
			return 0;
		}
	}

}

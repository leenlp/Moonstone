package tsl.utilities;

public class SeqUtils {

	public static boolean before(int start1, int end1, int start2, int end2) {
		return end1 < start2;
	}

	public static boolean after(int start1, int end1, int start2, int end2) {
		return start1 > end2;
	}

	public static boolean disjoint(int start1, int end1, int start2, int end2) {
		return end1 < start2 || end2 < start1;
	}

	public static boolean contains(int start1, int end1, int start2, int end2) {
		return start1 <= start2 && end1 >= end2;
	}

	public static boolean containedBy(int start1, int end1, int start2, int end2) {
		return start1 >= start2 && end1 <= end2;
	}

	public static boolean overlaps(int start1, int end1, int start2, int end2) {
		if ((start1 <= start2 && start2 <= end1 && end1 <= end2)
				|| (start2 <= start1 && start1 <= end2 && end2 <= end1)) {
			return true;
		} else {
			return false;
		}
	}

	// 1/15/2015
	public static boolean coincides(int start1, int end1, int start2, int end2) {
		return (start1 == start2 && end1 == end2);
	}

	public static boolean simpleContains(int start1, int end1, int start2,
			int end2) {
		return (contains(start1, end1, start2, end2) && !coincides(start1,
				end1, start2, end2));
	}

	public static int amountOverlap(int start1, int end1, int start2, int end2) {
		if ((start1 == -1) && (start2 == -1)) {
			return 1;
		}
		if (start1 == -1) {
			return end2 - start2;
		}
		if (start2 == -1) {
			return end1 - start1;
		}
		if (overlaps(start1, end1, start2, end2)) {
			if (start2 >= start1 && start2 <= end1) {
				return end1 - start2;
			}
			if (start1 >= start2 && start1 <= end2) {
				return end2 - start1;
			}

		}
		return 0;
	}

}

package factorio.calculator;

public class EditDistance {

	private EditDistance() {}

//	public static int damerauLevenshtein(String key, String match, boolean extend) {
//		key = key.toLowerCase();
//		match = match.toLowerCase();
//
//		if (key.length() <= 0) return match.length();
//		if (match.length() <= 0) return key.length();
//
//		int[][] matrix = new int[key.length() + 1][(extend ? Math.min(match.length(), key.length()) : match.length()) + 1];
//
//		Map<Character, Integer> keyCharIndexes = new HashMap<>();
//
//		for (int i = 0; i < matrix.length; i++)
//			matrix[i][0] = i;
//		for (int i = 0; i < matrix[0].length; i++)
//			matrix[0][i] = i;
//
//		for (int k = 1; k < matrix.length; k++) {
//			// The highest index of key.charAt(k) in match "so far" (i.e. up to m - 1)
//			int newClosestMatchCharIndex = -1;
//			for (int m = 1; m < matrix[k].length; m++) {
//				int closestMatchCharIndex = newClosestMatchCharIndex;
//				int closestKeyCharIndex = keyCharIndexes.getOrDefault(match.charAt(m - 1), -1);
//
//				int min1 = matrix[k - 1][m] + 1;
//				int min2 = matrix[k][m - 1] + 1;
//				int min3 = matrix[k - 1][m - 1];
//
//				if (key.charAt(k - 1) == match.charAt(m - 1)) {
//					newClosestMatchCharIndex = m - 1;
//				} else {
//					min3++;
//				}
//
//				int min4 = closestMatchCharIndex >= 0 && closestKeyCharIndex >= 0 ? matrix[closestKeyCharIndex][closestMatchCharIndex] + (k - closestKeyCharIndex - 2) + (m - closestMatchCharIndex - 2) + 1 : Integer.MAX_VALUE;
//
//				matrix[k][m] = Math.min(Math.min(min1, min2), Math.min(min3, min4));
//			}
//
//			keyCharIndexes.put(key.charAt(k - 1), k - 1);
//		}
//
//		return matrix[matrix.length - 1][matrix[matrix.length - 1].length - 1];
//	}
//
//	public static double wordDamerauLevenshtein(String key, String match) {
//		String[] keys = key.toLowerCase().split("[\\s\\-_]");
//		String[] matches = match.toLowerCase().split("[\\s\\-_]");
//
//		if (keys.length <= 0) return matches.length;
//		if (matches.length <= 0) return keys.length;
//
//		double[][] matrix = new double[keys.length + 1][matches.length + 1];// [Math.min(matches.length + 1, keys.length + 1)];
//
//		for (int i = 0; i < matrix.length; i++)
//			matrix[i][0] = i;
//		for (int i = 0; i < matrix[0].length; i++)
//			matrix[0][i] = i;
//
//		for (int k = 1; k < matrix.length; k++) {
//			for (int m = 1; m < matrix[k].length; m++) {
//				double min = matrix[k - 1][m - 1] + 3.0 * (double) damerauLevenshtein(keys[k - 1], matches[m - 1], false) / Math.max(keys[k - 1].length(), matches[m - 1].length()) + 0.5;
//
//				for (int k2 = 0; k2 < k - 1; k2++) {
//					for (int m2 = 0; m2 < m - 1; m2++) {
//						min = Math.min(min, matrix[k2][m2] + (k - k2 - 2) + (m - m2 - 2) + (double) damerauLevenshtein(keys[k2], matches[m2], false) / Math.max(keys[k2].length(), matches[m2].length()) + 0.5);
//					}
//				}
//
//				matrix[k][m] = min;
//			}
//		}
//
//		return matrix[matrix.length - 1][matrix[matrix.length - 1].length - 1];
//	}

	public static double distance(String key, String match) {
		// TODO create good edit distance algorithm
		return match.toLowerCase().indexOf(key.toLowerCase()) + Integer.MIN_VALUE;
	}

}

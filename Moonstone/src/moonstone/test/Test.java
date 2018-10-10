package moonstone.test;

import java.util.Vector;
import java.util.regex.Pattern;

import tsl.utilities.StrUtils;

public class Test {
	
	public static void main(String[] args) {
		String fstr = "a\\b\\c\\d\\e";
		String delim = Pattern.quote("\\");
		Vector v = StrUtils.stringList(fstr, delim);
//		String[] strs = fstr.split("\\");
		System.out.println(v);
	}

}

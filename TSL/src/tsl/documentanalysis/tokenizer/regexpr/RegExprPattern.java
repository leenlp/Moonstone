package tsl.documentanalysis.tokenizer.regexpr;

import java.util.regex.Pattern;

public class RegExprPattern {

	private Pattern regExPattern = null;

	public RegExprPattern(int tokenType, String description, String pattern) {
		this.regExPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	public Pattern getRegExPattern() {
		return regExPattern;
	}

}

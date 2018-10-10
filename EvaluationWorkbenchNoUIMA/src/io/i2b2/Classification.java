package io.i2b2;

public class Classification {
	char startChar = (char) -1;
	String value = null;
	int startLine = -1;
	int startWord = -1;
	int endLine = -1;
	int endWord = -1;
	int startText = -1;
	int endText = -1;

	public Classification(Token classtoken) {
		this.startChar = classtoken.startChar;
		this.value = classtoken.value;
	}
	
	public Classification(Token classtoken, Token starttoken, Token endtoken) {
		this.startChar = classtoken.startChar;
		this.value = classtoken.value;
		// Apparently begins with offset of 1, e.g. line 32 is annotated as 33.
		this.startLine = starttoken.lineval - 1;
		this.startWord = starttoken.wordval;
		this.endLine = endtoken.lineval - 1;
		this.endWord = endtoken.wordval;
	}

	public char getStartChar() {
		return startChar;
	}

	public void setStartChar(char startChar) {
		this.startChar = startChar;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getStartWord() {
		return startWord;
	}

	public void setStartWord(int startWord) {
		this.startWord = startWord;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getEndWord() {
		return endWord;
	}

	public void setEndWord(int endWord) {
		this.endWord = endWord;
	}

	public int getStartText() {
		return startText;
	}

	public void setStartText(int startText) {
		this.startText = startText;
	}

	public int getEndText() {
		return endText;
	}

	public void setEndText(int endText) {
		this.endText = endText;
	}
	
	public String toString() {
		String str = null;
		if (this.startLine > -1) {
			str = "<Classification: StartLine= " + this.startLine
					+ ",StartWord=" + this.startWord + ",StartText="
					+ this.startText + ",EndLine=" + this.endLine + ",EndWord="
					+ this.endWord + ",EndText=" + this.endText + ">";
		} else {
			str = "<Classification: StartChar= " + this.startChar + ",Value="
					+ this.value + ">";
		}
		return str;
	}

}

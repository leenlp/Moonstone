package io.i2b2;

import java.util.Vector;

import tsl.utilities.VUtils;

public class Parser {

	String inputString = null;
	String token = null;
	int number = 0;
	int tokenIndex = 0;
	Vector<Token> tokens = null;
	Vector<Classification> classifications = null;
	int inputIndex = 0;
	int inputStringLength = 0;

	public Parser(String str) {
		this.inputString = str;
		this.inputStringLength = str.length();
		this.tokens = Token.readTokensFromInput(this);
		gatherClassifications();
	}

	void gatherClassifications() {
		if (this.tokens != null) {
			for (int i = 0; i < this.tokens.size(); i++) {
				Token token = this.tokens.elementAt(i);
				Token next1 = null;
				Token next2 = null;
				Classification c = null;
				if (i < this.tokens.size() - 2) {
					next1 = this.tokens.elementAt(i + 1);
					next2 = this.tokens.elementAt(i + 2);
				}
				if (token.isCharEqualsString()) {
					if (next2 != null) {
						c = new Classification(token, next1, next2);
					} else {
						c = new Classification(token);
					}
					this.classifications = VUtils.add(this.classifications, c);
				}
			}
		}
	}
	
	public String getInputString() {
		return inputString;
	}

	public int getInputIndex() {
		return inputIndex;
	}

	public void setInputIndex(int inputIndex) {
		this.inputIndex = inputIndex;
	}

	public int getInputStringLength() {
		return inputStringLength;
	}

	public int getTokenIndex() {
		return tokenIndex;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

}

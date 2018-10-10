package tsl.documentanalysis.document;

import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.Term;

public class AttributeValuePair extends Term {
	
	private Header header = null;
	private String attribute = null;
	private String value = null;
	private int tokenStart = -1;
	private int tokenEnd = -1;
	private int textStart = -1;
	private int textEnd = -1;
	
	public AttributeValuePair(Vector<Token> atokens, Vector<Token> vtokens) {
		Token first = atokens.firstElement();
		Token last = vtokens.lastElement();
		this.tokenStart = first.getIndex();
		this.tokenEnd = last.getIndex();
		this.textStart = first.getStart();
		this.textEnd = last.getEnd();
		this.attribute = Token.getString(atokens);
		this.value = Token.getString(vtokens);
	}
	
	public Header getHeader() {
		return header;
	}
	
	public static AttributeValuePair getAttributeValuePair(Header header) {
		int index = header.document.getTokenIndex();
		Vector<Token> atokens = null;
		Token dtoken = null;
		Vector<Token> vtokens = null;
		atokens = header.getSpaceDelimitedStringTokens();
		if (atokens != null) {
			dtoken = header.getAVPairDelimiterToken();
			if (dtoken != null) {
				vtokens = header.getSpaceDelimitedStringTokens();
			}
		}
		if (vtokens != null) {
			AttributeValuePair av = new AttributeValuePair(atokens, vtokens);
			return av;
		}
		return null;
	}
	
	public String toString() {
		return "<[" + attribute + "]=[" + value + "]>";
	}

	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}

	public int getTokenStart() {
		return tokenStart;
	}

	public int getTokenEnd() {
		return tokenEnd;
	}

	public int getTextStart() {
		return textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}
	
}

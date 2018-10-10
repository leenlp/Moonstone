package tsl.documentanalysis.tokenizer;


public class URLToken extends Token {
	
	public static int URLTOKEN = 100;
	
	
//	public URLToken(Token token, Element element) {
//		super(token);
//		this.setValue(element);
//		setProperties();
//	}
	
	void setProperties() {
		
	}
	
	public boolean isWord() {
		return true;
	}

}

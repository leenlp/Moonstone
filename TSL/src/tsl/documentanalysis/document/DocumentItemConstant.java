package tsl.documentanalysis.document;

import tsl.expression.term.constant.ObjectConstant;

public abstract class DocumentItemConstant extends ObjectConstant {

	public DocumentItemConstant() {
	}
	
	public DocumentItemConstant(String name) {
		super(name);
	}
	
	public String getText() {
		return null;
	}
	
	public int getTextLength() {
		String text = this.getText();
		if (text != null) {
			return text.length();
		}
		return 0;
	}
}

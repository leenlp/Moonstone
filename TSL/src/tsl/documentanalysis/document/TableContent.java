package tsl.documentanalysis.document;

import java.util.Vector;
import tsl.utilities.VUtils;

public class TableContent extends HeaderContent {

	private Vector<AttributeValuePair> attributeValuePairs = null;

	public TableContent(Header header) {
		super(header);
	}
	
	public void readContent() {
		Document doc = this.header.getDocument();
		int tindex = this.header.getTextStartTokenIndex();
		doc.setTokenIndex(tindex);
		AttributeValuePair av = null;
		while (!this.header.atTextEnd()) {
			av = AttributeValuePair.getAttributeValuePair(this.header);
			if (av != null) {
				attributeValuePairs = VUtils.add(attributeValuePairs, av);
				tindex = doc.getTokenIndex();
			} else {
				doc.setTokenIndex(tindex++);
			}
		}
	}

	public Vector<AttributeValuePair> getAttributeValuePairs() {
		return attributeValuePairs;
	}


}

package moonstone.learning.ncbo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

public class MSAccess {
	DocumentBuilder docBuilder = null;
	String apiKey = "3c98c4df-7b8d-4971-b540-e8aa8651a87b";
	static MSAccess currentAccess = null;

	// static String BIOPORTAL_URL_PREFIX =
	// "http://rest.bioontology.org/bioportal/search/";

	static String BIOPORTAL_URL_PREFIX = "http://rest.bioontology.org/bioportal/";

	public MSAccess() {
		try {
			currentAccess = this;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static MSAccess getAccess() {
		if (currentAccess == null) {
			currentAccess = new MSAccess();
		}
		return currentAccess;
	}

	public Document parseXMLFile(String command, String connector) {
		Document doc = null;
		try {
			String str = BIOPORTAL_URL_PREFIX + command + connector + "apikey="
					+ this.apiKey;
			doc = docBuilder.parse(str);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

}

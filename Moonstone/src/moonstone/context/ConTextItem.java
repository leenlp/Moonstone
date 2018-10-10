package moonstone.context;

import java.util.Enumeration;
import java.util.Hashtable;


public class ConTextItem {

	private String string = null;
	private ConText conText = null;
	private Hashtable<String, String> properties = new Hashtable();
	private boolean isMatch = true;

	public ConTextItem(ConText ct) {
		this.conText = ct;
	}

	public String toString() {
		String str = "<" + this.getString() + ":";
		for (Enumeration<String> e = this.getProperties().keys(); e
				.hasMoreElements();) {
			String header = e.nextElement();
			String value = this.getProperty(header);
			String hvstr = header + "=" + value;
			str += hvstr;
			if (e.hasMoreElements()) {
				str += ",";
			}
		}
		str += ">";
		return str;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public ConText getConText() {
		return conText;
	}

//	public String getCategory() {
//		return this.properties.get(ConText.exCategoryHeader);
//	}
//
//	public String getClosure() {
//		return this.properties.get(ConText.exClosureHeader);
//	}
//
//	public String getAction() {
//		return this.properties.get(ConText.exActionHeader);
//	}

	public String getProperty(String property) {
		return this.properties.get(property);
	}

	public void setProperty(String property, String value) {
		if (value != null && value.length() > 0
				&& Character.isLetter(value.charAt(0))) {
			this.properties.put(property, value);
		}
	}

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public Hashtable<String, String> getProperties() {
		return properties;
	}

}


package tsl.utilities;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jdom.Element;

public class JDomUtils {
	
	public static Vector<Element> getSingleElementsByName(Element element, String name) {
		Vector<Element> urls = null;
		List l = element.getChildren();
		if (!l.isEmpty()) {
			for (ListIterator<Element> li = l.listIterator(); li.hasNext();) {
				Element child = li.next();
				if (name.equals(child.getName())) {
					urls = VUtils.add(urls, child);
				}
			}
		}
		return urls;
	}

	public static Vector<Element> getElementsByName(Element element, String name) {
		Vector<Element> urls = null;
		if (name.equals(element.getName())) {
			urls = VUtils.listify(element);
		} else {
			List l = element.getChildren();
			if (!l.isEmpty()) {
				for (ListIterator<Element> li = l.listIterator(); li.hasNext();) {
					Element child = li.next();
					urls = VUtils.append(urls, getElementsByName(child, name));
				}
			}
		}
		return urls;
	}
	
	public static Element getElementByName(Element element, String name) {
		if (name.equals(element.getName())) {
			return element;
		}
		List l = element.getChildren();
		if (!l.isEmpty()) {
			for (ListIterator<Element> li = l.listIterator(); li.hasNext();) {
				Element child = li.next();
				Element ele = getElementByName(child, name);
				if (ele != null) {
					return ele;
				}
			}
		}
		return null;
	}
	
	public static Vector<String> getValuesByName(Element element, String name) {
		Vector<String> values = null;
		if (name.equals(element.getName())) {
			values = VUtils.listify(element.getValue());
		} else {
			List l = element.getChildren();
			if (!l.isEmpty()) {
				for (ListIterator<Element> li = l.listIterator(); li.hasNext();) {
					Element child = li.next();
					values = VUtils.append(values, getValuesByName(child, name));
				}
			}
		}
		return values;
	}

	public static String getValueByName(Element element, String name) {
		if (element != null) {
			if (name.equals(element.getName())) {
				return element.getValue();
			}
			List l = element.getChildren();
			if (!l.isEmpty()) {
				for (ListIterator<Element> li = l.listIterator(); li.hasNext();) {
					Element child = li.next();
					String value = getValueByName(child, name);
					if (value != null) {
						return value;
					}
				}
			}
		}
		return null;
	}

	public static Vector<String> getValues(Vector<Element> elements) {
		Vector<String> values = null;
		if (elements != null) {
			for (Element element : elements) {
				values = VUtils.add(values, element.getValue());
			}
		}
		return values;
	}

}

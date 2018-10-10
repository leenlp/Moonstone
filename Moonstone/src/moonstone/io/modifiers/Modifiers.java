package moonstone.io.modifiers;

import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import tsl.utilities.VUtils;

public class Modifiers {
	
	private Hashtable<String, Vector<String>> propertyValueHash = new Hashtable();
	private Hashtable<String, String> propertyDefaultHash = new Hashtable();
	
	public Modifiers (String[][] pv) {
		initialize(pv);
	}
	
	private void initialize(String[][] pv) {
		for (String[] strs : pv) {
			String property = strs[0];
			String def = strs[1];
			propertyDefaultHash.put(property, def);
			for (int i = 1; i < strs.length; i++) {
				String value = strs[i];
				VUtils.pushHashVector(propertyValueHash, property, value);
			}
		}
	}
	
	public void assignModifiers(Annotation annotation) {
		
	}
	
	public void assignDefaults(Annotation annotation) {
		for (String key : this.propertyDefaultHash.keySet()) {
			if (annotation.getProperty(key) == null) {
				String value = this.propertyDefaultHash.get(key);
				annotation.setProperty(key, value);
			}
		}
	}

}

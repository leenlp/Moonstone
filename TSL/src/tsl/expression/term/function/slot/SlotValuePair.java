package tsl.expression.term.function.slot;

import java.util.Vector;
import tsl.utilities.VUtils;

public class SlotValuePair {
	private String name = null;
	private Object value = null;
	
	// Like variables, without the "?"...
	
	public SlotValuePair(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public static Vector<SlotValuePair> createSlotValuePairs(Vector<String> names,
			Vector<Object> values) {
		Vector<SlotValuePair> pairs = null;
		if (names != null && values != null && names.size() == values.size()) {
			for (int i = 0; i < names.size(); i++) {
				String name = names.elementAt(i);
				Object value = values.elementAt(i);
				SlotValuePair pair = new SlotValuePair(name, value);
				pairs = VUtils.add(pairs, pair);
			}
		}
		return pairs;
	}

}

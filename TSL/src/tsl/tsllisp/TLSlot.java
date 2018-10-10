package tsl.tsllisp;

import java.util.Vector;

public class TLSlot {
	private String name = null;
	private TLObject value = null;

	public TLSlot(String name, TLObject value) {
		this.name = name;
		this.value = value;
	}

	public TLSlot(String name) {
		this.name = name;
	}
	
	public static TLSlot getSlot(Vector<TLSlot> slots, String name) {
		if (slots != null && name != null) {
			for (TLSlot slot : slots) {
				if (name.equals(slot.name)) {
					return slot;
				}
			}
		}
		return null;
	}

	public TLObject getValue() {
		return value;
	}

	public void setValue(TLObject value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "<Name=" + this.name + ",Value=" + this.value + ">";
	}

}

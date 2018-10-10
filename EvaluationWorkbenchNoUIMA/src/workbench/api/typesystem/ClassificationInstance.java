package workbench.api.typesystem;

import java.util.Vector;

public class ClassificationInstance {
	private Vector<String> attributes = null;
	private Vector<String> values = null;

	public ClassificationInstance(Vector<String> attributes,
			Vector<String> values) {
		this.attributes = attributes;
		this.values = values;
	}

	public Vector<String> getAttributes() {
		return attributes;
	}

	public Vector<String> getValues() {
		return values;
	}
	
}


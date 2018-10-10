package moonstone.io.readmission;

import java.util.Vector;

public class ReadmissionQuery {

	private Readmission readmission = null;
	private String description = null;
	private Vector queryVector = null;

	public ReadmissionQuery(Readmission readmission, String d,
			Vector qv) {
		this.readmission = readmission;
		this.description = d;
		this.queryVector = qv;
	}

	public String getDescription() {
		return description;
	}

	public Readmission getReadmission() {
		return readmission;
	}

	public Vector getQueryVector() {
		return queryVector;
	}

}

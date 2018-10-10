package moonstone.io.readmission;

public class OWLPneumoniaSchema extends ReadmissionProjectSchema {

	public OWLPneumoniaSchema() {

		this.relevantTypes = new String[] { "CONDITION" };

		this.relevantAttributes = new String[] { "classification", "directionality", "experiencer", "temporality",
				"time frame" };

		this.relevantTypeAttributeMap = new String[][] { { "CONDITION", "classification" }};

		this.defaultAttributeValueMap = new String[][] {};

		this.targetConcepts = this.relevantTypes;

		// NEW
		this.conceptConversionMap = new String[][] { { "CONDITION", ":CONDITION:" } };

		this.translationalConceptMap = null;

		this.negatedConceptMap = new String[][] { };
	}
}

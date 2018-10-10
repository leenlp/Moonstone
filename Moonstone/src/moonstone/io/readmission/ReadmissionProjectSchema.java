package moonstone.io.readmission;

public class ReadmissionProjectSchema {

	public String[] relevantTypes = null;
	public String[] relevantAttributes = null;

	// Maps types to attributes
	public String[][] relevantTypeAttributeMap = null;

	// contains lists of types, and values for each type.
	public String[][] typeAttributeValueMap = null;
	
	//
	public String[] targetBinaryConcepts = null;

	public String[] targetConcepts = null;

	// Maps "type:value" pairs to Moonstone concepts.
	public String[][] conceptConversionMap = null;

	public String[][] translationalConceptMap = null;
	public String[][] negatedConceptMap = null;

	// 3/28/2016
	public String[][] defaultAttributeValueMap = null;

	public String[] getRelevantTypes() {
		return relevantTypes;
	}

	public String[] getRelevantAttributes() {
		return relevantAttributes;
	}

	public String[][] getRelevantTypeAttributeMap() {
		return relevantTypeAttributeMap;
	}

	public String[][] getTypeAttributeValueMap() {
		return typeAttributeValueMap;
	}

	public String[] getTargetBinaryConcepts() {
		return targetBinaryConcepts;
	}

	public String[] getTargetConcepts() {
		return targetConcepts;
	}

	public String[][] getConceptConversionMap() {
		return conceptConversionMap;
	}

	public String[][] getTranslationalConceptMap() {
		return translationalConceptMap;
	}

	public String[][] getNegatedConceptMap() {
		return negatedConceptMap;
	}

}

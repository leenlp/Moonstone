package moonstone.io.readmission;

public class AMIR01Schema extends ReadmissionProjectSchema {

	public AMIR01Schema() {

		this.relevantTypes = new String[] { "LIVING ALONE", "INSTRUMENTAL SUPPORT", "COMMUNITY SUPPORT",
				"MARGINAL SUPPORT", "IMPAIRED ADL/IADL", "MEDICAL CONDITION", "MEDICATION COMPLIANCE",
				"DEPRESSION", "LANGUAGE BARRIER", "SECTION", "DEMENTIA" };

		this.relevantAttributes = new String[] { "classification", "assertion", "experiencer", "temporality", "time frame"};

		this.relevantTypeAttributeMap = new String[][] { { "LIVING ALONE", "classification" },
				{ "INSTRUMENTAL SUPPORT", "classification" }, { "COMMUNITY SUPPORT", "classification" },
				{ "MARGINAL SUPPORT", "classification" }, { "IMPAIRED ADL/IADL", "classification" },
				{ "MEDICAL CONDITION", "classification" }, { "MEDICATION COMPLIANCE", "classification" },
				{ "DEPRESSION", "classification" }, { "LANGUAGE BARRIER", "classification" },
				{ "SECTION", "classification" }, { "DEMENTIA", "classification" } };

		this.defaultAttributeValueMap = new String[][] {};

		this.targetBinaryConcepts = new String[] { ":HAVE-SUPPORT:", ":LACK-SUPPORT:", ":LIVING-ALONE:",
				":NOT-LIVING-ALONE:", ":LIVE-AT-HOME:", ":DOES-NOT-LIVE-AT-HOME:" };

		this.targetConcepts = this.relevantTypes;

		// NEW
		this.conceptConversionMap = new String[][] { { "LIVING ALONE", ":LIVING-ALONE:" },
				{ "INSTRUMENTAL SUPPORT", ":HAVE-SUPPORT:" }, { "COMMUNITY SUPPORT", ":ACCESS-TO-COMMUNITY-SERVICES:" },
				{ "MARGINAL SUPPORT", ":PATIENT-IN-MARGINAL-SUPPORT-FACILITY:" }, { "IMPAIRED ADL/IADL", ":PATIENT-HAS-ADL-DISABILITY:" },
				{ "MEDICAL CONDITION", ":PATIENT-HAS-ADL-DISABILITY-DUE-TO-MEDICAL-CONDITION:" }, { "MEDICATION COMPLIANCE", ":PATIENT-IS-NONCOMPLIANT:" },
				{ "DEPRESSION", ":PERSON-IS-DEPRESSED:" }, { "LANGUAGE BARRIER", ":PATIENT-HAVE-LANGUAGE-BARRIER-SENTENCE:" },
				{ "SECTION", ":???:" }, { "DEMENTIA", ":PERSON-HAS-DEMENTIA:"} };

		this.translationalConceptMap = null;

		this.negatedConceptMap = new String[][] {
			{ ":LIVING_ALONE:", ":NOT_LIVING_ALONE:" },
			
			{ ":HAVE_SUPPORT:", ":LACK_SUPPORT:" },
			{ ":HAVE_SUPPORT:", ":POSSIBLE_LACK_SUPPORT:" },
			{ ":POSSIBLE_SUPPORT:", ":LACK_SUPPORT:" },
			{ ":POSSIBLE_SUPPORT:", ":POSSIBLE_LACK_SUPPORT:" },
			
			{ ":LIVE_AT_HOME:", ":DOES_NOT_LIVE_AT_HOME:" },
			{ ":UNSTABLE_HOUSING:", ":STABLE_HOUSING:" } };


	}

}

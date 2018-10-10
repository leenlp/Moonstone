package moonstone.io.modifiers;

import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.javafunction.JavaFunctions;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.VUtils;

public class AMIR01Modifiers extends Modifiers {

	private MoonstoneRuleInterface msri = null;
	private Hashtable<String, Vector<String>> propertyValueHash = new Hashtable();
	private Hashtable<String, String> propertyDefaultHash = new Hashtable();
	private static TypeConstant FamilyType = TypeConstant.findByName("family");
	
	private static String[][] PVals = { { "Assertion", "positive", "hypothetical", "uncertain", "negative" }};
	
	// Before 2/9/2018
//	private static String[][] PVals = { { "Assertion", "positive", "hypothetical", "uncertain", "negative" },
//			{ "Experiencer", "patient", "not patient" }, { "Time Frame", "current", "future", "past" } };

	public AMIR01Modifiers(MoonstoneRuleInterface msri) {
		super(PVals);
		this.msri = msri;
	}

	public void assignModifiers(Annotation annotation) {
		String def = null;
		int x = 1;
		this.assignDefaults(annotation);
		if (JavaFunctions.isNegated(annotation)) {
			annotation.removeProperty("directionality");
			annotation.removeProperty("assertion");
			annotation.setProperty("Assertion", "negative");
		}
		if (JavaFunctions.isUncertain(annotation)) {
			annotation.removeProperty("directionality");
			annotation.removeProperty("assertion");
			annotation.setProperty("Assertion", "uncertain");
		}
		if (JavaFunctions.isHypothetical(annotation)) {
			annotation.removeProperty("directionality");
			annotation.removeProperty("assertion");
			annotation.setProperty("Assertion", "uncertain");
		}
		// removed 2/9/2018
//		StringConstant sc = (StringConstant) annotation.getConcept();
//		String cname = sc.toString().toLowerCase();
//		if (cname != null && cname.contains("med") || cname.contains("depr")) {
//			Annotation subject = (Annotation) annotation.getProperty("subject");
//			if (subject != null && subject.getType() != null
//					&& TypeConstant.isUnifiableBelowBase(subject.getType(), FamilyType)) {
//				annotation.setProperty("Experiencer", "family");
//			}
//		}
//		Annotation tchild = JavaFunctions.getChildAnnotationViaProperty(annotation, "temporality");
//		Annotation achild = JavaFunctions.getTypedChildAnnotation(annotation, "action");
//		if ((tchild != null && ("past".equals(tchild.getProperty("temporality"))
//				|| "historical".equals(tchild.getProperty("temporality"))))
//		// || (achild != null && JavaFunctions.isEDFormActionWord(achild))
//		) {
//			annotation.setProperty("Time Frame", "past");
//		}
	}

}

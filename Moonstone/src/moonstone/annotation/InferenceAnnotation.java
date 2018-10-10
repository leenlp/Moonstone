package moonstone.annotation;

import java.util.Collections;
import java.util.Vector;

import moonstone.information.Information;
import moonstone.rule.Rule;
import moonstone.semantic.Interpretation;

public class InferenceAnnotation extends Annotation {

	public InferenceAnnotation(Vector<Annotation> sources, Rule rule,
			String text, String cui, String concept) {
		Collections.sort(sources, new Annotation.StartPositionSorter());
		this.setRule(rule);
		this.setString(text);
		this.setTextuallySortedSourceAnnotations(sources);
		Annotation first = sources.firstElement();
		Annotation last = sources.lastElement();
		this.setTextStart(first.getTextStart());
		this.setTextEnd(last.getTextEnd());
		if (rule != null) {
			Interpretation si = new Interpretation(this, null,
					null, null, this.getRule(), null);
			this.setSemanticInterpretation(si);
			for (Annotation source : sources) {
				Interpretation ssi = source.getSemanticInterpretation();
				Information.copyProperties(ssi.getProperties(),
						si.getProperties());
			}
			si.setCui(cui);
			si.setConcept(concept);
			si.setProperty("MODULE", "INFERENCE");
			String ruleid = (rule != null ? rule.getRuleID() : "*INFERENCE*");
			si.setProperty("ruleid", ruleid);
		}
	}


}

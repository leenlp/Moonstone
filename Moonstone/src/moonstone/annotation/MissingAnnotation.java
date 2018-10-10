package moonstone.annotation;

public class MissingAnnotation extends Annotation {

	public MissingAnnotation(String cui, String concept, int start, int end,
			String directionality, String temporality, String experiencer) {
		this.setCui(cui);
		this.setConcept(concept);
		this.setTextStart(start);
		this.setTextEnd(end);
		this.setProperty("MODALITY", "MISSING");
		this.setProperty("concept", concept);
		this.setProperty("cui", cui);
		this.setProperty("directionality", directionality);
		this.setProperty("experiencer", experiencer);
		this.setProperty("temporality", temporality);
	}

}

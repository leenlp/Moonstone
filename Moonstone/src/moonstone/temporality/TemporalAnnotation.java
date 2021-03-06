package moonstone.temporality;


import moonstone.annotation.TerminalAnnotation;
import tsl.expression.term.type.TypeConstant;

public class TemporalAnnotation extends TerminalAnnotation {

	private int sentenceIndex = -1;

	public TemporalAnnotation(String concept, String string, int tokenStart, int tokenEnd, int textStart, int textEnd,
			int wordTokenStart, int wordTokenEnd, TypeConstant type) {
		super(null, null, concept, null, string, tokenStart, tokenEnd, textStart, textEnd, wordTokenStart, wordTokenEnd,
				null, type);

	}

	// document,string,concept,stype,textStart,textEnd,tokenStart,tokenEnd,sentenceIndex

	public String toFileString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getDocument().getAbsoluteFilePath() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getText() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getConcept() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getType() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getTextStart() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getTextEnd() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getTokenStart() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getTokenEnd() + TemporalAnnotationManager.FileStringDelimiter);
		sb.append(this.getSentenceAnnotation().getSentence().getDocumentIndex());
		sb.append("\n");
		return sb.toString();
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}
	
	

}

package tsl.documentanalysis.document;

import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.VUtils;

public class NarrativeContent extends HeaderContent {

	public NarrativeContent(Header header) {
		super(header);
	}

	public void readContent() {
		gatherSentences();
	}

	public void gatherSentences() {
		if (this.getSentences() == null) {
			Document doc = this.header.document;
			Sentence sentence = null;
			doc.setTokenIndex(this.header.getTextStartTokenIndex());
			while ((sentence = readNextSentence()) != null) {
				this.addSentence(sentence);
			}
//			System.out.println("Header=" + this.header + "Sentences=" + this.getSentences());
			int x = 1;
		}
	}
	
	public Sentence readNextSentence() {
		Document document = header.document;
		int currentIndex = document.getTokenIndex();
		int endIndex = header.getTextEndTokenIndex();
		Sentence sentence = null;
		Vector tokens = null;
		int newlineCount = 0;
		if (document.getTokenIndex() <= endIndex) {

			Token.advanceToNextWord(header.document);
			
			boolean atEnd = false;
			while (!atEnd) {
				Token token = Token.peekNextToken(document);
				if (token == null || token.isSentenceDelimiter()
						|| document.getTokenIndex() > endIndex) {
					atEnd = true;
				} else {
					tokens = VUtils.add(tokens, Token.readNextToken(document));
				}
			}
			if (tokens != null) {
				sentence = new Sentence(header, tokens);
				sentence.documentIndex = document.getNumberOfSentences() - 1;
			}
		}
		return sentence;
	}

}

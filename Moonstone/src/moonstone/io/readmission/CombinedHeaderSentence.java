package moonstone.io.readmission;

import java.util.Vector;

import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;

public class CombinedHeaderSentence extends Sentence {

	public CombinedHeaderSentence(Header header, Vector<Token> tokens,
			boolean addToDocument) {
		super(header, tokens, addToDocument);
	}

}

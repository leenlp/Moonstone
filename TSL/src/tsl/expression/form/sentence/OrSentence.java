package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.utilities.VUtils;

public class OrSentence extends ComplexSentence {
	
	public OrSentence() {
	}
	
	public OrSentence(Vector v) {
		super(v);
	}

	public static OrSentence createOrSentence(Vector v) {
		if (v != null && "or".equals(v.firstElement())) {
			return new OrSentence(v);
		}
		return null;
	}
	
	public static boolean isOrSentence(Vector v) {
		return (v != null && "or".equals(v.firstElement()));
	}
	
	public Expression copy() {
		OrSentence os = new OrSentence();
		for (Sentence oldsent : this.sentences) {
			Sentence newsent = (Sentence) oldsent.copy();
			os.sentences = VUtils.add(os.sentences, newsent);
		}
		return os;
	}
	
	public String toString() {
		String str = "(or ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s + " ";
			}
		}
		str += ")";
		return str;
	}
	
	public String toLisp() {
		String str = "(or ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s.toLisp() + " ";
			}
		}
		str += ")";
		return str;
	}
	
	public String toShortString() {
		String str = "(or ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s.getStringID() + " ";
			}
		}
		str += ")";
		return str;
	}

}

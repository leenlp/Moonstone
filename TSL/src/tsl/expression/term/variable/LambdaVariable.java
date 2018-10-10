package tsl.expression.term.variable;

import java.util.Vector;

import tsl.expression.form.sentence.Sentence;

public class LambdaVariable extends Variable {
	
	// 10/16/2013
	// Syntax:  (lambda (x) (..sentence ..))
	// For variables with sentential modifiers, i.e. "X such that Y".  
	// Like a Lisp lambda function.
	// e.g. "X of type cough such that severity is high and temporality is acute
	// Corresponds to Wendy's idea of a schema
	
	private Sentence modifier = null;
	
	public LambdaVariable(String vname, Sentence modifier) {
		this.setName(vname);
		this.setModifier(modifier);
		modifier.setVariable(this);
	}
	
	public static LambdaVariable createLambdaVariable(Vector v) {
		Vector vv = (Vector) v.elementAt(1);
		Vector sv = (Vector) v.elementAt(2);
		String vname = (String) vv.firstElement();
		Sentence modifier = Sentence.createSentence(sv);
		return new LambdaVariable(vname, modifier);
	}
	
	public static boolean isLambdaVariableDefinition(Vector v) {
		return v != null && "lambda".equals(v.firstElement());
	}

	public Sentence getModifier() {
		return modifier;
	}
	
	public void setModifier(Sentence modifier) {
		this.modifier = modifier;
	}

}

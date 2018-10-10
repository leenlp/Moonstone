package tsl.expression.form;

import java.util.Vector;
import tsl.expression.Expression;
import tsl.expression.form.assertion.Assertion;
import tsl.expression.form.definition.Definition;
import tsl.expression.form.sentence.Sentence;
import tsl.inference.backwardchaining.Query;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;


public abstract class Form extends Expression {

	public Form() {
		super();
	}

	public Form(String name) {
		super(name);
	}
	
	public Form(Vector pattern) {
		super(pattern);
	}

	public static Form createForm(Vector v) {
		Form f = null;
		if ((f = Query.createQuery(v)) != null
				|| (f = Sentence.createSentence(v)) != null
				|| (f = Assertion.createAssertion(v)) != null
				|| (f = Definition.createDefinition(v)) != null) {
			return f;
		}
		return null;
	}

	public static Form createForm(Sexp sexp) {
		Vector v = TLUtils.convertSexpToJVector(sexp);
		return createForm(v);
	}

}

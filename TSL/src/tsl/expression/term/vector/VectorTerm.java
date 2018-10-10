package tsl.expression.term.vector;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

// 8/3/2016:  A Term which contains a list of Terms, for use with Java "or".

public class VectorTerm extends Term {
	
	public VectorTerm(KnowledgeBase kb, Expression expr, Vector<Vector> v) {
		super();
		this.setKnowledgeBase(kb);
		for (Vector sv : v) {
			Object t = kb.getTerm(expr, sv);
			this.addTerm(t);
		}
	}
	
	// 8/10/2016:  Not yet tested...
	public Object eval() {
		Vector v = null;
		for (Object o : this.getTerms()) {
			Object rv = o;
			if (o instanceof Term) {
				Term t = (Term) o;
				rv = t.eval();
			} 
			if (rv != null) {
				rv = Term.wrapTerm(rv);
				v = VUtils.add(v, rv);
			} else {
				return null;
			}
		}
		return v;
	}
	
	// Before 8/10/2016
//	public Object eval() {
//		Vector v = new Vector(0);
//		for (Object o : this.getTerms()) {
//			JavaFunctionTerm t = (JavaFunctionTerm) o;
//			Object rv = t.eval();
//			v.add(rv);
//		}
//		return v;
//	}

}

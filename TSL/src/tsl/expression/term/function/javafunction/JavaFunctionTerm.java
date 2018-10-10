package tsl.expression.term.function.javafunction;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionTerm;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class JavaFunctionTerm extends FunctionTerm {

	private JavaFunctionConstant fconst = null;
	
	// Before 3/21/2014
//	private Vector argv = null;
//	private Object[] argv = null;

	public JavaFunctionTerm(Vector v) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.setKnowledgeBase(kb);
		this.fconst = JavaFunctionConstant
				.createJavaFunctionConstant((String) v.firstElement());
		for (int i = 1; i < v.size(); i++) {
			Object o = v.elementAt(i);
			o = kb.getTerm(this, o);
			this.addTerm(o);
		}
		// 3/21/2014
		this.argumentArray = new Object[this.getTermCount()];
	}

	public static JavaFunctionTerm createJavaFunctionTerm(Vector v) {
		JavaFunctionConstant jfc = JavaFunctionConstant
				.createJavaFunctionConstant((String) v.firstElement());
		if (jfc != null) {
			return new JavaFunctionTerm(v);
		}
		return null;
	}

	public Object eval() {
		Term.unpack(this.getTerms(), this.argumentArray);
		for (int i = 0; i < this.argumentArray.length; i++) {
			if (this.argumentArray[i] == null) {
				return null;
			}
		}
		return eval(this.argumentArray);
	}

	public Object eval(Object[] arguments) {
		try {
			if (this.fconst != null) {
				return this.fconst.apply(arguments);
			}
		} catch (Exception e) {
			System.out.println("JavaFunctionTerm error: " + e);
		}
		return null;
	}
	
	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		for (Object o : this.getTerms()) {
			if (o instanceof Expression) {
				((Expression) o).assignContainingKBExpression(kb,
						containingKBExpression);
			}
		}
	}
	
	public String toString() {
		String str = this.fconst.getName() + "(";
		for (int i = 0; i < this.getTermCount(); i++) {
			Object o = this.getTerm(i);
			str += o;
			if (i < this.getTermCount() - 1) {
				str += ", ";
			}
		}
		str += ")";
		return str;
	}

}

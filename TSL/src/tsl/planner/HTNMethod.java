package tsl.planner;

import java.util.Vector;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.rule.RuleSentence;
import tsl.utilities.VUtils;

public class HTNMethod extends RuleSentence {

	public HTNMethod(Vector v) {
		super();
		Vector<String> nv = (Vector) v.firstElement();
		this.setName(nv.elementAt(2));
		Vector rv = (Vector) VUtils.assocValueTopLevel("task", v);
		this.setConsequent(new HTNTaskSentence(rv));
		Vector<Vector> stvs = VUtils.assocTopLevel("subtasks", v);
		Vector<Sentence> subtasks = null;
		if (stvs != null) {
			for (Vector stv : stvs) {
				String tname = (String) stv.firstElement();
				Vector sv = (Vector) stv.elementAt(1);
				Vector pcv = (Vector) stv.elementAt(2);
				HTNTaskSentence stsent = new HTNTaskSentence(sv);
				stsent.setName(tname);
				stsent.setMethod(this);
				Sentence precond = Sentence.createSentence(pcv);
				stsent.setPrecondition(precond);
				subtasks = VUtils.add(subtasks, stsent);
			}
			this.setAntecedent(new AndSentence(subtasks));
		}
	}

	public static HTNMethod createHTNMethod(Vector v) {
		if (v != null
				&& v.firstElement() instanceof Vector
				&& "htnmethod".equals(((Vector) v.firstElement())
						.firstElement())) {
			return new HTNMethod(v);
		}
		return null;
	}

}

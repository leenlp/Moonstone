package tsl.expression.term.function.slot;

import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class SlotFunctionConstant extends FunctionConstant {

	public SlotFunctionConstant(String sname) {
		super(sname);
	}

	public static SlotFunctionConstant createSlotFunctionConstant(String sname) {
		SlotFunctionConstant sfc = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		sfc = (SlotFunctionConstant) kb.getNameSpace().getFunctionConstant(
				sname);
		if (sfc == null) {
			sfc = new SlotFunctionConstant(sname);
		}
		return sfc;
	}

	public Object apply(Object arg) {
		Object result = null;
		if (arg instanceof Term) {
			Term sterm = (Term) arg;
			result = sterm.getHeritableProperty(this.getName());
		}
		return result;
	}

	// e.g. "slot-age" => "age"
	public static String getSlotName(String str) {
		if (str.indexOf("get-slot-") == 0 || str.indexOf("set-slot-") == 0) {
			int i = str.lastIndexOf("-");
			return str.substring(i + 1);
		}
		return null;
	}

}

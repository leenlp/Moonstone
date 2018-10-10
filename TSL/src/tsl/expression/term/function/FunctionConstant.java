package tsl.expression.term.function;

import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.function.javafunction.JavaFunctionConstant;
import tsl.expression.term.function.logicfunction.LogicFunctionConstant;

public abstract class FunctionConstant extends Constant {

	public FunctionConstant() {
		super();
	}

	public FunctionConstant(String name) {
		super(name);
	}

	public static FunctionConstant createFunctionConstant(Vector v) {
		FunctionConstant fc = null;
		if (v != null) {
			if ((fc = JavaFunctionConstant
					.createJavaFunctionConstant((String) v.firstElement())) != null
					|| (fc = LogicFunctionConstant
							.createLogicFunctionConstant(v)) != null) {
				return fc;
			}
		}
		return null;
	}

}

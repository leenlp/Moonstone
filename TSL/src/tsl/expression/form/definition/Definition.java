package tsl.expression.form.definition;

import java.util.Vector;

import tsl.expression.form.Form;

public class Definition extends Form {

	public static Definition createDefinition(Vector v) {
		Definition def = null;
		if ((def = DefineGlobalVariable.createDefineGlobalVariable(v)) != null) {
			return def;
		}
		return null;
	}

	public static boolean isDefinition(Vector v) {
		String str = (String) v.firstElement();
		return (str.indexOf("def") == 0);
	}

}

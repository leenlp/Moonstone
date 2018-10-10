package moonstone.annotation;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.grammar.GrammarModule;
import tsl.expression.term.type.TypeConstant;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.utilities.FUtils;

public class AnnotationIntegrationMaps {

	public static Hashtable<String, String> map = new Hashtable<String, String>();
	public static Vector<String> uniqueCUIs = null;
	public static Vector<String> uniqueConcepts = null;
	public static boolean initialized = false;

	public static void initialize(GrammarModule control) {
		try {
			if (initialized) {
				return;
			}
			initialized = true;
			Hashtable cuihash = new Hashtable();
			Hashtable concepthash = new Hashtable();
			JLisp jlisp = JLisp.getJLisp();
			if (control != null) {
				String resourceDirectory = control.getKnowledgeEngine()
						.getStartupParameters().getResourceDirectory();
				String filename = FUtils.getFileName(resourceDirectory,
						"CoreConceptMap");
				Sexp sexp = (Sexp) jlisp.loadFile(filename);
				if (sexp != null) {
					Vector<Vector<String>> v = JLUtils
							.convertSexpToJVector(sexp);
					for (Vector<String> subv : v) {
						// String concept = subv.firstElement().toLowerCase();
						String concept = subv.firstElement();
						String cui = subv.lastElement().toUpperCase();
						map.put(concept, cui);
						map.put(concept.toLowerCase(), cui);
						map.put(concept.toUpperCase(), cui);
						map.put(cui, concept);
						cuihash.put(cui, cui);
						concepthash.put(concept, concept);

						// 5/13/2014: TEMPORALITY TOOK THIS OUT...
						// TypeConstant.createTypeConstant(concept);
					}
				}
			}
			uniqueCUIs = new Vector(cuihash.keySet());
			uniqueConcepts = new Vector(concepthash.keySet());
			Collections.sort(uniqueCUIs);
			Collections.sort(uniqueConcepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCUI(String name) {
		String cui = map.get(name.toLowerCase());
		if (cui == null) {
			cui = map.get(name.toUpperCase());
		}
		return cui;
	}

	public static String getName(String cui, String concept) {
		String cstr = (cui != null ? map.get(cui.toUpperCase()) : null);
		concept = (cstr != null ? cstr : concept);
		return concept;
	}

	public static Vector<String> getCUIs() {
		return uniqueCUIs;
	}

	public static Vector<String> getConcepts() {
		return uniqueConcepts;
	}

}

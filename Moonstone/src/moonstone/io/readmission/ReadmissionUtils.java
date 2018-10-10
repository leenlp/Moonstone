package moonstone.io.readmission;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class ReadmissionUtils {

	public static void storeAllAnnotationSnippets(MoonstoneRuleInterface msri) {
		Hashtable<String, Vector<String>> shash = new Hashtable();
		Analysis analysis = msri.getWorkbench().getAnalysis();
		for (Annotation annotation : analysis.getAllAnnotations()) {
			String str = annotation.getText();
			String c = annotation.getName();
			VUtils.pushIfNotHashVector(shash, c, str);
		}
		StringBuffer sb = new StringBuffer();
		for (String c : shash.keySet()) {
			Vector<String> v = shash.get(c);
			if (v != null) {
				sb.append("\nCLASS= " + c + "\n");
				for (String str : v) {
					sb.append("\t" + str + "\n");
				}
			}
		}
		String fpath = msri.getResourceDirectoryName() + File.separatorChar
				+ "AllAnnotationSnippets";
		FUtils.writeFile(fpath, sb.toString());
	}

}

package edu.utah.blulab.evaluationworkbenchmanager;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import tsl.utilities.FUtils;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.ResultTable;
import com.ed.wew.api.WEWManager;

public class WEWManagerInterface {
	
	public static void main(final String[] args) throws Exception {
		invokeWEWManagerLocalFileSystem(true);
	}
	
	public static EvaluationWorkbench initializeOldWorkbench(boolean dobail) {
		EvaluationWorkbench wb = null;
		try {
			wb = new EvaluationWorkbench();
			new StartupParameters(wb, true, dobail);
			wb.reinitializeTypeSystem();
			AnnotationAnalysis aa = new AnnotationAnalysis(wb);
			aa.initializeAnnotators();
			aa.readAnnotationCollections();
			aa.postProcessAnnotationCollections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wb;
	}
	
	public static void invokeWEWManagerLocalFileSystem(boolean dobail) throws Exception {

		EvaluationWorkbench wb = WEWManagerInterface.initializeOldWorkbench(dobail);

		// Schema
		DocumentImpl schema = new DocumentImpl();
		String format = wb.getStartupParameters().getInputTypeFirstAnnotator();
		String schemaFileName = wb.getStartupParameters()
				.getKnowtatorSchemaFile();
		String documentDir = wb.getStartupParameters().getTextInputDirectory();
		String primaryAnnotationDir = wb.getStartupParameters()
				.getAnnotationInputDirectoryFirstAnnotator();
		String secondaryAnnotationDir = wb.getStartupParameters()
				.getAnnotationInputDirectorySecondAnnotator();
		String primaryAnnotatorName = wb.getStartupParameters()
				.getFirstAnnotatorName();
		String secondaryAnnotatorName = wb.getStartupParameters()
				.getSecondAnnotatorName();

		if (schemaFileName != null) {
			schema.setName(schemaFileName);
			schema.setReader(new FileReader(schemaFileName));
		}

		// Documents
		List<DocumentReference> documents = new ArrayList();
		Vector<File> files = tsl.utilities.FUtils
				.readFilesFromDirectory(documentDir);
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				DocumentImpl d = new DocumentImpl();
				d.setName(sname);
				d.setReader(new FileReader(lname));
				documents.add(d);
			}
		}

		// Primary
		List<AnnotatorReference> primary = new ArrayList();
		files = tsl.utilities.FUtils
				.readFilesFromDirectory(primaryAnnotationDir);
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Primary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				primary.add(a1);
			}
		}

		// Secondary
		List<AnnotatorReference> secondary = new ArrayList();
		files = tsl.utilities.FUtils
				.readFilesFromDirectory(secondaryAnnotationDir);
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Secondary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				secondary.add(a1);
			}
		}

		// Params
		Params params = new Params();
		params.putParam("format", format);
		params.putParam("firstAnnotator", primaryAnnotatorName);
		params.putParam("secondAnnotator", secondaryAnnotatorName);

		ResultTable result = WEWManager.load(schema, documents, primary,
				secondary, params);

		String text = result.toString();

		FUtils.writeFile("/Users/leechristensen/Desktop/WEWManagerResult.txt",
				text);
		// System.out.println("\n\n" + result + "\n\n");
	}


}

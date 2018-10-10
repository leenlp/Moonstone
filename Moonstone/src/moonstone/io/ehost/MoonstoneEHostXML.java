package moonstone.io.ehost;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.input.knowtator.KTClassMention;
import workbench.api.input.knowtator.KTSimpleInstance;
import workbench.api.input.knowtator.KTStringSlotMention;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.TypeSystem;
import moonstone.annotation.Annotation;
import moonstone.io.readmission.Readmission;
import moonstone.io.readmission.ReadmissionPatientResults;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class MoonstoneEHostXML {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private int EHostID = 0;
	private boolean useLowestOnly = false;

	private Vector<String> relevantAttributeNames = VUtils.arrayToVector(new String[] { "concept" });

	public MoonstoneEHostXML(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
	}

	public void generateEHostAnnotationsNested(MoonstoneRuleInterface msri, boolean targetsOnly, boolean applyModifiers,
			boolean nestedOutputDir) {
		readmissionGenerateEHostAnnotationsNestedDirectories(msri, targetsOnly, applyModifiers, nestedOutputDir);
	}

	public void readmissionGenerateEHostAnnotationsFlat(MoonstoneRuleInterface msri, boolean targetsOnly) {
		readmissionGenerateEHostAnnotationsFlatDirectory(msri, targetsOnly);
	}

	public void readmissionGenerateEHostAnnotationsFlatDirectory(MoonstoneRuleInterface msri, boolean targetsOnly) {
		Readmission readmission = Readmission.createReadmission(msri);
		String inputdirname = this.moonstoneRuleInterface.getStartupParameters().getTextInputDirectory();
		String outputdirname = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue("ResultsOutputDirectory");
		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}

		File sourcedir = new File(inputdirname);
		File[] files = sourcedir.listFiles();

		System.out.println("About to process " + files.length + " files...");

		int fileStartIndex = 0;
		int fileEndIndex = files.length;

		String startstr = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue("ReadmissionFileStartIndex");
		String endstr = this.moonstoneRuleInterface.getStartupParameters().getPropertyValue("ReadmissionFileEndIndex");

		if (startstr != null && endstr != null) {
			fileStartIndex = Integer.parseInt(startstr);
			fileEndIndex = Integer.parseInt(endstr);
		}
		String rangestr = fileStartIndex + "_" + fileEndIndex;

		String delim = this.moonstoneRuleInterface.getStartupParameters().getPropertyValue("FilePatientNameDelimiter");
		if (delim == null) {
			delim = "_";
		}

		for (int i = fileStartIndex; i < fileEndIndex; i++) {
			File file = files[i];

			// String fname = file.getName().toLowerCase();
			String fname = file.getName();

			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}

			if (!fname.endsWith(".txt") || fname.contains("xml") || fname.contains("knowtator")) {
				continue;
			}

			String pname = Document.extractPatientNameFromReportName(file.getName(), delim);
			if (pname == null) {
				continue;
			}

			if (ReadmissionPatientResults.doRejectFileName(fname)) {
				continue;
			}

			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// String casename = null;
			// String fpath = file.getAbsolutePath();
			// String delim = Pattern.quote("\\");
			// Vector v = StrUtils.stringList(fpath, delim);
			// if (v != null && v.size() > 2) {
			// String penult = (String) v.elementAt(v.size() - 1);
			// if (Character.isDigit(penult.charAt(0))) {
			// casename = penult;
			// }
			// }

			System.out.print("Processing [" + i + "]: " + fname + "...");
			long start = System.currentTimeMillis();
			Document doc = new Document(fname, text);
			doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
			Vector<Annotation> targets = this.gatherTargetAnnotations(readmission, doc, false);
			try {
				String htmlfilename = StrUtils.textToHtml(file.getName());
				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
				xml += this.toXML(targets);
				if (targets != null) {
					System.out.println(xml);
					int x = 1;
				}
				xml += "</annotations>\n";

				String sname = outputdirname + File.separatorChar;
				if (pname != null) {
					sname += pname + File.separatorChar;
				}
				sname += file.getName() + ".knowtator.xml";
				fcount++;
				FUtils.writeFile(sname, xml);
				this.moonstoneRuleInterface.releaseAnnotations();
			} catch (Exception e) {
				e.printStackTrace();
			}

			long end = System.currentTimeMillis();
			long duration = end - start;
			this.moonstoneRuleInterface.releaseAnnotations();
			System.out.println("(" + duration + " milliseconds)");
		}
		System.out.println("ReadmissionGeneralEHostAnnotation Done.  File count=" + fcount);
	}

	public void readmissionGenerateEHostAnnotationsNestedDirectories(MoonstoneRuleInterface msri, boolean targetsOnly,
			boolean applyModifiers, boolean nestedOutputDir) {
		Readmission readmission = Readmission.createReadmission(msri);
		int x = 1;
		String inputdirname = msri.getStartupParameters().getTextInputDirectory();
		String outputdirname = msri.getStartupParameters().getResultsOutputDirectory();

		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}
		Vector<File> files = FUtils.readFilesFromDirectory(inputdirname);

		String delim = this.moonstoneRuleInterface.getStartupParameters().getPropertyValue("FilePatientNameDelimiter");
		if (delim == null) {
			delim = "_";
		}

		int i = 0;
		for (File file : files) {
			String fname = file.getName().toLowerCase();

			x = 1;
			boolean isReadmission = false; // 6/4/2018:  Need to implement this...
			if (!EvaluationWorkbenchManager.isReportFile(file, isReadmission, true)) {
				continue;
			}

			if (!fname.endsWith(".txt") || fname.contains("xml") || fname.contains("knowtator")) {
				continue;
			}

			String pname = Document.extractPatientNameFromReportName(file.getName(), delim);
			if (pname == null) {
				continue;
			}

			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// String casename = null;
			// String fpath = file.getAbsolutePath();
			// String delim = Pattern.quote("\\");
			// Vector v = StrUtils.stringList(fpath, delim);
			// if (v != null && v.size() > 2) {
			// String penult = (String) v.elementAt(v.size() - 1);
			// if (Character.isDigit(penult.charAt(0))) {
			// casename = penult;
			// }
			// }

			System.out.print("Processing [" + i++ + "]: " + fname + "...");
			long start = System.currentTimeMillis();
			Document doc = new Document(fname, text);
			doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
			Vector<Annotation> targets = this.gatherTargetAnnotations(readmission, doc, applyModifiers);

			// 11/28/2017
			if (targets != null && applyModifiers) {
				for (Annotation target : targets) {
					this.moonstoneRuleInterface.getAnnotationModifiers().assignModifiers(target);
				}
			}

			try {
				String htmlfilename = StrUtils.textToHtml(file.getName());
				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
				xml += this.toXML(targets);
//				if (targets != null) {
//					System.out.println(xml);
//				}
				xml += "</annotations>\n";

				String sname = outputdirname + File.separatorChar;
				if (nestedOutputDir && pname != null) {
					sname += pname + File.separatorChar;
				}
				sname += file.getName() + ".knowtator.xml";
				fcount++;
				FUtils.writeFile(sname, xml);
			} catch (Exception e) {
				e.printStackTrace();
			}

			long end = System.currentTimeMillis();
			long duration = end - start;
			this.moonstoneRuleInterface.releaseAnnotations();
			System.out.println("(" + duration + " milliseconds)");
		}
		System.out.println("ReadmissionGeneralEHostAnnotation Done.  File count=" + fcount);
	}

	public String generateEHostXMLSingleDocument(MoonstoneRuleInterface msri, String text, boolean targetsOnly,
			boolean applyModifiers) {
		String xml = "";
		// Readmission readmission = new Readmission(msri);
		Readmission readmission = Readmission.createReadmission(msri);
		Document doc = new Document(null, text);
		Vector<Annotation> targets = this.gatherTargetAnnotations(readmission, doc, applyModifiers);
		if (targets != null) {
			if (applyModifiers) {
				for (Annotation target : targets) {
					msri.getAnnotationModifiers().assignModifiers(target);
					int x = 1;
				}
			}
		}
		try {
			xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			xml += "<annotations textSource=\"000\">\n";
			xml += this.toXML(targets);
			xml += "</annotations>\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.moonstoneRuleInterface.releaseAnnotations();
		return xml;
	}

	public Vector<Annotation> gatherTargetAnnotations(Readmission readmission, Document doc, boolean allowNegation) {
		Vector<Annotation> targets = null;
		
		Vector<Annotation> annotations = this.moonstoneRuleInterface.getControl().applyNarrativeGrammarToText(doc, true, true, true);
		annotations = this.moonstoneRuleInterface.getControl().getDocumentGrammar().getDisplayedAnnotations();

		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> relevant = null;
				if (annotation.getGoodness() < 0.10) {
					continue;
				}
				if (this.useLowestOnly) {
					Annotation ann = readmission.getLowestRelevantAnnotation(annotation, allowNegation);
					relevant = VUtils.listify(ann);
				} else {
					int x = 1;
					relevant = readmission.gatherAllRelevantAnnotations(annotation, false, allowNegation);
				}
				if (relevant != null) {
					// targets = appendIfSeparate(targets, relevant);
					targets = VUtils.append(targets, relevant);
				}
			}
		}
		targets = Annotation.removeDuplicates(targets);
		if (targets !=  null) {
			Collections.sort(targets, new Annotation.StartPositionSorter());
		}
		return targets;
	}

	public void displayParsedSentenceXML() {
		Vector<Annotation> annotations = this.moonstoneRuleInterface.getControl().getDocumentGrammar()
				.getDisplayedAnnotations();
		String xml = toXML(annotations);
		System.out.println(xml);
	}

	public String toXML(Vector<Annotation> annotations) {
		String rv = null;
		Vector<KTSimpleInstance> sis = extractAnnotations(annotations);
		StringBuffer sb = new StringBuffer();
		if (sis != null) {
			for (KTSimpleInstance si : sis) {
				sb.append(si.toXML() + "\n");
			}
		}
		rv = sb.toString();
		return rv;
	}

	public Vector<KTSimpleInstance> extractAnnotations(Vector<Annotation> annotations) {
		// KnowtatorIO kio = this.moonstoneRuleInterface.getWorkbench()
		// .getAnalysis().getKnowtatorIO();
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				sis = VUtils.append(sis, extractKTSimpleInstances(annotation));
			}
		}
		return sis;
	}

	Vector<KTSimpleInstance> extractKTSimpleInstances(Annotation annotation) {
		int x = 1;
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		Object concept = annotation.getConcept();
		String semanticType = rd.extractSchemaValueFromMoonstoneConcept(concept, true);

		String attributeName = rd.getRelevantTypeAttribute(semanticType);
		String attributeValue = rd.extractSchemaValueFromMoonstoneConcept(concept, false);

		if (attributeName != null && attributeValue != null) {
			int start = annotation.getTextStart();
			int end = annotation.getTextEnd();
			String datestr = getEHostDateString();
			String text = annotation.getText();

			try {
				String substr = annotation.getDocument().getText().substring(start, end + 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			text = StrUtils.removeNonAlphaDigitSpaceCharacters(text);

			KTAnnotation kta = new KTAnnotation(getEHostInstanceID(), annotation.getText(), annotation.getTextStart(),
					annotation.getTextEnd() + 1, datestr);
			KTClassMention cm = new KTClassMention(getEHostInstanceID(), semanticType, text, kta);
			kta.setAnnotatedMentionID(cm.getID());
			KTStringSlotMention ssm = new KTStringSlotMention(getEHostInstanceID(), attributeName, attributeValue);
			cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());

			sis = VUtils.add(sis, kta);
			sis = VUtils.add(sis, ssm);
			sis = VUtils.add(sis, cm);

			TypeSystem ts = annotation.getMoonstoneRuleInterface().getWorkbench().getAnalysis().getTypeSystem();
			if (annotation.hasProperties()) {
				for (String property : annotation.getPropertyNames()) {
					Attribute attr = ts.getAttribute(property);
					if (attr != null || rd.isAttributeRelevant(property)) {
						Object value = annotation.getProperty(property);
						ssm = new KTStringSlotMention(getEHostInstanceID(), property, value.toString());
						cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());
						sis = VUtils.add(sis, ssm);
					}
				}
			}
		}
		return sis;
	}

	Vector<KTSimpleInstance> extractKTSimpleInstancesWithComplexMentions(Annotation annotation) throws Exception {
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		Object concept = annotation.getConcept();
		String semanticType = rd.extractSchemaValueFromMoonstoneConcept(concept, true);
		String attributeName = rd.getRelevantTypeAttribute(semanticType);
		Object attributeValue = rd.extractSchemaValueFromMoonstoneConcept(concept, false);
		if (attributeName != null && attributeValue != null) {
			int start = annotation.getTextStart();
			int end = annotation.getTextEnd();
			String datestr = getEHostDateString();
			String text = annotation.getText();
			text = StrUtils.removeNonAlphaDigitSpaceCharacters(text);
			KTAnnotation kta = new KTAnnotation(getEHostInstanceID(), annotation.getText(), annotation.getTextStart(),
					annotation.getTextEnd(), datestr);

			KTClassMention cm = new KTClassMention(getEHostInstanceID(), semanticType, (String) attributeValue, kta);
			sis = VUtils.add(sis, cm);
			sis = VUtils.add(sis, kta);

			if (attributeValue instanceof Annotation) {
				Annotation child = (Annotation) attributeValue;
				Vector<KTSimpleInstance> csis = extractKTSimpleInstancesWithComplexMentions(child);
				if (csis != null) {
					KTClassMention ccm = (KTClassMention) csis.firstElement();
					// KTComplexSlotMention cplxsm = new KTComplexSlotMention();
					// cplxsm.complexSlotClassMention = ccm;
					// cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs,
					// cplxsm.getID());
					// sis = VUtils.add(sis, cplxsm);
				}
			} else {
				KTStringSlotMention ssm = new KTStringSlotMention(getEHostInstanceID(), attributeName,
						(String) attributeValue);
				kta.setAnnotatedMentionID(ssm.getID());
				cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());
				sis = VUtils.add(sis, ssm);
			}

		}
		return sis;
	}

	// Thu Jan 31 15:54:18 MST 2013

	public String getEHostDateString() {
		Calendar c = Calendar.getInstance();
		Date date = new Date();
		c.setTime(date);
		return c.getTime().toString();
	}

	public String getEHostInstanceID() {
		String rv = "EHOST_Instance_" + this.EHostID++;
		rv = rv.trim();
		return rv;
	}

}

package workbench.api.input.knowtator;

import workbench.api.Analysis;

public class Knowtator {

	public static KnowtatorIO createKnowtatorIO(Analysis analysis) {
		KnowtatorIO kio = new KnowtatorIO(analysis, null, null);
		return kio;
	}

	public static void readSchema(KnowtatorIO kio, String str, boolean isXML)
			throws Exception {
		kio.extractSchema(str, isXML);
	}

	public static void readAnnotationFile(KnowtatorIO kio, String docname, String fstr,
			boolean isXML) throws Exception {
		if (isXML) {
			kio.setXMLFormat(KnowtatorIO.SHARPXMLFormat);
			kio.extractAnnotationsFromXMLString(docname, fstr);
			kio.resolveReferences();
			kio.clearSimpleInstances();
		} else {
			kio.extractAnnotationsFromPinsFileString(fstr);
		}
	}

	public static void postProcess(KnowtatorIO kio, Analysis analysis)
			throws Exception {
		kio.createWorkbenchAnnotations(analysis);
		
		// I don't use Document objects, so no need to attach to ACs?
//		analysis.attachDocumentsToKnowtatorAnnotationCollections(analysis
//				.getPrimaryAnnotator());
//		analysis.attachDocumentsToKnowtatorAnnotationCollections(analysis
//				.getSecondaryAnnotator());
	}
	
}

package moonstone.rulebuilder;

public class TSLInterface {

	public static tsl.documentanalysis.document.Document createTSLDocument(
			tsl.documentanalysis.document.Document tdoc) {
		tsl.documentanalysis.document.Document adoc = new tsl.documentanalysis.document.Document();
		adoc.setText(tdoc.getText());
		adoc.setAbsoluteFilePath(tdoc.getAbsoluteFilePath());
		adoc.setName(tdoc.getName());
		return adoc;
	}

}

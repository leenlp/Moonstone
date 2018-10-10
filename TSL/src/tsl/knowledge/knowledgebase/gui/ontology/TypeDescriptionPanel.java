package tsl.knowledge.knowledgebase.gui.ontology;

import java.awt.Dimension;

import javax.swing.JPanel;

public class TypeDescriptionPanel extends JPanel {
	private OntologyModeler ontologyModeler = null;
	
	public TypeDescriptionPanel(OntologyModeler om) {
		this.ontologyModeler = om;
		Dimension minimumSize = new Dimension(400, 600);
		this.setMinimumSize(minimumSize);
		
	}

}

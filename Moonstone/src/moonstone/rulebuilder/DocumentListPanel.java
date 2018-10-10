package moonstone.rulebuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tsl.documentanalysis.document.Document;

public class DocumentListPanel extends JPanel implements ListSelectionListener {

	Vector<Document> documents = null;
	DefaultListModel listModel = null;
	JList jlist = null;
	MoonstoneRuleInterface msri = null;
	JFrame frame = null;

	public DocumentListPanel(MoonstoneRuleInterface msri,
			Vector<Document> documents) {
		this.msri = msri;
		listModel = new DefaultListModel();
		this.documents = documents;
		this.jlist = new JList(listModel);
		for (Document d : documents) {
			String dname = d.getName();
			listModel.addElement(dname);
		}
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setSelectedIndex(0);
		jlist.addListSelectionListener(this);
		JScrollPane jsp = new JScrollPane(jlist);
		
		Dimension d = new Dimension(400, 200);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		
		this.add(jsp);
		
		this.frame = new JFrame();
		this.frame.setContentPane(this);
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
	}
	
	public void valueChanged(ListSelectionEvent e) {
		int index = jlist.getSelectedIndex();
		Document d = this.documents.elementAt(index);
		msri.setDisplayedDocument(d);
		this.frame.dispose();
	}

	public JFrame getFrame() {
		return frame;
	}

}

package workbench.api.tsl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;

public class QueryPanel extends JPanel implements ActionListener {
	private JTextField questionPane = null;
	private EvaluationWorkbench arrTool = null;
	
	public QueryPanel(EvaluationWorkbench arrTool) {
		this.arrTool = arrTool;
		this.questionPane = new JTextField(80);
		this.questionPane.addActionListener(this);
		this.add(this.questionPane);
	}
	
	public static void displayQueryPanel(EvaluationWorkbench arrTool) {
		JFrame frame = new JFrame("TSLQuery");
		QueryPanel queryPanel = new QueryPanel(arrTool);
		frame.setContentPane(queryPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			AnnotationAnalysis analysis = this.arrTool.getAnalysis();
			String qstr = this.questionPane.getText();
			// 11/2/2014:  Deactivated.
//			if (qstr != null && analysis.getTslInterface() != null) {
//				analysis.getTslInterface().doQueryFromString(qstr);
//				arrTool.getDocumentPane().highlightSentencesTSLQuery(true);
//			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
}

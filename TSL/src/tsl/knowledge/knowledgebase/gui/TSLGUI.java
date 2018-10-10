package tsl.knowledge.knowledgebase.gui;

import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import tsl.expression.form.sentence.Sentence;

public class TSLGUI extends JPanel {
	private JTextArea textArea = null;
	private Sentence sentence = null;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new TSLGUI();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	public TSLGUI() {
		Dimension dim = new Dimension(1200, 600);
		this.textArea = new JTextArea();
		this.textArea.setMinimumSize(dim);
		this.textArea.setPreferredSize(dim);
		dim = new Dimension(600, 200);
		JScrollPane jsp = new JScrollPane(this.textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(dim);
		jsp.setMinimumSize(dim);
		this.add(jsp);
		
		JFrame frame = new JFrame();
		frame.setContentPane(this);
		frame.setTitle("TSL Sentence Editor");
		frame.pack();
		frame.setVisible(true);
	}

	public Sentence getSentence() {
		Sentence sentence = null;
		try {
			String text = this.textArea.getText();
			this.sentence = Sentence.createSentence(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.sentence;
	}
	
	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
		String tstr = sentence.toNewlinedString();
		this.textArea.setText(tstr);
	}
	

}

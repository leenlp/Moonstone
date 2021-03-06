package workbench.api.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import tsl.utilities.VUtils;
import workbench.api.WorkbenchAPIObject;

public class ObjectSelectionList extends JDialog implements ActionListener {
	private JList list = null;
	private JFrame frame = null;

	public ObjectSelectionList(WorkbenchAPIObject wao) {
		Vector values = wao.getAlternativeValues();
		Object[] array = new String[] { "hello", "goodbye" };
		if (values != null) {
			array = VUtils.vectorToArray(values);
		}
		JScrollPane jsp = new JScrollPane(list);
		jsp.setPreferredSize(new Dimension(250, 80));
		jsp.setAlignmentX(LEFT_ALIGNMENT);
		this.add(jsp);
		JButton button = new JButton("Done");
		button.addActionListener(this);
		add(button);
		JFrame frame = new JFrame();
		frame.setContentPane(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();
//		if ("Done".equals(cmd)) {
//			Object value = this.list.getSelectedValue();
//			&&&& WHAT TO DO WITH THIS??  &&&&
//			this.frame.dispose();
//		}
	}

}

package commit_task_visualization.task_visualization.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;
import commit_task_visualization.task_visualization.VisualizationConstants;
import prefuse.visual.VisualItem;

public class TaskElementDialog {

	private RSyntaxTextArea pastCodeView;
	private RSyntaxTextArea curCodeView;
	private JTable jtable;
	private JTextField commitIDtextField;
	private JScrollPane scrollPane;
	private Highlighter curCVhighlighter;
	private Highlighter pastCVhighlighter;
	private Color PAST_CODE_COLOR = Color.PINK;
	private Color CURRENT_CODE_COLOR = Color.GREEN;
	private JFrame teDialog;
	private JPanel tablePanel;

	public TaskElementDialog() {
		teDialog = new JFrame();
		teDialog.setPreferredSize(new Dimension(900, 600));
		teDialog.setMinimumSize(new Dimension(650, 450));
		teDialog.setLayout(new BorderLayout());
//		teDialog.setResizable(false);

		JPanel commitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 0));
		Box commitPanelBox = Box.createHorizontalBox();
		JLabel commitLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		commitLabel.setPreferredSize(new Dimension(80, 25));
		commitLabel.setText("Commit ID");
		commitLabel.setFont(new Font("Serif", Font.BOLD, 16));
		commitPanelBox.add(commitLabel);

		commitIDtextField = new JTextField();
		commitIDtextField.setPreferredSize(new Dimension(450, 25));
		commitIDtextField.setEditable(false);
		commitPanelBox.add(commitIDtextField);
		commitPanel.add(commitPanelBox);

		JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
		codePanel.setPreferredSize(new Dimension(450, 250));
		Box codePanelBox = Box.createHorizontalBox();

		Box pastCodeBox = Box.createVerticalBox();
		pastCodeBox.setAlignmentX(Box.TOP_ALIGNMENT);
		JLabel pastCodeLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		pastCodeLabel.setPreferredSize(new Dimension(60, 25));
		pastCodeLabel.setText("Past Code");
		pastCodeLabel.setFont(new Font("Serif", Font.BOLD, 14));
		pastCodeBox.add(pastCodeLabel);

		pastCodeView = new RSyntaxTextArea();
		pastCodeView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		pastCodeView.setCodeFoldingEnabled(true);
		pastCodeView.setAutoscrolls(true);
		pastCodeView.setEditable(false);
		pastCVhighlighter = pastCodeView.getHighlighter();
		RTextScrollPane pastCodeViewSP = new RTextScrollPane(pastCodeView);
		pastCodeViewSP.setPreferredSize(new Dimension(450, 300));
		pastCodeBox.add(pastCodeViewSP);

		codePanelBox.add(pastCodeBox);
		codePanelBox.add(Box.createVerticalStrut(2));

		codePanelBox.add(Box.createRigidArea(new Dimension(20, 0)));

		Box curCodeBox = Box.createVerticalBox();
		curCodeBox.setAlignmentX(Box.TOP_ALIGNMENT);
		JLabel curCodeLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		curCodeLabel.setPreferredSize(new Dimension(60, 25));
		curCodeLabel.setText("Current Code");
		curCodeLabel.setFont(new Font("Serif", Font.BOLD, 14));
		curCodeBox.add(curCodeLabel);

		curCodeView = new RSyntaxTextArea();
		curCodeView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		curCodeView.setCodeFoldingEnabled(true);
		curCodeView.setAutoscrolls(true);
		curCodeView.setEditable(false);
		curCVhighlighter = curCodeView.getHighlighter();
		codePanelBox.add(curCodeView);
		RTextScrollPane curCodeViewSP = new RTextScrollPane(curCodeView);
		curCodeViewSP.setPreferredSize(new Dimension(450, 300));
		curCodeBox.add(curCodeViewSP);

		codePanelBox.add(curCodeBox);
		codePanelBox.add(Box.createVerticalStrut(2));
		codePanel.add(codePanelBox);

		teDialog.add(commitPanel, BorderLayout.NORTH);
		teDialog.add(codePanel, BorderLayout.CENTER);

		tablePanel = new JPanel();
		jtable = new JTable();
		JTableHeader header = jtable.getTableHeader();
		header.setReorderingAllowed(false);
		header.setFont(new Font("Serif", Font.BOLD, 16));
		scrollPane = new JScrollPane(jtable);
		scrollPane.setPreferredSize(new Dimension(900, 300));
		tablePanel.add(scrollPane);
	}

	public void drawDialog(TaskElement te, int x, int y){
		TaskElementUtil teUtil = new TaskElementUtil();
		teUtil.setTaskElement(te);
		String CommitID = teUtil.getElementID();
		String pastCode = teUtil.getPastCode();
		String currentCode = teUtil.getCurrentCode();
		List<TaskStatement> stmts = teUtil.getStatements();
		commitIDtextField.setText(CommitID);

		pastCodeView.setText(pastCode);
		curCodeView.setText(currentCode);
		if (stmts.size() != 0) {
			jtable.removeAll();
			StatementTableModel tableModel = new StatementTableModel(stmts);
			jtable.setModel(tableModel);
			teDialog.add(tablePanel, BorderLayout.SOUTH);

			jtable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					pastCVhighlighter.removeAllHighlights();
					curCVhighlighter.removeAllHighlights();

					Object obj = jtable.getValueAt(jtable.getSelectedRow(), jtable.getSelectedColumn());
					String selectedValue = null;
					if (obj != null)
						selectedValue = obj.toString();
					if (selectedValue != null) {
						if (pastCodeView.getText().contains(selectedValue)) {
							String text = pastCodeView.getText();
							int index = text.indexOf(selectedValue);
							DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(
									PAST_CODE_COLOR);
							try {
								pastCVhighlighter.addHighlight(index, index + selectedValue.length(), painter);
							} catch (BadLocationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						if (curCodeView.getText().contains(selectedValue)) {
							String text = curCodeView.getText();
							int index = text.indexOf(selectedValue);
							DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(
									CURRENT_CODE_COLOR);
							try {
								curCVhighlighter.addHighlight(index, index + selectedValue.length(), painter);
							} catch (BadLocationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			});
		} else {
			teDialog.remove(tablePanel);
		}
		teDialog.setLocation((int) x, (int) y);
		teDialog.setVisible(true);
		teDialog.revalidate();
		teDialog.pack();
		teDialog.repaint();
	}

}
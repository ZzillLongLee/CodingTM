package commit_task_visualization.causal_relationship_visualization.task_element_diff_visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

import commit_task_visualization.causal_relationship_visualization.CausalRelationshipVisualizer;
import commit_task_visualization.causal_relationship_visualization.TaskVisualizerUtil;
import commit_task_visualization.causal_relationship_visualization.VisualizationConstants;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;

public class TaskElementDiffDialog {

	private JPanel listPane;
	private RSyntaxTextArea pastCodeView;
	private RSyntaxTextArea curCodeView;
	private JTable jtable;
	private JTextField commitIDtextField;
	private JTextField causedByTextField;
	private JScrollPane scrollPane;
	private Highlighter curCVhighlighter;
	private Highlighter pastCVhighlighter;
	private Color PAST_CODE_COLOR = Color.PINK;
	private Color CURRENT_CODE_COLOR = Color.GREEN;
	private JFrame teDialog;
	private final String Deleted_ChangeType = "(Deleted)";
	private final String MODIFIED_ChangeType = "(Modified)";
	private final String ADDED_ChangeType = "(Added)";

	public TaskElementDiffDialog() {
		teDialog = new JFrame();
		teDialog.setPreferredSize(new Dimension(900, 600));
		teDialog.setMinimumSize(new Dimension(650, 450));
		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(Box.createVerticalGlue());
//		teDialog.setResizable(false);

		// Why don't we aggregate all the UI elements in a single panel by using box
		Box causedByPanelBox = Box.createHorizontalBox();
		JLabel causedByLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		causedByLabel.setPreferredSize(new Dimension(80, 25));
		causedByLabel.setText("Caused By");
		causedByLabel.setFont(new Font("Serif", Font.BOLD, 16));
		causedByPanelBox.add(causedByLabel);

		causedByTextField = new JTextField();
		causedByTextField.setPreferredSize(new Dimension(450, 25));
		causedByTextField.setEditable(false);
		causedByPanelBox.add(causedByTextField);

		Box commitPanelBox = Box.createHorizontalBox();
		JLabel commitLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		commitLabel.setPreferredSize(new Dimension(100, 25));
		commitLabel.setText("Task Element");
		commitLabel.setFont(new Font("Serif", Font.BOLD, 16));
		commitPanelBox.add(commitLabel);

		commitIDtextField = new JTextField();
		commitIDtextField.setPreferredSize(new Dimension(450, 25));
		commitIDtextField.setEditable(false);
		commitPanelBox.add(commitIDtextField);

		listPane.add(causedByPanelBox);
		listPane.add(commitPanelBox);

		Box codePanelBox = Box.createHorizontalBox();

		Box pastCodeBox = Box.createVerticalBox();
		pastCodeBox.setAlignmentX(Box.TOP_ALIGNMENT);
		JLabel pastCodeLabel = new JLabel(VisualizationConstants.COMMIT_ID_LABEL);
		pastCodeLabel.setPreferredSize(new Dimension(60, 25));
		pastCodeLabel.setText("Code of N-1th Version");
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
		curCodeLabel.setText("Code of Nth Version");
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
//		codePanel.add(codePanelBox);

		listPane.add(codePanelBox);

		jtable = new JTable();
		JTableHeader header = jtable.getTableHeader();
		header.setReorderingAllowed(false);
		header.setFont(new Font("Serif", Font.BOLD, 16));
		scrollPane = new JScrollPane(jtable);
		scrollPane.setPreferredSize(new Dimension(900, 300));
//		jtable.add(scrollPane);
	}

	public void drawDialog(TaskElement te, int x, int y, int viewType) {
		TaskElementUtil teUtil = new TaskElementUtil();
		teUtil.setTaskElement(te);
		String taskElementLabel = TaskVisualizerUtil.getLabel(teUtil.getElementID());
		String packagePath = TaskVisualizerUtil.getPackagePath(teUtil.getElementID());
		String taskElementID = packagePath + VisualizationConstants.SPLITMARK + taskElementLabel;
		String pastCode = teUtil.getPastCode();
		String currentCode = teUtil.getCurrentCode();
		List<TaskStatement> stmts = teUtil.getStatements();
		commitIDtextField.setText(taskElementID);
		List<TaskElement> causedByList = te.getCausedBy();
		StringBuilder causedByAsString = new StringBuilder();
		int causByListSize = causedByList.size();
		for (int i = 0; i < causByListSize; i++) {
			teUtil.setTaskElement(causedByList.get(i));
			packagePath = TaskVisualizerUtil.getPackagePath(teUtil.getElementID());
			taskElementLabel = TaskVisualizerUtil.getLabel(teUtil.getElementID());
			String causedByID = packagePath + VisualizationConstants.SPLITMARK + taskElementLabel;
			if (i != causByListSize - 1)
				causedByAsString.append(causedByID + "\n");
			else
				causedByAsString.append(causedByID);
		}
		causedByTextField.setText(causedByAsString.toString());

		pastCodeView.setText(pastCode);
		curCodeView.setText(currentCode);
		if (stmts.size() != 0) {
			jtable.removeAll();
			StatementTableModel tableModel = new StatementTableModel(stmts);
			jtable.setModel(tableModel);
			listPane.add(scrollPane);

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
						selectedValue = removeChangeType(selectedValue);
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

				private String removeChangeType(String selectedValue) {
					if (selectedValue.contains(ADDED_ChangeType)) {
						return selectedValue.replace(ADDED_ChangeType, "");
					}
					if (selectedValue.contains(Deleted_ChangeType)) {
						return selectedValue.replace(Deleted_ChangeType, "");
					}
					if (selectedValue.contains(MODIFIED_ChangeType)) {
						return selectedValue.replace(MODIFIED_ChangeType, "");
					}
					return selectedValue;
				}
			});
		} else {
			teDialog.remove(jtable);
		}
		teDialog.add(listPane);
		if(viewType == CausalRelationshipVisualizer.aggregationView)
			teDialog.setLocation((int) x, (int) y);
		teDialog.setVisible(true);
		teDialog.revalidate();
		teDialog.pack();
		teDialog.repaint();
	}

}
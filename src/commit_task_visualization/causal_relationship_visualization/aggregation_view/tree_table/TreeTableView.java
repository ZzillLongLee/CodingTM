package commit_task_visualization.causal_relationship_visualization.aggregation_view.tree_table;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import commit_task_visualization.causal_relationship_visualization.CausalRelationshipVisualizer;
import commit_task_visualization.causal_relationship_visualization.VisualizationConstants;
import commit_task_visualization.causal_relationship_visualization.task_element_diff_visualization.TaskElementDiffDialog;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import prefuse.data.Node;

public class TreeTableView extends JScrollPane {

	private CausalLinkTreeGenerator causedToTreeGen;
	private CausalLinkTreeGenerator cuasedByTreeGen;
	private TaskElementDiffDialog teDialog;

	public TreeTableView() {
		// TODO Auto-generated constructor stub
	}

	public Box buildTreeTableView(Box treeTableBox, Node node) {
		// show treeTable for causally linked task elements.
		if (treeTableBox != null) {
			treeTableBox.removeAll();
			treeTableBox.setVisible(false);
		}
		treeTableBox = new Box(BoxLayout.X_AXIS);
		Box causedToBox = Box.createVerticalBox();
		causedToBox.setAlignmentX(Box.TOP_ALIGNMENT);
		causedToBox.setBorder(VisualizationConstants.BorderType_raisedetched);
		JLabel causedToLabel = new JLabel();
		causedToLabel.setPreferredSize(new Dimension(60, 30));
		causedToLabel.setText("It caused to: \r\n" + "The element referenced by this code ");
		causedToLabel.setFont(new Font("Serif", Font.BOLD, 14));
		causedToBox.add(causedToLabel);
		JScrollPane causedTottv = showTreeTableView(node, VisualizationConstants.CAUSEDTO);
		causedTottv.setPreferredSize(new Dimension(30, 120));
		causedToBox.add(causedTottv);

		Box CausedByBox = Box.createVerticalBox();
		CausedByBox.setAlignmentX(Box.TOP_ALIGNMENT);
		CausedByBox.setBorder(VisualizationConstants.BorderType_raisedetched);
		JLabel causedByLabel = new JLabel();
		causedByLabel.setPreferredSize(new Dimension(60, 30));
		causedByLabel.setText("It is caused by: \r\n" + "Elements referencing this code");
		causedByLabel.setFont(new Font("Serif", Font.BOLD, 14));
		CausedByBox.add(causedByLabel);
		JScrollPane causedByttv = showTreeTableView(node, VisualizationConstants.CAUSEDBY);
		causedByttv.setPreferredSize(new Dimension(30, 120));
		CausedByBox.add(causedByttv);

		treeTableBox.add(CausedByBox);
		treeTableBox.add(causedToBox);
		treeTableBox.setVisible(true);
		return treeTableBox;
	}

	public JScrollPane showTreeTableView(Node node, int linkType) {
		JTree tree = null;
		DefaultMutableTreeNode causalToTree = null;
		DefaultMutableTreeNode causalByTree = null;
		if (node == null)
			return new JScrollPane();
		if (linkType == VisualizationConstants.CAUSEDTO) {
			causedToTreeGen = new CausalLinkTreeGenerator();
			causalToTree = causedToTreeGen.generateCausalLinkTree(node, VisualizationConstants.CAUSEDTO);
			tree = new JTree(causalToTree);
			setTreeTableListener(tree);
			return new JScrollPane(tree);
		}
		if (linkType == VisualizationConstants.CAUSEDBY) {
			cuasedByTreeGen = new CausalLinkTreeGenerator();
			causalByTree = cuasedByTreeGen.generateCausalLinkTree(node, VisualizationConstants.CAUSEDBY);
			tree = new JTree(causalByTree);
			setTreeTableListener(tree);
			return new JScrollPane(tree);
		} else
			return new JScrollPane();
	}

	private void setTreeTableListener(JTree tree) {
		teDialog = new TaskElementDiffDialog();
		tree.addMouseListener(new MouseAdapter() {
			 public void mousePressed(MouseEvent e) {
				 int clickCount = e.getClickCount();
				 if(clickCount == VisualizationConstants.DOUBLE_CLICK) {
					 Object obj = tree.getSelectionPath().getLastPathComponent();
					 if(obj instanceof DefaultMutableTreeNode) {
						 DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)obj;
							Rectangle nodeBounds = tree.getPathBounds(new TreePath(selectedNode.getPath()));
							TaskElement causedToTaskElement = causedToTreeGen.getTaskElement(selectedNode);
							if (causedToTaskElement != null) {
								try {
									teDialog.drawDialog(causedToTaskElement, nodeBounds.x, nodeBounds.y, CausalRelationshipVisualizer.aggregationView);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							TaskElement causedByTaskElement = cuasedByTreeGen.getTaskElement(selectedNode);
							if (causedByTaskElement != null) {
								try {
									teDialog.drawDialog(causedByTaskElement, nodeBounds.x, nodeBounds.y, CausalRelationshipVisualizer.aggregationView);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
					 }
				 }
			 }
		});
	}

}
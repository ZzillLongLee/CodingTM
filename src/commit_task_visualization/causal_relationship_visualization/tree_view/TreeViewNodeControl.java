package commit_task_visualization.causal_relationship_visualization.tree_view;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import commit_task_visualization.causal_relationship_visualization.CausalRelationshipVisualizer;
import commit_task_visualization.causal_relationship_visualization.VisualizationConstants;
import commit_task_visualization.causal_relationship_visualization.task_element_diff_visualization.TaskElementDiffDialog;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.visual.VisualItem;

public class TreeViewNodeControl extends ControlAdapter {

	private TaskElementDiffDialog taskElementDialog;

	public void itemPressed(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (item instanceof Node && e.getClickCount() == VisualizationConstants.DOUBLE_CLICK) {
			TaskElement taskElement = (TaskElement) item.get(VisualizationConstants.TASKELEMENT);
			taskElementDialog = new TaskElementDiffDialog();
			taskElementDialog.drawDialog(taskElement, 0, 0, CausalRelationshipVisualizer.treeView);
		}
	}

}

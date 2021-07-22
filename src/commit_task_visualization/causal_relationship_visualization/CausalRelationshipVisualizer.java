package commit_task_visualization.causal_relationship_visualization;

import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import org.eclipse.swt.widgets.Composite;

import commit_task_visualization.causal_relationship_visualization.aggregation_view.AggregationTypeCRVisualizer;
import commit_task_visualization.causal_relationship_visualization.model.CommitData;
import commit_task_visualization.causal_relationship_visualization.tree_view.TreeTypeCRVisualizer;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.development_task_list_visualization.util.TaskFinder;
import commit_task_visualization.topological_sort.CausalRelationshipGraph;

public class CausalRelationshipVisualizer{

	public final static int treeView = 1;
	public final static int aggregationView = 2;
	private JPanel viewPanel;
	
	public CausalRelationshipVisualizer(CommitData cd, String targetTaskElementID, int viewType) {
		List<List<TaskElement>> taskList = cd.getTaskList();
		HashMap<String, TaskElement> taskElementHashmap = cd.getTaskElementHashmap();
		CausalRelationshipGraph graph = TaskFinder.findTask(cd, targetTaskElementID);
		String curCommitID = cd.getCommitID();
		String prevCommitID = cd.getPrevCommitID();
		if(viewType == treeView) {
			TreeTypeCRVisualizer treeTypeCR = new TreeTypeCRVisualizer(cd, graph);
			viewPanel = treeTypeCR.showTask();
		}
		if(viewType == aggregationView) {
			AggregationTypeCRVisualizer agreeTypeCR = new AggregationTypeCRVisualizer(taskElementHashmap, taskList, curCommitID, prevCommitID);
			viewPanel = agreeTypeCR.showTask();
		}
	}
	
	public JPanel showTask() {
		return viewPanel;
	}
	
}

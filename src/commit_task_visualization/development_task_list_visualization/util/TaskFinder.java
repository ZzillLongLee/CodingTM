package commit_task_visualization.development_task_list_visualization.util;

import java.util.List;

import commit_task_visualization.causal_relationship_visualization.model.CommitData;
import commit_task_visualization.topological_sort.CausalRelationshipGraph;

public class TaskFinder {

	public static CausalRelationshipGraph findTask(CommitData cd, String targetTaskElement) {
		List<CausalRelationshipGraph> sortedTasks = cd.getSortedTasks();
		for (CausalRelationshipGraph causalRelationshipGraph : sortedTasks) {
			List<String> taskElements = causalRelationshipGraph.getSortedTaskElements();
			for (String taskElement : taskElements) {
				if(taskElement.equals(targetTaskElement))
					return causalRelationshipGraph;
			}
		}
		return null;
	}
}

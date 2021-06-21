package commit_task_visualization.causal_relationship_visualization;

import java.util.ArrayList;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import prefuse.data.Node;

public class TaskElementNodeRepo {
	private static TaskElementNodeRepo ISTNACE = null;
	private static List<Node> taskElementNodes;

	public static TaskElementNodeRepo getInstance() {
		if (ISTNACE == null) {
			ISTNACE = new TaskElementNodeRepo();
			taskElementNodes = new ArrayList<Node>();
		}
		return ISTNACE;
	}

	
	public static List<Node> getTaskElementNodes() {
		return taskElementNodes;
	}


	public Node getNode(String taskElementID) {
		for (Node node : taskElementNodes) {
			Object obj = node.get("TaskElement");
			if(obj instanceof TaskElement) {
				TaskElement te = (TaskElement)obj;
				String teID = te.getTaskElementID();
				if(taskElementID.equals(teID))
					return node;
			}
		}
		return null;
	}
	
	public void emptyNodes() {
		taskElementNodes.clear();
	}
}

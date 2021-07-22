package commit_task_visualization.topological_sort;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;

public class CausalRelationship {

	private TaskElement causedToElement;
	private TaskElement cuasedByElement;

	public CausalRelationship(TaskElement cuasedByElement, TaskElement causedToElement) {
		this.causedToElement = causedToElement;
		this.cuasedByElement = cuasedByElement;
	}

	public TaskElement getCausedToElement() {
		return causedToElement;
	}

	public TaskElement getCuasedByElement() {
		return cuasedByElement;
	}
}

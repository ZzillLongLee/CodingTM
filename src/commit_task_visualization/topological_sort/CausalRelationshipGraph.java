package commit_task_visualization.topological_sort;

import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;

public class CausalRelationshipGraph {

	private int numberOfCausalRelationship;
	private List<String> sortedTaskElements;

	public CausalRelationshipGraph(int numberOfCausalRelationship, List<String> sortedTaskElements) {
		this.numberOfCausalRelationship = numberOfCausalRelationship;
		this.sortedTaskElements= sortedTaskElements;
	}

	public int getNumberOfCausalRelationship() {
		return numberOfCausalRelationship;
	}

	public List<String> getSortedTaskElements() {
		return sortedTaskElements;
	}

}

package commit_task_visualization.topological_sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementRepo;


public class CausalRelationshipOrderer {

	private ArrayList<CausalRelationshipGraph> graphs;

	public CausalRelationshipOrderer() {
		graphs = new ArrayList<CausalRelationshipGraph>();
	}

	public List<CausalRelationshipGraph> generate(List<List<TaskElement>> taskList, TaskElementRepo taskElementRepo) {
		for (List<TaskElement> task : taskList) {
			if (task.size() > 0) {
				List<CausalRelationship> causalRelationships = new ArrayList<CausalRelationship>();
				for (TaskElement taskElement : task) {
					String taskElementID = taskElement.getTaskElementID();
					TaskElement searchedElement = taskElementRepo.getTaskElement(taskElementID);
					List<TaskElement> causedToRelationships = searchedElement.getCausedTo();
					for (TaskElement causedToTaskElement : causedToRelationships) {
						causalRelationships.add(new CausalRelationship(searchedElement, causedToTaskElement));
					}
				}
				TopologicalGraph graph = new TopologicalGraph(task.size());
				for (CausalRelationship causalRelationship : causalRelationships) {
					graph.addEdge(task.indexOf(causalRelationship.getCuasedByElement()),
							task.indexOf(causalRelationship.getCausedToElement()));
				}
				Stack<Integer> sortedValue = graph.topologicalSort();
				List<String> encodedValue = encodeSortValue(sortedValue, task);
				CausalRelationshipGraph crGraph = new CausalRelationshipGraph(causalRelationships.size(), encodedValue);
				graphs.add(crGraph);
			}
		}
		return graphs;
	}

	private List<String> encodeSortValue(Stack<Integer> sortedValue, List<TaskElement> task) {
		List<String> sortedCausalRelationships = new ArrayList<String>();
		while (!sortedValue.empty()) {
			int elementIdx = sortedValue.pop();
			TaskElement taskElement = task.get(elementIdx);
			sortedCausalRelationships.add(taskElement.getTaskElementID());
		}
		return sortedCausalRelationships;
	}
}

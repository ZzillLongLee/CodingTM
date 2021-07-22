package commit_task_visualization.causal_relationship_visualization.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.topological_sort.CausalRelationshipGraph;

public class CommitData implements Serializable {

	private String commitID;
	private String prevCommitID;
	private List<List<TaskElement>> taskList;
	private HashMap<String, TaskElement> taskElementHashmap;
	private List<CausalRelationshipGraph> sortedTasks;

	public CommitData(String commitID, String prevCommitID, List<List<TaskElement>> taskList,
			HashMap<String, TaskElement> taskElementHashmap, List<CausalRelationshipGraph> graphs) {
		this.commitID = commitID;
		this.prevCommitID = prevCommitID;
		this.taskList = taskList;
		this.taskElementHashmap = taskElementHashmap;
		this.sortedTasks = graphs;
	}
	
	public String getCommitID() {
		return commitID;
	}

	
	public String getPrevCommitID() {
		return prevCommitID;
	}

	public List<List<TaskElement>> getTaskList() {
		return taskList;
	}

	public HashMap<String, TaskElement> getTaskElementHashmap() {
		return taskElementHashmap;
	}

	public List<CausalRelationshipGraph> getSortedTasks() {
		return sortedTasks;
	}

}

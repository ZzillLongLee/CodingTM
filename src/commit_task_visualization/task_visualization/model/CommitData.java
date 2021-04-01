package commit_task_visualization.task_visualization.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;

public class CommitData implements Serializable{

	private List<List<TaskElement>> taskList;
	private HashMap<String, TaskElement> taskElementHashmap;

	public CommitData(List<List<TaskElement>> taskList, HashMap<String, TaskElement> taskElementHashmap) {
		this.taskList = taskList;
		this.taskElementHashmap = taskElementHashmap;
	}

	public List<List<TaskElement>> getTaskList() {
		return taskList;
	}

	public HashMap<String, TaskElement> getTaskElementHashmap() {
		return taskElementHashmap;
	}

}

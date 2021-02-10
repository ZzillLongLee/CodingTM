package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {

	private String commitID;

	private List<TaskClass> classes;
	
	private List<TaskElement> taskTrees;

	public Task(String commitID, List<TaskClass> classes) {
		this.commitID = commitID;
		this.classes = classes;
	}

	public List<TaskClass> getClasses() {
		return classes;
	}

	public String getCommitID() {
		return commitID;
	}

	public List<TaskElement> getTaskTrees() {
		return taskTrees;
	}

	public void setTaskTrees(List<TaskElement> taskTrees) {
		this.taskTrees = taskTrees;
	}

}

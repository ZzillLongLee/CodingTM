package commit_task_visualization.task_visualization.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;

public class CommitData implements Serializable {

	private String commitID;
	private String prevCommitID;
	private List<List<TaskElement>> taskList;
	private HashMap<String, TaskElement> taskElementHashmap;

	public CommitData(String commitID, String prevCommitID, List<List<TaskElement>> taskList,
			HashMap<String, TaskElement> taskElementHashmap) {
		this.commitID = commitID;
		this.prevCommitID = prevCommitID;
		this.taskList = taskList;
		this.taskElementHashmap = taskElementHashmap;
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

}

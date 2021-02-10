package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Taeyoung Kim
 *
 */
public class TaskElement implements Serializable {

	private static final long serialVersionUID = 6529685098267757690L;
	/**
	 * @TaskElementID: Class: commitID + packageName + className; Method: commitID +
	 *                 packageName + className + method signature Attribute:
	 *                 commitID + packageName + className + AttributeName Statement:
	 *                 commitID + packageName + className + StatementCode;
	 */
	protected String taskElementID;
	private String pastCode;
	private String currentCode;
	private String changedType;
	private List<TaskElement> causedBy = new ArrayList<TaskElement>();
	private List<TaskElement> causedTo = new ArrayList<TaskElement>();

	public TaskElement(String taskElementID, String pastCode, String currentCode, String changedType) {
		this.taskElementID = taskElementID;
		this.pastCode = pastCode;
		this.currentCode = currentCode;
		this.changedType = changedType;
	}

	public List<TaskElement> getCausedBy() {
		return causedBy;
	}

	public void setCausedBy(List<TaskElement> causedByList) {
		this.causedBy = causedByList;
	}

	public List<TaskElement> getCausedTo() {
		return causedTo;
	}

	public void setCausedTo(List<TaskElement> causedToList) {
		this.causedTo = causedToList;
	}

	public String getTaskElementID() {
		return taskElementID;
	}

	public String getPastCode() {
		return pastCode;
	}

	public String getCurrentCode() {
		return currentCode;
	}

	public String getChangedType() {
		return changedType;
	}

	public void setPastCode(String pastCode) {
		this.pastCode = pastCode;
	}

	public void setCurrentCode(String currentCode) {
		this.currentCode = currentCode;
	}

	public void setChangedType(String changedType) {
		this.changedType = changedType;
	}

}

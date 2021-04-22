package commit_task_visualization.single_task_visualization.dialog;

import java.util.ArrayList;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;
import commit_task_visualization.single_task_visualization.VisualizationConstants;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class TaskElementUtil {

	private TaskElement taskElement;

	public void setTaskElement(TaskElement taskElement) {
		this.taskElement = taskElement;
	}

	public String getCommitID() {
		return taskElement.getTaskElementID();
	}

	public List<TaskStatement> getStatements() {
		List<TaskStatement> stmts = new ArrayList<TaskStatement>();
		if (taskElement instanceof TaskMethod) {
			TaskMethod tm = (TaskMethod) taskElement;
			List<TaskStatement> statementSet = tm.getStatementsSet();
			stmts.addAll(statementSet);
		}
		return stmts;
	}

	public String getPastCode() {
		return taskElement.getPastCode();
	}

	public String getCurrentCode() {
		return taskElement.getCurrentCode();
	}

	public String getElementID() {
		String commitID = taskElement.getTaskElementID();
		String[] splitedID = commitID.split("----");
		return splitedID[1];
	}

}

package commit_task_visualization.task_visualization.dialog;

import java.util.ArrayList;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;
import commit_task_visualization.task_visualization.VisualizationConstants;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class ItemUtil {

	private static VisualItem item;

	public static void setItem(VisualItem visItem) {
		item = visItem;
	}

	public static String getCommitID() {
		String commitID = "";
		Object obj = item.get(VisualizationConstants.TASKELEMENT);
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			commitID = te.getTaskElementID();
		}
		return commitID;
	}

	public static List<TaskStatement> getStatements() {
		List<TaskStatement> stmts = new ArrayList<TaskStatement>();
		Object obj = item.get(VisualizationConstants.TASKELEMENT);
		if (obj instanceof TaskMethod) {
			TaskMethod tm = (TaskMethod) obj;
			List<TaskStatement> statementSet = tm.getStatementsSet();
			stmts.addAll(statementSet);
		}
		return stmts;
	}

	public static String getPastCode() {
		String pastCode = "";
		Object obj = item.get(VisualizationConstants.TASKELEMENT);
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			pastCode = te.getPastCode();
		}
		return pastCode;
	}

	public static String getCurrentCode() {
		String currentCode = "";
		Object obj = item.get(VisualizationConstants.TASKELEMENT);
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			currentCode = te.getCurrentCode();
		}
		return currentCode;
	}

	public static String getElementID() {
		String commitID = "";
		Object obj = item.get(VisualizationConstants.TASKELEMENT);
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			commitID = te.getTaskElementID();
			String[] splitedID = commitID.split("----");
			commitID = splitedID[1];
		}
		return commitID;
	}

}

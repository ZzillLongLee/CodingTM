package commit_task_visualization.task_visualization.nat_table.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskAttribute;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;
import commit_task_visualization.code_change_extraction.util.Constants;
import commit_task_visualization.task_visualization.model.CommitData;
import commit_task_visualization.task_visualization.nat_table.util.NatTableConstants;

public class CommitTableDataGenerator {

	private String[] PROPERTY_NAMES = null;

	private List<CommitData> commitDataList;

	public CommitTableDataGenerator(List<CommitData> commitDataList) {
		this.commitDataList = commitDataList;
	}

	public String[] getPROPERTY_NAMES() {
		return PROPERTY_NAMES;
	}

	public void getInput(List<LinkedHashMap<String, String>> values) {
		List<String> propertyNames = new ArrayList<String>();
		propertyNames.add("Class Name");
		propertyNames.add("Task Element");
		buildInputeTableFrame(values);
		buildSubColumnData(values, propertyNames);
		PROPERTY_NAMES = propertyNames.toArray(new String[propertyNames.size()]);
	}

	private void buildInputeTableFrame(List<LinkedHashMap<String, String>> values) {
		for (CommitData commitData : commitDataList) {
			List<List<TaskElement>> commitTaskList = commitData.getTaskList();
			for (List<TaskElement> commitTask : commitTaskList) {
				for (TaskElement taskElement : commitTask) {
					LinkedHashMap<String, String> valueRow = new LinkedHashMap<>();
					String className = getClassName(taskElement);
					String taskID = taskElement.getTaskElementID();
					String idWithOutCommit = getIdWithoutCommit(taskID);
					boolean isContained = isTaskElementContained(idWithOutCommit, values);
					if (isContained == false) {
						valueRow.put(NatTableConstants.CLASS_NAME_COLUMN, className);
						valueRow.put(NatTableConstants.TASK_ELEMENT_COLUMN, idWithOutCommit);
						values.add(valueRow);
					}
				}

			}
		}
	}

	private void buildSubColumnData(List<LinkedHashMap<String, String>> values, List<String> propertyNames) {
		for (Map<String, String> map : values) {
			String targetID = map.get(NatTableConstants.TASK_ELEMENT_COLUMN);
			int commitNum = 1;
			for (CommitData commitData : commitDataList) {
				boolean isCommitInclude = false;
				List<List<TaskElement>> commitTaskList = commitData.getTaskList();
				int taskNum = 1;
				for (List<TaskElement> commitTask : commitTaskList) {
					for (TaskElement taskElement : commitTask) {
						String taskID = taskElement.getTaskElementID();
						String idWithOutCommit = getIdWithoutCommit(taskID);
						if (idWithOutCommit.equals(targetID)) {
							isCommitInclude = true;
							map.put(NatTableConstants.TASK_COLUMN + commitNum + ")", NatTableConstants.TASK_CELL_VALUE + taskNum);
							map.put(NatTableConstants.CHANGETYPE_COLUMN + commitNum + ")",
									taskElement.getChangedType());
						}
					}
					taskNum++;
				}
				if (isCommitInclude == false) {
					map.put(NatTableConstants.TASK_COLUMN + commitNum + ")", NatTableConstants.NONE_VALUE);
					map.put(NatTableConstants.CHANGETYPE_COLUMN + commitNum + ")", NatTableConstants.NONE_VALUE);
				}
				// label setting				
				if (!propertyNames.contains(NatTableConstants.CHANGETYPE_COLUMN + commitNum + ")")
						&& !propertyNames.contains(NatTableConstants.TASK_COLUMN + commitNum + ")")) {
					propertyNames.add(NatTableConstants.TASK_COLUMN + commitNum + ")");
					propertyNames.add(NatTableConstants.CHANGETYPE_COLUMN + commitNum + ")");
				}
				commitNum++;
			}
		}
	}

	private boolean isTaskElementContained(String idWithOutCommit, List<LinkedHashMap<String, String>> values) {
		for (Map<String, String> map : values) {
			String targetID = map.get(NatTableConstants.TASK_ELEMENT_COLUMN);
			if (targetID.equals(idWithOutCommit))
				return true;
		}
		return false;
	}

	private String getIdWithoutCommit(String taskID) {
		String[] splitedID = taskID.split(Constants.SEPERATOR);
		return splitedID[splitedID.length - 1];
	}

	private String getClassName(TaskElement taskElement) {
		if (taskElement instanceof TaskMethod) {
			TaskMethod tm = (TaskMethod) taskElement;
			return tm.getClassName();
		}
		if (taskElement instanceof TaskAttribute) {
			TaskAttribute ta = (TaskAttribute) taskElement;
			return ta.getClassName();
		}
		return null;
	}

}

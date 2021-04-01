package commit_task_visualization.code_change_extraction.model.task_elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;

public class TaskElementRepo {

	private HashMap<String, TaskElement> taskElementHashMap;

	public TaskElementRepo() {
		taskElementHashMap = new HashMap<String, TaskElement>();
	}
	
	public HashMap<String, TaskElement> getTaskElementHashMap() {
		return taskElementHashMap;
	}

	public TaskElement getTaskElement(String id) {
		return taskElementHashMap.get(id);
	}

	public void putTaskElement(String id, TaskElement value) {
		if (taskElementHashMap.containsKey(id))
			merge(taskElementHashMap.get(id), value);
		else
			taskElementHashMap.put(id, value);
	}

	private void merge(TaskElement taskElement, TaskElement value) {

		List<TaskElement> targetCausedByList = taskElement.getCausedBy();
		List<TaskElement> valueCausedByList = value.getCausedBy();
		List<TaskElement> mergedCausedByList = Stream.concat(targetCausedByList.stream(), valueCausedByList.stream())
				.collect(Collectors.toList());
		taskElement.setCausedBy(mergedCausedByList);

		List<TaskElement> targetCausedToList = taskElement.getCausedTo();
		List<TaskElement> valueCausedToList = value.getCausedTo();
		List<TaskElement> mergedCausedToList = Stream.concat(targetCausedToList.stream(), valueCausedToList.stream())
				.collect(Collectors.toList());
		taskElement.setCausedTo(mergedCausedToList);

	}

	public List<TaskElement> getElements(List<TaskElement> causalRelationship) {
		List<TaskElement> casualRe = new ArrayList<TaskElement>();
		for (TaskElement taskElement : causalRelationship) {
			if (taskElement != null) {
				TaskElement te = getTaskElement(taskElement.taskElementID);
				if (te == null && taskElement.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
					te = getMergedElement(taskElement);
					if (!casualRe.contains(te))
						casualRe.add(te);
				} else {
					if (!casualRe.contains(te))
						casualRe.add(te);
				}
			}
		}
		return casualRe;
	}

//	public HashMap<String, TaskElement> getElements(HashMap<String, TaskElement> causalRelationship) {
//		HashMap<String, TaskElement> casualRe = new HashMap<String, TaskElement>();
//		for (Entry<String, TaskElement> causalRelMap : causalRelationship.entrySet()) {
//			TaskElement casualValue = causalRelMap.getValue();
//			TaskElement te = getTaskElement(causalRelMap.getKey());
//			if (te == null && casualValue.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
//				te = getMergedElement(casualValue);
//				casualRe.put(te.taskElementID, te);
//			} else {
//				casualRe.put(te.taskElementID, te);
//			}
//		}
//		return casualRe;
//	}

	public TaskElement getMergedElement(TaskElement casualValue) {
		String casualValueID = casualValue.getTaskElementID();

		for (Entry<String, TaskElement> taskElementHash : taskElementHashMap.entrySet()) {
			TaskElement searchedTE = taskElementHash.getValue();
			if (searchedTE instanceof TaskMethod) {
				TaskMethod tm = (TaskMethod) searchedTE;
				String curID = tm.getPrevID();
				String prevID = tm.getCurID();
				if (curID != null && curID.equals(casualValueID))
					return searchedTE;
				if (prevID != null && prevID.equals(casualValueID))
					return searchedTE;
			}
			if (searchedTE instanceof TaskAttribute) {
				TaskAttribute ta = (TaskAttribute) searchedTE;
				String curID = ta.getPrevID();
				String prevID = ta.getCurID();
				if (curID != null && curID.equals(casualValueID))
					return searchedTE;
				if (prevID != null && prevID.equals(casualValueID))
					return searchedTE;
			}
		}
		return null;
	}
}

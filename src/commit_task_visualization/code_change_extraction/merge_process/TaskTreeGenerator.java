package commit_task_visualization.code_change_extraction.merge_process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commit_task_visualization.code_change_extraction.model.task_elements.Task;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskAttribute;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskClass;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementRepo;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;

public class TaskTreeGenerator {

	private HashMap<String, Boolean> checkMap;
	private TaskElementRepo taskRepo;

	public TaskTreeGenerator(TaskElementRepo taskRepo) {
		this.taskRepo = taskRepo;
		this.checkMap = new HashMap<String, Boolean>();
	}
	
	public List<List<TaskElement>> buildTaskTree(Task curTask, Task prevTask) {
		List<TaskClass> curClasses = curTask.getClasses();
		List<TaskClass> prevClasses = prevTask.getClasses();
		List<TaskClass> taskClasses = Stream.concat(curClasses.stream(), prevClasses.stream())
				.collect(Collectors.toList());
		List<List<TaskElement>> taskList = generateTree(taskClasses);
		return taskList;
	}

	private List<List<TaskElement>> generateTree(List<TaskClass> taskClasses) {
		List<List<TaskElement>> taskTreeList = new ArrayList<List<TaskElement>>();
		HashMap<String, TaskElement> taskElementHash = taskRepo.getTaskElementHashMap();
		for (TaskClass taskClass : taskClasses) {
			List<TaskMethod> taskMethods = taskClass.getTaskMethods();
			for (TaskMethod taskMethod : taskMethods) {
				String teID = taskMethod.getTaskElementID();
				TaskElement firstTE = taskElementHash.get(teID);
				if (firstTE == null) {
					// ************this part seems weird************************
					firstTE = taskRepo.getMergedElement(taskMethod);
					teID = firstTE.getTaskElementID();
					// ************************************
				}
				if (!checkMap.containsKey(teID)) {
					checkMap.put(firstTE.getTaskElementID(), true);
					List<TaskElement> taskList = new ArrayList<TaskElement>();
					taskList.add(firstTE);
					traverseAndBuildSubTree(firstTE, taskElementHash, taskList);
					taskTreeList.add(taskList);
				}
			}
			List<TaskAttribute> taskAttributes = taskClass.getTaskAttributes();
			for (TaskAttribute taskAttribute : taskAttributes) {
				String teID = taskAttribute.getTaskElementID();
				TaskElement firstTE = taskElementHash.get(teID);
				if (firstTE == null) {
					firstTE = taskRepo.getMergedElement(taskAttribute);
					teID = firstTE.getTaskElementID();
				}
				if (!checkMap.containsKey(teID)) {
					checkMap.put(firstTE.getTaskElementID(), true);
					List<TaskElement> taskList = new ArrayList<TaskElement>();
					taskList.add(firstTE);
					traverseAndBuildSubTree(firstTE, taskElementHash, taskList);
					taskTreeList.add(taskList);
				}
			}
		}
		return taskTreeList;
	}

	private void traverseAndBuildSubTree(TaskElement targetElement, HashMap<String, TaskElement> taskElementHash,
			List<TaskElement> taskList) {

		List<TaskElement> causedBy = taskRepo.getElements(targetElement.getCausedBy());
		List<TaskElement> removeList = new ArrayList<TaskElement>();
		for (TaskElement taskElement : causedBy) {
			if (!checkMap.containsKey(taskElement.getTaskElementID())) {
				checkMap.put(taskElement.getTaskElementID(), true);
				taskList.add(taskElement);
				traverseAndBuildSubTree(taskElement, taskElementHash, taskList);
			}
		}
		causedBy.removeAll(removeList);
		removeList.clear();

		List<TaskElement> causedTo = taskRepo.getElements(targetElement.getCausedTo());
		for (TaskElement taskElement : causedTo) {
			if (!checkMap.containsKey(taskElement.getTaskElementID())) {
				checkMap.put(taskElement.getTaskElementID(), true);
				taskList.add(taskElement);
				traverseAndBuildSubTree(taskElement, taskElementHash, taskList);
			}
		}
	}
}

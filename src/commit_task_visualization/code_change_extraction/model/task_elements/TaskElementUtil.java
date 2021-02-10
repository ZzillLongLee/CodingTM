package commit_task_visualization.code_change_extraction.model.task_elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.internal.runners.TestMethod;

public class TaskElementUtil {

	public static void initTMconnectedList(TaskMethod tm) {
		tm.getConnectedTMs().clear();
		tm.getConnectedTAs().clear();
		tm.getParentTAs().clear();
		tm.getParentTMs().clear();
	}

	public static void initTAconnectedList(TaskAttribute targetTA) {
		targetTA.getParentTMs().clear();

	}

	public static String generateTaskElementID(String prevID, String curID) {
		String elementID = null;
		if (prevID != null)
			elementID = prevID;
		if (curID != null)
			elementID = curID;
		return elementID;
	}

	public static void insertTEtoRepo(List<TaskClass> curTaskClasses) {
		TaskElementRepo taskElementRepo = TaskElementRepo.getInstance();
		for (TaskClass taskClass : curTaskClasses) {
			List<TaskAttribute> tas = taskClass.getTaskAttributes();
			for (TaskAttribute taskAttribute : tas) {
				buildCausalRelationShip(taskAttribute, taskElementRepo);
			}
			List<TaskMethod> tms = taskClass.getTaskMethods();
			for (TaskMethod taskMethod : tms) {
				buildCausalRelationShip(taskMethod, taskElementRepo);
			}
		}
	}

	private static void buildCausalRelationShip(TaskElement taskElement, TaskElementRepo taskElementRepo) {
//		The reason why we clone the object is because the fast-XML can't write duplicated object.
		try {
			if (taskElement instanceof TaskAttribute) {
				TaskAttribute taskAttribute = (TaskAttribute) taskElement;
				List<TaskMethod> parentTMs = taskAttribute.getParentTMs();

				List<TaskElement> cuasedBy = buildCRsetAsList(null, parentTMs);

				taskAttribute.setCausedBy(cuasedBy);
				taskElementRepo.putTaskElement(taskAttribute.getTaskElementID(), taskAttribute);
			}
			if (taskElement instanceof TaskMethod) {
				TaskMethod taskMethod = (TaskMethod) taskElement;
				List<TaskAttribute> parentTAs = taskMethod.getParentTAs();
				List<TaskMethod> parentTMs = taskMethod.getParentTMs();

				List<TaskElement> cuasedByAsList = buildCRsetAsList(parentTAs, parentTMs);

				List<TaskAttribute> connectedTAs = taskMethod.getConnectedTAs();
				for (TaskAttribute connectedTA : connectedTAs) {
					buildCausalRelationShip(connectedTA, taskElementRepo);
				}
				List<TaskMethod> connectedTMs = taskMethod.getConnectedTMs();
				for (TaskMethod connectedTM : connectedTMs) {
					buildCausalRelationShip(connectedTM, taskElementRepo);
				}
				List<TaskElement> causedTo = buildCRsetAsList(connectedTAs, connectedTMs);

				taskMethod.setCausedBy(cuasedByAsList);
				taskMethod.setCausedTo(causedTo);

				taskElementRepo.putTaskElement(taskMethod.getTaskElementID(), taskMethod);
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<TaskElement> buildCRsetAsList(List<TaskAttribute> tas, List<TaskMethod> tms)
			throws CloneNotSupportedException {
		List<TaskElement> causalRl = new ArrayList<TaskElement>();
		if (tms != null) {
			for (TaskMethod taskMethod : tms) {
				TaskMethod cloneTm = (TaskMethod) taskMethod.clone();
				causalRl.add(cloneTm);
//				causalRl.add(taskMethod);
			}
		}
		if (tas != null) {
			for (TaskAttribute taskAttribute : tas) {
				TaskAttribute cloneTa = (TaskAttribute) taskAttribute.clone();
				causalRl.add(cloneTa);
//				causalRl.add(taskAttribute);
			}
		}
		return causalRl;
	}

	public static String getCode(TaskElement te) {
		String code = null;
		String pastCode = te.getPastCode();
		String currentCode = te.getCurrentCode();
		if (pastCode != null)
			code = pastCode;
		if (currentCode != null)
			code = currentCode;
		return code;
	}

	public static boolean isTest(TaskElement te) {
		boolean isTest = false;
		if (te instanceof TaskMethod) {
			TaskMethod tm = (TaskMethod) te;
			String methodSig = tm.getMethodSignature();
			if(methodSig.contains("test"))
				isTest = true;
		}
		return isTest;
	}
}

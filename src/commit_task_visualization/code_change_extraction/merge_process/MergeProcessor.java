package commit_task_visualization.code_change_extraction.merge_process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskAttribute;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementRepo;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementUtil;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;


public class MergeProcessor {

	private List<String> cloneRemoveSet = new ArrayList<String>();
	private String prevCommitID;
	private String curCommitID;

	public MergeProcessor(String prevCommitID, String curCommitID) {
		this.prevCommitID = prevCommitID;
		this.curCommitID = curCommitID;
	}

	public void mergeTwoVersion(HashMap<String, TaskElement> taskElementHashmap) throws CloneNotSupportedException {
		HashMap<String, TaskElement> clonedTEHashMap = new HashMap<String, TaskElement>(taskElementHashmap);

		for (Entry<String, TaskElement> taskElementHash : taskElementHashmap.entrySet()) {
			TaskElement taskElement = taskElementHash.getValue();
			clonedTEHashMap.remove(taskElementHash.getKey());
			if (taskElement.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
				if (!cloneRemoveSet.contains(taskElementHash.getKey())) {
					mergeTaskElement(taskElement, clonedTEHashMap);
//					for (String key : cloneRemoveSet) {
//						clonedTEHashMap.remove(key);
//					}
				}
			} else {
				if (taskElement instanceof TaskMethod)
					TaskElementUtil.initTMconnectedList((TaskMethod) taskElement);
				if (taskElement instanceof TaskAttribute)
					TaskElementUtil.initTAconnectedList((TaskAttribute) taskElement);
			}
		}
		removedUsedElement(taskElementHashmap);
	}

	private void removedUsedElement(HashMap<String, TaskElement> taskElementHashmap) {
		for (String id : cloneRemoveSet) {
			taskElementHashmap.remove(id);
		}
	}

	private boolean mergeTaskElement(TaskElement taskElement, HashMap<String, TaskElement> cloneTeHashmap)
			throws CloneNotSupportedException {
		boolean isMerged = false;

		if (taskElement instanceof TaskAttribute) {
			TaskAttribute targetTA = (TaskAttribute) taskElement;
			String targetClassName = targetTA.getClassName();
			TaskElementUtil.initTAconnectedList(targetTA);
			String matchedAttributeCode = targetTA.getMatchedAttribute();
			for (Entry<String, TaskElement> cloneTeHash : cloneTeHashmap.entrySet()) {
				TaskElement te = cloneTeHash.getValue();
				if (te.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
					if (te instanceof TaskAttribute) {
						TaskAttribute ta = (TaskAttribute) te;
						String code = TaskElementUtil.getCode(ta);
						String className = ta.getClassName();
						boolean isSameID = checkSameCommitID(targetTA, ta);
						if (isSameID == false && targetClassName.equals(className)
								&& code.equals(matchedAttributeCode)) {
							isMerged = mergeAttribute(targetTA, ta);
							if (!cloneRemoveSet.contains(cloneTeHash.getKey()))
								cloneRemoveSet.add(cloneTeHash.getKey());
							return isMerged;
//							taskElementHashmap.remove(taskElementHash.getKey());
						}
					}
				}
			}
		}
		if (taskElement instanceof TaskMethod) {
			TaskMethod targetTM = (TaskMethod) taskElement;
			TaskElementUtil.initTMconnectedList(targetTM);
			String targetMethodSig = targetTM.getMethodSignature();
			String targetClassName = targetTM.getClassName();
			for (Entry<String, TaskElement> cloneTeHash : cloneTeHashmap.entrySet()) {
				TaskElement te = cloneTeHash.getValue();
				if (te.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
					if (te instanceof TaskMethod) {
						TaskMethod tm = (TaskMethod) te;
						String methodSig = tm.getMethodSignature();
						String className = tm.getClassName();
						boolean isSameID = checkSameCommitID(targetTM, tm);
						if (isSameID == false && targetClassName.equals(className)
								&& targetMethodSig.equals(methodSig)) {
							isMerged = mergeMethod(targetTM, tm);
							if (!cloneRemoveSet.contains(cloneTeHash.getKey()))
								cloneRemoveSet.add(cloneTeHash.getKey());
							return isMerged;
//							taskElementHashmap.remove(taskElementHash.getKey());

						}
					}
				}
			}
		}
		return isMerged;
	}

	private boolean checkSameCommitID(TaskElement targetTE, TaskElement te) {
		if (targetTE.getTaskElementID().contains(curCommitID) && te.getTaskElementID().contains(curCommitID))
			return true;
		else if (targetTE.getTaskElementID().contains(prevCommitID) && te.getTaskElementID().contains(prevCommitID))
			return true;
		else
			return false;
	}

	private boolean mergeAttribute(TaskAttribute targetTA, TaskAttribute ta) {
		mergeTaskElement(targetTA, ta);
		String curID = ta.getCurID();
		String prevID = ta.getPrevID();
		String curCode = ta.getCurrentCode();
		String pastCode = ta.getPastCode();
		if (curID != null) {
			targetTA.setCurID(curID);
			targetTA.setCurrentCode(curCode);
		}
		if (prevID != null) {
			targetTA.setPrevID(prevID);
			targetTA.setPastCode(pastCode);
		}
		return true;
	}

	private boolean mergeMethod(TaskMethod targetTM, TaskMethod tm) {
		mergeTaskElement(targetTM, tm);

		String curID = tm.getCurID();
		String prevID = tm.getPrevID();
		String curCode = tm.getCurrentCode();
		String pastCode = tm.getPastCode();
		if (curID != null) {
			targetTM.setCurID(curID);
			targetTM.setCurrentCode(curCode);
		}
		if (prevID != null) {
			targetTM.setPrevID(prevID);
			targetTM.setPastCode(pastCode);
		}

		List<TaskStatement> targetStmts = targetTM.getStatementsSet();
		List<TaskStatement> stmts = tm.getStatementsSet();
		List<TaskStatement> mergedStmts = mergeStatements(targetStmts, stmts);
		targetTM.setStatementsSet(mergedStmts);

		List<TaskMethod> targetChildTMs = targetTM.getChildTM();
		List<TaskMethod> childTMs = tm.getChildTM();
		List<TaskMethod> mergedChildTMs = Stream.concat(targetChildTMs.stream(), childTMs.stream())
				.collect(Collectors.toList());
		targetTM.setChildTMs(mergedChildTMs);
		return true;
	}

	private List<TaskStatement> mergeStatements(List<TaskStatement> targetStmts, List<TaskStatement> stmts) {
		List<TaskStatement> targetDuplicatedStmtList = new ArrayList<TaskStatement>();
		List<TaskStatement> duplicatedStmtList = new ArrayList<TaskStatement>();
		for (TaskStatement targetStmt : targetStmts) {
			String prevStmtString = targetStmt.getPastCode();
			for (TaskStatement stmt : stmts) {
				String curStmtString = stmt.getCurrentCode();
				if (prevStmtString != null && curStmtString != null) {
					if (prevStmtString.equals(curStmtString)) {
						targetDuplicatedStmtList.add(targetStmt);
						duplicatedStmtList.add(stmt);
					}
				}
			}
		}

		targetStmts.removeAll(targetDuplicatedStmtList);
		stmts.removeAll(duplicatedStmtList);
		List<TaskStatement> mergedStmts = Stream.concat(targetStmts.stream(), stmts.stream())
				.collect(Collectors.toList());
		return mergedStmts;
	}

	private void mergeTaskElement(TaskElement targetTE, TaskElement te) {

		List<TaskElement> targetCausedByList = targetTE.getCausedBy();
		List<TaskElement> causedByList = te.getCausedBy();
		List<TaskElement> mergedCausedBy = Stream.concat(targetCausedByList.stream(), causedByList.stream())
				.collect(Collectors.toList());
		targetTE.setCausedBy(mergedCausedBy);

		List<TaskElement> targetCausedToList = targetTE.getCausedTo();
		List<TaskElement> causedToList = te.getCausedTo();
		List<TaskElement> mergedCausedTo = Stream.concat(targetCausedToList.stream(), causedToList.stream())
				.collect(Collectors.toList());
		targetTE.setCausedTo(mergedCausedTo);
	}

	public void updateCausalRel(HashMap<String, TaskElement> taskElementHashmap) {
		TaskElementRepo taskRepo = TaskElementRepo.getInstance();
		for (Entry<String, TaskElement> taskElementHash : taskElementHashmap.entrySet()) {
			TaskElement te = taskElementHash.getValue();
			List<TaskElement> causedBy = taskRepo.getElements(te.getCausedBy());
			List<TaskElement> causedTo = taskRepo.getElements(te.getCausedTo());
			te.setCausedBy(causedBy);
			te.setCausedTo(causedTo);
		}
	}

}

package commit_task_visualization.code_change_extraction.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskAttribute;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskClass;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskAttribute.TaskAttributeBuilder;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskMethod.TaskMethodBuilder;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement.TaskStatementBuilder;


public class TaskElementGenerater {

	private final int deleted = 0;
	private final int added = 1;
	private final int modified = 2;

	public List<TaskClass> convertTaskDataStructure(List<ClassPart> classPartSet, int stateType) {
		List<TaskClass> cccList = new ArrayList<TaskClass>();
		boolean isMethodsNull = true;
		boolean isAttributeNull = true;

		for (ClassPart classPart : classPartSet) {
			TaskClass ccc = new TaskClass(classPart);
			List<AttributePart> attributeParts = classPart.getAttributeParts();
			List<MethodPart> methodParts = classPart.getMethodParts();
			if (stateType == deleted) {
				if (attributeParts != null && attributeParts.size() != 0) {
					isAttributeNull = false;
					for (AttributePart attributePart : attributeParts) {
						TaskAttribute cca = generateCodeChunkAttribute(attributePart, null, stateType);
						ccc.addCodeChunkAttribute(cca);
					}
				}
				if (methodParts != null && methodParts.size() != 0) {
					isMethodsNull = false;
					for (MethodPart methodPart : methodParts) {

						TaskMethod ccm = generateCodeChunkMethod(methodPart, null, stateType);
						ccc.addCodeChunkMethod(ccm);
					}
				}
			}

			if (stateType == added) {
				if (attributeParts != null && attributeParts.size() != 0) {
					isAttributeNull = false;
					for (AttributePart attributePart : attributeParts) {
						TaskAttribute cca = generateCodeChunkAttribute(null, attributePart, stateType);
						ccc.addCodeChunkAttribute(cca);
					}
				}
				if (methodParts != null && methodParts.size() != 0) {
					isMethodsNull = false;
					for (MethodPart methodPart : methodParts) {

						TaskMethod ccm = generateCodeChunkMethod(null, methodPart, stateType);
						ccc.addCodeChunkMethod(ccm);
					}
				}
			}
			if (isAttributeNull == true && isMethodsNull == true)
				continue;
			else
				cccList.add(ccc);
		}
		return cccList;
	}

	public TaskAttribute generateCodeChunkAttribute(AttributePart prevAttributePart, AttributePart curAttributePart,
			int versionType) {
		if (versionType == deleted) {
			String className = prevAttributePart.getClassName();
			String matchedAttribute = null;
			if (prevAttributePart.getMatchedAttribute() != null)
				matchedAttribute = prevAttributePart.getMatchedAttribute();
			TaskAttribute cca = new TaskAttributeBuilder().setClassName(className)
					.setChangedType(prevAttributePart.getChangedType().name()).setMatchedAttribute(matchedAttribute)
					.setPrevAttributeCode(prevAttributePart.getAttributeAsString()).setPrevId(prevAttributePart.getID())
					.build();
			return cca;
		}
		if (versionType == added) {
			String className = curAttributePart.getClassName();
			String matchedAttribute = null;
			if (curAttributePart.getMatchedAttribute() != null)
				matchedAttribute = curAttributePart.getMatchedAttribute();
			TaskAttribute cca = new TaskAttributeBuilder().setClassName(className)
					.setChangedType(curAttributePart.getChangedType().name()).setMatchedAttribute(matchedAttribute)
					.setCurAttributeCode(curAttributePart.getAttributeAsString()).setCurID(curAttributePart.getID())
					.build();
			return cca;
		}
		return null;
	}

	public TaskMethod generateCodeChunkMethod(MethodPart previousMethod, MethodPart currentMethod, int stateType) {

		if (stateType == deleted) {
			String prevID = previousMethod.getID();
			String clsName = previousMethod.getClassName();
			String methodSig = previousMethod.getMethodSignature();
			String changeType = previousMethod.getChangedType().name();
			String prevMethodCode = previousMethod.getMethodAsString();
			List<StatementPart> prevStmts = previousMethod.getStatements();
			List<TaskMethod> connectedTMset = new ArrayList<TaskMethod>();
			List<TaskAttribute> connectedTAset = new ArrayList<TaskAttribute>();

			List<MethodPart> connectedMethods = previousMethod.getConnectedMethods();
			if (connectedMethods != null && connectedMethods.size() != 0) {
				for (MethodPart connectedMethod : connectedMethods) {
					TaskMethod connectedCCM = generateCodeChunkMethod(connectedMethod, null, stateType);
					connectedTMset.add(connectedCCM);
				}
			}
			List<AttributePart> connectedAttributes = previousMethod.getConnectedAttributes();
			List<TaskAttribute> connectedCCA = new ArrayList<TaskAttribute>();
			if (connectedAttributes != null && connectedAttributes.size() != 0) {
				for (AttributePart connectedAttribute : connectedAttributes) {
					TaskAttribute cca = generateCodeChunkAttribute(connectedAttribute, null, stateType);
					connectedTAset.add(cca);
				}
			}
			List<MethodPart> prevChildMethods = previousMethod.getChildMethods();
			List<TaskMethod> childCCMset = new ArrayList<TaskMethod>();
			if (prevChildMethods != null && prevChildMethods.size() != 0) {
				for (MethodPart prevChildMethod : prevChildMethods) {
					TaskMethod childCCM = generateCodeChunkMethod(prevChildMethod, null, stateType);
					childCCMset.add(childCCM);
				}
			}

			List<TaskStatement> statements = generateCodeChunkStatements(prevStmts, null, stateType);
			TaskMethod codeChunkMethod = new TaskMethodBuilder().setClassName(clsName).setPrevID(prevID)
					.setPrevMethodCode(prevMethodCode).setChangeType(changeType).setMethodSignature(methodSig)
					.setStatementsSet(statements).setConnectedCCM(connectedTMset).setConnectedCCA(connectedTAset)
					.setChildCCM(childCCMset).build();
			codeChunkMethod.setParentMethodToConnectElements();
			return codeChunkMethod;
		}

		if (stateType == added) {
			String curID = currentMethod.getID();
			String clsName = currentMethod.getClassName();
			String methodSig = currentMethod.getMethodSignature();
			String changeType = currentMethod.getChangedType().name();
			String curMethodCode = currentMethod.getMethodAsString();
			List<StatementPart> curStmts = currentMethod.getStatements();
			List<MethodPart> connectedMethods = currentMethod.getConnectedMethods();
			List<TaskMethod> connectedCCMset = new ArrayList<TaskMethod>();

			if (connectedMethods != null && connectedMethods.size() != 0) {
				for (MethodPart connectedMethod : connectedMethods) {
					TaskMethod connectedCCM = generateCodeChunkMethod(null, connectedMethod, stateType);
					connectedCCMset.add(connectedCCM);
				}
			}
			List<AttributePart> connectedAttributes = currentMethod.getConnectedAttributes();
			List<TaskAttribute> connectedCCAset = new ArrayList<TaskAttribute>();
			if (connectedAttributes != null && connectedAttributes.size() != 0) {
				for (AttributePart connectedAttribute : connectedAttributes) {
					TaskAttribute cca = generateCodeChunkAttribute(null, connectedAttribute, stateType);
					connectedCCAset.add(cca);
				}
			}
			List<MethodPart> curChildMethods = currentMethod.getChildMethods();
			List<TaskMethod> childCCMset = new ArrayList<TaskMethod>();
			if (curChildMethods != null && curChildMethods.size() != 0) {
				for (MethodPart curChildMethod : curChildMethods) {
					TaskMethod childCCM = generateCodeChunkMethod(null, curChildMethod, stateType);
					childCCMset.add(childCCM);
				}
			}

			List<TaskStatement> statements = generateCodeChunkStatements(null, curStmts, stateType);
			TaskMethod codeChunkMethod = new TaskMethodBuilder().setClassName(clsName).setCurID(curID)
					.setCurMethodCode(curMethodCode).setChangeType(changeType).setMethodSignature(methodSig)
					.setStatementsSet(statements).setConnectedCCM(connectedCCMset).setConnectedCCA(connectedCCAset)
					.setChildCCM(childCCMset).build();
			codeChunkMethod.setParentMethodToConnectElements();
			return codeChunkMethod;
		}
		return null;
	}

	public List<TaskStatement> generateCodeChunkStatements(List<StatementPart> prevStmts, List<StatementPart> curStmts,
			int existType) {
		List<TaskStatement> ccsList = new ArrayList<TaskStatement>();
		if (existType == deleted) {
			if (prevStmts != null) {
				for (StatementPart prevStmt : prevStmts) {
					String id = prevStmt.getID();
					String pastCode = prevStmt.getStmt().toString();
					String changeType = prevStmt.getChangedType().name();
					String stmtAsString = prevStmt.stmtAsString();
					List<String> ids = generateIdList(prevStmt);
					TaskStatement taskStmt = new TaskStatementBuilder().setPrevID(id).setPastCode(pastCode)
							.setChangeType(changeType).setConnecteIdList(ids).build();
					ccsList.add(taskStmt);
				}
			}
		}

		if (existType == added) {
			if (curStmts != null) {
				for (StatementPart curStmt : curStmts) {
					String id = curStmt.getID();
					String changeType = curStmt.getChangedType().name();
					String currentCode = curStmt.stmtAsString();
					List<String> ids = generateIdList(curStmt);
					TaskStatement taskStmt = new TaskStatementBuilder().setCurID(id).setCurrentCode(currentCode)
							.setChangeType(changeType).setConnecteIdList(ids).build();
					ccsList.add(taskStmt);
				}
			}
		}

		return ccsList;
	}

	private List<String> generateIdList(StatementPart prevStmt) {
		List<String> idList = new ArrayList<String>();
		List<MethodPart> connectedMethods = prevStmt.getConnectedMethods();
		if (connectedMethods != null && connectedMethods.size() != 0) {
			for (MethodPart methodPart : connectedMethods) {
				idList.add(methodPart.getID());
			}
		}
		List<AttributePart> connectedAttributes = prevStmt.getConnectedAttributes();
		if (connectedAttributes != null && connectedAttributes.size() != 0) {
			for (AttributePart attributePart : connectedAttributes) {
				idList.add(attributePart.getID());
			}
		}
		return idList;
	}

}

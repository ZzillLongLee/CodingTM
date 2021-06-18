package commit_task_visualization.code_change_extraction.development_flow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.ClassInstanceCreationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.FieldAccessPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.MethodInvocationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.QualifiedNamePart;
import commit_task_visualization.code_change_extraction.util.Constants;

public class FlowConnector {

	private List<MethodPart> methodPartSet;
	private List<ClassPart> interfaceClassSet;
	private List<ClassPart> abstractClassSet;
	private ArrayList<MethodPart> flowChecker;

	public FlowConnector(List<MethodPart> methodPartSet, List<ClassPart> interfaceClassSet,
			List<ClassPart> abstractClassSet) {
		this.methodPartSet = methodPartSet;
		this.interfaceClassSet = interfaceClassSet;
		this.abstractClassSet = abstractClassSet;
	}

	public void connectFlow(StatementPart stmtPart, List<AttributePart> attributePartSet,
			List<MethodPart> clonedMethodPartSet, int type, MethodPart causedByMethod) {
		List<ClassInstanceCreationPart> instanceCreationPartList = stmtPart.getClassInstanceCrePart();
		List<FieldAccessPart> fieldAccessPartList = stmtPart.getFieldAccessPart();
		List<MethodInvocationPart> methodInvocationPartList = stmtPart.getMethodInvoPartList();
		List<QualifiedNamePart> qualifiedNamePartList = stmtPart.getQualifiedNamePart();
		List<String> simpleNames = stmtPart.getNames();
		// Method Invocation Compare and Connect Part
		if (methodInvocationPartList != null && methodInvocationPartList.size() != 0) {
			for (MethodInvocationPart methodInvocationPart : methodInvocationPartList) {
				MethodPart methodPart = getMethodFromMethodInvocation(clonedMethodPartSet, methodInvocationPart);
				//	We need to be cautious this part because it might make a bugs.	
				if (methodPart != null) {
					checkUsedPart(methodPartSet, type, methodPart);
					if (causedByMethod != null) {
						String causedMethodID = causedByMethod.getID();
						String methodID = methodPart.getID();
						if (causedByMethod != null && causedMethodID.equals(methodID)) {
							break;
						}
					}
					//This if statement condition prevent call method itself and connect each other.
					//	We need to be cautious this part because it might make a bugs.	
					if(causedByMethod == null && stmtPart.getMethodDecl().equals(methodPart.getMethodDecl()))
						break;
					if (flowChecker.contains(methodPart)) {
						break;
					} else {
						causedByMethod = methodPart;
						flowChecker.add(methodPart);
					}
					stmtPart.setConnectedMethod(methodPart);
					MethodPart connectedMethod = stmtPart.getConnectedSingleMethod(methodPart);

					List<StatementPart> statements = connectedMethod.getStatements();
					List<MethodPart> connectedClonedMethodPartSet = new ArrayList<MethodPart>(clonedMethodPartSet);
					if (statements != null) {
						for (StatementPart connectedMethodStmtPart : statements) {
							// connect method at this part. It's because avoiding infinite loop.
							connectFlow(connectedMethodStmtPart, attributePartSet, connectedClonedMethodPartSet, type,
									causedByMethod);
						}
					}
				}
			}
		}
		// Class Instance Creation Compare and Connect Part
		if (instanceCreationPartList != null && instanceCreationPartList.size() != 0) {
			for (ClassInstanceCreationPart instanceCreationPart : instanceCreationPartList) {
				MethodPart methodPart = getMethodFromInstnaceCreation(clonedMethodPartSet, instanceCreationPart);
				if (methodPart != null) {
					checkUsedPart(methodPartSet, type, methodPart);

					stmtPart.setConnectedMethod(methodPart);
					MethodPart connectedMethod = stmtPart.getConnectedSingleMethod(methodPart);

					List<StatementPart> statements = connectedMethod.getStatements();
					List<MethodPart> connectedClonedMethodPartSet = new ArrayList<MethodPart>(clonedMethodPartSet);
//					List<AttributePart> connectedClonedAttributePartSet = new ArrayList<AttributePart>(
//							attributePartSet);

					for (StatementPart connectedMethodStmtPart : statements) {
						MethodDeclaration stmtMethod = stmtPart.getMethodDecl();
						// connect method at this part. It's because avoiding infinite loop.
						if (stmtMethod.equals(connectedMethod.getMethodDecl()))
							break;
						else
							connectFlow(connectedMethodStmtPart, attributePartSet, connectedClonedMethodPartSet, type,
									null);
					}
				}
			}
		}
		// Field Access Compare and Connect Part
		if (fieldAccessPartList != null && fieldAccessPartList.size() != 0) {
			for (FieldAccessPart fieldAccessPart : fieldAccessPartList) {
				String fieldName = fieldAccessPart.getFieldName();
				AttributePart attributePart = getAttributeCheckingAttributeAccess(attributePartSet, fieldName);
				if (attributePart != null)
					stmtPart.setConnectedAttribute(attributePart);
			}
		}
		//
		if (qualifiedNamePartList != null && qualifiedNamePartList.size() != 0) {
			for (QualifiedNamePart qualifiedNamePart : qualifiedNamePartList) {
				String qualifier = qualifiedNamePart.getQualifier();
				String qualifiedNameIdentifier = qualifiedNamePart.getQualifiedNameIdentifier();
				AttributePart attributePart = getAttributeCheckingQualifiedName(attributePartSet, qualifier,
						qualifiedNameIdentifier);
				if (attributePart != null)
					stmtPart.setConnectedAttribute(attributePart);
			}
		}
		// simple name list check and connect
		if (simpleNames != null && simpleNames.size() != 0) {
			String className = stmtPart.getClassName();
			for (AttributePart attributePart : attributePartSet) {
				String attributeClassName = attributePart.getClassName();
				if (className.equals(attributeClassName)) {
					List fragments = attributePart.getFieldDecl().fragments();
					for (int i = 0; i < fragments.size(); i++) {
						Object obj = fragments.get(i);
						if (obj instanceof VariableDeclarationFragment) {
							VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
							String variableName = vdf.getName().getIdentifier();
							for (String simpleName : simpleNames) {
								if (simpleName.equals(variableName))
									stmtPart.setConnectedAttribute(attributePart);
							}
						}
					}
				}
			}
		}
	}

	private MethodPart getMethodCheckingQualifiedName(List<MethodPart> clonedMethodPartSet, String qualifier,
			String qualifiedNameIdentifier) {
		for (MethodPart methodPart : clonedMethodPartSet) {
			String className = methodPart.getClassName();
		}
		return null;
	}

	// need to connect hierarchy structure at this part.
	private MethodPart getMethodFromMethodInvocation(List<MethodPart> clonedMethodPartSet,
			MethodInvocationPart methodInvocationPart) {
		String invokedMethodName = methodInvocationPart.getMethodName();
		int invokedMethodSize = methodInvocationPart.getMethodArgu().size();
		QualifiedName qualifiedName = methodInvocationPart.getQualifiedName();
		List<MethodPart> abstractMethodList = hasAbstractMethod(clonedMethodPartSet);
		for (MethodPart methodPart : clonedMethodPartSet) {
			String className = methodPart.getClassName();
			String methodName = methodPart.getMethodName();
			int methodParamSize = methodPart.getParametersSize();
			MethodPart abstractMethodPart = getAbstractMethod(abstractMethodList, methodName, methodParamSize);
			if (abstractMethodPart != null) {
				String abstractMethodName = abstractMethodPart.getMethodName();
				int abstractMethodParamSize = abstractMethodPart.getParametersSize();
				if (invokedMethodName.equals(abstractMethodName) && invokedMethodSize == abstractMethodParamSize) {
					return abstractMethodPart;
				}
			} else {
				if(qualifiedName != null) {
					String qualifier = qualifiedName.getQualifier().getFullyQualifiedName();
					if(qualifier.equals(className) && invokedMethodName.equals(methodName) && invokedMethodSize == methodParamSize)
						return methodPart;
				}
				if (invokedMethodName.equals(methodName) && invokedMethodSize == methodParamSize) {
					return methodPart;
				}
			}
		}
		return null;
	}

	private MethodPart getMethodFromInstnaceCreation(List<MethodPart> clonedMethodPartSet,
			ClassInstanceCreationPart instanceCreationPart) {
		String createdInstanceName = instanceCreationPart.getCreationType().toString();
		int createdInstanceParamSize = instanceCreationPart.getCreationArguSize();
		for (MethodPart methodPart : clonedMethodPartSet) {
			String methodName = methodPart.getMethodName();
			int methodParamSize = methodPart.getParametersSize();
			if (createdInstanceName.equals(methodName) && createdInstanceParamSize == methodParamSize) {
				return methodPart;
			}
		}
		return null;
	}

	private AttributePart getAttributeCheckingQualifiedName(List<AttributePart> attributePartSet, String qualifier,
			String qualifiedNameIdentifier) {
		for (AttributePart attributePart : attributePartSet) {
			List<String> namesList = attributePart.getNames();
			String attributeClassName = attributePart.getClassName();
			if (namesList.contains(qualifiedNameIdentifier) && attributeClassName.equals(qualifier)) {
				return attributePart;
			}
		}
		return null;
	}

	private AttributePart getAttributeCheckingAttributeAccess(List<AttributePart> attributePartSet, String fieldName) {
		for (AttributePart attributePart : attributePartSet) {
			List<String> namesList = attributePart.getNames();
			if (namesList.contains(fieldName)) {
				return attributePart;
			}
		}
		return null;
	}

	private void checkUsedPart(List<MethodPart> methodPartSet, int type, MethodPart methodPart) {
		if (type == Constants.TEST_METHOD_TRAVERSE) {
			if (methodPartSet.contains(methodPart)) {
				int index = methodPartSet.indexOf(methodPart);
				methodPartSet.get(index).setUsed(true);
			}
		}
	}

	private List<MethodPart> hasAbstractMethod(List<MethodPart> clonedMethodPartSet) {
		List<MethodPart> abstractMethodList = new ArrayList<MethodPart>();
		for (MethodPart methodPart : clonedMethodPartSet) {
			String methodClassName = methodPart.getClassName();
			String methodName = methodPart.getMethodName();
			int methodParamSize = methodPart.getParametersSize();
			for (ClassPart interfaceClassPart : interfaceClassSet) {
				String itnerfaceClasName = interfaceClassPart.getClassName();
				if (itnerfaceClasName.equals(methodClassName)) {
					List<MethodPart> interfaceMethodParts = interfaceClassPart.getMethodParts();
					for (MethodPart interfaceMethodPart : interfaceMethodParts) {
						String interfaceMethodName = interfaceMethodPart.getMethodName();
						int interfaceMethodParamSize = interfaceMethodPart.getParametersSize();
						if (methodName.equals(interfaceMethodName) && methodParamSize == interfaceMethodParamSize)
							abstractMethodList.add(interfaceMethodPart);
					}
				}
			}
			for (ClassPart abstractClassPart : abstractClassSet) {
				String abstractClasName = abstractClassPart.getClassName();
				if (abstractClasName.equals(methodClassName)) {
					List<MethodPart> abstractMethodParts = abstractClassPart.getMethodParts();
					for (MethodPart abstractMethodPart : abstractMethodParts) {
						String interfaceMethodName = abstractMethodPart.getMethodName();
						int interfaceMethodParamSize = abstractMethodPart.getParametersSize();
						if (methodName.equals(interfaceMethodName) && methodParamSize == interfaceMethodParamSize)
							abstractMethodList.add(abstractMethodPart);
					}
				}
			}
		}
		return abstractMethodList;
	}

	private MethodPart getAbstractMethod(List<MethodPart> abstractMethodList, String methodName, int methodParamSize) {
		if (abstractMethodList.size() != 0) {
			for (MethodPart abstractMethodPart : abstractMethodList) {
				String abstractMethodName = abstractMethodPart.getMethodName();
				int abstractMethodParamSize = abstractMethodPart.getParametersSize();
				if (abstractMethodName.equals(methodName) && abstractMethodParamSize == methodParamSize) {
					return abstractMethodPart;
				}
			}
		}
		return null;
	}

	public void setFlowCheckList(ArrayList<MethodPart> flowChecker) {
		this.flowChecker = flowChecker;
	}

}

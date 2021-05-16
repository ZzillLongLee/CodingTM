package commit_task_visualization.code_change_extraction.development_flow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.SubCodeChunk;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;

public class DevelopmentFlowGenerator {

	private SubCodeChunk codeChunk;
	private List<ClassPart> classPartSet;
	private List<AttributePart> attributePartSet;
	private List<MethodPart> methodPartSet;
	private List<ClassPart> testClassPartSet;
	private List<AttributePart> testAttributePartSet;
	private List<MethodPart> testMethodPartSet;

	private List<ClassPart> interfaceClassSet;
	private List<ClassPart> abstractClassSet;

	public DevelopmentFlowGenerator(SubCodeChunk codeChunk) {
		this.codeChunk = codeChunk;
		this.classPartSet = codeChunk.getClassPartSet();
		this.attributePartSet = codeChunk.getAttributePartSet();
		this.methodPartSet = codeChunk.getMethodPartSet();
		this.testClassPartSet = codeChunk.getTestClassPartSet();
		this.testAttributePartSet = codeChunk.getTestAttributePartSet();
		this.testMethodPartSet = codeChunk.getTestMethodPartSet();
		generateDevelopmentFlow();
	}

	private void generateDevelopmentFlow() {
		// class part connection
		abstractClassSet = new ArrayList<ClassPart>();
		interfaceClassSet = new ArrayList<ClassPart>();

		List<MethodPart> clonedMethodPartSet = new ArrayList<MethodPart>(methodPartSet);

		// This method collect interface and abstract class set.
		// Maybe i gotta connect Hierarchical methods in this part...
		if (classPartSet != null) {
			collectParentClassSet();
			for (ClassPart classPart : classPartSet) {
				List<String> parentClasses = classPart.getParentClassesNames();
				if (parentClasses.size() != 0) {
					for (String parentClass : parentClasses) {
						connectFlowClass(classPart, parentClass, abstractClassSet);
						connectFlowClass(classPart, parentClass, interfaceClassSet);
					}
				}
			}
			assignMethodToHierarchyClasses(abstractClassSet, interfaceClassSet);
			buildHierarchyMethod(abstractClassSet, methodPartSet);
			buildHierarchyMethod(interfaceClassSet, methodPartSet);
		}

		FlowConnector flowConnector = new FlowConnector(methodPartSet, interfaceClassSet, abstractClassSet);
		// Method part connection
		// In order to set the "isUsed" status, we need to use the original
		// methodPartSet.
		if (testClassPartSet != null) {
			for (ClassPart testClassPart : testClassPartSet) {
				String testClassName = testClassPart.getClassName();
				List<MethodPart> clonedTestMethodPartSet = new ArrayList<MethodPart>(testMethodPartSet);
				for (MethodPart testMethodPart : testMethodPartSet) {
					String testMethodClassName = testMethodPart.getClassName();
					// starting point from test case method
					if (testClassName.equals(testMethodClassName)) {
						List<StatementPart> statements = testMethodPart.getStatements();
						if (statements != null) {
							for (StatementPart stmtPart : statements) {
								checkUsedPart(testMethodPartSet, Constants.TEST_METHOD_TRAVERSE, testMethodPart);
								// this part might need to be added to connect method in test class.
								checkIncludeTestMethod(stmtPart, clonedTestMethodPartSet);
								flowConnector.connectFlow(stmtPart, attributePartSet, methodPartSet,
										Constants.TEST_METHOD_TRAVERSE);
							}
						}
					}
				}
			}
		}
		for (ClassPart classPart : classPartSet) {
			String className = classPart.getClassName().toString();
			for (MethodPart methodPart : methodPartSet) {
				if (!methodPart.isUsed()) {
					String methodClassName = methodPart.getClassName().toString();
					// starting point from test case method
					if (className.equals(methodClassName)) {
						List<StatementPart> statements = methodPart.getStatements();
						if (statements != null) {
							for (StatementPart stmtPart : statements) {
								flowConnector.connectFlow(stmtPart, attributePartSet, clonedMethodPartSet,
										Constants.ORDINARY_METHOD_TRAVERSE);
							}
							methodPart.setUsed(true);
						}
					}
				}
			}
		}
	}

	private void assignMethodToHierarchyClasses(List<ClassPart> abstractClassSet, List<ClassPart> interfaceClassSet) {
		for (ClassPart interfaceClass : interfaceClassSet) {
			String className = interfaceClass.getClassName();
			List<ClassPart> connectedClasses = interfaceClass.getConnectedParts();
			for (MethodPart methodPart : methodPartSet) {
				String methodClassNmae = methodPart.getClassName();
				if (className.equals(methodClassNmae)) {
					interfaceClass.setMethodPart(methodPart);
				}
				for (ClassPart connectedClass : connectedClasses) {
					String connectedClassName = connectedClass.getClassName();
					if (connectedClassName.equals(methodClassNmae))
						connectedClass.setMethodPart(methodPart);
				}
			}
		}

		for (ClassPart abstractClass : abstractClassSet) {
			String className = abstractClass.getClassName();
			List<ClassPart> connectedClasses = abstractClass.getConnectedParts();
			for (MethodPart methodPart : methodPartSet) {
				String methodClassNmae = methodPart.getClassName();
				if (className.equals(methodClassNmae)) {
					abstractClass.setMethodPart(methodPart);
				}
				for (ClassPart connectedClass : connectedClasses) {
					String connectedClassName = connectedClass.getClassName();
					if (connectedClassName.equals(methodClassNmae))
						connectedClass.setMethodPart(methodPart);
				}
			}
		}
	}

	private void buildHierarchyMethod(List<ClassPart> parentClassSet, List<MethodPart> methodPartSet) {
		// TODO Auto-generated method stub
		for (ClassPart classPart : parentClassSet) {
			List<MethodPart> parentMethodParts = classPart.getMethodParts();
			List<ClassPart> connectedClassParts = classPart.getConnectedParts();
			for (MethodPart parentMethodPart : parentMethodParts) {
				String parentMethodName = parentMethodPart.getMethodName();
				int parentMethodParamSize = parentMethodPart.getParametersSize();
				for (ClassPart connectedClassPart : connectedClassParts) {
					List<MethodPart> connectedMethodParts = connectedClassPart.getMethodParts();
					for (MethodPart connectedMethodPart : connectedMethodParts) {
						String connectedMethodName = connectedMethodPart.getMethodName();
						int connectedMethodParamSize = connectedMethodPart.getParametersSize();
						if (parentMethodName.equals(connectedMethodName)
								&& parentMethodParamSize == connectedMethodParamSize) {
							parentMethodPart.linkChildMethod(connectedMethodPart);
						}
					}
				}
			}
		}
	}

	private void checkIncludeTestMethod(StatementPart stmtPart, List<MethodPart> clonedTestMethodPartSet) {
		// TODO Auto-generated method stub

	}

	private void connectFlowClass(ClassPart classPart, String targetClass, List<ClassPart> parentClassSet) {
		for (ClassPart parentClass : parentClassSet) {
			String parentClassName = parentClass.getClassName();
			if (parentClassName.equals(targetClass))
				parentClass.setConnectFlow(classPart);
		}
	}

	private void collectParentClassSet() {
		for (ClassPart classPart : classPartSet) {
			if (classPart.getClassIdentifierState() == InsideClassChangeType.ADD
					|| classPart.getClassIdentifierState() == InsideClassChangeType.MODIFIED) {
				if (classPart.isAbstract() && !abstractClassSet.contains(classPart))
					abstractClassSet.add(classPart);
				if (classPart.isInterface() && !abstractClassSet.contains(classPart))
					interfaceClassSet.add(classPart);
			}
		}
	}

	private void checkUsedPart(List<MethodPart> methodPartSet, int type, MethodPart methodPart) {
		if (type == Constants.TEST_METHOD_TRAVERSE) {
			if (methodPartSet.contains(methodPart)) {
				int index = methodPartSet.indexOf(methodPart);
				methodPartSet.get(index).setUsed(true);
			}
		}
	}
}

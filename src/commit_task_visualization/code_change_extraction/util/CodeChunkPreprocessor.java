package commit_task_visualization.code_change_extraction.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import commit_task_visualization.code_change_extraction.ast.ASTSupportSingleton;
import commit_task_visualization.code_change_extraction.ast.ExpressionVisitor;
import commit_task_visualization.code_change_extraction.ast.StatementVisitor;
import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ChangedFilePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.SubCodeChunk;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;

public class CodeChunkPreprocessor {

	private static TypeDeclaration classNode = null;

	public static String extractClassIdentifier(String classString, TypeDeclaration node) {
		List modifiers = node.modifiers();
		String className = node.getName().getFullyQualifiedName();
		Javadoc javadoc = node.getJavadoc();
		if (javadoc != null) {
			String javaDoc = javadoc.toString();
			classString = classString.replace(javaDoc, "");
		}
		Stream<String> lines = classString.lines();
		Iterator<String> linesIter = lines.iterator();
		while (linesIter.hasNext()) {
			boolean hasClassName = false;
			boolean hasModifiers = false;
			List<Boolean> hasModifierSet = new ArrayList<Boolean>();
			String line = (String) linesIter.next();
			if (modifiers.size() != 0) {
				for (Object modifier : modifiers) {
					if (modifier instanceof Modifier) {
						Modifier mod = (Modifier) modifier;
						String modAsString = mod.toString();
						if (line.contains(modAsString))
							hasModifierSet.add(true);
					}
				}
			}
			if (line.contains(className))
				hasClassName = true;

			if (modifiers.size() != 0) {
				hasModifiers = hasModifier(hasModifierSet);
				if (hasClassName == true && hasModifiers == true)
					return line;
			} else {
				if (hasClassName == true)
					return line;
			}
		}
		return null;
	}

	private static boolean hasModifier(List<Boolean> hasModifierSet) {
		for (Boolean boolean1 : hasModifierSet) {
			if (boolean1 == false)
				return false;
		}
		return true;
	}

	public static SubCodeChunk generateInnerStmtData(SubCodeChunk codeChunk) {
		StatementVisitor stmtVisitor = new StatementVisitor();
		ExpressionVisitor expVisitor = new ExpressionVisitor();
		List<MethodPart> methodPartSet = codeChunk.getMethodPartSet();
		if (methodPartSet != null)
			setInnerStmtSet(stmtVisitor, expVisitor, methodPartSet);
		List<MethodPart> testMethodPartSet = codeChunk.getTestMethodPartSet();
		if (testMethodPartSet != null)
			setInnerStmtSet(stmtVisitor, expVisitor, testMethodPartSet);
		return codeChunk;
	}

	private static void setInnerStmtSet(StatementVisitor stmtVisitor, ExpressionVisitor expVisitor,
			List<MethodPart> methodPartSet) {
		for (MethodPart methodPart : methodPartSet) {
			List<StatementPart> stmts = methodPart.getStatements();
			if (stmts != null) {
				for (StatementPart stmtPart : stmts) {
					Statement stmt = stmtPart.getStmt();
					Expression expression = getExpression(stmt);
					if (expression != null) {
						String exp = expression.toString();
						expVisitor.parsingExpression(exp, stmtPart);
					} else {
						setInnderStmt(stmtPart, stmtVisitor);
					}
				}
			}
		}
	}

	private static void setInnderStmt(StatementPart stmtPart, StatementVisitor stmtVisitor) {
		Statement stmt = stmtPart.getStmt();
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement expStmt = (ExpressionStatement) stmt;
			stmtVisitor.parsingStatement(expStmt.toString(), stmtPart);
		}
		if (stmt instanceof LabeledStatement) {
			LabeledStatement labelStmt = (LabeledStatement) stmt;
			stmtVisitor.parsingStatement(labelStmt.toString(), stmtPart);
		}
		if (stmt instanceof ReturnStatement) {
			ReturnStatement returnStmt = (ReturnStatement) stmt;
			stmtVisitor.parsingStatement(returnStmt.toString(), stmtPart);
		}
		if (stmt instanceof ThrowStatement) {
			ThrowStatement throwStmt = (ThrowStatement) stmt;
			stmtVisitor.parsingStatement(throwStmt.toString(), stmtPart);
		}
		if (stmt instanceof TypeDeclarationStatement) {
			TypeDeclarationStatement typeDeclStmt = (TypeDeclarationStatement) stmt;
			stmtVisitor.parsingStatement(typeDeclStmt.toString(), stmtPart);
		}
		if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement variableDeclStmt = (VariableDeclarationStatement) stmt;
			stmtVisitor.parsingStatement(variableDeclStmt.toString(), stmtPart);
		}
		if (stmt instanceof AssertStatement) {
			AssertStatement assertStmt = (AssertStatement) stmt;
			stmtVisitor.parsingStatement(assertStmt.toString(), stmtPart);
		}
	}

	private static Expression getExpression(Statement stmt) {
		Expression expression = null;
		if (stmt instanceof ForStatement) {
			ForStatement forStmt = (ForStatement) stmt;
			expression = forStmt.getExpression();
		}
		if (stmt instanceof IfStatement) {
			IfStatement ifStmt = (IfStatement) stmt;
			expression = ifStmt.getExpression();
		}
		if (stmt instanceof EnhancedForStatement) {
			EnhancedForStatement enhancedForStmt = (EnhancedForStatement) stmt;
			expression = enhancedForStmt.getExpression();
		}
		if (stmt instanceof SwitchStatement) {
			SwitchStatement switchStmt = (SwitchStatement) stmt;
			expression = switchStmt.getExpression();
		}
		if (stmt instanceof WhileStatement) {
			WhileStatement switchStmt = (WhileStatement) stmt;
			expression = switchStmt.getExpression();
		}
		if (stmt instanceof SynchronizedStatement) {
			SynchronizedStatement switchStmt = (SynchronizedStatement) stmt;
			expression = switchStmt.getExpression();
		}
		return expression;
	}

	public static SubCodeChunk collectingElementSet(CodeSnapShot codeSnapShot, int version) {
		List<ChangedFilePart> changedFileList = codeSnapShot.getChangedFileList();

		List<ClassPart> classPartSet = new ArrayList<ClassPart>();
		List<AttributePart> attributePartSet = new ArrayList<AttributePart>();
		List<MethodPart> methodPartSet = new ArrayList<MethodPart>();

		List<ClassPart> testClassPartSet = new ArrayList<ClassPart>();
		List<AttributePart> testAttributePartSet = new ArrayList<AttributePart>();
		List<MethodPart> testMethodPartSet = new ArrayList<MethodPart>();

		for (ChangedFilePart changedFilePart : changedFileList) {
			List<ClassPart> classParts = changedFilePart.getClassParts(version);
			List<AttributePart> fieldObjects = changedFilePart.getFieldObjects(version);
			List removeFieldList = removeNoneType(fieldObjects);
			fieldObjects.removeAll(removeFieldList);
			List<MethodPart> methodObjects = changedFilePart.getMethodObjects(version);
			List removeMethodList = removeNoneType(methodObjects);
			methodObjects.removeAll(removeMethodList);

			filterStmts(methodObjects);
			for (ClassPart classPart : classParts) {
				if (classPart.getClassName() != null) {
					String className = classPart.getClassName();
					List classFieldObjects = getClassElements(className, fieldObjects);
					List classMethodObjects = getClassElements(className, methodObjects);
					if (className.contains(Constants.KEY_WORD_TEST)) {
						testClassPartSet.add(classPart);
						testAttributePartSet.addAll(classFieldObjects);
						classMethodObjects = filterNoneTestMethodInTest(classMethodObjects, methodPartSet);
						testMethodPartSet.addAll(classMethodObjects);
					} else {
						classPartSet.add(classPart);
						attributePartSet.addAll(classFieldObjects);
						methodPartSet.addAll(classMethodObjects);
					}
				}
			}
		}

		return new SubCodeChunk.SubCodeChunkBuilder().setClassPartSet(classPartSet)
				.setTestClassPartSet(testClassPartSet).setAttributePartSet(attributePartSet)
				.setTestAttributePartSet(testAttributePartSet).setMethodPartSet(methodPartSet)
				.setTestMethodPartSet(testMethodPartSet).build();
	}

	private static List filterNoneTestMethodInTest(List classMethodObjects, List<MethodPart> methodPartSet) {
		List classMethodObjectsClone = new ArrayList<Object>(classMethodObjects);
		for (Object classMethodObject : classMethodObjects) {
			if (classMethodObject instanceof MethodPart) {
				boolean hasTestAnotation = false;
				boolean isPublicType = false;
				MethodPart methodPart = (MethodPart) classMethodObject;
				String methodName = methodPart.getMethodName();
				List modifiers = methodPart.getMethodDecl().modifiers();
				for (Object object : modifiers) {
					if (object instanceof MarkerAnnotation) {
						MarkerAnnotation ma = (MarkerAnnotation) object;
						if (ma.toString().equals(Constants.TEST_ANOTATION))
							hasTestAnotation = true;

					}
					if (object instanceof Modifier) {
						Modifier modifier = (Modifier) object;
						isPublicType = modifier.isPublic();
					}
				}
				if (hasTestAnotation == false && isPublicType == false) {
					methodPartSet.add(methodPart);
					classMethodObjectsClone.remove(methodPart);
				}
			}
		}
		return classMethodObjectsClone;
	}

	private static List getClassElements(String className, List elementObjects) {
		List classElements = new ArrayList<>();
		for (Object object : elementObjects) {
			if (object instanceof AttributePart) {
				AttributePart attributePart = (AttributePart) object;
				String attributeClassName = attributePart.getClassName();
				if (className.equals(attributeClassName))
					classElements.add(attributePart);
			}
			if (object instanceof MethodPart) {
				MethodPart methodPart = (MethodPart) object;
				String methodClassName = methodPart.getClassName();
				if (className.equals(methodClassName))
					classElements.add(methodPart);
			}
		}
		return classElements;
	}

	private static void filterStmts(List<MethodPart> methodObjects) {
		for (MethodPart methodPart : methodObjects) {
			List<StatementPart> stmts = methodPart.getStatements();
			if (stmts != null) {
				List removeStmts = removeNoneType(stmts);
				stmts.removeAll(removeStmts);
			}
		}
	}

	public static List removeNoneType(List elementObjects) {
		List<Object> removeList = new ArrayList<Object>();
		for (Object object : elementObjects) {
			if (object instanceof AttributePart) {
				AttributePart attributePart = (AttributePart) object;
				if (attributePart.getChangedType() == InsideClassChangeType.NONE
						|| attributePart.getChangedType() == null)
					removeList.add(attributePart);
			}
			if (object instanceof MethodPart) {
				MethodPart methodPart = (MethodPart) object;
				if (methodPart.getChangedType() == InsideClassChangeType.NONE || methodPart.getChangedType() == null)
					removeList.add(methodPart);
			}
			if (object instanceof StatementPart) {
				StatementPart statementPart = (StatementPart) object;
				if (statementPart.getChangedType() == InsideClassChangeType.NONE
						|| statementPart.getChangedType() == null)
					removeList.add(statementPart);
			}
		}
		return removeList;
	}

	public static void reAssignCodeElement(SubCodeChunk subCodeChunk, HashMap<String, List<String>> idSet) {
		List<MethodPart> testMethodPartsSet = subCodeChunk.getTestMethodPartSet();
		for (MethodPart testMethodPart : testMethodPartsSet) {
			List<MethodPart> connectedMethodsList = new ArrayList<MethodPart>();
			List<AttributePart> connectedAttributesList = new ArrayList<AttributePart>();
			List<StatementPart> stmts = testMethodPart.getStatements();
			if (stmts != null && stmts.size() != 0) {
				for (StatementPart stmt : stmts) {
					List<MethodPart> connectedMethods = stmt.getConnectedMethods();
					connectedMethodsList.addAll(connectedMethods);
					List<AttributePart> connectedAttributes = stmt.getConnectedAttributes();
					connectedAttributesList.addAll(connectedAttributes);
					assignConnectedList(connectedMethods, idSet);
				}
			}
			connectedMethodsList = connectedMethodsList.stream().distinct().collect(Collectors.toList());
			connectedAttributesList = connectedAttributesList.stream().distinct().collect(Collectors.toList());
			testMethodPart.setConnectedMethods(connectedMethodsList);
			generateIDsForMethods(testMethodPart, idSet, connectedMethodsList);

			testMethodPart.setConnectedAttributes(connectedAttributesList);
			generateIDsForAttributes(testMethodPart, idSet, connectedAttributesList);
		}

		List<MethodPart> methodParts = subCodeChunk.getMethodPartSet();
		for (MethodPart methodPart : methodParts) {
			List<MethodPart> connectedMethodsList = new ArrayList<MethodPart>();
			List<AttributePart> connectedAttributesList = new ArrayList<AttributePart>();
			List<StatementPart> stmts = methodPart.getStatements();
			if (stmts != null && stmts.size() != 0) {
				for (StatementPart stmt : stmts) {
					List<MethodPart> connectedMethods = stmt.getConnectedMethods();
					connectedMethodsList.addAll(connectedMethods);
					List<AttributePart> connectedAttributes = stmt.getConnectedAttributes();
					connectedAttributesList.addAll(connectedAttributes);
					assignConnectedList(connectedMethods, idSet);
				}
			}
			connectedMethodsList = connectedMethodsList.stream().distinct().collect(Collectors.toList());
			connectedAttributesList = connectedAttributesList.stream().distinct().collect(Collectors.toList());

			methodPart.setConnectedMethods(connectedMethodsList);
			generateIDsForMethods(methodPart, idSet, connectedMethodsList);

			methodPart.setConnectedAttributes(connectedAttributesList);
			generateIDsForAttributes(methodPart, idSet, connectedAttributesList);
		}
	}

	private static void assignConnectedList(List<MethodPart> methods, HashMap<String, List<String>> idSet) {
		for (MethodPart method : methods) {
			List<StatementPart> connectedMethodStmts = method.getStatements();
			List<MethodPart> connectedMethodsList = new ArrayList<MethodPart>();
			List<AttributePart> connectedAttributesList = new ArrayList<AttributePart>();
			if (connectedMethodStmts != null) {
				for (StatementPart connectedMethodStmt : connectedMethodStmts) {
					List<MethodPart> connectedMethods = connectedMethodStmt.getConnectedMethods();
					connectedMethodsList.addAll(connectedMethods);
					List<AttributePart> connectedAttributes = connectedMethodStmt.getConnectedAttributes();
					connectedAttributesList.addAll(connectedAttributes);
					assignConnectedList(connectedMethods, idSet);
				}
			}
			connectedMethodsList = connectedMethodsList.stream().distinct().collect(Collectors.toList());
			connectedAttributesList = connectedAttributesList.stream().distinct().collect(Collectors.toList());

			method.setConnectedMethods(connectedMethodsList);
			generateIDsForMethods(method, idSet, connectedMethodsList);

			method.setConnectedAttributes(connectedAttributesList);
			generateIDsForAttributes(method, idSet, connectedAttributesList);
		}
	}

	private static void generateIDsForMethods(MethodPart parentMethod, HashMap<String, List<String>> idSet,
			List<MethodPart> connectedMethods) {
		String parentID = parentMethod.getID();
		for (MethodPart connectedMethod : connectedMethods) {
			String connectedID = connectedMethod.getID();
			if (idSet.containsKey(parentID)) {
				List<String> values = idSet.get(parentID);
				if (!values.contains(connectedID))
					values.add(connectedID);
			} else {
				List<String> values = new ArrayList<String>();
				values.add(connectedID);
				idSet.put(parentID, values);
			}
		}
	}

	private static void generateIDsForAttributes(MethodPart parentMethod, HashMap<String, List<String>> idSet,
			List<AttributePart> connectedAttributes) {
		String parentID = parentMethod.getID();
		for (AttributePart connectedAttribute : connectedAttributes) {
			String connectedID = connectedAttribute.getID();
			if (idSet.containsKey(parentID)) {
				List<String> values = idSet.get(parentID);
				if (!values.contains(connectedID))
					values.add(connectedID);
			} else {
				List<String> values = new ArrayList<String>();
				values.add(connectedID);
				idSet.put(parentID, values);
			}
		}
	}
}

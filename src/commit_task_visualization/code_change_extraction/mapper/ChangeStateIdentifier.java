package commit_task_visualization.code_change_extraction.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WhileStatement;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ChangedFilePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;

public class ChangeStateIdentifier {

	private HashMap<String, String> matchedAttributes = new HashMap<String, String>();
	private CodeChangeSimilarityChecker ccsc = new CodeChangeSimilarityChecker();

	public void identifyInnerChangedFileContentsStates(ChangedFilePart changedFilePart) {

		List<ClassPart> prevVersionClassParts = changedFilePart.getClassParts(Constants.PREV_VERSION);
		List<ClassPart> curVersionClassParts = changedFilePart.getClassParts(Constants.CUR_VERSION);

		List<AttributePart> prevVersionFieldObjects = changedFilePart.getFieldObjects(Constants.PREV_VERSION);
		List<AttributePart> curVersionFieldObjects = changedFilePart.getFieldObjects(Constants.CUR_VERSION);

		List<MethodPart> prevVersionMethodObjects = changedFilePart.getMethodObjects(Constants.PREV_VERSION);
		List<MethodPart> curVersionMethodObjects = changedFilePart.getMethodObjects(Constants.CUR_VERSION);

		if (changedFilePart.getChangeType().equals(InsideClassChangeType.ADD))
			setStatusType(changedFilePart, InsideClassChangeType.ADD);
		else if (changedFilePart.getChangeType().equals(InsideClassChangeType.DELETE))
			setStatusType(changedFilePart, InsideClassChangeType.DELETE);
		else {

			setClassIdentifierChangeState(prevVersionClassParts, curVersionClassParts);
			setClassIdentifierChangeState(curVersionClassParts, prevVersionClassParts);

			List<AttributePart> filteredPrevVersionFieldObjects = filterAttributePart(curVersionFieldObjects,
					prevVersionFieldObjects);
			List<AttributePart> filteredCurVersionFieldObjects = filterAttributePart(prevVersionFieldObjects,
					curVersionFieldObjects);

			HashMap<AttributePart, AttributePart> similarAttributeSet = ccsc
					.getAttributeSimilaritySet(filteredCurVersionFieldObjects, filteredPrevVersionFieldObjects);
			setAttributesChangeState(filteredCurVersionFieldObjects, filteredPrevVersionFieldObjects,
					Constants.PREV_VERSION, similarAttributeSet);

			similarAttributeSet = ccsc.getAttributeSimilaritySet(filteredPrevVersionFieldObjects,
					filteredCurVersionFieldObjects);
			setAttributesChangeState(filteredPrevVersionFieldObjects, filteredCurVersionFieldObjects,
					Constants.CUR_VERSION, similarAttributeSet);

			// the statement compare also should be changed in near future.
			setMethodsChangeState(curVersionMethodObjects, prevVersionMethodObjects, Constants.PREV_VERSION);
			setMethodsChangeState(prevVersionMethodObjects, curVersionMethodObjects, Constants.CUR_VERSION);

		}
	}

	private void setStatusType(ChangedFilePart changedFilePart, InsideClassChangeType changedType) {
		List<AttributePart> fieldObjects = null;
		List<MethodPart> methodObjects = null;
		List<ClassPart> classParts = null;
		if (changedType == InsideClassChangeType.ADD) {
			classParts = changedFilePart.getClassParts(Constants.CUR_VERSION);
			fieldObjects = changedFilePart.getFieldObjects(Constants.CUR_VERSION);
			methodObjects = changedFilePart.getMethodObjects(Constants.CUR_VERSION);
		} else if (changedType == InsideClassChangeType.DELETE) {
			classParts = changedFilePart.getClassParts(Constants.PREV_VERSION);
			fieldObjects = changedFilePart.getFieldObjects(Constants.PREV_VERSION);
			methodObjects = changedFilePart.getMethodObjects(Constants.PREV_VERSION);
		}
		setSubElementStateTypes(classParts, fieldObjects, methodObjects, changedType);
	}

	private void setSubElementStateTypes(List<ClassPart> classParts, List<AttributePart> fieldObjects,
			List<MethodPart> methodObjects, InsideClassChangeType changedType) {
		for (ClassPart classPart : classParts) {
			classPart.setClassIdentifierState(changedType);
		}
		for (AttributePart attributePart : fieldObjects) {
			attributePart.setChangedType(changedType);
		}
		for (MethodPart methodPart : methodObjects) {
			methodPart.setChangedType(changedType);
			List<StatementPart> statements = methodPart.getStatements();
			if (statements != null) {
				for (StatementPart stmt : statements) {
					stmt.setChangedType(changedType);
				}
			}
		}
	}

//	This is conceptually weird. It's bacuase the change of class siganutre could mean the class is changed
	private void setClassIdentifierChangeState(List<ClassPart> classParts1, List<ClassPart> classParts2) {
		for (ClassPart classPart2 : classParts2) {
			String curClassIdentifier = classPart2.getClassIdentifier();
			String classPart2Name = classPart2.getClassName();
			for (ClassPart classPart1 : classParts1) {
				String prevClassIdentifier = classPart1.getClassIdentifier();
				String classPart1Name = classPart1.getClassName().toString();
				if (classPart2Name.equals(classPart1Name)) {
					if (curClassIdentifier != null && prevClassIdentifier != null) {
						if (!curClassIdentifier.equals(prevClassIdentifier)) {
							classPart2.setClassIdentifierState(InsideClassChangeType.MODIFIED);
							classPart1.setClassIdentifierState(InsideClassChangeType.MODIFIED);
						} else {
							classPart2.setClassIdentifierState(InsideClassChangeType.NONE);
							classPart1.setClassIdentifierState(InsideClassChangeType.NONE);
						}
					}
				}
			}
		}
	}

	private void setAttributesChangeState(List<AttributePart> fieldObjects1, List<AttributePart> fieldObjects2,
			int versionType, HashMap<AttributePart, AttributePart> similarAttributeSet) {
		for (AttributePart attributePart2 : fieldObjects2) {
			if (similarAttributeSet.containsKey(attributePart2)) {
				AttributePart value = similarAttributeSet.get(attributePart2);
				attributePart2.setMatchedAttribute(value.getAttributeAsString());
				attributePart2.setChangedType(InsideClassChangeType.MODIFIED);
			} else if (versionType == Constants.CUR_VERSION) {
				attributePart2.setChangedType(InsideClassChangeType.ADD);
			} else if (versionType == Constants.PREV_VERSION) {
				attributePart2.setChangedType(InsideClassChangeType.DELETE);
			}
		}
	}

	private void setMethodsChangeState(List<MethodPart> methodObjects1, List<MethodPart> methodObjects2,
			int versionType) {
		for (MethodPart methodPart2 : methodObjects2) {
			String methodAsString2 = methodPart2.getMethodAsString();
			String className2 = methodPart2.getClassName();
			boolean isSame = false;
			boolean isSameIdentifier = false;
			for (MethodPart methodPart1 : methodObjects1) {
				String methodAsString1 = methodPart1.getMethodAsString();
				String className1 = methodPart1.getClassName();
				isSameIdentifier = compareMethodIdentifier(methodPart1, methodPart2);
				if (className2.equals(className1)&&isSameIdentifier == true) {
					if (methodAsString2.equals(methodAsString1) == false) {
						methodPart2.setChangedType(InsideClassChangeType.MODIFIED);
						// Matched ?ïòÎ©? previousÍ≤ÉÏù¥ ?Ñ§?†ï?ù¥ ?ïà?êò?äîÍ±? Í∞ôÏùå Í∑∏Îü¨ÎØ?Î°? ?ó¨Í∏∞ÏÑú ?àò?ñâ?ï¥ Î≥¥Ïù¥?äîÍ≤?
						// ?†Å?†à?ï¥ Î≥¥ÏûÑ
						List<StatementPart> methodObj2Statements = methodPart2.getStatements();
						List<StatementPart> matchedMethodStatements = methodPart1.getStatements();
						setModifiedMethodStatementsState(matchedMethodStatements, methodObj2Statements, versionType);
						break;
					} else {
						isSame = true;
						methodPart2.setChangedType(InsideClassChangeType.NONE);
						break;
					}
				}
			}
			if (isSame == false && isSameIdentifier == false) {
				if (methodPart2.getChangedType() != InsideClassChangeType.NONE
						&& versionType == Constants.CUR_VERSION) {
					methodPart2.setChangedType(InsideClassChangeType.ADD);
					List<StatementPart> methodPart2Stmts = methodPart2.getStatements();
					setAddedOrDeletedStmtState(methodPart2Stmts, InsideClassChangeType.ADD);
				} else if (methodPart2.getChangedType() != InsideClassChangeType.NONE
						&& versionType == Constants.PREV_VERSION) {
					methodPart2.setChangedType(InsideClassChangeType.DELETE);
					List<StatementPart> methodPart2Stmts = methodPart2.getStatements();
					setAddedOrDeletedStmtState(methodPart2Stmts, InsideClassChangeType.DELETE);
				}
			}
		}
	}

	private boolean compareMethodIdentifier(MethodPart methodPart1, MethodPart methodPart2) {

		String method1Name = methodPart1.getMethodName();
		String method1Parameters = methodPart1.getParametersAsString();
		Type method1ReturnType = methodPart1.getReturnType();
		String method1ReturnTypeAsString = null;
		if (method1ReturnType != null)
			method1ReturnTypeAsString = method1ReturnType.toString();

		String method2Name = methodPart2.getMethodName().toString();
		String method2Parameters = methodPart2.getParametersAsString();
		Type method2ReturnType = methodPart2.getReturnType();
		String method2ReturnTypeAsString = null;
		if (method2ReturnType != null)
			method2ReturnTypeAsString = method2ReturnType.toString();

		if (method1ReturnTypeAsString != null && method2ReturnTypeAsString != null) {
			if (method1Name.equals(method2Name) && method1Parameters.equals(method2Parameters)
					&& method1ReturnTypeAsString.equals(method2ReturnTypeAsString))
				return true;
			else
				return false;
		} else {
			if (method1Name.equals(method2Name) && method1Parameters.equals(method2Parameters))
				return true;
			else
				return false;
		}
	}

	private void setModifiedMethodStatementsState(List<StatementPart> stmtObjects1, List<StatementPart> stmtObjects2,
			int versionType) {
		if (stmtObjects2 != null && stmtObjects1 != null) {
			for (StatementPart stmtObject2 : stmtObjects2) {
				boolean isSame = false;
				HashMap<Double, StatementPart> similarStatementSet = new HashMap<Double, StatementPart>();
				Statement object2Stmt = stmtObject2.getStmt();
				String methodObj2StmtString = stmtObject2.stmtAsString();
				List<String> stmtPart2Names = stmtObject2.getNames();
				for (StatementPart stmtObject1 : stmtObjects1) {
					Statement object1Stmt = stmtObject1.getStmt();
					String prevMethodStmtString = stmtObject1.stmtAsString();
					List<String> stmtPart1Names = stmtObject1.getNames();
					if (methodObj2StmtString.equals(prevMethodStmtString)) {
						isSame = true;
						stmtObject2.setChangedType(InsideClassChangeType.NONE);
						break;
					}
					if (object2Stmt.getClass().isInstance(object1Stmt)) {
						double similarityValue = computeSimilarity(stmtPart1Names, stmtPart2Names, versionType);
						similarStatementSet.put(similarityValue, stmtObject1);
					}
				}
				if (isSame == false) {
					double minValue = 0;
					int idx = 0;
					Set<Entry<Double, StatementPart>> entrySet = similarStatementSet.entrySet();
					for (Entry<Double, StatementPart> entry : entrySet) {
						Double distanceValue = entry.getKey();
						if (idx == 0)
							minValue = distanceValue;
						else if (distanceValue < minValue)
							minValue = distanceValue;
						idx++;
					}
					if (0.0 < minValue && minValue < Constants.MODIFIED_STATUS_THRESHOLD_VALUE) {
						stmtObject2.setChangedType(InsideClassChangeType.MODIFIED);
						StatementPart bestMatchVar = similarStatementSet.get(minValue);
						setDetailContents(stmtObject2, bestMatchVar);
						// Switch, if , while. for, for each?ì±?óê ???ïú ExpressionÍ≥? bodyÎ•? ÎπÑÍµê?ïòÍ∏? ?úÑ?ïú
						// ?Ç¥?ö© ?ïÑ?öî
					} else if (versionType == Constants.CUR_VERSION) {
						stmtObject2.setChangedType(InsideClassChangeType.ADD);
					} else if (versionType == Constants.PREV_VERSION) {
						stmtObject2.setChangedType(InsideClassChangeType.DELETE);
					}
				}
			}
		}
	}

	private void setAddedOrDeletedStmtState(List<StatementPart> stmtObjects, InsideClassChangeType changedType) {
		if (stmtObjects != null && stmtObjects.size() != 0) {
			for (StatementPart stmtObject : stmtObjects) {
				stmtObject.setChangedType(changedType);
			}
		}
	}

	private double computeSimilarity(List<String> elementPart1Names, List<String> elementPart2Names, int versionType) {
		double similarity = 0.0;
		int totalSize;
		int uniqueValuesSize;
		int intersectionSize;

		if (versionType == Constants.CUR_VERSION) {
			try {
				totalSize = elementPart2Names.size() + elementPart1Names.size();
				List<String> curNames = new ArrayList<String>(elementPart2Names);
				curNames.removeAll(elementPart1Names);
				uniqueValuesSize = curNames.size();
				double uniqueValuesSizeDouble = uniqueValuesSize;
				intersectionSize = totalSize - uniqueValuesSize;
				double intersectionSizeDouble = intersectionSize;
				similarity = intersectionSize / totalSize * 100.0;
			} catch (ArithmeticException e) {
				System.out.println("The similarity value is :" + similarity);
			}

		} else if (versionType == Constants.PREV_VERSION) {
			try {
				totalSize = elementPart1Names.size();
				double totalSizeDouble = totalSize;
				List<String> prevNames = new ArrayList<String>(elementPart1Names);
				prevNames.removeAll(elementPart2Names);
				uniqueValuesSize = prevNames.size();
				intersectionSize = totalSize - uniqueValuesSize;
				double intersectionSizeDouble = intersectionSize;
				similarity = (intersectionSizeDouble / totalSizeDouble) * 100.0;
			} catch (ArithmeticException e) {
				System.out.println("The similarity value is :" + similarity);
			}
		}

		return Math.ceil(similarity);
	}

	private void setDetailContents(StatementPart stmtObject2, StatementPart bestMatchVar) {
		Statement stmt2 = stmtObject2.getStmt();
		Statement bestMatchStmt = bestMatchVar.getStmt();
		if (stmt2 instanceof ForStatement && bestMatchStmt instanceof ForStatement) {
			ForStatement stmt2For = (ForStatement) stmt2;
			ForStatement bestMatchStmtFor = (ForStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2For.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtFor.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);

		} else if (stmt2 instanceof EnhancedForStatement && bestMatchStmt instanceof EnhancedForStatement) {
			EnhancedForStatement stmt2EnhancedFor = (EnhancedForStatement) stmt2;
			EnhancedForStatement bestMatchStmtEnhancedFor = (EnhancedForStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2EnhancedFor.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtEnhancedFor.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);

		} else if (stmt2 instanceof IfStatement && bestMatchStmt instanceof IfStatement) {
			// if statement
			IfStatement stmt2If = (IfStatement) stmt2;
			IfStatement bestMatchStmtIf = (IfStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2If.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtIf.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);

		} else if (stmt2 instanceof WhileStatement && bestMatchStmt instanceof WhileStatement) {
			WhileStatement stmt2While = (WhileStatement) stmt2;
			WhileStatement bestMatchStmtWhile = (WhileStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2While.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtWhile.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);

		} else if (stmt2 instanceof SwitchStatement && bestMatchStmt instanceof SwitchStatement) {
			// switch statement
			SwitchStatement stmt2Switch = (SwitchStatement) stmt2;
			SwitchStatement bestMatchStmtSwitch = (SwitchStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2Switch.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtSwitch.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);
		} else if (stmt2 instanceof SynchronizedStatement && bestMatchStmt instanceof SynchronizedStatement) {

			SynchronizedStatement stmt2Switch = (SynchronizedStatement) stmt2;
			SynchronizedStatement bestMatchStmtSwitch = (SynchronizedStatement) bestMatchStmt;

			Expression stmt2Expression = stmt2Switch.getExpression();
			Expression bestMatchStmtExpression = bestMatchStmtSwitch.getExpression();

			if (stmt2Expression.toString().equals(bestMatchStmtExpression.toString()))
				stmtObject2.setExpChanged(false);
			else
				stmtObject2.setExpChanged(true);
		}
	}

	private List<AttributePart> filterAttributePart(List<AttributePart> fieldObjects1,
			List<AttributePart> fieldObjects2) {
		List<AttributePart> clonedFieldObjects2 = new ArrayList<AttributePart>(fieldObjects2);
		List<AttributePart> candidateRemovalPart = new ArrayList<AttributePart>();
		for (AttributePart attributePart2 : fieldObjects2) {
			String attributeAsString1 = attributePart2.getFieldDecl().toString();
			String attributeCalssName1 = attributePart2.getClassName();
			for (AttributePart attributePart1 : fieldObjects1) {
				String attributeAsString2 = attributePart1.getFieldDecl().toString();
				String attributeClassName2 = attributePart1.getClassName();
				if (attributeCalssName1.equals(attributeClassName2) && attributeAsString1.equals(attributeAsString2)) {
					candidateRemovalPart.add(attributePart2);
					break;
				}
			}
		}
		for (AttributePart attributePart : candidateRemovalPart) {
			clonedFieldObjects2.remove(attributePart);
		}
		return clonedFieldObjects2;
	}

}

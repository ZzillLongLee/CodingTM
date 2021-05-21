package commit_task_visualization.code_change_extraction.development_flow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.SubCodeChunk;


public class DuplicatedFlowFilter {

	private boolean isMethodFlow = false;

	public DuplicatedFlowFilter() {

	}

	public SubCodeChunk filterDuplicatedFlow(SubCodeChunk codeChunk) {
		List<MethodPart> duplicatedMethodList = new ArrayList<MethodPart>();
		List<MethodPart> methodPartSet = codeChunk.getMethodPartSet();
		List<MethodPart> testMethodPartSet = codeChunk.getTestMethodPartSet();
		List<MethodPart> mergedMethodPartSet = ListUtils.union(methodPartSet, testMethodPartSet);

		List<AttributePart> duplicatedAttributeList = new ArrayList<AttributePart>();

		for (MethodPart methodPart : mergedMethodPartSet) {
			List<MethodPart> cloneMergedMethodPartSet = new ArrayList<MethodPart>(mergedMethodPartSet);
			cloneMergedMethodPartSet.remove(methodPart);
			boolean hasFlow = searchCodeElementInTheFlow(methodPart, cloneMergedMethodPartSet);
			System.out.println();
			if (hasFlow == true && !duplicatedMethodList.contains(methodPart)) {
				duplicatedMethodList.add(methodPart);
			}
		}
		methodPartSet.removeAll(duplicatedMethodList);
		codeChunk.setMethodPartSet(methodPartSet);
		testMethodPartSet.removeAll(duplicatedMethodList);
		codeChunk.setTestMethodPartSet(testMethodPartSet);

		List<AttributePart> attributePartSet = codeChunk.getAttributePartSet();
		for (AttributePart attributePart : attributePartSet) {
			boolean hasFlow = searchCodeElementInTheFlow(attributePart, mergedMethodPartSet);
			if (hasFlow == true && !duplicatedAttributeList.contains(attributePart))
				duplicatedAttributeList.add(attributePart);
		}
		attributePartSet.removeAll(duplicatedAttributeList);
		codeChunk.setAttributePartSet(attributePartSet);
		
		return codeChunk;
	}

	private boolean searchCodeElementInTheFlow(Object codePart, List<MethodPart> clonedMethodPartSet) {
		if (codePart instanceof MethodPart) {
			MethodPart methodPart = (MethodPart) codePart;
			String methodID = methodPart.getID();
			for (MethodPart clonedMethodPart : clonedMethodPartSet) {
				boolean validateValue = false;
				validateValue = searchID(methodID, clonedMethodPart, null);
				if (validateValue == true)
					return validateValue;
			}
		}
		if (codePart instanceof AttributePart) {
			AttributePart attributePart = (AttributePart) codePart;
			String attributeID = attributePart.getID();
			for (MethodPart clonedMethodPart : clonedMethodPartSet) {
				boolean validateValue = false;
				validateValue = searchID(attributeID, clonedMethodPart, null);
				if (validateValue == true)
					return validateValue;
			}
		}
		return false;
	}

	private boolean searchID(String codePartID, Object clonedCodePart, MethodPart causedByMethod) {
		boolean validationValue = false;
		MethodPart clonedMethodPart = null;
		AttributePart clonedAttributePart = null;
		String clonedCodePartID = null;

		if (clonedCodePart instanceof MethodPart) {
			clonedMethodPart = (MethodPart) clonedCodePart;
			clonedCodePartID = clonedMethodPart.getID();
		}
		if (clonedCodePart instanceof AttributePart) {
			clonedAttributePart = (AttributePart) clonedCodePart;
			clonedCodePartID = clonedAttributePart.getID();
		}

		if (codePartID.equals(clonedCodePartID))
			return true;
		else {
			if (clonedMethodPart != null) {
				List<StatementPart> stmts = clonedMethodPart.getStatements();
				if (stmts != null && stmts.size() != 0) {
					for (StatementPart stmtPart : stmts) {
						List<MethodPart> connectedMethods = stmtPart.getConnectedMethods();
						for (MethodPart connectedMethod : connectedMethods) {
							if (connectedMethod.getID().equals(codePartID))
								return true;
							else {
								// This part must be modified later, cuz it makes bugs								
								if(causedByMethod != null && causedByMethod.equals(connectedMethod))
									return true;
								causedByMethod = clonedMethodPart;
								validationValue = searchID(codePartID, connectedMethod, causedByMethod);
								if(validationValue == true)
									return validationValue;
							}
						}
						List<AttributePart> connectedAttributeParts = stmtPart.getConnectedAttributes();
						for (AttributePart connectedAttributePart : connectedAttributeParts) {
							validationValue = searchID(codePartID, connectedAttributePart, causedByMethod);
							if(validationValue == true)
								return validationValue;
						}
					}
				}
				//	TODO: the child method need to make a connection flow then i gotta search within these methods one by one.
				List<MethodPart> childMethods = clonedMethodPart.getChildMethods();
				for (MethodPart childMethod : childMethods) {
					if (childMethod.getID().equals(codePartID))
						return true;
				}
			}
		}
		return validationValue;
	}

}

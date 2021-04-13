package commit_task_visualization.code_change_extraction.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;


public class MethodPart implements Serializable {

	private boolean isUsed = false;
	private MethodDeclaration methodDecl;
	private InsideClassChangeType changedType;
	private MethodPart matchedMethod;
	private List<StatementPart> statements;
	private String methodName;
	private String parametersAsString;
	private String methodAsString;
	private Type returnType;
	private String className;
	private int parametersSize;
	private String uniqueID;
	private String methodSignature;

	private List<MethodPart> connectedMethods = new ArrayList<MethodPart>();
	private List<AttributePart> connectedAttributes = new ArrayList<AttributePart>();
	private List<MethodPart> childMethods = new ArrayList<MethodPart>();

	public MethodPart(String packageName, MethodDeclaration node, List<StatementPart> statements,
			SimpleName className) {
		this.className = className.getFullyQualifiedName();
		this.statements = statements;
		this.methodDecl = node;
		this.methodAsString = methodDecl.toString();
		this.returnType = methodDecl.getReturnType2();
		this.methodName = methodDecl.getName().getFullyQualifiedName();
		List parameters = methodDecl.parameters();
		this.parametersSize = parameters.size();

		StringBuffer paramAsStringBuffer = new StringBuffer();
		for (int i = 0; i < parameters.size(); i++) {
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(i);
			Type paramType = parameter.getType();
			SimpleName paramName = parameter.getName();
			paramAsStringBuffer.append(paramType.toString() + " " + paramName).append(",");
		}
		if (parameters.size() != 0) {
			parametersAsString = paramAsStringBuffer.toString().substring(0,
					paramAsStringBuffer.toString().length() - 1);
		} else
			this.parametersAsString = "";

		this.methodSignature = getMethodSignature(node);
		this.uniqueID = packageName + " " + className.getFullyQualifiedName() + " " + methodSignature;
	}

	private String getMethodSignature(MethodDeclaration node) {
		if (methodSignature == null) {
			Javadoc javaDoc = methodDecl.getJavadoc();
			if (javaDoc != null) {
				String javaDocString = javaDoc.toString();
				methodAsString = methodAsString.replace(javaDocString, "");
			}
			this.methodSignature = methodAsString.substring(0, methodAsString.indexOf(Constants.KEY_WORD_NEWLINE));
		}
		return methodSignature;
	}

	public MethodDeclaration getMethodDecl() {
		return methodDecl;
	}

	public InsideClassChangeType getChangedType() {
		return changedType;
	}

	public void setChangedType(InsideClassChangeType changedType) {
		this.changedType = changedType;
	}

	public List<StatementPart> getStatements() {
		return statements;
	}

	public MethodPart getMatchedMethod() {
		return matchedMethod;
	}

	public void setMatchedMethod(MethodPart matchedMethod) {
		this.matchedMethod = matchedMethod;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getParametersAsString() {
		return parametersAsString;
	}

	public Type getReturnType() {
		return returnType;
	}

	public String getReturnTypeAsString() {
		if (returnType == null)
			return "";
		else
			return returnType.toString();
	}

	public String getMethodAsString() {
		if (methodAsString == null) {
			List modifier = methodDecl.modifiers();
			Object firstModifier = modifier.get(0);
			if (firstModifier instanceof Modifier) {
				Modifier mod = (Modifier) firstModifier;
				String modAsString = mod.toString();
				return methodAsString.substring(methodAsString.indexOf(modAsString),
						methodAsString.lastIndexOf(Constants.LAST_BLOCK) + 1);
			}
			if (firstModifier instanceof MarkerAnnotation) {
				MarkerAnnotation ma = (MarkerAnnotation) firstModifier;
				String maAsString = ma.toString();
				return methodAsString.substring(methodAsString.indexOf(maAsString),
						methodAsString.lastIndexOf(Constants.LAST_BLOCK) + 1);
			}
		}
		return methodAsString;
	}

	public String getClassName() {
		return className;
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	public int getParametersSize() {
		return parametersSize;
	}

	public String getID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void linkChildMethod(MethodPart methodPart) {
		this.childMethods.add(methodPart);
	}

	public List<MethodPart> getChildMethods() {
		return this.childMethods;
	}

	public List<MethodPart> getConnectedMethods() {
		return connectedMethods;
	}

	public void setConnectedMethods(List<MethodPart> connectedMethods) {
		List<MethodPart> listWithoutDuplicates = new ArrayList<>(new HashSet<>(connectedMethods));
		this.connectedMethods = listWithoutDuplicates;
	}

	public void setConnectedAttributes(List<AttributePart> connectedAttributes) {
		List<AttributePart> listWithoutDuplicates = new ArrayList<>(new HashSet<>(connectedAttributes));
		this.connectedAttributes = listWithoutDuplicates;
	}

	public List<AttributePart> getConnectedAttributes() {
		return connectedAttributes;
	}

}

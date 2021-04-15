package commit_task_visualization.code_change_extraction.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.CodeChunkPreprocessor;
import commit_task_visualization.code_change_extraction.util.Constants;

public class ClassPart implements Serializable {

	private boolean isAbstract;
	private boolean isInterface;
	private String superClassName;
	private InsideClassChangeType classIdentifierState;
	private String classIdentifier;
	private String className;
	private List<ClassPart> connectedClassParts = new ArrayList<ClassPart>();

	private List<MethodPart> methodParts = new ArrayList<MethodPart>();
	private List<AttributePart> attributeParts = new ArrayList<AttributePart>();
	private String classConnection;
	private String uniqueID;
	private List modifiers;
	private String classString;

	public ClassPart(String packageName, TypeDeclaration node) {
		this.classString = node.toString();
		this.classIdentifier = CodeChunkPreprocessor.extractClassIdentifier(this.classString, node);
		this.className = node.getName().getFullyQualifiedName();
		this.uniqueID = packageName + Constants.SEPERATOR + className;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public InsideClassChangeType getClassIdentifierState() {
		return classIdentifierState;
	}

	public String getClassName() {
		return className;
	}

	public void setClassIdentifierState(InsideClassChangeType classIdentifierState) {
		this.classIdentifierState = classIdentifierState;
	}

	public String getClassIdentifier() {
		return classIdentifier;
	}

	public void setConnectFlow(ClassPart classPart) {
		this.connectedClassParts.add(classPart);
	}

	public List<ClassPart> getConnectedParts() {
		return connectedClassParts;
	}

	public List<MethodPart> getMethodParts() {
		return methodParts;
	}

	public void setMethodPart(MethodPart methodPart) {
		this.methodParts.add(methodPart);
	}

	public List<AttributePart> getAttributeParts() {
		return attributeParts;
	}

	public void setAttributePart(AttributePart attributePart) {
		this.attributeParts.add(attributePart);
	}

	public void setStringClassConnection(String classConnection) {
		this.classConnection = classConnection;
	}

	public String getID() {
		return uniqueID;
	}

}

package commit_task_visualization.code_change_extraction.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import commit_task_visualization.code_change_extraction.model.sub_chunk.ClassInstanceCreationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.FieldAccessPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.MethodInvocationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.QualifiedNamePart;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;

public class AttributePart implements Serializable {

	private FieldDeclaration fieldDecl;
	private InsideClassChangeType changedType;
	private String matchedAttribute;
	private String parentClassName;

	private List<MethodInvocationPart> methodInvoPartList;
	private List<ClassInstanceCreationPart> classInstanceCrePartList;
	private List<FieldAccessPart> fieldAccessPartList;
	private List<QualifiedNamePart> qualifiedNamePartList;
	private String uniqueID;
	private ArrayList<String> namesAsString;

	public AttributePart(String packageName, FieldDeclaration node, List<SimpleName> names,
			SimpleName parentClassName) {
		this.fieldDecl = node;
		this.namesAsString = new ArrayList<String>();
		for (SimpleName name : names) {
			namesAsString.add(name.toString());
		}
		this.parentClassName = parentClassName.getFullyQualifiedName();
		this.uniqueID = packageName + Constants.SEPERATOR + parentClassName.getFullyQualifiedName()
				+ Constants.SEPERATOR + node.toString();
	}

	public String getFieldContent() {
		return fieldDecl.toString();
	}

	public FieldDeclaration getFieldDecl() {
		return fieldDecl;
	}

	public InsideClassChangeType getChangedType() {
		return changedType;
	}

	public void setChangedType(InsideClassChangeType changedType) {
		this.changedType = changedType;
	}

	public String getMatchedAttribute() {
		return matchedAttribute;
	}

	public void setMatchedAttribute(String matchedAttribute) {
		this.matchedAttribute = matchedAttribute;
	}

	public String getAttributeAsString() {
		return fieldDecl.toString();
	}

	public List<String> getNames() {
		return namesAsString;
	}

	public String getClassName() {
		return parentClassName;
	}

	public List<MethodInvocationPart> getMethodInvoPartList() {
		return methodInvoPartList;
	}

	public void setMethodInvoPartList(List<MethodInvocationPart> methodInvoPartList) {
		this.methodInvoPartList = methodInvoPartList;
	}

	public List<ClassInstanceCreationPart> getClassInstanceCrePartList() {
		return classInstanceCrePartList;
	}

	public void setClassInstanceCrePartList(List<ClassInstanceCreationPart> classInstanceCrePartList) {
		this.classInstanceCrePartList = classInstanceCrePartList;
	}

	public List<FieldAccessPart> getFieldAccessPartList() {
		return fieldAccessPartList;
	}

	public void setFieldAccessPartList(List<FieldAccessPart> fieldAccessPartList) {
		this.fieldAccessPartList = fieldAccessPartList;
	}

	public List<QualifiedNamePart> getQualifiedNamePartList() {
		return qualifiedNamePartList;
	}

	public void setQualifiedNamePartList(List<QualifiedNamePart> qualifiedNamePartList) {
		this.qualifiedNamePartList = qualifiedNamePartList;
	}

	public String getID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

}

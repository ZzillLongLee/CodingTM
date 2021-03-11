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
import commit_task_visualization.code_change_extraction.util.Constants;


public class ClassPart implements Serializable {

	private boolean isAbstract;
	private boolean isInterface;
	private String superClassName;
	private InsideClassChangeType classIdentifierState;
	private String classModifiersAsString;
	private String classIdentifier;
	private String className;
	private List<ClassPart> connectedClassParts = new ArrayList<ClassPart>();

	private List<MethodPart> methodParts = new ArrayList<MethodPart>();
	private List<AttributePart> attributeParts = new ArrayList<AttributePart>();
	private String classConnection;
	private String uniqueID;
	private List modifiers;
	private String javaDoc;
	private String classString;

	public ClassPart(String packageName, TypeDeclaration node) {
		classString = node.toString();
		this.className = node.getName().getFullyQualifiedName();
		Javadoc javadoc = node.getJavadoc();
		if (javadoc != null)
			javaDoc = javadoc.toString();
		this.modifiers = node.modifiers();
		StringBuffer sb = new StringBuffer();
		if (modifiers.size() != 0) {
			for (Object object : modifiers) {
				if (object instanceof Modifier) {
					Modifier modifier = (Modifier) object;
					sb.append(modifier.toString() + " ");
				}
			}
			this.classModifiersAsString = sb.toString().substring(0, sb.toString().length() - 1);
		}
		
		this.uniqueID = packageName+" " + className;
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
		if (classIdentifier == null) {
			if (modifiers.size() != 0) {
				if (javaDoc != null) {
					classString = classString.replace(javaDoc, "");
				}
				for (Object modifier : modifiers) {
					if (modifier instanceof Modifier) {
						Modifier mod = (Modifier) modifier;
						String modAsString = mod.toString();
						this.classIdentifier = classString.substring(classString.indexOf(modAsString),
								classString.indexOf(Constants.START_BLOCK+"\n"));
					}
//					if (modifier instanceof MarkerAnnotation) {
//						MarkerAnnotation ma = (MarkerAnnotation) modifier;
//						String maAsString = ma.toString();
//						this.classIdentifier = classString.substring(classString.indexOf(maAsString),
//								classString.indexOf(Constants.START_BLOCK));
//					}
				}
			} else {
				this.classIdentifier = classString.substring(classString.indexOf(Constants.CLASS_KEY_WORD),
						classString.indexOf(Constants.START_BLOCK));
			}
		}
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

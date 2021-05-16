package commit_task_visualization.code_change_extraction.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.util.Constants;

public class SourceCodeVisitor extends ASTVisitor {

	private List<MethodPart> methodObjects;
	private List<AttributePart> fieldObjects;
	private List<ClassPart> classParts;
	private String packageName;
	private String commitID;

	public SourceCodeVisitor(String commitID, List<ClassPart> prevClsParts, List<AttributePart> fieldObjects,
			List<MethodPart> methodObjects, String filePath) {
		this.commitID = commitID;
		this.classParts = prevClsParts;
		this.methodObjects = methodObjects;
		this.fieldObjects = fieldObjects;
	}

	public boolean visit(PackageDeclaration node) {
		packageName = commitID.trim() + Constants.SEPERATOR + node.getName().getFullyQualifiedName();
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		ClassPart classPart = new ClassPart(packageName, node);
		// handling nested classes
		Type superClass = node.getSuperclassType();
		List superInterfaceClasses = node.superInterfaceTypes();
		List modifiers = node.modifiers();
		List<String> superClasses = buildSuperClassesSet(superClass, superInterfaceClasses);
		classPart.setParentClasses(superClasses);
		if (node.isInterface() == true)
			classPart.setInterface(true);

		for (Object object : modifiers) {
			if (object instanceof Modifier) {
				Modifier modifier = (Modifier) object;
				if (modifier.isAbstract())
					classPart.setAbstract(true);
			}
		}

		SimpleName className = node.getName();
		FieldDeclaration[] fields = node.getFields();
		classPart.setAttributeSize(fields.length);
		for (FieldDeclaration fieldDeclaration : fields) {
			String field = fieldDeclaration.toString();

			AttributeVisitor attributeParser = new AttributeVisitor(fieldDeclaration);
			List<SimpleName> names = attributeParser.getNames();
			AttributePart attributePart = new AttributePart(packageName, fieldDeclaration, names, className);
			fieldObjects.add(attributePart);
		}
		MethodDeclaration[] methods = node.getMethods();
		classPart.setMethodSize(methods.length);
		for (MethodDeclaration methodDeclaration : methods) {
//			if (methodDeclaration.getBody() != null) {
			MethodVisitor methodParser = new MethodVisitor(methodDeclaration, className);
			List statements = methodParser.getStatements();
			methodObjects.add(new MethodPart(packageName, methodDeclaration, statements, className));
//			}
		}
		classParts.add(classPart);
		return true;
	}

	private List<String> buildSuperClassesSet(Type superClass, List superInterfaceClasses) {
		List<String> superClasses = new ArrayList<String>();
		if (superClass != null)
			superClasses.add(superClass.toString());
		for (Object obj : superInterfaceClasses) {
			if (obj instanceof SimpleType) {
				SimpleType interfaceClass = (SimpleType) obj;
				superClasses.add(interfaceClass.toString());
			}
		}
		return superClasses;
	}

}
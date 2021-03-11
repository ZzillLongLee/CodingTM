package commit_task_visualization.code_change_extraction.ast;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.MethodPart;


public class SourceCodeVisitor extends ASTVisitor {

	private List<MethodPart> methodObjects;
	private List<AttributePart> fieldObjects;
	private HashMap<String, Type> fields = new HashMap<String, Type>();
	private String filePath;
	private List<ClassPart> classParts;
	private String packageName;
	private String commitID;

	public SourceCodeVisitor(String commitID, List<ClassPart> prevClsParts, List<AttributePart> fieldObjects,
			List<MethodPart> methodObjects, String filePath) {
		this.commitID = commitID;
		this.classParts = prevClsParts;
		this.methodObjects = methodObjects;
		this.fieldObjects = fieldObjects;
		this.filePath = filePath;
	}

	public boolean visit(PackageDeclaration node) {
		packageName = commitID.trim()+ " " + node.getName().getFullyQualifiedName();
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		ClassPart classPart = new ClassPart(packageName, node);
		// handling nested classes
		Type superClass = node.getSuperclassType();
		List modifiers = node.modifiers();
		if (superClass != null) {
			classPart.setSuperClassName(superClass.toString());
		}
		if (node.isInterface() == true)
			classPart.setInterface(true);

		for (Object object : modifiers) {
			if (object instanceof Modifier) {
				Modifier modifier = (Modifier) object;
				if (modifier.isAbstract())
					classPart.setAbstract(true);
			}
		}
		classParts.add(classPart);

		SimpleName className = node.getName();
		FieldDeclaration[] fields = node.getFields();
		for (FieldDeclaration fieldDeclaration : fields) {
			String field = fieldDeclaration.toString();

			AttributeVisitor attributeParser = new AttributeVisitor(fieldDeclaration);
			List<SimpleName> names = attributeParser.getNames();
			AttributePart attributePart = new AttributePart(packageName, fieldDeclaration, names, className);
			fieldObjects.add(attributePart);
		}
		MethodDeclaration[] methods = node.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
//			if (methodDeclaration.getBody() != null) {
			MethodVisitor methodParser = new MethodVisitor(methodDeclaration, className);
			List statements = methodParser.getStatements();
			methodObjects.add(new MethodPart(packageName, methodDeclaration, statements, className));
//			}
		}
		return true;
	}

}
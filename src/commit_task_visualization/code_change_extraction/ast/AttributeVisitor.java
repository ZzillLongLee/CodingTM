package commit_task_visualization.code_change_extraction.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.compiler.impl.Constant;

import commit_task_visualization.code_change_extraction.util.Constants;


public class AttributeVisitor {

	private FieldDeclaration fieldDeclaration;
	private List<SimpleName> names;

	public AttributeVisitor(FieldDeclaration node) {
		this.fieldDeclaration = node;
		parseField();
	}

	public List<SimpleName> getNames() {
		return names;
	}

	private void parseField() {
		// TODO Auto-generated method stub
		if (fieldDeclaration.toString() == null)
			return;
		names = new ArrayList<SimpleName>();
		String fieldAsString = fieldDeclaration.toString();

		String fieldWithTemplate = "public class AA{ " + fieldAsString.substring(0, fieldAsString.length() - 1) + "}";
		System.out.println();

		ASTSupportSingleton astSupport = ASTSupportSingleton.getInstance();
		astSupport.parse(fieldWithTemplate, new ASTVisitor() {

			public boolean visit(SimpleName simplename) {
				if (!simplename.toString().equals(Constants.DEFAULT_CLASS_NAME)) {
					names.add(simplename);
				}
				return true;
			}
			
		});
	}

}

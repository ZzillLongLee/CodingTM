package commit_task_visualization.code_change_extraction.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.ClassInstanceCreationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.FieldAccessPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.MethodInvocationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.QualifiedNamePart;


public class ExpressionVisitor {

	private List<MethodInvocationPart> methodInvoPartList;
	private List<ClassInstanceCreationPart> classInstanceCrePart;
	private List<FieldAccessPart> fieldAccessPart;
	private List<QualifiedNamePart> qualifiedNamePart;
	
	public void parsingExpression(String sourceCode, StatementPart stmtPart) {

		methodInvoPartList= new ArrayList<MethodInvocationPart>();
		classInstanceCrePart = new ArrayList<ClassInstanceCreationPart>();
		fieldAccessPart = new ArrayList<FieldAccessPart>();
		qualifiedNamePart = new ArrayList<QualifiedNamePart>();
		
		final ASTSupportSingleton astSupport = ASTSupportSingleton.getInstance();
		astSupport.expParse(sourceCode, new ASTVisitor() {
			
			public boolean visit(MethodInvocation methodInvocation) {
				methodInvoPartList.add(new MethodInvocationPart(methodInvocation));
				return true;
			}
			
			public boolean visit(ClassInstanceCreation classInstanceCreation) {
				classInstanceCrePart.add(new ClassInstanceCreationPart(classInstanceCreation));
				return true;
			}
			
			public boolean visit(FieldAccess fieldAccess) {
				fieldAccessPart.add(new FieldAccessPart(fieldAccess));
				return true;
			}

			public boolean visit(QualifiedName qualifiedName) {
				qualifiedNamePart.add(new QualifiedNamePart(qualifiedName));
				return true;
			}
		});
		
		stmtPart.setFieldAccessPart(fieldAccessPart);
		stmtPart.setMethodInvoPartList(methodInvoPartList);
		stmtPart.setQualifiedNamePart(qualifiedNamePart);
		stmtPart.setClassInstanceCrePart(classInstanceCrePart);
	}	
	
}

package commit_task_visualization.code_change_extraction.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


public class StatementParserTest {

	public static void main(String[] args) {

		String sourceCode = "double avgTime = getAverageTime(timeList);"; 
		ASTSupportSingleton astTest = ASTSupportSingleton.getInstance();
		astTest.stmtParse(sourceCode, new ASTVisitor() {
			
			
			public boolean visit(AssertStatement assertStatement) {
				return true;
			}
			
			public boolean visit(ThrowStatement node) {
				return true;
			}
			
			public boolean visit(ClassInstanceCreation node) {
				System.out.println(node.getType().toString());
				return true;
			}
			
			public boolean visit(ExpressionStatement node) {
				System.out.println();
				return true;
			}
			
			public boolean visit(MethodInvocation node) {
				System.out.println();
				return true;
			}
			
			public boolean visit(QualifiedName node) {
				String qualifiedName = node.getName().getIdentifier();
				String qualifier = node.getQualifier().getFullyQualifiedName();
				return true;
			}
		});
	}

}

package commit_task_visualization.code_change_extraction.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import commit_task_visualization.code_change_extraction.model.StatementPart;


public class MethodVisitor {

	private MethodDeclaration methodDeclaration;
	private List<StatementPart> statements;
	private SimpleName className;

	public MethodVisitor(MethodDeclaration node, SimpleName className) {
		methodDeclaration = node;
		this.className = className;
		parseMethodBody();
	}
	
	private void parseMethodBody() {
		if (methodDeclaration.getBody() == null)
			return;
		statements = new ArrayList<StatementPart>();
		String methodBodyAsString = methodDeclaration.toString();

		String methodBodyWithTemplate = "public class AA{ "
				+ methodBodyAsString.substring(0, methodBodyAsString.length() - 1) + "}";

		final ASTSupportSingleton astSupport = ASTSupportSingleton.getInstance();
		astSupport.parse(methodBodyWithTemplate, new ASTVisitor() {

			public boolean visit(BreakStatement breakStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, breakStatement);
				statements.add(new StatementPart(breakStatement, names, className, methodDeclaration));
				return true;
			}

			public boolean visit(ContinueStatement continueStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, continueStatement);
				statements.add(new StatementPart(continueStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(DoStatement doStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, doStatement);
				statements.add(new StatementPart(doStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(EnhancedForStatement enhancedForStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, enhancedForStatement);
				
				statements.add(new StatementPart(enhancedForStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(ExpressionStatement expressionStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, expressionStatement);
				statements.add(new StatementPart(expressionStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(ForStatement forStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, forStatement);
				statements.add(new StatementPart(forStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(IfStatement ifStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport,
						ifStatement);
				statements.add(new StatementPart(ifStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(LabeledStatement labeledStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, labeledStatement);
				statements.add(new StatementPart(labeledStatement, names, className, methodDeclaration));
				return true;
			}	
			
			public boolean visit(ReturnStatement returnStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, returnStatement);
				statements.add(new StatementPart(returnStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(SwitchStatement switchStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, switchStatement);
				statements.add(new StatementPart(switchStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(SynchronizedStatement synchronizedStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, synchronizedStatement);
				statements.add(new StatementPart(synchronizedStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(ThrowStatement trowStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, trowStatement);
				statements.add(new StatementPart(trowStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(TryStatement tryStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, tryStatement);
				statements.add(new StatementPart(tryStatement, names, className, methodDeclaration));
				return true;
			}

			public boolean visit(TypeDeclarationStatement typeDeclarationStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, typeDeclarationStatement);
				statements.add(new StatementPart(typeDeclarationStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, variableDeclarationStatement);
				statements.add(new StatementPart(variableDeclarationStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(WhileStatement whileStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, whileStatement);
				statements.add(new StatementPart(whileStatement, names, className, methodDeclaration));
				return true;
			}
			
			public boolean visit(AssertStatement assertStatement) {
				List<SimpleName> names = getStatementSimpleNames(astSupport, assertStatement);
				statements.add(new StatementPart(assertStatement, names, className, methodDeclaration));
				return true;
			}
		});
	}

	private List<SimpleName> getStatementSimpleNames(ASTSupportSingleton astSupport,
			Statement stmt) {
		final List<SimpleName> names = new ArrayList<SimpleName>();
		astSupport.stmtParse(stmt.toString(), new ASTVisitor() {
			public boolean visit(SimpleName node) {
				names.add(node);
				return true;
			}
		});
		return names;
	}
	
	public List<StatementPart> getStatements() {
		return statements;
	}

	public String getMethodBody() {
		return methodDeclaration.toString();
	}

}

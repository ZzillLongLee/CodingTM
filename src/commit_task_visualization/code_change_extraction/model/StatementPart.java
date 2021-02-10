package commit_task_visualization.code_change_extraction.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import commit_task_visualization.code_change_extraction.model.sub_chunk.ClassInstanceCreationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.FieldAccessPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.MethodInvocationPart;
import commit_task_visualization.code_change_extraction.model.sub_chunk.QualifiedNamePart;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;


public class StatementPart implements Serializable{
	
	private Statement stmt;
	private InsideClassChangeType changedType;
	private StatementPart matchedStatement;
	private ArrayList<String> namesAsString;
	private String className;
	//This attribute is applied at the for, if, while and switch statement.
	private boolean isExpChanged;
	
	private List<MethodInvocationPart> methodInvoPartList;
	private List<ClassInstanceCreationPart> classInstanceCrePart;
	private List<FieldAccessPart> fieldAccessPart;
	private List<QualifiedNamePart> qualifiedNamePart;
	
	private MethodDeclaration methodDecl;
	
	private List<MethodPart> connectedMethods;
	private List<AttributePart> connectedAttributes;
	private String uniqueID;
	
	public StatementPart(Statement stmt, List<SimpleName> names, SimpleName className, MethodDeclaration methodDeclaration) {
		this.uniqueID = UUID.randomUUID().toString();
		this.stmt = stmt;
		this.namesAsString = new ArrayList<String>();
		for (SimpleName name : names) {
			namesAsString.add(name.toString());
		}
		this.className = className.getFullyQualifiedName();
		this.methodDecl = methodDeclaration;
		this.connectedMethods = new ArrayList<MethodPart>();
		this.connectedAttributes = new ArrayList<AttributePart>();
	}

	public Statement getStmt() {
		return stmt;
	}

	public InsideClassChangeType getChangedType() {
		return changedType;
	}

	public void setChangedType(InsideClassChangeType changedType) {
		this.changedType = changedType;
	}

	public void setMatchedStatement(StatementPart bestMatchedStatement) {
		this.matchedStatement = bestMatchedStatement;
	}

	public StatementPart getMatchedStatement() {
		return matchedStatement;
	}

	public List<String> getNames() {
		return namesAsString;
	}
	
	public String getClassName() {
		return className;
	}

	public boolean isExpChanged() {
		return isExpChanged;
	}

	public void setExpChanged(boolean isExpChanged) {
		this.isExpChanged = isExpChanged;
	}

	public List<MethodInvocationPart> getMethodInvoPartList() {
		return methodInvoPartList;
	}

	public void setMethodInvoPartList(List<MethodInvocationPart> methodInvoPartList) {
		this.methodInvoPartList = methodInvoPartList;
	}

	public List<ClassInstanceCreationPart> getClassInstanceCrePart() {
		return classInstanceCrePart;
	}

	public void setClassInstanceCrePart(List<ClassInstanceCreationPart> classInstanceCrePart) {
		this.classInstanceCrePart = classInstanceCrePart;
	}

	public List<FieldAccessPart> getFieldAccessPart() {
		return fieldAccessPart;
	}

	public void setFieldAccessPart(List<FieldAccessPart> fieldAccessPart) {
		this.fieldAccessPart = fieldAccessPart;
	}

	public List<QualifiedNamePart> getQualifiedNamePart() {
		return qualifiedNamePart;
	}

	public void setQualifiedNamePart(List<QualifiedNamePart> qualifiedNamePart) {
		this.qualifiedNamePart = qualifiedNamePart;
	}

	public MethodDeclaration getMethodDecl() {
		return methodDecl;
	}

	public List<MethodPart> getConnectedMethods() {
		return new ArrayList<MethodPart>(new LinkedHashSet<MethodPart>(connectedMethods));
	}

	public void setConnectedMethod(MethodPart connectedMethod) {
		if(this.connectedMethods == null)
			this.connectedMethods = new ArrayList<MethodPart>();
		this.connectedMethods.add(connectedMethod);
	}

	public List<AttributePart> getConnectedAttributes() {
		return new ArrayList<AttributePart>(new LinkedHashSet<AttributePart>(connectedAttributes));
	}

	public void setConnectedAttribute(AttributePart connectedAttribute) {
		if(this.connectedAttributes == null)
			this.connectedAttributes = new ArrayList<AttributePart>();
		if(!this.connectedAttributes.contains(connectedAttribute))
			this.connectedAttributes.add(connectedAttribute);
	}
	
	public MethodPart getConnectedSingleMethod(MethodPart methodPart) {
		return connectedMethods.get(connectedMethods.indexOf(methodPart));
	}
	
	public String stmtAsString() {
		if(stmt instanceof ForStatement) {
			ForStatement stmtFor = (ForStatement) stmt;
			String forStmtAsString = stmtFor.toString();
			int index = forStmtAsString.indexOf(Constants.KEY_WORD_NEWLINE);
			forStmtAsString = forStmtAsString.substring(0, index);
			return forStmtAsString;
		}else if (stmt instanceof EnhancedForStatement) {
			EnhancedForStatement stmtEnhancedFor = (EnhancedForStatement) stmt;
			String stmtEnhancedForString = stmtEnhancedFor.toString();
			int index = stmtEnhancedForString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtEnhancedForString = stmtEnhancedForString.substring(0, index);
			return stmtEnhancedForString;
		}else if (stmt instanceof IfStatement) {
			IfStatement stmtIf = (IfStatement) stmt;
			Expression exp = stmtIf.getExpression();
			String expString = exp.toString();
			String stmtIfString = "if ("+expString +")";
			return stmtIfString;
		}else if (stmt instanceof WhileStatement){
			WhileStatement stmtWhile = (WhileStatement) stmt;
			String stmtWhileString = stmtWhile.toString();
			int index = stmtWhileString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtWhileString = stmtWhileString.substring(0, index);
			return stmtWhileString;
		}else if (stmt instanceof SwitchStatement){
			SwitchStatement stmtSwitch = (SwitchStatement) stmt;
			String stmtSwitchString = stmtSwitch.toString();
			int index = stmtSwitchString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtSwitchString = stmtSwitchString.substring(0, index);
			return stmtSwitchString;
		}else if (stmt instanceof SynchronizedStatement) {
			SynchronizedStatement stmtSynch = (SynchronizedStatement) stmt;
			String stmtSynchString = stmtSynch.toString();
			int index = stmtSynchString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtSynchString = stmtSynchString.substring(0, index);
			return stmtSynchString;
		}else if(stmt instanceof TryStatement) {
			TryStatement stmtTry = (TryStatement) stmt;
			String stmtTryString = stmtTry.toString();
			int index = stmtTryString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtTryString = stmtTryString.substring(0, index);
			return stmtTryString;
		}else if(stmt instanceof ThrowStatement) {
			ThrowStatement stmtTrow = (ThrowStatement) stmt;
			String stmtTrowString = stmtTrow.toString();
			int index = stmtTrowString.indexOf(Constants.KEY_WORD_NEWLINE);
			stmtTrowString = stmtTrowString.substring(0, index);
			return stmtTrowString;
		}
			return stmt.toString();
	}
	
	public String getID() {
		return uniqueID;
	}
}

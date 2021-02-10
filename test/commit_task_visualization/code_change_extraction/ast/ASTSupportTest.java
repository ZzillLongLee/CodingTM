package commit_task_visualization.code_change_extraction.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.junit.Test;

public class ASTSupportTest {

	String source = "public abstract class MisraTableViewColumn extends ColumnLabelProvider {\r\n" + 
			"  public abstract String getText(  Object element);\r\n" + 
			"  public abstract String getTitle();\r\n" + 
			"  public abstract int getWidth();\r\n" + 
			"  public TableViewerColumn addColumnTo(  TableViewer tableViewer){\r\n" + 
			"    TableViewerColumn tableViewerColumn=new TableViewerColumn(tableViewer,SWT.NONE);\r\n" + 
			"    TableColumn column=tableViewerColumn.getColumn();\r\n" + 
			"    column.setMoveable(false);\r\n" + 
			"    column.setResizable(false);\r\n" + 
			"    column.setText(getTitle());\r\n" + 
			"    column.setWidth(getWidth());\r\n" + 
			"    tableViewerColumn.setLabelProvider(this);\r\n" + 
			"    return tableViewerColumn;\r\n" + 
			"  }\r\n" + 
			"}";

	
	@Test
	public void generateAstTest2() {
		ASTSupportSingleton astTest = ASTSupportSingleton.getInstance();
		astTest.parse(source,  new ASTVisitor() {
			public boolean visit(CompilationUnit node) {
				System.out.println();
				return true;
			}
			
			public boolean visit(TypeDeclaration node) {
				List monodedi = node.modifiers();
				System.out.println(monodedi.size());
				return true;
			}
			
			public boolean visit(SuperMethodInvocation node) {
				System.out.println();
				return false;
			}
		});
	}
	

}

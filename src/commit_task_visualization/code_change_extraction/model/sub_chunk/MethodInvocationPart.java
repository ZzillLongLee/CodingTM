package commit_task_visualization.code_change_extraction.model.sub_chunk;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

public class MethodInvocationPart implements Serializable{

	private String methodName;
	private List methodArgu;
	private MethodInvocation methodInvocation;
	private QualifiedName qualifiedName;

	public MethodInvocationPart(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
		Expression exp = methodInvocation.getExpression();
		if(exp instanceof QualifiedName) {
			qualifiedName = (QualifiedName)exp;
		}
		this.methodName = methodInvocation.getName().getIdentifier();
		this.methodArgu = methodInvocation.arguments();
	}

	public String getMethodName() {
		return methodName;
	}

	public List getMethodArgu() {
		return methodArgu;
	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}
	
}

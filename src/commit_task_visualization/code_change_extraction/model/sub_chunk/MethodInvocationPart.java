package commit_task_visualization.code_change_extraction.model.sub_chunk;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationPart implements Serializable{

	private String methodName;
	private List methodArgu;
	private MethodInvocation methodInvocation;

	public MethodInvocationPart(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
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
	
}

package commit_task_visualization.code_change_extraction.model.sub_chunk;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Type;

public class ClassInstanceCreationPart implements Serializable{

	private Type creationType;
	private List creationArgu;
	private ClassInstanceCreation classInstanceCreation;

	public ClassInstanceCreationPart(ClassInstanceCreation classInstanceCreation) {
		this.classInstanceCreation = classInstanceCreation;
		this.creationType = classInstanceCreation.getType();
		this.creationArgu = classInstanceCreation.arguments();
	}

	public Type getCreationType() {
		return creationType;
	}

	public List getCreationArgu() {
		return creationArgu;
	}
	
	public int getCreationArguSize() {
		if(creationArgu != null)
			return creationArgu.size();
		return 0;
	}

	public ClassInstanceCreation getClassInstanceCreation() {
		return classInstanceCreation;
	}
	
}

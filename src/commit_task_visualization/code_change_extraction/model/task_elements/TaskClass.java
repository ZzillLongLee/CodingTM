package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;


public class TaskClass extends TaskElement implements Serializable{

	private String className;
	private String classIdentifier;
	private List<TaskMethod> codeChunkMethods = new ArrayList<TaskMethod>();
	private List<TaskAttribute> codeChunkAttributes = new ArrayList<TaskAttribute>();

	public TaskClass(ClassPart classPart) {
		super(classPart.getID(), null, null, classPart.getClassIdentifierState().name());
		this.className = classPart.getClassName();
		this.classIdentifier = classPart.getClassIdentifier().toString();
	}

	public void addCodeChunkMethod(TaskMethod codeChunkMethod) {
		this.codeChunkMethods.add(codeChunkMethod);
	}

	public void addCodeChunkAttribute(TaskAttribute codeChunkAttribute) {
		this.codeChunkAttributes.add(codeChunkAttribute);
	}

	public String getClassName() {
		return className;
	}

	public String getClassIdentifier() {
		return classIdentifier;
	}

	public List<TaskMethod> getTaskMethods() {
		return codeChunkMethods;
	}

	public List<TaskAttribute> getTaskAttributes() {
		return codeChunkAttributes;
	}

	public void setCodeChunkMethods(List<TaskMethod> codeChunkMethods) {
		this.codeChunkMethods = codeChunkMethods;
	}

	public void setCodeChunkAttributes(List<TaskAttribute> codeChunkAttributes) {
		this.codeChunkAttributes = codeChunkAttributes;
	}
	
}

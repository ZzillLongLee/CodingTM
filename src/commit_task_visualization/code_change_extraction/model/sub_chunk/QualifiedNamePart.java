package commit_task_visualization.code_change_extraction.model.sub_chunk;

import java.io.Serializable;

import org.eclipse.jdt.core.dom.QualifiedName;

public class QualifiedNamePart implements Serializable{

	private String qualifiedNameIdentifier;
	private String qualifier;
	private QualifiedName qualifiedName;

	public QualifiedNamePart(QualifiedName qualifiedName) {
		this.qualifiedName = qualifiedName;
		this.qualifiedNameIdentifier = qualifiedName.getName().getIdentifier();
		this.qualifier = qualifiedName.getQualifier().getFullyQualifiedName();
	}

	public String getQualifiedNameIdentifier() {
		return qualifiedNameIdentifier;
	}

	public String getQualifier() {
		return qualifier;
	}

	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

}

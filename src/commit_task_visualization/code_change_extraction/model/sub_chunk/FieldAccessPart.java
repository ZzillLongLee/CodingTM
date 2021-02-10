package commit_task_visualization.code_change_extraction.model.sub_chunk;

import java.io.Serializable;

import org.eclipse.jdt.core.dom.FieldAccess;

public class FieldAccessPart implements Serializable{

	private String fieldName;
	private FieldAccess fieldAcess;

	public FieldAccessPart(FieldAccess fieldAccess) {
		this.fieldAcess = fieldAccess;
		this.fieldName = fieldAccess.getName().getIdentifier();
	}

	public String getFieldName() {
		return fieldName;
	}

	public FieldAccess getFieldAcess() {
		return fieldAcess;
	}
	
}

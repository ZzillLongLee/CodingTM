package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskAttribute extends TaskElement implements Serializable, Cloneable {
	
	private String prevId;
	private String curID;
	private String className;
	private String matchedAttribute;
	private List<TaskMethod> parentTMs = new ArrayList<TaskMethod>();

	public TaskAttribute(TaskAttributeBuilder codeChunkAttributeBuilder) {
		super(codeChunkAttributeBuilder.elementID, codeChunkAttributeBuilder.pastCode,
				codeChunkAttributeBuilder.currentCode, codeChunkAttributeBuilder.changedType);
		this.className = codeChunkAttributeBuilder.className;
		this.prevId = codeChunkAttributeBuilder.prevID;
		this.curID = codeChunkAttributeBuilder.curID;
		this.matchedAttribute = codeChunkAttributeBuilder.matchedAttribute;
	}

	public String getPrevID() {
		return prevId;
	}

	public String getCurID() {
		return curID;
	}

	public String getClassName() {
		return className;
	}

	public String getMatchedAttribute() {
		return matchedAttribute;
	}

	public List<TaskMethod> getParentTMs() {
		return parentTMs;
	}

	public void addParentTM(TaskMethod ccm) {
		parentTMs.add(ccm);
	}

	public void setPrevID(String prevId) {
		this.prevId = prevId;
	}

	public void setCurID(String curID) {
		this.curID = curID;
	}

	public void setParentTMs(List<TaskMethod> parentTMs) {
		if (this.parentTMs.size() != 0) {
			this.parentTMs.clear();
		}
		this.parentTMs = parentTMs;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public static class TaskAttributeBuilder {

		private String elementID;
		private String prevID;
		private String curID;
		private String className;
		private String changedType;
		private String pastCode;
		private String currentCode;
		private String matchedAttribute;
		private List<TaskMethod> parentTMs;

		public TaskAttributeBuilder setPrevId(String prevId) {
			this.prevID = prevId;
			return this;
		}

		public TaskAttributeBuilder setCurID(String curID) {
			this.curID = curID;
			return this;
		}

		public TaskAttributeBuilder setClassName(String className) {
			this.className = className;
			return this;
		}

		public TaskAttributeBuilder setChangedType(String changedType) {
			this.changedType = changedType;
			return this;
		}

		public TaskAttributeBuilder setPrevAttributeCode(String prevAttributeCode) {
			this.pastCode = prevAttributeCode;
			return this;
		}

		public TaskAttributeBuilder setCurAttributeCode(String curAttributeCode) {
			this.currentCode = curAttributeCode;
			return this;
		}

		public TaskAttributeBuilder setMatchedAttribute(String matchedAttribute) {
			this.matchedAttribute = matchedAttribute;
			return this;
		}

		public TaskAttributeBuilder setParentTMs(List<TaskMethod> parentCCMs) {
			this.parentTMs = parentCCMs;
			return this;
		}

		public TaskAttribute build() {
			this.elementID = TaskElementUtil.generateTaskElementID(prevID, curID);
			return new TaskAttribute(this);
		}

	}
}

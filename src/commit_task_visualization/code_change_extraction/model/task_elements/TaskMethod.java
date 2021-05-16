package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TaskMethod extends TaskElement implements Cloneable, Serializable {

	/**
	 * 
	 */
	private String prevID;
	private String curID;
	private String className;
	private String methodSignature;
	private List<TaskStatement> statementsSet;
	private List<TaskMethod> childTMs;
	private List<TaskMethod> connectedTMs;
	private List<TaskAttribute> connectedTAs;
	private List<TaskMethod> parentTMs = new ArrayList<TaskMethod>();
	private List<TaskAttribute> parentTAs = new ArrayList<TaskAttribute>();

	public TaskMethod(TaskMethodBuilder codeChunkMethodBuilder) {
		super(codeChunkMethodBuilder.elementID, codeChunkMethodBuilder.prevMethodCode, codeChunkMethodBuilder.curMethodCode, codeChunkMethodBuilder.changeType);
		this.prevID = codeChunkMethodBuilder.prevID;
		this.curID = codeChunkMethodBuilder.curID;
		this.className = codeChunkMethodBuilder.className;
		this.methodSignature = codeChunkMethodBuilder.methodSignature;
		this.statementsSet = codeChunkMethodBuilder.statementsSet;
		this.connectedTAs = codeChunkMethodBuilder.connectedCCA;
		this.connectedTMs = codeChunkMethodBuilder.connectedCCM;
		this.childTMs = codeChunkMethodBuilder.childCCM;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public List<TaskStatement> getStatementsSet() {
		return statementsSet;
	}

	public List<TaskMethod> getConnectedTMs() {
		return connectedTMs;
	}

	public List<TaskAttribute> getConnectedTAs() {
		return connectedTAs;
	}

	public String getPrevID() {
		return prevID;
	}

	public String getCurID() {
		return curID;
	}

	public List<TaskMethod> getChildTMs() {
		return childTMs;
	}

	public void setPrevID(String prevID) {
		this.prevID = prevID;
	}

	public void setCurID(String curID) {
		this.curID = curID;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public void setChildTMs(List<TaskMethod> childTMs) {
		if(childTMs.size()!=0)
			childTMs.clear();
		this.childTMs = childTMs;
	}

	public void setConnectedTMs(List<TaskMethod> connectedTMs) {
		if(connectedTMs.size()!=0)
			connectedTMs.clear();
		this.connectedTMs = connectedTMs;
	}

	public void setConnectedTAs(List<TaskAttribute> connectedTAs) {
		if(connectedTAs.size()!=0)
			connectedTAs.clear();
		this.connectedTAs = connectedTAs;
	}

	public void setStatementsSet(List<TaskStatement> statementsSet) {
		this.statementsSet = statementsSet;
	}

	public List<TaskMethod> getParentTMs() {
		return parentTMs;
	}

	public List<TaskAttribute> getParentTAs() {
		return parentTAs;
	}

	public void addParentTM(TaskMethod parentCCM) {
		this.parentTMs.add(parentCCM);
	}

	public void addParentTA(TaskAttribute parentCCA) {
		this.parentTAs.add(parentCCA);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setParentTMs(List<TaskMethod> parentTMs) {
		if(this.parentTMs.size()!=0)
			this.parentTMs.clear();
		this.parentTMs = parentTMs;
	}

	public void setParentTAs(List<TaskAttribute> parentTAs) {
		if(this.parentTAs.size()!=0)
			this.parentTAs.clear();
		this.parentTAs = parentTAs;
	}

	public void setParentMethodToConnectElements() {
		if (connectedTAs != null && connectedTAs.size() != 0) {
			for (TaskAttribute connectedCCA : connectedTAs) {
				connectedCCA.addParentTM(this);
			}
		}
		if (connectedTMs != null && connectedTMs.size() != 0) {
			for (TaskMethod connectedCCM : connectedTMs) {
				connectedCCM.addParentTM(this);
			}
		}
		if (childTMs != null && childTMs.size() != 0) {
			for (TaskMethod childCCM : childTMs) {
				childCCM.addParentTM(this);
			}
		}

	}

	public static class TaskMethodBuilder {

		private String elementID;
		private String prevID;
		private String curID;
		private String className;
		private String methodSignature;
		private String changeType;
		private String prevMethodCode;
		private String curMethodCode;
		private List<TaskStatement> statementsSet;
		private List<TaskMethod> childCCM;
		private List<TaskMethod> connectedCCM;
		private List<TaskAttribute> connectedCCA;

		private List<TaskMethod> parentCCMs;
		private List<TaskAttribute> parentCCAs;
		private List<TaskMethod> parentChildCCM;

		public TaskMethodBuilder setClassName(String className) {
			this.className = className;
			return this;
		}

		public TaskMethodBuilder setMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
			return this;
		}

		public TaskMethodBuilder setChangeType(String changeType) {
			this.changeType = changeType;
			return this;
		}

		public TaskMethodBuilder setPrevMethodCode(String prevMethodCode) {
			this.prevMethodCode = prevMethodCode;
			return this;
		}

		public TaskMethodBuilder setCurMethodCode(String curMethodCode) {
			this.curMethodCode = curMethodCode;
			return this;
		}

		public TaskMethodBuilder setStatementsSet(List<TaskStatement> statementsSet) {
			this.statementsSet = statementsSet;
			return this;
		}

		public TaskMethodBuilder setConnectedCCM(List<TaskMethod> connectedCCM) {
			this.connectedCCM = connectedCCM;
			return this;
		}

		public TaskMethodBuilder setConnectedCCA(List<TaskAttribute> connectedCCA) {
			this.connectedCCA = connectedCCA;
			return this;
		}

		public TaskMethodBuilder setPrevID(String prevID) {
			this.prevID = prevID;
			return this;
		}

		public TaskMethodBuilder setCurID(String curID) {
			this.curID = curID;
			return this;
		}

		public TaskMethodBuilder setChildCCM(List<TaskMethod> childCCM) {
			this.childCCM = childCCM;
			return this;
		}

		public void setParentCCMs(List<TaskMethod> parentCCMs) {
			this.parentCCMs = parentCCMs;
		}

		public void setParentCCAs(List<TaskAttribute> parentCCAs) {
			this.parentCCAs = parentCCAs;
		}

		public void setParentChildCCM(List<TaskMethod> parentChildCCM) {
			this.parentChildCCM = parentChildCCM;
		}

		public TaskMethod build() {
			this.elementID = TaskElementUtil.generateTaskElementID(prevID, curID);
			return new TaskMethod(this);
		}

	}

}

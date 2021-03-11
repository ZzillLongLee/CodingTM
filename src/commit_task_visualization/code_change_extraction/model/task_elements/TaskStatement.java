package commit_task_visualization.code_change_extraction.model.task_elements;

import java.io.Serializable;
import java.util.List;


public class TaskStatement extends TaskElement implements Serializable {

	private List<String> connecteIdList;
	private String prevID;
	private String curID;

	public TaskStatement(TaskStatementBuilder taskStatementBuilder) {
		super(taskStatementBuilder.elementID, taskStatementBuilder.pastCode, taskStatementBuilder.currentCode,
				taskStatementBuilder.changeType);
		this.connecteIdList = taskStatementBuilder.connecteIdList;
		this.prevID = taskStatementBuilder.prevID;
		this.curID = taskStatementBuilder.curID;
	}

	public List<String> getconnecteIdList() {
		return connecteIdList;
	}

	public String getPrevID() {
		return prevID;
	}

	public String getCurID() {
		return curID;
	}



	public static class TaskStatementBuilder {

		private String elementID;
		private String prevID;
		private String curID;
		private String pastCode;
		private String currentCode;
		private String changeType;
		private List<String> connecteIdList;

		public TaskStatementBuilder setPrevID(String prevID) {
			this.prevID = prevID;
			return this;
		}

		public TaskStatementBuilder setCurID(String curID) {
			this.curID = curID;
			return this;
		}

		public TaskStatementBuilder setPastCode(String pastCode) {
			this.pastCode = pastCode;
			return this;
		}

		public TaskStatementBuilder setCurrentCode(String currentCode) {
			this.currentCode = currentCode;
			return this;
		}

		public TaskStatementBuilder setChangeType(String changeType) {
			this.changeType = changeType;
			return this;
		}

		public TaskStatementBuilder setConnecteIdList(List<String> connecteIdList) {
			this.connecteIdList = connecteIdList;
			return this;
		}

		public TaskStatement build() {
			this.elementID = TaskElementUtil.generateTaskElementID(prevID, curID);
			return new TaskStatement(this);
		}

	}

}

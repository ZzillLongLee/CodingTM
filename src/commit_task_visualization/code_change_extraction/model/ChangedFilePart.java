package commit_task_visualization.code_change_extraction.model;

import java.util.List;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Constants;


public class ChangedFilePart {

	private InsideClassChangeType changeType;
	private String curFilePath;
	private String prevFilePath;
	private String curVersionSourceCode;
	private String prevVersionSourceCode;
	private List<AttributePart> curVersionFieldObjects;
	private List<AttributePart> prevVersionFieldObjects;
	private List<MethodPart> curVersionMethodObjects;
	private List<MethodPart> prevVersionMethodObjects;
	private List<ClassPart> prevClassParts;
	private List<ClassPart> curClassParts;

	public ChangedFilePart(ChnagedFilePartBuilder changFilePartBuilder) {
		this.changeType = changFilePartBuilder.changeType;
		this.curFilePath = changFilePartBuilder.curFilePath;
		this.prevFilePath = changFilePartBuilder.prevFilePath;
		this.curVersionSourceCode = changFilePartBuilder.curVersionSourceCode;
		this.prevVersionSourceCode = changFilePartBuilder.prevVersionSourceCode;
		this.curVersionFieldObjects = changFilePartBuilder.curVersionFieldObjects;
		this.prevVersionFieldObjects = changFilePartBuilder.prevVersionFieldObjects;
		this.curVersionMethodObjects = changFilePartBuilder.curVersionMethodObjects;
		this.prevVersionMethodObjects = changFilePartBuilder.prevVersionMethodObjects;
		this.prevClassParts = changFilePartBuilder.prevClassPart;
		this.curClassParts = changFilePartBuilder.curClassPart;
	}

	public InsideClassChangeType getChangeType() {
		return changeType;
	}

	public String getChangedFilePath() {
		return curFilePath;
	}

	public String getSourceCode(int versionType) {
		if (versionType == Constants.CUR_VERSION)
			return curVersionSourceCode;
		if (versionType == Constants.PREV_VERSION)
			return prevVersionSourceCode;
		else
			throw new NullPointerException();
	}

	public List<ClassPart> getClassParts(int versionType) {
		if (versionType == Constants.CUR_VERSION)
			return curClassParts;
		if (versionType == Constants.PREV_VERSION)
			return prevClassParts;
		else
			throw new NullPointerException();
	}

	public List<AttributePart> getFieldObjects(int versionType) {
		if (versionType == Constants.CUR_VERSION)
			return curVersionFieldObjects;
		if (versionType == Constants.PREV_VERSION)
			return prevVersionFieldObjects;
		else
			throw new NullPointerException();
	}

	public List<MethodPart> getMethodObjects(int versionType) {
		if (versionType == Constants.CUR_VERSION)
			return curVersionMethodObjects;
		if (versionType == Constants.PREV_VERSION)
			return prevVersionMethodObjects;
		else
			throw new NullPointerException();
	}

	public static class ChnagedFilePartBuilder {

		private InsideClassChangeType changeType;
		private String curFilePath;
		private String prevFilePath;
		private String curVersionSourceCode;
		private String prevVersionSourceCode;
		private List<AttributePart> curVersionFieldObjects;
		private List<AttributePart> prevVersionFieldObjects;
		private List<MethodPart> curVersionMethodObjects;
		private List<MethodPart> prevVersionMethodObjects;
		private List<ClassPart> prevClassPart;
		private List<ClassPart> curClassPart;

		public ChnagedFilePartBuilder(ChangeType changeType, String curFilePath, String prevFilePath) {
			if (changeType.equals(changeType.ADD))
				this.changeType = InsideClassChangeType.ADD;
			else if (changeType.equals(changeType.DELETE))
				this.changeType = InsideClassChangeType.DELETE;
			else if (changeType.equals(changeType.MODIFY))
				this.changeType = InsideClassChangeType.MODIFIED;
			this.curFilePath = curFilePath;
			this.prevFilePath = prevFilePath;
		}

		public ChnagedFilePartBuilder setSourceCode(String curVersionSourceCode, String prevVersionSourceCode) {
			this.curVersionSourceCode = curVersionSourceCode;
			this.prevVersionSourceCode = prevVersionSourceCode;
			return this;
		}

		public ChnagedFilePartBuilder setFieldObjects(List<AttributePart> curVersionFieldObjects,
				List<AttributePart> prevVersionFieldObjects) {
			this.curVersionFieldObjects = curVersionFieldObjects;
			this.prevVersionFieldObjects = prevVersionFieldObjects;
			return this;
		}

		public ChnagedFilePartBuilder setMethodObjects(List<MethodPart> curVersionMethodObjects,
				List<MethodPart> prevVersionMethodObjects) {
			this.curVersionMethodObjects = curVersionMethodObjects;
			this.prevVersionMethodObjects = prevVersionMethodObjects;
			return this;
		}

		public ChnagedFilePartBuilder setClassPart(List<ClassPart> curClsParts, List<ClassPart> prevClsParts) {
			this.curClassPart = curClsParts;
			this.prevClassPart = prevClsParts;
			return this;
		}

		public ChangedFilePart build() {
			return new ChangedFilePart(this);
		}
	}
}

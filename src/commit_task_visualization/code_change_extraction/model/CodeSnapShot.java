package commit_task_visualization.code_change_extraction.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

public class CodeSnapShot {

	RevCommit commit;
	RevCommit prevCommit;
	private HashMap<DiffEntry, String> diffContents;
	private List<String> sourceCodeList;
	private List<ChangedFilePart> changedFileList;

	public CodeSnapShot() {

	}

	public CodeSnapShot(RevCommit prevCommit, RevCommit commit, HashMap<DiffEntry, String> diffContents) {
		this.prevCommit = prevCommit;
		this.commit = commit;
		this.diffContents = diffContents;
	}

	public String getCommitMsg() {
		return commit.getFullMessage();
	}

	public RevCommit getCommit() {
		return commit;
	}


	public HashMap<DiffEntry, String> getDiffContents() {
		return diffContents;
	}

	public List<String> getSourceCodeList() {
		return sourceCodeList;
	}

	public void setSourceCodeList(List<String> sourceCodeList) {
		this.sourceCodeList = sourceCodeList;
	}

	public boolean hasPrevCommit() {
		if (prevCommit == null)
			return false;
		else
			return true;
	}

	public RevCommit getPrevCommit() {
		return prevCommit;
	}

	public void setChangedFileList(List<ChangedFilePart> changedFileList) {
		this.changedFileList = changedFileList;
	}

	public List<ChangedFilePart> getChangedFileList() {
		return changedFileList;
	}

}

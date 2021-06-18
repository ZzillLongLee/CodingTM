package commit_task_visualization.code_change_extraction.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.util.Constants;

public class CommitFilter {

	private Repository repo;
	private Git git;
	private RevWalk walk;
	private CommitDiffGenerator commitDiffGenerator;

	public CommitFilter(GitRepositoryGenerator gitRepositoryGenerator) {
		git = gitRepositoryGenerator.getGit();
		repo = gitRepositoryGenerator.getRepo();
		walk = gitRepositoryGenerator.getWalk();
		commitDiffGenerator = new CommitDiffGenerator(gitRepositoryGenerator);
	}

	public List<CodeSnapShot> filterCommits(Iterable<RevCommit> commits, String keyWord)
			throws IOException, GitAPIException {
		int size = 0;
		List<CodeSnapShot> codeChunkList = new ArrayList<CodeSnapShot>();
		for (RevCommit commit : commits) {
			String commitMsg = commit.getFullMessage();
			List<DiffEntry> diffs;
			if (commit.getId().toString().contains(keyWord)) {
				System.out.println("Commit MSG:" + commitMsg);
				RevCommit[] prevCommit = commit.getParents();
				if (prevCommit.length != 0) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					if(diffContents.size() == 0)
						size++;
					codeChunkList.add(new CodeSnapShot(prevCommit[0], commit, diffContents));
				}
			} else if (commitMsg.contains(keyWord)) {
				System.out.println("Commit MSG:" + commitMsg);
				RevCommit[] prevCommit = commit.getParents();
				if (prevCommit.length != 0) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot(prevCommit[0], commit, diffContents));
				}
			} else if (keyWord.equals(Constants.KEY_WORD_EMPTY)) {
				RevCommit[] prevCommit = commit.getParents();
				if (prevCommit.length != 0) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot(prevCommit[0], commit, diffContents));
				}
			}
		}
		System.out.println("This size of commit has no java files"+size);
		return codeChunkList;
	}

	private List<DiffEntry> generateDiff(RevCommit prevCommit, RevCommit commit) throws IOException, GitAPIException {
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(commit);
		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(prevCommit);
		List<DiffEntry> diffs = git.diff().setNewTree(newTreeIterator).setOldTree(oldTreeIterator).call();
		return diffs;
	}

	private boolean hasTest(List<DiffEntry> diffs) {
		for (DiffEntry diffEntry : diffs) {
			String changedFilePath = diffEntry.getNewPath();
			if (changedFilePath.contains(Constants.TEST_JAVA))
				return true;
		}
		return false;
	}

	private AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId) throws IOException {
		RevCommit commit = walk.parseCommit(commitId);
		ObjectId treeId = commit.getTree().getId();
		ObjectReader reader = repo.newObjectReader();
		return new CanonicalTreeParser(null, reader, treeId);
	}
}

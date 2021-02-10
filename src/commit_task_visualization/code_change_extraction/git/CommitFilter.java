package commit_task_visualization.code_change_extraction.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		List<CodeSnapShot> codeChunkList = new ArrayList<CodeSnapShot>();
		RevCommit targetCommit = null;
		for (RevCommit commit : commits) {
			if (targetCommit != null) {
				String commitMsg = targetCommit.getFullMessage();
				AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(targetCommit);
				AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(commit);
				List<DiffEntry> diffs = git.diff().setNewTree(newTreeIterator).setOldTree(oldTreeIterator).call();
				if (targetCommit.getId().toString().contains(keyWord)) {
					System.out.println("Commit MSG:" + commitMsg);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot(targetCommit, commit, diffContents));
				} else if (commitMsg.contains(keyWord)) {
					System.out.println("Commit MSG:" + commitMsg);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot(targetCommit, commit, diffContents));
				} else if (keyWord.equals(Constants.KEY_WORD_EMPTY)) {
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot(targetCommit, commit, diffContents));
				}
			}
			// This previous commit is the after version of commit.
			targetCommit = commit;
		}
		return codeChunkList;
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

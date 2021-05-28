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
		List<CodeSnapShot> codeChunkList = new ArrayList<CodeSnapShot>();
		for (RevCommit commit : commits) {
			String commitMsg = commit.getFullMessage();
			List<DiffEntry> diffs;
			if (commit.getId().toString().contains(keyWord)) {
				System.out.println("Commit MSG:" + commitMsg);
				RevCommit[] prevCommit = commit.getParents();
				if(prevCommit != null) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot( prevCommit[0], commit, diffContents));
				}
			} else if (commitMsg.contains(keyWord)) {
				System.out.println("Commit MSG:" + commitMsg);
				RevCommit[] prevCommit = commit.getParents();
				if(prevCommit != null) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot( prevCommit[0], commit, diffContents));
				}
			} else if (keyWord.equals(Constants.KEY_WORD_EMPTY)) {
				RevCommit[] prevCommit = commit.getParents();
				if(prevCommit != null) {
					diffs = generateDiff(prevCommit[0], commit);
					HashMap<DiffEntry, String> diffContents = commitDiffGenerator.generateDiffContents(diffs);
					codeChunkList.add(new CodeSnapShot( prevCommit[0], commit, diffContents));
				}
			}
		}
		misDiffMerge(codeChunkList);
		return codeChunkList;
	}

	private List<DiffEntry> generateDiff(RevCommit prevCommit, RevCommit commit) throws IOException, GitAPIException {
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(commit);
		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(prevCommit);
		List<DiffEntry> diffs = git.diff().setNewTree(newTreeIterator).setOldTree(oldTreeIterator).call();
		return diffs;
	}

	private void misDiffMerge(List<CodeSnapShot> codeChunkList) {
		for (CodeSnapShot codeSnapShot : codeChunkList) {
			HashMap<DiffEntry, String> diffs = codeSnapShot.getDiffContents();
			HashMap<DiffEntry, String> copyDiffs = new HashMap<DiffEntry, String>(diffs);
			for ( Entry<DiffEntry, String> diff : diffs.entrySet()) {
				String value = diff.getValue();
			}
		}
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

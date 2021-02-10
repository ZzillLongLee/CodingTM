package commit_task_visualization.code_change_extraction.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import commit_task_visualization.code_change_extraction.util.Constants;


public class CommitDiffGenerator {

	private Repository repo;
	private Git git;
	private RevWalk walk;

	public CommitDiffGenerator(GitRepositoryGenerator gitRepositoryGenerator) {
		git = gitRepositoryGenerator.getGit();
		repo = gitRepositoryGenerator.getRepo();
		walk = gitRepositoryGenerator.getWalk();
	}
	// current commit is the previous commit and prevCommit is the next version commit.
	public List<DiffEntry> generateDiff(RevCommit prevCommit, RevCommit curCommit) throws IOException, GitAPIException {
		List<DiffEntry> diffs = null;
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(curCommit);
		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(prevCommit);
		diffs = git.diff().setNewTree(newTreeIterator).setOldTree(oldTreeIterator).call();
		return diffs;
	}

	private AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId) throws IOException {
		RevCommit commit = walk.parseCommit(commitId);
		ObjectId treeId = commit.getTree().getId();
		ObjectReader reader = repo.newObjectReader();
		return new CanonicalTreeParser(null, reader, treeId);
	}

	public HashMap<DiffEntry, String> generateDiffContents(List<DiffEntry> diffs) {
		HashMap<DiffEntry, String> diffContents = new HashMap<DiffEntry, String>();
		try {
			for (DiffEntry diffEntry : diffs) {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				DiffFormatter df = new DiffFormatter(output);
				df.setRepository(repo);
				df.format(diffEntry);
				// It has some problem only using new path.
				if (diffEntry.getNewPath().contains(Constants.JAVA_FILE_EXTENSION)
						|| diffEntry.getOldPath().contains(Constants.JAVA_FILE_EXTENSION))
					diffContents.put(diffEntry, output.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return diffContents;
	}

}

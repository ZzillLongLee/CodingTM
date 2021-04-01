package commit_task_visualization.code_change_extraction.git;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class CommitExtractor {

	private Repository repo;
	private Git git;
	private Ref masterBranch;

	public CommitExtractor(GitRepositoryGenerator gitRepositoryGenerator) {
		git = gitRepositoryGenerator.getGit();
		repo = gitRepositoryGenerator.getRepo();
	}

	// This method extract commits for master branch
	public Iterable<RevCommit> extractCommits() throws NoHeadException, GitAPIException, IOException {
		Iterable<RevCommit> commits = null;
		masterBranch = getMasterBranch(git);
		if (masterBranch != null) {
			String masterBranchName = masterBranch.getName();
			System.out.println("Commits of branch: " + masterBranchName);
			System.out.println("-------------------------------------");

			commits = git.log().add(repo.resolve(masterBranchName)).call();
		}
		return commits;
	}

	public RevCommit getCommit(String commitID) {
		RevCommit commit = null;
		try {
			RevWalk walk = new RevWalk(repo);
			ObjectId id = null;
			id = repo.resolve(commitID);
			commit = walk.parseCommit(id);
			System.out.println("Found Commit again: " + commit);
			walk.dispose();
		} catch (RevisionSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return commit;
	}
	// the prevCommit is the right next version of commit in commit list.
	public RevCommit getPrevCommit(RevCommit curCommit, Iterable<RevCommit> commits) {
		RevCommit targetCommit = null;
		RevCommit prevCommit = null;
		for (RevCommit revCommit : commits) {
			if (revCommit.equals(curCommit))
				targetCommit = prevCommit;
			else
				prevCommit = revCommit;
		}
		return targetCommit;
	}

	private Ref getMasterBranch(Git git) {
		List<Ref> branches;
		Ref masterBrunch = null;
		try {
			branches = git.branchList().call();
			for (Ref branch : branches) {
				String branchName = branch.getName();
				if (branchName.contains(Constants.MASTER)) {
					masterBrunch = branch;
					break;
				}
				if (branches.size() == 1) {
					masterBrunch = branch;
					break;
				}
			}
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return masterBrunch;
	}

}

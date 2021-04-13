package commit_task_visualization.code_change_extraction.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

public class GitRepositoryGenerator {

	private Repository repo;
	private Git git;
	private RevWalk walk;

	public GitRepositoryGenerator(String URL, String localProjectPath) {
		File file = new File(localProjectPath);
		if (file.exists())
			getLocalRepository(file);
		else
			initGitRepo(URL, localProjectPath);
	}

	public Repository getRepo() {
		return repo;
	}

	public Git getGit() {
		return git;
	}

	public RevWalk getWalk() {
		return walk;
	}

	// This method is used about the case of clone project existed in local
	// directory.
	public void getLocalRepository(File file) {
		try {
			git = Git.open(file);
			repo = git.getRepository();
			walk = new RevWalk(repo);
			if (repo.isBare() == false)
				System.out.println(" ----------Successfully Accessed Local Repository");
			else
				System.out.println(" ----------Failed Accssing Local Repository");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean initGitRepo(String URL, String localDir) {
		boolean isInit = false;
		if (git == null) {
			try {
				git = Git.cloneRepository().setURI(URL).setDirectory(new File(localDir)).call();
				repo = git.getRepository();
				// The RevWalk walks a commit graph and produces the matching commits in orde
				walk = new RevWalk(repo);
				isInit = true;
				return isInit;
			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isInit;
	}

}

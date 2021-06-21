package commit_task_visualization.code_change_extraction.git;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;


public class CommitFilterTest {

	@Test
	public void filterCommitTest() throws NoHeadException, GitAPIException, IOException {
		GitRepositoryGenerator gitRepositoryGen = new GitRepositoryGenerator("https://github.com/stkim123/kr.ac.jbnu.ssel.misrac.git", "D:\\test\\kr.ac.jbnu.ssel.misrac.git");
		CommitExtractor commitChangesExtractor = new CommitExtractor(gitRepositoryGen);
		Iterable<RevCommit> commits = commitChangesExtractor.extractCommits();
		CommitFilter commitFilter = new CommitFilter(gitRepositoryGen);
		List<CodeSnapShot> codeChunkList = commitFilter.filterCommits(commits, "c7167f", 0);
		for (CodeSnapShot codeChunk : codeChunkList) {
			System.out.println(codeChunk.getCommitMsg());
			System.out.println("Current Commit: "+ codeChunk.getCommit().getId().toString());
			System.out.println("Previous Commit: "+ codeChunk.getPrevCommit().toString());
			HashMap<DiffEntry, String> diffContents = codeChunk.getDiffContents();
			Set<Entry<DiffEntry, String>> diffEntryContents = diffContents.entrySet();
			System.out.println("Diff Size is: " + diffContents.size());
			for (Entry<DiffEntry, String> diffEntryContent : diffEntryContents) {
				System.out.println(diffEntryContent.getKey().getOldPath());
				System.out.println(diffEntryContent.getKey().getNewPath());
//				System.out.println(diffEntryContent.getValue());
				System.out.println("-------------------------");
			}
		}
	}

}

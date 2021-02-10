package commit_task_visualization.code_change_extraction.git;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.junit.Test;


public class CommitChangesExtractorTest {

	@Test
	public void accessLocalRepositoryTest() throws NoHeadException, GitAPIException, IOException {
		CommitExtractor commitChangesExtractor = new CommitExtractor(new GitRepositoryGenerator("URL", "Local_Dir"));
		commitChangesExtractor.extractCommits();
	}
}

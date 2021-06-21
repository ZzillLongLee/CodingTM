package commit_task_visualization.code_change_extraction.git;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.util.Configuration;


public class ChangedFileExtractorTest {
	@Test
	public void extractFileTest() throws NoHeadException, GitAPIException, IOException {
		
		GitRepositoryGenerator gitRepositoryGen = new GitRepositoryGenerator("URL",
				"Local_Dir");
		
		CommitExtractor commitChangesExtractor = new CommitExtractor(gitRepositoryGen);
		Iterable<RevCommit> commits = commitChangesExtractor.extractCommits();
		CommitFilter commitFilter = new CommitFilter(gitRepositoryGen);
		
		ChangedFileContentExtractor cfx = new ChangedFileContentExtractor(gitRepositoryGen);
		
		List<CodeSnapShot> codeChunkList = commitFilter.filterCommits(commits, "712afd131d377f1462d96fe8e72e2efa0f33a12b", 0);
		for (CodeSnapShot codeChunk : codeChunkList) {
			RevCommit commit = codeChunk.getPrevCommit();
			HashMap<DiffEntry, String> diffContents = codeChunk.getDiffContents();
			Set<Entry<DiffEntry, String>> diffEntryContents = diffContents.entrySet();
			int i = 0;
			for (Entry<DiffEntry, String> diffEntryContent : diffEntryContents) {
				FileWriter fw = new FileWriter("Outcome\\SourceCodeFile"+i+".txt");
				DiffEntry diffEntry = diffEntryContent.getKey();
				String sourceCode = cfx.extractChangedFileSourceCode(commit, diffEntry.getOldPath());
				if(sourceCode!=null) {
				fw.write(sourceCode);
				fw.flush();
				fw.close();
				}
				i++;
			}
		}
	}
}

package commit_task_visualization.code_change_extraction.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import commit_task_visualization.code_change_extraction.ast.ASTSupportSingleton;
import commit_task_visualization.code_change_extraction.ast.SourceCodeVisitor;
import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ChangedFilePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.model.MethodPart;

public class ChangedFileContentExtractor {

	private Repository repo;
	private Git git;
	private RevWalk walk;

	public ChangedFileContentExtractor(GitRepositoryGenerator gitRepositoryGenerator) {
		git = gitRepositoryGenerator.getGit();
		repo = gitRepositoryGenerator.getRepo();
		walk = gitRepositoryGenerator.getWalk();
	}

//	This method retrieve the certain file at the commit and generate file to String type for AST Parser.
	public String extractChangedFileSourceCode(RevCommit commit, String filePath)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		// and using commit's tree find the path
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(filePath));
		if (!treeWalk.next()) {
			return null;
		}
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		String sourceCode = readStream(loader.openStream());

		return sourceCode;
	}

	private String readStream(InputStream iStream) throws IOException {
		// build a Stream Reader, it can read char by char
		InputStreamReader iStreamReader = new InputStreamReader(iStream);
		// build a buffered Reader, so that i can read whole line at once
		BufferedReader bReader = new BufferedReader(iStreamReader);
		String line = null;
		StringBuilder builder = new StringBuilder();
		while ((line = bReader.readLine()) != null) { // Read till end
			builder.append(line + '\n');
		}
		bReader.close(); // close all opened stuff
		iStreamReader.close();
		iStream.close();
		return builder.toString();
	}

	public void addChangedFiles(CodeSnapShot codeChunk, ASTSupportSingleton astSupport)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		RevCommit curCommit = codeChunk.getCommit();
		RevCommit prevCommit = codeChunk.getPrevCommit();
		HashMap<DiffEntry, String> diffContents = codeChunk.getDiffContents();
		Set<Entry<DiffEntry, String>> diffEntryContents = diffContents.entrySet();

		List<ChangedFilePart> changedFileList = new ArrayList<ChangedFilePart>();

		for (Entry<DiffEntry, String> diffEntryContent : diffEntryContents) {
			DiffEntry diff = diffEntryContent.getKey();
			ChangeType changeType = diff.getChangeType();
			// the oldPath is previous file and newPath is current file.
			String prevChangedFilePath = diff.getOldPath();
			String prevVersionSourceCode = null;
			if (!prevChangedFilePath.equals(diff.DEV_NULL))
				prevVersionSourceCode = extractChangedFileSourceCode(curCommit, prevChangedFilePath);
			String curChangedFilePath = diff.getNewPath();
			String curVersionSourceCode = null;
			if (!curChangedFilePath.equals(diff.DEV_NULL))
				curVersionSourceCode = extractChangedFileSourceCode(prevCommit, curChangedFilePath);

			List<ClassPart> curClsParts = new ArrayList<ClassPart>();
			List<AttributePart> curVersionFieldObjects = new ArrayList<AttributePart>();
			List<MethodPart> curVersionMethodObjects = new ArrayList<MethodPart>();
			List<ClassPart> prevClsParts = new ArrayList<ClassPart>();
			List<AttributePart> prevVersionFieldObjects = new ArrayList<AttributePart>();
			List<MethodPart> prevVersionMethodObjects = new ArrayList<MethodPart>();

			if (prevVersionSourceCode != null) {
				astSupport.parse(prevVersionSourceCode, new SourceCodeVisitor(curCommit.getId().toString(),
						prevClsParts, prevVersionFieldObjects, prevVersionMethodObjects, prevChangedFilePath));
			}
			if (curVersionSourceCode != null) {
				astSupport.parse(curVersionSourceCode, new SourceCodeVisitor(prevCommit.getId().toString(), curClsParts,
						curVersionFieldObjects, curVersionMethodObjects, curChangedFilePath));
			}
			ChangedFilePart changedFilePart = new ChangedFilePart.ChnagedFilePartBuilder(changeType, curChangedFilePath,
					prevChangedFilePath).setFieldObjects(curVersionFieldObjects, prevVersionFieldObjects)
							.setMethodObjects(curVersionMethodObjects, prevVersionMethodObjects)
							.setSourceCode(curVersionSourceCode, prevVersionSourceCode)
							.setClassPart(curClsParts, prevClsParts).build();
			changedFileList.add(changedFilePart);
		}
		codeChunk.setChangedFileList(changedFileList);
	}
}

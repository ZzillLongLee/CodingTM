package commit_task_visualization.code_change_extraction.mapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import commit_task_visualization.code_change_extraction.ast.ASTSupportSingleton;
import commit_task_visualization.code_change_extraction.git.ChangedFileContentExtractor;
import commit_task_visualization.code_change_extraction.git.CommitExtractor;
import commit_task_visualization.code_change_extraction.git.CommitFilter;
import commit_task_visualization.code_change_extraction.git.GitRepositoryGenerator;
import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.model.ChangedFilePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.model.MethodPart;
import commit_task_visualization.code_change_extraction.model.StatementPart;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import commit_task_visualization.code_change_extraction.util.Configuration;
import commit_task_visualization.code_change_extraction.util.Constants;

public class ChangeStateIdentifierTest {

	private static GitRepositoryGenerator gitRepositoryGen;
	private static ASTSupportSingleton astSupport;
	private static CommitExtractor commitChangesExtractor;
	private static CommitFilter commitFilter;
	private static ChangedFileContentExtractor cfx;

	public void CodeChangeExtractionInit() {
		gitRepositoryGen = new GitRepositoryGenerator("URL", "Local_Dir");

		commitChangesExtractor = new CommitExtractor(gitRepositoryGen);

		commitFilter = new CommitFilter(gitRepositoryGen);

		cfx = new ChangedFileContentExtractor(gitRepositoryGen);

		astSupport = ASTSupportSingleton.getInstance();
	}

	@Test
	public void codeStateIdentifierTest() throws NoHeadException, GitAPIException, IOException {
		CodeChangeExtractionInit();

		Iterable<RevCommit> commits = commitChangesExtractor.extractCommits();

		List<CodeSnapShot> codeChunkList = commitFilter.filterCommits(commits,
				"712afd131d377f1462d96fe8e72e2efa0f33a12b", 0);

		for (CodeSnapShot codeChunk : codeChunkList) {
			cfx.addChangedFiles(codeChunk, astSupport);

			ChangeStateIdentifier csi = new ChangeStateIdentifier();
			List<ChangedFilePart> changedFileLilst = codeChunk.getChangedFileList();
			for (ChangedFilePart changedFile : changedFileLilst) {
				csi.identifyInnerChangedFileContentsStates(changedFile);
			}
		}

		for (CodeSnapShot codeChunk : codeChunkList) {
			String commitID = codeChunk.getPrevCommit().getId().toString();
			int idx = 0;
			List<ChangedFilePart> changedFileLilst = codeChunk.getChangedFileList();
			for (ChangedFilePart changedFile : changedFileLilst) {
				FileWriter myWriter = new FileWriter("Outcome\\" + commitID + "_changedFile_" + idx + ".txt");
				myWriter.write("FileName: " + changedFile.getChangedFilePath() + "\n");

				List<ClassPart> prevClassParts = changedFile.getClassParts(Constants.PREV_VERSION);

				for (ClassPart classPart : prevClassParts) {
					String className = classPart.getClassName();
					InsideClassChangeType classIdentifierState = classPart.getClassIdentifierState();
					if (classIdentifierState != null) {
						if (!classIdentifierState.equals(InsideClassChangeType.NONE)) {
							myWriter.write("-------------------Previous version's class-------------------" + "\n");
							myWriter.write("Status: " + classIdentifierState + "\n");
							myWriter.write("Code: " + classPart.getClassIdentifier() + "\n");
						}
					}
					List<AttributePart> prevFieldObjects = changedFile.getFieldObjects(Constants.PREV_VERSION);
					if (prevFieldObjects != null) {
						for (AttributePart prevFieldObject : prevFieldObjects) {
							String attributeClassName = prevFieldObject.getClassName();
							InsideClassChangeType prevFieldChangedTpye = prevFieldObject.getChangedType();
							if (className.equals(attributeClassName)) {
								if (prevFieldChangedTpye != null) {
									if (!prevFieldChangedTpye.equals(InsideClassChangeType.NONE)) {
										myWriter.write("-------------------Previous version's field-------------------"
												+ "\n");
										myWriter.write("Status: " + prevFieldObject.getChangedType() + "\n");
										myWriter.write("Code: " + prevFieldObject.getAttributeAsString() + "\n");
									}
								}
							}
						}
					}

					List<MethodPart> prevMethodObjects = changedFile.getMethodObjects(Constants.PREV_VERSION);
					if (prevMethodObjects != null) {
						for (MethodPart prevMethodObject : prevMethodObjects) {
							String methodClassName = prevMethodObject.getClassName();
							if (className.equals(methodClassName)) {
								InsideClassChangeType prevMethodChangedTpye = prevMethodObject.getChangedType();
								if (prevMethodChangedTpye != null) {
									if (!prevMethodChangedTpye.equals(InsideClassChangeType.NONE)) {
										myWriter.write("-------------------Previous version's method-------------------"
												+ "\n");
										myWriter.write("Status: " + prevMethodObject.getChangedType() + "\n");
										myWriter.write(
												"MethodSignature: " + prevMethodObject.getMethodSignature() + "\n");

										List<StatementPart> prevMethodStmts = prevMethodObject.getStatements();
										for (StatementPart stmt : prevMethodStmts) {
											InsideClassChangeType stmtChangedType = stmt.getChangedType();
											if (stmtChangedType != null) {
												if (!stmtChangedType.equals(InsideClassChangeType.NONE)) {
													myWriter.write("Status: " + stmtChangedType + "\n");
													myWriter.write("Code: " + stmt.stmtAsString() + "\n");
												}
											}
										}
									}
								}
							}
						}
					}

				}

				List<ClassPart> curClassParts = changedFile.getClassParts(Constants.CUR_VERSION);
				for (ClassPart classPart : curClassParts) {
					String className = classPart.getClassName().toString();
					InsideClassChangeType classIdentifierState = classPart.getClassIdentifierState();
					if (classIdentifierState != null) {
						if (!classIdentifierState.equals(InsideClassChangeType.NONE)) {
							myWriter.write("-------------------Present version's class-------------------" + "\n");
							myWriter.write("Status: " + classIdentifierState + "\n");
							myWriter.write("Code: " + classPart.getClassIdentifier() + "\n");
						}
					}
					List<AttributePart> curFieldObjects = changedFile.getFieldObjects(Constants.CUR_VERSION);
					if (curFieldObjects != null) {
						for (AttributePart curFieldObject : curFieldObjects) {
							String attributeClassName = curFieldObject.getClassName().toString();
							if (className.equals(attributeClassName)) {
								InsideClassChangeType curFieldChangedTpye = curFieldObject.getChangedType();
								if (curFieldChangedTpye != null) {
									if (!curFieldChangedTpye.equals(InsideClassChangeType.NONE)) {
										myWriter.write(
												"-------------------Present version's field-------------------" + "\n");
										myWriter.write("Status: " + curFieldObject.getChangedType() + "\n");
										myWriter.write("Code: " + curFieldObject.getAttributeAsString() + "\n");
									}
								}
							}
						}
					}
					List<MethodPart> curMethodObjects = changedFile.getMethodObjects(Constants.CUR_VERSION);
					if (curMethodObjects != null) {
						for (MethodPart curMethodObject : curMethodObjects) {
							String methodClassName = curMethodObject.getClassName().toString();
							if (className.equals(methodClassName)) {
								InsideClassChangeType curMethodChangedTpye = curMethodObject.getChangedType();
								if (curMethodChangedTpye != null) {
									if (!curMethodChangedTpye.equals(InsideClassChangeType.NONE)) {
										myWriter.write("-------------------Present version's method-------------------"
												+ "\n");
										myWriter.write("Status: " + curMethodObject.getChangedType() + "\n");
										myWriter.write(
												"MethodSignature: " + curMethodObject.getMethodSignature() + "\n");

										List<StatementPart> curMethodStmts = curMethodObject.getStatements();
										for (StatementPart stmt : curMethodStmts) {
											InsideClassChangeType stmtChangedType = stmt.getChangedType();
											if (stmtChangedType != null) {
												if (!stmtChangedType.equals(InsideClassChangeType.NONE)) {
													myWriter.write("Status: " + stmtChangedType + "\n");
													myWriter.write("Code: " + stmt.stmtAsString() + "\n");
												}
											}
										}
									}
								}
							}
						}
					}
				}
				myWriter.write("----------------------------------------------------------------" + "\n");
				myWriter.close();
				idx++;
			}
		}
	}

}

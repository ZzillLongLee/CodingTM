package commit_task_visualization.code_change_extraction;

import java.awt.Frame;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import commit_task_visualization.code_change_extraction.ast.ASTSupportSingleton;
import commit_task_visualization.code_change_extraction.development_flow.DevelopmentFlowGenerator;
import commit_task_visualization.code_change_extraction.development_flow.DuplicatedFlowFilter;
import commit_task_visualization.code_change_extraction.git.ChangedFileContentExtractor;
import commit_task_visualization.code_change_extraction.git.CommitExtractor;
import commit_task_visualization.code_change_extraction.git.CommitFilter;
import commit_task_visualization.code_change_extraction.git.GitRepositoryGenerator;
import commit_task_visualization.code_change_extraction.mapper.ChangeStateIdentifier;
import commit_task_visualization.code_change_extraction.merge_process.MergeProcessor;
import commit_task_visualization.code_change_extraction.merge_process.TaskTreeGenerator;
import commit_task_visualization.code_change_extraction.model.ChangedFilePart;
import commit_task_visualization.code_change_extraction.model.ClassPart;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.code_change_extraction.model.SubCodeChunk;
import commit_task_visualization.code_change_extraction.model.task_elements.Task;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskClass;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementRepo;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementUtil;
import commit_task_visualization.code_change_extraction.util.CodeChunkPreprocessor;
import commit_task_visualization.code_change_extraction.util.Constants;
import commit_task_visualization.code_change_extraction.util.TaskElementGenerater;
import commit_task_visualization.task_visualization.TaskVisualizer;

public class CodeChangeExtractionControl {

	private static CodeChangeExtractionControl ISTNACE = null;
	private static ASTSupportSingleton astSupport;
	private static CommitExtractor commitChangesExtractor;
	private static CommitFilter commitFilter;
	private ChangedFileContentExtractor cfx;
	private HashMap<String, List<String>> idSet;
	private TaskVisualizer tv;
	private static final int deleted = 0;
	private static final int added = 1;
	private static final int modified = 2;

	public void init(GitRepositoryGenerator gitRepositoryGen) {
		commitChangesExtractor = new CommitExtractor(gitRepositoryGen);
		commitFilter = new CommitFilter(gitRepositoryGen);
		cfx = new ChangedFileContentExtractor(gitRepositoryGen);
		astSupport = ASTSupportSingleton.getInstance();
	}

	public List<CodeSnapShot> getCommitList(String keyword) {
		List<CodeSnapShot> codecodeSnapShotList = null;
		try {
			Iterable<RevCommit> commits = commitChangesExtractor.extractCommits();
			codecodeSnapShotList = commitFilter.filterCommits(commits, keyword);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return codecodeSnapShotList;
	}

	public void visualizeTask(CodeSnapShot codeSnapShot, Composite parent) {
		try {
			cfx.addChangedFiles(codeSnapShot, astSupport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		identifyChangeType(codeSnapShot);

		DuplicatedFlowFilter dff = new DuplicatedFlowFilter();
		idSet = new HashMap<String, List<String>>();

		System.out.println("Current Version Code Chunk Generating.....");
		SubCodeChunk curVersionSubCodeChunk = CodeChunkPreprocessor.collectingElementSet(codeSnapShot,
				Constants.CUR_VERSION);
		CodeChunkPreprocessor.generateInnerStmtData(curVersionSubCodeChunk);
		new DevelopmentFlowGenerator(curVersionSubCodeChunk);
		curVersionSubCodeChunk = dff.filterDuplicatedFlow(curVersionSubCodeChunk);
		CodeChunkPreprocessor.reAssignCodeElement(curVersionSubCodeChunk, idSet);
		curVersionSubCodeChunk.assignConnectedElementsToClassPart();

		List<TaskClass> curTaskClasses = generateTaskClasses(curVersionSubCodeChunk, added);
		Task curTask = new Task(codeSnapShot.getCommit().getId().toString(), curTaskClasses);
		TaskElementUtil.insertTEtoRepo(curTaskClasses);
		String curCommitID = curTask.getCommitID();

		System.out.println("Previous Version Code Chunk Generating.....");
		idSet.clear();
		SubCodeChunk prevVersionSubCodeChunk = CodeChunkPreprocessor.collectingElementSet(codeSnapShot,
				Constants.PREV_VERSION);
		CodeChunkPreprocessor.generateInnerStmtData(prevVersionSubCodeChunk);
		new DevelopmentFlowGenerator(prevVersionSubCodeChunk);
		prevVersionSubCodeChunk = dff.filterDuplicatedFlow(prevVersionSubCodeChunk);
		CodeChunkPreprocessor.reAssignCodeElement(prevVersionSubCodeChunk, idSet);
		prevVersionSubCodeChunk.assignConnectedElementsToClassPart();

		List<TaskClass> prevTaskClasses = generateTaskClasses(prevVersionSubCodeChunk, deleted);
		Task prevTask = new Task(codeSnapShot.getCommit().getId().toString(), prevTaskClasses);
		TaskElementUtil.insertTEtoRepo(prevTaskClasses);
		String prevCommitID = prevTask.getCommitID();

		HashMap<String, TaskElement> taskElementHashmap = TaskElementRepo.getInstance().getTaskElementHashMap();
		MergeProcessor mp = new MergeProcessor(prevCommitID, curCommitID);
		try {
			mp.mergeTwoVersion(taskElementHashmap);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.updateCausalRel(taskElementHashmap);
		TaskTreeGenerator ttg = new TaskTreeGenerator();
		List<List<TaskElement>> taskList = ttg.buildTaskTree(curTask, prevTask);

		visualize(parent, curCommitID, taskElementHashmap, taskList);

	}

	private void identifyChangeType(CodeSnapShot codeSnapShot) {
		ChangeStateIdentifier csi = new ChangeStateIdentifier();
		List<ChangedFilePart> changedFileLilst = codeSnapShot.getChangedFileList();
		for (ChangedFilePart changedFile : changedFileLilst) {
			csi.identifyInnerChangedFileContentsStates(changedFile);
		}
	}

	private void visualize(Composite parent, String curCommitID, HashMap<String, TaskElement> taskElementHashmap,
			List<List<TaskElement>> taskList) {
		tv = new TaskVisualizer(taskElementHashmap, taskList);
		JPanel panel = tv.showTask();
		
		Shell dialog = new Shell(parent.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setSize(500, 500);
		dialog.setText("Task View |" + curCommitID);
		System.setProperty("sun.awt.noerasebackground", "true");
		Composite composite = new Composite(dialog, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite.setBounds(5, 5, 500, 500);
		Display display = dialog.getDisplay();
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);
		frame.add(panel);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				dialog.open();
			}
		});
	}

	private static List<TaskClass> generateTaskClasses(SubCodeChunk SubCodeChunk, int status) {
		TaskElementGenerater cceg = new TaskElementGenerater();
		List<ClassPart> classSet = SubCodeChunk.getClassPartSet();
		List<TaskClass> taskCCCset = cceg.convertTaskDataStructure(classSet, status);
		List<ClassPart> testClassSet = SubCodeChunk.getTestClassPartSet();
		List<TaskClass> taskCCCTset = cceg.convertTaskDataStructure(testClassSet, status);
		System.out.println();
		List<TaskClass> totalCCC = Stream.concat(taskCCCset.stream(), taskCCCTset.stream())
				.collect(Collectors.toList());
		return totalCCC;
	}

	public static CodeChangeExtractionControl getInstance() {
		if (ISTNACE == null) {
			ISTNACE = new CodeChangeExtractionControl();
		}
		return ISTNACE;
	}

	public TaskVisualizer getTv() {
		return tv;
	}

}

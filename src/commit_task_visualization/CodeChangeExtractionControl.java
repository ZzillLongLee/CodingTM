package commit_task_visualization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
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
import commit_task_visualization.multiple_task_visualization.MultipleCommitViewDialog;
import commit_task_visualization.single_task_visualization.TaskVisualizer;
import commit_task_visualization.single_task_visualization.model.CommitData;

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

	private CommitData generateTask(CodeSnapShot codeSnapShot) {
		try {
			cfx.addChangedFiles(codeSnapShot, astSupport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		identifyChangeType(codeSnapShot);

		DuplicatedFlowFilter dff = new DuplicatedFlowFilter();
		idSet = new HashMap<String, List<String>>();

		TaskElementRepo taskElementRepo = new TaskElementRepo();

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
		TaskElementUtil.insertTEtoRepo(curTaskClasses, taskElementRepo);
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
		Task prevTask = new Task(codeSnapShot.getPrevCommit().getId().toString(), prevTaskClasses);
		TaskElementUtil.insertTEtoRepo(prevTaskClasses, taskElementRepo);
		String prevCommitID = prevTask.getCommitID();

		HashMap<String, TaskElement> taskElementHashmap = taskElementRepo.getTaskElementHashMap();
		MergeProcessor mp = new MergeProcessor(prevCommitID, curCommitID);
		try {
			mp.mergeTwoVersion(taskElementHashmap);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.updateCausalRel(taskElementHashmap, taskElementRepo);
		TaskTreeGenerator ttg = new TaskTreeGenerator(taskElementRepo);
		List<List<TaskElement>> taskList = ttg.buildTaskTree(curTask, prevTask);
		return new CommitData(curCommitID, prevCommitID, taskList, taskElementHashmap);
	}

	public void visualizeTask(CodeSnapShot codeSnapShot, Composite parent) {
		String curCommitID = codeSnapShot.getCommit().getId().toString();
		String prevCommitID = codeSnapShot.getPrevCommit().getId().toString();
		CommitData cd = generateTask(codeSnapShot);
		List<List<TaskElement>> taskList = cd.getTaskList();
		HashMap<String, TaskElement> taskElementHashmap = cd.getTaskElementHashmap();
		visualizeSingleCommit(parent, curCommitID, prevCommitID, taskElementHashmap, taskList);
	}

	public void visulizeMultipleTask(Composite parent, Object[] checkedElements) {
		StringBuilder sb = new StringBuilder();
		List<CommitData> commitDataList = new ArrayList<CommitData>();
		boolean hasZeroDiff = false;
		for (int i = checkedElements.length-1; i >= 0; i--) {
			CodeSnapShot codeSnapShot = (CodeSnapShot)checkedElements[i];
			int diffSize = codeSnapShot.getDiffContents().size();
			if (diffSize != 0) {
				CommitData cd = generateTask(codeSnapShot);
				commitDataList.add(cd);
			} else {
				hasZeroDiff = true;
				if (i != checkedElements.length - 1)
					sb.append(codeSnapShot.getCommit().getId().toString() + ", ");
				else
					sb.append(codeSnapShot.getCommit().getId().toString());
			}
		}
		if (hasZeroDiff != true) {
			MultipleCommitViewDialog ntd = new MultipleCommitViewDialog(parent.getShell(), commitDataList);
			ntd.open();
			
		} else {
			MessageBox msgDialog = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
			msgDialog.setMessage("These Following Commits: " + sb.toString() + "doesn't have java files.");
			msgDialog.open();
		}
	}

	private void identifyChangeType(CodeSnapShot codeSnapShot) {
		ChangeStateIdentifier csi = new ChangeStateIdentifier();
		List<ChangedFilePart> changedFileLilst = codeSnapShot.getChangedFileList();
		for (ChangedFilePart changedFile : changedFileLilst) {
			csi.identifyInnerChangedFileContentsStates(changedFile);
		}
	}

	public void visualizeSingleCommit(Composite parent, String curCommitID, String prevCommitID,
			HashMap<String, TaskElement> taskElementHashmap, List<List<TaskElement>> taskList) {
		tv = new TaskVisualizer(taskElementHashmap, taskList, curCommitID, prevCommitID);
		JPanel panel = tv.showTask();
		Shell shell = new Shell(parent.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setBounds(5, 5, 1000, 1100);
		shell.setText("Task View |" + curCommitID);
		System.setProperty("sun.awt.noerasebackground", "true");
		Composite composite = new Composite(shell, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);
		composite.setBounds(5, 5, 1000, 1100);
		frame.add(panel);
		Display display = shell.getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				shell.open();
			}
		});
	}

	private static List<TaskClass> generateTaskClasses(SubCodeChunk SubCodeChunk, int status) {
		TaskElementGenerater cceg = new TaskElementGenerater();
		List<ClassPart> classSet = SubCodeChunk.getClassPartSet();
		List<TaskClass> taskCCCset = cceg.convertTaskDataStructure(classSet, status);
		List<ClassPart> testClassSet = SubCodeChunk.getTestClassPartSet();
		List<TaskClass> taskCCCTset = cceg.convertTaskDataStructure(testClassSet, status);
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

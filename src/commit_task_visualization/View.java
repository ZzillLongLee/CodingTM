package commit_task_visualization;

import java.io.File;
import java.util.*;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.ViewPart;

import commit_task_visualization.code_change_extraction.git.GitRepositoryGenerator;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.table_view_columns.CommitIDColumn;
import commit_task_visualization.table_view_columns.CommitMsgColumn;
import commit_task_visualization.table_view_columns.CommitTimeColumn;
import commit_task_visualization.table_view_columns.CommitterColumn;

public class View extends ViewPart {
	public static final String ID = "CommitTaskVisualization.view";

	private static GitRepositoryGenerator gitRepositoryGen;
	private static CodeChangeExtractionControl ccec;

	IWorkbench workbench;

	private CheckboxTableViewer commitTableViewer;

	private Text searchText;

	private Text projectPathText;

	private String localPath = "";

	private Text localPathText;

	private List<CodeSnapShot> commitList;

	private boolean isClicked = false;
	
	private boolean isRegen = false;

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));
		parent.setSize(700, 265);

		createCommitSearch(parent);

		Table table = new Table(parent,
				SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.RESIZE | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Display device = Display.getCurrent();
		table.setHeaderBackground(new Color(device, 220, 220, 220));
		table.setBounds(10, 10, 400, 140);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(680, 250));

		commitTableViewer = new CheckboxTableViewer(table);

		new CommitTimeColumn().addColumnTo(commitTableViewer);
		new CommitterColumn().addColumnTo(commitTableViewer);
		new CommitMsgColumn().addColumnTo(commitTableViewer);
		new CommitIDColumn().addColumnTo(commitTableViewer);

		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					TableItem ti = (TableItem) event.item;
					if (table.indexOf((TableItem) event.item) == table.getSelectionIndex()) {
						ti.setChecked(!ti.getChecked());
					}
				}
			}
		});

		commitTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		getSite().setSelectionProvider(commitTableViewer);
		// Provide the input to the ContentProvider
		if (commitList != null && isClicked == true)
			commitTableViewer.setInput(commitList);
		commitTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof CodeSnapShot) {
					CodeSnapShot codeSnapShot = (CodeSnapShot) obj;
					HashMap<DiffEntry, String> diffs = codeSnapShot.getDiffContents();
					if (diffs.size() > 0) {
						ccec.visualizeTask(codeSnapShot, parent);
					} else {
						MessageBox msgDialog = new MessageBox(parent.getShell(),
								SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						msgDialog.setMessage("The changed java file doesn't exist.");
						msgDialog.open();
					}
				}
			}
		});
		Button mutipleCommitViewButton = new Button(parent, SWT.PUSH | SWT.TOGGLE);
		mutipleCommitViewButton.setText("Show Development List View");
		mutipleCommitViewButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Object[] checkedElements = commitTableViewer.getCheckedElements();
				ccec.visulizeMultipleTask(parent, checkedElements);
				commitTableViewer.setAllChecked(false);
			}
		});

	}

	private void createCommitSearch(Composite parent) {

		drawGitClonePart(parent);
		Composite comp = new Composite(parent, 0);
		comp.setLayout(new RowLayout());
		drawListSearchBar(comp);

	}

	private void drawGitClonePart(Composite parent) {
		String defaultPathString = "Please put the Git URL here.";

		Composite gitCloneComp = new Composite(parent, 0);
		gitCloneComp.setLayout(new RowLayout());

		Button FileBrowseBt = new Button(gitCloneComp, SWT.PUSH | SWT.TOGGLE);
		FileBrowseBt.setText("Set Local Directory");
		FileBrowseBt.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				DirectoryDialog dirDialog = new DirectoryDialog(new Shell(), SWT.MULTI);
				dirDialog.setFilterPath(Constants.intialFilePath);
				String firstFile = dirDialog.open();
				if (firstFile != null) {
					File destination = Path.fromOSString(firstFile).makeAbsolute().toFile();
					destination.mkdirs();
					localPath = destination.getAbsolutePath();
					localPathText.setText(localPath);
				}
			}
		});
		localPathText = new Text(gitCloneComp, SWT.MULTI | SWT.BORDER);
		localPathText.setSize(80, 10);
		localPathText.setEditable(false);

		Label projectPathLabel = new Label(gitCloneComp, SWT.WRAP);
		FontDescriptor descriptor = FontDescriptor.createFrom(projectPathLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		projectPathLabel.setFont(descriptor.createFont(projectPathLabel.getDisplay()));

		projectPathLabel.setText("Project");
		projectPathText = new Text(gitCloneComp, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		projectPathText.setText(defaultPathString);
		projectPathText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {

			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if (projectPathText.getText().equals(defaultPathString))
					projectPathText.setText("");
			}
		});

		Button localGenBT = new Button(gitCloneComp, SWT.PUSH);
		localGenBT.setText("Clone");
		localGenBT.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Shell shell = gitCloneComp.getShell();
				if (projectPathText.getText().equals(defaultPathString) && !localPath.equals(Constants.EMPTY_STRING)) {
					MessageBox msgDialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
					msgDialog.setMessage("Please put the GIT URL.");
					msgDialog.open();
				} else if (!projectPathText.getText().equals(Constants.EMPTY_STRING)
						&& !localPath.equals(Constants.EMPTY_STRING)) {
					String url = projectPathText.getText();
					String[] splitedURL = url.split("/");
					String projectName = splitedURL[splitedURL.length - 1];
					if (gitRepositoryGen == null) {
						String localProjectPath = localPath + "\\" + projectName;
						gitRepositoryGen = new GitRepositoryGenerator(url, localProjectPath);
						MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						dialog.setMessage("Git clone is done");
						dialog.open();
					} else {
						MessageBox msgDialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						msgDialog.setMessage("The git repository object is already exist.\n Do you want to remove git repository object?");
						int i = msgDialog.open();
						if(i == SWT.OK) {
							gitRepositoryGen = null;
							isRegen = true;
						}
					}
				}
			}
		});
	}

	private void drawListSearchBar(Composite comp) {
		String defaultString = "Please put the keyword in commit msg or commit number here.";
		Label searchCommitLabel = new Label(comp, SWT.NONE);
		FontDescriptor descriptor = FontDescriptor.createFrom(searchCommitLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		searchCommitLabel.setFont(descriptor.createFont(searchCommitLabel.getDisplay()));

		searchCommitLabel.setText("Search Commits");
		searchText = new Text(comp, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		searchText.setText(defaultString);
		searchText.setSize(40, 10);
		searchText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {

			}

			@Override
			public void focusGained(FocusEvent arg0) {
				searchText.setText("");
			}
		});
		Button commitListGenBT = new Button(comp, SWT.PUSH);
		commitListGenBT.setText("Search");
		commitListGenBT.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if (commitList != null)
					commitList = null;
				isClicked = false;
				if (ccec == null && gitRepositoryGen != null) {
					ccec = CodeChangeExtractionControl.getInstance();
					ccec.init(gitRepositoryGen);
					if (!searchText.getText().equals(defaultString)) {
						commitList = ccec.getCommitList(searchText.getText());
						isClicked = true;
						commitTableViewer.setInput(commitList);
						commitTableViewer.getTable().redraw();
					}

				} else {
					Shell shell = comp.getShell();
					if (gitRepositoryGen == null) {
						MessageBox msgDialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						msgDialog.setMessage("The git repository isn't set.");
						msgDialog.open();
					}
					if (ccec == null) {
						MessageBox msgDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
						msgDialog.setMessage("The Git Control module is null.");
						msgDialog.open();
					}
					if (ccec != null && gitRepositoryGen != null && isRegen == false) {
						if (!searchText.getText().equals(defaultString)) {
							commitList = ccec.getCommitList(searchText.getText());
							isClicked = true;
							commitTableViewer.setInput(commitList);
							commitTableViewer.getTable().redraw();
						}
					}
					if (ccec != null && gitRepositoryGen != null && isRegen == true) {
						if (!searchText.getText().equals(defaultString)) {
							ccec.init(gitRepositoryGen);
							commitList = ccec.getCommitList(searchText.getText());
							isClicked = true;
							commitTableViewer.setInput(commitList);
							commitTableViewer.getTable().redraw();
							isRegen = false;
						}
					}
				}
			}
		});
	}

	@Override
	public void setFocus() {

	}

	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
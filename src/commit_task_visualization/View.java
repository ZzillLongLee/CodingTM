package commit_task_visualization;

import java.io.File;
import java.util.*;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.ViewPart;

import commit_task_visualization.code_change_extraction.CodeChangeExtractionControl;
import commit_task_visualization.code_change_extraction.git.GitRepositoryGenerator;
import commit_task_visualization.code_change_extraction.model.CodeSnapShot;
import commit_task_visualization.table_view.CommitIDColumn;
import commit_task_visualization.table_view.CommitMsgColumn;
import commit_task_visualization.table_view.CommitTimeColumn;

public class View extends ViewPart {
	public static final String ID = "CommitTaskVisualization.view";

	private static GitRepositoryGenerator gitRepositoryGen;
	private static CodeChangeExtractionControl ccec;

	IWorkbench workbench;

	private TableViewer commitTableViewer;

	private Text searchText;

	private Text projectPathText;

	private String localPath = "";

	private Text localPathText;

	private List<CodeSnapShot> commitList;

	private boolean isClicked = false;

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		createCommitSearch(parent);

		commitTableViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table table = commitTableViewer.getTable();
		Display device = Display.getCurrent();
		table.setHeaderBackground(new Color(device, 220, 220, 220));
		table.setBounds(10, 10, 350, 150);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(600, 250));

		new CommitMsgColumn().addColumnTo(commitTableViewer);
		new CommitIDColumn().addColumnTo(commitTableViewer);
		new CommitTimeColumn().addColumnTo(commitTableViewer);

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
					ccec.visualizeTask(codeSnapShot, parent);
				}
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
		localGenBT.setText("Generate");
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
						gitRepositoryGen = new GitRepositoryGenerator(url, localPath + "\\" + projectName);
						MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						dialog.setMessage("Git clone is done");
						dialog.open();
					} else {
						MessageBox msgDialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						msgDialog.setMessage("The git repository object is already exist.");
						msgDialog.open();
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
		commitListGenBT.setText("Export Commit List");
		commitListGenBT.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
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
					if (ccec != null && gitRepositoryGen != null) {
						if (!searchText.getText().equals(defaultString)) {
							commitList = ccec.getCommitList(searchText.getText());
							isClicked = true;
							commitTableViewer.setInput(commitList);
							commitTableViewer.getTable().redraw();
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
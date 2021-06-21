package commit_task_visualization.development_task_list_visualization;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

import commit_task_visualization.CodeChangeExtractionControl;
import commit_task_visualization.causal_relationship_visualization.dialog.TaskElementDialog;
import commit_task_visualization.causal_relationship_visualization.model.CommitData;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;

public class CellMenuConfiguration extends AbstractUiBindingConfiguration {

	private Menu bodyMenu;
	private int columnPosition;
	private int rowPosition;
	private List<CommitData> commitDataList;
	private ColumnGroupModel columnGroupModel;
	private final int taskElementColumn = 2;
	private TaskElementDialog teDialog;
	private final String diffViewMenu = "show Task Element Diff View";
	private final String crViewMenu = "show Causal Relationship View";

	public CellMenuConfiguration(final NatTable natTable, List<CommitData> commitDataList,
			ColumnGroupModel columnGroupModel) {
		this.commitDataList = commitDataList;
		this.bodyMenu = createBodyMenu(natTable).build();
		this.columnGroupModel = columnGroupModel;

		natTable.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				// TODO Auto-generated method stub

			}
			// code to dispose the menu }
		});
	}

	// construct the menu
	private PopupMenuBuilder createBodyMenu(NatTable natTable) {
		Menu menu = new Menu(natTable);
		natTable.setMenu(menu);

		MenuItem mntmCopy = new MenuItem(menu, SWT.PUSH);
		mntmCopy.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (columnPosition > 2 && rowPosition > 1) {
					ColumnGroup columnGroup = columnGroupModel.getColumnGroupByIndex(columnPosition - 1);
					if (columnGroup != null) {
						// add event to show the single commit view
						showView(natTable, columnGroup);
					}
				} else {
					MessageBox msgDialog = new MessageBox(natTable.getShell(),
							SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
					msgDialog.setMessage("Popup menu can be run at the changeType and task columns");
					msgDialog.open();
				}
			}

			private void showView(NatTable natTable, ColumnGroup columnGroup) {
				String itemValue = bodyMenu.getItem(0).getText();
				String commitID = columnGroup.getName();
				CommitData commitData = getCommitData(commitID);
				if (itemValue.equals(diffViewMenu)) {
					ILayerCell taskElementCell = natTable.getCellByPosition(taskElementColumn, rowPosition);
					String taskElementID = (String) taskElementCell.getDataValue();
					TaskElement taskElement = findTaskElement(commitData, taskElementID);
					if (taskElement != null) {
						teDialog = new TaskElementDialog();
						ILayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);
						Rectangle bounds = cell.getBounds();
						teDialog.drawDialog(taskElement, bounds.x, bounds.y);
					}
				}
				if (itemValue.equals(crViewMenu)) {
					CodeChangeExtractionControl ccec = CodeChangeExtractionControl.getInstance();
					ccec.visualizeSingleCommit(natTable, commitData.getCommitID(), commitData.getPrevCommitID(),
							commitData.getTaskElementHashmap(), commitData.getTaskList());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});

		return new PopupMenuBuilder(natTable, menu);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {

		MouseEventMatcher matcher = new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON) {

			@Override
			public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
				if (super.matches(natTable, event, regionLabels)) {
					columnPosition = natTable.getColumnPositionByX(event.x);
					rowPosition = natTable.getRowPositionByY(event.y);
					setMenu(natTable);
				} else {
					columnPosition = natTable.getColumnPositionByX(event.x);
					rowPosition = natTable.getRowPositionByY(event.y);
					setMenu(natTable);
				}
				return false;
			}

		};
		uiBindingRegistry.registerMouseDownBinding(matcher, new CellPopupMenuAction(bodyMenu));
	}

	private CommitData getCommitData(String commitID) {
		for (CommitData commitData : commitDataList) {
			String targetCommitID = commitData.getCommitID();
			if (targetCommitID.equals(commitID))
				return commitData;
		}
		return null;
	}

	private TaskElement findTaskElement(CommitData commitData, String taskElementID) {
		List<List<TaskElement>> taskList = commitData.getTaskList();
		for (List<TaskElement> task : taskList) {
			for (TaskElement taskElement : task) {
				String elementID = taskElement.getTaskElementID();
				if (elementID.contains(taskElementID))
					return taskElement;
			}
		}
		return null;

	}

	private void setMenu(NatTable natTable) {
		ILayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);
		String cellValue = (String) cell.getDataValue();
		MenuItem item = bodyMenu.getItem(0);
		if (cellValue.equals(InsideClassChangeType.Added.name())
				|| cellValue.equals(InsideClassChangeType.Modified.name())
				|| cellValue.equals(InsideClassChangeType.Deleted.name())) {
			item.setText(diffViewMenu);
		}
		if (cellValue.contains("Task")) {
			item.setText(crViewMenu);
		}
	}
}

package commit_task_visualization.nat_table_multiple_task_visualization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import commit_task_visualization.code_change_extraction.CodeChangeExtractionControl;
import commit_task_visualization.nat_table_multiple_task_visualization.model.CommitTableDataGenerator;
import commit_task_visualization.nat_table_multiple_task_visualization.util.NatTableConstants;
import commit_task_visualization.task_visualization.model.CommitData;

public class MultipleCommitViewDialog extends Dialog {

	private static final String ADD_LABEL = "ADD";
	private static final String MODIFIED_LABEL = "MODIFIED";
	private static final String DELETED_LABEL = "DELETE";
	private static final String DEFAULT_LABEL = "NONE";
	private static final String TASK_LABEL = "taskLabel";

	private static final int columnGroupPosition = 0;
	private static final int Default_Height = 500;
	private static final int Default_Width = 500;

	private List<String> columns;
	private List<LinkedHashMap<String, String>> values;
	private NatTable natTable;
	private List<CommitData> commitDataList;
	private DataLayer bodyDataLayer;

	public MultipleCommitViewDialog(Shell parentShell, List<CommitData> commitDataList) {
		super(parentShell);
		this.commitDataList = commitDataList;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		this.columns = new ArrayList<>();
		this.values = new ArrayList<>();

		Composite container = (Composite) super.createDialogArea(parent);

		CommitTableDataGenerator ctdg = new CommitTableDataGenerator(this.commitDataList);
		ctdg.getInput(this.values);
		String[] propertyNames = ctdg.getPROPERTY_NAMES();
		columns = Arrays.asList(propertyNames);

		ConfigRegistry configRegistry = new ConfigRegistry();

		ColumnGroupModel columnGroupModel = new ColumnGroupModel();

		EventList<LinkedHashMap<String, String>> eventValues = GlazedLists.eventList(this.values);
		SortedList<LinkedHashMap<String, String>> sortedList = new SortedList<>(eventValues, null);

		IColumnPropertyAccessor<LinkedHashMap<String, String>> accessor = new CommitDataColumnPropertyAccessor(
				this.columns);
		IDataProvider bodyDataProvider = new ListDataProvider<>(sortedList, accessor);
		bodyDataLayer = new DataLayer(bodyDataProvider);

		GlazedListsEventLayer<LinkedHashMap<String, String>> eventLayer = new GlazedListsEventLayer<>(bodyDataLayer,
				sortedList);

		ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(eventLayer);
		ColumnGroupReorderLayer columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer,
				columnGroupModel);
		SelectionLayer selectionLayer = new SelectionLayer(columnGroupReorderLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		// build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, null);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		SortHeaderLayer<LinkedHashMap<String, String>> sortHeaderLayer = new SortHeaderLayer<>(columnHeaderLayer,
				new GlazedListsSortModel<>(sortedList, accessor, configRegistry, columnHeaderDataLayer));
		ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(sortHeaderLayer, selectionLayer,
				columnGroupModel);
		groupColumns(columnGroupHeaderLayer, this.commitDataList, columnGroupModel);

		// build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

		// build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider,
				rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnGroupHeaderLayer);

		// build the grid layer
		GridLayer gridLayer = new GridLayer(viewportLayer, columnGroupHeaderLayer, rowHeaderLayer, cornerLayer);
		// Custom label according to the change type.
		IConfigLabelAccumulator cellLabelAccumulator = new IConfigLabelAccumulator() {

			@Override
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				String value = (String) bodyDataProvider.getDataValue(columnPosition, rowPosition);
				if (value.equals(ADD_LABEL)) {
					configLabels.addLabel(ADD_LABEL);
				}
				if (value.equals(MODIFIED_LABEL)) {
					configLabels.addLabel(MODIFIED_LABEL);
				}
				if (value.equals(DELETED_LABEL)) {
					configLabels.addLabel(DELETED_LABEL);
				}
				if (value.equals(DEFAULT_LABEL)) {
					configLabels.addLabel(DEFAULT_LABEL);
				}
				if (value.contains(NatTableConstants.TASK_CELL_VALUE)) {
					configLabels.addLabel(TASK_LABEL);
				}
			}
		};
		// Register label accumulator with the data layer
		bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);
		// turn the auto configuration off as we want to add our header menu
		// configuration
		natTable = new NatTable(container, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new SingleClickSortConfiguration());
		natTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int rowPosition = natTable.getRowPositionByY(e.y);
				int columnIndex = natTable.getColumnPositionByX(e.x) - 1;
				if (rowPosition == columnGroupPosition) {
					ColumnGroup columnGroup = columnGroupModel.getColumnGroupByIndex(columnIndex);
					if (columnGroup != null) {
						// add event to show the single commit view
						String commitID = columnGroup.getName();
						CommitData commitData = getCommitData(commitID);
						if (commitData != null) {
							CodeChangeExtractionControl ccec = CodeChangeExtractionControl.getInstance();
							ccec.visualizeSingleCommit(container, commitData.getCommitID(), commitData.getPrevCommitID(),
									commitData.getTaskElementHashmap(), commitData.getTaskList());
						}else {
							MessageBox msgDialog = new MessageBox(container.getShell(), SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
							msgDialog.setMessage("The Commit Data is not exist");
							msgDialog.open();
						}
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			private CommitData getCommitData(String commitID) {
				for (CommitData commitData : commitDataList) {
					String targetCommitID = commitData.getCommitID();
					if (targetCommitID.equals(commitID))
						return commitData;
				}
				return null;
			}
			
		});

		natTable.addConfiguration(new AbstractRegistryConfiguration() {

			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// add cell style
				Style addCellStyle = new Style();
				addCellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_GREEN);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, addCellStyle,
						DisplayMode.NORMAL, ADD_LABEL);
				// modified cell style
				Style modifiedCellStyle = new Style();
				modifiedCellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_YELLOW);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, modifiedCellStyle,
						DisplayMode.NORMAL, MODIFIED_LABEL);
				// deleted cell style
				Style deletedCellStyle = new Style();
				deletedCellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, deletedCellStyle,
						DisplayMode.NORMAL, DELETED_LABEL);
				// default cell style
				Style defaultCellStyle = new Style();
				defaultCellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, defaultCellStyle,
						DisplayMode.NORMAL, DEFAULT_LABEL);

				Font font = new Font(container.getDisplay(), "Tahoma", 10, SWT.BOLD);
				Style taskCellStyle = new Style();
				taskCellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, taskCellStyle,
						DisplayMode.NORMAL, TASK_LABEL);
			}
		});
		natTable.setConfigRegistry(configRegistry);
		natTable.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		System.out.println(bodyDataLayer.getWidth() + bodyDataLayer.getHeight());
		return container;

	}

	private void groupColumns(ColumnGroupHeaderLayer columnGroupHeaderLayer, List<CommitData> commitDataList,
			ColumnGroupModel columnGroupModel) {
		int maximumColumnSize = commitDataList.size() * 2;
		int index = 3;
		if (maximumColumnSize == columns.size() - 2) {
			for (int i = 2; i < columns.size(); i++) {
				if (i % 2 != 0) {
					int commitIndex = i - index;
					CommitData commitData = commitDataList.get(commitIndex);
					String commitID = commitData.getCommitID();
					columnGroupHeaderLayer.addColumnsIndexesToGroup(commitID, i - 1, i);
					index++;
				}
			}
		}
		for (CommitData commitData : commitDataList) {
			String commitID = commitData.getCommitID();
			ColumnGroup columnGroup = columnGroupModel.getColumnGroupByName(commitID);
			columnGroup.setUnbreakable(true);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		if (bodyDataLayer != null)
			return new Point(bodyDataLayer.getWidth(), bodyDataLayer.getHeight());
		else
			return new Point(Default_Width, Default_Height);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Multi Commit View");
	}
}
package commit_task_visualization.table_view;


import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public abstract class CommitTableColumn extends ColumnLabelProvider {

	public abstract String getText(Object element);
	public abstract String getTitle();
	
	public abstract int getWidth();
	
	public TableViewerColumn addColumnTo(TableViewer viewer) {
		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = tableViewerColumn.getColumn();
		column.setResizable(false);
		column.setText(getTitle());
		column.setWidth(getWidth());
		tableViewerColumn.setLabelProvider(this);
		return tableViewerColumn;
	}
	
}

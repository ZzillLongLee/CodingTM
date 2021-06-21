package commit_task_visualization.development_task_list_visualization;

import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;

public class CommitDataColumnPropertyAccessor implements IColumnPropertyAccessor<LinkedHashMap<String, String>> {

	private List<String> columns;

	public CommitDataColumnPropertyAccessor(List<String> columns) {
		this.columns = columns;
	}
	
	@Override
	public Object getDataValue(LinkedHashMap<String, String> rowObject, int columnIndex) {
		return rowObject.get(getColumnProperty(columnIndex));
	}

	@Override
	public void setDataValue(LinkedHashMap<String, String> rowObject, int columnIndex, Object newValue) {
		rowObject.put(getColumnProperty(columnIndex), newValue.toString());
	}

	@Override
	public int getColumnCount() {
		return this.columns.size();
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		return this.columns.get(columnIndex);
	}

	@Override
	public int getColumnIndex(String propertyName) {
		return this.columns.indexOf(propertyName);
	}
}

package commit_task_visualization.task_visualization.dialog;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskStatement;


public class StatementTableModel extends AbstractTableModel {

	private static String[] columnNames = { "Statement pastCode", "Statement currentCode", "Connected Element" };

	protected Class[] columnClasses = new Class[] { String.class, String.class, String.class };

	private List<TaskStatement> stmtList;

	public StatementTableModel(List<TaskStatement> stmts) {
		stmtList = stmts;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		int size;
		if (stmtList == null) {
			size = 0;
		} else {
			size = stmtList.size();
		}
		return size;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Class getColumnClass(int col) {
		return columnClasses[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object temp = null;
		if (columnIndex == 0) {
			Object obj = stmtList.get(rowIndex);
			if (obj instanceof TaskStatement) {
				TaskStatement ts = (TaskStatement) obj;
				String pastCode = ts.getPastCode();
				if (pastCode != null)
					temp = pastCode;
				else
					temp = "";
			}
		} else if (columnIndex == 1) {
			Object obj = stmtList.get(rowIndex);
			if (obj instanceof TaskStatement) {
				TaskStatement ts = (TaskStatement) obj;
				String currentCode = ts.getCurrentCode();
				if (currentCode != null)
					temp = currentCode;
				else
					temp = "";
			}
		} else if (columnIndex == 2) {
			temp = "";
			StringBuilder sb = new StringBuilder();
			Object obj = stmtList.get(rowIndex);
			if (obj instanceof TaskStatement) {
				TaskStatement ts = (TaskStatement) obj;
				List<String> connectedIDs = ts.getconnecteIdList();
				if (connectedIDs.size() != 0) {
					for (int i = 0; i < connectedIDs.size(); i++) {
						String id = connectedIDs.get(i);
						if (i != connectedIDs.size() - 1)
							sb.append(id + ",");
						else
							sb.append(id);
					}
					temp = new String(sb);
				}
			}
		}
		return temp;
	}

	public static String[] getColumnNames() {
		return columnNames;
	}

}

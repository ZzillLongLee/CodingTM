package commit_task_visualization.task_visualization.nat_table.model;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;



public class CommitDataComparator implements Comparator<LinkedHashMap<String, String>> {

	int colIdx = 0;
	SortDirectionEnum sortDirection;

	public CommitDataComparator(int columnIndex, SortDirectionEnum sortDirection) {
		this.colIdx = columnIndex;
		this.sortDirection = sortDirection;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(LinkedHashMap<String, String> row1, LinkedHashMap<String, String> row2) {
		Comparable compareObject1 = null;
		Comparable compareObject2 = null;

		compareObject1 = getCompareObject(row1);
		compareObject2 = getCompareObject(row2);
		
		int result = 0;

		// make null safe compare
		if (compareObject1 == null) {
			if (compareObject2 != null) {
				result = -1;
			} else {
				result = 0;
			}
		} else {
			if (compareObject2 != null) {
				result = compareObject1.compareTo(compareObject2);
			} else {
				result = 1;
			}
		}

		// negate compare result if sort direction is descending
		if (this.sortDirection.equals(SortDirectionEnum.DESC)) {
			result = result * -1;
		}

		return result;
	}

	private Comparable getCompareObject(LinkedHashMap<String, String> row1) {
		Comparable compareObject1 = null;
		int i = 0;
		for (Entry<String, String> columnValue : row1.entrySet()) {
			if (i == this.colIdx) {
				compareObject1 = columnValue.getValue();
			}
		}
		return compareObject1;
	}

}

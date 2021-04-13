package commit_task_visualization.task_visualization.nat_table.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

public class CommitDataSortModel implements ISortModel {

	/**
	 * Array that contains the sort direction for every column. Needed to access the
	 * current sort state of a column.
	 */
	protected SortDirectionEnum[] sortDirections;

	/**
	 * Array that contains the sorted flags for every column. Needed to access the
	 * current sort state of a column.
	 */
	protected boolean[] sorted;

	/**
	 * As this implementation only supports single column sorting, this property
	 * contains the the column index of the column that is currently used for
	 * sorting. Initial value = -1 for no sort column
	 */
	protected int currentSortColumn = -1;

	/**
	 * As this implementation only supports single column sorting, this property
	 * contains the current sort direction of the column that is currently used for
	 * sorting.
	 */
	protected SortDirectionEnum currentSortDirection = SortDirectionEnum.ASC;

	/**
	 * Data list that is sorted
	 */
	private List<LinkedHashMap<String, String>> inputData;

	/**
	 * Creates a new {@link PersonWithAddressSortModel} for the list of objects.
	 *
	 * @param inputData the list of objects that should be sorted
	 */
	public CommitDataSortModel(List<LinkedHashMap<String, String>> inputData, int columnSize) {
		this.inputData = inputData;

		this.sortDirections = new SortDirectionEnum[columnSize];
		Arrays.fill(this.sortDirections, SortDirectionEnum.NONE);

		this.sorted = new boolean[columnSize];
		Arrays.fill(this.sorted, false);

		// call initial sorting
		sort(0, SortDirectionEnum.ASC, false);
	}

	/**
	 * As this is a simple implementation of an {@link ISortModel} and we don't
	 * support multiple column sorting, this list returns either a list with one
	 * entry for the current sort column or an empty list.
	 */
	@Override
	public List<Integer> getSortedColumnIndexes() {
		List<Integer> indexes = new ArrayList<>();
		if (this.currentSortColumn > -1) {
			indexes.add(Integer.valueOf(this.currentSortColumn));
		}
		return indexes;
	}

	/**
	 * @return TRUE if the column with the given index is sorted at the moment.
	 */
	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		return this.sorted[columnIndex];
	}

	/**
	 * @return the direction in which the column with the given index is currently
	 *         sorted
	 */
	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		return this.sortDirections[columnIndex];
	}

	/**
	 * @return 0 as we don't support multiple column sorting.
	 */
	@Override
	public int getSortOrder(int columnIndex) {
		return 0;
	}

	/**
	 * Remove all sorting
	 */
	@Override
	public void clear() {
		Arrays.fill(this.sortDirections, SortDirectionEnum.NONE);
		Arrays.fill(this.sorted, false);
		this.currentSortColumn = -1;
	}

	/**
	 * This method is called by the {@link SortCommandHandler} in response to a sort
	 * command. It is responsible for sorting the requested column.
	 */
	@Override
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		if (!isColumnIndexSorted(columnIndex)) {
			clear();
		}

		if (sortDirection.equals(SortDirectionEnum.NONE)) {
			// we don't support NONE as user action
			sortDirection = SortDirectionEnum.ASC;
		}

		Collections.sort(this.inputData, new CommitDataComparator(columnIndex, sortDirection));
		this.sortDirections[columnIndex] = sortDirection;
		this.sorted[columnIndex] = sortDirection.equals(SortDirectionEnum.NONE) ? false : true;

		this.currentSortColumn = columnIndex;
		this.currentSortDirection = sortDirection;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return null;
	}

	@Override
	public Comparator<?> getColumnComparator(int columnIndex) {
		return null;
	}
}

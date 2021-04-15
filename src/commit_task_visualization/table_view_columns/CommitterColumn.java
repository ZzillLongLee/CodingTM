package commit_task_visualization.table_view_columns;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;

public class CommitterColumn extends CommitTableColumn {

	@Override
	public String getText(Object element) {
		if (element instanceof CodeSnapShot) {
			CodeSnapShot codeSnapShot = (CodeSnapShot) element;
			return codeSnapShot.getCommit().getCommitterIdent().getName();
		} else
			return null;
	}

	@Override
	public String getTitle() {
		return "Committer";
	}

	@Override
	public int getWidth() {
		return 80;
	}

}

package commit_task_visualization.table_view_columns;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;

public class CommitMsgColumn extends CommitTableColumn {

	@Override
	public String getText(Object element) {
		if (element instanceof CodeSnapShot) {
			CodeSnapShot codeSnapShot = (CodeSnapShot) element;
			return codeSnapShot.getCommit().getShortMessage();
		} else
			return null;
	}

	@Override
	public String getTitle() {
		return "Commit MSG";
	}

	@Override
	public int getWidth() {
		return 250;
	}

}

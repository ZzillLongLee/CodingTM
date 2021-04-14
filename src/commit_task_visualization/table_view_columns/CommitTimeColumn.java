package commit_task_visualization.table_view_columns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.lib.PersonIdent;

import commit_task_visualization.code_change_extraction.model.CodeSnapShot;

public class CommitTimeColumn extends CommitTableColumn {

	@Override
	public String getText(Object element) {
		if (element instanceof CodeSnapShot) {
			CodeSnapShot codeSnapShot = (CodeSnapShot) element;
			PersonIdent authorIdent = codeSnapShot.getCommit().getAuthorIdent();
			Date authorDate = authorIdent.getWhen();
			return authorDate.toString();  
		} else
			return null;
	}

	@Override
	public String getTitle() {
		return "Commit Time";
	}

	@Override
	public int getWidth() {
		return 180;
	}

}

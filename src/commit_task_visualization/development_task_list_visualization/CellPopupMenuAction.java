package commit_task_visualization.development_task_list_visualization;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

public class CellPopupMenuAction implements IMouseAction {

	private final Menu menu;

	public CellPopupMenuAction(Menu menu) {
		this.menu = menu;
	}

	@Override
	public void run(NatTable natTable, MouseEvent event) {
	}

}

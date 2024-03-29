package commit_task_visualization.causal_relationship_visualization.aggregation_view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.SwingUtilities;

import commit_task_visualization.causal_relationship_visualization.VisualizationConstants;
import commit_task_visualization.causal_relationship_visualization.aggregation_view.tree_table.TreeTableView;
import commit_task_visualization.causal_relationship_visualization.task_element_diff_visualization.TaskElementDiffDialog;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;

/**
 * Interactive drag control that is "aggregate-aware"
 */
class AggregationViewDragControl extends ControlAdapter {

	private VisualItem activeItem;
	protected Point2D down = new Point2D.Double();
	protected Point2D temp = new Point2D.Double();
	protected boolean dragged;
	private TaskElementDiffDialog teDialog;
	private final Font defaultFont = FontLib.getFont("SansSerif", 10);
	private AggregationTypeCRVisualizer taskVisualizer;
	private SingleTaskAggregationViewCRVis singleTaskVisualizer;
	private TreeTableView ttv;
	private Box treeTableBox;
	private Box defaultTreeTableBox;
	private boolean isSingleTaskVis;

	/**
	 * Creates a new drag control that issues repaint requests as an item is
	 * dragged.
	 * @param defaultTreeTableBox 
	 * @param taskVisualizer 
	 * 
	 * @param prevCommitID
	 * @param curCommitID
	 */
	public AggregationViewDragControl(AggregationTypeCRVisualizer taskVisualizer, Box defaultTreeTableBox) {
		this.defaultTreeTableBox = defaultTreeTableBox;
		ttv = new TreeTableView();
		teDialog = new TaskElementDiffDialog();
		this.taskVisualizer = taskVisualizer;
		this.isSingleTaskVis = false;
	}
	
	public AggregationViewDragControl(SingleTaskAggregationViewCRVis taskVisualizer, Box defaultTreeTableBox) {
		this.defaultTreeTableBox = defaultTreeTableBox;
		ttv = new TreeTableView();
		teDialog = new TaskElementDiffDialog();
		this.singleTaskVisualizer = taskVisualizer;
		this.isSingleTaskVis = true;
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemEntered(VisualItem item, MouseEvent e) {
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		activeItem = item;
		if (!(item instanceof AggregateItem))
			setFixed(item, true);
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		if (activeItem == item) {
			activeItem = null;
			setFixed(item, false);
		}
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getDefaultCursor());
		if (item instanceof Node) {
			Node node = (Node) item;
			Iterator edges = node.edges();
			while (edges.hasNext()) {
				TableEdgeItem edgeItem = (TableEdgeItem) edges.next();
				NodeItem targetItem = edgeItem.getTargetItem();
				edgeItem.setSize(1);
				targetItem.setFont(defaultFont);
				targetItem.setTextColor(ColorLib.color(Color.black));
			}
		}
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemPressed(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		dragged = false;
		Display d = (Display) e.getComponent();
		d.getAbsoluteCoordinate(e.getPoint(), down);
		if (item instanceof AggregateItem)
			setFixed(item, true);
		if (item instanceof Node && e.getClickCount() == VisualizationConstants.CLICK) {
			Node node = (Node) item;
			Iterator edges = node.edges();
			while (edges.hasNext()) {
				TableEdgeItem edgeItem = (TableEdgeItem) edges.next();
				NodeItem targetItem = edgeItem.getTargetItem();
				targetItem.setFont(FontLib.getFont("Bold", 15));
				targetItem.setTextColor(ColorLib.color(Color.red));
				edgeItem.setSize(6);
			}
			// show treeTable for causally linked task elements.
			defaultTreeTableBox.removeAll();
			defaultTreeTableBox.setVisible(false);
			treeTableBox = ttv.buildTreeTableView(treeTableBox, node);
			if(this.isSingleTaskVis == true)
				singleTaskVisualizer.add(treeTableBox, BorderLayout.SOUTH);
			else
				taskVisualizer.add(treeTableBox, BorderLayout.SOUTH);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemReleased(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (dragged) {
			activeItem = null;
			setFixed(item, false);
			dragged = false;
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemDragged(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		dragged = true;
		Display d = (Display) e.getComponent();
		d.getAbsoluteCoordinate(e.getPoint(), temp);
		double dx = temp.getX() - down.getX();
		double dy = temp.getY() - down.getY();

		move(item, dx, dy);

		down.setLocation(temp);
	}

	protected static void setFixed(VisualItem item, boolean fixed) {
		if (item instanceof AggregateItem) {
			Iterator items = ((AggregateItem) item).items();
			while (items.hasNext()) {
				setFixed((VisualItem) items.next(), fixed);
			}
		} else {
			item.setFixed(fixed);
		}
	}

	protected static void move(VisualItem item, double dx, double dy) {
		if (item instanceof AggregateItem) {
			Iterator items = ((AggregateItem) item).items();
			while (items.hasNext()) {
				move((VisualItem) items.next(), dx, dy);
			}
		} else {
			double x = item.getX();
			double y = item.getY();
			item.setStartX(x);
			item.setStartY(y);
			item.setX(x + dx);
			item.setY(y + dy);
			item.setEndX(x + dx);
			item.setEndY(y + dy);
		}
	}

} // end of class AggregateDragControl

package commit_task_visualization.causal_relationship_visualization.aggregation_view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import commit_task_visualization.causal_relationship_visualization.TaskElementNodeRepo;
import commit_task_visualization.causal_relationship_visualization.TaskVisualizerUtil;
import commit_task_visualization.causal_relationship_visualization.VisualizationConstants;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementUtil;
import commit_task_visualization.code_change_extraction.state_enum.InsideClassChangeType;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

public class AggregationTypeCRVisualizer extends JPanel {
	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
	public static final String ARROWS = "graph.arrows";
	public static final String AGGR = "aggregates";
	public static final int add_color = ColorLib.rgb(51, 251, 51);
	public static final int delete_color = ColorLib.rgb(255, 51, 51);
	public static final int modified_color = ColorLib.rgb(255, 255, 51);
	public static final int FIpro_TASK_COLOR = ColorLib.rgb(246, 204, 255);
	public static String curCommitID = "";
	public static String prevCommitID = "";
	private Visualization m_vis;
	private Display m_Display;

	public AggregationTypeCRVisualizer(HashMap<String, TaskElement> taskElementHashmap, List<List<TaskElement>> taskList,
			String curCommitID, String prevCommitID) {
		// initialize display and data
		super(new BorderLayout());
		this.m_vis = new Visualization();
		this.curCommitID = curCommitID;
		this.prevCommitID = prevCommitID;
		initDataGroups(taskElementHashmap, taskList);

		// set up the renderers
		// draw the nodes as basic shapes
		Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_STACK);
		((PolygonRenderer) polyR).setCurveSlack(0.15f);

		LabelRenderer labelR = new LabelRenderer(VisualizationConstants.TASKELEMENT_LABEL);
		labelR.setRoundedCorner(20, 20); // round the corners

		EdgeRenderer edgeRenderer = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_CURVE);

		DefaultRendererFactory drf = new DefaultRendererFactory();
		drf.setDefaultRenderer(labelR);
		drf.setDefaultEdgeRenderer(edgeRenderer);
		drf.add("ingroup('aggregates')", polyR);
		m_vis.setRendererFactory(drf);

		ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
		nEdges.setDefaultColor(ColorLib.gray(100));

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nEdges);

		// now create the main layout routine
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(colors);
		layout.add(new ForceDirectedLayout(GRAPH, true));
		layout.add(new TaskVisualizerLayout(AGGR));
		layout.add(new RepaintAction());
		m_vis.putAction("layout", layout);

		Box defaultTreeTableBox = new Box(BoxLayout.X_AXIS);
		defaultTreeTableBox.add(new JScrollPane());
		defaultTreeTableBox.add(new JScrollPane());
		defaultTreeTableBox.setVisible(true);
		add(defaultTreeTableBox, BorderLayout.SOUTH);
		
		// set up the display
		m_Display = new Display(m_vis);
		m_Display.setPreferredSize(new Dimension(800, 700));
		m_Display.pan(500, 500);
		m_Display.setHighQuality(true);
		m_Display.addControlListener(new AggregationViewDragControl(this, defaultTreeTableBox));
		m_Display.addControlListener(new ZoomControl());
		m_Display.addControlListener(new PanControl());

		add(m_Display, BorderLayout.CENTER);
		// set things running
		m_vis.run("layout");
	}

	private void initDataGroups(HashMap<String, TaskElement> taskElementHashmap, List<List<TaskElement>> taskList) {
		Graph g = new Graph();

		initTable(g, taskElementHashmap);
		// add visual data groups
		VisualGraph vg = m_vis.addGraph(GRAPH, g);
		m_vis.setInteractive(EDGES, null, false);
		m_vis.setValue(NODES, null, VisualItem.SHAPE, new Integer(Constants.SHAPE_HEXAGON));
		Iterator nodes = vg.nodes();
		
		Iterator edges = vg.edges();
		while (edges.hasNext()) {
			VisualItem item = (VisualItem) edges.next();
			item.setBounds(0, 0, 30, 10);
		}
		// I have no idea why but I must not put the (Node)nodes.next(); here. Because,
		// the node doesn't show up at view.
		while (nodes.hasNext()) {
			VisualItem item = (VisualItem) nodes.next();
			Object obj = item.get(VisualizationConstants.TASKELEMENT);
			item.setTextColor(ColorLib.rgb(0, 0, 0));
			setTaskElementColor(item, obj);
		}
		// add nodes to aggregates
		AggregateTable at = m_vis.addAggregates(AGGR);
		at.addColumn(VisualItem.POLYGON, float[].class);
		at.addColumn("id", int.class);
		at.addColumn("label", String.class);
		for (int i = 0; i < taskList.size(); ++i) {
			AggregateItem aitem = (AggregateItem) at.addItem();
			aitem.setInt("id", i);
			aitem.setString("label", "Task_" + i);
			List<TaskElement> task = taskList.get(i);
			for (TaskElement taskElement : task) {
				VisualItem node = TaskVisualizerUtil.getNode(vg, taskElement);
				aitem.addItem(node);
				aitem.setStrokeColor(FIpro_TASK_COLOR);
				aitem.setFillColor(FIpro_TASK_COLOR);
			}
		}
	}

	private void setTaskElementColor(VisualItem item, Object obj) {
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			if (te.getChangedType().equals(InsideClassChangeType.Added.name())) {
				item.setFillColor(add_color);
				item.setStrokeColor(add_color);
			}
			if (te.getChangedType().equals(InsideClassChangeType.Deleted.name())) {
				item.setFillColor(delete_color);
				item.setStrokeColor(delete_color);
			}
			if (te.getChangedType().equals(InsideClassChangeType.Modified.name())) {
				item.setFillColor(modified_color);
				item.setStrokeColor(modified_color);
			}
		}
	}

	private void initTable(Graph g, HashMap<String, TaskElement> taskElementHashmap) {
		TaskElementNodeRepo taskElementNodeRepo = TaskElementNodeRepo.getInstance();
		buildTaskElementNodes(g, taskElementHashmap, taskElementNodeRepo);
		for (Entry<String, TaskElement> taskElementHash : taskElementHashmap.entrySet()) {
			TaskElement te = taskElementHash.getValue();
			Node sourceNode = taskElementNodeRepo.getNode(te.getTaskElementID());
			List<TaskElement> causedTo = te.getCausedTo();
			for (TaskElement causedToTE : causedTo) {
				Node targetNode = taskElementNodeRepo.getNode(causedToTE.getTaskElementID());
				g.addEdge(sourceNode, targetNode);
			}
		}
	}

	private void buildTaskElementNodes(Graph g, HashMap<String, TaskElement> taskElementHashmap,
			TaskElementNodeRepo taskElementNodeRepo) {
		List<Node> taskElementNodes = taskElementNodeRepo.getTaskElementNodes();
		if (!taskElementHashmap.isEmpty()) {
			if (taskElementNodes.size() != 0) {
				taskElementNodeRepo.emptyNodes();
			}
			int i = 0;
			for (Entry<String, TaskElement> taskElementHash : taskElementHashmap.entrySet()) {
				TaskElement te = taskElementHash.getValue();
				Node TaskElementNode = g.addNode();
				if (i == 0) {
					TaskElementNode.getTable().addColumn(VisualizationConstants.TASKELEMENT, TaskElement.class);
					TaskElementNode.getTable().addColumn(GraphLib.LABEL, String.class);
				}
				String label = TaskVisualizerUtil.getLabel(te.getTaskElementID());
				TaskElementNode.setString(VisualizationConstants.TASKELEMENT_LABEL, label);

				TaskElementNode.set(VisualizationConstants.TASKELEMENT, te);
				taskElementNodes.add(TaskElementNode);
				i++;
			}
		}
	}

	public JPanel showTask() {
		JPanel panel = new JPanel();
		panel.add(this);
		return panel;
	}
	
}
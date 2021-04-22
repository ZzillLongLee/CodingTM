package commit_task_visualization.single_task_visualization;

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

public class TaskVisualizer extends JPanel {
	public static final String[] legend = { "Feature Implementation", "Feature Improvement", "Etc." };
	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
	public static final String ARROWS = "graph.arrows";
	public static final String AGGR = "aggregates";
	public static final int add_color = ColorLib.rgb(172, 251, 175);
	public static final int delete_color = ColorLib.rgb(255, 69, 0);
	public static final int modified_color = ColorLib.rgb(255, 255, 0);
	public static final int FI_TASK_COLOR = ColorLib.rgb(50, 205, 50);
	public static final int FIpro_TASK_COLOR = ColorLib.rgb(246, 204, 255);
	public static final int RF_TASK_COLOR = ColorLib.rgb(102, 204, 255);
	public static String curCommitID = "";
	public static String prevCommitID = "";
	private Visualization m_vis;
	private Display m_display;

	public TaskVisualizer(HashMap<String, TaskElement> taskElementHashmap, List<List<TaskElement>> taskList,
			String curCommitID, String prevCommitID) {
		// initialize display and data
		super(new BorderLayout());
		m_vis = new Visualization();
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

		// set up the visual operators
		ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
		nFill.setDefaultColor(ColorLib.gray(255));
		nFill.add("_hover", ColorLib.gray(200));

		ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
		nEdges.setDefaultColor(ColorLib.gray(100));

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nFill);
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
		m_display = new Display(m_vis);
		m_display.setPreferredSize(new Dimension(750, 800));
		m_display.pan(500, 500);
		m_display.setHighQuality(true);
		m_display.addControlListener(new TaskVisualizerDragControl(this, defaultTreeTableBox));
		m_display.addControlListener(new ZoomControl());
		m_display.addControlListener(new PanControl());

		add(m_display, BorderLayout.CENTER);
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
			aitem.setString("label", "Task_"+i);
			List<TaskElement> task = taskList.get(i);
			boolean isTestCase = false;
			for (TaskElement taskElement : task) {
				boolean isTest = TaskElementUtil.isTest(taskElement);
				VisualItem node = TaskVisualizerUtil.getNode(vg, taskElement);
				aitem.addItem(node);
				if (isTest == true)
					isTestCase = isTest;
			}
			if (isTestCase == true && task.size() > 1) {
				aitem.setFillColor(FI_TASK_COLOR);
				aitem.setStrokeColor(ColorLib.rgb(213, 255, 128));
			}
			if (isTestCase == false && task.size() > 1) {
				aitem.setFillColor(FIpro_TASK_COLOR);
				aitem.setStrokeColor(ColorLib.rgb(255, 20, 147));
			}
			if (task.size() == 1) {
				aitem.setFillColor(RF_TASK_COLOR);
				aitem.setStrokeColor(ColorLib.rgb(51, 85, 255));
			}
		}
		addLegend();
	}

	private void setTaskElementColor(VisualItem item, Object obj) {
		if (obj instanceof TaskElement) {
			TaskElement te = (TaskElement) obj;
			if (te.getChangedType().equals(InsideClassChangeType.ADD.name())) {
				item.setStrokeColor(add_color);
				item.setFillColor(add_color);
			}
			if (te.getChangedType().equals(InsideClassChangeType.DELETE.name())) {
				item.setStrokeColor(delete_color);
				item.setFillColor(delete_color);
			}
			if (te.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
				item.setStrokeColor(modified_color);
				item.setFillColor(modified_color);
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

	private void addLegend() {

		Box legendBox = new Box(BoxLayout.X_AXIS);
		JLabel legendTitlePanel = new JLabel("legend: ");
		legendTitlePanel.setForeground(Color.black);
		legendTitlePanel.setFont(new Font("Bold", Font.BOLD, 25));
		legendBox.add(Box.createHorizontalStrut(16));
		legendBox.add(legendTitlePanel);
		JLabel FiLabel = new JLabel(legend[0] + ",");
		FiLabel.setForeground(new Color(50, 205, 50));
		FiLabel.setFont(new Font("Bold", Font.BOLD, 25));
		legendBox.add(Box.createHorizontalStrut(16));
		legendBox.add(FiLabel);
		JLabel FIproLabel = new JLabel(legend[1] + ",");
		FIproLabel.setForeground(new Color(246, 204, 255));
		FIproLabel.setFont(new Font("Bold", Font.BOLD, 25));
		legendBox.add(Box.createHorizontalStrut(24));
		legendBox.add(FIproLabel);
		JLabel RFLabel = new JLabel(legend[2]);
		RFLabel.setForeground(new Color(102, 204, 255));
		RFLabel.setFont(new Font("Bold", Font.BOLD, 25));
		legendBox.add(Box.createHorizontalStrut(16));
		legendBox.add(RFLabel);
		add(legendBox, BorderLayout.NORTH);

	}

	public JPanel showTask() {
		JPanel panel = new JPanel();
		panel.add(this);
		return panel;
	}

}
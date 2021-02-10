package commit_task_visualization.task_visualization;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

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

public class TaskVisualizer extends Display {
	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
	public static final String ARROWS = "graph.arrows";
	public static final String AGGR = "aggregates";

	public TaskVisualizer(HashMap<String, TaskElement> taskElementHashmap, List<List<TaskElement>> taskList) {
		// initialize display and data
		super(new Visualization());
		initDataGroups(taskElementHashmap, taskList);

		// set up the renderers
		// draw the nodes as basic shapes
		Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_STACK);
		((PolygonRenderer) polyR).setCurveSlack(0.15f);

		LabelRenderer labelR = new LabelRenderer(VisualizationConstants.TASKELEMENT_LABEL);
		labelR.setRoundedCorner(20, 20); // round the corners

		EdgeRenderer edgeRenderer = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_CURVE,
				prefuse.Constants.EDGE_ARROW_FORWARD);

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

		ColorAction arrow = new ColorAction(ARROWS, VisualItem.FILLCOLOR, ColorLib.red(400));

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nFill);
		colors.add(nEdges);
		colors.add(arrow);

		// now create the main layout routine
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(colors);
		layout.add(new ForceDirectedLayout(GRAPH, true));
		layout.add(new TaskVisualizerLayout(AGGR));
		layout.add(new RepaintAction());
		m_vis.putAction("layout", layout);

		// set up the display
		setSize(500, 500);
		pan(250, 250);
		setHighQuality(true);
		addControlListener(new TaskVisualizerDragControl());
		addControlListener(new ZoomControl());
		addControlListener(new PanControl());

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
			if (obj instanceof TaskElement) {
				TaskElement te = (TaskElement) obj;
				if (te.getChangedType().equals(InsideClassChangeType.ADD.name())) {
					item.setStrokeColor(ColorLib.rgb(0, 128, 0));
					item.setFillColor(ColorLib.rgb(0, 128, 0));
				}
				if (te.getChangedType().equals(InsideClassChangeType.DELETE.name())) {
					item.setStrokeColor(ColorLib.rgb(255, 69, 0));
					item.setFillColor(ColorLib.rgb(255, 69, 0));
				}
				if (te.getChangedType().equals(InsideClassChangeType.MODIFIED.name())) {
					item.setStrokeColor(ColorLib.rgb(255, 255, 0));
					item.setFillColor(ColorLib.rgb(255, 255, 0));
				}
			}
		}
		// add nodes to aggregates
		AggregateTable at = m_vis.addAggregates(AGGR);
		at.addColumn(VisualItem.POLYGON, float[].class);
		at.addColumn("id", int.class);
		for (int i = 0; i < taskList.size(); ++i) {
			AggregateItem aitem = (AggregateItem) at.addItem();
			aitem.setInt("id", i);
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
				aitem.setFillColor(ColorLib.rgb(128, 255, 149));
				aitem.setStrokeColor(ColorLib.rgb(213, 255, 128));
			} else if (isTestCase == false && task.size() > 1) {
				aitem.setFillColor(ColorLib.rgb(246, 204, 255));
				aitem.setStrokeColor(ColorLib.rgb(128, 255, 212));
			}
			else
				aitem.setFillColor(ColorLib.rgb(102, 204, 255));
				aitem.setStrokeColor(ColorLib.rgb(51, 85, 255));
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
			if(taskElementNodes.size() != 0) {
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
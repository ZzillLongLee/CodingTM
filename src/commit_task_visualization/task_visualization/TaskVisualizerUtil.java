package commit_task_visualization.task_visualization;

import java.util.Iterator;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

public class TaskVisualizerUtil {

	public static VisualItem getNode(VisualGraph vg, TaskElement targetTaskElement) {
		Iterator nodes = vg.nodes();
		while (nodes.hasNext()) {
			VisualItem node = (VisualItem) nodes.next();
			TaskElement taskElement = (TaskElement) node.get(VisualizationConstants.TASKELEMENT);
			if (taskElement.equals(targetTaskElement))
				return node;
		}
		return null;
	}

	public static String getLabel(String taskElementID) {
		String[] splitedID = taskElementID.split(VisualizationConstants.SPLITMARK);
		String packagePath = splitedID[2];
		String className = splitedID[3];
		String identifier = splitedID[4];
		return className+VisualizationConstants.SPLITMARK+identifier;
	}
	
	public static String className(String taskElementID) {
		String[] splitedID = taskElementID.split(VisualizationConstants.SPLITMARK);
		return splitedID[2];
	}
}

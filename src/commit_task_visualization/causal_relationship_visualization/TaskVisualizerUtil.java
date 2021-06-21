package commit_task_visualization.causal_relationship_visualization;

import java.util.Iterator;

import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.util.Constants;
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
		String[] splitedID = taskElementID.split(Constants.SEPERATOR);
		String packagePath = splitedID[1];
		String className = splitedID[2];
		String identifier = splitedID[3];
		return className + VisualizationConstants.SPLITMARK + identifier;
	}
	
	public static String getPackagePath(String taskElementID) {
		String[] splitedID = taskElementID.split(Constants.SEPERATOR);
		String packagePath = splitedID[1];
		return packagePath;
	}

	public static String className(String taskElementID) {
		String[] splitedID = taskElementID.split(VisualizationConstants.SPLITMARK);
		return splitedID[2];
	}

	public static String getIDwithoutCommitID(String taskElementID) {
		String[] splitedID = taskElementID.split(Constants.SEPERATOR);
		String packagePath = splitedID[1];
		String className = splitedID[2];
		String identifier = splitedID[3];
		return packagePath + VisualizationConstants.SPLITMARK + className + VisualizationConstants.SPLITMARK + identifier;
	}
}

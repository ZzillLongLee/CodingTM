package commit_task_visualization.task_visualization.tree_table;

import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.collections4.map.HashedMap;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.util.Constants;
import commit_task_visualization.task_visualization.VisualizationConstants;
import prefuse.data.Node;
import prefuse.visual.tuple.TableEdgeItem;

public class CausalLinkTreeGenerator {

	private HashedMap<DefaultMutableTreeNode, TaskElement> tableNodeMap;

	public CausalLinkTreeGenerator() {
		this.tableNodeMap = new HashedMap<DefaultMutableTreeNode, TaskElement>();
	}

	public DefaultMutableTreeNode generateCausalLinkTree(Node node, int linkType) {
		DefaultMutableTreeNode rootTreeNode = null;
		if (node != null) {
			rootTreeNode = generateNode(node);
			addChildNodes(node, linkType, rootTreeNode);
		}
		return rootTreeNode;
	}

	private void addChildNodes(Node node, int linkType, DefaultMutableTreeNode parentTreeNode) {
		if (linkType == VisualizationConstants.CAUSEDTO) {
			Iterator edges = node.inEdges();
			while (edges.hasNext()) {
				TableEdgeItem edgeItem = (TableEdgeItem) edges.next();
				Node childNode = edgeItem.getSourceNode();
				DefaultMutableTreeNode childTreeNode = generateNode(childNode);
				if (!parentTreeNode.toString().equals(childTreeNode.toString())) {
					addChildNodes(childNode, linkType, childTreeNode);
					parentTreeNode.add(childTreeNode);
				}
			}
		}
		if (linkType == VisualizationConstants.CAUSEDBY) {
			Iterator edges = node.outEdges();
			while (edges.hasNext()) {
				TableEdgeItem edgeItem = (TableEdgeItem) edges.next();
				Node childNode = edgeItem.getTargetNode();
				DefaultMutableTreeNode childTreeNode = generateNode(childNode);
				if (!parentTreeNode.toString().equals(childTreeNode.toString())) {
					addChildNodes(childNode, linkType, childTreeNode);
					parentTreeNode.add(childTreeNode);
				}
			}
		}
	}

	private DefaultMutableTreeNode generateNode(Node node) {
		TaskElement te = (TaskElement) node.get(VisualizationConstants.TASKELEMENT);
		String teID = te.getTaskElementID();
		String changeType = te.getChangedType();
		String[] splitedID = teID.split(Constants.SEPERATOR);
		String nodeValue = splitedID[splitedID.length - 1];
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(changeType + ": " + nodeValue);
		tableNodeMap.put(treeNode, te);
		return treeNode;
	}

	public TaskElement getTaskElement(DefaultMutableTreeNode node) {
		return tableNodeMap.get(node);
	}
}

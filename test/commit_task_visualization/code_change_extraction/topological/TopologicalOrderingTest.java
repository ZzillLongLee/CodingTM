package commit_task_visualization.code_change_extraction.topological;

// A Java program to print topological
// sorting of a DAG
import java.io.*;
import java.util.*;

import commit_task_visualization.topological_sort.TopologicalGraph;

public class TopologicalOrderingTest {

	// Driver code
	public static void main(String args[]) {
		// Create a graph given in the above diagram
		TopologicalGraph g = new TopologicalGraph(6);
		g.addEdge(5, 2);
		g.addEdge(5, 0);
		g.addEdge(4, 0);
		g.addEdge(4, 1);
		g.addEdge(2, 3);
		g.addEdge(3, 1);

		System.out.println("Following is a Topological " + "sort of the given graph");
		// Function Call
		g.topologicalSort();
	}
}

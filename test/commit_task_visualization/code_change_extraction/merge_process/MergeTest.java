package commit_task_visualization.code_change_extraction.merge_process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import org.junit.Test;

import commit_task_visualization.code_change_extraction.model.task_elements.Task;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElement;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementRepo;
import commit_task_visualization.code_change_extraction.model.task_elements.TaskElementUtil;


public class MergeTest {

	public static void main(String[] args) throws CloneNotSupportedException {
		TaskElementRepo taskElementRepo = new TaskElementRepo();
		Task curTask = getTaskClasses("Outcome\\1taskClasses.dat");
		String curCommitID = curTask.getCommitID();
		TaskElementUtil.insertTEtoRepo(curTask.getClasses(), taskElementRepo);
		Task prevTask = getTaskClasses("Outcome\\2taskClasses.dat");
		String prevCommitID = prevTask.getCommitID();
		TaskElementUtil.insertTEtoRepo(prevTask.getClasses(), taskElementRepo);
		HashMap<String, TaskElement> taskElementHashmap = taskElementRepo.getTaskElementHashMap();
		MergeProcessor mp = new MergeProcessor(prevCommitID, curCommitID);
		mp.mergeTwoVersion(taskElementHashmap);
		mp.updateCausalRel(taskElementHashmap, taskElementRepo);
		TaskTreeGenerator ttg = new TaskTreeGenerator(taskElementRepo);
		ttg.buildTaskTree(curTask, prevTask);
		System.out.println("Each of Task Element's causaul relationship is updated!");
	}

	public static Task getTaskClasses(String path) {
		Task task = null;
		try {
			FileInputStream fi = new FileInputStream(new File(path));
			ObjectInputStream oi = new ObjectInputStream(fi);
			task = (Task) oi.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}
}

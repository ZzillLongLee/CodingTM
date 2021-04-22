package commit_task_visualization;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import commit_task_visualization.single_task_visualization.model.CommitData;

public class MultiTaskFileWriter {

	private List<CommitData> commitDataList;
	private static final String filepath="D:\\javaWorkspace\\2021-03 workspace\\CommitTaskVisualization\\Outcome\\obj.ser";

	public MultiTaskFileWriter(List<CommitData> commitDataList) {
		this.commitDataList = commitDataList;
	}

	public void visulize() {
		WriteObjectToFile(commitDataList);
		
	}


    private void WriteObjectToFile(Object serObj) {
 
        try {
 
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("The Object  was succesfully written to a file");
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}

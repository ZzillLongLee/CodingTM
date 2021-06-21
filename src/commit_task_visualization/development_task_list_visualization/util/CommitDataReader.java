package commit_task_visualization.development_task_list_visualization.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import commit_task_visualization.causal_relationship_visualization.model.CommitData;
import commit_task_visualization.development_task_list_visualization.model.CommitTableDataGenerator;

public class CommitDataReader {

	private FileInputStream streamIn;

	public CommitDataReader(String filePath) {
		try {
			this.streamIn = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Object deserialize() {
		if (this.streamIn != null) {
			ObjectInputStream in;
			Object obj;
			try {
				in = new ObjectInputStream(streamIn);
				obj = in.readObject();
				in.close();
				return obj;
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
		return null;
	}

	public static void main(String[] args) throws FileNotFoundException {
		
		List<LinkedHashMap<String, String>> values = new ArrayList<>();
		CommitDataReader cdr = new CommitDataReader("D:\\javaWorkspace\\NatTableWorkspace\\NatTableExample\\Data\\obj.ser");
		Object obj = cdr.deserialize();
		if (obj instanceof ArrayList<?>) {
			ArrayList<CommitData> commitDataList = (ArrayList<CommitData>) obj;
			CommitTableDataGenerator ctdg = new CommitTableDataGenerator(commitDataList);
			ctdg.getInput(values);
			Map<String, String> propertyToLabelMap = new HashMap<>();

			String[] propertyNames = ctdg.getPROPERTY_NAMES();
			if (propertyNames != null) {


			}
		}
	}

}

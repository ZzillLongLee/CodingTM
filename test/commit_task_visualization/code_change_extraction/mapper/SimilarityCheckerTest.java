package commit_task_visualization.code_change_extraction.mapper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.Test;

public class SimilarityCheckerTest {

	@Test
	public void testComputeSimilarity() {
		List<String> aa = new ArrayList<String>();
		aa.add("Set");
		aa.add("String");
		aa.add("ATLAS_BUILTIN_TYPENAMES");
		aa.add("HashSet");

		List<String> bb = new ArrayList<String>();
		bb.add("String");
		bb.add("NAME_REGEX");

		int totalSize = aa.size();
		List<String> prevNames = new ArrayList<String>(aa);
		prevNames.removeAll(bb);
		int uniqueValuesSize = prevNames.size();
		double uniqueValuesSizeDouble = uniqueValuesSize;
		int intersectionSize = totalSize - uniqueValuesSize;
		double intersectionSizeDouble = intersectionSize;
		double similarity = (intersectionSizeDouble / uniqueValuesSizeDouble) * 100.0;
		System.out.println(similarity);
	}
}

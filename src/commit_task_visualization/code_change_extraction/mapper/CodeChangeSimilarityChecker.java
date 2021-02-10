package commit_task_visualization.code_change_extraction.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import commit_task_visualization.code_change_extraction.model.AttributePart;
import commit_task_visualization.code_change_extraction.util.Constants;

import java.util.Map.Entry;


public class CodeChangeSimilarityChecker {

	public HashMap<AttributePart, AttributePart> getAttributeSimilaritySet(List<AttributePart> curVersionFieldObjects,
			List<AttributePart> prevVersionFieldObjects) {
		HashMap<AttributePart, AttributePart> matchedAttributeSet = new HashMap<AttributePart, AttributePart>();
		for (AttributePart prevAttributePart : prevVersionFieldObjects) {
			String prevAttributeAsString = prevAttributePart.getFieldDecl().toString();
			List<String> prevAttrPartNames = prevAttributePart.getNames();
			HashMap<Double, AttributePart> similarAttributeSet = new HashMap<Double, AttributePart>();
			for (AttributePart curAttributePart : curVersionFieldObjects) {
				String curAttributeAsString = curAttributePart.getFieldDecl().toString();
				List<String> curAttrPartNames = curAttributePart.getNames();
				if (!curAttributeAsString.equals(prevAttributeAsString)) {
					double similarityValue = computeSimilarity(curAttrPartNames, prevAttrPartNames);
					similarAttributeSet.put(similarityValue, curAttributePart);
				}
			}
//			this part is changed to compute ratio for changing word
			Double minValue = 0.0;
			int idx = 0;
			Set<Entry<Double, AttributePart>> entrySet = similarAttributeSet.entrySet();
			for (Entry<Double, AttributePart> entry : entrySet) {
				Double distanceValue = entry.getKey();
				if (idx == 0)
					minValue = distanceValue;
				else if (distanceValue < minValue)
					minValue = distanceValue;
			}
			if (minValue > Constants.MODIFIED_STATUS_THRESHOLD_VALUE) {
				matchedAttributeSet.put(prevAttributePart, similarAttributeSet.get(minValue));
				matchedAttributeSet.put(similarAttributeSet.get(minValue), prevAttributePart);
			}
		}
		return matchedAttributeSet;
	}

	private double computeSimilarity(List<String> curAttrPartNames, List<String> prevAttrPartNames) {
		double similarity = 0.0;
		int totalSize;
		int uniqueValuesSize;
		int intersectionSize;

		try {
			totalSize = curAttrPartNames.size();
			double totalSizeDouble = totalSize;
			List<String> prevNames = new ArrayList<String>(curAttrPartNames);
			prevNames.removeAll(prevAttrPartNames);
			uniqueValuesSize = prevNames.size();
			intersectionSize = totalSize - uniqueValuesSize;
			double intersectionSizeDouble = intersectionSize;
			similarity = (intersectionSizeDouble / totalSizeDouble) * 100.0;
		} catch (ArithmeticException e) {
			System.out.println("The similarity value is :" + similarity);
		}
		return Math.ceil(similarity);
	}
}

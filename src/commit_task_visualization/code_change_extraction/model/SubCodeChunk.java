package commit_task_visualization.code_change_extraction.model;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;

public class SubCodeChunk implements Serializable{

	private List<ClassPart> classPartSet;
	private List<ClassPart> testClassPartSet;
	private List<AttributePart> attributePartSet;
	private List<MethodPart> methodPartSet;
	private List<AttributePart> testAttributePartSet;
	private List<MethodPart> testMethodPartSet;

	public SubCodeChunk(SubCodeChunkBuilder codeChunkBuilder) {
		this.classPartSet = codeChunkBuilder.classPartSet;
		this.testClassPartSet = codeChunkBuilder.testClassPartSet;
		this.attributePartSet = codeChunkBuilder.attributePartSet;
		this.methodPartSet = codeChunkBuilder.methodPartSet;
		this.testAttributePartSet = codeChunkBuilder.testAttributePartSet;
		this.testMethodPartSet = codeChunkBuilder.testMethodPartSet;
	}

	public List<ClassPart> getClassPartSet() {
		return classPartSet;
	}

	public List<ClassPart> getTestClassPartSet() {
		return testClassPartSet;
	}

	public List<AttributePart> getAttributePartSet() {
		return attributePartSet;
	}

	public List<MethodPart> getMethodPartSet() {
		return methodPartSet;
	}

	public List<AttributePart> getTestAttributePartSet() {
		return testAttributePartSet;
	}

	public List<MethodPart> getTestMethodPartSet() {
		return testMethodPartSet;
	}
	
	public void setMethodPartSet(List<MethodPart> methodPartSet) {
		this.methodPartSet = methodPartSet;
	}
	
	public void setTestMethodPartSet(List<MethodPart> testMethodPartSet) {
		this.testMethodPartSet = testMethodPartSet;
	}
	
	public void setAttributePartSet(List<AttributePart> attributePartSet) {
		this.attributePartSet = attributePartSet;
	}
	
	public void setTestAttributePartSet(List<AttributePart> testAttributePartSet) {
		this.testAttributePartSet = testAttributePartSet;
	}

	public void assignConnectedElementsToClassPart() {
		if (testClassPartSet.size() != 0) {
			for (ClassPart testClassPart : testClassPartSet) {
				testClassPart.getMethodParts().clear();
				String testClassName = testClassPart.getClassName();
				for (MethodPart testMethodPart : testMethodPartSet) {
					String parentClssName = testMethodPart.getClassName();
					if (parentClssName.equals(testClassName)) {
						testClassPart.setMethodPart(testMethodPart);
					}
				}
				for (AttributePart attributePart : testAttributePartSet) {
					String parentClssName = attributePart.getClassName();
					if (parentClssName.equals(testClassName)) {
						testClassPart.setAttributePart(attributePart);
					}
				}
			}
		}
		if (classPartSet.size() != 0) {
			for (ClassPart classPart : classPartSet) {
				String className = classPart.getClassName().toString();
				classPart.getMethodParts().clear();
				classPart.getAttributeParts().clear();
				for (MethodPart methodPart : methodPartSet) {
					String parentClssName = methodPart.getClassName();
					if (parentClssName.equals(className)) {
						classPart.setMethodPart(methodPart);
					}
				}
				for (AttributePart attributePart : attributePartSet) {
					String parentClssName = attributePart.getClassName();
					if (parentClssName.equals(className)) {
						classPart.setAttributePart(attributePart);
					}
				}
			}
		}
	}

	public static class SubCodeChunkBuilder {

		private List<ClassPart> classPartSet;
		private List<ClassPart> testClassPartSet;
		private List<AttributePart> attributePartSet;
		private List<AttributePart> testAttributePartSet;
		private List<MethodPart> methodPartSet;
		private List<MethodPart> testMethodPartSet;

		public SubCodeChunkBuilder setClassPartSet(List<ClassPart> classPartSet) {
			this.classPartSet = classPartSet;
			return this;
		}

		public SubCodeChunkBuilder setTestClassPartSet(List<ClassPart> testClassPartSet) {
			this.testClassPartSet = testClassPartSet;
			return this;
		}

		public SubCodeChunkBuilder setAttributePartSet(List<AttributePart> attributePartSet) {
			this.attributePartSet = attributePartSet;
			return this;
		}

		public SubCodeChunkBuilder setTestAttributePartSet(List<AttributePart> testAttributePartSet) {
			this.testAttributePartSet = testAttributePartSet;
			return this;
		}

		public SubCodeChunkBuilder setMethodPartSet(List<MethodPart> methodPartSet) {
			this.methodPartSet = methodPartSet;
			return this;
		}

		public SubCodeChunkBuilder setTestMethodPartSet(List<MethodPart> testMethodPartSet) {
			this.testMethodPartSet = testMethodPartSet;
			return this;
		}

		public SubCodeChunk build() {
			return new SubCodeChunk(this);
		}

	}

}

package commit_task_visualization.code_change_extraction.util;

public class Constants {
	public final static String TEST_JAVA = "Test.java";
	public final static String REQUIREMENT_NUMBER = "IOTDB-932 Removed (wrong and redundant) Verification of Paths";
	public final static String SEPERATOR = "@@@";
	public final static String CLASS_KEY_WORD = "class";
	public final static String KEY_WORD_ABSTRACT = "abstract";
	public final static String KEY_WORD_SEMICOLON = ";";
	public final static String KEY_WORD_NEWLINE = "\n";
	public final static String KEY_WORD_EMPTY = "";
	public final static String START_BLOCK = "{";
	public final static String LAST_BLOCK = "}";
	public final static String KEY_WORD_TEST = "Test";
	
	public static final String JAVA_FILE_EXTENSION = ".java";
	
	
	public final static int CUR_VERSION = 1;
	public final static int PREV_VERSION = 2;
	
	public static final double MODIFIED_STATUS_THRESHOLD_VALUE = 30.0;
	public static final String DEFAULT_CLASS_NAME = "AA";
	
	public static final int TYPE_CLASS_PART = 3;
	public static final int TYPE_ATTRIBUTE_PART = 4;
	public static final int TYPE_METHOD_PART = 5;
	
	public static final int TEST_METHOD_TRAVERSE = 6;
	public static final int ORDINARY_METHOD_TRAVERSE = 7;
	
	public static final String EDGE = "--> ";
}

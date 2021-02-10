package commit_task_visualization.code_change_extraction.ast;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.junit.Test;


public class FieldParserTest {

	@Test
	public void parseFieldTest() {

		String field = "private HashMap<String, Type> fields = new HashMap<String, Type>();";

		String fieldWithTemplate = "public class AA{ " + field.substring(0, field.length() - 1) + "}";
		System.out.println();
		
		ASTSupportSingleton astSupport = ASTSupportSingleton.getInstance();
		astSupport.parse(fieldWithTemplate, new ASTVisitor() {

			public boolean visit(SimpleName simplename) {
				if (!simplename.toString().equals("AA")) {
					System.out.println(simplename);
				}
				return true;
			}

		});
	}

}

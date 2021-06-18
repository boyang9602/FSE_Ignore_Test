package cotest_tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;

import cotest_tracker.comments.Target;
import cotest_tracker.model.COTestModel;
import cotest_tracker.model.COTestModel.Factory;
import cotest_tracker.model.Model;

public class UtilsTest {
	@Test
	public void testGetLeadingAnnotation() {
		String case1 = "@Test@Ignore";
		String case2 = "@Test(timeout=10) @Ignore(\"Hello world\")";
		String case3 = "@ Test@ Ignore ( \"\\\"\" )";
		String case4 = "@Test(timeout =    getTimeout())@Ignore";
		String case5 = "@Test public void test";
		String case6 = "@Ignore()";
		String case7 = "@Ignore(()";
		String case8 = "@Ignore()(";
		String tmp = Utils.getLeadingAnnotation(case1);
		String remaining = case1.substring(case1.indexOf(tmp) + tmp.length());
		assertEquals(tmp, "@Test");
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Ignore");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);
		
		remaining = case2;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Test(timeout=10)");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Ignore(\"Hello world\")");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case3;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@ Test");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@ Ignore ( \"\\\"\" )");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case4;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Test(timeout =    getTimeout())");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Ignore");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case5;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Test ");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case6;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Ignore()");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case7;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);

		remaining = case8;
		tmp = Utils.getLeadingAnnotation(remaining);
		assertEquals(tmp, "@Ignore()");
		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
		tmp = Utils.getLeadingAnnotation(remaining);
		assertNull(tmp);
	}

	
	@Test
	public void testRemoveLineComment() {
		String case1 = "//abc";
		String case2 = "int a = 0; //";
		String case3 = "String str = \"//\"";
		String case4 = "String str = \"xyz\"//aaa";
		String case5 = "int a; / / aaa";
		String case6 = "abc////";

		assertEquals("", Utils.removeLineComment(case1));
		assertEquals("int a = 0; ", Utils.removeLineComment(case2));
		assertEquals("String str = \"//\"", Utils.removeLineComment(case3));
		assertEquals("String str = \"xyz\"", Utils.removeLineComment(case4));
		assertEquals("int a; / / aaa", Utils.removeLineComment(case5));
		assertEquals("abc", Utils.removeLineComment(case6));
	}

	@Test
	public void testStripBlockComment() {
		String case1 = "	 * ABC\r\n"
				+ "	 *  DEF\r\n"
				+ "	 *   GHI";
		BlockComment comment = new BlockComment();
		comment.setContent(case1);
		assertEquals(" ABC\n"
				+ "  DEF\n"
				+ "   GHI\n", Utils.stripBlockComment(comment));
	}
	
	@Test
	public void testStripBlockComment2() {
		String case1 = "	@Test\r\n"
				+ "	public void testStripBlockComment() { // just for test\r\n"
				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
				+ "				+ \"	 *   GHI\";\r\n"
				+ "		BlockComment comment = new BlockComment();\r\n"
				+ "		comment.setContent(case1);\r\n"
				+ "		assertEquals(\" ABC\\n\"\r\n"
				+ "				+ \"  DEF\\n\"\r\n"
				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n"
				+ "	}";
		BlockComment comment = new BlockComment();
		comment.setContent(case1);
		assertEquals("@Test\n"
				+ "public void testStripBlockComment() { // just for test\n"
				+ "String case1 = \"	 * ABC\\r\\n\"\n"
				+ "+ \"	 *  DEF\\r\\n\"\n"
				+ "+ \"	 *   GHI\";\n"
				+ "BlockComment comment = new BlockComment();\n"
				+ "comment.setContent(case1);\n"
				+ "assertEquals(\" ABC\\n\"\n"
				+ "+ \"  DEF\\n\"\n"
				+ "+ \"   GHI\\n\", Utils.stripBlockComment(comment));\n"
				+ "}\n", Utils.stripBlockComment(comment));
	}
	
	@Test
	public void testGroupComments() {
		List<Comment> arrList = new ArrayList<Comment>();
		LineComment l1 = new LineComment();
		l1.setContent("line1");
		l1.setRange(new Range(new Position(1, 0), new Position(1, 4)));
		LineComment l2 = new LineComment();
		l2.setContent("line2");
		l2.setRange(new Range(new Position(2, 0), new Position(2, 4)));
		LineComment l3 = new LineComment();
		l3.setContent("line3");
		l3.setRange(new Range(new Position(3, 0), new Position(3, 4)));
		
		LineComment l4 = new LineComment();
		l4.setContent("line4");
		l4.setRange(new Range(new Position(5, 0), new Position(5, 4)));
		LineComment l5 = new LineComment();
		l5.setContent("line5");
		l5.setRange(new Range(new Position(6, 0), new Position(6, 4)));

		BlockComment l6 = new BlockComment();
		l6.setContent("line6");
		l6.setRange(new Range(new Position(7, 0), new Position(7, 4)));
		
		BlockComment l7 = new BlockComment();
		l7.setContent("line7");
		l7.setRange(new Range(new Position(8, 0), new Position(8, 4)));
		
		LineComment l8 = new LineComment();
		l8.setContent("line8");
		l8.setRange(new Range(new Position(9, 0), new Position(9, 4)));
		LineComment l9 = new LineComment();
		l9.setContent("line9");
		l9.setRange(new Range(new Position(10, 0), new Position(10, 4)));

		arrList.add(l1);
		arrList.add(l2);
		arrList.add(l3);
		arrList.add(l4);
		arrList.add(l5);
		arrList.add(l6);
		arrList.add(l7);
		arrList.add(l8);
		arrList.add(l9);
		
		List<Target> targets = Utils.groupComments(arrList);
		assertEquals(5, targets.size());
		assertEquals(1, targets.get(0).getBegin());
		assertEquals(3, targets.get(0).getEnd());
		assertEquals(5, targets.get(1).getBegin());
		assertEquals(6, targets.get(1).getEnd());
		assertEquals(7, targets.get(2).getBegin());
		assertEquals(7, targets.get(2).getEnd());
		assertEquals(8, targets.get(3).getBegin());
		assertEquals(8, targets.get(3).getEnd());
		assertEquals(9, targets.get(4).getBegin());
		assertEquals(10, targets.get(4).getEnd());
		assertEquals("line1\nline2\nline3\n", targets.get(0).getContent());
		assertEquals("line4\nline5\n", targets.get(1).getContent());
		assertEquals("line6\n", targets.get(2).getContent());
		assertEquals("line7\n", targets.get(3).getContent());
		assertEquals("line8\nline9\n", targets.get(4).getContent());
	}
	
	@Test
	public void testGroupComments2() {
		List<Comment> arrList = new ArrayList<Comment>();
		LineComment l1 = new LineComment();
		l1.setContent("line1");
		l1.setRange(new Range(new Position(1, 0), new Position(1, 4)));
		LineComment l2 = new LineComment();
		l2.setContent("line2");
		l2.setRange(new Range(new Position(2, 0), new Position(2, 4)));
		LineComment l3 = new LineComment();
		l3.setContent("line3");
		l3.setRange(new Range(new Position(3, 0), new Position(3, 4)));
		
		LineComment l4 = new LineComment();
		l4.setContent("line4");
		l4.setRange(new Range(new Position(5, 0), new Position(5, 4)));
		LineComment l5 = new LineComment();
		l5.setContent("line5");
		l5.setRange(new Range(new Position(6, 0), new Position(6, 4)));

		BlockComment l6 = new BlockComment();
		l6.setContent("line6");
		l6.setRange(new Range(new Position(7, 0), new Position(7, 4)));
		
		BlockComment l7 = new BlockComment();
		l7.setContent("line7");
		l7.setRange(new Range(new Position(8, 0), new Position(8, 4)));
		
		LineComment l8 = new LineComment();
		l8.setContent("line8");
		l8.setRange(new Range(new Position(9, 0), new Position(9, 4)));
		LineComment l9 = new LineComment();
		l9.setContent("line9");
		l9.setRange(new Range(new Position(10, 0), new Position(10, 4)));

		arrList.add(l1);
		arrList.add(l2);
		arrList.add(l3);
		arrList.add(l4);
		arrList.add(l5);
		arrList.add(l6);
		arrList.add(l7);
		arrList.add(l8);
		arrList.add(l9);
		
		List<Target> targets = Utils.groupComments(arrList, 2);
		assertEquals(4, targets.size());
		assertEquals(1, targets.get(0).getBegin());
		assertEquals(6, targets.get(0).getEnd());
		assertEquals(7, targets.get(1).getBegin());
		assertEquals(7, targets.get(1).getEnd());
		assertEquals(8, targets.get(2).getBegin());
		assertEquals(8, targets.get(2).getEnd());
		assertEquals(9, targets.get(3).getBegin());
		assertEquals(10, targets.get(3).getEnd());
		assertEquals("line1\nline2\nline3\n\nline4\nline5\n", targets.get(0).getContent());
		assertEquals("line6\n", targets.get(1).getContent());
		assertEquals("line7\n", targets.get(2).getContent());
		assertEquals("line8\nline9\n", targets.get(3).getContent());
	}
	
	@Test
	public void testDetectBodyEndLine() {
		String body = "{\r\n"
				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
				+ "				+ \"	 *   GHI\";\r\n"
				+ "		BlockComment comment = new BlockComment();\r\n"
				+ "		comment.setContent(case1);\r\n"
				+ "		assertEquals(\" ABC\\n\"\r\n"
				+ "				+ \"  DEF\\n\"\r\n"
				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n"
				+ "	}";
		String[] lines = body.split("\r\n");
		int end = Utils.detectBodyEndLine(lines[0], lines, 0);
		assertEquals(9, end);
		
		body = "{\r\n"
				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
				+ "				+ \"	 *   GHI\";\r\n"
				+ "		BlockComment comment = new BlockComment();\r\n"
				+ "		comment.setContent(case1);\r\n"
				+ "		assertEquals(\" ABC\\n\"\r\n"
				+ "				+ \"  DEF\\n\"\r\n"
				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n";
		lines = body.split("\r\n");
		end = Utils.detectBodyEndLine(lines[0], lines, 0);
		assertEquals(-1, end);
	}
	
	// test handling of single quote '
	@Test
	public void testDetectBodyEndLine2() {
		String body = "		{\r\n"
				+ "			String x = \"abc'ddddd\";\r\n"
				+ "			char y = 'a';\r\n"
				+ "			char z = '\\'';\r\n"
				+ "			char r = '\\n';\r\n"
				+ "		}";
		String[] lines = body.split("\r\n");
		int end = Utils.detectBodyEndLine(lines[0], lines, 0);
		assertEquals(5, end);
	}
	
	@Test
	public void testDetectCOTestMethods() throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File("src/test/resources/cotest_tracker/TestCase1.java"));
		List<Comment> comments = cu.getAllContainedComments();
		assertEquals(244 - 19 + 1, comments.size());
		
		List<Target> targets = Utils.groupComments(comments);
		assertEquals(1, targets.size());
		
		Factory fac = (decl, klass, content, startLine, endLine) -> {
			return new COTestModel(decl, klass, content, startLine, endLine, "src/test/resources/cotest_tracker/TestCase1.java", true, false, false);
		};
		List<Model> models = Utils.detectCOTestMethods(comments, fac);
		assertEquals(6, models.size());
		int[] begins = {19, 95, 112, 124, 152, 216};
		int[] ends = {92, 110, 122, 150, 214, 244};
		Iterator<Model> it = models.iterator();
		int index = 0;
		while(it.hasNext()) {
			COTestModel m = (COTestModel) it.next();
			assertEquals(begins[index], m.getStartLine());
			assertEquals(ends[index], m.getEndLine());
			index++;
		}
	}
	
	@Test
	public void testDetectCOTestMethods2() throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File("src/test/resources/cotest_tracker/TestCase2.java"));
		List<Comment> comments = cu.getAllContainedComments();
		assertEquals(1, comments.size());
		
		List<Target> targets = Utils.groupComments(comments);
		assertEquals(1, targets.size());
		
		Factory fac = (decl, klass, content, startLine, endLine) -> {
			return new COTestModel(decl, klass, content, startLine, endLine, "src/test/resources/cotest_tracker/TestCase2.java", true, false, false);
		};
		List<Model> models = Utils.detectCOTestMethods(comments, fac);
		assertEquals(6, models.size());
		int[] begins = {20, 96, 113, 125, 153, 217};
		int[] ends = {93, 111, 123, 151, 215, 245};
		Iterator<Model> it = models.iterator();
		int index = 0;
		while(it.hasNext()) {
			COTestModel m = (COTestModel) it.next();
			assertEquals(begins[index], m.getStartLine());
			assertEquals(ends[index], m.getEndLine());
			index++;
		}
	}
	
	@Test
	public void testDetectCOTestMethods3() throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File("src/test/resources/cotest_tracker/TestCase3.java"));
		List<Comment> comments = cu.getAllContainedComments();
		
		List<Target> targets = Utils.groupComments(comments, 2);
		assertEquals(3, targets.size());
		
		Factory fac = (decl, klass, content, startLine, endLine) -> {
			return new COTestModel(decl, klass, content, startLine, endLine, "src/test/resources/cotest_tracker/TestCase3.java", true, false, false);
		};
		List<Model> models = Utils.detectCOTestMethods(comments, fac);
		assertEquals(7, models.size());
		int[] begins = {21, 75, 92, 104, 132, 197, 227};
		int[] ends = {72, 90, 102, 130, 195, 225, 230};
		Iterator<Model> it = models.iterator();
		int index = 0;
		while(it.hasNext()) {
			COTestModel m = (COTestModel) it.next();
			assertEquals(begins[index], m.getStartLine());
			assertEquals(ends[index], m.getEndLine());
			index++;
		}
	}
	
	@Test
	public void testDetectCOTestMethods4() throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File("src/test/resources/cotest_tracker/TestCase4.java"));
		List<Comment> comments = cu.getAllContainedComments();
		Factory fac = (decl, klass, content, startLine, endLine) -> {
			return new COTestModel(decl, klass, content, startLine, endLine, "src/test/resources/cotest_tracker/TestCase4.java", true, false, false);
		};
		List<Model> models = Utils.detectCOTestMethods(comments, fac);
		assertEquals(2, models.size());
		assertTrue(models.get(0).getFullyQualifiedName().contains("case2"));
		assertTrue(models.get(1).getFullyQualifiedName().contains("case3"));
	}
	
	@Test
	public void testSortComments() {
		List<Comment> comments = new ArrayList<Comment>();
		Comment c1, c2, c3, c4, c5, c6;
		c1 = new LineComment();
		c1.setRange(new Range(new Position(1, 1), new Position(1, 2)));
		c2 = new LineComment();
		c2.setRange(new Range(new Position(7, 1), new Position(7, 2)));
		c3 = new LineComment();
		c3.setRange(new Range(new Position(7, 1), new Position(7, 2)));
		c4 = new LineComment();
		c4.setRange(new Range(new Position(7, 2), new Position(7, 3)));
		
		c5 = new BlockComment();
		c5.setRange(new Range(new Position(3, 1), new Position(3, 2)));
		c6 = new BlockComment();
		c6.setRange(new Range(new Position(4, 1), new Position(4, 2)));

		comments.add(c1);
		comments.add(c2);
		comments.add(c3);
		comments.add(c4);
		comments.add(c5);
		comments.add(c6);
		
		Collections.shuffle(comments);
		Utils.sortComments(comments);
		assertTrue(c1 == comments.get(0));
		assertTrue(c5 == comments.get(1));
		assertTrue(c6 == comments.get(2));
		assertTrue(c2 == comments.get(3) || c2 == comments.get(4));
		assertTrue(c3 == comments.get(4) || c3 == comments.get(3));
		assertTrue(c4 == comments.get(5));
	}
	
	// test handling of single quote '
	@Test
	public void testDetectCOTestMethods5() throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File("src/test/resources/cotest_tracker/SourceFunctionTest.java"));
		List<Comment> comments = cu.getAllContainedComments();
		Factory fac = (decl, klass, content, startLine, endLine) -> {
			return new COTestModel(decl, klass, content, startLine, endLine, "src/test/resources/cotest_tracker/SourceFunctionTest.java", true, false, false);
		};
		List<Model> models = Utils.detectCOTestMethods(comments, fac);
		assertEquals(1, models.size());
		assertEquals(models.get(0).getFullyQualifiedName(), "org.apache.flink.streaming.api.SourceFunctionTest::socketTextStreamTest()");
		assertEquals(65, models.get(0).getStartLine());
		assertEquals(83, models.get(0).getEndLine());
	}
}

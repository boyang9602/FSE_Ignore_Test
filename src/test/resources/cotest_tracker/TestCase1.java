package cotest_tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;

import cotest_tracker.comments.Target;

public class TestCase1 {
//	@Test
//	public void testGetLeadingAnnotation() {
//		String case1 = "@Test@Ignore";
//		String case2 = "@Test(timeout=10) @Ignore(\"Hello world\")";
//		String case3 = "@ Test@ Ignore ( \"\\\"\" )";
//		String case4 = "@Test(timeout =    getTimeout())@Ignore";
//		String case5 = "@Test public void test";
//		String case6 = "@Ignore()";
//		String case7 = "@Ignore(()";
//		String case8 = "@Ignore()(";
//		String tmp = Utils.getLeadingAnnotation(case1);
//		String remaining = case1.substring(case1.indexOf(tmp) + tmp.length());
//		assertEquals(tmp, "@Test");
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Ignore");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//		
//		remaining = case2;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Test(timeout=10)");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Ignore(\"Hello world\")");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case3;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@ Test");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@ Ignore ( \"\\\"\" )");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case4;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Test(timeout =    getTimeout())");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Ignore");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case5;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Test ");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case6;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Ignore()");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case7;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//
//		remaining = case8;
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertEquals(tmp, "@Ignore()");
//		remaining = remaining.substring(remaining.indexOf(tmp) + tmp.length());
//		tmp = Utils.getLeadingAnnotation(remaining);
//		assertNull(tmp);
//	}
//
//	
//	@Test
//	public void testRemoveLineComment() {
//		String case1 = "//abc";
//		String case2 = "int a = 0; //";
//		String case3 = "String str = \"//\"";
//		String case4 = "String str = \"xyz\"//aaa";
//		String case5 = "int a; / / aaa";
//		String case6 = "abc////";
//
//		assertEquals("", Utils.removeLineComment(case1));
//		assertEquals("int a = 0; ", Utils.removeLineComment(case2));
//		assertEquals("String str = \"//\"", Utils.removeLineComment(case3));
//		assertEquals("String str = \"xyz\"", Utils.removeLineComment(case4));
//		assertEquals("int a; / / aaa", Utils.removeLineComment(case5));
//		assertEquals("abc", Utils.removeLineComment(case6));
//	}
//
//	@Test
//	public void testStripBlockComment() {
//		String case1 = "	 * ABC\r\n"
//				+ "	 *  DEF\r\n"
//				+ "	 *   GHI";
//		BlockComment comment = new BlockComment();
//		comment.setContent(case1);
//		assertEquals(" ABC\n"
//				+ "  DEF\n"
//				+ "   GHI\n", Utils.stripBlockComment(comment));
//	}
//	
//	@Test
//	public void testStripBlockComment2() {
//		String case1 = "	@Test\r\n"
//				+ "	public void testStripBlockComment() { // just for test\r\n"
//				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
//				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
//				+ "				+ \"	 *   GHI\";\r\n"
//				+ "		BlockComment comment = new BlockComment();\r\n"
//				+ "		comment.setContent(case1);\r\n"
//				+ "		assertEquals(\" ABC\\n\"\r\n"
//				+ "				+ \"  DEF\\n\"\r\n"
//				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n"
//				+ "	}";
//		BlockComment comment = new BlockComment();
//		comment.setContent(case1);
//		assertEquals("@Test\n"
//				+ "public void testStripBlockComment() { // just for test\n"
//				+ "String case1 = \"	 * ABC\\r\\n\"\n"
//				+ "+ \"	 *  DEF\\r\\n\"\n"
//				+ "+ \"	 *   GHI\";\n"
//				+ "BlockComment comment = new BlockComment();\n"
//				+ "comment.setContent(case1);\n"
//				+ "assertEquals(\" ABC\\n\"\n"
//				+ "+ \"  DEF\\n\"\n"
//				+ "+ \"   GHI\\n\", Utils.stripBlockComment(comment));\n"
//				+ "}\n", Utils.stripBlockComment(comment));
//	}
//	
//	@Test
//	public void testGroupComments() {
//		List<Comment> arrList = new ArrayList<Comment>();
//		LineComment l1 = new LineComment();
//		l1.setContent("line1");
//		l1.setRange(new Range(new Position(1, 0), new Position(1, 4)));
//		LineComment l2 = new LineComment();
//		l2.setContent("line2");
//		l2.setRange(new Range(new Position(2, 0), new Position(2, 4)));
//		LineComment l3 = new LineComment();
//		l3.setContent("line3");
//		l3.setRange(new Range(new Position(3, 0), new Position(3, 4)));
//		
//		LineComment l4 = new LineComment();
//		l4.setContent("line4");
//		l4.setRange(new Range(new Position(5, 0), new Position(5, 4)));
//		LineComment l5 = new LineComment();
//		l5.setContent("line5");
//		l5.setRange(new Range(new Position(6, 0), new Position(6, 4)));
//
//		BlockComment l6 = new BlockComment();
//		l6.setContent("line6");
//		l6.setRange(new Range(new Position(7, 0), new Position(7, 4)));
//		
//		BlockComment l7 = new BlockComment();
//		l7.setContent("line7");
//		l7.setRange(new Range(new Position(8, 0), new Position(8, 4)));
//		
//		LineComment l8 = new LineComment();
//		l8.setContent("line8");
//		l8.setRange(new Range(new Position(9, 0), new Position(9, 4)));
//		LineComment l9 = new LineComment();
//		l9.setContent("line9");
//		l9.setRange(new Range(new Position(10, 0), new Position(10, 4)));
//
//		arrList.add(l1);
//		arrList.add(l2);
//		arrList.add(l3);
//		arrList.add(l4);
//		arrList.add(l5);
//		arrList.add(l6);
//		arrList.add(l7);
//		arrList.add(l8);
//		arrList.add(l9);
//		
//		List<Target> targets = Utils.groupComments(arrList);
//		assertEquals(5, targets.size());
//		assertEquals(1, targets.get(0).getBegin());
//		assertEquals(3, targets.get(0).getEnd());
//		assertEquals(5, targets.get(1).getBegin());
//		assertEquals(6, targets.get(1).getEnd());
//		assertEquals(7, targets.get(2).getBegin());
//		assertEquals(7, targets.get(2).getEnd());
//		assertEquals(8, targets.get(3).getBegin());
//		assertEquals(8, targets.get(3).getEnd());
//		assertEquals(9, targets.get(4).getBegin());
//		assertEquals(10, targets.get(4).getEnd());
//		assertEquals("line1\nline2\nline3\n", targets.get(0).getContent());
//		assertEquals("line4\nline5\n", targets.get(1).getContent());
//		assertEquals("line6\n", targets.get(2).getContent());
//		assertEquals("line7\n", targets.get(3).getContent());
//		assertEquals("line8\nline9\n", targets.get(4).getContent());
//	}
//	
//	@Test
//	public void testDetectBodyEndLine() {
//		String body = "{\r\n"
//				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
//				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
//				+ "				+ \"	 *   GHI\";\r\n"
//				+ "		BlockComment comment = new BlockComment();\r\n"
//				+ "		comment.setContent(case1);\r\n"
//				+ "		assertEquals(\" ABC\\n\"\r\n"
//				+ "				+ \"  DEF\\n\"\r\n"
//				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n"
//				+ "	}";
//		String[] lines = body.split("\r\n");
//		int end = Utils.detectBodyEndLine(lines[0], lines, 0);
//		assertEquals(9, end);
//		
//		body = "{\r\n"
//				+ "		String case1 = \"	 * ABC\\r\\n\"\r\n"
//				+ "				+ \"	 *  DEF\\r\\n\"\r\n"
//				+ "				+ \"	 *   GHI\";\r\n"
//				+ "		BlockComment comment = new BlockComment();\r\n"
//				+ "		comment.setContent(case1);\r\n"
//				+ "		assertEquals(\" ABC\\n\"\r\n"
//				+ "				+ \"  DEF\\n\"\r\n"
//				+ "				+ \"   GHI\\n\", Utils.stripBlockComment(comment));\r\n";
//		lines = body.split("\r\n");
//		end = Utils.detectBodyEndLine(lines[0], lines, 0);
//		assertEquals(-1, end);
//	}
}

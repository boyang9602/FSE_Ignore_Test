package cotest_tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;

import cotest_tracker.comments.BlockCommentTarget;
import cotest_tracker.comments.LineCommentsTarget;
import cotest_tracker.comments.Target;
import cotest_tracker.model.COTestModel;
import cotest_tracker.model.COTestModel.Factory;
import cotest_tracker.model.Model;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLType;

public class Utils {
	private static Logger logger = Logger.getLogger(Utils.class);

	public static String UMLOperation2String(UMLOperation op) {
		return UMLOperation2String(op, true);
	}

	public static String UMLOperation2String(UMLOperation op, boolean withClassName) {
		List<UMLType> paramTypes = op.getParameterTypeList();
		StringBuilder sb = new StringBuilder();
		if (withClassName) {
			sb.append(op.getClassName()).append("::");
		}
		sb.append(op.getName()).append('(');
		for (int i = 0; i < paramTypes.size(); i++) {
			sb.append(paramTypes.get(i).toString().replaceAll(" ", ""));
			if (i < paramTypes.size() - 1) {
				sb.append(',');
			}
		}
		sb.append(')');
		return sb.toString();
	}

	public static String getLeadingAnnotation(String commentContent) {
		if (commentContent.length() == 0) {
			return null;
		}
		
		int start = 0;
		int end = 0;
		int curr = 0;

		char[] charArr = commentContent.toCharArray();
		
		// passing leading spaces
		for (; curr < charArr.length; curr++) {
			if (Character.isWhitespace(charArr[curr])) {
			} else {
				break;
			}
		}
		
		if (curr >= charArr.length) {
			return null;
		}
		
		// first non space char is not @
		if (charArr[curr] != '@') {
			return null;
		} else {
			start = curr++;
		}
		
		// pass the spaces between @ and the first letter
		for (; curr < charArr.length; curr++) {
			if (Character.isWhitespace(charArr[curr])) {
			} else if (Character.isJavaIdentifierPart(charArr[curr])) {
				break;
			} else {
				return null;
			}
		}

		Stack<Character> pairs = new Stack<Character>();
		char last = '\0';
		for (; curr < charArr.length; curr++) {
			if (Character.isJavaIdentifierPart(charArr[curr]) && pairs.empty() && !Character.isWhitespace(last)) {
			} else {
				if (Character.isWhitespace(charArr[curr])) {
				} else if (charArr[curr] == '(') {
					if (!pairs.empty() && pairs.lastElement() == '"') {
					} else {
						pairs.push('(');
					}
				} else {
					if (pairs.empty()) {
						end = curr;
						break;
					} else if (pairs.lastElement() == '"') {
						if (charArr[curr] == '\\') {
							curr++;
						} else if (charArr[curr] == '"') {
							pairs.pop();
						} else {
						}
					} else {
						if (charArr[curr] == '"') {
							pairs.push('"');
						} else if (charArr[curr] == ')') {
							pairs.pop();
							if (pairs.empty()) {
								end = curr + 1;
								break;
							}
						}
					}
				}
			}
			last = charArr[curr];
		}
		if (!pairs.empty()) {
			return null;
		}
		if (end == 0) {
			end = curr;
		}
		return new String(Arrays.copyOfRange(charArr, start, end));
	}
	
	public static String removeLineComment(String line) {
		char[] charArr = line.toCharArray();
		boolean quote = false;
		int i = 0;
		for (; i < charArr.length; i++) {
			if (quote) {
				if (charArr[i] == '\\') {
					i++;
					continue;
				}
				if (charArr[i] == '"') {
					quote = false;
				}
			} else {
				if (charArr[i] == '"') {
					quote = true;
				} else if (charArr[i] == '/' && i < charArr.length - 1 && charArr[i + 1] == '/') {
					break;
				}
			}
		}
		return new String(Arrays.copyOfRange(charArr, 0, i));
	}
	
	public static String stripBlockComment(BlockComment bc) {
		String content = bc.getContent();
		StringBuilder sb = new StringBuilder();
		for (String s : content.split("\n")) {
			String tmp = s.strip();
			if (tmp.length() > 0 && tmp.charAt(0) == '*') {
				sb.append(tmp.substring(1)).append('\n');
			} else {
				sb.append(tmp).append('\n');
			}
		}
		return sb.toString();
	}
	
	public static List<Target> groupComments(List<Comment> comments) {
		return groupComments(comments, 1);
	}
	
	public static List<Target> groupComments(List<Comment> comments, int minGap) {
		sortComments(comments);
		List<Target> targets = new LinkedList<Target>();
		for (int i = 0; i < comments.size(); i++) {
			StringBuilder sb = new StringBuilder();
			Comment curr = comments.get(i);
			if (curr instanceof LineComment) {
				List<LineComment> raw = new LinkedList<LineComment>();
				raw.add((LineComment) curr);
				sb.append(curr.getContent()).append('\n');
				Comment next;
				int begin = curr.getBegin().get().line;
				int end = curr.getEnd().get().line;
				while (i < comments.size() - 1) {
					next = comments.get(i + 1);
					int gap = ((Position)next.getEnd().get()).line - ((Position)curr.getEnd().get()).line;
					if (next instanceof LineComment && gap <= minGap) {
						raw.add((LineComment) next);
						for (int j = 0; j < gap - 1; j++) {
							sb.append('\n');
						}
						curr = next;
						sb.append(curr.getContent()).append('\n');
						end = curr.getEnd().get().line;
						i++;
					} else {
						break;
					}
				}
				targets.add(new LineCommentsTarget(begin, end, sb.toString(), raw));
			} else {
				targets.add(new BlockCommentTarget(curr.getBegin().get().line, curr.getEnd().get().line, stripBlockComment((BlockComment) curr), (BlockComment) curr));
			}
		}
		
		return targets;
	}
	
	public static boolean containsAnnotation(Pattern p, String line) {
		String annotation = null;
		while (true) {
			annotation = getLeadingAnnotation(line);
			if (annotation != null) {
				line = line.substring(line.indexOf(annotation) + annotation.length());
				Matcher m = p.matcher(annotation);
				if (m.lookingAt()) {
					return true;
				}
			} else {
				return false;
			}
		}
	}
	
	public static MethodDeclaration parseMethod(StringBuilder sb, String line) {
		MethodDeclaration md = null;
		int startfrom = line.indexOf('{') + 1;
		while (true) {
			try {
				md = StaticJavaParser.parseMethodDeclaration(sb.toString());
				break;
			} catch (ParseProblemException e) {
				logger.warn("ParseError: " + sb.toString(), e);
				int bodyStart = line.indexOf('{', startfrom);
				if (bodyStart >= 0) {
					sb.delete(sb.length() - startfrom - 1, sb.length());
					sb.append(line.substring(0, bodyStart)).append("{}");
					startfrom = bodyStart + 1;
				} else {
					break;
				}
			}
		}
		return md;
	}
	
	public static int detectBodyEndLine(String line, String[] lines, int end) {
		// detect the body
		Stack<Character> pairs = new Stack<Character>();
		line = line.substring(line.indexOf('{'));
		while (true) {
			try {
				char[] tmp = line.toCharArray();
				for (int l = 0; l < tmp.length; l++) {
					char c = tmp[l];
					if (c == '"') {
						if (pairs.empty()) {
							// impossible
							throw new RuntimeException("Unexpected \", " + line);
						} else {
							// end of string constant
							if (pairs.lastElement() == '"') {
								pairs.pop();
							} else { // start of string constant
								pairs.push('"');
							}
						}
					} else if (c == '\'') {
						if (!pairs.empty() && pairs.lastElement() != '"') {
							if (l < tmp.length - 2 && tmp[l + 1] != '\\' && tmp[l + 2] == '\'') {
								l += 2;
								continue;
							} else if (l < tmp.length - 2 && tmp[l + 1] == '\\' && tmp[l + 3] == '\'') {
								l += 3;
								continue;
							} else {
								throw new RuntimeException("Unexpected \', " + line);
							}
						}
					} else if (c == '\\') {
						// char escape
						if (!pairs.empty() && pairs.lastElement() == '"') {
							l++;
							continue;
						} else {
							throw new RuntimeException("Unexpected \\, " + line);
						}
					} else if (c == '{') {
						// start of a block
						if (pairs.empty() || pairs.lastElement() != '"') {
							pairs.push('{');
						}
					} else if (c == '}') {
						if (pairs.empty()) {
							// impossible
							throw new RuntimeException("Unexpected }, " + line);
						} else if (pairs.lastElement() != '"') { // end of a block
							pairs.pop();
							// it is the end of the method body
							if (pairs.empty()) {
								break;
							}
						}
					}
				}
			} catch (EmptyStackException e) {
				e.printStackTrace();
				break;
			} catch (RuntimeException e) {
				e.printStackTrace();
				break;
			}
			if (pairs.empty()) {
				break;
			}
			if (lines.length == end + 1) {
				break;
			}
			line = removeLineComment(lines[++end]);
		}
		
		// the body is incomplete
		if (!pairs.empty()) {
			return -1;
		}
		return end;
	}
	
	public static List<Comment> sortComments(List<Comment> comments) {
		Collections.sort(comments, new Comparator<Comment>() {
			@Override
			public int compare(Comment o1, Comment o2) {
				int line = o1.getBegin().get().line - o2.getBegin().get().line;
				if (line == 0) {
					return o1.getBegin().get().column - o2.getBegin().get().column;
				} else {
					return line;
				}
			}
			
		});
		return comments;
	}
	
	public static List<Model> detectCOTestMethods(List<Comment> comments, Factory fac) {
		List<Target> commentTargets = groupComments(comments, 2);
		List<Model> methods = new ArrayList<Model>();
		Pattern p = Pattern.compile("@\\s*(Test|Ignore)");
		for (Target t : commentTargets) {
			String[] lines = t.getContent().split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				boolean found = containsAnnotation(p, line);
				if (!found) {
					continue;
				}
				
				int begin = i;
				int end = i;
				StringBuilder sb = new StringBuilder();
				while (true) {
					line = removeLineComment(lines[end]);
					int bodyStart = line.indexOf('{');
					if (bodyStart >= 0) {
						sb.append(line.substring(0, bodyStart)).append("{}");
						break;
					} else {
						if (Utils.getLeadingAnnotation(line) != null || line.indexOf('(') > 0 || line.indexOf(')') > 0) {
							sb.append(line).append('\n');
						}
						if (end + 1 == lines.length) {
							break;
						}
						++end;
					}
				}
				
				if (sb.lastIndexOf("{}") != sb.length() - 2) {
					continue;
				}
				MethodDeclaration md = null;
				try {
					md = StaticJavaParser.parseMethodDeclaration(sb.toString());
				} catch (ParseProblemException e) {
					logger.warn("ParseError: " + sb.toString(), e);
					continue;
				}
				
				end = detectBodyEndLine(line, lines, end);
				
				if (end == -1) {
					continue;
				}
				
				StringBuilder content = new StringBuilder();
				for (int k = begin; k <= end; k++) {
					content.append(lines[k]).append('\n');
				}
				COTestModel cotest = fac.create(md, t.getParentNode(), content.toString(), t.getBegin() + begin, t.getBegin() + end);
				methods.add(cotest);
				i = end;
			}
		}
		return methods;
	}
	
	public static String MethodDeclaration2String(MethodDeclaration decl, boolean withParamName) {
		NodeList<Parameter> parameters = decl.getParameters();
		StringBuilder sb = new StringBuilder(decl.getNameAsString());
		sb.append('(');
		for (int i = 0; i < parameters.size(); i++) {
			Parameter param = parameters.get(i);
			sb.append(param.getType().asString().replaceAll(" ", ""));
			if (withParamName) {
				sb.append(' ').append(param.getNameAsString());
			}
			if (i < parameters.size() - 1) {
				sb.append(',');
			}
		}
		sb.append(')');
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static int getLastBodyDeclarationLineAbove(BodyDeclaration<?> target, List<BodyDeclaration> bodyDecls) {
		int result = 0;
		for (BodyDeclaration<?> decl : bodyDecls) {
			if (((Position)decl.getEnd().get()).line < ((Position)target.getBegin().get()).line && 
					((Position)decl.getEnd().get()).line > result) {
				result = ((Position)decl.getEnd().get()).line;
			}
		}
		return result;
	}
	
	public static int getLastCOTestLineAbove(BodyDeclaration<?> target, List<Model> currCOTests) {
		int result = 0;
		for (Model model : currCOTests) {
			if (model.getEndLine() < ((Position)target.getBegin().get()).line && 
					model.getEndLine() > result) {
				result = model.getEndLine();
			}
		}
		return result;
	}

	public static List<AnnotationExpr> getCOAnnotations(ClassOrInterfaceDeclaration klass, List<Comment> comments, int minLine, String... targetAnnotations) {
		Stream<Comment> releventComments = comments.stream()
				.filter(
						c -> c.getEnd().get().line > minLine && 
						c.getBegin().get().line <= klass.getName().getBegin().get().line
						);
		List<AnnotationExpr> annotations = new ArrayList<AnnotationExpr>();
		Iterator<Comment> it = releventComments.iterator();
		for (; it.hasNext();) {
			Comment c = it.next();
			if (c instanceof LineComment) {
				String content = c.getContent();
				while (true) {
					String annotation = getLeadingAnnotation(content);
					if (annotation == null) {
						break;
					}
					content = content.substring(content.indexOf(annotation) + annotation.length());
					try {
						AnnotationExpr anno = StaticJavaParser.parseAnnotation(annotation);
						annotations.add(anno);
					} catch (ParseProblemException e) {
						logger.error(String.format("Parse error, annotation: %s, comment: %s", annotation, c.getContent()));
					}
				}
			} else if (c instanceof BlockComment) {
				String content = Utils.stripBlockComment((BlockComment) c);
				for (String line : content.split("\n")) {
					while (true) {
						String annotation = getLeadingAnnotation(line);
						if (annotation == null) {
							break;
						}
						line = line.substring(line.indexOf(annotation) + annotation.length());
						try {
							AnnotationExpr anno = StaticJavaParser.parseAnnotation(annotation);
							annotations.add(anno);
						} catch (ParseProblemException e) {
							logger.error(String.format("Parse error, annotation: %s, line: %s, comment: %s", annotation, line, c.getContent()));
						}
					}
				}
			}
		}
		annotations.removeIf(a -> {
			for (String t : targetAnnotations) {
				if (a.getNameAsString().equals(t)) {
					return false;
				}
			}
			return true;
		});
		return annotations;
	}
	
	public static List<AnnotationExpr> getCOAnnotations(MethodDeclaration method, List<Comment> comments, int minLine, String... targetAnnotations) {
		Stream<Comment> releventComments = comments.stream()
				.filter(
						c -> c.getBegin().get().line > minLine && 
						c.getBegin().get().line <= method.getName().getBegin().get().line
						);
		List<AnnotationExpr> annotations = new ArrayList<AnnotationExpr>();
		Iterator<Comment> it = releventComments.iterator();
		for (;it.hasNext();) {
			Comment c = it.next();
			if (c instanceof LineComment) {
				String content = c.getContent();
				while (true) {
					String annotation = getLeadingAnnotation(content);
					if (annotation == null) {
						break;
					}
					content = content.substring(content.indexOf(annotation) + annotation.length());
					try {
						AnnotationExpr anno = StaticJavaParser.parseAnnotation(annotation);
						annotations.add(anno);
					} catch (ParseProblemException e) {
						logger.error(String.format("Parse error, annotation: %s, comment: %s", annotation, c.getContent()));
					}
				}
			} else if (c instanceof BlockComment) {
				String content = Utils.stripBlockComment((BlockComment) c);
				for (String line : content.split("\n")) {
					while (true) {
						String annotation = getLeadingAnnotation(line);
						if (annotation == null) {
							break;
						}
						line = line.substring(line.indexOf(annotation) + annotation.length());
						try {
							AnnotationExpr anno = StaticJavaParser.parseAnnotation(annotation);
							annotations.add(anno);
						} catch (ParseProblemException e) {
							logger.error(String.format("Parse error, annotation: %s, line: %s, comment: %s", annotation, line, c.getContent()));
						}
					}
				}
			}
		}
		annotations.removeIf(a -> {
			boolean hasTarget = false;
			for (String t : targetAnnotations) {
				if (a.getNameAsString().equals(t)) {
					hasTarget |= true;
				}
			}
			return !hasTarget;
		});
		return annotations;
	}
	
	public static boolean equals(List<AnnotationExpr> annos1, List<AnnotationExpr> annos2) {
		if (annos1.size() != annos2.size()) {
			return false;
		}
		List<AnnotationExpr> eqElements = new ArrayList<AnnotationExpr>();
		for (AnnotationExpr anno1 : annos1) {
			for (AnnotationExpr anno2 : annos2) {
				if (anno1.toString().equals(anno2.toString())) {
					eqElements.add(anno1);
					break;
				}
			}
		}
		return eqElements.size() == annos1.size();
	}
}

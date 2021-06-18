package cotest_tracker.comments;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;

public class LineCommentsTarget extends Target {
	private List<LineComment> raw;
	public List<LineComment> getRaw() {
		return raw;
	}
	public LineCommentsTarget(int begin, int end, String content, List<LineComment> comments) {
		super(begin, end, content);
		this.raw = comments;
	}
	@Override
	public ClassOrInterfaceDeclaration getParentNode() {
		// TODO Auto-generated method stub
		return (ClassOrInterfaceDeclaration) raw.get(0).getParentNode().get();
	}
}

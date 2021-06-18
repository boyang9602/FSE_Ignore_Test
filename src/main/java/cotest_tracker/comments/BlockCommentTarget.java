package cotest_tracker.comments;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.BlockComment;

public class BlockCommentTarget extends Target {
	private BlockComment raw;
	public BlockComment getRaw() {
		return raw;
	}
	public BlockCommentTarget(int begin, int end, String content, BlockComment comment) {
		super(begin, end, content);
		this.raw = comment;
	}
	@Override
	public ClassOrInterfaceDeclaration getParentNode() {
		// TODO Auto-generated method stub
		return (ClassOrInterfaceDeclaration) raw.getParentNode().get();
	}
}

package cotest_tracker.comments;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public abstract class Target {
	protected int begin;
	protected int end;
	protected String content;
	public int getBegin() {
		return begin;
	}
	public int getEnd() {
		return end;
	}
	public String getContent() {
		return content;
	}
	public Target(int begin, int end, String content) {
		this.begin = begin;
		this.end = end;
		this.content = content;
	}
	public abstract ClassOrInterfaceDeclaration getParentNode();
}

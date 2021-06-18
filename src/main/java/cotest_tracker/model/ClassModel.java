package cotest_tracker.model;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

public class ClassModel extends Model {

	private ClassOrInterfaceDeclaration klass;
	@Override
	public int getStartLine() {
		return this.klass.getBegin().get().line;
	}

	@Override
	public int getEndLine() {
		return this.klass.getEnd().get().line;
	}

	public ClassModel(ClassOrInterfaceDeclaration klass, List<AnnotationExpr> COAnnotations, String filePath, boolean junit3, boolean junit4, boolean testng) {
		super(filePath, COAnnotations, junit3, junit4, testng);
		this.klass = klass;
	}

	@Override
	public String getFullyQualifiedName() {
		return klass.getFullyQualifiedName().get();
	}

	@Override
	public List<AnnotationExpr> getActiveAnnotations() {
		return klass.getAnnotations();
	}

	@Override
	public String getContent() {
		return this.klass.toString();
	}

	@Override
	public String getStrippedContent() {
		ClassOrInterfaceDeclaration clone = this.klass.clone();
		List<Node> tbr = new ArrayList<Node>();
		tbr.addAll(clone.getAllContainedComments());
		tbr.addAll(clone.getAnnotations());
		for (Node n : tbr) {
			clone.remove(n);
		}
		return clone.toString();
	}
}

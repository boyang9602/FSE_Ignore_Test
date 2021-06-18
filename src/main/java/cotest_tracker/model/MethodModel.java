package cotest_tracker.model;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import cotest_tracker.Utils;

public class MethodModel extends Model {

	private MethodDeclaration decl;
	private String fullyQualifiedName = null;
	
	public MethodModel(MethodDeclaration decl, List<AnnotationExpr> annos, String filePath, boolean junit3, boolean junit4, boolean testng) {
		super(filePath, annos, junit3, junit4, testng);
		this.decl = decl;
	}
	
	@Override
	public String getFullyQualifiedName() {
		if (fullyQualifiedName == null) {
			this.fullyQualifiedName = ((ClassOrInterfaceDeclaration)decl.getParentNode().get()).getFullyQualifiedName().get() + "::" + Utils.MethodDeclaration2String(decl, false);
		}
		return fullyQualifiedName;
	}

	@Override
	public List<AnnotationExpr> getActiveAnnotations() {
		return this.decl.getAnnotations();
	}

	@Override
	public int getStartLine() {
		return this.decl.getBegin().get().line;
	}

	@Override
	public int getEndLine() {
		return this.decl.getEnd().get().line;
	}

	@Override
	public String getContent() {
		return this.decl.toString();
	}

	@Override
	public String getStrippedContent() {
		MethodDeclaration clone = this.decl.clone();
		List<Node> tbr = new ArrayList<Node>();
		tbr.addAll(clone.getAllContainedComments());
		tbr.addAll(clone.getAnnotations());
		for (Node n : tbr) {
			clone.remove(n);
		}
		return clone.toString();
	}

}

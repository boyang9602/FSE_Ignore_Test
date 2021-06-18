package cotest_tracker.model;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import cotest_tracker.Utils;

public class COTestModel extends Model {
	
	@FunctionalInterface
	public interface Factory {
		COTestModel create(MethodDeclaration decl, ClassOrInterfaceDeclaration klass, String content, int startLine, int endLine);
	}
	
	private String content;
	private ClassOrInterfaceDeclaration klass;
	private MethodDeclaration decl;
	private int startLine;
	private int endLine;
	private String fullyQualifiedName;

	public String getContent() {
		return content;
	}

	public ClassOrInterfaceDeclaration getKlass() {
		return klass;
	}

	public MethodDeclaration getDecl() {
		return decl;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}
	
	@Override
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	@Override
	public List<AnnotationExpr> getActiveAnnotations() {
		return null;
	}
	
	@Override
	public String getStrippedContent() {
		return "";
	}
	
	public COTestModel(MethodDeclaration decl, ClassOrInterfaceDeclaration klass, String content, int startLine, int endLine, String filePath, boolean junit3, boolean junit4, boolean testng) {
		super(filePath, decl.getAnnotations(), junit3, junit4, testng);
		this.decl = decl;
		this.fullyQualifiedName = klass.getFullyQualifiedName().get() + "::" + Utils.MethodDeclaration2String(decl, false);
		this.content = content;
		this.startLine = startLine;
		this.endLine = endLine;
	}
}

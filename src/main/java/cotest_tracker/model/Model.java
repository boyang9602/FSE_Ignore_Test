package cotest_tracker.model;

import java.util.List;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

public abstract class Model {
	protected List<AnnotationExpr> COAnnotations;
	protected String filePath;
	public abstract String getFullyQualifiedName();
	public abstract String getContent();
	public List<AnnotationExpr> getCOAnnotations() {
		return this.COAnnotations;
	}
	
	// if imported testng.Test/junit.framework.Test/org.junit.Test
	protected boolean testng;
	protected boolean junit3;
	protected boolean junit4;
	public boolean isTestng() {
		return testng;
	}
	public boolean isJunit3() {
		return junit3;
	}
	public boolean isJunit4() {
		return junit4;
	}

	public Model (String filePath, List<AnnotationExpr> COAnnotations, boolean junit3, boolean junit4, boolean testng) {
		this.filePath = filePath;
		this.COAnnotations = COAnnotations;
		this.junit3 = junit3;
		this.junit4 = junit4;
		this.testng = testng;
	}
	
	public abstract List<AnnotationExpr> getActiveAnnotations();
	
	//active mean non-commented-out
	public AnnotationExpr getActiveTest() {
		if (this.getActiveAnnotations() == null) {
			return null;
		}
		for (AnnotationExpr anno : this.getActiveAnnotations()) {
			if (anno.getNameAsString().equals("Test")) {
				return anno;
			}
		}
		return null;
	}
	
	public static boolean isADisabledTest(AnnotationExpr anno) {
		if (anno == null) return false;
		if (anno.getNameAsString().equals("Test") && anno instanceof NormalAnnotationExpr) {
			NormalAnnotationExpr nAnno = (NormalAnnotationExpr) anno;
			for (MemberValuePair pair : nAnno.getPairs()) {
				if (pair.getNameAsString().equals("enabled") && pair.getValue().isBooleanLiteralExpr()) {
					return pair.getValue().asBooleanLiteralExpr().getValue() == false;
				}
			}
		}
		return false;
	}
	
	public AnnotationExpr getActiveIgnore() {
		if (this.getActiveAnnotations() == null) {
			return null;
		}
		for (AnnotationExpr anno : this.getActiveAnnotations()) {
			if (anno.getNameAsString().equals("Ignore")) {
				return anno;
			}
		}
		return null;
	}
	
	public AnnotationExpr getCOTest() {
		if (this.getCOAnnotations() == null) {
			return null;
		}
		for (AnnotationExpr anno : this.getCOAnnotations()) {
			if (anno.getNameAsString().equals("Test")) {
				return anno;
			}
		}
		return null;
	}
	
	public AnnotationExpr getCOIgnore() {
		if (this.getCOAnnotations() == null) {
			return null;
		}
		for (AnnotationExpr anno : this.getCOAnnotations()) {
			if (anno.getNameAsString().equals("Ignore")) {
				return anno;
			}
		}
		return null;
	}

	public String getFilePath() {
		return filePath;
	}

	public abstract int getStartLine();
	public abstract int getEndLine();
	public abstract String getStrippedContent();
}

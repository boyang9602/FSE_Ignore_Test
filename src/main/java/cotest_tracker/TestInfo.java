package cotest_tracker;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.javaparser.ast.expr.AnnotationExpr;

import cotest_tracker.model.Model;

public class TestInfo {
	private RevCommit commit;
	private Status status;
	private Model model;
	public String getFullyQualifiedName() {
		return this.model.getFullyQualifiedName();
	}
	public RevCommit getCommit() {
		return commit;
	}
	public Model getModel() {
		return model;
	}
	public Status getStatus() {
		return status;
	}

	public TestInfo(Model model, RevCommit commit, Status status) {
		this.model = model;
		this.commit = commit;
		this.status = status;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("commitId", this.commit.getName());
		json.put("time", this.commit.getCommitTime());
		json.put("author", this.commit.getAuthorIdent().getEmailAddress());
		json.put("name", this.getFullyQualifiedName());
		json.put("status", this.status);
		json.put("filePath", this.model.getFilePath());
		json.put("strippedContent", this.model.getStrippedContent());
		json.put("content", this.model.getContent());
		json.put("activeTest", this.model.getActiveTest());
		json.put("COTest", this.model.getCOTest());
		json.put("activeIgnore", this.model.getActiveIgnore());
		json.put("COIgnore", this.model.getCOIgnore());
		if (json.has("activeTest")) {
			json.put("isActiveTestDisabled", Model.isADisabledTest(this.model.getActiveTest()));
		}
		if (json.has("COTest")) {
			json.put("isCOTestDisabled", Model.isADisabledTest(this.model.getCOTest()));
		}
		json.put("COAnnotations", this.toJSONArray(this.model.getCOAnnotations()));
		json.put("activeAnnotations", this.toJSONArray(this.model.getActiveAnnotations()));
		json.put("junit3", this.model.isJunit3());
		json.put("junit4", this.model.isJunit4());
		json.put("testng", this.model.isTestng());
		return json;
	}
	
	private JSONArray toJSONArray(List<AnnotationExpr> annotations) {
		JSONArray arr = new JSONArray();
		if (annotations == null) {
			return arr;
		}
		for (AnnotationExpr anno : annotations) {
			arr.put(anno.toString());
		}
		return arr;
	}
	
	enum Status {
		NEW,
		CO,
		ACT,
		DEL,
		NEW_CO,
	}
}
package cotest_tracker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.util.GitServiceImpl;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;

import cotest_tracker.TestInfo.Status;
import cotest_tracker.model.COTestModel;
import cotest_tracker.model.COTestModel.Factory;
import cotest_tracker.model.ClassModel;
import cotest_tracker.model.MethodModel;
import cotest_tracker.model.Model;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import info.debatty.java.stringsimilarity.Levenshtein;

public class ChangeHandler {

	private Logger logger = Logger.getLogger(this.getClass());
	private RevCommit commit;
	private RevCommit parent;
	private Repository repo;

	private List<String> filePathsBefore = new ArrayList<String>();
	private List<String> filePathsCurrent = new ArrayList<String>();
	private Map<String, String> renamedFilesHint = new HashMap<String, String>();

	private Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
	private Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
	private Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
	private Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();

	private Map<String, String> nameChangeMap = new HashMap<String, String>();
	private Map<String, List<TestInfo>> chain;
	boolean skipRMiner = false;
	
	public Map<String, String> getNameChangeMap() {
		return this.nameChangeMap;
	}
	
	public ChangeHandler(Repository repo, String commitId, Map<String, List<TestInfo>> chain, Map<String, String> nameChangeMap) throws Exception {
		this(repo, repo.parseCommit(ObjectId.fromString(commitId)), chain, nameChangeMap);
	}
	
	public ChangeHandler(Repository repo, RevCommit commit, Map<String, List<TestInfo>> chain, Map<String, String> nameChangeMap) throws Exception {
		this(repo, commit, chain);
		this.nameChangeMap = nameChangeMap;
		this.skipRMiner = true;
	}
	
	public ChangeHandler(Repository repo, String commitId, Map<String, List<TestInfo>> chain) throws Exception {
		this(repo, repo.parseCommit(ObjectId.fromString(commitId)), chain);
	}
	
	public ChangeHandler(Repository repo, RevCommit commit, Map<String, List<TestInfo>> chain) throws Exception {
		logger.info(String.format("Handling %s ...", commit.getName()));
		this.commit = commit;
		this.parent = repo.parseCommit(commit.getParent(0));
		this.repo = repo;
		GitService gitService = new GitServiceImpl();
		gitService.fileTreeDiff(repo, commit, this.filePathsBefore, this.filePathsCurrent, this.renamedFilesHint);
		JGitUtils.populateFileContents(this.repo, this.parent, this.filePathsBefore, this.fileContentsBefore, this.repositoryDirectoriesBefore);
		JGitUtils.populateFileContents(this.repo, this.commit, this.filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
		this.chain = chain;
	}
	
	@SuppressWarnings("rawtypes")
	private void handle(Map<String, Model> cotests, Map<String, Model> classes, Map<String, Model> methods, boolean before) {
		// detect annotations changes
		// detect annotations changes in comments
		// detect tests cases in comments
		List<String> paths = before ? this.filePathsBefore : this.filePathsCurrent;
		Map<String, String> contents = before ? this.fileContentsBefore : this.fileContentsCurrent;
		for (String path : paths) {
			CompilationUnit cu = null;
			try {
				String fileContent = contents.get(path);
				if (fileContent == null) {
					logger.error(String.format("Did not get file %s at commit: %s, before: %b", path, this.commit.getName(), before));
				}
				cu = StaticJavaParser.parse(fileContent);
			} catch (ParseProblemException e) {
				logger.warn(String.format("Commit: %s, Parse error for %s in %s code", this.commit.getName(), path, before ? "old" : "new"), e);
				continue;
			}
			/*
			 * 1. all the classes with @Test/@Ignore or commented out @Test/@Ignore
			 * 2. all the methods with @Test/@Ignore or commented out @Test/@Ignore
			 * 3. all the commented out test methods(commented out methods with a commented out @Test/@Ignore). 
			 */
			List<ImportDeclaration> imptDecls = cu.findAll(ImportDeclaration.class);
			int lastImportLine = 0;
			boolean junit3 = false;
			boolean junit4 = false;
			boolean testng = false;
			for (ImportDeclaration impt : imptDecls) {
				if (impt.getEnd().get().line > lastImportLine) {
					lastImportLine = impt.getEnd().get().line;
				}
				if (impt.isAsterisk()) {
					if (impt.getNameAsString().equals("org.junit")) {
						junit4 = true;
					} else if (impt.getNameAsString().equals("junit.framework")) {
						junit3 = true;
					} else if (impt.getNameAsString().equals("org.testng.annotations")) {
						testng = true;
					}
				} else {
					if (impt.getNameAsString().equals("org.junit.Test")) {
						junit4 = true;
					} else if (impt.getNameAsString().equals("junit.framework.TestCase")) {
						junit3 = true;
					} else if (impt.getNameAsString().equals("org.testng.annotations.Test")) {
						testng = true;
					}
				}
			}
			int packageLine = 0;
			if (cu.getPackageDeclaration().isPresent()) {
				packageLine = cu.getPackageDeclaration().get().getEnd().get().line;
			}
			List<BodyDeclaration> bodyDecls = cu.findAll(BodyDeclaration.class);
			List<Comment> comments = new LinkedList<Comment>();
			for (Comment c : cu.getAllComments()) {
				if (!c.isJavadocComment() && c.getBegin().get().line > 5) {
					comments.add(c);
				}
			}
			
			final boolean b1 = junit3;
			final boolean b2 = junit4;
			final boolean b3 = testng;
			Factory fac = (decl, klass, content, startLine, endLine) ->{
				return new COTestModel(decl, klass, content, startLine, endLine, path, b1, b2, b3);
			};
			// detect CO tests and their ranges
			List<Model> currCOTests = Utils.detectCOTestMethods(comments, fac);
			for (Model ctm : currCOTests) {
				Model prev = cotests.put(ctm.getFullyQualifiedName(), ctm);
				if (prev != null) {
					logger.warn(String.format("Duplicated CO tests detected, name: %s, commit:%s, path: %s, before: %b", 
							prev.getFullyQualifiedName(), this.commit.getName(), path, before));
				}
			}
			
			// detect classes
			for (ClassOrInterfaceDeclaration klass : cu.findAll(ClassOrInterfaceDeclaration.class, c -> !(c.getParentNode().get() instanceof LocalClassDeclarationStmt))) {
				int minLine = lastImportLine > packageLine ? lastImportLine : packageLine;
				int lastBodyDeclAbove = Utils.getLastBodyDeclarationLineAbove(klass, bodyDecls);
				minLine = minLine > lastBodyDeclAbove ? minLine : lastBodyDeclAbove;
				int lastCOTestAbove = Utils.getLastCOTestLineAbove(klass, currCOTests);
				minLine = minLine > lastCOTestAbove ? minLine : lastCOTestAbove;
				List<AnnotationExpr> COAnnotations = Utils.getCOAnnotations(klass, comments, minLine, "Test", "Ignore");

				Model cm = new ClassModel(klass, COAnnotations, path, junit3, junit4, testng);
				Model prev = classes.put(cm.getFullyQualifiedName(), cm);
				if (prev != null) {
					logger.warn(String.format("Duplicated classes detected, name: %s, commit:%s, path: %s, before: %b", 
							prev.getFullyQualifiedName(), this.commit.getName(), path, before));
				}
			}
			
			// detect methods
			for (MethodDeclaration m : cu.findAll(MethodDeclaration.class)) {
				int lastBodyDeclAbove = Utils.getLastBodyDeclarationLineAbove(m, bodyDecls);
				int lastCOTestAbove = Utils.getLastCOTestLineAbove(m, currCOTests);
				int minLine = lastBodyDeclAbove > lastCOTestAbove ? lastBodyDeclAbove : lastCOTestAbove;
				Node parentNode = m.getParentNode().get();
				if (!(parentNode instanceof ClassOrInterfaceDeclaration) || parentNode.getParentNode().get() instanceof LocalClassDeclarationStmt) {
					continue;
				}
				int parentStartLine = ((ClassOrInterfaceDeclaration)parentNode).getName().getBegin().get().line;
				minLine = minLine > parentStartLine ? minLine : parentStartLine;
				List<AnnotationExpr> COAnnotations = Utils.getCOAnnotations(m, comments, minLine, "Test", "Ignore");
				Model mm = new MethodModel(m, COAnnotations, path, junit3, junit4, testng);
				Model prev = methods.put(mm.getFullyQualifiedName(), mm);
				if (prev != null) {
					logger.warn(String.format("Duplicated methods detected, name: %s, commit:%s, path: %s, before: %b", 
							prev.getFullyQualifiedName(), this.commit.getName(), path, before));
				}
			}
		}
	}

	private List<TestInfo> findOrCreate(String k, Map<String, Model> modelsBefore) {
		List<TestInfo> list = this.chain.get(k);
		if (list == null) {
			list = new LinkedList<TestInfo>();
			this.chain.put(k, list);
		}

		// init the list
		if (list.isEmpty()) {
			TestInfo testBefore = new TestInfo(modelsBefore.get(k), this.commit, modelsBefore.get(k) instanceof COTestModel ? Status.CO : Status.ACT);
			list.add(testBefore);
		}
		return list;
	}
	
	private boolean isTest(Model model) {
		boolean isTest = false;
		for (AnnotationExpr a : model.getActiveAnnotations()) {
			isTest |= a.getNameAsString().equals("Test") || a.getNameAsString().equals("Ignore");
		}
		for (AnnotationExpr a : model.getCOAnnotations()) {
			isTest |= a.getNameAsString().equals("Test") || a.getNameAsString().equals("Ignore");
		}
		return isTest;
	}
	
	public void handle() throws Exception {
		if (!skipRMiner) {
			this.buildRenameMap();
		}
		Map<String, Model> cotestsBefore = new HashMap<String, Model>();
		Map<String, Model> classesBefore = new HashMap<String, Model>();
		Map<String, Model> methodsBefore = new HashMap<String, Model>();
		Map<String, Model> cotestsAfter = new HashMap<String, Model>();
		Map<String, Model> classesAfter = new HashMap<String, Model>();
		Map<String, Model> methodsAfter = new HashMap<String, Model>();
		handle(cotestsBefore, classesBefore, methodsBefore, true);
		handle(cotestsAfter, classesAfter, methodsAfter, false);
		
		Set<String> handled = new HashSet<String>();
		for (String k : cotestsBefore.keySet()) {
			if (cotestsAfter.containsKey(k)) {
				// pass
				handled.add(k);
			} else if (methodsAfter.containsKey(k)) {
				// uncommented
				handled.add(k);
				TestInfo test = new TestInfo(methodsAfter.get(k), this.commit, Status.ACT);
				List<TestInfo> list = this.findOrCreate(k, cotestsBefore);
				list.add(test);
			} else {
				String className = k.split("::")[0];
				if (this.nameChangeMap.containsKey(className)) {
					// changed name
					String newName = this.nameChangeMap.get(className) + k.substring(className.length());
					if (cotestsAfter.containsKey(newName)) {
						// move
						handled.add(newName);
						TestInfo test = new TestInfo(cotestsAfter.get(newName), this.commit, Status.CO);
						List<TestInfo> list = this.findOrCreate(k, cotestsBefore);
						list.add(test);
						this.chain.put(newName, list);
						this.chain.remove(k);
					} else if (methodsAfter.containsKey(newName)) {
						// move and uncomment
						handled.add(newName);
						TestInfo test = new TestInfo(methodsAfter.get(newName), this.commit, Status.ACT);
						List<TestInfo> list = this.findOrCreate(k, cotestsBefore);
						list.add(test);
						this.chain.put(newName, list);
						this.chain.remove(k);
					} else {
						// deleted
						TestInfo test = new TestInfo(cotestsBefore.get(k), this.commit, Status.DEL);
						List<TestInfo> list = this.findOrCreate(k, cotestsBefore);
						list.add(test);
					}
				} else {
					// deleted
					TestInfo test = new TestInfo(cotestsBefore.get(k), this.commit, Status.DEL);
					List<TestInfo> list = this.findOrCreate(k, cotestsBefore);
					list.add(test);
				}
			}
		}
		
		for (String k : classesBefore.keySet()) {
			if (classesAfter.containsKey(k)) {
				// check if annotation changed
				handled.add(k);
				if (!isTest(classesBefore.get(k)) && !isTest(classesAfter.get(k))) {
					continue;
				}
				if (Utils.equals(classesBefore.get(k).getCOAnnotations(), classesAfter.get(k).getCOAnnotations()) && 
						Utils.equals(classesBefore.get(k).getActiveAnnotations(), classesAfter.get(k).getActiveAnnotations())) {
					// pass
				} else {
					TestInfo test = new TestInfo(classesAfter.get(k), this.commit, Status.ACT);
					List<TestInfo> list = this.findOrCreate(k, classesBefore);
					list.add(test);
				}
			} else if (this.nameChangeMap.containsKey(k)) {
				// renamed
				handled.add(this.nameChangeMap.get(k));
				if (!classesAfter.containsKey(this.nameChangeMap.get(k))) {
					logger.warn(String.format("class changed name from %s to %s, but the later one does not exist in classesAfter.", k, this.nameChangeMap.get(k)));
					continue;
				}
				if (!isTest(classesBefore.get(k)) && !isTest(classesAfter.get(this.nameChangeMap.get(k)))) {
					continue;
				}
				TestInfo test = new TestInfo(classesAfter.get(this.nameChangeMap.get(k)), this.commit, Status.ACT);
				List<TestInfo> list = this.findOrCreate(k, classesBefore);
				list.add(test);
				this.chain.put(test.getFullyQualifiedName(), list);
				this.chain.remove(k);
			} else {
				// deleted
				if (!isTest(classesBefore.get(k))) {
					continue;
				}
				TestInfo test = new TestInfo(classesBefore.get(k), this.commit, Status.DEL);
				List<TestInfo> list = this.findOrCreate(k, classesBefore);
				list.add(test);
			}
		}
		
		for (String k : methodsBefore.keySet()) {
			if (methodsAfter.containsKey(k)) {
				// check if annotation changes
				handled.add(k);
				if (!isTest(methodsBefore.get(k)) && !isTest(methodsAfter.get(k))) {
					continue;
				}
				if (Utils.equals(methodsBefore.get(k).getCOAnnotations(), methodsAfter.get(k).getCOAnnotations()) &&
						Utils.equals(methodsBefore.get(k).getActiveAnnotations(), methodsAfter.get(k).getActiveAnnotations())) {
					// pass
				} else {
					TestInfo test = new TestInfo(methodsAfter.get(k), this.commit, Status.ACT);
					List<TestInfo> list = this.findOrCreate(k, methodsBefore);
					list.add(test);
				}
			} else if (cotestsAfter.containsKey(k)) {
				// commented out
				handled.add(k);
				TestInfo test = new TestInfo(cotestsAfter.get(k), this.commit, Status.CO);
				List<TestInfo> list = this.findOrCreate(k, methodsBefore);
				list.add(test);
			} else if (this.nameChangeMap.containsKey(k)) {
				// changed name
				handled.add(this.nameChangeMap.get(k));
				if (!methodsAfter.containsKey(this.nameChangeMap.get(k))) {
					logger.warn(String.format("Method change name from %s to %s, but the later one does not exist in methodsAfter", k, this.nameChangeMap.get(k)));
					continue;
				}
				if (!isTest(methodsBefore.get(k)) && !isTest(methodsAfter.get(this.nameChangeMap.get(k)))) {
					continue;
				}
				TestInfo test = new TestInfo(methodsAfter.get(this.nameChangeMap.get(k)), this.commit, Status.ACT);
				List<TestInfo> list = this.findOrCreate(k, methodsBefore);
				list.add(test);
				this.chain.put(test.getFullyQualifiedName(), list);
				this.chain.remove(k);
			} else {
				String className = k.split("::")[0];
				if (this.nameChangeMap.containsKey(className)) {
					// changed name
					String newName = this.nameChangeMap.get(className) + k.substring(className.length());
					if (cotestsAfter.containsKey(newName)) {
						// move and commented out
						handled.add(newName);
						TestInfo test = new TestInfo(cotestsAfter.get(newName), this.commit, Status.CO);
						List<TestInfo> list = this.findOrCreate(k, methodsBefore);
						list.add(test);
						this.chain.put(newName, list);
						this.chain.remove(k);
					} else if (methodsAfter.containsKey(newName)) {
						// class changed name
						handled.add(newName);
						if (!isTest(methodsBefore.get(k)) && !isTest(methodsAfter.get(newName))) {
							continue;
						}
						TestInfo test = new TestInfo(methodsAfter.get(newName), this.commit, Status.ACT);
						List<TestInfo> list = this.findOrCreate(k, methodsBefore);
						list.add(test);
						this.chain.put(newName, list);
						this.chain.remove(k);
					} else {
						// deleted
						if (!isTest(methodsBefore.get(k))) {
							continue;
						}
						TestInfo test = new TestInfo(methodsBefore.get(k), this.commit, Status.DEL);
						List<TestInfo> list = this.findOrCreate(k, methodsBefore);
						list.add(test);
					}
				} else {
					// deleted
					if (!isTest(methodsBefore.get(k))) {
						continue;
					}
					TestInfo test = new TestInfo(methodsBefore.get(k), this.commit, Status.DEL);
					List<TestInfo> list = this.findOrCreate(k, methodsBefore);
					list.add(test);
				}
			}
		}
		
		for (String k : cotestsAfter.keySet()) {
			if (!handled.contains(k)) {
				// newly added CO test
				TestInfo test = new TestInfo(cotestsAfter.get(k), this.commit, Status.NEW_CO);
				List<TestInfo> list = new ArrayList<TestInfo>();
				list.add(test);
				List<TestInfo> prev = this.chain.put(k, list);
				if (prev != null) {
					logger.warn(String.format("Duplicated new tests, prev commit: %s, current commit: %s, name: %s", 
							prev.get(0).getCommit().getName(), test.getCommit().getName(), test.getFullyQualifiedName()));
				}
			}
		}
		
		for (String k : classesAfter.keySet()) {
			if (!handled.contains(k) && isTest(classesAfter.get(k))) {
				// newly added class
				TestInfo test = new TestInfo(classesAfter.get(k), this.commit, Status.NEW);
				List<TestInfo> list = new ArrayList<TestInfo>();
				list.add(test);
				List<TestInfo> prev = this.chain.put(k, list);
				if (prev != null) {
					logger.warn(String.format("Duplicated new tests, prev commit: %s, current commit: %s, name: %s", 
							prev.get(0).getCommit().getName(), test.getCommit().getName(), test.getFullyQualifiedName()));
				}
			}
		}
		
		for (String k : methodsAfter.keySet()) {
			if (!handled.contains(k) && isTest(methodsAfter.get(k))) {
				// newly added method
				TestInfo test = new TestInfo(methodsAfter.get(k), this.commit, Status.NEW);
				List<TestInfo> list = new ArrayList<TestInfo>();
				list.add(test);
				List<TestInfo> prev = this.chain.put(k, list);
				if (prev != null) {
					logger.warn(String.format("Duplicated new tests, prev commit: %s, current commit: %s, name: %s", 
							prev.get(0).getCommit().getName(), test.getCommit().getName(), test.getFullyQualifiedName()));
				}
			}
		}
	}
	
	private void buildRenameMap() throws Exception {
		List<Refactoring> refactorings = this.detectRefactorings();
		if (refactorings == null) {
			return;
		}
		List<Refactoring> unhandledRefactorings = new LinkedList<Refactoring>();
		Levenshtein l = new Levenshtein();
		for (Refactoring refactoring : refactorings) {
			switch (refactoring.getRefactoringType()) {
			case RENAME_CLASS:// update the class name
			case MOVE_CLASS:
			case MOVE_RENAME_CLASS:
				Class<? extends Refactoring> klazz = refactoring.getClass();
				Field oldClassField = klazz.getDeclaredField("originalClass");
				Field newClassField = refactoring.getRefactoringType() == RefactoringType.MOVE_CLASS ? klazz.getDeclaredField("movedClass") : klazz.getDeclaredField("renamedClass");
				oldClassField.setAccessible(true);
				newClassField.setAccessible(true);
				UMLClass oldClass = (UMLClass) oldClassField.get(refactoring);
				UMLClass newClass = (UMLClass) newClassField.get(refactoring);
				this.nameChangeMap.put(oldClass.getName(), newClass.getName());
				break;

			case RENAME_PACKAGE:
				// should have been handled in MOVE_CLASS
				break;
				
			case RENAME_METHOD:
				RenameOperationRefactoring renameMethodRef = (RenameOperationRefactoring) refactoring;
				UMLOperation origOperation = renameMethodRef.getOriginalOperation();
				UMLOperation renamedOperation = renameMethodRef.getRenamedOperation();
				String origName = Utils.UMLOperation2String(origOperation);
				String renamedName = Utils.UMLOperation2String(renamedOperation);
				String prev = this.nameChangeMap.put(origName, renamedName);
				if (prev != null && !renamedName.equals(prev)) {
					if (l.distance(origName, renamedName) > l.distance(origName, prev)) {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), origName, prev, renamedName, prev));
						this.nameChangeMap.put(origName, prev);
					} else {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), origName, prev, renamedName, renamedName));
					}
				}
				break;

			case MOVE_OPERATION:
			case PULL_UP_OPERATION:
			case PUSH_DOWN_OPERATION:
			case MOVE_AND_RENAME_OPERATION:
				MoveOperationRefactoring moveMethodRef = (MoveOperationRefactoring) refactoring;
				UMLOperation origOp = moveMethodRef.getOriginalOperation();
				UMLOperation movedOp = moveMethodRef.getMovedOperation();
				String origMethodName = Utils.UMLOperation2String(origOp);
				String movedMethodName = Utils.UMLOperation2String(movedOp);
				String prevNewName = this.nameChangeMap.put(origMethodName, movedMethodName);
				if (prevNewName != null && !movedMethodName.equals(prevNewName)) {
					if (l.distance(origMethodName, movedMethodName) > l.distance(origMethodName, prevNewName)) {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), origMethodName, prevNewName, movedMethodName, prevNewName));
						this.nameChangeMap.put(origMethodName, prevNewName);
					} else {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), origMethodName, prevNewName, movedMethodName, origMethodName));
					}
				}
				break;

			case ADD_PARAMETER:
			case REMOVE_PARAMETER:
			case REORDER_PARAMETER:
			case PARAMETERIZE_VARIABLE:
			case MERGE_PARAMETER:
			case SPLIT_PARAMETER:
			case CHANGE_PARAMETER_TYPE:
				Class<? extends Refactoring> klass = refactoring.getClass();
				Field f1 = klass.getDeclaredField("operationBefore");
				Field f2 = klass.getDeclaredField("operationAfter");
				f1.setAccessible(true);
				f2.setAccessible(true);
				UMLOperation operationBefore = (UMLOperation) f1.get(refactoring);
				UMLOperation operationAfter = (UMLOperation) f2.get(refactoring);
				String oldName = Utils.UMLOperation2String(operationBefore);
				String newName = Utils.UMLOperation2String(operationAfter);
				String previous = this.nameChangeMap.put(oldName, newName);
				if (previous != null && !newName.equals(previous)) {
					if (l.distance(oldName, newName) > l.distance(oldName, previous)) {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), oldName, previous, newName, previous));
						this.nameChangeMap.put(oldName, previous);
					} else {
						logger.warn(String.format("Commit: %s, refactoring: %s, original name: %s, prev new name: %s, curr new name: %s, finally use %s", 
								this.commit.getName(), refactoring.getRefactoringType(), oldName, previous, newName, newName));
					}
				}
				break;

			case EXTRACT_SUPERCLASS:
			case EXTRACT_SUBCLASS:
			case EXTRACT_CLASS:
			case EXTRACT_OPERATION:
			case EXTRACT_AND_MOVE_OPERATION:
			case INLINE_OPERATION:
			case MOVE_AND_INLINE_OPERATION:
				unhandledRefactorings.add(refactoring);
				break;
			default:
				break;
			}
		}
	}
	
	// from RefactoringMiner
	private UMLModel createModel(Map<String, String> fileContents, Set<String> repositoryDirectories) throws Exception {
		return new UMLModelASTReader(fileContents, repositoryDirectories).getUmlModel();
	}
	
	// from RefactoringMiner
	private List<Refactoring> detectRefactorings() throws Exception {
		if (this.filePathsBefore.isEmpty() || this.filePathsCurrent.isEmpty()) {
			return null;
		}
		UMLModel parentUMLModel = createModel(this.fileContentsBefore, this.repositoryDirectoriesBefore);
		UMLModel currentUMLModel = createModel(this.fileContentsCurrent, this.repositoryDirectoriesCurrent);

		List<Refactoring> refactoringsAtRevision = parentUMLModel.diff(currentUMLModel, this.renamedFilesHint).getRefactorings();
		return refactoringsAtRevision;
	}
}

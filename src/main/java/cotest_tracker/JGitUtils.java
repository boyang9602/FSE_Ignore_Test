package cotest_tracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

public class JGitUtils {
	static String LOCAL_PREFIX = "refs/heads/";
	static Logger logger = Logger.getLogger(Utils.class);
	public static Repository createRepo(String folder) throws IOException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repo = builder.setGitDir(new File(folder, ".git")).readEnvironment() // scan environment GIT_*
																							// variables
				.findGitDir() // scan up the file system tree
				.build();
		return repo;
	}
	
	public static Ref findBranch(Repository repo, String branch) throws IOException {
		Ref ref = null;
		for (Ref tmp : repo.getRefDatabase().getRefs()) {
			if (tmp.getName().equals(LOCAL_PREFIX + branch)) {
				ref = tmp;
			}
		}
		if (ref == null) {
			throw new RuntimeException("No such branch: " + branch);
		}
		return ref;
	}
	
	public static List<DiffEntry> getDiffEntryList(Repository repo, String commitId) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		RevCommit revCommit = repo.parseCommit(ObjectId.fromString(commitId));
		RevCommit parent = repo.parseCommit(revCommit.getParent(0));
		return getDiffEntryList(repo, parent.getTree(), revCommit.getTree());
	}
	/*
	 * compare a commit with its parent
	 */
	public static List<DiffEntry> getDiffEntryList(Repository repo, ObjectId parentTree, ObjectId childTree) throws IOException {
		DiffFormatter fmtter = getDiffFormatter(repo);
		List<DiffEntry> diffEntries = fmtter.scan(parentTree, childTree);
//		for (DiffEntry entry : diffEntries) {
//			FileHeader fh = fmtter.toFileHeader(entry);
//			EditList elist = fh.toEditList();
//			for (Edit e : elist) {
//				System.out.println(e.toString());
//			}
//		}
		fmtter.close();
		return diffEntries;
	}
	
	// from RefactoringMiner
	public static void populateFileContents(Repository repository, RevCommit commit,
			List<String> filePaths, Map<String, String> fileContents, Set<String> repositoryDirectories) throws Exception {
		RevTree parentTree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(parentTree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String pathString = treeWalk.getPathString();
				if(filePaths.contains(pathString)) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					StringWriter writer = new StringWriter();
					IOUtils.copy(loader.openStream(), writer);
					fileContents.put(pathString, writer.toString());
				}
				if(pathString.endsWith(".java") && pathString.contains("/")) {
					String directory = pathString.substring(0, pathString.lastIndexOf("/"));
					repositoryDirectories.add(directory);
					//include sub-directories
					String subDirectory = new String(directory);
					while(subDirectory.contains("/")) {
						subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
						repositoryDirectories.add(subDirectory);
					}
				}
			}
		}
	}
	
	public static DiffFormatter getDiffFormatter(Repository repo) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DiffFormatter fmtter = new DiffFormatter(baos);
		fmtter.setRepository(repo);
		fmtter.setPathFilter(PathSuffixFilter.create(".java"));
		fmtter.setDetectRenames(true);
		return fmtter;
	}
	
	public static String getFileContent(Repository repo, String commitId, String path, boolean parent) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		RevCommit revCommit = repo.parseCommit(ObjectId.fromString(commitId));
		if (parent) {
			revCommit = repo.parseCommit(revCommit.getParent(0));
		}
		
		RevTree tree = revCommit.getTree();
		TreeWalk tw = new TreeWalk(repo);
		tw.addTree(tree);
		tw.setRecursive(true);
		tw.setFilter(PathFilter.create(path));
		if (!tw.next()) {
			tw.close();
			if (parent) {
				throw new RuntimeException(String.format("Cannot find %s in commit %s's parent %s: ", path, commitId, revCommit.getId().toString()));
			} else {
				throw new RuntimeException(String.format("Cannot find %s in commit %s", path, commitId));
			}
		}
		ObjectLoader loader = repo.open(tw.getObjectId(0));
		tw.close();
		return new String(loader.getBytes());
	}
	
	public static void main(String[] args) throws IOException {
		Repository repo = createRepo(args[0]);
		@SuppressWarnings("unused")
		Ref branch = findBranch(repo, args[1]);
		RevWalk rw = new RevWalk(repo);
//		rw.setRevFilter(RevFilter.NO_MERGES);
//		rw.markStart(rw.parseCommit(branch.getObjectId()));
//		Iterator<RevCommit> it = rw.iterator();
//		int i = 0;
//		while (it.hasNext() && ++i < 10) {
//			RevCommit rc = it.next();
//			RevCommit prc = rc.getParent(0);
//			for(DiffEntry entry : getDiffEntryList(repo, prc.getTree(), rc.getTree())) {
//				System.out.println(entry);
//			}
//		}
		for(DiffEntry entry : getDiffEntryList(repo, "a996c8214baa4fa886071a24b8058ed1ca284686")) {
			System.out.println(entry);
		}
		rw.close();
	}
	
	// from RefactoringMiner
	public static Repository cloneIfNotExists(String projectPath, String cloneUrl/*, String branch*/) throws Exception {
		File folder = new File(projectPath);
		Repository repository;
		if (folder.exists()) {
			RepositoryBuilder builder = new RepositoryBuilder();
			repository = builder
					.setGitDir(new File(folder, ".git"))
					.readEnvironment()
					.findGitDir()
					.build();
			
			//logger.info("Project {} is already cloned, current branch is {}", cloneUrl, repository.getBranch());
			
		} else {
			logger.info(String.format("Cloning %s ...", cloneUrl));
			Git git = Git.cloneRepository()
					.setDirectory(folder)
					.setURI(cloneUrl)
					.setCloneAllBranches(true)
					.call();
			repository = git.getRepository();
			//logger.info("Done cloning {}, current branch is {}", cloneUrl, repository.getBranch());
		}
		return repository;
	}
}

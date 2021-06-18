/**
 * Usage of jgit refers to GitServiceImpl of RefactoringMiner.
 */
package cotest_tracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.refactoringminer.api.RefactoringType;

public class COTTracker {
	static Logger logger = Logger.getLogger(COTTracker.class);

	public static CommandLine getCommandLine(String[] args) throws ParseException {
		Options opts = new Options();
		opts.addRequiredOption("r", "repo", true, "the path of the git repository of the studied system");
		opts.addRequiredOption("b", "branch", true, "the branch of the repo for the walker to start to traverse");
		opts.addOption("s", "start-time", true, "the start time of the studied duration, format should be yyyy-mm-dd; if not set, it will start from the first commit");
		opts.addOption("e", "end-time", true, "the end time of the studued duration, format should be yyyy-mm-dd; if not set, it will end at the last commit");
		opts.addRequiredOption("o", "output", true, "output file name, if it has a folder, the folder should exist");
		opts.addOption("t", "timeout", true, "max execution time (in seconds) for each commit, default is 3600 seconds");
		opts.addOption("c", "cache", true, "the cache file (json) of detected rename practices by the refactoring miner, the cache could be used to save time for further re-execution");
		opts.addOption("m", "commit", true, "only analyze the commit");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(opts, args);
		return cmd;
	}
	public static void main(String[] args) throws Exception {
		CommandLine cmd = getCommandLine(args);
		Repository repo = JGitUtils.createRepo(cmd.getOptionValue("r"));
		Ref ref = JGitUtils.findBranch(repo, cmd.getOptionValue("b"));

		long start = 0;
		if (cmd.hasOption("s")) {
			start = Timestamp.valueOf(cmd.getOptionValue("s") + " 00:00:00").getTime() / 1000;
		}
		long end = 0;
		if (cmd.hasOption("e")) {
			end = Timestamp.valueOf(cmd.getOptionValue("e") + " 00:00:00").getTime() / 1000;
		}
		// iterate all commits of the specified branch
		RevWalk walk = new RevWalk(repo);
		walk.setRevFilter(RevFilter.NO_MERGES);
		walk.markStart(walk.parseCommit(ref.getObjectId()));
		walk.sort(RevSort.TOPO);
		walk.sort(RevSort.REVERSE);
		Iterator<RevCommit> it = walk.iterator();
		Map<String, List<TestInfo>> chain = new HashMap<String, List<TestInfo>>();
		Map<String, Map<String, String>> renameMap = new HashMap<String, Map<String, String>>();
		boolean cacheExist = false;
		if (cmd.hasOption("c")) {
			File cacheFile = new File(cmd.getOptionValue("c"));
			if (cacheFile.exists()) {
				JSONObject cache = new JSONObject(new JSONTokener(new FileInputStream(cacheFile)));
				for (String k : cache.keySet()) {
					JSONObject single = cache.getJSONObject(k);
					Map<String, String> tmp = new HashMap<String, String>();
					for (String k2 : single.keySet()) {
						tmp.put(k2, single.getString(k2));
					}
					renameMap.put(k, tmp);
				}
				cacheExist = true;
			}
		}
		while(it.hasNext()) {
			RevCommit next = it.next();
			if (start != 0 && next.getCommitTime() <= start) {
				continue;
			}
			if (end != 0 && next.getCommitTime() >= end) {
				continue;
			}
			if (next.getParentCount() == 0) {
				continue;
			}
			if (cmd.hasOption("m") && !cmd.getOptionValue("m").equals(next.getName())) {
				continue;
			}
			ChangeHandler handler = null;
			if (cacheExist) {
				handler= new ChangeHandler(repo, next, chain, renameMap.get(next.getName()));
			} else {
				handler= new ChangeHandler(repo, next, chain);
			}
			final ChangeHandler tmp = handler;
			ExecutorService service = Executors.newSingleThreadExecutor();
			Runnable r = () -> {
				try {
					tmp.handle();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			Future<?> f = service.submit(r);
			try {
				if (cmd.hasOption("t")) {
					f.get(Integer.parseInt(cmd.getOptionValue("t")), TimeUnit.SECONDS);
				} else {
					f.get(60 * 60, TimeUnit.SECONDS);
				}
				renameMap.put(next.getName(), handler.getNameChangeMap());
			} catch (TimeoutException e) {
				logger.error(String.format("Timeout for commit %s", next.getName()));
				renameMap.put(next.getName(), new HashMap<String, String>());
				f.cancel(true);
				continue;
			} finally {
				service.shutdown();
			}
		}
		walk.close();
		
		JSONObject json = new JSONObject();
		for (String k : chain.keySet()) {
			JSONArray arr = new JSONArray();
			json.put(k, arr);
			List<TestInfo> list = chain.get(k);
			for (TestInfo t : list) {
				arr.put(t.toJSON());
			}
		}
	    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
	    writer.write(json.toString());
	    writer.close();
	    
	    if (cmd.hasOption("c") && !cacheExist) {
		    json = new JSONObject();
		    for (String k : renameMap.keySet()) {
		    	JSONObject json2 = new JSONObject();
		    	for (String k2 : renameMap.get(k).keySet()) {
		    		json2.put(k2, renameMap.get(k).get(k2));
		    	}
		    	json.put(k, json2);
		    }
		    writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("c")));
		    writer.write(json.toString());
		    writer.close();
	    }
	}
	static RefactoringType[] consideredTypes = { 
			RefactoringType.RENAME_CLASS, 
			RefactoringType.MOVE_CLASS,
	//		RefactoringType.MOVE_SOURCE_FOLDER,
			RefactoringType.RENAME_METHOD, 
			RefactoringType.EXTRACT_OPERATION, 
			RefactoringType.INLINE_OPERATION,
			RefactoringType.MOVE_OPERATION, 
			RefactoringType.PULL_UP_OPERATION, 
			RefactoringType.PUSH_DOWN_OPERATION,
	//		RefactoringType.MOVE_ATTRIBUTE,
	//		RefactoringType.MOVE_RENAME_ATTRIBUTE,
	//		RefactoringType.REPLACE_ATTRIBUTE,
	//		RefactoringType.PULL_UP_ATTRIBUTE,
	//		RefactoringType.PUSH_DOWN_ATTRIBUTE,
	//		RefactoringType.EXTRACT_INTERFACE,
			RefactoringType.EXTRACT_SUPERCLASS, 
			RefactoringType.EXTRACT_SUBCLASS, 
			RefactoringType.EXTRACT_CLASS,
			RefactoringType.EXTRACT_AND_MOVE_OPERATION, 
			RefactoringType.MOVE_RENAME_CLASS,
			RefactoringType.RENAME_PACKAGE,
	//		RefactoringType.EXTRACT_VARIABLE,
	//		RefactoringType.INLINE_VARIABLE,
	//		RefactoringType.RENAME_VARIABLE,
	//		RefactoringType.RENAME_PARAMETER,
	//		RefactoringType.RENAME_ATTRIBUTE,
	//		RefactoringType.REPLACE_VARIABLE_WITH_ATTRIBUTE,
			RefactoringType.PARAMETERIZE_VARIABLE,
	//		RefactoringType.MERGE_VARIABLE,
			RefactoringType.MERGE_PARAMETER,
	//		RefactoringType.MERGE_ATTRIBUTE,
	//		RefactoringType.SPLIT_VARIABLE,
			RefactoringType.SPLIT_PARAMETER,
	//		RefactoringType.SPLIT_ATTRIBUTE,
	//		RefactoringType.CHANGE_RETURN_TYPE,
	//		RefactoringType.CHANGE_VARIABLE_TYPE,
			RefactoringType.CHANGE_PARAMETER_TYPE,
	//		RefactoringType.CHANGE_ATTRIBUTE_TYPE,
	//		RefactoringType.EXTRACT_ATTRIBUTE,
			RefactoringType.MOVE_AND_RENAME_OPERATION, 
			RefactoringType.MOVE_AND_INLINE_OPERATION,
			RefactoringType.ADD_METHOD_ANNOTATION, 
			RefactoringType.REMOVE_METHOD_ANNOTATION,
			RefactoringType.MODIFY_METHOD_ANNOTATION,
	//		RefactoringType.ADD_ATTRIBUTE_ANNOTATION,
	//		RefactoringType.REMOVE_ATTRIBUTE_ANNOTATION,
	//		RefactoringType.MODIFY_ATTRIBUTE_ANNOTATION,
			RefactoringType.ADD_CLASS_ANNOTATION, 
			RefactoringType.REMOVE_CLASS_ANNOTATION,
			RefactoringType.MODIFY_CLASS_ANNOTATION,
			RefactoringType.ADD_PARAMETER,
			RefactoringType.REMOVE_PARAMETER,
			RefactoringType.REORDER_PARAMETER,
	//		RefactoringType.ADD_PARAMETER_ANNOTATION,
	//		RefactoringType.REMOVE_PARAMETER_ANNOTATION,
	//		RefactoringType.MODIFY_PARAMETER_ANNOTATION,
	//		RefactoringType.ADD_VARIABLE_ANNOTATION,
	//		RefactoringType.REMOVE_VARIABLE_ANNOTATION,
	//		RefactoringType.MODIFY_VARIABLE_ANNOTATION
	};
}

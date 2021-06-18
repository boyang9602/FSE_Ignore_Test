### Description
1. RAW.zip is the raw data
2. RQ_data is the folder of data for RQs
3. src is the folder of source code of the disabled tests tracking tool
4. scripts is the folder of scripts that process raw data

### How to compile? 
mvn clean package

### How to run?
`java -Xmx60G -jar target/cotest_tracker-0.0.1-SNAPSHOT.jar -r $repo -b $branch -s 2015-01-01 -e 2020-01-01 -o outputs/$output.json`  
You may remove the -Xmx60G if you run on small commits, i.e., commits does not touch many files.

Below are the possible options:
```
opts.addRequiredOption("r", "repo", true, "the path of the git repository of the studied system");
opts.addRequiredOption("b", "branch", true, "the branch of the repo for the walker to start to traverse");
opts.addOption("s", "start-time", true, "the start time of the studied duration, format should be yyyy-mm-dd; if not set, it will start from the first commit");
opts.addOption("e", "end-time", true, "the end time of the studued duration, format should be yyyy-mm-dd; if not set, it will end at the last commit");
opts.addRequiredOption("o", "output", true, "output file name, if it has a folder, the folder should exist");
opts.addOption("t", "timeout", true, "max execution time (in seconds) for each commit, default is 3600 seconds");
opts.addOption("c", "cache", true, "the cache file (json) of detected rename practices by the refactoring miner, the cache could be used to save time for further re-execution");
opts.addOption("m", "commit", true, "only analyze the commit");
```

import json
import os
from common import *

if __name__ == "__main__":
    results = {}
    for file in os.listdir("data-03"):
        if not file.endswith(".json"):
            continue

        with open(os.path.join("data-03", file)) as f:
            content = json.load(f)
        time_stats = {}
        commit_stats = {}

        time_stats["DELETED"] = []
        time_stats["RE-ENABLED"] = []
        commit_stats["DELETED"] = []
        commit_stats["RE-ENABLED"] = []
        del_same = 0
        del_diff = 0
        re_same = 0
        re_diff = 0
        for chain in content.values():
            chain2 = list(map(lambda x: State(x), chain))
            while True:
                index, detected_types = find_disabled(chain2)
                if index == -1:
                    break
                if index == len(chain2) - 1:
                    break
                index2, del_or_reenabled = find_deleted_or_reenabled(chain2[index + 1:])
                if index2 == -1:
                    break
                cmd = "git --git-dir /mnt/c/Users/boyan/projects/ignoreprojects/{}/.git log {}..{} --pretty=oneline | wc -l".format(file.split("@")[0], chain2[index].commitId(), chain2[index + 1:][index2].commitId())
                print(cmd)
                commit_counts = int(os.popen(cmd).read()[:-1])
                print(commit_counts)
                if del_or_reenabled == "deleted":
                    time_stats["DELETED"].append(chain2[index + 1:][index2].time() - chain2[index].time())
                    commit_stats["DELETED"].append(commit_counts)
                    if chain2[index].author() == chain2[index + 1:][index2].author():
                        del_same += 1
                    else:
                        del_diff += 1
                elif del_or_reenabled == "reenabled":
                    time_stats["RE-ENABLED"].append(chain2[index + 1:][index2].time() - chain2[index].time())
                    commit_stats["RE-ENABLED"].append(commit_counts)
                    if chain2[index].author() == chain2[index + 1:][index2].author():
                        re_same += 1
                    else:
                        re_diff += 1
                if index2 == len(chain2[index + 1:]) - 1:
                    break
                chain2 = chain2[index + 1:][index2:]
        results[file[:-5]] = {"del": {"same": del_same, "diff": del_diff}, "re": {"same": re_same, "diff": re_diff}, "durations": time_stats, "commits": commit_stats}
    with open("rq2v4.json", "w") as f:
        json.dump(results, f)

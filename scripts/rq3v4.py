import json
import os
import csv
from common import *

if __name__ == "__main__":
    pro_stats = {}
    patterns = set()
    for file in os.listdir("data-03"):
        if not file.endswith(".json"):
            continue

        with open(os.path.join("data-03", file)) as f:
            content = json.load(f)
        evolution = {}
        for k in content.keys():
            evolution[k] = []
            chain = content[k]
            chain2 = list(map(lambda x: State(x), chain))
            while True:
                index, detected_types = find_disabled(chain2)
                if index == -1:
                    break
                evolution[k].append({
                    "name": chain2[index].name(),
                    "commit": chain2[index].commitId(),
                    "change_type": "DISABLED",
                    "sub_types": detected_types
                })
                if index == len(chain2) - 1:
                    break
                index2, del_or_reenabled = find_deleted_or_reenabled(chain2[index + 1:])
                if index2 == -1:
                    break
                if del_or_reenabled == "deleted":
                    evolution[k].append({
                        "name": chain2[index + 1:][index2].name(),
                        "commit": chain2[index + 1:][index2].commitId(),
                        "change_type": "DELETED"
                    })
                elif del_or_reenabled == "reenabled":
                    evolution[k].append({
                        "name": chain2[index + 1:][index2].name(),
                        "commit": chain2[index + 1:][index2].commitId(),
                        "change_type": "RE-ENABLED"
                    })
                if index2 == len(chain2[index + 1:]) - 1:
                    break
                chain2 = chain2[index + 1:][index2:]
            if len(evolution[k]) == 0:
                del evolution[k]
        with open("evolutions-03/" + file, "w") as f:
            json.dump(evolution, f)
        stats = {}
        for chain in evolution.values():
            k = "->".join(list(map(lambda x: x["change_type"], chain)))
            patterns.add(k)
            count = stats.get(k, 0)
            count += 1
            stats[k] = count
        pro_stats[file[:-5]] = stats
    with open("rq3v4.csv", "w") as f:
        writer = csv.writer(f, delimiter = ",")
        patterns = list(patterns)
        writer.writerow(["Project"] + patterns)
        for k in pro_stats:
            stats = pro_stats[k]
            writer.writerow([k] + [stats[p] if p in stats else 0 for p in patterns])

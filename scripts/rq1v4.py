import json
import os
import csv
from common import *
from state import State

types = ["CO->Entire Test(New Test)", "ADD->@Ignore(New Test)", "CO->@Test(New Test)", "enabled=false->@Test(New Test)",
        "CO->Entire Test", "ADD->@Ignore", "UNCO->@Ignore", "CO->@Test", "DEL->@Test", "enabled=false->@Test", "Disabled Total"]

if __name__ == "__main__":
    with open("rq1v3.csv", "w") as f:
        writer = csv.writer(f, delimiter = ",")
        writer.writerow(["Project"] + [t for t in types] + ["DEL", "RE-ENABLED", "CHANGED", "UNCHANGED", "NOTCOMPARED"])
    for file in os.listdir("data-03"):
        if not file.endswith(".json"):
            continue

        with open(os.path.join("data-03", file)) as f:
            content = json.load(f)
        stats = {}
        deleted = 0
        reenabled = 0
        changed = 0
        unchanged = 0
        notcompared = 0

        for t in types:
            stats[t] = 0
        for chain in content.values():
            chain2 = list(map(lambda x: State(x), chain))
            while True:
                index, detected_types = find_disabled(chain2)
                if index == -1:
                    break
                stats["Disabled Total"] += 1

                for _type in detected_types:
                    stats[_type] += 1
                if index == len(chain2) - 1:
                    break
                index2, _type = find_deleted_or_reenabled(chain2[index + 1:])
                if index2 == -1:
                    break
                if _type == "deleted":
                    deleted += 1
                elif _type == "reenabled":
                    reenabled += 1
                    if chain2[index].status() == "NEW_CO":
                        notcompared += 1
                    else:
                        if chain2[index].stripped_content() == chain2[index + 1:][index2].stripped_content():
                            unchanged += 1
                        else:
                            changed += 1
                if index2 == len(chain2[index + 1:]) - 1:
                    break
                chain2 = chain2[index + 1:][index2:]
        with open("rq1v4.csv", "a") as f:
            writer = csv.writer(f, delimiter = ",")
            writer.writerow([file[:-5]] + [stats[t] for t in types] + [deleted, reenabled, changed, unchanged, notcompared])

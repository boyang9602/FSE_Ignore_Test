import json
import os

projects = ["camel@master", "cassandra@trunk", "cloudstack@master", 
            "druid@master", "flink@master", "hadoop@trunk", 
            "hbase@master", "hive@master", "ignite@master", 
            "incubator-pinot@master", "kafka@trunk", "maven@master", 
            "Openfire@master", "orientdb@develop", "storm@master"]

folder1 = "data-04"
folder2 = "data-03"

def equals(a1, a2):
    if len(a1) != len(a2):
        return False
    for a in a1:
        if a not in a2:
            return False
    return True

diff_data = {}
for p in projects:
    diff_data[p] = {}
for project in projects:
    with open(os.path.join(folder1, project + ".json"), "r") as f:
        data1 = json.load(f)
    with open(os.path.join(folder2, project + ".json"), "r") as f:
        data2 = json.load(f)
    test_only_1 = data1.keys() - data2.keys()
    test_only_2 = data2.keys() - data1.keys()
    test_common = data1.keys() - test_only_1
    test_different = set()
    for k in test_common:
        if len(data1[k]) == len(data2[k]):
            for i in range(len(data1[k])):
                if data1[k][i]["status"] != data2[k][i]["status"] or not equals(data1[k][i]["COAnnotations"], data2[k][i]["COAnnotations"]):
                    test_different.add(k)
        else:
            test_different.add(k)

    print("-------{}--------".format(project))
    print("Only in 1:")
    print(test_only_1)
    print("Only in 2:")
    print(test_only_2)
    print("Different tests:")
    print(test_different)
    for t in test_only_1:
        diff_data[project][t] = {}
        diff_data[project][t]["old"] = data1[t]
    for t in test_only_2:
        diff_data[project][t] = {}
        diff_data[project][t]["new"] = data2[t]
    for t in test_different:
        diff_data[project][t] = {}
        diff_data[project][t]["old"] = data1[t]
        diff_data[project][t]["new"] = data2[t]
with open("diff_01_03.json", "w") as f:
    json.dump(diff_data, f)
    
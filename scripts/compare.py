OLD_ADD_IDS = []
NEW_ADD_IDS = []

import json
import os
import csv
def hasActiveTest(state):
    flag = False
    for anno in state["activeAnnotations"]:
        if "@Test" in anno:
            flag = True
    return flag

def hasActiveIgnore(state):
    flag = False
    for anno in state["activeAnnotations"]:
        if "@Ignore" in anno:
            flag = True
    return flag

def hasCOTest(state):
    flag = False
    for anno in state["COAnnotations"]:
        if "@Test" in anno:
            flag = True
    return flag

def hasCOIgnore(state):
    flag = False
    for anno in state["COAnnotations"]:
        if "@Ignore" in anno:
            flag = True
    return flag
with open("data\\storm.json.old") as old:
    content = json.load(old)
    for chain in content.values():
        for change in chain:
            if change["type"] == "DELTest":
                OLD_ADD_IDS.append(change["nameAfter"])

with open("data\\storm@master.json") as n:
    content = json.load(n)
    for chain in content.values():
        for state in chain:
            if state["status"] == "DEL":
                NEW_ADD_IDS.append(state["name"])
        # if len(chain) == 1:
        #     continue
        # for i in range(1, len(chain)):
        #     prev_state = chain[i - 1]
        #     state = chain[i]
        #     if hasActiveTest(prev_state) and hasCOTest(state) and not hasActiveTest(state):
        #         NEW_ADD_IDS.append(state["name"])

print(len(OLD_ADD_IDS), len(NEW_ADD_IDS))
print(set(OLD_ADD_IDS) - set(NEW_ADD_IDS))
print("-------------------------------")
print(set(NEW_ADD_IDS) - set(OLD_ADD_IDS))
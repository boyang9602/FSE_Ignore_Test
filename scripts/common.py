from state import State
def find_disabled(chain):
    prev = chain[0]
    if prev.status() == "NEW_CO":
        return 0, ["CO->Entire Test(New Test)"]
    elif prev.status() == "NEW":
        details = []
        if prev.has_active_ignore():
            details.append("ADD->@Ignore(New Test)")
        if prev.has_co_test():
            details.append("CO->@Test(New Test)")
        if prev.has_active_test() and prev.is_active_test_disabled():
            details.append("enabled=false->@Test(New Test)")
        if len(details) > 0:
            return 0, details
    for i in range(1, len(chain)):
        prev = chain[i - 1]
        curr = chain[i]
        if prev.is_active() and not curr.is_active() and curr.status() != "DEL": # active -> inactive => disabled
            details = []
            test_change = State.compare_test(prev, curr)
            ignore_change = State.compare_ignore(prev, curr)
            status_change = State.compare_status(prev, curr)
            if test_change != None:
                details.append(test_change + "->@Test")
            if ignore_change != None:
                details.append(ignore_change + "->@Ignore")
            if status_change != None:
                details.append(status_change + "->Entire Test")
            assert len(details) > 0, "No sub types detected, \n" + str(prev.state) + "\n|\nv\n" + str(curr.state)
            return i, details
    return -1, []
def find_deleted_or_reenabled(chain):
    for i in range(len(chain)):
        if chain[i].status() == "DEL":
            return i, "deleted"
        elif chain[i].is_active():
            return i, "reenabled"
    return -1, ""

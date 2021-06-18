class State:
    def __init__(self, state):
        self.state = state

    def has_active_test(self):
        return "activeTest" in self.state

    def has_co_test(self):
        return "COTest" in self.state

    def is_active_test_disabled(self):
        return self.state["isActiveTestDisabled"]

    def is_co_test_disabled(self):
        return self.state["isCOTestDisabled"]

    def has_active_ignore(self):
        return "activeIgnore" in self.state

    def has_co_ignore(self):
        return "COIgnore" in self.state

    def testng(self):
        return self.state["testng"]

    def junit3(self):
        return self.state["junit3"]

    def junit4(self):
        return self.state["junit4"]

    def content(self):
        return self.state["content"]

    def stripped_content(self):
        return self.state["strippedContent"]

    def author(self):
        return self.state["author"]

    def time(self):
        return self.state["time"]

    def is_class(self):
        return "::" not in self.state["name"]

    def status(self):
        return self.state["status"]

    def name(self):
        return self.state["name"]

    def commitId(self):
        return self.state["commitId"]

    def is_active(self):
        if self.status() == "DEL":
            return False
        if self.is_class():
            if self.junit3 or self.junit4():
                return not self.has_active_ignore()
            if self.testng():
                return self.has_active_test() and not self.is_active_test_disabled()
            return not self.has_active_ignore() and not (self.has_active_test() and self.is_active_test_disabled())
        else:
            return self.status() in ["NEW", "ACT"] and not self.has_active_ignore() and self.has_active_test() and not self.is_active_test_disabled()
    
    def is_co(self):
        return self.status() in ["CO", "NEW_CO"]

    @staticmethod
    def compare_test(state1, state2):
        if state1.has_active_test(): # @Test -> ?(@Test or //@Test or del @Test)
            if state2.has_active_test(): # @Test -> @Test, check the enabled param
                if state1.is_active_test_disabled():
                    if state2.is_active_test_disabled(): # @Test(enabled=false) -> @Test(enabled=false)
                        return None # no change
                    else: # @Test(enabled=false) -> @Test(enabled=true)/@Test
                        return "enabled=true"
                else:
                    if state2.is_active_test_disabled(): # @Test(enabled=true)/@Test -> @Test(enabled=false)
                        return "enabled=false"
                    else: # @Test(enabled=true)/@Test -> @Test(enabled=true)/@Test
                        return None
            elif state2.has_co_test(): # @Test -> //@Test, check the enabled param for state1
                if state1.is_active_test_disabled(): # @Test(enabled=false) -> //@Test(enabled=false)
                    return None # no real effect
                else: # @Test/@Test(enabled=true) -> //@Test
                    return "CO" # commented out the @Test annotaton
            else: # @Test -> delete @Test, check the enabled param for the state1
                if state1.is_active_test_disabled(): # @Test(enabled=false) -> delete @Test
                    return None
                else:
                    return "DEL" # delete an enabled @Test
        elif state1.has_co_test(): # //@Test -> ?
            if state2.has_active_test():
                if state2.is_active_test_disabled(): # //@Test -> @Test(enabled=false)
                    return None
                else: # //@Test -> @Test(enabled=true)/@Test
                    return "UNCO"
            else: # do not care if it stays commented out or it is deleted
                return None
        else: # N/A -> ?
            if state2.has_active_test(): # N/A -> @Test
                if state2.is_active_test_disabled():
                    if state1.is_class() and state2.is_class():
                        return "enabled=false"
                    return None # Add a @Test(enabled=false)
                else:
                    return "ADD"
            else: # do not care as long as it does not have an active @Test
                return None

    @staticmethod
    def compare_ignore(state1, state2):
        if state1.has_active_ignore():
            if state2.has_active_ignore():
                return None
            elif state2.has_co_ignore():
                return "CO"
            else:
                return "DEL"
        elif state1.has_co_ignore():
            if state2.has_active_ignore():
                return "UNCO"
            else:
                return None
        else:
            if state2.has_active_ignore():
                return "ADD"
            else:
                return None

    @staticmethod
    def compare_status(state1, state2):
        if state1.is_co() and not state2.is_co():
            return "UNCO"
        if not state1.is_co() and state2.is_co():
            return "CO"
        if state1.status() != "DEL" and state2.status() == "DEL":
            return "DEL"

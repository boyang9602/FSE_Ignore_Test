package cotest_tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ChangeHandlerTest {
	static Map<String, List<TestInfo>> chain;
	
	@BeforeAll
	public static void setUp() throws Exception {
		Map<String, String> nameChangeMap = new HashMap<String, String>();
		nameChangeMap.put("toy.TestCase2", "toy.TestCase3");
		nameChangeMap.put("toy.TestCase1::test14()", "toy.TestCase1::test15()");
		Repository repo = JGitUtils.cloneIfNotExists("src/test/resources/toy", "https://github.com/boyang9602/toy.git");
		chain = new HashMap<String, List<TestInfo>>();
		RevWalk walk = new RevWalk(repo);
		walk.markStart(repo.parseCommit(ObjectId.fromString("8bb90b6058a74a8756d8acdd7fc8c508815595fb")));
		ChangeHandler handler = new ChangeHandler(repo, walk.next(), chain, nameChangeMap);
		handler.handle();
		walk.close();
	}

	@Test
	public void testTotal(){
		assertEquals(14 + 4, chain.keySet().size());
	}
	
	@Test
	public void test1(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test1()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test1()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertFalse(before.has("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(0, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test2(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test2()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test2()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertFalse(after.has("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(0, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test3(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test3()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test3()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertEquals("@Ignore", before.getString("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(2, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test4(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test4()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test4()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertFalse(after.has("activeTest"));
		assertFalse(before.has("COTest"));
		assertEquals("@Test", after.getString("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isCOTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(0, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(1, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test5(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test5()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test5()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertFalse(before.has("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertEquals("@Test", before.getString("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertFalse(before.getBoolean("isCOTestDisabled"));
		assertEquals(0, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(1, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test6(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test6()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test6()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertEquals("@Ignore", after.getString("activeIgnore"));
		assertEquals("@Ignore", before.getString("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(2, after.getJSONArray("activeAnnotations").length());
		assertEquals(1, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test7(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test7()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test7()").get(1).toJSON().toString());

		assertEquals("CO", before.getString("status"));
		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertFalse(before.has("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertEquals("@Test", before.getString("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertFalse(before.getBoolean("isCOTestDisabled"));
		assertEquals(0, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(1, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test8(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test8()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test8()").get(1).toJSON().toString());

		assertEquals("CO", before.getString("status"));
		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertFalse(before.has("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertEquals("@Test", before.getString("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertFalse(before.getBoolean("isCOTestDisabled"));
		assertEquals(0, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(1, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test9(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test9()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test9()").get(1).toJSON().toString());

		assertEquals("ACT", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test(enabled=false)", before.getString("activeTest").replaceAll(" ", ""));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertTrue(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test10(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test10()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test10()").get(1).toJSON().toString());

		assertEquals("DEL", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test11(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test11()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test11()").get(1).toJSON().toString());

		assertEquals("ACT", before.getString("status"));
		assertEquals("CO", after.getString("status"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertFalse(after.has("activeTest"));
		assertFalse(before.has("COTest"));
		assertEquals("@Test", after.getString("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isCOTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(0, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(1, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test12() {
		assertFalse(chain.containsKey("toy.TestCase1::test12()"));
	}
	
	@Test
	public void test13() {
		assertEquals(1, chain.get("toy.TestCase1::test13()").size());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test13()").get(0).toJSON().toString());

		assertEquals("NEW", after.getString("status"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(after.has("COTest"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(after.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test
	public void test15(){
		JSONObject before = new JSONObject(chain.get("toy.TestCase1::test15()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test15()").get(1).toJSON().toString());

		assertEquals("ACT", before.getString("status"));
		assertEquals("ACT", after.getString("status"));
		assertEquals("toy.TestCase1::test14()", before.getString("name"));
		assertEquals("TestCase1.java", before.getString("filePath"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertEquals("@Test", before.getString("activeTest"));
		assertEquals("@Test", after.getString("activeTest"));
		assertFalse(before.has("COTest"));
		assertFalse(after.has("COTest"));
		assertFalse(before.has("activeIgnore"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(before.has("COIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(before.getBoolean("isActiveTestDisabled"));
		assertFalse(after.getBoolean("isActiveTestDisabled"));
		assertEquals(1, before.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("activeAnnotations").length());
		assertEquals(0, before.getJSONArray("COAnnotations").length());
		assertEquals(0, after.getJSONArray("COAnnotations").length());
		assertFalse(before.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit3"));
		assertFalse(before.getBoolean("junit4"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(before.getBoolean("testng"));
		assertFalse(after.getBoolean("testng"));
	}

	@Test
	public void test16() {
		assertEquals(1, chain.get("toy.TestCase1::test16()").size());
		JSONObject after = new JSONObject(chain.get("toy.TestCase1::test16()").get(0).toJSON().toString());
		assertEquals("NEW_CO", after.getString("status"));
		assertEquals("TestCase1.java", after.getString("filePath"));
		assertFalse(after.has("activeTest"));
		assertEquals("@Test", after.getString("COTest"));
		assertFalse(after.has("activeIgnore"));
		assertFalse(after.has("COIgnore"));
		assertFalse(after.getBoolean("isCOTestDisabled"));
		assertEquals(0, after.getJSONArray("activeAnnotations").length());
		assertEquals(1, after.getJSONArray("COAnnotations").length());
		assertFalse(after.getBoolean("junit3"));
		assertFalse(after.getBoolean("junit4"));
		assertFalse(after.getBoolean("testng"));
	}
	
	@Test public void test17() {
		JSONObject before = new JSONObject(chain.get("toy.TestCase3::test1()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase3::test1()").get(1).toJSON().toString());

		assertEquals("ACT", before.getString("status"));
		assertEquals("ACT", after.getString("status"));
		assertEquals("toy.TestCase2::test1()", before.getString("name"));
		assertEquals("TestCase2.java", before.getString("filePath"));
		assertEquals("TestCase3.java", after.getString("filePath"));
	}
	
	@Test public void test18() {
		JSONObject before = new JSONObject(chain.get("toy.TestCase3::test2()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase3::test2()").get(1).toJSON().toString());

		assertEquals("CO", before.getString("status"));
		assertEquals("CO", after.getString("status"));
		assertEquals("toy.TestCase2::test2()", before.getString("name"));
		assertEquals("TestCase2.java", before.getString("filePath"));
		assertEquals("TestCase3.java", after.getString("filePath"));
	}
	
	@Test public void test19() {
		JSONObject before = new JSONObject(chain.get("toy.TestCase3::test3()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase3::test3()").get(1).toJSON().toString());

		assertEquals("CO", before.getString("status"));
		assertEquals("ACT", after.getString("status"));
		assertEquals("toy.TestCase2::test3()", before.getString("name"));
		assertEquals("TestCase2.java", before.getString("filePath"));
		assertEquals("TestCase3.java", after.getString("filePath"));
	}
	
	@Test public void test20() {
		JSONObject before = new JSONObject(chain.get("toy.TestCase3::test4()").get(0).toJSON().toString());
		JSONObject after = new JSONObject(chain.get("toy.TestCase3::test4()").get(1).toJSON().toString());

		assertEquals("ACT", before.getString("status"));
		assertEquals("CO", after.getString("status"));
		assertEquals("toy.TestCase2::test4()", before.getString("name"));
		assertEquals("TestCase2.java", before.getString("filePath"));
		assertEquals("TestCase3.java", after.getString("filePath"));
	}
}

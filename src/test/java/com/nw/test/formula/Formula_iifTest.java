package com.nw.test.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nw.formula.FormulaParser;
import org.nw.vo.pub.lang.UFDouble;


public class Formula_iifTest extends AbstractFormulaTestCase {
	@Test
	public void test1() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", "0");
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test2() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 0);
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test3() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 0.0);
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test4() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", Integer.valueOf(0));
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test5() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", Double.valueOf(0.0));
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test6() {
		String[] formulas = { "b->iif(a==0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", UFDouble.ZERO_DBL);
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test7() {
		String[] formulas = { "b->iif(a==0.0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", UFDouble.ZERO_DBL);
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

	@Test
	public void test8() {
		String[] formulas = { "b->iif(a==0.0,\"true\",\"false\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 0);
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);

		for(Map<String, Object> rtMap : resultList) {
			// System.out.println(rtMap);
			Assert.assertEquals(rtMap.get("b"), "true");
		}
	}

}

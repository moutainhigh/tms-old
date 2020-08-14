package com.nw.test.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nw.formula.FormulaParser;

public class Formula_substringTest extends AbstractFormulaTestCase {
	@Test
	public void test1() {
		String[] formulas = { "b->substring(a,2)" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", "abcdefg");
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);
	}

	@Test
	public void test2() {
		String[] formulas = { "b->substring(a,2,4)" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", "abcdefg");
		context.add(map);

		FormulaParser formulaParse = new FormulaParser(ds);
		formulaParse.setFormulas(formulas);
		formulaParse.setContext(context);
		formulaParse.setMergeContextToResult(false);
		List<Map<String, Object>> resultList = formulaParse.getResult();

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() != 0);
	}

}

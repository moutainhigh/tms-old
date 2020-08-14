package com.nw.test.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nw.formula.FormulaParser;


public class Formula_getcolvaluemorewithcondTest extends AbstractFormulaTestCase {
	@Test
	public void test1() {
		String[] formulas = { "a->getcolvaluemorewithcond(\"sm_user\",\"pk_corp\",\"cuserid\",cuserid,\"dr\",dr,\"1=1\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cuserid", "0001N11000000007G5SB");
		map.put("dr", "0");
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
			Assert.assertEquals(rtMap.get("a"), "1001");
		}
	}

	@Test
	public void test2() {
		String[] formulas = { "a->getcolvaluemorewithcond(\"sm_user\",\"pk_corp\",\"cuserid\",cuserid,\"dr\",dr,\"1=1\",__nocache);b->getcolvaluemorewithcond(\"sm_user\",\"pk_corp\",\"cuserid\",cuserid,\"dr\",dr,\"1=1\",__nocache);" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cuserid", "0001N11000000007G5SB");
		map.put("dr", 0);
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
			Assert.assertEquals(rtMap.get("a"), "1001");
			Assert.assertEquals(rtMap.get("b"), "1001");
		}
	}

	@Test
	public void test3() {
		String[] formulas = { "a->getcolvaluemorewithcond(\"sm_user\",\"pk_corp\",\"cuserid\",cuserid,\"dr\",dr,\"\")" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cuserid", "0001N11000000007G5SB");
		map.put("dr", "0");
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
			Assert.assertEquals(rtMap.get("a"), "1001");
		}
	}

}

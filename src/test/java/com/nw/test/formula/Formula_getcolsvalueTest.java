package com.nw.test.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nw.formula.FormulaParser;

public class Formula_getcolsvalueTest extends AbstractFormulaTestCase {
	@Test
	public void test1() {
		// vadmindeptname,pk_unit->getColsValue("bd_deptdoc","deptname","def3","pk_deptdoc",vadmindeptid)
		String[] formulas = { "a,b->getColsValue(\"sm_user\",\"pk_corp\",\"dr\",\"cuserid\",cuserid)" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cuserid", "0001N11000000007G5SB");
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
			Assert.assertEquals(rtMap.get("b"), 0);
		}
	}

	@Test
	public void test2() {
		String[] formulas = { "a,b->getColsValue(\"sm_user\",\"pk_corp\",\"dr\",\"cuserid\",cuserid,__nocache);c,d->getColsValue(\"sm_user\",\"pk_corp\",\"dr\",\"cuserid\",cuserid,__nocache);" };

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cuserid", "0001N11000000007G5SB");
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
			Assert.assertEquals(rtMap.get("b"), 0);
			Assert.assertEquals(rtMap.get("c"), "1001");
			Assert.assertEquals(rtMap.get("d"), 0);
		}
	}

}

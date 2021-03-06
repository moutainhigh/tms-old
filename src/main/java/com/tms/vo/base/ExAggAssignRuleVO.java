
package com.tms.vo.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.nw.vo.HYBillVO;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.trade.pub.IExAggVO;

@SuppressWarnings("serial")
public class ExAggAssignRuleVO extends HYBillVO implements IExAggVO {

	public static final String TS_ASSIGN_RULE_B = "ts_assign_rule_b";
	
	@SuppressWarnings("rawtypes")
	private HashMap hmChildVOs = new HashMap();

	public String[] getTableCodes() {

		return new String[] { TS_ASSIGN_RULE_B };

	}

	public String[] getTableNames() {

		return new String[] { TS_ASSIGN_RULE_B };
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CircularlyAccessibleValueObject[] getAllChildrenVO() {

		ArrayList al = new ArrayList();
		for(int i = 0; i < getTableCodes().length; i++) {
			CircularlyAccessibleValueObject[] cvos = getTableVO(getTableCodes()[i]);
			if(cvos != null)
				al.addAll(Arrays.asList(cvos));
		}

		return (SuperVO[]) al.toArray(new SuperVO[0]);
	}

	public CircularlyAccessibleValueObject[] getTableVO(String tableCode) {

		return (CircularlyAccessibleValueObject[]) hmChildVOs.get(tableCode);
	}

	public void setParentId(SuperVO item, String id) {
	}

	@SuppressWarnings("unchecked")
	public void setTableVO(String tableCode, CircularlyAccessibleValueObject[] vos) {

		hmChildVOs.put(tableCode, vos);
	}

	public String getDefaultTableCode() {

		return getTableCodes()[0];
	}

	public SuperVO[] getChildVOsByParentId(String tableCode, String parentId) {

		return null;
	}

	@SuppressWarnings("rawtypes")
	public HashMap getHmEditingVOs() throws Exception {

		return null;
	}

	public String getParentId(SuperVO item) {
		return null;
	}
}

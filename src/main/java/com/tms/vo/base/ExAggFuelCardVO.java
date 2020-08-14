package com.tms.vo.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.nw.vo.HYBillVO;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.trade.pub.IExAggVO;

@SuppressWarnings("serial")
public class ExAggFuelCardVO extends HYBillVO implements IExAggVO {
	
	public static final String TS_FUELCARD_B = "ts_fuelcard_b";
	
	private HashMap hmChildVOs = new HashMap();

	public CircularlyAccessibleValueObject[] getAllChildrenVO() {
		ArrayList al = new ArrayList();
		for(int i = 0; i < getTableCodes().length; i++) {
			CircularlyAccessibleValueObject[] cvos = getTableVO(getTableCodes()[i]);
			if(cvos != null)
				al.addAll(Arrays.asList(cvos));
		}

		return (SuperVO[]) al.toArray(new SuperVO[0]);
	}

	public SuperVO[] getChildVOsByParentId(String tableCode, String parentid) {
		return null;
	}

	public String getDefaultTableCode() {
		return getTableCodes()[0];
	}

	public HashMap getHmEditingVOs() throws Exception {
		return null;
	}

	public String getParentId(SuperVO item) {
		return null;
	}

	public String[] getTableCodes() {
		return new String[] {"ts_fuelcard_b"};
	}

	public String[] getTableNames() {
		return new String[] {"ts_fuelcard_b"};
	}

	public CircularlyAccessibleValueObject[] getTableVO(String tableCode) {
		return (CircularlyAccessibleValueObject[]) hmChildVOs.get(tableCode);
	}

	public void setParentId(SuperVO item, String id) {

	}

	public void setTableVO(String tableCode, CircularlyAccessibleValueObject[] values) {
		hmChildVOs.put(tableCode, values);
	}

}

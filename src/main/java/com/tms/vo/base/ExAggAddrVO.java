package com.tms.vo.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.nw.vo.HYBillVO;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.trade.pub.IExAggVO;

@SuppressWarnings("serial")
public class ExAggAddrVO extends HYBillVO implements IExAggVO {
	
	public static final String TS_ADDR_CONTACT = "ts_addr_contact";
	public static final String TS_ADDRESS_CAL_WIN = "ts_address_cal_win";
	public static final String TS_ADDRESS_CAPABILITY = "ts_address_capability";
	
	private HashMap hmChildVOs = new HashMap();

	public CircularlyAccessibleValueObject[] getAllChildrenVO() {
		// TODO Auto-generated method stub
		ArrayList al = new ArrayList();
		for(int i = 0; i < getTableCodes().length; i++) {
			CircularlyAccessibleValueObject[] cvos = getTableVO(getTableCodes()[i]);
			if(cvos != null)
				al.addAll(Arrays.asList(cvos));
		}

		return (SuperVO[]) al.toArray(new SuperVO[0]);
	}

	public String getDefaultTableCode() {
		return getTableCodes()[0];
	}

	public String[] getTableCodes() {
		return new String[] {"ts_addr_contact", "ts_address_cal_win","ts_address_capability" };
	}

	public String[] getTableNames() {
		return new String[] {"ts_addr_contact", "ts_address_cal_win","ts_address_capability" };
	}

	public CircularlyAccessibleValueObject[] getTableVO(String tableCode) {
		return (CircularlyAccessibleValueObject[]) hmChildVOs.get(tableCode);
	}

	public void setTableVO(String tableCode, CircularlyAccessibleValueObject[] values) {
		hmChildVOs.put(tableCode, values);
	}

	public SuperVO[] getChildVOsByParentId(String tableCode, String parentid) {
		return null;
	}

	public HashMap getHmEditingVOs() throws Exception {
		return null;
	}

	public String getParentId(SuperVO item) {
		return null;
	}

	public void setParentId(SuperVO item, String id) {
		
	}

}

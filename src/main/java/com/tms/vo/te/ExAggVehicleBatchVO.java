package com.tms.vo.te;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.nw.vo.HYBillVO;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.trade.pub.IExAggVO;



@SuppressWarnings("serial")
public class ExAggVehicleBatchVO extends HYBillVO implements IExAggVO {

	private HashMap hmChildVOs = new HashMap();

	public String[] getTableCodes() {

		return new String[] { "ts_entrust", EntLotTrackingViewVO.TS_ENT_LOT_TRACKING_VIEW, EntLotLineViewVO.TS_ENT_LOT_LINE_VIEW, EntLotPayDetailViewVO.TS_ENT_LOT_PAY_DETAIL_VIEW};

	}

	/**
	 * ���ض���ӱ��������� �������ڣ�2012-08-22 17:38:44
	 * 
	 * @return String[]
	 */
	public String[] getTableNames() {

		return new String[] { "ts_entrust", EntLotTrackingViewVO.TS_ENT_LOT_TRACKING_VIEW, EntLotLineViewVO.TS_ENT_LOT_LINE_VIEW, EntLotPayDetailViewVO.TS_ENT_LOT_PAY_DETAIL_VIEW};
	}

	/**
	 * ȡ�������ӱ������VO���� �������ڣ�2012-08-22 17:38:44
	 * 
	 * @return CircularlyAccessibleValueObject[]
	 */
	public CircularlyAccessibleValueObject[] getAllChildrenVO() {

		ArrayList al = new ArrayList();
		for(int i = 0; i < getTableCodes().length; i++) {
			CircularlyAccessibleValueObject[] cvos = getTableVO(getTableCodes()[i]);
			if(cvos != null)
				al.addAll(Arrays.asList(cvos));
		}

		return (SuperVO[]) al.toArray(new SuperVO[0]);
	}

	/**
	 * ����ÿ���ӱ��VO���� �������ڣ�2012-08-22 17:38:44
	 * 
	 * @return CircularlyAccessibleValueObject[]
	 */
	public CircularlyAccessibleValueObject[] getTableVO(String tableCode) {

		return (CircularlyAccessibleValueObject[]) hmChildVOs.get(tableCode);
	}

	/**
	 * 
	 * �������ڣ�2012-08-22 17:38:44
	 * 
	 * @param SuperVO
	 *            item
	 * @param String
	 *            id
	 */
	public void setParentId(SuperVO item, String id) {
	}

	/**
	 * Ϊ�ض��ӱ�����VO��� �������ڣ�2012-08-22 17:38:44
	 * 
	 * @param String
	 *            tableCode
	 * @para CircularlyAccessibleValueObject[] vos
	 */
	public void setTableVO(String tableCode, CircularlyAccessibleValueObject[] vos) {

		hmChildVOs.put(tableCode, vos);
	}

	// 增加一个删除某个tableVO的方法
	public void removeTableVO(String tableCode) {
		hmChildVOs.remove(tableCode);
	}

	/**
	 * ȱʡ��ҳǩ���� �������ڣ�2012-08-22 17:38:44
	 * 
	 * @return String
	 */
	public String getDefaultTableCode() {

		return getTableCodes()[0];
	}

	/**
	 * 
	 * �������ڣ�2012-08-22 17:38:44
	 * 
	 * @param String
	 *            tableCode
	 * @param String
	 *            parentId
	 * @return SuperVO[]
	 */
	public SuperVO[] getChildVOsByParentId(String tableCode, String parentId) {

		return null;
	}

	/**
	 * 
	 * �������ڣ�2012-08-22 17:38:44
	 * 
	 * @return HashMap
	 */
	public HashMap getHmEditingVOs() throws Exception {

		return null;
	}

	/**
	 * 
	 * ��������:2012-08-22 17:38:44
	 * 
	 * @param SuperVO
	 *            item
	 * @return String
	 */
	public String getParentId(SuperVO item) {

		return null;
	}
}

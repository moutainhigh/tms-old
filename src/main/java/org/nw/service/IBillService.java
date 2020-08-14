package org.nw.service;

import java.util.List;
import java.util.Map;

import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;

/**
 * 单据处理接口
 * 
 * @author xuqc
 * @date 2012-7-7 上午10:51:16
 */
public interface IBillService extends IToftService {
	/**
	 * 返回单据状态的字段名称
	 * 
	 * @return
	 */
	public String getBillStatusField();

	/**
	 * 返回单据类型，目前用于生成单据号
	 * 
	 * @return
	 */
	public String getBillType();

	/**
	 * 根据vbillno查询billId（主表pk）,vbillno可能是code字段
	 * 
	 * @param vbillno
	 * @return
	 */
	public String getPKByCode(String vbillno);

	/**
	 * 确认
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject confirm(ParamVO paramVO);
	
	/**
	 * 批量确认 只支持在列表界面的刷新
	 * 
	 * @param paramVO
	 * @return
	 */
	public SuperVO[] batchConfirm(ParamVO paramVO,String[] ids);

	/**
	 * 反确认
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject unconfirm(ParamVO paramVO);
	
	/**
	 * 批量反确认 只支持在列表界面的刷新
	 * 
	 * @param paramVO
	 * @return
	 */
	public SuperVO[] batchUnconfirm(ParamVO paramVO,String[] ids);


	/**
	 * 退单-委托单
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject vent(ParamVO paramVO);

	/**
	 * 撤销退单-委托单
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject unvent(ParamVO paramVO);

	/**
	 * 审核，如果存在审批流，则调用审批流
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> approve(ParamVO paramVO);

	/**
	 * 反审
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> unapprove(ParamVO paramVO);
}

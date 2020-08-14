package com.tms.service.te;

import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;

import com.tms.vo.te.ExpAccidentVO;

/**
 * 
 * @author xuqc
 * @date 2013-5-5 下午12:03:39
 */
public interface ExpAccidentService extends IBillService {

	/**
	 * 根据发货单号查询客户，返回客户pk
	 * 
	 * @param invoice_vbillno
	 * @return
	 */
	public String getCustomerByInvoice_vbillno(String invoice_vbillno);

	/**
	 * 根据委托单号查询客商，返回客商pk
	 * 
	 * @param entrust_vbillno
	 * @return
	 */
	public String getCarrierByEntrust_vbillno(String entrust_vbillno);

	/**
	 * 根据发货单查询，用于检查发货单是否已经登记过了,如果已登记，返回单据号
	 * 
	 * @param invoice_vbillno
	 * @return
	 */
	public String getByInvoice_vbillno(String invoice_vbillno);

	/**
	 * 根据委托单查询，用于检查委托单是否已经登记过了,如果已登记，返回单据号
	 * 
	 * @param entrust_vbillno
	 * @return
	 */
	public String getByEntrust_vbillno(String entrust_vbillno);

	/**
	 * 登记异常事故
	 * 
	 * @param eaVO
	 */
	public void addExpAccident(ExpAccidentVO eaVO);

	/**
	 * 撤销处理
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doRevocation(ParamVO paramVO);

	/**
	 * 结案处理
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doFinish(ParamVO paramVO);

	/**
	 * 撤销结案
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doUnfinish(ParamVO paramVO);

	/**
	 * 关闭
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doClose(ParamVO paramVO);
}

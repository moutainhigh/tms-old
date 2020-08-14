package com.tms.service.cm;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;


/**
 * 应收对账处理接口
 * 
 * @author xuqc
 * @date 2012-8-12 上午01:01:29
 */
public interface ReceCheckSheetService extends IBillService {

	/**
	 * 根据结算客户查询对账单,只查询新建状态的对账单
	 * 
	 * @param bala_customer
	 * @return
	 */
	public PaginationVO getByBala_customer(String bala_customer, ParamVO paramVO);

	/**
	 * 收款
	 * 
	 * @param json
	 */
	public Map<String, Object> doReceivable(ParamVO paramVO, String json);
	
	public void receCheckSheetInvoice(ParamVO paramVO, String json);

	/**
	 * 根据主表pk查询收款纪录
	 * 
	 * @return
	 */
	public PaginationVO loadReceCheckSheetRecord(String pk_rece_check_sheet, ParamVO paramVO, int offset, int pageSize);

	/**
	 * 导出收款记录
	 * 
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @return
	 */
	public HSSFWorkbook exportReceCheckSheetRecord(ParamVO paramVO, int offset, int pageSize);

	/**
	 * 删除收款纪录
	 * 
	 * @param pk_rece_record
	 */
	public Map<String, Object> deleteReceCheckSheetRecord(ParamVO paramVO, String pk_rece_check_sheet_record);
	
	
	/**
	 * yaojie 2015-11-10 添加集货应收明细导出功能
	 * 导出集货应收明细记录
	 * 
	 * @param 
	 */
	public HSSFWorkbook exportPickupReceCheckSheetRecord(ParamVO paramVO, String[] receDetaiPKs, String strOrderType);
	
	
	public String getReceCheckSheetCheckType(String checkType);
	
	public String getReceCheckSheetCheckCorp(String checkCorp);
	
	public String getReceCheckSheetTaxtCat(String taxtCat);
	
	public String getReceCheckSheetCheckTaxtRate(String taxtRate);
	
	public String getReceCheckSheetCheckHead(String vbillno);
}

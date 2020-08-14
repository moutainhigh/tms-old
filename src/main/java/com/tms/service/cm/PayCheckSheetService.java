package com.tms.service.cm;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;


/**
 * 应付对账处理接口
 * 
 * @author xuqc
 * @date 2012-8-12 上午01:01:29
 */
public interface PayCheckSheetService extends IBillService {

	/**
	 * 根据承运商查询对账单,只查询新建状态的对账单
	 * 
	 * @param pk_carrier
	 * @return
	 */
	public PaginationVO getByPk_carrier(String pk_carrier, ParamVO paramVO);

	/**
	 * 付款
	 * 
	 * @param json
	 */
	public Map<String, Object> doPayable(ParamVO paramVO, String json);
	
	public void payCheckSheetInvoice(ParamVO paramVO, String json);

	/**
	 * 根据主表pk查询付款纪录
	 * 
	 * @return
	 */
	public PaginationVO loadPayCheckSheetRecord(String pk_pay_check_sheet, ParamVO paramVO, int offset, int pageSize);

	/**
	 * 导出付款记录
	 * 
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @return
	 */
	public HSSFWorkbook exportPayCheckSheetRecord(ParamVO paramVO, int offset, int pageSize);

	/**
	 * 删除付款纪录
	 * 
	 * @param pk_rece_record
	 */
	public Map<String, Object> deletePayCheckSheetRecord(ParamVO paramVO, String pk_pay_check_sheet_record);
	
	public String getPayCheckSheetCheckType(String checkType);
	
	public String getPayCheckSheetTaxtCat(String taxtCat);
	
	public String getPayCheckSheetCheckTaxtRate(String taxtRate);
	
	public String getPayCheckSheetCheckHead(String vbillno);
}

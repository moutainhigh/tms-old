package com.tms.service.cm;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;

import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.te.EntSegBVO;

/**
 * 应付明细操作接口
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:21:21
 */
public interface PayDetailService extends IBillService {
	/**
	 * 根据委托单据号查询应付明细,这里是一对多的关系，但是对于type=0的应付明细，则只有一条
	 * 
	 * @return
	 */
	public PayDetailVO[] getByEntrustBillno(String entrustBillno);

	/**
	 * 根据主表pk查询付款纪录
	 * 
	 * @param pk_receive_detail
	 * @return
	 */
	public PaginationVO loadPayRecord(String pk_pay_detail, ParamVO paramVO, int offset, int pageSize);

	/**
	 * 付款
	 * 
	 * @param json
	 */
	public Map<String, Object> doPayable(ParamVO paramVO, String json);

	/**
	 * 全额付款
	 * 
	 * @param pk_pay_detail
	 */
	public List<Map<String, Object>> doPayableAll(ParamVO paramVO, String[] pk_pay_detail);

	/**
	 * 删除付款纪录
	 * 
	 * @param pk_rece_record
	 */
	public Map<String, Object> deletePayRecord(ParamVO paramVO, String pk_pay_record);

	/**
	 * 生成对账单
	 * 
	 * @param json
	 */
	public List<Map<String, Object>> buildPayCheckSheet(ParamVO paramVO, String json);

	/**
	 * 将应付明细添加到对账单中
	 * 
	 * @param pk_pay_check_sheet
	 * @param pk_pay_detail
	 */
	public List<Map<String, Object>> addToPayCheckSheet(ParamVO paramVO, String pk_pay_check_sheet,
			String[] pk_pay_detail);

	public Map<String, Object> commit(ParamVO paramVO);

	public Map<String, Object> uncommit(ParamVO paramVO);

	public Map<String,Object> reComputeMny(AggregatedValueObject billVO, ParamVO paramVO);
	
	
	public List<Map<String, Object>> reComputeMnyByLots(ParamVO paramVO , String[] billIds);
	
	
	public List<Map<String, Object>> payDetailComputer(ParamVO paramVO , String[] billIds);
	/**
	 * 集货段提货段，干线段、配送段委托单重算金额  2015-10-24
	 */
	public void doPayDetailRebuildBySegtype(ParamVO paramVO, int seg_type, String[] billId);
	
	public void saveLotPay(List<PayDetailBVO> detailBVOs,String[] pdpks);
	
	public List<Map<String, Object>> loadPayDetail(String[] pk_pay_detail);
	
	public Map<String, Object> close(ParamVO paramVO);

	public Map<String, Object> unclose(ParamVO paramVO);
	
	public String getSegmentCond(EntSegBVO[] entSegBVOs);
	
	public Map<String,String> CheckSheetByProc(String ids); 
	
}

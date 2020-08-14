package com.tms.service.cm;

import java.util.List;
import java.util.Map;

import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;

import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * 应收明细处理接口
 * 
 * @author xuqc
 * @date 2012-8-12 上午01:01:29
 */
public interface ReceiveDetailService extends IBillService {

	/**
	 * 根据发货单单据号查询应收明细,这里是一对多的关系，但是对于type=0的应收明细，则只有一条
	 * 
	 * @return
	 */
	public ReceiveDetailVO[] getByInvoiceBillno(String invoiceBillno);

	/**
	 * 根据发货单号查询费用明细
	 * 
	 * @param invoiceBillno
	 * @return
	 */
	public ReceDetailBVO[] getReceDetailBVOsByInvoiceBillno(String invoiceBillno);

	/**
	 * 根据主表pk查询收款纪录
	 * 
	 * @param pk_receive_detail
	 * @return
	 */
	public PaginationVO loadReceRecord(String pk_receive_detail, ParamVO paramVO, int offset, int pageSize);

	/**
	 * 收款
	 * 
	 * @param json
	 */
	public Map<String, Object> doReceivable(ParamVO paramVO, String json);

	/**
	 * 全额收款
	 * 
	 * @param pk_receive_detail
	 */
	public List<Map<String, Object>> doReceivableAll(ParamVO paramVO, String[] pk_receive_detail);

	/**
	 * 删除收款纪录
	 * 
	 * @param pk_rece_record
	 */
	public Map<String, Object> deleteReceRecord(ParamVO paramVO, String pk_rece_record);

	/**
	 * 生成对账单
	 * 
	 * @param json
	 */
	public List<Map<String, Object>> buildReceCheckSheet(ParamVO paramVO, String json);

	/**
	 * 将应收明细添加到对账单中
	 * 
	 * @param pk_rece_check_sheet
	 * @param pk_receive_detail
	 */
	public List<Map<String, Object>> addToReceCheckSheet(ParamVO paramVO, String pk_rece_check_sheet,
			String[] pk_receive_detail);

	public Map<String, Object> commit(ParamVO paramVO);

	public Map<String, Object> uncommit(ParamVO paramVO);

	public Map<String,Object> reComputeMny(AggregatedValueObject billVO, ParamVO paramVO);
	
	public List<Map<String,Object>> reComputeMny(String[] billIds, ParamVO paramVO);

	/**
	 * 集货发货单重算金额，根据发货单所属运段计算
	 * 
	 * @param paramVO
	 * @param seg_type
	 * @param billId
	 */
	public void doRebuildBySegtype(ParamVO paramVO, int seg_type, String[] billId);
	
	
	/**
	 * 导出集货应收明细  songf 2015-11-03
	 * 
	 * @param paramVO  暂时没用到
	 * @param receDetaiPKs  跨页选中的主键值
	 * @param strOrderType 类型，是正常，还是返箱
	 * @return
	 */
	public HSSFWorkbook exportPickupReceiveDetailRecord(ParamVO paramVO, String[] receDetaiPKs,String strOrderType);
	
	public Map<String, Object> close(ParamVO paramVO);

	public Map<String, Object> unclose(ParamVO paramVO);
	
	public Map<String,String> CheckSheetByProc(String ids);
}

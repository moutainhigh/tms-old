package com.tms.service.inv;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;

import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvoiceVO;

/**
 * 发货单操作接口
 * 
 * @author xuqc
 * @date 2012-8-8 下午09:09:55
 */
public interface InvoiceService extends IBillService {

	/**
	 * 发货单对应的原始应收明细
	 * 
	 * @param invoiceBillno
	 * @return
	 */
	public ReceiveDetailVO getReceiveDetailVOByInvoiceBillno(String invoiceBillno);

	/**
	 * 查询提货方和收货方
	 * 
	 * @param pk_address
	 * @param type
	 * @return
	 */
	public Map<String, Object> getAddrInfoByPkAddress(String pk_address, String type);

	/**
	 * 根据起始城市和目的城市计算运输里程和区间距离, <br/>
	 * XXX 先根据提货方，收货方来匹配，若匹配不到，则根据城市来批评
	 * 
	 * @param deli_city
	 * @param arri_city
	 * @return
	 */
	public Map<String, Object> getMileageAndDistance(String deli_city, String arri_city, String pk_delivery,
			String pk_arrival);

	/**
	 * 根据单据号查询发货单
	 * 
	 * @param vbillno
	 * @return
	 */
	public InvoiceVO getByVbillno(String vbillno);

	/**
	 * 匹配合同，返回费用明细，这里匹配方式和配载页面完全相同，但是多了一个处理步骤，如果该费用是系统生成的，则需要更新，否则合并
	 * <ul>
	 * <li>1、结算客户、运输方式、起始地、目的地不能为空</li>
	 * <li>
	 * 2、先根据起始地、目的地去匹配，如果匹配不到，再根据起始城市、目的城市匹配</li>
	 * <li>
	 * 3、如果合同中，区间报价（报价类型）、固定价格（价格类型）、设备（计价方式），则根据设备类型进行匹配</li>
	 * </ul>
	 * 
	 * @param pk_entrust
	 *            发货单主键
	 * @param num_count
	 *            总件数-用于确定单价以及计算金额
	 * @param fee_weight_count
	 *            总计费重-用于确定单价以及计算金额
	 * @param volume_count
	 *            总体积-用于确定单价以及计算金额
	 * @param node_count
	 *            节点数-用于确定单价以及计算金额
	 * @param pk_carrier
	 *            承运商
	 * @param pk_trans_type
	 *            运输方式
	 * @param start_addr
	 *            起始地
	 * @param end_addr
	 *            目的地
	 * @param start_city
	 *            起始城市
	 * @param end_city
	 *            目的城市
	 * @param pk_car_type
	 *            车辆类型
	 * @return
	 */
	public List<Map<String, Object>> refreshReceDetail(String pk_invoice, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, String bala_customer,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date,Integer urgent_level, String item_code, 
			String pk_trans_line,UFBoolean if_return);

	public PaginationVO loadReceDetail(String pk_invoice, ParamVO paramVO, int offset, int pageSize);

	/**
	 * 返回今天最新的5条待提交（新增状态）的发货单
	 * 
	 * @return
	 */
	public List<InvoiceVO> getTodayTop5();

	//yaojiie 2015 11 23添加接口方法，milkrun一键生成方法。 
	public void milkRun(ParamVO paramVO, String[] pk_invoices) ;
	
	public void keyStowage(ParamVO paramVO, String[] pk_invoices) ;
	
	public Map<String, Object> close(ParamVO paramVO);

	public Map<String, Object> unclose(ParamVO paramVO);

}

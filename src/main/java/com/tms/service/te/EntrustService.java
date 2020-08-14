package com.tms.service.te;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;

import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;

/**
 * 委托单操作接口
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:21:00
 */
public interface EntrustService extends IBillService {

	/**
	 * 根据委托单pk返回委托单的路线信息
	 * 
	 * @param pk_entrust
	 * @return
	 */
	public List<Map<String, Object>> loadEntLineB(String pk_entrust);
	
	//yaojiie 2015 12 27 添加此接口
	public List<Map<String, Object>> loadEntLinePackB(String pk_ent_line_b);

	/**
	 * 根据委托单pk返回委托单的运力信息
	 * 
	 * @param pk_entrust
	 * @return
	 */
	public List<Map<String, Object>> loadEntTransbilityB(String pk_entrust);

	/**
	 * 匹配合同，返回费用明细，这里匹配方式和配载页面完全相同，但是多了一个处理步骤，如果该费用是系统生成的，则需要更新，否则合并
	 * <ul>
	 * <li>1、承运商、运输方式、起始地、目的地不能为空</li>
	 * <li>
	 * 2、先根据起始地、目的地去匹配，如果匹配不到，再根据起始城市、目的城市匹配</li>
	 * <li>
	 * 3、如果合同中，区间报价（报价类型）、固定价格（价格类型）、设备（计价方式），则根据设备类型进行匹配</li>
	 * </ul>
	 * 
	 * @param pk_entrust
	 *            委托单主键
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
	public List<Map<String, Object>> refreshPayDetail(String pk_entrust, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, int node_count, String pk_carrier,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date,Integer urgent_level,String item_code ,
			String pk_trans_line,UFBoolean if_return);

	/**
	 * 返回今天最新的5条待提交（新增状态）的委托单
	 * 
	 * @return
	 */
	public List<EntrustVO> getTodayTop5();

	/**
	 * 生成入库单
	 * 
	 * @param paramVO
	 */
	public void buildInstorage(ParamVO paramVO);

	/**
	 * 生成出库单
	 * 
	 * @param paramVO
	 */
	public void buildOutstorage(ParamVO paramVO);

	/**
	 * 查询委托单对应的应付凭证
	 * 
	 * @param entrustBillno
	 * @return
	 */
	public PayDetailVO getPayDetailVOsByEntrustBillno(String entrustBillno);
	

	/**
	 * 导入运力信息的保存方法
	 * 
	 * 2015 11 12 14:15 yaojie 
	 * 
	 * @param List<EntTransbilityBVO>
	 * 
	 */
	public void saveEntTransbility(List<EntTransbilityBVO> etbBVOs);
	
	
	public List<SuperVO> cashToPay(EntrustVO parentVO);
	
	
	public void generateInvoice(ExAggEntrustVO aggEntrustVO);
	
	public String sendEntEmail(String[] ids,String funcode);
	
	
	public Map<String,Object> authentication(String lot, String card_msg);
	
	public List<Map<String,Object>> syncExpress(String[] ids);
	
	public Map<String, Object> receipt(String vbillno,PodVO podVO,ParamVO paramVO);
	
	public Map<String, Object> expReceipt(String pk_entrust,PodVO podVO,ParamVO paramVO,List<EntPackBVO> packBVOs);
	
	public Map<String, Object> unReceipt(String vbillno,ParamVO paramVO);
	
	public List<Map<String, Object>> getPackRecord(String id);
	
}

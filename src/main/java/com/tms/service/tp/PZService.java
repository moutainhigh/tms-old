package com.tms.service.tp;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;

import com.tms.vo.cm.PackInfo;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 调度配载-配载页面
 * 
 * @author xuqc
 * @date 2012-9-12 下午09:37:33
 */
public interface PZService extends IBillService {

	public SegmentVO[] querySegmentByPKs(String[] pk_segment);

	/**
	 * 配载页面-根据pk查询运段,需要执行模板中的公式
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<Map<String, Object>> loadSegmentByPKs(String[] pk_segment);

	/**
	 * 配载页面-根据pk查询运段的路线信息，需要执行模板中的公式 <br/>
	 * 根据运段手机运段的收货方和提货方，一条运段对应两条路线信息
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<Map<String, Object>> loadLineInfo(String[] pk_segment);

	/**
	 * 读取发货单的运力信息
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<Map<String, Object>> loadTransbilityB(String[] pk_segment);

	/**
	 * 匹配合同，返回费用明细
	 * <ul>
	 * <li>1、承运商、运输方式、起始地、目的地不能为空</li>
	 * <li>
	 * 2、先根据起始地、目的地去匹配，如果匹配不到，再根据起始城市、目的城市匹配</li>
	 * <li>
	 * 3、如果合同中，区间报价（报价类型）、固定价格（价格类型）、设备（计价方式），则根据设备类型进行匹配</li>
	 * </ul>
	 * 
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
	 * @param deli_node_count
	 *            委托单的提货点个数
	 * @return
	 */
	public List<Map<String, Object>> loadPayDetail(double pack_num_count, int num_count, double fee_weight_count,
			double weight_count, double volume_count, int node_count, int deli_node_count,List<PackInfo> packInfos, String pk_carrier,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date,Integer urgent_level,String item_code,
			String pk_trans_line,UFBoolean if_return);

	/**
	 * 根据运段返回发货单信息
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<InvoiceVO> getInvoiceVOBySegmentPKs(String[] pk_segment);

	/**
	 * 批量配载,定义这个名称，避免使用事务，在起每个计划的配载中使用事务，这样不会造成死锁。 TODO
	 * sqlserver默认的锁粒度是表格，现在还不知道怎么改成行锁，改完后，直接对这个方法使用事务
	 * 
	 * @param headerVO
	 * @param vbillnoAry
	 */
	public void processBatchSave(PZHeaderVO headerVO, String[] vbillnoAry, ParamVO paramVO);
	
	public Map<String, Object> save(AggregatedValueObject billVO, ParamVO paramVO,String placeholder);
	
	public ExAggEntrustVO doProcessPZ(PZHeaderVO headerVO, SegmentVO segVO, EntTransbilityBVO[] entTransbilityVOs,ParamVO paramVO,int ent_serialno);

}

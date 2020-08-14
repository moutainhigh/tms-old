package com.tms.service.cm;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFBoolean;

import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBMatchVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.ReceDetailBMatchVO;
import com.tms.vo.cm.ReceDetailBVO;

/**
 * 合同处理接口
 * 
 * @author xuqc
 * @date 2012-8-28 上午10:16:51
 */
public interface ContractService extends IToftService {

	/**
	 * 批量新增,用于导入合同明细
	 * 
	 * @param superVOs
	 * @return
	 */
	public int addChildren(List<ContractBVO> superVOs);

	/**
	 * 匹配合同
	 * <p>
	 * FIXME,请确认sql语句的性能
	 * </p>
	 * 
	 * @param contract_type
	 *            ,合同类型，客户合同或者承运商合同
	 * @param pk_carrierOrBala_customer
	 * @param pk_trans_type
	 * @param start_addr
	 * @param end_addr
	 * @param start_city
	 * @param end_city
	 * @return
	 */
	public List<ContractBVO> matchContract(int contract_type, String pk_carrierOrBala_customer, String pk_trans_type,
			String start_addr, String end_addr, String start_city, String end_city, String pk_corp, String req_arri_date,
			Integer urgent_level, String item_code, String pk_trans_line,UFBoolean if_return);
	
	public List<ContractBVO> matchContract(ContractMatchVO matchVO);
	/**
	 * 根据合同VO生成应付明细的费用明细，这是关键的方法，需要根据不同的条件计算单价及金额
	 * 
	 * @param strPkEntrust
	 * 			 当费用为可选费用时，用来查找可选费用项
	 * @param num_count
	 *            总件数-用于确定单价以及计算金额
	 * @param fee_weight_count
	 *            总计费重-用于确定单价以及计算金额
	 * @param volume_count
	 *            总体积-用于确定单价以及计算金额
	 * @param pk_car_type
	 *            设备类型-用于确定单价以及计算金额
	 * @param deli_node_count
	 *            提货点的个数
	 * @param contractBVOs
	 * @return
	 */
	public List<PayDetailBVO> buildPayDetailBVO(String strPkEntrust, String pk_carrier, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, int node_count, int deli_node_count, List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp,Integer urgent_level, String item_code, String pk_trans_line, UFBoolean if_return, List<ContractBVO> contractBVOs);
	
	public List<PayDetailBVO> buildPayDetailBVO(String pk_carrier, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, int node_count, int deli_node_count, List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp,Integer urgent_level, String item_code, String pk_trans_line, UFBoolean if_return, List<ContractBVO> contractBVOs);

	public List<PayDetailBVO> buildPayDetailBVO(PayDetailBMatchVO matchVO);
	/**
	 * 根据合同VO生成应收明细的费用明细，这是关键的方法，需要根据不同的条件计算单价及金额
	 * 
	 * @param num_count
	 *            总件数-用于确定单价以及计算金额
	 * @param fee_weight_count
	 *            总计费重-用于确定单价以及计算金额
	 * @param volume_count
	 *            总体积-用于确定单价以及计算金额
	 * @param pk_car_type
	 *            设备类型-用于确定单价以及计算金额
	 * @param contractBVOs
	 * @return
	 */
	public List<ReceDetailBVO> buildReceDetailBVO(String pk_customer, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp, List<ContractBVO> contractBVOs);
	
	public List<ReceDetailBVO> buildReceDetailBVO(ReceDetailBMatchVO matchVO);
	
}

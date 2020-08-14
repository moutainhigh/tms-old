package com.tms.service.tp;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 运段操作接口
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:20:14
 */
public interface StowageService extends IBillService {
	
	/**
	 * 返回承运商
	 * 
	 * @return
	 */
	public List<Map<String, String>> getCarrData(String[] billIDs);

	/**
	 * 返回路线树
	 * 
	 * @return
	 */
	public List<TreeVO> getLineTree();

	/**
	 * 返回车辆树
	 * 
	 * @return
	 */
	//public List<TreeVO> getCarTree(String[] carrIds);
	
	public List<Map<String,String>> getCarData(String[] carrIds);

	/**
	 * 定义这个接口是为了在Controller中调用
	 * 
	 * @param lineVO
	 * @return
	 */
	public String getCondString(TransLineVO lineVO);

	/**
	 * 根据主键查询
	 * 
	 * @param pks
	 * @return
	 */
	public List<SegmentVO> loadByPKs(String[] pks);

	/**
	 * 根据运段id查询包含的货品
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<Map<String, Object>> loadSegPackByParent(String pk_segment);

	/**
	 * 分段操作
	 * 
	 * @param pk_address
	 *            增加的节点
	 * @param req_deli_date
	 *            要求离开日期
	 * @param req_arri_date
	 *            要求到货日期
	 * @param pk_segment
	 *            待分段的运段
	 * @return
	 */
	public List<SegmentVO> saveDistSection(String[] pk_address, String[] req_deli_date, String[] req_arri_date,
			String[] pk_segment);

	/**
	 * 自动分段
	 * 
	 * @param segVOs
	 * @param packVOs
	 *            这个参数的作用实际上在发货单的自动分段时使用，如果不是发货单那边的自动分段，那么传入null即可
	 * @return
	 */
	public void autoDistSection(SegmentVO[] segVOs, SegPackBVO[] packVOs);

	/**
	 * 分段
	 * 
	 * @param addrVOs
	 * @param req_deli_date
	 * @param req_arri_date
	 * @param segVOs
	 * @param packVOs
	 *            这个参数的作用实际上在发货单的自动分段时使用，如果不是发货单那边的自动分段，那么传入null即可
	 * @return
	 */
	public List<SegmentVO> distSection(List<AddressVO> addrVOs, List<String> req_deli_date, List<String> req_arri_date,
			List<SegmentVO> segVOs, SegPackBVO[] packVOs);

	/**
	 * 自动分段操作
	 * 
	 * @param pk_segment
	 *            待分段的运段pk
	 * @return
	 */
	public List<SegmentVO> saveAutoDistSection(String[] pk_segment);

	/**
	 * 撤销分段
	 * 
	 * @param pk_segment
	 *            待撤销的运段pk
	 * @return
	 */
	public List<SegmentVO> saveCancelSection(String[] pk_segment);

	/**
	 * 分量
	 * 
	 * @param pk_seg_pack_b
	 *            货品包装pk
	 * @param dist_num
	 *            拆分数量
	 * @param dist_weight
	 *            拆分重量
	 * @param dist_volume
	 *            拆分体积
	 * @return
	 */
	public List<SegmentVO> saveDistQuantity(String pk_segment, String[] pk_seg_pack_b, UFDouble[] dist_pack_num_count,
			int[] dist_num, UFDouble[] dist_weight, UFDouble[] dist_volume);

	/**
	 * 撤销分量
	 * 
	 * @param pk_segment
	 *            待撤销的运段pk
	 * @return
	 */
	public List<SegmentVO> saveCancelQuantity(String[] pk_segment);

	/**
	 * 委派
	 * 
	 * @param pk_segment
	 * @return
	 */
	public List<Map<String, Object>> doDelegate(String pk_corp, String[] pk_segment, ParamVO paramVO);

	/**
	 * 撤销委派
	 * 
	 * @param pk_segment
	 * @param paramVO
	 * @return
	 */
	public List<Map<String, Object>> cancelDelegate(String[] pk_segment, ParamVO paramVO);
	
	public RootVO getCurrentTrackVO(String carno);
	
	/**
	 * 同步订单  songf  2015-12-14
	 * 
	 * @param pk_segment
	 * @param paramVO
	 * @return
	 */
	public void syncOrders(String[] pk_segment, ParamVO paramVO);
	
	
	public List<Map<String, Object>> delegateCarrier(String[] billIds, String carrier, ParamVO paramVO);
	
	public Map<String,String> getFullLoadRates(String[] ids,String[] cars);
	
	public Map<String,Object> getProcDatas(String[] ids,String pk_car);
	
	public List<SegmentVO> getSegments(String[] ids);
	
	public Map<String,Object> mergeSegments(String[] billIds, ParamVO paramVO);
	
	public void dispatch(String pk_car, String pk_carrier,String[] pk_segments);
	
}

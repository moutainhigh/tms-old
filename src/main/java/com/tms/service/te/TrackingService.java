package com.tms.service.te;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.nw.dao.PaginationVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.FilesystemVO;

import com.tms.vo.base.APPTrackingVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntOperationBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;

/**
 * 异常跟踪操作接口
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:21:00
 */
public interface TrackingService extends IBillService {
	/**
	 * 根据委托单pk返回委托单的路线信息
	 * 
	 * @param pk_entrust
	 * @return
	 */
	public List<Map<String, Object>> loadEntLineB(String pk_entrust);
	
	public List<Map<String, Object>> loadDeliEntLineB(String pk_entrust);
	
	public List<Map<String, Object>> loadArriEntLineB(String pk_entrust);
	
	//yaojiie 2015 12 27 添加此接口
	public List<Map<String, Object>> loadEntLinePackB(String pk_ent_line_b);
	

	/**
	 * 确认节点到货,一个对象数组，第一个元素是委托单ＶＯ，第二个是EntLineBVO
	 * 
	 * @param pk_ent_line_b
	 * @return 一个对象数组，第一个元素是委托单ＶＯ，第二个是EntLineBVO
	 */
	public Object[] confirmArrival(EntLineBVO entLineBVO,  Integer type);

	/**
	 * 反确认节点到货,一个对象数组，第一个元素是委托单ＶＯ，第二个是EntLineBVO
	 * 
	 * @return 一个对象数组，第一个元素是委托单ＶＯ，第二个是EntLineBVO
	 * @return
	 */
	public Object[] unconfirmArrival(EntLineBVO entLineBVO);
	
	public Object[] unArrival(EntLineBVO entLineBVO);

	/**
	 * 读取委托单的跟踪信息
	 * 
	 * @param paramVO
	 * @param entrust_vbillno
	 * @return
	 */
	public PaginationVO loadEntTracking(String entrust_vbillno, int offset, int pageSize);

	/**
	 * 删除跟踪信息
	 * 
	 * @param pk_ent_tracking
	 */
	public void deleteEntTracking(String pk_ent_tracking, String last_pk);

	/**
	 * 保存一条委托单跟踪信息
	 * 
	 * @param etVO
	 * @return
	 */
	public Map<String, Object> saveEntTracking(EntTrackingVO etVO, EntrustVO entVO, ExpAccidentVO eaVO,
			String[] pk_filesystems, String[] invoiceVbillnoAry);

	/**
	 * 保存委托单跟踪信息，包含附件信息
	 * 
	 * @param etVO
	 * @param entVO
	 * @param eaVO
	 * @param attachVOs
	 * @param inAry
	 * @return
	 */
	public Map<String, Object> saveEntTracking2(EntTrackingVO etVO, EntrustVO entVO, ExpAccidentVO eaVO,
			List<FilesystemVO> attachVOs, List<InputStream> inAry);

	/**
	 * 批量保存跟踪信息
	 * 
	 * @param etVO
	 * @param vbillno
	 * @return
	 */
	public Map<String, Object> batchSaveEntTracking(EntTrackingVO etVO, String[] vbillno);

	/**
	 * 调用lbs服务读取位置信息
	 * 
	 * @param pk_entrust
	 * @param gps_id
	 * @return
	 */
	public RootVO getTrackVOs(String pk_entrust, String gps_id);

	/**
	 * 查询当前车辆的位置信息
	 * 
	 * @param pk_entrust
	 * @param carno
	 * @return
	 */
	public RootVO getCurrentTrackVO(String pk_entrust, String carno, String pk_driver);

	/**
	 * 查询可跟踪的发货单
	 * 
	 * @param entrust_vbillno
	 * @return
	 */
	public List<Map<String, Object>> loadTrackingInvoice(String entrust_vbillno);

	/**
	 * @author:zhuyj
	 * @for: APP当前位置定位
	 * @param appTrackingVO
	 */
	public void confirmTracking(APPTrackingVO appTrackingVO);
	
	/**
	 * 保存作业反馈
	 * @param billVO
	 * @param pk_entrust
	 */
	public void saveOperation(EntOperationBVO[] entOperationBVOs,String pk_entrust, ParamVO paramVO);
	
	public List<Map<String, Object>> loadEntOperationB(String pk_entrust);
}

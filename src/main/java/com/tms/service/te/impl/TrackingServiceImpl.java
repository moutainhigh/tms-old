package com.tms.service.te.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nw.basic.util.SecurityUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.helper.DaoHelper;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.mail.MailSenderInfo;
import org.nw.mail.SimpleMailSender;
import org.nw.mail.ent.EntMailTemplate;
import org.nw.service.sys.FilesystemService;
import org.nw.utils.BillnoHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.HttpUtils;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.FilesystemVO;
import org.nw.web.utils.WebUtils;
import org.omg.CORBA.INV_POLICY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.AddressConst;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpAccidentConst.ExpAccidentOrgin;
import com.tms.constants.FunConst;
import com.tms.constants.OrderTypeConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TrackingConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CarrService;
import com.tms.service.base.CustService;
import com.tms.service.cm.PayDetailService;
import com.tms.service.inv.InvoiceService;
import com.tms.service.job.lbs.TrackInfoConverter;
import com.tms.service.pod.PodService;
import com.tms.service.te.ExpAccidentService;
import com.tms.service.te.TrackingService;
import com.tms.vo.base.APPTrackingVO;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.OrderAssVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntOperationBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.te.ExpAccidentVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 异常跟踪
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:26:41
 */
//zhuyj 对外接口依据beanName获取对象实例
@Service("trackingService")
public class TrackingServiceImpl extends TMSAbsBillServiceImpl implements TrackingService {

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private PodService podService;

	@Autowired
	private ExpAccidentService expAccidentService;

	@Autowired
	private FilesystemService filesystemService;

	@Autowired
	private PayDetailService payDetailService;
	
	@Autowired
	private CustService custService;
	
	@Autowired
	private CarrService carrService;
	
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggEntrustVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, EntrustVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, EntrustVO.PK_ENTRUST);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.WTD;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setBeforeRenderer("req_deli_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setBeforeRenderer("req_arri_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("exp_flag")) {
					// 跟踪信息是否异常
					fieldVO.setBeforeRenderer("exp_flagBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
				} else if(fieldVO.getItemkey().equals("vbillno")) {
					fieldVO.setRenderer("vbillnoRenderer");
				} else if(fieldVO.getItemkey().equals("carno_name")) {
					fieldVO.setRenderer("carno_nameRenderer");
				} else if(fieldVO.getItemkey().equals("pk_driver")) {
					fieldVO.setRenderer("pk_driverRenderer");
				}
				
			}
		}
		return templetVO;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		// 这个单据没有表体，所以不需要判断是否表头
		String cond = "vbillstatus <>0";
		String cond1 = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond1)) {
			cond += " and " + cond1;
		}
		return cond;
	}

	public List<Map<String, Object>> loadEntLineB(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return null;
		}
		String unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
				+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
				+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
				+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
				+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
				+ " FROM ts_ent_line_b WITH(NOLOCK) WHERE dr = 0";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
					+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
					+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
					+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
					+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
					+ " FROM ts_ent_line_b WHERE dr = 0";
		}
		String sql = unitSql + " AND ts_ent_line_b.pk_entrust = " + "'"+pk_entrust+"'"+" ORDER BY ts_ent_line_b.req_arri_date asc ";
		
		List<EntLineBVO> lineVOs = NWDao.getInstance().queryForList(sql, EntLineBVO.class);
		if(lineVOs == null || lineVOs.size() == 0){
			return null;
		}

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(lineVOs.size());
		for(SuperVO vo : lineVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getEntLineBFormulas(), true);
		return list;
	}
	
	public List<Map<String, Object>> loadDeliEntLineB(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return null;
		}
		String unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
				+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
				+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
				+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
				+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
				+ " FROM ts_ent_line_b WITH(NOLOCK) WHERE isnull(dr,0)=0 and addr_flag = 'S'";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
					+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
					+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
					+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
					+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
					+ " FROM ts_ent_line_b WHERE isnull(dr,0)=0 and addr_flag = 'S'";
		}
		String sql = unitSql + " AND ts_ent_line_b.pk_entrust = " + "'"+pk_entrust+"'"+" ORDER BY ts_ent_line_b.req_arri_date asc ";
		
		List<Map<String, Object>> mapList = NWDao.getInstance().queryForList(sql);
		for(Map<String, Object> map : mapList){
			map.put("act_arri_date", new UFDateTime(new Date()));
		}
		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getEntLineBFormulas(), true);
		return list;
	}
	
	public List<Map<String, Object>> loadArriEntLineB(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return null;
		}
		String unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
				+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
				+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
				+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
				+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
				+ " FROM ts_ent_line_b WITH(NOLOCK) WHERE isnull(dr,0)=0 and addr_flag = 'E'";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
					+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
					+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
					+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
					+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
					+ " FROM ts_ent_line_b WHERE isnull(dr,0)=0 and addr_flag = 'E'";
		}
		String sql = unitSql + " AND ts_ent_line_b.pk_entrust = " + "'"+pk_entrust+"'"+" ORDER BY ts_ent_line_b.req_arri_date asc ";
		
		List<Map<String, Object>> mapList = NWDao.getInstance().queryForList(sql);
		for(Map<String, Object> map : mapList){
			map.put("act_arri_date", new UFDateTime(new Date()));
		}
		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getEntLineBFormulas(), true);
		return list;
	}
	
	//yaojiie 2015 12 27 添加此方法，根据PK值，加载此单价下所有的路线包装明细
	public List<Map<String, Object>> loadEntLinePackB(String pk_ent_line_b) {
		if(StringUtils.isBlank(pk_ent_line_b)) {
			return null;
		}
		EntLinePackBVO[] entLinePackBVOs = dao.queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_line_b=? order by serialno",pk_ent_line_b);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(entLinePackBVOs.length);
		for(SuperVO vo : entLinePackBVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		return mapList;
	}
	
	
	private String[] getEntLineBFormulas() {
		return new String[] {
				"addr_code,addr_name,pk_city,pk_province,pk_area,detail_addr->getcolsvalue(\"ts_address\",\"addr_code\",\"addr_name\",\"pk_city\",\"pk_province\",\"pk_area\",\"detail_addr\",\"pk_address\",pk_address)",
				"city_name->getColValue(ts_area, name, pk_area, pk_city)",
				"province_name->getColValue(ts_area, name, pk_area, pk_province)",
				"area_name->getColValue(ts_area, name, pk_area, pk_area)" };
	}
	

	/**
	 * 节点到货时插入跟踪表记录
	 * 
	 * @param entVO
	 * @param entLineBVO
	 * @param startFlag
	 *            是否是委托单的第一个节点
	 * @param endFlag
	 *            是否是委托单的最后一个节点
	 */
	private EntTrackingVO processEntrustTrackingInfoWhileConfirmArrival(EntrustVO entVO, EntLineBVO entLineBVO,
			boolean startFlag, boolean endFlag) {
		String pk_address = entLineBVO.getPk_address();
		AddressVO addrVO = dao.queryByCondition(AddressVO.class, "pk_address=?", pk_address);
		if(addrVO == null) {
			throw new BusiException("该地址已经被删除，地址PK[" + pk_address+"]");
		}
		EntTrackingVO etVO = null;
		if(startFlag) {
			// 第一个节点
			etVO = new EntTrackingVO();
			etVO.setTracking_status(TrackingConst.DELI);
			etVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			etVO.setTracking_memo(addrVO.getAddr_name()==null?"":addrVO.getAddr_name() + "提货");
		}
		if(endFlag) {
			// 最后一个节点
			etVO = new EntTrackingVO();
			etVO.setTracking_status(TrackingConst.ARRI);
			etVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			etVO.setTracking_memo(addrVO.getAddr_name()==null?"":addrVO.getAddr_name() + "到货");
		}
		if(etVO == null) {
			// 中间节点到货
			etVO = new EntTrackingVO();
			etVO.setTracking_status(TrackingConst.NODE_ARRI);
			etVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			etVO.setTracking_memo("到达" + addrVO.getAddr_name()==null?"":addrVO.getAddr_name());
		}
		etVO.setLot(entVO.getLot());
		etVO.setNode_flag(UFBoolean.TRUE);// 节点到货增加的跟踪信息
		// 节点到货时向跟踪信息表中插入一条记录
		etVO.setEntrust_vbillno(entVO.getVbillno());
		etVO.setInvoice_vbillno(entVO.getInvoice_vbillno());
		etVO.setPk_corp(WebUtils.getLoginInfo() == null ? entVO.getPk_corp() : WebUtils.getLoginInfo().getPk_corp());
		etVO.setCreate_user(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		etVO.setCreate_time(new UFDateTime(new Date()));
		
		etVO.setCurr_latitude(entLineBVO.getCurr_latitude());
		etVO.setCurr_longitude(entLineBVO.getCurr_longitude());
		etVO.setApp_detail_addr(entLineBVO.getApp_detail_addr());
		
		etVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(etVO);
		NWDao.getInstance().saveOrUpdate(etVO);

		// 同步最新的跟踪信息到委托单主表
		EntrustUtils.syncEntrustTrackingInfo(etVO, entVO, false);
		return etVO;
	}

	/**
	 * 处理发货单的跟踪信息
	 * 
	 * @param entVO
	 * @param invVO
	 * @param entLineBVO
	 * @param addr_flag
	 *            标识这个节点在这个运段中属于提货点还是到货点
	 * @return
	 */
	private InvTrackingVO processInvoiceTrackingInfoWhileConfirmArrival(EntrustVO entVO, InvoiceVO invVO,
			EntLineBVO entLineBVO, String addr_flag, EntTrackingVO etVO) {
		// 处理发货单的跟踪信息
		String pk_address = entLineBVO.getPk_address();
		AddressVO addrVO = dao.queryByCondition(AddressVO.class, "pk_address=?", pk_address);
		if(addrVO == null) {
			throw new BusiException("该地址已经被删除，地址PK[?]！" + pk_address);
		}
		InvTrackingVO itVO = null;
		// XXX 关键问题是如何判断这个节点是否是发货单的第一个节点或者最后一个节点
		if(pk_address.equals(invVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
			// 第一个节点
			itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.DELI);
			itVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			// 不需要组装，在相应的同步方法会做
			itVO.setTracking_memo(addrVO.getAddr_name() + "提货");
		} else if(pk_address.equals(invVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
			// 最后一个节点
			itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.ARRI);
			itVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			itVO.setTracking_memo(addrVO.getAddr_name() + "到货");
		}
		if(itVO == null) {
			// 中间节点到货
			itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.NODE_ARRI);
			itVO.setTracking_time(new UFDateTime(entLineBVO.getAct_arri_date()));
			//itVO.setTracking_memo("到达" + addrVO.getAddr_name());
			itVO.setTracking_memo(etVO.getTracking_memo());
		}
		itVO.setNode_flag(UFBoolean.TRUE);// 节点到货增加的跟踪信息
		// 节点到货时向跟踪信息表中插入一条记录
		itVO.setEntrust_vbillno(entVO.getVbillno());
		itVO.setInvoice_vbillno(invVO.getVbillno());
		itVO.setPk_corp(WebUtils.getLoginInfo() == null ? entVO.getPk_corp() : WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);

		// 同步跟踪信息到发货单
		EntrustUtils.syncInvoiceTrackingInfo(etVO, invVO, false);
		return itVO;
	}

	/**
	 * 反确认节点到货时删除跟踪表记录,并返回最新的一条跟踪记录
	 * 
	 * @param entVO
	 * @param pk_address
	 * @param startFlag
	 *            是否是委托单的第一个节点
	 * @param endFlag
	 *            是否是委托单的最后一个节点
	 */
	private EntTrackingVO processEntrustTrackingInfoWhileUnconfirmArrival(EntrustVO entVO, String pk_address,
			boolean startFlag, boolean endFlag) {
		int tracking_status;
		if(startFlag) {
			tracking_status = TrackingConst.DELI;
		} else if(endFlag) {
			tracking_status = TrackingConst.ARRI;
		} else {
			tracking_status = TrackingConst.NODE_ARRI;
		}
		EntTrackingVO etVO = null, latestEtVO = new EntTrackingVO();
		latestEtVO.setEntrust_vbillno(entVO.getVbillno());
		EntTrackingVO[] etVOs = dao.queryForSuperVOArrayByCondition(EntTrackingVO.class, "entrust_vbillno=? ORDER BY ts asc ",
				entVO.getVbillno());
		for(int i = 0; i < etVOs.length; i++) {
			//yaojiie 2015 12 01 为解决莫名其妙出现节点状态为空的Tracking_status加的判断，其实没啥用。
			if(etVOs[i].getTracking_status() == null){
				throw new BusiException("跟踪信息状态为空，请检查单据!");
			}
			if(etVOs[i].getTracking_status().intValue() == tracking_status) {
				// 定位到要删除的那一行
				etVO = etVOs[i];
				dao.delete(etVO);
				if(i > 0) {
					latestEtVO = etVOs[i - 1];// 最近的一条跟踪记录
				}
				break;
			}
		}
		// 将委托单的跟踪信息清空
		EntrustUtils.syncEntrustTrackingInfo(latestEtVO, entVO, true);
		return latestEtVO;
	}

	/**
	 * 处理发货点的跟踪信息
	 * 
	 * @param invVO
	 * @param pk_address
	 * @param addr_flag
	 *            用来标识这个节点是运段的提货点还是到货点
	 * @return
	 */
	private InvTrackingVO processInvoiceTrackingInfoWhileUnconfirmArrival(InvoiceVO invVO, String pk_address,
			String addr_flag, EntTrackingVO etVO) {
		// 处理发货单的跟踪信息
		int tracking_status;
		if(pk_address.equals(invVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
			tracking_status = TrackingConst.DELI;
		} else if(pk_address.equals(invVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
			tracking_status = TrackingConst.ARRI;
		} else {
			tracking_status = TrackingConst.NODE_ARRI;
		}
		InvTrackingVO itVO = dao.queryByCondition(InvTrackingVO.class, "invoice_vbillno=? and tracking_status=? and entrust_vbillno=?",
				invVO.getVbillno(), tracking_status, etVO.getEntrust_vbillno());
		if(itVO != null) {
			dao.delete(itVO);
		}

		// 将委托单的跟踪信息清空
		EntrustUtils.syncInvoiceTrackingInfo(null, invVO, true);
		return itVO;
	}
	
	/**
	 * type 0正常操作 1提货 2到货
	 */
	public Object[] confirmArrival(EntLineBVO entLineBVO, Integer type) {
		if(entLineBVO == null) {
			throw new BusiException("参数不能为空！");
		}
		if(entLineBVO.getArrival_flag() != null && entLineBVO.getArrival_flag().booleanValue()) {
			//增加一个判断，当登录用户是RF用户时，不产生异常，而是将错误信息返回给controller。
			if(WebUtils.getLoginInfo().getPlatform_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getPlatform_type()){
				String msg = "委托单必须是[已确认、已提货、已到货]状态下才能保存跟踪信息！";
				return new String[]{msg};
			}else{
				throw new BusiException("委托单必须是[已确认、已提货]状态下才能保存跟踪信息！");
			}
		}
		// 1、实际到达时间必须小于等于当前时间
		// 2、实际离开时间必须小于等于当前时间
		UFDateTime currentDate = new UFDateTime(new Date());
		if(StringUtils.isNotBlank(entLineBVO.getAct_arri_date())) {
			UFDateTime act_arri_date = new UFDateTime(entLineBVO.getAct_arri_date());
			if(act_arri_date.after(currentDate)){
				throw new BusiException("实际到达时间必须小于等于当前时间！");
			}
		}
		if(StringUtils.isNotBlank(entLineBVO.getAct_leav_date())) {
			UFDateTime act_leav_date = new UFDateTime(entLineBVO.getAct_leav_date());
			if(act_leav_date.after(currentDate)){
				throw new BusiException("实际离开时间必须小于等于当前时间！");
			}
		}

		List<SuperVO> updateList = new ArrayList<SuperVO>();
		entLineBVO.setStatus(VOStatus.UPDATED); // 将实际到达时间、实际离开时间、备注等更新到数据库中
		entLineBVO.setArrival_flag(new UFBoolean(true));// 标识为已确认
		updateList.add(entLineBVO);
		
		// 判断是否是委托单的第一个节点，如果是则将委托单更新为已提货
		// 判断是否是委托单的最后一个节点，如果是则将委托单更新为已到货
		// 1、检测下一个节点是否已经是未确认状态
		EntLineBVO[] lineVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class,
				"pk_entrust=? order by serialno", entLineBVO.getPk_entrust());
		if(lineVOs == null || lineVOs.length == 0) {
			return null;
		}
		// 识别这个节点所处的位置，比如是第一个节点还是最后一个节点
		boolean startFlag = false, endFlag = false;
		for(int i = 0; i < lineVOs.length; i++) {
			EntLineBVO lineVO = lineVOs[i];
			if(lineVO.getPk_ent_line_b().equals(entLineBVO.getPk_ent_line_b())) {
				if(i == 0) {
					startFlag = true;
				} else if(i == lineVOs.length - 1) {
					endFlag = true;
				} else {
					// 得到上一个节点，判断这个节点的确认状态
					if(lineVOs[i - 1].getArrival_flag() == null || !lineVOs[i - 1].getArrival_flag().booleanValue()) {
						if(WebUtils.getLoginInfo().getPlatform_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getPlatform_type()){
							String msg = "请先确认上一个节点，节点行号[" + lineVOs[i - 1].getSerialno() + "]！";
							return new String[]{msg};
						}else{
							throw new BusiException("请先确认上一个节点，节点行号[?]！",lineVOs[i - 1].getSerialno()+"");
						}
						
						
					}
				}
			}
		}
		//当委托单提货的时候，如果需要更新运段信息则走一下流程
		if(type == 1){
			processSegmengAndEntrust(entLineBVO);
		}else{
			//处理货品 信息更新记录
			List<EntLinePackBVO> entLinePackBVOs = processEntLinePackBVOWhileConfirmArrival(entLineBVO);
			if(entLinePackBVOs != null && entLinePackBVOs.size() > 0){
				updateList.addAll(entLinePackBVOs);
			}
		}
		String pk_address = entLineBVO.getPk_address();
		EntrustVO entVO = this.getByPrimaryKey(EntrustVO.class, entLineBVO.getPk_entrust());
		if(startFlag) {
			entVO.setVbillstatus(BillStatus.ENT_DELIVERY);
			entVO.setAct_deli_date(entLineBVO.getAct_arri_date());// 委托单实际提货日期=第一个节点的实际到达日期
		}
		if(endFlag) {
			entVO.setVbillstatus(BillStatus.ENT_ARRIVAL);
			entVO.setAct_arri_date(entLineBVO.getAct_arri_date());// 实际到货日期=最后一个节点的实际到达日期
			
			if(type == 2){
				updateList.addAll(processPODAndEntrust(entLineBVO,entVO));
			}
		}
		entVO.setStatus(VOStatus.UPDATED);
		updateList.add(entVO);

		// 处理跟踪表记录
		EntTrackingVO etVO = processEntrustTrackingInfoWhileConfirmArrival(entVO, entLineBVO, startFlag, endFlag);
		// 如果是中间节点，则不更新委托单的状态
		boolean ifNeedPOD = false;
		PodVO podVO = new PodVO();
		// 一个节点可能对应多个运段，因为之前进行了节点合并
		if(StringUtils.isNotBlank(entLineBVO.getPk_segment())) {
			// 非系统节点
			String[] pk_segmentAry = entLineBVO.getPk_segment().split("\\" + Constants.SPLIT_CHAR);
			// 和运段是一一对应的
			String[] addr_flagAry = entLineBVO.getAddr_flag().split("\\" + Constants.SPLIT_CHAR);
			Map<String, SegmentVO> segMap = new HashMap<String, SegmentVO>();
			Map<String, InvoiceVO> invMap = new HashMap<String, InvoiceVO>();
			for(int i = 0; i < pk_segmentAry.length; i++) {
				String pk_segment = pk_segmentAry[i];
				String addr_flag = addr_flagAry[i];

				SegmentVO segVO = segMap.get(pk_segment);
				if(segVO == null) {
					// 先从缓存中取，满足同一条运段被多次修改的情况
					segVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_segment=?", pk_segment);
					segMap.put(pk_segment, segVO);
				}
				// 校验是否能进行节点到货
				Object[] msg = checkConfirmValid(entVO, segVO, segMap);
				if(msg != null && msg.length > 0){
					return msg;
				}

				// 根据发货单号查询发货单
				InvoiceVO invVO = invMap.get(segVO.getInvoice_vbillno());
				if(invVO == null) {
					// 先从缓存中取，满足同一条发货单被多次修改的情况
					invVO = invoiceService.getByVbillno(segVO.getInvoice_vbillno());
					invMap.put(segVO.getInvoice_vbillno(), invVO);
				}
				// 必须满足提货点和到货点一样的情况
				if(pk_address.equals(segVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
					// 如果是运段的第一个节点，则将运段更新为已提货
					segVO.setVbillstatus(BillStatus.SEG_DELIVERY);
					segVO.setStatus(VOStatus.UPDATED);
					updateList.add(segVO);
				} else if(pk_address.equals(segVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
					// 如果是运段的最后一个节点，则将运段更新为已到货
					segVO.setVbillstatus(BillStatus.SEG_ARRIVAL);
					segVO.setStatus(VOStatus.UPDATED);
					updateList.add(segVO);
				}
				// 注意要满足提货点和到货点相同的情况
				// 起点 确认-》已提货，部分提货
				if(pk_address.equals(invVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
					// 如果是发货单提货点
					if(invVO.getVbillstatus().intValue() == BillStatus.INV_PART_ARRIVAL) {
						// 2014-10-12如果发货单是部分到货，那么不需要更改

					} else {
						// 同一个发货单、相同起始地、生效的、排除自身
						SegmentVO[] childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
								"invoice_vbillno=? and pk_delivery=? and seg_mark<>? and pk_segment<>? and vbillstatus<>? ",
								segVO.getInvoice_vbillno(), segVO.getPk_delivery(), SegmentConst.SEG_MARK_PARENT,
								segVO.getPk_segment(),BillStatus.SEG_CLOSE);
						if(childSegVOs != null && childSegVOs.length > 0) {
							boolean allDeliOrArri = true;// 是否全部是已提货或到货
							boolean hasWplanOrDisp = false;// 是否包含待调度和已调度
							for(SegmentVO childSegVO : childSegVOs) {
								if(segMap.keySet().contains(childSegVO.getPk_segment())) {
									// 缓存中已经存在
									childSegVO = segMap.get(childSegVO.getPk_segment());
								}
								int vbillstatus = childSegVO.getVbillstatus().intValue();
								if(vbillstatus == BillStatus.SEG_WPLAN || vbillstatus == BillStatus.SEG_DISPATCH) {
									hasWplanOrDisp = true;
									allDeliOrArri = false;
								} else if(vbillstatus == BillStatus.SEG_ARRIVAL
										|| vbillstatus == BillStatus.SEG_DELIVERY) {

								}
							}
							if(allDeliOrArri) {
								invVO.setVbillstatus(BillStatus.INV_DELIVERY);
								// 2015-05-29 记录实际提货日期
								invVO.setAct_deli_date(entLineBVO.getAct_arri_date());
								EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
								if(entLineBVOs != null && entLineBVOs.length > 0){
									entLineBVOs[0].setAct_arri_date(entLineBVO.getAct_arri_date());
									entLineBVOs[0].setAct_leav_date(entLineBVO.getAct_leav_date());
									entLineBVOs[0].setCurr_longitude(entLineBVO.getCurr_longitude());
									entLineBVOs[0].setCurr_latitude(entLineBVO.getCurr_latitude());
									entLineBVOs[0].setApp_detail_addr(entLineBVO.getApp_detail_addr());
									entLineBVOs[0].setMileage(entLineBVO.getMileage());
									entLineBVOs[0].setMemo(entLineBVO.getMemo());
									confirmArrival(entLineBVOs[0],0);
								}
								processOrderAssWhenDelivery(invVO);
								//yaojiie 2016 1 22 当发货单提货时，这个发货单有可能属于分公司结算类型，这是需要将对应的委托单状态改为已提货。
							} else if(hasWplanOrDisp) {
								invVO.setVbillstatus(BillStatus.INV_PART_DELIVERY);
								// 2015-05-29 记录实际提货日期
								invVO.setAct_deli_date(entLineBVO.getAct_arri_date());
								processOrderAssWhenDelivery(invVO);
							} else {
								// 不需要更改发货单的状态
							}
						} else {
							invVO.setVbillstatus(BillStatus.INV_DELIVERY);
							// 2015-05-29 记录实际提货日期
							invVO.setAct_deli_date(entLineBVO.getAct_arri_date());
							EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
							if(entLineBVOs != null && entLineBVOs.length > 0){
								entLineBVOs[0].setAct_arri_date(entLineBVO.getAct_arri_date());
								entLineBVOs[0].setAct_leav_date(entLineBVO.getAct_leav_date());
								entLineBVOs[0].setCurr_longitude(entLineBVO.getCurr_longitude());
								entLineBVOs[0].setCurr_latitude(entLineBVO.getCurr_latitude());
								entLineBVOs[0].setApp_detail_addr(entLineBVO.getApp_detail_addr());
								entLineBVOs[0].setMileage(entLineBVO.getMileage());
								entLineBVOs[0].setMemo(entLineBVO.getMemo());
								confirmArrival(entLineBVOs[0],0);
							}
							processOrderAssWhenDelivery(invVO);
						}
					}
				} else if(pk_address.equals(invVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
					// 如果是发货单的到货点
					// 同一个发货单、相同目的地、生效的、排除自身
					SegmentVO[] childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
							"invoice_vbillno=? and pk_arrival=? and seg_mark<>? and pk_segment<>?  and vbillstatus<>? ",
							segVO.getInvoice_vbillno(), segVO.getPk_arrival(), SegmentConst.SEG_MARK_PARENT,
							segVO.getPk_segment(),BillStatus.SEG_CLOSE);
					if(childSegVOs != null && childSegVOs.length > 0) {
						boolean allArrival = true;// 是否全部到货
						for(SegmentVO childSegVO : childSegVOs) {
							if(segMap.keySet().contains(childSegVO.getPk_segment())) {
								// 缓存中已经存在
								childSegVO = segMap.get(childSegVO.getPk_segment());
							}
							int vbillstatus = childSegVO.getVbillstatus().intValue();
							if(vbillstatus != BillStatus.SEG_ARRIVAL) {
								allArrival = false;
							}
						}
						if(allArrival) {
							invVO.setVbillstatus(BillStatus.INV_ARRIVAL);
							EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
							if(entLineBVOs != null && entLineBVOs.length > 0){
								entLineBVOs[entLineBVOs.length-1].setAct_arri_date(entLineBVO.getAct_arri_date());
								entLineBVOs[entLineBVOs.length-1].setAct_leav_date(entLineBVO.getAct_leav_date());
								entLineBVOs[entLineBVOs.length-1].setCurr_longitude(entLineBVO.getCurr_longitude());
								entLineBVOs[entLineBVOs.length-1].setCurr_latitude(entLineBVO.getCurr_latitude());
								entLineBVOs[entLineBVOs.length-1].setApp_detail_addr(entLineBVO.getApp_detail_addr());
								entLineBVOs[entLineBVOs.length-1].setMileage(entLineBVO.getMileage());
								entLineBVOs[entLineBVOs.length-1].setMemo(entLineBVO.getMemo());
								confirmArrival(entLineBVOs[entLineBVOs.length-1],0);
							}
							// 2015-05-29 记录实际到货日期
							invVO.setAct_arri_date(entLineBVO.getAct_arri_date());
							processOrderAssWhenArrival(invVO);
							//如果执行的是到货操作，那么将POD签收掉
							podVO = podService.afterChangeInvoiceToArrival(invVO, entVO);
							if(type == 2){
								ifNeedPOD = true;
							}
						} else {
							invVO.setVbillstatus(BillStatus.INV_PART_ARRIVAL);
							// 2015-05-29 记录实际到货日期
							invVO.setAct_arri_date(entLineBVO.getAct_arri_date());
							processOrderAssWhenArrival(invVO);
						}
					} else {
						invVO.setVbillstatus(BillStatus.INV_ARRIVAL);
						//yaojiie 2016 1 22 当发货单提货时，这个发货单有可能属于分公司结算类型，这是需要将对应的委托单状态改为已提货。
						// 2015-05-29 记录实际到货日期
						invVO.setAct_arri_date(entLineBVO.getAct_arri_date());
						EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
						if(entLineBVOs != null && entLineBVOs.length > 0){
							entLineBVOs[entLineBVOs.length-1].setAct_arri_date(entLineBVO.getAct_arri_date());
							entLineBVOs[entLineBVOs.length-1].setAct_leav_date(entLineBVO.getAct_leav_date());
							entLineBVOs[entLineBVOs.length-1].setCurr_longitude(entLineBVO.getCurr_longitude());
							entLineBVOs[entLineBVOs.length-1].setCurr_latitude(entLineBVO.getCurr_latitude());
							entLineBVOs[entLineBVOs.length-1].setApp_detail_addr(entLineBVO.getApp_detail_addr());
							entLineBVOs[entLineBVOs.length-1].setMileage(entLineBVO.getMileage());
							entLineBVOs[entLineBVOs.length-1].setMemo(entLineBVO.getMemo());
							confirmArrival(entLineBVOs[entLineBVOs.length-1],0);
						}
						processOrderAssWhenArrival(invVO);
						podVO = podService.afterChangeInvoiceToArrival(invVO, entVO);
						if(type == 2){
							ifNeedPOD = true;
						}
					}
				}
				invVO.setStatus(VOStatus.UPDATED);
				processInvoiceTrackingInfoWhileConfirmArrival(entVO, invVO, entLineBVO, addr_flag, etVO);
				updateList.add(invVO);
			}
			segMap.clear();
			invMap.clear();
		}
		dao.saveOrUpdate(updateList);
		if (ifNeedPOD) {
			doEntrustPod(podVO,entLineBVO);
		}
		doProcAfterArrival(entLineBVO);
		return new Object[] { entVO, entLineBVO };
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doProcAfterArrival(EntLineBVO entLineBVO){
		// 用于存储查询数据
		// 存储过程名称
		final String PROC_NAME =  "ts_confirm_arrival_proc";
		final String ID = entLineBVO.getPk_entrust();
		final String PK_USER = WebUtils.getLoginInfo() == null ? "" : WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, ID);
					cs.setString(2, PK_USER);
					cs.setString(3, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			String ee = e.toString();
			return;
		}
	}
	
	/**
	 * 节点到货时更新货品信息
	 * 
	 * @param entLineBVO  
	 *            线路信息
	 *            
	 * @return  List<EntLinePackBVO>
	 *           更新后的货品信息列表           
	 */
	private List<EntLinePackBVO> processEntLinePackBVOWhileConfirmArrival(EntLineBVO entLineBVO) {
		List<EntLinePackBVO> entLinePackBVOs = entLineBVO.getEntLinePackBVOs();
		List<String> pks = new ArrayList<String>();
		if(entLinePackBVOs == null || entLinePackBVOs.size() == 0){
			return null;
		}
		for(EntLinePackBVO vo: entLinePackBVOs){
			pks.add(vo.getPk_ent_line_b());
		}
		
		//判断为空返回 
		if(pks.size() == 0){
			return null;
		}
		String sql = "SELECT pk_ent_line_pack_b, pk_entrust, pk_ent_line_b, dr, ts, serialno, pk_goods, goods_code, goods_name, num, pack, weight, volume, unit_weight, unit_volume,"
				+ " length, width, height, trans_note, low_temp, hight_temp, reference_no, memo, pack_num_count, plan_num, plan_pack_num_count, def1, def2, def3, def4, def5, def6,"
				+ " def7, def8, def9, def10, def11, def12 FROM ts_ent_line_pack_b where dr =0 and pk_ent_line_b in ";
		String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
		
		sql = sql + cond;
		List<EntLinePackBVO> entLinePackBVOs_db = dao.queryForList(sql, EntLinePackBVO.class);
		
		//比较前台货品信息与数据库信息是否一致，如果 一致不更新，否则更新数据库查询信息
		List<EntLinePackBVO> entLinePackBVOs_return = new ArrayList<EntLinePackBVO>();
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			for(EntLinePackBVO vo : entLinePackBVOs){
				if(vo.getPk_ent_line_pack_b().equals(vo_db.getPk_ent_line_pack_b())){
					if(vo.getNum().equals(vo_db.getNum()) && (vo.getVolume() == null ? UFDouble.ZERO_DBL : vo.getVolume()).equals(vo_db.getVolume() == null ? UFDouble.ZERO_DBL : vo.getVolume()) 
							&& (vo.getWeight() == null ? UFDouble.ZERO_DBL : vo.getWeight()).equals(vo_db.getWeight() == null ? UFDouble.ZERO_DBL : vo.getWeight())){
						continue;
					}
					else
					{
						//保存更新信息，修改状态为更新 
						vo_db.setNum(vo.getNum());
						vo_db.setWeight(vo.getWeight());
						vo_db.setVolume(vo.getVolume());
						vo_db.setStatus(VOStatus.UPDATED);
						entLinePackBVOs_return.add(vo_db);
						break;
					}
				}
			}
		}
		
		return entLinePackBVOs_return;
	}
	
	//YAOJIIE 1 24 根据发货单号查询对应的分公司结算的委托单节点，递归进行跟踪等动作。
	public EntLineBVO[] getFGSEntLineBVO(InvoiceVO invoiceVO){
		if(invoiceVO.getInvoice_origin() == null || DataDictConst.INVOICE_ORIGIN.FGS.intValue() != invoiceVO.getInvoice_origin()){
			return null;
		}
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "vbillno =?",invoiceVO.getOrderno());
		if(entrustVOs == null || entrustVOs.length == 0){
			return null;
		}
		EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust =?",entrustVOs[0].getPk_entrust());
		return entLineBVOs;
	}

	/**
	 * 对于提货段的发货单，当发货单变成已提货或者部分提货时，向订单辅助表插入记录
	 * 
	 * @param invVO
	 */
	private void processOrderAssWhenDelivery(InvoiceVO invVO) {
		if(Constants.Y.equalsIgnoreCase(ParameterHelper.getParamValue("use_order_ass"))) {
			if((SegmentConst.SEG_TYPE_THD + "").equals(invVO.getDef1())) {
				// 提货段
				if(StringUtils.isBlank(invVO.getOrderno())) {
					throw new BusiException("发货单[?]的订单号不能为空！", invVO.getVbillno());
				}
				// 查询是否已经存在
				OrderAssVO oaVO = NWDao.getInstance().queryByCondition(OrderAssVO.class, "orderno=?",
						invVO.getOrderno());
				if(oaVO == null) {
					oaVO = new OrderAssVO();
					oaVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(oaVO);
				} else {
					oaVO.setStatus(VOStatus.UPDATED);
				}
				oaVO.setOrder_type(getOrderType(invVO.getPk_delivery()));
				oaVO.setOrderno(invVO.getOrderno());
				oaVO.setPk_delivery(invVO.getPk_delivery());
				oaVO.setDeli_city(invVO.getDeli_city());
				oaVO.setPk_supplier(getSupplier(invVO.getPk_delivery()));
				oaVO.setReq_deli_date(invVO.getReq_deli_date());
				oaVO.setAct_deli_date(invVO.getAct_deli_date());
				NWDao.getInstance().saveOrUpdate(oaVO);
			}
		}
	}

	/**
	 * 当提货段的发货单从已提货或者部分提货，经过反确认，返回其他状态时，清空实际提货时间，并且相应把辅助表数据删除
	 * 
	 * @param invVO
	 */
	private void processOrderAssWhenUndelivery(InvoiceVO invVO) {
		invVO.setAct_deli_date(null);
		if(Constants.Y.equalsIgnoreCase(ParameterHelper.getParamValue("use_order_ass"))) {
			if((SegmentConst.SEG_TYPE_THD + "").equals(invVO.getDef1())) {
				// 提货段
				if(StringUtils.isBlank(invVO.getOrderno())) {
					throw new BusiException("发货单[?]的订单号不能为空！", invVO.getVbillno());
				}
				// 查询是否已经存在
				OrderAssVO oaVO = NWDao.getInstance().queryByCondition(OrderAssVO.class, "orderno=?",
						invVO.getOrderno());
				if(oaVO != null) {
					// 删除
					NWDao.getInstance().delete(oaVO);
				}
			}
		}
	}

	/**
	 * 当收货段的发货单从已到货或者部分部分到货，经过反确认，返回其他状态时，清空实际到货时间，并且相应把辅助表数据更新
	 * 
	 * @param invVO
	 */
	private void processOrderAssWhenUnarrival(InvoiceVO invVO) {
		invVO.setAct_arri_date(null);

		if(Constants.Y.equalsIgnoreCase(ParameterHelper.getParamValue("use_order_ass"))) {
			if((SegmentConst.SEG_TYPE_SHD + "").equals(invVO.getDef1())) {
				// 送货段
				if(StringUtils.isBlank(invVO.getOrderno())) {
					throw new BusiException("发货单[?]的订单号不能为空！", invVO.getVbillno());
				}
				// 查询是否已经存在
				OrderAssVO oaVO = NWDao.getInstance().queryByCondition(OrderAssVO.class, "orderno=?",
						invVO.getOrderno());
				if(oaVO != null) {
					// 更新
					oaVO.setItem_name(null);
					oaVO.setPk_arrival(null);
					oaVO.setArri_city(null);
					oaVO.setReq_arri_date(null);
					oaVO.setAct_arri_date(null);
					oaVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(oaVO);
				}
			}
		}
	}

	/**
	 * 对于送货段的发货单，当发货单变成已到货或者部分到货时，向订单辅助表插入记录或者更新记录
	 * 
	 * @param invVO
	 */
	private void processOrderAssWhenArrival(InvoiceVO invVO) {
		if(Constants.Y.equalsIgnoreCase(ParameterHelper.getParamValue("use_order_ass"))) {
			if((SegmentConst.SEG_TYPE_SHD + "").equals(invVO.getDef1())) {
				// 送货段
				if(StringUtils.isBlank(invVO.getOrderno())) {
					throw new BusiException("发货单[?]的订单号不能为空！", invVO.getVbillno());
				}
				// 查询是否已经存在
				OrderAssVO oaVO = NWDao.getInstance().queryByCondition(OrderAssVO.class, "orderno=?",
						invVO.getOrderno());
				if(oaVO == null) {
					oaVO = new OrderAssVO();
					oaVO.setStatus(VOStatus.NEW);// 对于历史数据，可能还没有插入这个表
					NWDao.setUuidPrimaryKey(oaVO);
					oaVO.setOrder_type(getOrderType(invVO.getPk_delivery()));
					oaVO.setOrderno(invVO.getOrderno());
					oaVO.setPk_supplier(getSupplier(invVO.getPk_delivery()));
					oaVO.setReq_deli_date(invVO.getReq_deli_date());
					oaVO.setAct_deli_date(invVO.getAct_deli_date());
				} else {
					oaVO.setStatus(VOStatus.UPDATED);
				}
				oaVO.setItem_name(invVO.getItem_name());
				oaVO.setPk_arrival(invVO.getPk_arrival());
				oaVO.setArri_city(invVO.getArri_city());
				oaVO.setReq_arri_date(invVO.getReq_arri_date());
				oaVO.setAct_arri_date(invVO.getAct_arri_date());
				NWDao.getInstance().saveOrUpdate(oaVO);
			}
		}
	}

	/**
	 * 根据地址查询供应商，这里可能出现的问题是，一个地址和多个供应商关联了，正常情况不会出现这种，目前只取第一个
	 * 
	 * @param pk_address
	 * @return
	 */
	private String getSupplier(String pk_address) {
		if(StringUtils.isBlank(pk_address)) {
			return null;
		}
		String sql = "select pk_supplier from ts_supp_addr where pk_address=?";
		List<String> retList = NWDao.getInstance().queryForList(sql, String.class, pk_address);
		if(retList != null && retList.size() > 0) {
			return retList.get(0);
		}
		return null;
	}

	/**
	 * 订单类型，正常，返箱
	 * 
	 * @param pk_delivery
	 * @return
	 */
	private String getOrderType(String pk_delivery) {
		if(StringUtils.isBlank(pk_delivery)) {
			return null;
		}
		String sql = "select addr_code from ts_address where pk_address=?";
		String addr_code = NWDao.getInstance().queryForObject(sql, String.class, pk_delivery);
		if("8200".equals(addr_code) || "8201".equals(addr_code)) {
			return OrderTypeConst.FX;
		}
		return OrderTypeConst.NORMAL;
	}

	/**
	 * 校验是否可以进行节点到货
	 * 
	 * @param entVO
	 * @param segVO
	 * @param segMap
	 *            内存里面已经处理的运段，还没有更新到数据库
	 */
	public Object[] checkConfirmValid(EntrustVO entVO, SegmentVO segVO, Map<String, SegmentVO> segMap) {
		if(segVO == null) {
			return null;
		}
		if(StringUtils.isBlank(segVO.getParent_seg())) {
			return null;
		}
		Set<String> cacheKeys = segMap.keySet();

		SegmentVO recentlySegVO = getPrevRecentlySegVO(segVO);
		if(recentlySegVO == null) {
			return null;
		}
		// 采用右模糊方式查询出所有生效的子运段（包括它自己），如果子运段全部是分段运段，而且全部到货，那么才可以进行节点到货，否则不允许，
		SegmentVO[] childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				" vbillno like '" + recentlySegVO.getVbillno() + "%' and seg_mark <> ?", SegmentConst.SEG_MARK_PARENT);
		if(childSegVOs != null) {
			// 分离出所有的分量运段和分段运段
			List<SegmentVO> quantityList = new ArrayList<SegmentVO>();
			List<SegmentVO> sectionList = new ArrayList<SegmentVO>();
			for(SegmentVO siblingSegVO : childSegVOs) {
				if(siblingSegVO.getSeg_type() != null && siblingSegVO.getSeg_type().intValue() == SegmentConst.QUANTITY) {
					quantityList.add(siblingSegVO);
				} else {
					sectionList.add(siblingSegVO);
				}
			}
			if(sectionList.size() > 0) {
				// 说明全部是分段运段
				for(SegmentVO section : sectionList) {
					if(cacheKeys.contains(section.getPk_segment())) {
						// 缓存中已经存在
						section = segMap.get(section.getPk_segment());
					}
					if(section.getVbillstatus().intValue() != BillStatus.SEG_ARRIVAL) {
						
						if(WebUtils.getLoginInfo().getPlatform_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getPlatform_type()){
							String msg = "委托单[" + entVO.getVbillno() + "]上个运段节点未操作完成，当前节点无法进行后续操作！";
							return new String[]{msg};
						}else{
							throw new BusiException("委托单[?]上个运段节点未操作完成，当前节点无法进行后续操作！",entVO.getVbillno());
						}
					}
				}
			}
			if(quantityList.size() > 0) {
				// 如果包含分量运段，找到分量运段，并按每个分量运段父级运段进行分组，并且判断每个分组里面必须有一个分量运段为完全到货就可以进行节点到货。
				Map<Integer, List<SegmentVO>> mapList = new HashMap<Integer, List<SegmentVO>>();
				for(SegmentVO quantity : quantityList) {
					// XXX,使用横杠来区分经过了多少次分组，经过同样次数分组的运段属于同一组
					if(cacheKeys.contains(quantity.getPk_segment())) {
						// 缓存中已经存在
						quantity = segMap.get(quantity.getPk_segment());
					}
					int num = quantity.getVbillno().split("-").length - 1;// 分段和分量的总次数
					if(mapList.keySet().contains(num)) {
						List<SegmentVO> list = mapList.get(num);
						list.add(quantity);
					} else {
						List<SegmentVO> list = new ArrayList<SegmentVO>();
						list.add(quantity);
						mapList.put(num, list);
					}
				}
				for(Integer num : mapList.keySet()) {
					List<SegmentVO> list = mapList.get(num);
					boolean hasArrival = false;
					for(SegmentVO one : list) {
						if(one.getVbillstatus().intValue() == BillStatus.SEG_ARRIVAL) {
							hasArrival = true;
							break;
						}
					}
					if(!hasArrival) {
						
						if(WebUtils.getLoginInfo().getPlatform_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getPlatform_type()){
							String msg = "委托单[" + entVO.getVbillno() + "]上个运段节点未操作完成，当前节点无法进行后续操作！";
							return new String[]{msg};
						}else{
							throw new BusiException("委托单[?]上个运段节点未操作完成，当前节点无法进行后续操作！",entVO.getVbillno());
						}
						
						
					}
				}
			}
		}
		return null;
	}

	public Object[] unconfirmArrival(EntLineBVO entLineBVO) {
		if(entLineBVO == null) {
			throw new RuntimeException("参数不能为空！");
		}
		if(entLineBVO.getArrival_flag() == null || !entLineBVO.getArrival_flag().booleanValue()) {
			throw new RuntimeException("节点已经是非到货状态，不能反确认！");
		}
		// 1、检测下一个节点是否已经是未确认状态
		EntLineBVO[] lineVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class,
				"pk_entrust=? order by serialno", entLineBVO.getPk_entrust());
		if(lineVOs == null || lineVOs.length == 0) {
			return null;
		}
		// 识别这个节点所处的位置，比如是第一个节点还是最后一个节点
		boolean startFlag = false, endFlag = false;
		for(int i = 0; i < lineVOs.length; i++) {
			EntLineBVO lineVO = lineVOs[i];
			if(lineVO.getPk_ent_line_b().equals(entLineBVO.getPk_ent_line_b())) {
				if(i == 0) {
					startFlag = true;
				} else if(i == lineVOs.length - 1) {
					endFlag = true;
				} else {
					// 得到下一个节点，判断这个节点的确认状态
					if(lineVOs[i + 1].getArrival_flag() != null && lineVOs[i + 1].getArrival_flag().booleanValue()) {
						throw new BusiException("请先反确认下一个节点，节点行号[?]！",lineVOs[i + 1].getSerialno()+"");
					}
				}
			}
		}

		List<SuperVO> updateList = new ArrayList<SuperVO>();
		entLineBVO.setAct_arri_date("");
		entLineBVO.setAct_leav_date("");
		entLineBVO.setMemo("");
		entLineBVO.setStatus(VOStatus.UPDATED); // 清空实际到达时间、实际离开时间
		entLineBVO.setArrival_flag(new UFBoolean(false));// 标识为未确认
		updateList.add(entLineBVO);

		String pk_address = entLineBVO.getPk_address();
		EntrustVO entVO = this.getByPrimaryKey(EntrustVO.class, entLineBVO.getPk_entrust());
		// 判断是否是委托单的第一个节点，如果是则将委托单更新为已确认
		// 判断是否是委托单的最后一个节点，如果是则将委托单更新为已提货
		// XXX 这里可能第一个节点和最后一个节点是一样的,那么更新为已确认
		if(startFlag) {
			entVO.setAct_deli_date(null);
			entVO.setVbillstatus(BillStatus.ENT_CONFIRM);
			entVO.setAct_deli_man(null);
			entVO.setAct_deli_memo(null);
		} else if(endFlag) {
			entVO.setAct_arri_date(null);
			entVO.setVbillstatus(BillStatus.ENT_DELIVERY);
			entVO.setAct_arri_man(null);
			entVO.setAct_arri_memo(null);
		}
		// 如果是中间节点，则不更新委托单的状态

		// 出来跟踪信息
		EntTrackingVO latestEtVO = processEntrustTrackingInfoWhileUnconfirmArrival(entVO, pk_address, startFlag,
				endFlag);

		entVO.setStatus(VOStatus.UPDATED);
		updateList.add(entVO);

		// 一个节点可能对应多个运段，因为之前进行了节点合并
		if(StringUtils.isNotBlank(entLineBVO.getPk_segment())) {
			// 非系统节点
			String[] pk_segmentAry = entLineBVO.getPk_segment().split("\\" + Constants.SPLIT_CHAR);
			NWUtils.reverse(pk_segmentAry);// 反确认需要颠倒顺序
			// 地址标识和运段号是一一对应的
			String[] addr_flagAry = entLineBVO.getAddr_flag().split("\\" + Constants.SPLIT_CHAR);
			NWUtils.reverse(addr_flagAry);
			Map<String, SegmentVO> segMap = new HashMap<String, SegmentVO>();
			Map<String, InvoiceVO> invMap = new HashMap<String, InvoiceVO>();
			for(int i = 0; i < pk_segmentAry.length; i++) {
				String pk_segment = pk_segmentAry[i];
				String addr_flag = addr_flagAry[i];
				SegmentVO segVO = segMap.get(pk_segment);
				if(segVO == null) {
					// 先从缓存中取，满足同一条运段被多次修改的情况
					segVO = dao.queryByCondition(SegmentVO.class, "pk_segment=?", pk_segment);
					segMap.put(pk_segment, segVO);
				}
				// 校验
				checkUnconfirmValid(addr_flag, entVO, segVO, segMap);

				// 必须符合提货点和到货点相同的情况了
				if(pk_address.equals(segVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
					// 如果是运段的第一个节点，则将运段更新为已调度
					segVO.setVbillstatus(BillStatus.SEG_DISPATCH);
					segVO.setStatus(VOStatus.UPDATED);
					updateList.add(segVO);
				} else if(pk_address.equals(segVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
					// 如果是运段的最后一个节点，则将运段更新为已提货
					segVO.setVbillstatus(BillStatus.SEG_DELIVERY);
					segVO.setStatus(VOStatus.UPDATED);
					updateList.add(segVO);
				}

				// 根据发货单号查询发货单
				InvoiceVO invVO = invMap.get(segVO.getInvoice_vbillno());
				if(invVO == null) {
					// 先从缓存中取，满足同一条发货单被多次修改的情况
					invVO = invoiceService.getByVbillno(segVO.getInvoice_vbillno());
					invMap.put(segVO.getInvoice_vbillno(), invVO);
				}
				int invStatus = invVO.getVbillstatus().intValue();
				if(invStatus == BillStatus.INV_SIGN || invStatus == BillStatus.INV_BACK) {
					String text;
					if(invStatus == BillStatus.INV_SIGN) {
						text = "签收";
					} else {
						text = "回单";
					}
					// 如果发货单已经是签收或者回单状态，不能反确认到货
					throw new BusiException("委托单[?]对应的发货单已经[?]，不能反确认！",entVO.getVbillno(),text);
				}
				if(invStatus == BillStatus.INV_ARRIVAL) {
					// 发货单从已到货更新成其他状态[已提货或者已确认]时,需要检查签收回单表的相应记录是否删除了
					podService.afterChangeInvoiceToDelivery(invVO, entVO);
				}
				// 必须符合提货地和收货地相同的情况
				// 起点 反确认，-》部分提货，已确认
				if(pk_address.equals(invVO.getPk_delivery()) && AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
					if(invVO.getVbillstatus().intValue() == BillStatus.INV_PART_ARRIVAL) {
						// 2014-10-12 如果发货单是部分到货，那么不需要更改
					} else {
						// 同一个发货单、相同起始地、生效的、排除自身
						SegmentVO[] childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
								"invoice_vbillno=? and pk_delivery=? and seg_mark<>? and pk_segment<>? and vbillstatus <>?",
								segVO.getInvoice_vbillno(), segVO.getPk_delivery(), SegmentConst.SEG_MARK_PARENT,
								segVO.getPk_segment(),BillStatus.SEG_CLOSE);
						if(childSegVOs != null && childSegVOs.length > 0) {
							boolean hasDelivery = false;// 是否存在部分已提货
							boolean allWplanOrDisp = true;// 是否全部是待调度或已调度
							for(SegmentVO childSegVO : childSegVOs) {
								if(segMap.keySet().contains(childSegVO.getPk_segment())) {
									// 缓存中已经存在
									childSegVO = segMap.get(childSegVO.getPk_segment());
								}
								int vbillstatus = childSegVO.getVbillstatus().intValue();
								if(vbillstatus == BillStatus.SEG_DELIVERY) {
									hasDelivery = true;
									allWplanOrDisp = false;
								} else if(vbillstatus == BillStatus.SEG_WPLAN || vbillstatus == BillStatus.SEG_DISPATCH) {

								} else {
									allWplanOrDisp = false;
								}
							}
							if(hasDelivery) {
								invVO.setVbillstatus(BillStatus.INV_PART_DELIVERY);
							} else if(allWplanOrDisp) {
								EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
								if(entLineBVOs != null && entLineBVOs.length > 0){
									unconfirmArrival(entLineBVOs[0]);
								}
								invVO.setVbillstatus(BillStatus.INV_CONFIRM);
								// 2015-05-29 清空实际提货日期
								processOrderAssWhenUndelivery(invVO);
							}
						} else {
							invVO.setVbillstatus(BillStatus.INV_CONFIRM);
							EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
							if(entLineBVOs != null && entLineBVOs.length > 0){
								unconfirmArrival(entLineBVOs[0]);
							}
							// 2015-05-29 清空实际提货日期
							processOrderAssWhenUndelivery(invVO);
						}
					}
				}
				// 终点 反确认-》部分到货，已提货
				else if(pk_address.equals(invVO.getPk_arrival()) && AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
					// 同一个发货单、相同目的地、生效的、排除自身
					SegmentVO[] childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
							"invoice_vbillno=? and pk_arrival=? and seg_mark<>? and pk_segment<>? ",
							segVO.getInvoice_vbillno(), segVO.getPk_arrival(), SegmentConst.SEG_MARK_PARENT,
							segVO.getPk_segment());
					if(childSegVOs != null && childSegVOs.length > 0) {
						boolean hasArrival = false;// 是否包含已到货
						for(SegmentVO childSegVO : childSegVOs) {
							if(segMap.keySet().contains(childSegVO.getPk_segment())) {
								// 缓存中已经存在
								childSegVO = segMap.get(childSegVO.getPk_segment());
							}
							int vbillstatus = childSegVO.getVbillstatus().intValue();
							if(vbillstatus == BillStatus.SEG_ARRIVAL) {
								hasArrival = true;
								break;
							}
						}
						boolean allDelivery = true;// 是否全部已提货
						// 2014-10-12 对于已提货状态的判断，需要根据如下来判断
						childSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
								"invoice_vbillno=? and pk_delivery=? and seg_mark<>? and pk_segment<>? ",
								segVO.getInvoice_vbillno(), invVO.getPk_delivery(), SegmentConst.SEG_MARK_PARENT,
								segVO.getPk_segment());
						if(childSegVOs != null && childSegVOs.length > 0) {
							for(SegmentVO childSegVO : childSegVOs) {
								if(segMap.keySet().contains(childSegVO.getPk_segment())) {
									// 缓存中已经存在
									childSegVO = segMap.get(childSegVO.getPk_segment());
								}
								int vbillstatus = childSegVO.getVbillstatus().intValue();
								if(vbillstatus != BillStatus.SEG_DELIVERY && vbillstatus != BillStatus.SEG_ARRIVAL) {
									allDelivery = false;
									break;
								}
							}
						}

						if(hasArrival) {
							invVO.setVbillstatus(BillStatus.INV_PART_ARRIVAL);
						} else if(allDelivery) {
							invVO.setVbillstatus(BillStatus.INV_DELIVERY);
							EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
							if(entLineBVOs != null && entLineBVOs.length > 0){
								unconfirmArrival(entLineBVOs[entLineBVOs.length - 1]);
							}
							// 2015-05-29 清空实际到货日期
							processOrderAssWhenUnarrival(invVO);
						} else {
							invVO.setVbillstatus(BillStatus.INV_PART_DELIVERY);
							// 2015-05-29 清空实际到货日期
							processOrderAssWhenUnarrival(invVO);
						}
					} else {
						invVO.setVbillstatus(BillStatus.INV_DELIVERY);
						EntLineBVO[] entLineBVOs = getFGSEntLineBVO(invVO);
						if(entLineBVOs != null && entLineBVOs.length > 0){
							unconfirmArrival(entLineBVOs[entLineBVOs.length - 1]);
						}
						// 2015-05-29 清空实际到货日期
						processOrderAssWhenUnarrival(invVO);
					}
				}
				processInvoiceTrackingInfoWhileUnconfirmArrival(invVO, pk_address, addr_flag, latestEtVO);
				invVO.setStatus(VOStatus.UPDATED);
				updateList.add(invVO);
			}
		}
		dao.saveOrUpdate(updateList);
		return new Object[] { entVO, entLineBVO };
	}
	
	/**
	 * 撤销到货
	 */
	public Object[] unArrival(EntLineBVO entLineBVO){
		if(entLineBVO == null || StringUtils.isBlank(entLineBVO.getPk_entrust())){
			throw new RuntimeException("参数不能为空！");
		}
		if(entLineBVO.getArrival_flag() == null || !entLineBVO.getArrival_flag().booleanValue()) {
			throw new RuntimeException("节点已经是非到货状态，不能反确认！");
		}
		EntrustVO entVO = this.getByPrimaryKey(EntrustVO.class, entLineBVO.getPk_entrust());
		if(StringUtils.isNotBlank(entLineBVO.getPk_segment())){
			// 非系统节点
			String[] pk_segmentAry = entLineBVO.getPk_segment().split("\\"+ Constants.SPLIT_CHAR);
			NWUtils.reverse(pk_segmentAry);// 反确认需要颠倒顺序
			// 地址标识和运段号是一一对应的
			String[] addr_flagAry = entLineBVO.getAddr_flag().split("\\" + Constants.SPLIT_CHAR);
			NWUtils.reverse(addr_flagAry);
			Map<String, SegmentVO> segMap = new HashMap<String, SegmentVO>();
			Map<String, InvoiceVO> invMap = new HashMap<String, InvoiceVO>();	
			List<SuperVO> updateList = new ArrayList<SuperVO>();
			for(int i = 0; i < pk_segmentAry.length; i++){
				String pk_segment = pk_segmentAry[i];
				String addr_flag = addr_flagAry[i];
				SegmentVO segVO = segMap.get(pk_segment);
				if(segVO == null) {
					// 先从缓存中取，满足同一条运段被多次修改的情况
					segVO = dao.queryByCondition(SegmentVO.class, "pk_segment=?", pk_segment);
					segMap.put(pk_segment, segVO);
				}
				// 校验
				checkUnconfirmValid(addr_flag, entVO, segVO, segMap);
				// 根据发货单号查询发货单
				InvoiceVO invVO = invMap.get(segVO.getInvoice_vbillno());
				if(invVO == null) {
					// 先从缓存中取，满足同一条发货单被多次修改的情况
					invVO = invoiceService.getByVbillno(segVO.getInvoice_vbillno());
					invMap.put(segVO.getInvoice_vbillno(), invVO);
				}
				int invStatus = invVO.getVbillstatus().intValue();
				if(invStatus == BillStatus.INV_BACK) {
					throw new BusiException("委托单[?]对应的发货单已经回单，不能反确认！",entVO.getVbillno());
				}
				
				PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", invVO.getPk_invoice());
				if(podVO != null) {
					// 这里其实可能发货单进行了分量，只是部分到货而已，并没有完全到货。所以pod中还不存在记录
					podVO.setStatus(VOStatus.DELETED);
					updateList.add(podVO);
				}
				invVO.setVbillstatus(BillStatus.INV_ARRIVAL);
				
				InvTrackingVO itVO = dao.queryByCondition(InvTrackingVO.class, "invoice_vbillno=? and tracking_status=?",
						invVO.getVbillno(), TrackingConst.POD);
				if(itVO != null) {
					itVO.setStatus(VOStatus.DELETED);
					updateList.add(itVO);
				}
				invVO.setStatus(VOStatus.UPDATED);
				updateList.add(invVO);
				InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", invVO.getPk_invoice());
				for(InvPackBVO invPackBVO : invPackBVOs){
					invPackBVO.setStatus(VOStatus.UPDATED);
					invPackBVO.setPod_num(null);
					updateList.add(invPackBVO);
				}
				
			}
			NWDao.getInstance().saveOrUpdate(updateList);
		}
		return unconfirmArrival(entLineBVO);
	}

	/**
	 * 查找当前运段最靠近的运段,要找的是哥哥
	 * 
	 * @param segVO
	 * @return
	 */
	private SegmentVO getPrevRecentlySegVO(SegmentVO segVO) {
		if(StringUtils.isBlank(segVO.getParent_seg())) {
			return null;
		}
		SegmentVO parentSegVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_segment=?",
				segVO.getParent_seg());
		if(parentSegVO == null) {
			return null;
		}
		if(segVO.getSeg_type() == null || segVO.getSeg_type().intValue() == SegmentConst.SECTION) {
			// 分段
			// 找自身的兄弟
			SegmentVO[] siblingSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"parent_seg=? order by vbillno", parentSegVO.getPk_segment());
			// 不可能为空，至少包括它自己
			if(siblingSegVOs == null || siblingSegVOs.length == 0) {
				return null;
			}
			for(int i = 0; i < siblingSegVOs.length; i++) {
				SegmentVO siblingSegVO = siblingSegVOs[i];
				if(siblingSegVO.getPk_segment().equals(segVO.getPk_segment())) {
					// 定位到当前运段所在的位置，找他的下一个
					if(i > 0) {
						return siblingSegVOs[i - 1];
					}
				}
			}
		}
//		else {
//			// 直接找父亲的兄弟
//			getPrevRecentlySegVO(parentSegVO);
//		}
		return getPrevRecentlySegVO(parentSegVO);
	}

	/**
	 * 查找当前运段最靠近的运段,要找的是弟弟
	 * 
	 * @param segVO
	 * @return
	 */
	private SegmentVO getNextRecentlySegVO(SegmentVO segVO) {
		if(StringUtils.isBlank(segVO.getParent_seg())) {
			return null;
		}
		SegmentVO parentSegVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_segment=?",
				segVO.getParent_seg());
		if(parentSegVO == null) {
			return null;
		}
		if(segVO.getSeg_type() == null || segVO.getSeg_type().intValue() == SegmentConst.SECTION) {
			// 分段
			// 找自身的兄弟
			SegmentVO[] siblingSegVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"parent_seg=? order by vbillno", parentSegVO.getPk_segment());
			// 不可能为空，至少包括它自己
			if(siblingSegVOs == null || siblingSegVOs.length == 0) {
				return null;
			}
			for(int i = 0; i < siblingSegVOs.length; i++) {
				SegmentVO siblingSegVO = siblingSegVOs[i];
				if(siblingSegVO.getPk_segment().equals(segVO.getPk_segment())) {
					// 定位到当前运段所在的位置，找他的下一个
					if(i < siblingSegVOs.length - 1) {
						return siblingSegVOs[i + 1];
					}
				}
			}
		} else {
			// 直接找父亲的兄弟
			getNextRecentlySegVO(parentSegVO);
		}
		return getNextRecentlySegVO(parentSegVO);
	}

	/**
	 * 校验是否可以进行节点反确认
	 * 
	 * @param entVO
	 * @param segVO
	 * @param segMap
	 *            内存里面已经处理的运段，还没有更新到数据库
	 */
	public void checkUnconfirmValid(String addr_flag, EntrustVO entVO, SegmentVO segVO, Map<String, SegmentVO> segMap) {
		Set<String> cacheKeys = segMap.keySet();
		if(AddressConst.START_ADDR_FLAG.equals(addr_flag)) {
			if(segVO.getVbillstatus().intValue() != BillStatus.SEG_DELIVERY) {
				throw new BusiException("节点是运段[?]的起始地，该运段必须是[已提货]状态才能反确认！",segVO.getVbillno());
			}
		} else if(AddressConst.END_ADDR_FLAG.equals(addr_flag)) {
			// 是运段的目的地
			SegmentVO recentlySegVO = getNextRecentlySegVO(segVO);
			if(recentlySegVO == null) {
				return;
			}
			SegmentVO[] childVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"vbillno like '" + recentlySegVO.getVbillno() + "%' and seg_mark <> ?",
					SegmentConst.SEG_MARK_PARENT);
			if(childVOs != null && childVOs.length > 0) {
				for(SegmentVO childVO : childVOs) {
					if(cacheKeys.contains(childVO.getPk_segment())) {
						childVO = segMap.get(childVO.getPk_segment());
					}
					if(childVO.getVbillstatus() != BillStatus.SEG_DISPATCH
							&& childVO.getVbillstatus() != BillStatus.SEG_WPLAN) {
						throw new BusiException("节点是运段[?]的目的地，该运段的子运段[?]不是[待调度、已调度]状态，不能反确认！",segVO.getVbillno(),childVO.getVbillno());
					}
				}
			}
		}
	}

	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		super.processAfterExecFormula(list, paramVO, orderBy);
		Set<String> pk_customers = new HashSet<String>();
		Set<String> pk_entrusts = new HashSet<String>();

		CustomerVO[] customerVOs = null;
		List<Map<String,Object>> transList = null;
		for (Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			if (pk_customer != null) {
				String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
				pk_customers.addAll(Arrays.asList(pks));
			}
			Object pk_entrust = map.get("pk_entrust");
			if (pk_entrust != null) {
				pk_entrusts.add(pk_entrust.toString());
			}
		}
		if (pk_customers != null && pk_customers.size() > 0) {
			customerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, " pk_customer in "
					+ NWUtils.buildConditionString(pk_customers.toArray(new String[pk_customers.size()])));
		}
		if (pk_entrusts != null && pk_entrusts.size() > 0) {
			String sql = "SELECT trans.pk_entrust,trans.pk_driver,trans.carno,dri.driver_name "
					+ "FROM ts_entrust ent WITH(nolock) LEFT JOIN ts_ent_transbility_b trans WITH(nolock) ON ent.pk_entrust = trans.pk_entrust "
					+ "LEFT JOIN ts_driver dri WITH(nolock) ON trans.pk_driver = dri.driver_code "
					+ "WHERE isnull(ent.dr,0)=0 AND isnull(trans.dr,0)=0 AND isnull(dri.dr,0)=0 "
					+ "AND ent.pk_entrust IN ";
			if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
				sql = "SELECT trans.pk_entrust,trans.pk_driver,trans.carno,dri.driver_name "
						+ "FROM ts_entrust ent  LEFT JOIN ts_ent_transbility_b trans  ON ent.pk_entrust = trans.pk_entrust "
						+ "LEFT JOIN ts_driver dri  ON trans.pk_driver = dri.driver_code "
						+ "WHERE nvl(ent.dr,0)=0 AND nvl(trans.dr,0)=0 AND nvl(dri.dr,0)=0 "
						+ "AND ent.pk_entrust IN ";
			}
			sql +=	NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()]));
			transList = NWDao.getInstance().queryForList(sql);
		}
		for (Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			StringBuffer cust_name = new StringBuffer();
			StringBuffer carno_name = new StringBuffer();
			StringBuffer driver_name = new StringBuffer();
			StringBuffer pk_driver = new StringBuffer();
			if (pk_customer != null) {
				String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
				if (pks != null && pks.length > 0 && customerVOs != null && customerVOs.length > 0) {
					for (String pk : pks) {
						for (CustomerVO customerVO : customerVOs) {
							if (pk.equals(customerVO.getPk_customer())) {
								cust_name.append(customerVO.getCust_name()).append(Constants.SPLIT_CHAR);
							}
						}
						if(StringUtils.isNotBlank(cust_name.toString())){
							String cust_name1 = cust_name.substring(0, cust_name.length() - 1);
							map.put("cust_name", cust_name1);
						}
					}
				}
			}
			Object pk_entrust = map.get("pk_entrust");
			if (pk_entrust != null) {
				if(transList != null && transList.size() > 0){
					for(Map<String,Object> trans : transList){
						//将司机PK，司机姓名，车牌号，拼接在一起显示。
						if(pk_entrust.equals(String.valueOf(trans.get("pk_entrust")))){
							if(trans.get("pk_driver") != null && !"null".equalsIgnoreCase(trans.get("pk_driver").toString())){
								pk_driver.append(trans.get("pk_driver").toString()).append(Constants.SPLIT_CHAR);
							}
							if(trans.get("carno") != null && !"null".equalsIgnoreCase(trans.get("carno").toString())){
								carno_name.append(trans.get("carno").toString()).append(Constants.SPLIT_CHAR);
							}
							if(trans.get("driver_name") != null && !"null".equalsIgnoreCase(trans.get("driver_name").toString())){
								driver_name.append(trans.get("driver_name").toString()).append(Constants.SPLIT_CHAR);
							}
						}
					}
					if(StringUtils.isNotBlank(pk_driver.toString())){
						String pk_driverStr = pk_driver.substring(0, pk_driver.length() - 1);
						map.put("pk_driver", pk_driverStr);
					}
					if(StringUtils.isNotBlank(carno_name.toString())){
						String carno_nameStr = carno_name.substring(0, carno_name.length() - 1);
						map.put("carno_name", carno_nameStr);
					}
					if(StringUtils.isNotBlank(driver_name.toString())){
						String driver_nameStr = driver_name.substring(0, driver_name.length() - 1);
						map.put("driver_name", driver_nameStr);
					}
					
				}
			}

		}
	}
	
	public PaginationVO loadEntTracking(String entrust_vbillno, int offset, int pageSize) {
		if(StringUtils.isBlank(entrust_vbillno)) {
			return null;
		}
		String sql = "select * from ts_ent_tracking WITH(NOLOCK) where entrust_vbillno=? and isnull(dr,0)=0 order by tracking_time desc";
		PaginationVO pageVO = NWDao.getInstance().queryBySqlWithPaging(sql, EntTrackingVO.class, offset, pageSize,
				entrust_vbillno);
		return pageVO;
	}


	public void deleteEntTracking(String pk_ent_tracking, String last_pk) {
		logger.info("删除跟踪信息，pk_ent_tracking：" + pk_ent_tracking);
		if(StringUtils.isBlank(pk_ent_tracking)) {
			throw new RuntimeException("删除时主键不能为空！");
		}
		EntTrackingVO etVO = dao.queryByCondition(EntTrackingVO.class, "pk_ent_tracking=?", pk_ent_tracking);
		if(etVO == null) {
			throw new RuntimeException("该记录已经被删除，请刷新页面！");
		}
		dao.delete(etVO);

		// 相关的委托单
		EntrustVO entVO = dao.queryByCondition(EntrustVO.class, "vbillno=?", etVO.getEntrust_vbillno());

		EntTrackingVO lastEtVO = null;
		if(StringUtils.isNotBlank(last_pk)) {// 最后一条跟踪记录
			lastEtVO = dao.queryByCondition(EntTrackingVO.class, "pk_ent_tracking=?", last_pk);
		}

		// 查询委托单关联的所有发货单
		String sql = "select pk_invoice from ts_ent_inv_b WITH(NOLOCK) where pk_entrust=(select pk_entrust from ts_entrust where vbillno=?)";
		List<String> PKs = NWDao.getInstance().queryForList(sql, String.class, etVO.getEntrust_vbillno());
		if(PKs != null && PKs.size() > 0) {
			String condString = NWUtils.buildConditionString(PKs.toArray(new String[PKs.size()]));
			sql = "select * from ts_invoice WITH(NOLOCK) where pk_invoice in " + condString;
			List<InvoiceVO> invVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class);
			if(invVOs != null && invVOs.size() > 0) {
				for(InvoiceVO invVO : invVOs) {
					// 同步发货单的跟踪状态
					EntrustUtils.syncInvoiceTrackingInfo(lastEtVO, invVO, false);
					invVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(invVO);
				}
			}
		}
		// 同步委托单的跟踪状态
		EntrustUtils.syncEntrustTrackingInfo(lastEtVO, entVO, false);
		entVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(entVO);
	}

	/**
	 * 保存跟踪信息
	 */
	public Map<String, Object> saveEntTracking(EntTrackingVO etVO, EntrustVO entVO, ExpAccidentVO eaVO,
			String[] pk_filesystems, String[] invoiceVbillnoAry) {
		logger.info("保存一条委托单跟踪信息...");
		if(etVO == null) {
			return null;
		}
		String tracking_limit = ParameterHelper.getSaveTrackingLimit();
		if(StringUtils.isBlank(tracking_limit)){
			tracking_limit = "已确认,已提货,已到货";
		}
		//解析状态
		String[] limits = tracking_limit.split(",");
		Integer[] statusLimits = new Integer[limits.length];
		for(int i=0;i<limits.length;i++){
			if(limits[i].equals("待确认")){
				statusLimits[i] = BillStatus.ENT_UNCONFIRM;
			}else if(limits[i].equals("已确认")){
				statusLimits[i] = BillStatus.ENT_CONFIRM;
			}else if(limits[i].equals("已提货")){
				statusLimits[i] = BillStatus.ENT_DELIVERY;
			}else if(limits[i].equals("已到货")){
				statusLimits[i] = BillStatus.ENT_ARRIVAL;
			}else if(limits[i].equals("退单")){
				statusLimits[i] = BillStatus.ENT_VENT;
			}else{
				throw new BusiException("系统参数[save_tracking_limit]维护错误[?]！",limits[i]);
			}
		}
		boolean flag = false;
		for(Integer statusLimit : statusLimits){
			if(statusLimit.equals(entVO.getVbillstatus())){
				flag = true;
				break;
			}
		}
		if (!flag) {
			//增加一个判断，当登录用户是RF用户时，不产生异常，而是将错误信息返回给controller。
			if(WebUtils.getLoginInfo().getUser_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getUser_type()){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("msg", "委托单必须是[" + tracking_limit + "]状态下才能保存跟踪信息！");
				return map;
			}else{
				throw new BusiException("委托单必须是[?]状态下才能保存跟踪信息！",tracking_limit);
			}
		}
//		if(entVO.getVbillstatus() != BillStatus.ENT_CONFIRM 
//				&& entVO.getVbillstatus() != BillStatus.ENT_DELIVERY
//				&& entVO.getVbillstatus() != BillStatus.ENT_ARRIVAL) {
//			//增加一个判断，当登录用户是RF用户时，不产生异常，而是将错误信息返回给controller。
//			if(WebUtils.getLoginInfo().getUser_type() != null && DataDictConst.PLATFORM_TYPE.RF.intValue() == WebUtils.getLoginInfo().getUser_type()){
//				Map<String, Object> map = new HashMap<String, Object>();
//				map.put("msg", "委托单必须是[已确认、已提货、已到货]状态下才能保存跟踪信息！");
//				return map;
//			}else{
//				throw new BusiException("委托单必须是[已确认、已提货]状态下才能保存跟踪信息！");
//			}
//		}

		if(etVO.getTracking_time() != null && etVO.getTracking_time().after(new UFDateTime(new Date()))) {
			throw new BusiException("跟踪时间不能大于当前时间！");
		}
		String entrust_vbillno = etVO.getEntrust_vbillno();
		if(StringUtils.isNotBlank(entVO.getVbillno())) {
			entrust_vbillno = entVO.getVbillno();
		}
		etVO.setEntrust_vbillno(entrust_vbillno);
		etVO.setLot(entVO.getLot());
		etVO.setStatus(VOStatus.NEW);
		//yaojiie 2015 12 18 将Pk_ent_tracking置空，避免跟踪时，违反主键约束
		etVO.setPk_ent_tracking(null);
		NWDao.setUuidPrimaryKey(etVO);
		etVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		etVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		etVO.setCreate_time(new UFDateTime(new Date()));

		EntrustVO oriEntVO = dao.queryByCondition(EntrustVO.class, "vbillno=?", entrust_vbillno);
		oriEntVO.setStatus(VOStatus.UPDATED);
		oriEntVO.setMainno(entVO.getMainno());// 主单号
		oriEntVO.setFlightno(entVO.getFlightno());// 航班号
		oriEntVO.setPk_flight(entVO.getPk_flight());// 航班
		oriEntVO.setFlight_time(entVO.getFlight_time());// 航班时间

		// 设置发货单号
		etVO.setInvoice_vbillno(oriEntVO.getInvoice_vbillno());
		dao.saveOrUpdate(etVO);

		// 保存异常表的时候，将异常信息也保存到委托单主表
		EntrustUtils.syncEntrustTrackingInfo(etVO, oriEntVO, true);
		dao.saveOrUpdate(oriEntVO);

		// 如果存在异常，那么记录异常信息
		if(etVO.getExp_flag() != null && etVO.getExp_flag().booleanValue()) {
			if(eaVO == null) {
				eaVO = new ExpAccidentVO();
			}
			if(eaVO.getDbilldate() == null) {
				eaVO.setDbilldate(new UFDate());
			}
			eaVO.setOrigin(ExpAccidentOrgin.TRACKING.toString());// /设置异常的来源是“异常跟踪”
			eaVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			eaVO.setCreate_time(new UFDateTime(new Date()));
			eaVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			//将批次号也写到异常事故表里面
			eaVO.setLot(oriEntVO.getLot());
			ParamVO paramVO = new ParamVO();
			paramVO.setFunCode(FunConst.EXP_ACCIDENT_CODE);
			expAccidentService.setCodeField(eaVO, paramVO);
			eaVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(eaVO);
			dao.saveOrUpdate(eaVO);
		}
		// 如果存在附件pk，那么更新
		if(pk_filesystems != null && pk_filesystems.length > 0) {
			String cond = NWUtils.buildConditionString(pk_filesystems);
			if(StringUtils.isNotBlank(cond)){
				String sql = "update ts_attachment set pk_billno=? where pk_attachment in " + cond;
				NWDao.getInstance().update(sql, eaVO.getPk_exp_accident());
			}
		}
		// 如果勾选了同步客户跟踪信息
		if(etVO.getSync_flag() != null && etVO.getSync_flag().booleanValue()) {
			List<InvoiceVO> invVOs = null;
			if(StringUtils.isNotBlank(etVO.getInvoice_vbillno())) {
				// 同时向发货单插入一条跟踪记录
				String[] invoice_vbillno_ary = etVO.getInvoice_vbillno().split(",");
				String cond = NWUtils.buildConditionString(invoice_vbillno_ary);
				invVOs = dao.queryForList("select * from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and vbillno in" + cond,
						InvoiceVO.class);
			} else {
				// 同时向委托单关联的所有发货单插入跟踪记录
				String sql = "select * from ts_invoice WITH(NOLOCK) where pk_invoice in "
						+ "(select pk_invoice from ts_ent_inv_b WITH(NOLOCK) where pk_entrust=(select pk_entrust from ts_entrust WITH(NOLOCK) where vbillno=?))";
				invVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, etVO.getEntrust_vbillno());
			}

			// 读取参数
			int tracking_invoice_status = ParameterHelper.getIntParam("tracking_invoice_status");
			for(InvoiceVO invVO : invVOs) {
				if(tracking_invoice_status == BillStatus.INV_SIGN) {
					// 等于已签收
					if(invVO.getVbillstatus() == BillStatus.INV_SIGN || invVO.getVbillstatus() == BillStatus.INV_BACK) {
						// 如果是签收或者回单，那么不记录跟踪了
						continue;
					}
				} else if(tracking_invoice_status == BillStatus.INV_ARRIVAL) {
					// 已到货
					if(invVO.getVbillstatus() == BillStatus.INV_ARRIVAL
							|| invVO.getVbillstatus() == BillStatus.INV_SIGN
							|| invVO.getVbillstatus() == BillStatus.INV_BACK) {
						// 如果是已到货，签收或者回单，那么不记录跟踪了
						continue;
					}
				}
				if(invoiceVbillnoAry != null && invoiceVbillnoAry.length > 0) {
					boolean exist = false;
					for(int i = 0; i < invoiceVbillnoAry.length; i++) {
						if(invoiceVbillnoAry[i].equals(invVO.getVbillno())) {
							exist = true;
							break;
						}
					}
					if(!exist) {// 不是所选的发货单
						continue;
					}
				}

				InvTrackingVO itVO = new InvTrackingVO();
				itVO.setInvoice_vbillno(invVO.getVbillno());
				itVO.setEntrust_vbillno(etVO.getEntrust_vbillno());
				itVO.setTracking_status(etVO.getTracking_status());
				itVO.setTracking_time(etVO.getTracking_time());
				itVO.setTracking_memo(oriEntVO.getTracking_memo());// 已经结构化的信息
				itVO.setExp_flag(etVO.getExp_flag());
				itVO.setExp_type(etVO.getExp_type());
				itVO.setExp_memo(etVO.getExp_memo());
				itVO.setPk_corp(etVO.getPk_corp());
				itVO.setCreate_user(etVO.getCreate_user());
				itVO.setCreate_time(etVO.getCreate_time());
				itVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(itVO);
				NWDao.getInstance().saveOrUpdate(itVO);

				EntrustUtils.syncInvoiceTrackingInfo(etVO, invVO, false);
				invVO.setStatus(VOStatus.UPDATED);
				dao.saveOrUpdate(invVO);
			}
		}
		// 看是否需要执行公式了
		Map<String, Object> map = new HashMap<String, Object>();
		String[] attrs = etVO.getAttributeNames();
		for(String key : attrs) {
			map.put(key, etVO.getAttributeValue(key));
		}
		// Object tracking_status = map.get(EntTrackingVO.TRACKING_STATUS);
		// if(tracking_status != null) {
		// map.put(EntTrackingVO.TRACKING_STATUS,
		// getTrackingStatusName(tracking_status.toString()));
		// }
		// Object exp_type = map.get(EntTrackingVO.EXP_TYPE);
		// if(exp_type != null) {
		// map.put(EntTrackingVO.EXP_TYPE, getExpTypeName(exp_type.toString()));
		// }
		return map;
	}

	public Map<String, Object> saveEntTracking2(EntTrackingVO etVO, EntrustVO entVO, ExpAccidentVO eaVO,
			List<FilesystemVO> attachVOs, List<InputStream> inAry) {
		logger.info("保存委托单跟踪信息，包含附件信息...");
		if(etVO.getTracking_time() != null && etVO.getTracking_time().after(new UFDateTime(new Date()))) {
			throw new BusiException("跟踪时间不能大于当前时间！");
		}
		Map<String, Object> retMap = saveEntTracking(etVO, entVO, eaVO, null, null);
		// 保存附件
		if(attachVOs != null && attachVOs.size() > 0) {
			String billtype = expAccidentService.getBillType();
			String pk_bill = eaVO.getPk_exp_accident();
			for(FilesystemVO attachVO : attachVOs) {
				attachVO.setBilltype(billtype);
				attachVO.setPk_bill(pk_bill);
			}
			filesystemService.upload(attachVOs, inAry);
		}
		return retMap;
	}

	public Map<String, Object> batchSaveEntTracking(EntTrackingVO etVO, String[] vbillnoAry) {
		logger.info("批量保存跟踪信息...");
		if(vbillnoAry == null || vbillnoAry.length == 0) {
			return null;
		}
		for(String vbillno : vbillnoAry) {
			EntrustVO entVO = (EntrustVO) this.getByCode(vbillno);
			// 只有已提货和已到达的单据才能增加跟踪记录
			// if(entVO.getVbillstatus().intValue() == BillStatus.ENT_DELIVERY
			// || entVO.getVbillstatus().intValue() == BillStatus.ENT_ARRIVAL) {
			// 2015-04-29 在saveEntTracking中已经做了限制，[已确认、已提货]的委托单才能做异常跟踪
			this.saveEntTracking(etVO, entVO, null, null, null);
			// }
		}
		return null;
	}

	private LbsApiVO lbsApiVO = null;

	private static SAXReader getSAXReaderInstance() {
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		saxReader.setIgnoreComments(true);
		return saxReader;
	}

	/**
	 * 实例化配置文件
	 */
	@SuppressWarnings("rawtypes")
	protected void parse() throws Exception {
		String path = WebUtils.getClientConfigPath() + java.io.File.separator + "lbs.xml";
		File file = new File(path);

		Reader reader = null;
		if(!file.exists()) {
			throw new BusiException("[?]文件不存在!",path);
		}
		LbsApiVO apiVO = new LbsApiVO();
		reader = new InputStreamReader(new FileInputStream(file));
		SAXReader saxReader = getSAXReaderInstance();
		Document doc = saxReader.read(reader);

		Node host = doc.selectSingleNode("/lbs/api/host");
		apiVO.setHost(host.getText() == null ? "" : host.getText().trim());

		Node uid = doc.selectSingleNode("/lbs/api/uid");
		apiVO.setUid(uid.getText() == null ? "" : uid.getText().trim());

		Node pwd = doc.selectSingleNode("/lbs/api/pwd");
		apiVO.setPwd(pwd.getText() == null ? "" : pwd.getText().trim());

		Map<String, String> methodMap = new HashMap<String, String>();
		List list = doc.selectNodes("/lbs/api/methods/method");
		if(list != null && list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Node node = (Node) list.get(i);
				Node key = node.selectSingleNode("key");
				Node url = node.selectSingleNode("url");

				methodMap.put(key.getText().trim(), url.getText() == null ? "" : url.getText());
			}

		}
		apiVO.setMethodMap(methodMap);

		lbsApiVO = apiVO;
	}

	public RootVO getTrackVOs(String pk_entrust, String gps_id) {
		if(StringUtils.isBlank(pk_entrust) || StringUtils.isBlank(gps_id)) {
			return null;
		}
		EntrustVO entVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", pk_entrust);
		if(entVO == null) {
			throw new BusiException("委托单已经被删除，pk_entrust[?]！",pk_entrust);
		}
		String act_deli_date = entVO.getAct_deli_date();
		if(StringUtils.isBlank(act_deli_date)) {
			throw new BusiException("委托单[?]没有实际提货时间！",entVO.getVbillno());
		}

		if(lbsApiVO == null) {
			try {
				parse();
			} catch(Exception e) {
				throw new BusiException("解析配置文件时出错，错误信息[?]！",e.getMessage());
			}
		}
		if(lbsApiVO == null) {
			return null;
		}
		logger.info("----------------从LBS系统读取GPS设备的位置信息,开始--------------------");

		Map<String, String> methodMap = lbsApiVO.getMethodMap();
		StringBuffer url = new StringBuffer();
		url.append(lbsApiVO.getHost());
		url.append(methodMap.get("getTrackInfoByGpsID"));
		logger.info("参数信息如下：");
		logger.info("URL：" + url.toString());
		logger.info("设备号：" + gps_id);
		logger.info("实际提货时间：" + act_deli_date);
		String endDate = new UFDateTime(new Date()).toString();
		logger.info("当前服务器时间：" + endDate);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("gpsID", gps_id);
		paramMap.put("startDate", act_deli_date);
		paramMap.put("endDate", endDate);
		paramMap.put("uid", lbsApiVO.getUid());
		paramMap.put("pwd", lbsApiVO.getPwd());
		try {
			String xmlText = HttpUtils.post(url.toString(), paramMap);
			logger.info("请求结果：" + xmlText);
			TrackInfoConverter converter = new TrackInfoConverter();
			logger.info("----------------从LBS系统读取GPS设备的位置信息,结束--------------------");
			return converter.convertResponse(xmlText);
		} catch(java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			throw new BusiException("请求LBS数据时出错,请求地址不存在,URL[?]！",url.toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw new BusiException("请求LBS数据时出错,错误信息[?]！",e.getMessage());
		}
	}

	public RootVO getCurrentTrackVO(String pk_entrust, String carno,String pk_driver) {
		if(StringUtils.isBlank(pk_entrust) || (StringUtils.isBlank(carno) && StringUtils.isBlank(pk_driver))) {
			return null;
		}
		EntrustVO entVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", pk_entrust);
		if(entVO == null) {
			throw new BusiException("委托单已经被删除，pk_entrust[?]！",pk_entrust);
		}
		String gps_id = "";
		if(StringUtils.isNotBlank(carno)){
			String sql = "select gps_id from ts_ent_transbility_b WITH(NOLOCK) where  isnull(dr,0)=0 and pk_entrust=? and carno=?";
			List<String> gpsIDList = NWDao.getInstance().queryForList(sql, String.class, pk_entrust, carno);
			if(gpsIDList == null || gpsIDList.size() == 0) {
				throw new BusiException("委托单号[?]，车牌号[?]的车辆没有关联GPS_ID！",entVO.getVbillno(),carno);
			}
			if(gpsIDList.size() > 1) {
				throw new BusiException("委托单号[?]，车牌号[?]的车辆关联了多个GPS_ID！",entVO.getVbillno(),carno);
			}
			gps_id = gpsIDList.get(0);
		}else{
			gps_id = pk_driver;
		}
		if(lbsApiVO == null) {
			try {
				parse();
			} catch(Exception e) {
				throw new BusiException("解析配置文件时出错，错误信息[?]！",e.getMessage());
			}
		}
		if(lbsApiVO == null) {
			return null;
		}
		logger.info("----------------从LBS系统读取GPS设备的当前位置信息,开始--------------------");
		Map<String, String> methodMap = lbsApiVO.getMethodMap();
		StringBuffer url = new StringBuffer();
		url.append(lbsApiVO.getHost());
		url.append(methodMap.get("getPositionByGpsID"));
		logger.info("参数信息如下：");
		logger.info("URL：" + url.toString());
		logger.info("设备号：" + gps_id);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("gpsID", gps_id);
		paramMap.put("uid", lbsApiVO.getUid());
		paramMap.put("pwd", lbsApiVO.getPwd());
		try {
			String xmlText = HttpUtils.post(url.toString(), paramMap);
			logger.info("请求结果：" + xmlText);
			TrackInfoConverter converter = new TrackInfoConverter();
			logger.info("----------------从LBS系统读取GPS设备的当前位置信息,结束--------------------");
			return converter.convertResponse(xmlText);
		} catch(java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			throw new BusiException("请求LBS数据时出错,请求地址不存在,URL[?]！",url.toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw new BusiException("请求LBS数据时出错,错误信息[?]！",e.getMessage());
		}
	}

	public List<Map<String, Object>> loadTrackingInvoice(String entrust_vbillno) {
		if(StringUtils.isBlank(entrust_vbillno)) {
			return null;
		}
		String statusCond = "1=1";
		// 读取系统参数配置的可同步的发货单状态
		int tracking_invoice_status = ParameterHelper.getIntParam("tracking_invoice_status");
		if(tracking_invoice_status == BillStatus.INV_SIGN) {
			// 等于已签收
			statusCond = "vbillstatus <> 4 and vbillstatus <> 6";
		} else if(tracking_invoice_status == BillStatus.INV_ARRIVAL) {
			// 已到货
			statusCond = "vbillstatus <> 3 and vbillstatus <> 4 and vbillstatus <> 6";
		}

		String sql = "select * from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and "
				+ "pk_invoice in (select pk_invoice from ts_ent_inv_b where isnull(dr,0)=0 and "
				+ "pk_entrust=(select pk_entrust from ts_entrust where vbillno=? and isnull(dr,0)=0)) and "
				+ statusCond;
		List<InvoiceVO> invVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, entrust_vbillno);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(invVOs.size());
		for(InvoiceVO item : invVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			SuperVO vo = (SuperVO) item;
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				Object obj = vo.getAttributeValue(key);
				if(obj != null) {
					map.put(key, SecurityUtils.escape(obj.toString()));
				} else {
					map.put(key, obj);
				}
			}
			mapList.add(map);
		}
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVOByBilltypecode("ts_te_trin");
		return execFormula4Templet(mapList, uiBillTempletVO, false, new String[] { "ts_invoice" }, null);
	}

	
	/**
	 * 如果存在数据修改的情况：
	 * 1，修改运段信息和委托单信息，将修改的部分货物，重新退回给运段
	 * 2，修改委托单，运段起点，运段终点的货物明细
	 * @param entVO 委托单
	 * @param entLinePackBVOs 更新后的包装信息
	 * @param entLinePackBVOs_db 数据库的包装信息
	 */
	public void processSegmengAndEntrust(EntLineBVO entLineBVO){
		EntrustVO entVO = this.getByPrimaryKey(EntrustVO.class, entLineBVO.getPk_entrust());
		if(entVO.getVbillstatus() != BillStatus.ENT_CONFIRM
				&& entVO.getVbillstatus() != BillStatus.ENT_DELIVERY){
			throw new BusiException("委托单必须是[已确认、已提货]状态下才能保存跟踪信息！");
		}
		List<EntLinePackBVO> entLinePackBVOs = entLineBVO.getEntLinePackBVOs();
		
		List<String> pks = new ArrayList<String>();
		if(entLinePackBVOs == null || entLinePackBVOs.size() == 0){
			return;
		}
		for(EntLinePackBVO vo: entLinePackBVOs){
			pks.add(vo.getPk_ent_line_b());
		}
		
		//判断为空返回 
		if(pks.size() == 0){
			return;
		}
		String sql = "SELECT pk_ent_line_pack_b, pk_entrust, pk_ent_line_b,pk_ent_pack_b, dr, ts, serialno, pk_goods, goods_code, goods_name, num, pack, weight, volume, unit_weight, unit_volume,"
				+ " length, width, height, trans_note, low_temp, hight_temp, reference_no, memo, pack_num_count, plan_num, plan_pack_num_count, def1, def2, def3, def4, def5, def6,"
				+ " def7, def8, def9, def10, def11, def12 FROM ts_ent_line_pack_b WITH(NOLOCK) where dr =0 and pk_ent_line_b in ";
		String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
		
		sql = sql + cond;
		List<EntLinePackBVO> entLinePackBVOs_db = dao.queryForList(sql, EntLinePackBVO.class);
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> entPackbPks = new ArrayList<String>();
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			entPackbPks.add(vo_db.getPk_ent_pack_b());
		}
		//判断是否有货物修改情况
		boolean goodsModified = false;
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			for(EntLinePackBVO vo : entLinePackBVOs){
				if(vo.getPk_ent_line_pack_b().equals(vo_db.getPk_ent_line_pack_b())){
					if(!vo.getNum().equals(vo_db.getNum())){
						goodsModified = true;
						break;
					}
				}
			}
			if(goodsModified){
				break;
			}
		}
		String packb_sql = "SELECT  telp.* FROM ts_ent_line_b s WITH(NOLOCK) "
				+ " LEFT JOIN ts_ent_line_b e  WITH(NOLOCK) ON s.pk_entrust=e.pk_entrust "
				+ " LEFT JOIN ts_ent_line_pack_b telp  WITH(NOLOCK) ON e.pk_ent_line_b=telp.pk_ent_line_b "
				+ " WHERE isnull(s.dr,0)=0 AND isnull(e.dr,0)=0 AND isnull(telp.dr,0)=0 AND s.pk_ent_line_b=? AND e.addr_flag='E'";
		List<EntLinePackBVO> entLinePackBVOs_db_e = NWDao.getInstance().queryForList(packb_sql, EntLinePackBVO.class, entLinePackBVOs_db.get(0).getPk_ent_line_b());
		EntLineBVO entLineBVO_e = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_entrust=? and addr_flag='E'", entVO.getPk_entrust());
		EntPackBVO[] entPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class, 
				"pk_ent_pack_b in " + NWUtils.buildConditionString(entPackbPks));
		//没有货物修改
		if(!goodsModified){
			//没有短装，但是这时候需要把界面的数据和数据库的数据进行对比，看看是否有改动，如果有，需要把界面的数据的数据保存到数据库。
			//这段代码写在这里的原因是因为，下面代码写的很烂，尽量不要改动，会很麻烦。
			boolean flag = false;
			for(EntLinePackBVO vo_db : entLinePackBVOs_db){
				for(EntLinePackBVO vo : entLinePackBVOs){
					if(vo.getPk_ent_line_pack_b().equals(vo_db.getPk_ent_line_pack_b())){
						if(!vo.getWeight().equals(vo_db.getWeight())){
							flag = true;
						}
						if(!vo.getVolume().equals(vo_db.getVolume())){
							flag = true;
						}
						if(!(vo.getPack_num_count() == null ? UFDouble.ZERO_DBL : vo.getPack_num_count()).equals(vo_db.getPack_num_count() == null ? UFDouble.ZERO_DBL : vo_db.getPack_num_count())){
							flag = true;
						}
						if(flag){
							//将界面的重量体积等信息，放入数据库里
							vo_db.setPack_num_count(vo.getPack_num_count());
							vo_db.setWeight(vo.getWeight());
							vo_db.setVolume(vo.getVolume());
							vo_db.setStatus(VOStatus.UPDATED);
							toBeUpdate.add(vo_db);
							break;
						}
					}
				}
				if(flag){//因为vo_db的数据已经被修改过了，所以直接用就行了。
					//修改到货点数据
					for(EntLinePackBVO vo_db_e : entLinePackBVOs_db_e){
						if(vo_db.getPk_ent_pack_b().equals(vo_db_e.getPk_ent_pack_b())){
							vo_db_e.setPack_num_count(vo_db.getPack_num_count());
							vo_db_e.setWeight(vo_db.getWeight());
							vo_db_e.setVolume(vo_db.getVolume());
							vo_db_e.setStatus(VOStatus.UPDATED);
							toBeUpdate.add(vo_db_e);
							break;
						}
					}
					//修改委托单包装数据
					for(EntPackBVO entPackBVO : entPackBVOs){
						if(entPackBVO.getPk_ent_pack_b().equals(vo_db.getPk_ent_pack_b())){
							entPackBVO.setPack_num_count(vo_db.getPack_num_count());
							entPackBVO.setWeight(vo_db.getWeight());
							entPackBVO.setVolume(vo_db.getVolume());
							entPackBVO.setStatus(VOStatus.UPDATED);
							toBeUpdate.add(entPackBVO);
							break;
						}
					}
				}
			}
			//数量重量体积发生修改过
			if(flag){
				//统计委托单信息
				entVO.setNum_count(0);
				entVO.setWeight_count(UFDouble.ZERO_DBL);
				entVO.setVolume_count(UFDouble.ZERO_DBL);
				for(EntPackBVO entPackBVO : entPackBVOs){
					entVO.setNum_count(entVO.getNum_count() + entPackBVO.getNum());
					entVO.setWeight_count(entVO.getWeight_count().add(entPackBVO.getWeight() == null ? UFDouble.ZERO_DBL : entPackBVO.getWeight()));
					entVO.setVolume_count(entVO.getVolume_count().add(entPackBVO.getVolume() == null ? UFDouble.ZERO_DBL : entPackBVO.getVolume()));
				}
				UFDouble rate = carrService.getFeeRate(entVO.getPk_carrier(), entVO.getPk_trans_type(), entVO.getDeli_city(), entVO.getArri_city());
				if(rate != null && rate.doubleValue() != 0){
					UFDouble volume_weight_count = entVO.getVolume_count().multiply(rate);
					UFDouble fee_weight_count = new UFDouble();
					if(volume_weight_count.doubleValue() < entVO.getWeight_count().doubleValue()) {
						fee_weight_count = entVO.getWeight_count();
					} else {
						fee_weight_count = volume_weight_count;
					}
					entVO.setFee_weight_count(fee_weight_count);
					entVO.setVolume_weight_count(volume_weight_count);
				}
				
				PayDetailVO payDetailVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "entrust_vbillno=?", entVO.getVbillno());
				if(payDetailVO.getVbillstatus() != BillStatus.NEW){
					throw new BusiException("应付明细不是[新建]，不能修改货品信息！");
				}
				entVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(entVO);
				payDetailVO.setStatus(VOStatus.UPDATED);
				payDetailVO.setNum_count(entVO.getNum_count());
				payDetailVO.setWeight_count(entVO.getWeight_count());
				payDetailVO.setVolume_count(entVO.getVolume_count());
				payDetailVO.setFee_weight_count(entVO.getFee_weight_count());
				toBeUpdate.add(payDetailVO);
				NWDao.getInstance().saveOrUpdate(toBeUpdate);
			}
			return;
		}
		
		SegmentVO segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "vbillno=?", entVO.getSegment_vbillno());
		if(segmentVO == null){
			return;
		}
		SegPackBVO[] segPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?", segmentVO.getPk_segment());
		SegmentVO segmentVO1 = segmentVO.clone();
		SegmentVO segmentVO2 = segmentVO.clone();
		//将原有运段设为父辈运段
		segmentVO.setSeg_mark(SegmentConst.SEG_MARK_PARENT);
		segmentVO.setVbillstatus(BillStatus.SEG_WPLAN);
		segmentVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(segmentVO);
		//委托单对应的运段
		segmentVO1.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
		segmentVO1.setSeg_type(SegmentConst.QUANTITY);
		segmentVO1.setNum_count(0);
		segmentVO1.setWeight_count(UFDouble.ZERO_DBL);
		segmentVO1.setVolume_count(UFDouble.ZERO_DBL);
		segmentVO1.setPk_segment(null);
		segmentVO1.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(segmentVO1);
		segmentVO1.setParent_seg(segmentVO.getPk_segment());
		segmentVO1.setVbillno(segmentVO.getVbillno() + "-001");
		toBeUpdate.add(segmentVO1);
		//额外产生的运段（短装运段）
		segmentVO2.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
		segmentVO2.setSeg_type(SegmentConst.QUANTITY);
		segmentVO2.setPk_segment(null);
		segmentVO2.setStatus(VOStatus.NEW);
		segmentVO2.setVbillstatus(BillStatus.SEG_WPLAN);
		segmentVO2.setNum_count(0);
		segmentVO2.setWeight_count(UFDouble.ZERO_DBL);
		segmentVO2.setVolume_count(UFDouble.ZERO_DBL);
		segmentVO2.setCreate_user(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		segmentVO2.setCreate_time(new UFDateTime(new Date()));
		segmentVO2.setFee_weight_count(UFDouble.ZERO_DBL);
		segmentVO2.setNew_segment(UFBoolean.TRUE);
		NWDao.setUuidPrimaryKey(segmentVO2);
		segmentVO2.setParent_seg(segmentVO.getPk_segment());
		segmentVO2.setVbillno(segmentVO.getVbillno() + "-002");
		toBeUpdate.add(segmentVO2);
		EntSegBVO entSegBVO = NWDao.getInstance().queryByCondition(EntSegBVO.class, 
				"pk_entrust=? and pk_segment=?", entVO.getPk_entrust(),segmentVO.getPk_segment());
		entSegBVO.setPk_segment(segmentVO1.getPk_segment());
		entSegBVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(entSegBVO);
		//修改委托单字段
		entVO.setSegment_vbillno(segmentVO1.getVbillno());
		entVO.setStatus(VOStatus.UPDATED);
		entVO.setNum_count(0);
		entVO.setWeight_count(UFDouble.ZERO_DBL);
		entVO.setVolume_count(UFDouble.ZERO_DBL);
		entVO.setAct_deli_man(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		entVO.setAct_deli_memo(entLineBVO.getMemo());
		toBeUpdate.add(entVO);
		//处理包装明细
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			for(EntLinePackBVO vo : entLinePackBVOs){
				if(vo.getPk_ent_line_pack_b().equals(vo_db.getPk_ent_line_pack_b())){
					if(vo.getNum() == null){
						vo.setNum(0);
					}
					if(vo.getWeight() == null){
						vo.setWeight(UFDouble.ZERO_DBL);
					}
					if(vo.getVolume() == null){
						vo.setVolume(UFDouble.ZERO_DBL);
					}
					//假不管包装明细相不相同，都要将包装明细给运段1就行
					EntPackBVO currentPackBVO = new EntPackBVO();
					SegPackBVO currentSegPackBVO = new SegPackBVO();
					//当前需要处理的包装明细，和
					for(EntPackBVO entPackBVO : entPackBVOs){
						if(entPackBVO.getPk_ent_pack_b().equals(vo.getPk_ent_pack_b())){
							for(SegPackBVO segPackBVO : segPackBVOs){
								if(segPackBVO.getPk_seg_pack_b().equals(entPackBVO.getPk_seg_pack_b())){
									currentSegPackBVO = segPackBVO;
									currentPackBVO =  entPackBVO;//记录对应的packB下面就不要再查找了。
									break;
								}
							}
						}
					}
					SegPackBVO segPackBVO1 = currentSegPackBVO.clone();
					//处理运段包装明细 运段1
					//entPackBVO里的件重体是数据库里的件重体，这里需要用页面的数据替换掉。
					
					//数据库原始的包装的件重体
					Integer db_num = (vo_db.getNum() == null ? 0 : vo_db.getNum());
					UFDouble db_weight = (vo_db.getWeight() == null ? UFDouble.ZERO_DBL : vo_db.getWeight());
					UFDouble db_volume = (vo_db.getVolume() == null ? UFDouble.ZERO_DBL : vo_db.getVolume());
					
					//页面上传入的件重体（js已经根据单位件数单位重量算过了。）
					Integer num = (vo.getNum() == null ? 0 : vo.getNum());
					UFDouble weight = (vo.getWeight() == null ? UFDouble.ZERO_DBL : vo.getWeight());
					UFDouble volume = (vo.getVolume() == null ? UFDouble.ZERO_DBL : vo.getVolume());
					
					segPackBVO1.setNum(num);
					segPackBVO1.setWeight(weight);
					segPackBVO1.setVolume(volume);
					segPackBVO1.setPk_seg_pack_b(null);
					segPackBVO1.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(segPackBVO1);
					segPackBVO1.setPk_segment(segmentVO1.getPk_segment());
					toBeUpdate.add(segPackBVO1);
					if(WebUtils.getLoginInfo() == null){
						currentPackBVO.setDef1("edi");
					}
					currentPackBVO.setPk_seg_pack_b(segPackBVO1.getPk_seg_pack_b());
					currentPackBVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(currentPackBVO);
					
					segmentVO1.setNum_count(segmentVO1.getNum_count() + segPackBVO1.getNum());
					segmentVO1.setWeight_count(segmentVO1.getWeight_count().add(segPackBVO1.getWeight()));
					segmentVO1.setVolume_count(segmentVO1.getVolume_count().add(segPackBVO1.getVolume()));
					
					if(!vo.getNum().equals(vo_db.getNum())){
						//修改节点的包装明细 委托单的节点
						vo_db.setNum(num);
						vo_db.setWeight(weight);
						vo_db.setVolume(volume);
						vo_db.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(vo_db);
						//修改委托单的包装明细
						
						//EntPackBVO oldEntPackBVO = (EntPackBVO) currentPackBVO.clone();
						currentPackBVO.setNum(num);
						currentPackBVO.setWeight(weight);
						currentPackBVO.setVolume(volume);
						
						SegPackBVO segPackBVO2 = currentSegPackBVO.clone();
						segPackBVO2.setNum(db_num - num);
						segPackBVO2.setWeight(db_weight.sub(weight));
						segPackBVO2.setVolume(db_volume.sub(volume));
//						segPackBVO2.setWeight(currentSegPackBVO.getUnit_weight() == null ? UFDouble.ZERO_DBL : currentSegPackBVO.getUnit_weight().multiply(segPackBVO2.getNum()));
//						segPackBVO2.setVolume(currentSegPackBVO.getUnit_volume() == null ? UFDouble.ZERO_DBL : currentSegPackBVO.getUnit_volume().multiply(segPackBVO2.getNum()));
						segPackBVO2.setPk_seg_pack_b(null);
						segPackBVO2.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(segPackBVO2);
						segPackBVO2.setPk_segment(segmentVO2.getPk_segment());
						//处理运段2包装明细 只有数量不一致才生成
						segmentVO2.setNum_count(segmentVO2.getNum_count() + segPackBVO2.getNum());
						segmentVO2.setWeight_count(segmentVO2.getWeight_count().add(segPackBVO2.getWeight()));
						segmentVO2.setVolume_count(segmentVO2.getVolume_count().add(segPackBVO2.getVolume()));
						toBeUpdate.add(segPackBVO2);
						
					}
				}
			}
		}
		//重新计算委托单的件重体
		for(EntPackBVO entPackBVO : entPackBVOs){
			entVO.setNum_count(entVO.getNum_count() + entPackBVO.getNum());
			entVO.setWeight_count(entVO.getWeight_count().add(entPackBVO.getWeight() == null ? UFDouble.ZERO_DBL : entPackBVO.getWeight()));
			entVO.setVolume_count(entVO.getVolume_count().add(entPackBVO.getVolume() == null ? UFDouble.ZERO_DBL : entPackBVO.getVolume()));
		}
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			for(EntLinePackBVO vo_db_e : entLinePackBVOs_db_e){
				if(vo_db.getPk_ent_pack_b().equals(vo_db_e.getPk_ent_pack_b())){
					vo_db_e.setNum(vo_db.getNum());
					vo_db_e.setWeight(vo_db.getWeight());
					vo_db_e.setVolume(vo_db.getVolume());
					vo_db_e.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(vo_db_e);
				}
			}
		}
		
		
		// 根据运段读取客户
		String cust_sql = "select pk_customer from ts_invoice WITH(NOLOCK) where vbillno=(select invoice_vbillno from ts_segment where pk_segment=?)";
		String pk_customer = NWDao.getInstance() .queryForObject(cust_sql,String.class, segmentVO.getPk_segment());
		UFDouble segRate = custService.getFeeRate(pk_customer, segmentVO1.getPk_trans_type(), segmentVO1.getDeli_city(), segmentVO1.getArri_city());
		if(segRate != null && segRate.doubleValue() != 0){
			UFDouble seg1Volume_weight_count = segmentVO1.getVolume_count().multiply(segRate);
			UFDouble seg1Fee_weight_count = new UFDouble();
			if(seg1Volume_weight_count.doubleValue() < segmentVO1.getWeight_count().doubleValue()) {
				seg1Fee_weight_count = segmentVO1.getWeight_count();
			} else {
				seg1Fee_weight_count = seg1Volume_weight_count;
			}
			segmentVO1.setFee_weight_count(seg1Fee_weight_count);
			segmentVO1.setVolume_weight_count(seg1Volume_weight_count);
			
			UFDouble seg2Volume_weight_count = segmentVO2.getVolume_count().multiply(segRate);
			UFDouble seg2Fee_weight_count = new UFDouble();
			if(seg2Volume_weight_count.doubleValue() < segmentVO2.getWeight_count().doubleValue()) {
				seg2Fee_weight_count = segmentVO2.getWeight_count();
			} else {
				seg2Fee_weight_count = seg2Volume_weight_count;
			}
			segmentVO2.setFee_weight_count(seg2Fee_weight_count);
			segmentVO2.setVolume_weight_count(seg2Volume_weight_count);
		}
		UFDouble entRate = carrService.getFeeRate(entVO.getPk_carrier(), entVO.getPk_trans_type(), entVO.getDeli_city(), entVO.getArri_city());
		if(entRate != null && entRate.doubleValue() != 0){
			UFDouble volume_weight_count = entVO.getVolume_count().multiply(entRate);
			UFDouble fee_weight_count = new UFDouble();
			if(volume_weight_count.doubleValue() < entVO.getWeight_count().doubleValue()) {
				fee_weight_count = entVO.getWeight_count();
			} else {
				fee_weight_count = volume_weight_count;
			}
			entVO.setFee_weight_count(fee_weight_count);
			entVO.setVolume_weight_count(volume_weight_count);
		}
		PayDetailVO payDetailVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "entrust_vbillno=?", entVO.getVbillno());
		if(payDetailVO.getVbillstatus() != BillStatus.NEW){
			throw new BusiException("应付明细不是[新建]，不能修改货品信息！");
		}
		payDetailVO.setStatus(VOStatus.UPDATED);
		payDetailVO.setNum_count(entVO.getNum_count());
		payDetailVO.setWeight_count(entVO.getWeight_count());
		payDetailVO.setVolume_count(entVO.getVolume_count());
		payDetailVO.setFee_weight_count(entVO.getFee_weight_count());
		toBeUpdate.add(payDetailVO);
		entLineBVO_e.setStatus(VOStatus.UPDATED);
		entLineBVO_e.setPk_segment(segmentVO1.getPk_segment());
		toBeUpdate.add(entLineBVO_e);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		entLineBVO.setPk_segment(segmentVO1.getPk_segment());
	}
	

	public void confirmTracking(APPTrackingVO appTrackingVO) {
		dao.saveOrUpdate(appTrackingVO);
	}
	
	
	

	public void saveOperation(EntOperationBVO[] operationBVOs, String pk_entrust, ParamVO paramVO) {
		String ent_sql = "SELECT ent1.pk_entrust FROM ts_entrust ent1 WITH(NOLOCK) "
				+ "LEFT JOIN ts_entrust ent2 WITH(NOLOCK)  ON ent1.lot = ent2.lot "
				+ "WHERE isnull(ent1.dr,0)=0 AND isnull(ent2.dr,0)=0 AND ent2.pk_entrust =?";
		
		List<String> pk_ents = NWDao.getInstance().queryForList(ent_sql, String.class, pk_entrust);
		
		String cond = NWUtils.buildConditionString(pk_ents.toArray(new String[pk_ents.size()]));
		//把旧的作业删除，重新插入。
		EntOperationBVO[] oldOperationBVOs = dao.queryForSuperVOArrayByCondition(EntOperationBVO.class, "pk_entrust in "+cond);
		List<SuperVO>  toBeUpDate = new ArrayList<SuperVO>();
		for(EntOperationBVO oldOperationBVO : oldOperationBVOs){
			oldOperationBVO.setStatus(VOStatus.DELETED);
			toBeUpDate.add(oldOperationBVO);
		}
		
		for(String pk_ent : pk_ents){
			if(operationBVOs != null && operationBVOs.length > 0){
				for(EntOperationBVO newOperationBVO : operationBVOs){
					EntOperationBVO operationBVO = new EntOperationBVO();
					operationBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(operationBVO);
					operationBVO.setPk_entrust(pk_ent);
					for(String attr : operationBVO.getAttributeNames()){
						//PK字段不要复制
						if(!attr.equals(operationBVO.getPKFieldName())
								&& !attr.equals(operationBVO.getParentPKFieldName())){
							operationBVO.setAttributeValue(attr, newOperationBVO.getAttributeValue(attr));
						}
					}
					toBeUpDate.add(operationBVO);
				}
			}
		}
		//根据委托单，找到对应的应付明细，检查费用状态
		String pay_sql = "SELECT ts_pay_detail.* FROM ts_pay_detail WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_pay_detail.entrust_vbillno=ts_entrust.vbillno "
				+ " WHERE isnull(ts_pay_detail.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 AND ts_pay_detail.pay_type=0  "
				+ " AND ts_entrust.pk_entrust IN " + cond;
		List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(pay_sql, PayDetailVO.class);
		String[] billIds = new String[payDetailVOs.size()];
		for(int i=0; i<payDetailVOs.size(); i++){
			if(!payDetailVOs.get(i).getVbillstatus().equals(BillStatus.NEW)){
				throw new BusiException("应付明细[?]，不是新建，请检查！",payDetailVOs.get(i).getVbillno());
			}else{
				billIds[i] = payDetailVOs.get(i).getPrimaryKey();
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpDate);
		processAfterSaveOperation(billIds,paramVO);
		
	}
	public void processAfterSaveOperation(String[] billIds, ParamVO paramVO){
		paramVO.setTabCode(TabcodeConst.TS_ENT_OPERATION_B);
		payDetailService.reComputeMnyByLots(paramVO, billIds);
	}

	public List<Map<String, Object>> loadEntOperationB(String pk_entrust) {
		EntOperationBVO[] entOperationBVOs = dao.queryForSuperVOArrayByCondition(EntOperationBVO.class, "pk_entrust=?",
				pk_entrust);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(entOperationBVOs.length);
		for(SuperVO vo : entOperationBVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.ENTRUST_CODE);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, true,
				new String[] { TabcodeConst.TS_ENT_OPERATION_B }, null);
		return list;
	}
	
	
	public List<SuperVO> processPODAndEntrust(EntLineBVO entLineBVO,EntrustVO entVO){
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		entVO.setAct_arri_man(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		entVO.setAct_arri_memo(entLineBVO.getMemo());
//		entVO.setStatus(VOStatus.UPDATED);
//		toBeUpdate.add(entVO);
		
		List<EntLinePackBVO> entLinePackBVOs = entLineBVO.getEntLinePackBVOs();
		if(entLinePackBVOs == null || entLinePackBVOs.size() == 0){
			//没有货品信息，不需要处理货品，委托单，应付的货品信息
			return toBeUpdate;
		}
		
		List<String> pks = new ArrayList<String>();
		
		for(EntLinePackBVO vo: entLinePackBVOs){
			pks.add(vo.getPk_ent_line_b());
		}
		
		String sql = "SELECT pk_ent_line_pack_b, pk_entrust, pk_ent_line_b,pk_ent_pack_b, dr, ts, serialno, pk_goods, goods_code, goods_name, num, pack, weight, volume, unit_weight, unit_volume,"
				+ " length, width, height, trans_note, low_temp, hight_temp, reference_no, memo, pack_num_count, plan_num, plan_pack_num_count, def1, def2, def3, def4, def5, def6,"
				+ " def7, def8, def9, def10, def11, def12 FROM ts_ent_line_pack_b where dr =0 and pk_ent_line_b in ";
		String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
		sql = sql + cond;
		List<EntLinePackBVO> entLinePackBVOs_db = dao.queryForList(sql, EntLinePackBVO.class);
		
		List<String> entPackbPks = new ArrayList<String>();
		
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			entPackbPks.add(vo_db.getPk_ent_pack_b());
		}
		
		EntPackBVO[] entPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class, 
				"pk_ent_pack_b in " + NWUtils.buildConditionString(entPackbPks));
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		boolean flag = false;
		for(EntLinePackBVO vo_db : entLinePackBVOs_db){
			for(EntLinePackBVO vo_ar : entLinePackBVOs){
				if(vo_ar.getPk_ent_line_pack_b().equals(vo_db.getPk_ent_line_pack_b())){
					if(vo_ar.getNum() == null){
						vo_ar.setNum(0);
					}
					if(vo_ar.getWeight() == null){
						vo_ar.setWeight(UFDouble.ZERO_DBL);
					}
					if(vo_ar.getVolume() == null){
						vo_ar.setVolume(UFDouble.ZERO_DBL);
					}
					if(!vo_db.getNum().equals(vo_ar.getNum())){
						flag = true;
					}
					if(!(vo_db.getWeight() == null ? UFDouble.ZERO_DBL : vo_db.getWeight()).equals(vo_ar.getWeight())){
						flag = true;
					}
					if(!(vo_db.getVolume() == null ? UFDouble.ZERO_DBL : vo_db.getVolume()).equals(vo_ar.getVolume())){
						flag = true;
					}
					//修改节点的件重体
					vo_db.setNum(vo_ar.getNum());
					vo_db.setWeight(vo_ar.getWeight());
					vo_db.setVolume(vo_ar.getVolume());
					vo_db.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(vo_db);
					//修改包装明细的
					for(EntPackBVO entPackBVO : entPackBVOs){
						if(entPackBVO.getPk_ent_pack_b().equals(vo_ar.getPk_ent_pack_b())){
							entPackBVO.setNum(vo_ar.getNum());
							entPackBVO.setWeight(vo_ar.getWeight());
							entPackBVO.setVolume(vo_ar.getVolume());
							entPackBVO.setStatus(VOStatus.UPDATED);
							toBeUpdate.add(entPackBVO);
							break;
						}
					}
					num_count += vo_ar.getNum();
					weight_count = weight_count.add(vo_ar.getWeight() == null ? UFDouble.ZERO_DBL : vo_ar.getWeight());
					volume_count = volume_count.add(vo_ar.getVolume() == null ? UFDouble.ZERO_DBL : vo_ar.getVolume());
					break;
				}
			}
		}
		
		if(flag){
			//件重体不一致，要产生异常事故
			entVO.setExp_flag(UFBoolean.TRUE);
			ExpAccidentVO accidentVO = new ExpAccidentVO();
			accidentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(accidentVO);
			accidentVO.setEntrust_vbillno(entVO.getVbillno());
			accidentVO.setLot(entVO.getLot());
			accidentVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YCSG));
			accidentVO.setVbillstatus(BillStatus.NEW);
			accidentVO.setOrigin(ExpAccidentOrgin.POD.toString());
			accidentVO.setInvoice_vbillno(entVO.getInvoice_vbillno());
			accidentVO.setPk_customer(entVO.getPk_customer());
			accidentVO.setPk_carrier(entVO.getPk_carrier());
			accidentVO.setExp_type("0");
			accidentVO.setOccur_date(new UFDateTime(new Date()));
			accidentVO.setOccur_addr(entLineBVO.getPk_address());
			accidentVO.setFb_user(WebUtils.getLoginInfo().getPk_user());
			accidentVO.setFb_date(new UFDateTime(new Date()));
			accidentVO.setReason_type(null);
			accidentVO.setMemo(entLineBVO.getMemo());
			accidentVO.setCreate_time(new UFDateTime(new Date()));
			if(WebUtils.getLoginInfo() != null){
				accidentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				accidentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			}else{
				accidentVO.setCreate_user("32e6103e697f44b7ac98477583af49cd");
				accidentVO.setPk_corp(entVO.getPk_corp());
			}
			toBeUpdate.add(accidentVO);
			
		}
		
		entVO.setNum_count(num_count);
		entVO.setWeight_count(weight_count);
		entVO.setVolume_count(volume_count);
		//处理计费重体积重
		UFDouble rate = carrService.getFeeRate(entVO.getPk_carrier(), entVO.getPk_trans_type(), entVO.getDeli_city(), entVO.getArri_city());
		if(rate != null && rate.doubleValue() != 0){
			UFDouble volume_weight_count = entVO.getVolume_count().multiply(rate);
			UFDouble fee_weight_count = new UFDouble();
			if(volume_weight_count.doubleValue() < entVO.getWeight_count().doubleValue()) {
				fee_weight_count = entVO.getWeight_count();
			} else {
				fee_weight_count = volume_weight_count;
			}
			entVO.setFee_weight_count(fee_weight_count);
			entVO.setVolume_weight_count(volume_weight_count);
		}
		
		
		PayDetailVO payDetailVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "pay_type=0 and entrust_vbillno=?", entVO.getVbillno());
		if(payDetailVO.getVbillstatus() != BillStatus.NEW){
			throw new BusiException("应付明细不是[新建]状态，不能修改货品信息！");
		}
		payDetailVO.setStatus(VOStatus.UPDATED);
		payDetailVO.setNum_count(entVO.getNum_count());
		payDetailVO.setWeight_count(entVO.getWeight_count());
		payDetailVO.setVolume_count(entVO.getVolume_count());
		payDetailVO.setFee_weight_count(entVO.getFee_weight_count());
		toBeUpdate.add(payDetailVO);
		return toBeUpdate;
	}
	
	public void doEntrustPod(PodVO podVO, EntLineBVO lineBVO){
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		
		InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", podVO.getPk_invoice());
		if(invVO.getVbillstatus().intValue() != BillStatus.INV_ARRIVAL) {
			throw new BusiException("只有已到货的发货单才能执行签收！");
		}
		invVO.setVbillstatus(BillStatus.INV_SIGN);// 改成已签收
		invVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(invVO);
		
		podVO.setPod_date(new UFDateTime(lineBVO.getAct_arri_date()));
		podVO.setPod_exp(UFBoolean.FALSE);// 签收异常为否
		podVO.setPod_book_man(WebUtils.getLoginInfo() == null ?"32e6103e697f44b7ac98477583af49cd":WebUtils.getLoginInfo().getPk_user());
		podVO.setPod_book_time(new UFDateTime(lineBVO.getAct_arri_date()));
		podVO.setPod_man(WebUtils.getLoginInfo() == null ?"EDI":WebUtils.getLoginInfo().getUser_name());
		podVO.setPod_memo(lineBVO.getMemo());
		podVO.setReject_num_count(0);
		podVO.setDamage_num_count(0);
		podVO.setLost_num_count(0);
		
		podVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(podVO);
		
		//签收时，向发货单的跟踪信息表插入一条记录
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.POD);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("签收");
		itVO.setInvoice_vbillno(invVO.getVbillno());
		itVO.setPk_corp(WebUtils.getLoginInfo() == null ? invVO.getPk_corp() :WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo() == null ?"32e6103e697f44b7ac98477583af49cd":WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		toBeUpdate.add(itVO);
		
		String sql = "SELECT ts_inv_pack_b.pk_inv_pack_b,SUM(ts_ent_pack_b.num) AS num, "
				+ " SUM(ts_ent_pack_b.num*ts_ent_pack_b.unit_weight) AS weight , "
				+ " SUM(ts_ent_pack_b.num*ts_ent_pack_b.unit_volume) AS volume "
				+ " FROM ts_entrust te1 WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust te2 WITH(NOLOCK) ON te1.invoice_vbillno=te2.invoice_vbillno AND te1.pk_arrival=te2.pk_arrival AND te2.dr=0 "
				+ " LEFT JOIN ts_ent_pack_b WITH(NOLOCK) ON te2.pk_entrust=ts_ent_pack_b.pk_entrust AND ts_ent_pack_b.dr=0 "
				+ " LEFT JOIN ts_seg_pack_b WITH(NOLOCK) ON ts_ent_pack_b.pk_seg_pack_b=ts_seg_pack_b.pk_seg_pack_b AND ts_seg_pack_b.dr=0 "
				+ " LEFT JOIN ts_inv_pack_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_pack_b=ts_seg_pack_b.pk_inv_pack_b AND ts_inv_pack_b.dr=0 "
				+ " WHERE te1.vbillno=? GROUP BY ts_inv_pack_b.pk_inv_pack_b";
		List<Map<String,Object>> queryResults = NWDao.getInstance().queryForList(sql, podVO.getPod_entrust_vbillno());
		
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble volume_weight_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		
		if(queryResults == null || queryResults.size() == 0){
			//不处理明细
		}
		for (Map<String, Object> result : queryResults){
			num_count += Integer.parseInt(result.get("num") == null ? "0" : result.get("num").toString());
			weight_count = weight_count.add(new UFDouble(result.get("weight") == null ? "0" : result.get("weight").toString()));
			volume_count = volume_count.add(new UFDouble(result.get("volume") == null ? "0" : result.get("volume").toString()));
		}
		//处理计费重体积重
		UFDouble rate = carrService.getFeeRate(invVO.getPk_customer(), invVO.getPk_trans_type(), invVO.getDeli_city(), invVO.getArri_city());
		if(rate != null && rate.doubleValue() != 0){
			volume_weight_count = weight_count.multiply(rate);
			if(volume_weight_count.doubleValue() < weight_count.doubleValue()) {
				fee_weight_count = weight_count;
			} else {
				fee_weight_count = volume_weight_count;
			}
		}
		podVO.setPod_num_count(num_count);
		podVO.setPod_weight_count(weight_count);
		podVO.setPod_volume_count(volume_count);
		podVO.setPod_volume_weight_count(volume_weight_count);
		podVO.setPod_fee_weight_count(fee_weight_count);
		
		//处理每一行明细
		InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", invVO.getPk_invoice());
		for(InvPackBVO invPackBVO : invPackBVOs){
			for (Map<String, Object> result : queryResults){
				if(invPackBVO.getPk_inv_pack_b().equals(result.get("pk_inv_pack_b").toString())){
					invPackBVO.setPod_num(Integer.parseInt(result.get("num") == null ? "0" : result.get("num").toString()));
					invPackBVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(invPackBVO);
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
	}
	
	
}

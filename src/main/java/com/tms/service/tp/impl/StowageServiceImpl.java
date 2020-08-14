package com.tms.service.tp.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.ExcelImporter;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.HttpUtils;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.utils.QueryHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.AreaConst;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransLineConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CustService;
import com.tms.service.base.impl.CustExcelImporter;
import com.tms.service.inv.InvoiceService;
import com.tms.service.job.lbs.TrackInfoConverter;
import com.tms.service.te.impl.LbsApiVO;
import com.tms.service.tp.PZService;
import com.tms.service.tp.StowageService;
import com.tms.utils.TransLineUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import com.tms.service.job.edi.route.client.RouteClient;
import com.tms.service.job.edi.route.client.RouteVO;
/**
 * 调度配载界面的操作接口
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:22:20
 */
@Service
public class StowageServiceImpl extends TMSAbsBillServiceImpl implements StowageService {

	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private CustService custService;
	
	@Autowired
	private PZService pzService;
	
	
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, SegmentVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, SegmentVO.PK_SEGMENT);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.YDPZ;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setBeforeRenderer("req_deli_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setBeforeRenderer("req_arri_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
				}
				
			}
		}
		// 增加行操作列，包括展开的图标
		BillTempletBVO processor = new BillTempletBVO();
		processor.setItemkey("_processor");
		processor.setDefaultshowname("操作");
		processor.setDatatype(UiConstants.DATATYPE.TEXT.intValue());
		processor.setListflag(Constants.YES);
		processor.setCardflag(Constants.YES);
		processor.setListshowflag(new UFBoolean(true));
		processor.setShowflag(Constants.YES);
		processor.setEditflag(Constants.NO);
		processor.setLockflag(Constants.NO);
		processor.setReviseflag(new UFBoolean(false));
		processor.setTotalflag(Constants.NO);
		processor.setNullflag(Constants.NO);
		processor.setWidth(1);
		processor.setMetadataproperty("30");
		processor.setDr(0);
		processor.setPos(UiConstants.POS[0]);
		processor.setTable_code(TabcodeConst.TS_SEGMENT);
		processor.setPk_billtemplet(templateID);
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tms.service.TMSAbsBillServiceImpl#getCustomerOrCarrierCond(org.nw
	 * .vo.ParamVO)
	 */
	@SuppressWarnings("rawtypes")
	protected String getCustomerOrCarrierCond(ParamVO paramVO) {
		UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?",
				WebUtils.getLoginInfo().getPk_user());
		Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
		SuperVO superVO = null;
		try {
			superVO = (SuperVO) clazz.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(superVO != null) {
			if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CUSTOMER.intValue()) {
				// 如果是客户登陆
				Field pk_customer = ReflectionUtils.getDeclaredField(superVO, "pk_customer");
				if(pk_customer != null) {
					return " invoice_vbillno in (select vbillno from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and pk_customer='"
							+ userVO.getPk_customer() + "')";
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, String>> getCarrData(String[] billIDs) {
		String billidsStr = "";
		if (billIDs != null && billIDs.length >= 0 && StringUtils.isNotBlank(billIDs[0])) {
			billidsStr = billIDs[0];
		}
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 调用存储过程
		// 存储过程名称
		final String TS_ASSIGN_RULE_PROC = "ts_assign_rule_proc";
		final String vbillno = billidsStr;
		final String user = WebUtils.getLoginInfo().getPk_user();
		final String empyt = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(TS_ASSIGN_RULE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, vbillno);
					cs.setString(2, user);
					cs.setString(3, empyt);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs1 = cs.getResultSet();
					if (cs.getMoreResults()) {
						ResultSet rs = cs.getResultSet();
						while (rs.next()) {
							fillResultfSetForCarr(retList, rs);
						}
					}

					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Map<String, String>> returnResut = new ArrayList<Map<String, String>>();
		for (HashMap<String, String> resut : retList) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("pk_carrier", resut.get("pk_carrier"));
			map.put("carr_name", resut.get("carr_name"));
			map.put("ent_count", resut.get("ent_count"));
			map.put("accid_count", resut.get("accid_count"));
			map.put("price", resut.get("price"));
			map.put("kpi", resut.get("kpi"));
			returnResut.add(map);
		}
		return returnResut;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForCarr(List<HashMap<String,String>> resutlLists, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("pk_carrier", rs.getString("pk_carrier"));
		resut.put("carr_name", rs.getString("carr_name"));
		resut.put("pk_trans_type", rs.getString("pk_trans_type"));
		resut.put("trans_type_name", rs.getString("trans_type_name"));
//		resut.put("deli_city", rs.getString("deli_city"));
//		resut.put("arri_city", rs.getString("arri_city"));
		resut.put("ent_count", rs.getString("ent_count"));
		resut.put("accid_count", rs.getString("accid_count"));
		resut.put("price", rs.getString("price"));
		resut.put("kpi", rs.getString("kpi"));
		resutlLists.add(resut);
	}
	
	
	

	public List<TreeVO> getLineTree() {
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		// 查询所有路线类型是配载路线的路线
		TransLineVO[] lineVOs = dao.queryForSuperVOArrayByCondition(TransLineVO.class,
				"line_type=? and isnull(locked_flag,'N')='N'", DataDictConst.LINE_TYPE.PZLX.intValue());
		for(TransLineVO lineVO : lineVOs) {
			TreeVO treeVO = new TreeVO();
			treeVO.setId(lineVO.getPk_trans_line());
			treeVO.setCode(lineVO.getLine_code());
			treeVO.setText(lineVO.getLine_name() + " " + getStatInfoString(lineVO)); // 路线名称+总票数+总件数+总重量+总体积,其他字段在发货单中
			treeVO.setLeaf(true);
			treeVOs.add(treeVO);
		}
		return treeVOs;
	}

	/**
	 * 返回统计信息的一个字符串
	 * 
	 * @param lineVO
	 * @return
	 */
	private String getStatInfoString(TransLineVO lineVO) {
		Map<String, Object> statInfoMap = getStatInfoByLineVO(lineVO);
		if(statInfoMap == null) {
			return "";
		}
		int row_count = 0;
		int num_count = 0;
		double weight_count = 0, volume_count = 0;
		try {
			row_count = Integer.parseInt(statInfoMap.get("row_count").toString());
		} catch(Exception e) {
		}
		try {
			num_count = Integer.parseInt(statInfoMap.get("num_count").toString());
		} catch(Exception e) {
		}
		try {
			weight_count = Double.parseDouble(statInfoMap.get("weight_count").toString());
		} catch(Exception e) {
		}
		try {
			volume_count = Double.parseDouble(statInfoMap.get("volume_count").toString());
		} catch(Exception e) {
		}
		String split = "|";
		return new StringBuffer().append(row_count).append(split).append(num_count).append(split).append(weight_count)
				.append(split).append(volume_count).toString();
	}

	/**
	 * 根据路线VO返回运段的统计信息，包括总票数、总件数、总重量、总体积
	 * 
	 * @param lineVO
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getStatInfoByLineVO(TransLineVO lineVO) {
		//String condString = getCondString(lineVO);
		String sql = "select sum(1) as row_count,sum(num_count) as num_count,sum(weight_count) as weight_count,sum(volume_count) as volume_count "
				+ "from ts_segment WITH(NOLOCK) where isnull(dr,0)=0 ";
		sql += " and seg_mark !=" + SegmentConst.SEG_MARK_PARENT;
		//yaojiie 2015 12 16 增加状态判断
		sql += " and vbillstatus =" + BillStatus.SEG_WPLAN;
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(corpCond)) {
			sql += " and " + corpCond;
		}
		sql += " and pz_line='"+lineVO.getPk_trans_line()+"'"; 
//		if(StringUtils.isNotBlank(condString)) {
//			sql += " and " + condString;
//		}
		return (Map<String, Object>) dao.queryForObject(sql, HashMap.class);
	}

	/**
	 * 返回查询条件
	 * <p>
	 * 算法：
	 * </p>
	 * <p>
	 * 1）、如果起始地类型和目的地类型是地址，将起始地和目的地以及子表中的路线节点作为参考集合，如果运段中的提货方或收货方在这个地址集合中，
	 * 说明该运段属于这个路线，需要加入统计，如果查询了多条记录，那么统计多条记录的统计信息（总票数、总件数、总重量、总体积）
	 * </p>
	 * <p>
	 * 2）、如果起始地类型和目的地类型是城市，那么从区域档案中读取该城市的下一级节点，
	 * 加上这个当前的城市作为参考集合，（如以厦门市作为起始地城市，那么参考集合就是厦门市、湖里区、思明区...）
	 * ，比较提货方是否在起始地的集合中，同时比较收货方是否在目的地的集合中，如果都存在，则说明属于该路线，需要加入统计，如果查询了多条记录
	 * ，那么统计多条记录的统计信息（总票数、总件数、总重量、总体积）
	 * </p>
	 * <p>
	 * 3）、不考虑起始地类型和目的地类型不同的情况
	 * </p>
	 * 
	 * @param lineVO
	 * @return
	 */
	public String getCondString(TransLineVO lineVO) {
		if(lineVO == null) {
			return null;
		}
		StringBuffer condBuf = new StringBuffer();
		int start_addr_type = lineVO.getStart_addr_type(); // 起始地类型
		int end_addr_type = lineVO.getEnd_addr_type();// 目的地类型
		if(start_addr_type == DataDictConst.ADDR_TYPE.ADDR.intValue()
				&& end_addr_type == DataDictConst.ADDR_TYPE.ADDR.intValue()) {
			// 类型是地址
			Set<String> nodes = new HashSet<String>();
			nodes.add(lineVO.getStart_addr());
			nodes.add(lineVO.getEnd_addr());
			// 读取路线节点
			String sql = "select pk_address from ts_line_node WITH(NOLOCK) where pk_trans_line=? and isnull(dr,0)=0 and isnull(locked_flag,'N')='N'";
			List<String> nodeAry = dao.queryForList(sql, String.class, lineVO.getPk_trans_line());
			nodes.addAll(nodeAry);

			// 读取
			String cond = null;
			StringBuffer sb = new StringBuffer();
			for(String node : nodes) {
				sb.append("'");
				sb.append(node);
				sb.append("',");
			}
			cond = sb.substring(0, sb.length() - 1);
			condBuf.append(" (pk_delivery in (").append(cond).append(") or pk_arrival in (").append(cond).append(")) ");
			condBuf.append(" and pk_trans_type='").append(lineVO.getPk_trans_type()).append("'");
			return condBuf.toString();
		} else if(start_addr_type == DataDictConst.ADDR_TYPE.CITY.intValue()
				&& end_addr_type == DataDictConst.ADDR_TYPE.CITY.intValue()) {
			Set<String> startAry = new HashSet<String>(); // 提货方参考集合
			startAry.add(lineVO.getStart_addr());
			Set<String> endAry = new HashSet<String>(); // 收货方参考集合
			endAry.add(lineVO.getEnd_addr());
			AreaVO areaVO = dao.queryByCondition(AreaVO.class, "pk_area=?", lineVO.getStart_addr());
			if(areaVO.getArea_level() == AreaConst.CITY_LEVEL) {
				// 选择的是城市级别，比如厦门市，而不是地区级别，如思明区,此时需要读取地区
				String sql = "select pk_area from ts_area WITH(NOLOCK) where parent_id=? and isnull(dr,0)=0 and isnull(locked_flag,'N')='N'";
				List<String> nodeAry = dao.queryForList(sql, String.class, lineVO.getStart_addr());
				startAry.addAll(nodeAry);
			}
			areaVO = dao.queryByCondition(AreaVO.class, "pk_area=?", lineVO.getEnd_addr());
			if(areaVO.getArea_level() == AreaConst.CITY_LEVEL) {
				// 选择的是城市级别，比如厦门市，而不是地区级别，如思明区,此时需要读取地区
				String sql = "select pk_area from ts_area WITH(NOLOCK) where parent_id=? and isnull(dr,0)=0 and isnull(locked_flag,'N')='N'";
				List<String> nodeAry = dao.queryForList(sql, String.class, lineVO.getEnd_addr());
				endAry.addAll(nodeAry);
			}
			// 读取
			String startCond = null, endCond = null;
			StringBuffer sb = new StringBuffer();
			for(String node : startAry) {
				sb.append("'");
				sb.append(node);
				sb.append("',");
			}
			if(sb.length() > 0) {
				startCond = sb.substring(0, sb.length() - 1);
			}
			sb = new StringBuffer();
			for(String node : endAry) {
				sb.append("'");
				sb.append(node);
				sb.append("',");
			}
			if(sb.length() > 0) {
				endCond = sb.substring(0, sb.length() - 1);
			}
			if(StringUtils.isNotBlank(startCond) || StringUtils.isNotBlank(endCond)) {
				condBuf.append(" deli_city in (" + startCond + ") and arri_city in (" + endCond + ")");
			}
			//yaojiie 2015 12 16 增加公司条件判断
			String corpCond = CorpHelper.getCurrentCorpWithChildren();
			condBuf.append(" and pk_trans_type='").append(lineVO.getPk_trans_type()).append("'").append(" and ").append(corpCond);
			return condBuf.toString();
		}
		// 如果没有符合条件的，那么此时查询的数据应该为0才是，那么作为查询条件不能返回null
		return "1=2";
	}

	@SuppressWarnings("rawtypes")
	public List<Map<String,String>> getCarData(String[] carrIds) {
		String sql =  "select c.pk_car,c.pk_car_type,c.carno,ct.code as car_type_code,ct.name as car_type_name "
				+ "from ts_car c,ts_car_type ct WITH(NOLOCK) "
				+ "where c.pk_car_type=ct.pk_car_type and isnull(c.dr,0)=0 and isnull(c.locked_flag,'N')='N'";
		if(carrIds != null && carrIds.length > 0 && StringUtils.isNotBlank(carrIds[0])){
			//这里接受到的只有一个数据，用","拼接在一起，所以需要分割。
			sql += (" and c.pk_carrier in " + NWUtils.buildConditionString(carrIds[0].split(",")));
		}else{
			 List<Map<String, String>> carrieDatas = this.getCarrData(null);
			 if(carrieDatas != null && carrieDatas.size() > 0){
				 List<String> pk_carriers = new ArrayList<String>();
				 for(Map<String,String> data : carrieDatas){
					 pk_carriers.add(data.get("pk_carrier"));
				 }
				 sql += (" and c.pk_carrier in " + NWUtils.buildConditionString(pk_carriers.toArray(new String[pk_carriers.size()])));
			 }else{
				 sql += (" and 1<>1 ");
			 }
		}

		List<HashMap> list = (List<HashMap>) dao.queryForList(sql, HashMap.class);
		//不再使用树状结构
		List<String> carTypeList = new ArrayList<String>();
		List<String> carnoList = new ArrayList<String>();
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		for(HashMap map : list){
			carnoList.add(map.get("carno").toString());
			String pk_car_type = map.get("pk_car_type").toString();
			if(!carTypeList.contains(pk_car_type)){
				carTypeList.add(pk_car_type);
			}
		}
		Map carnoAndNumMap = getCarnoAndNumMap(carnoList);
		for(HashMap map : list){
			Map<String,String> retMap = new HashMap<String, String>();
			retMap.put("pk_car", map.get("pk_car").toString());
			retMap.put("carno", map.get("carno").toString());
			retMap.put("carType", map.get("car_type_code").toString());
			retMap.put("carInfo", getCarInfo(carnoAndNumMap, map.get("carno").toString(), map.get("pk_car").toString()));
			resultList.add(retMap);
		}
		
		return resultList;
	}

	/**
	 * 加在车辆树后面的信息
	 * 
	 * @param carno
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String getCarInfo(Map carnoAndNumMap, String carno, String pk_car) {
		int num = 0;
		if(carnoAndNumMap != null) {
			Object oNum = carnoAndNumMap.get(carno);
			if(oNum != null) {
				num = Integer.parseInt(oNum.toString());
			}
		}
		//yaojiie 2015 11 16 放出原本被注释的信息 将GPS添加到查看后面 
		String docUrl = "<span style=\"color:blue;text-decoration:underline\" onclick=\"openCarDoc('" + pk_car
				+ "')\">查看</span>";
		String gpsUrl = "<span style=\"color:blue;text-decoration:underline\" onclick=\"openCurrentTracking('" + pk_car
				+ "')\">GPS</span>";
		return new StringBuffer(" " + num).append(" ").append(docUrl).append(" ").append(gpsUrl).toString();
	}
	
	/**
	 * 加在车辆树后面的信息
	 * 
	 * @param carno
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String getCarInfo( String carno, String pk_car) {
		String docUrl = "<span style=\"color:blue;text-decoration:underline\" onclick=\"openCarDoc('" + pk_car
				+ "')\">查看</span>";
		String gpsUrl = "<span style=\"color:blue;text-decoration:underline\" onclick=\"openCurrentTracking('" + pk_car
				+ "')\">GPS</span>";
		return new StringBuffer().append(docUrl).append(" ").append(gpsUrl).toString();
	}

	/**
	 * 车辆相关未最终到货委托单的数量
	 * 
	 * @param carnoList
	 * @return 车牌号-委托单数量的Map
	 */
	@SuppressWarnings("rawtypes")
	private Map getCarnoAndNumMap(List<String> carnoList) {
		if(carnoList == null || carnoList.size() == 0) {
			return null;
		}
		String cond = NWUtils.buildConditionString(carnoList.toArray(new String[carnoList.size()]));
//		String sql = "select carno,count(1) as num from ts_entrust WITH(NOLOCK) where vbillstatus < ? and carno in " + cond
//				+ " group by carno";
		// yaojiie 2015 12 16 车辆信息已经不放在表头了，而是放在运力信息明细里，所以需要修改数据源。
		String sql = "select etb.carno,count(ent.lot) AS num from ts_ent_transbility_b etb WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = etb.pk_entrust "
				+ " WHERE  ent.vbillstatus < ? and isnull(etb.dr,0)=0 and isnull(ent.dr,0)=0 and "
				+ " etb.carno in " + cond
				+ " group by etb.carno ";
		List<HashMap> retList = NWDao.getInstance().queryForList(sql, HashMap.class, BillStatus.ENT_ARRIVAL);
		Map<String, Object> retMap = new HashMap<String, Object>();
		for(HashMap map : retList) {
			retMap.put(map.get("carno").toString(), map.get("num"));
		}
		return retMap;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String str = super.buildLoadDataCondition(params, paramVO, templetVO);
		// 父级运段不显示
		String condString = "seg_mark !=" + SegmentConst.SEG_MARK_PARENT;
		if(StringUtils.isNotBlank(str)) {
			condString += " and ";
			condString += str;
		}
		return condString;
	}

	/**
	 * tms的单据使用的是公司条件+逻辑条件，对于这个单据，另外需要加上委派公司等于当前公司的单据
	 */
	//yaojiie 2015 11 22 修改查询逻辑：解决OR　AND　ｂｕｇ
	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		try {
			SuperVO superVO = (SuperVO) clazz.newInstance();
			Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
			if(pk_corp != null) {
				//如果是承运商登陆，那么只能显示这个承运商的单据
				if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier())){
					return "("+ superVO.getTableName() + ".pk_carrier='"+ WebUtils.getLoginInfo().getPk_carrier() + "')";
				}else{
					String corpCond = CorpHelper.getCurrentCorpWithChildren(superVO.getTableName());
					String logicCond = QueryHelper.getLogicCond(templetVO);
					corpCond = "("+ corpCond +" or " + superVO.getTableName() + ".delegate_corp='"
							+ WebUtils.getLoginInfo().getPk_corp() + "')";
					if(StringUtils.isNotBlank(logicCond)) {
						corpCond = corpCond+ " and " + logicCond;
					}
					return corpCond;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy) || orderBy.trim().equals("order by create_time desc")) {
			orderBy = " order by vbillstatus,vbillno, req_arri_date";
		}
		return orderBy;
	}

	public List<SegmentVO> loadByPKs(String[] pks) {
		if(pks == null || pks.length == 0) {
			return new ArrayList<SegmentVO>();
		}
		StringBuffer buf = new StringBuffer("pk_segment in (");
		for(String pk : pks) {
			buf.append("'");
			buf.append(pk);
			buf.append("',");
		}
		String strWhere = buf.substring(0, buf.length() - 1);
		strWhere += ")";
		SegmentVO[] vos = dao.queryForSuperVOArrayByCondition(SegmentVO.class, strWhere);
		return Arrays.asList(vos);
	}

	public List<Map<String, Object>> loadSegPackByParent(String pk_segment) {
		SegPackBVO[] vos = dao.queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?", pk_segment);
		String[] formulas = new String[] { "pack_num->getColValue(ts_goods,pack_num,goods_code,goods_code)" };
		return FormulaHelper.execFormulaForSuperVO(Arrays.asList(vos), formulas);
	}

	// XXX 这里的pk_adress是有顺序的，必须按照这个顺序
	public List<SegmentVO> saveDistSection(String[] pk_address, String[] req_deli_date, String[] req_arri_date,
			String[] pk_segment) {
		logger.info("分段操作，待分段的运段PK：" + NWUtils.join(pk_segment, ","));
		String cond = NWUtils.buildConditionString(pk_segment);
		cond = " pk_segment in " + cond;
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, cond);
		for(SegmentVO segVO : segVOs){
			if(segVO.getSeg_mark() == SegmentConst.SEG_MARK_PARENT){
				throw new BusiException("当前运段已被拆段，请刷新页面！");
			}
		}
		cond = NWUtils.buildConditionString(pk_address);
		cond = " pk_address in " + cond;
		// 这里查询后要保持顺序，使用sqlserver的语法。如果要跨数据库需要做不同的处理
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			cond += " order by instr(pk_address,'" + NWUtils.join(pk_address, ",") + "')";
		}else{
			cond += " order by charindex(pk_address,'" + NWUtils.join(pk_address, ",") + "')";
		}
		AddressVO[] addrVOs = dao.queryForSuperVOArrayByCondition(AddressVO.class, cond);
		for(AddressVO addressVO : addrVOs){
			//yaojiie 2016 1 5 将所有的中间节点的运力信息都用第一个运段的运力信息表示
			addressVO.setLine_pk_trans_type(segVOs[0].getPk_trans_type());
		}
		// Arrays.asList返回的是Arrays的内部类，这里转成ArrayList
		List<SegmentVO> newSegmentVO = distSection(new ArrayList<AddressVO>(Arrays.asList(addrVOs)),
				new ArrayList<String>(Arrays.asList(req_deli_date)),
				new ArrayList<String>(Arrays.asList(req_arri_date)), new ArrayList<SegmentVO>(Arrays.asList(segVOs)),
				null);
		return newSegmentVO;
	}

	/**
	 * 执行分段操作，独立出来，可以复用
	 * 
	 * @param addrVOs
	 *            这里的addrVOs已经包含原
	 * @param segVOs
	 * @packVOs 通过这个参数来判断是否是发货单确认时自动分段
	 * @return
	 */
	public List<SegmentVO> distSection(List<AddressVO> addrVOs, List<String> req_deli_date, List<String> req_arri_date,
			List<SegmentVO> segVOs, SegPackBVO[] packVOs) {
		if(segVOs == null || segVOs.size() == 0 || addrVOs == null || addrVOs.size() == 0 || req_deli_date == null
				|| req_deli_date.size() == 0 || req_arri_date == null || req_arri_date.size() == 0) {
			// 不需要分段
			logger.info("[XXX]信息不完整，不需要分段...");
			return segVOs;
		}

		boolean getPackFromDb = false;
		if(packVOs == null) {// XXX 2015-05-13
								// 多个运段一起分段的情况下，货品信息每次都需要重新查询，这个问题调查太久了。
			getPackFromDb = true;
			logger.info("没有传入父级运段的包装信息，说明不是自动分段来的，需要从数据库中查询包装信息...");
		}

		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(int i = 0; i < segVOs.size(); i++) {
			List<AddressVO> newAddrVOs = new ArrayList<AddressVO>();
			newAddrVOs.addAll(addrVOs);
			SegmentVO parentSegVO = segVOs.get(i);
			parentSegVO.setSeg_mark(SegmentConst.SEG_MARK_PARENT); // 先定义为父级运段，该运段会隐藏
			if(parentSegVO.getStatus() == VOStatus.NEW) {
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]父级运段是新增状态，说明是自动分段来的...");
				// 如果是new类型，那么是从自动分段来的，此时不需要更新原始运段
			} else {
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]父级运段不是新增状态，此时需要更新父级运段...");
				parentSegVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(parentSegVO); // 更新原运段
			}
			// 父级运段的包装信息
			if(getPackFromDb) {
				packVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?",
						parentSegVO.getPk_segment());
			}

			// 将原运段的提货方和收货方加入这个addrVOs中，这样方便进行算法计算
			AddressVO deliAddrVO = dao.queryByCondition(AddressVO.class, "pk_address=?", parentSegVO.getPk_delivery());
			if(deliAddrVO == null) {
				throw new BusiException("运段的提货方不是有效的地址，请检查！");
			}
			logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]将父级运段的提货方[" + deliAddrVO.getAddr_code() + "|"
					+ deliAddrVO.getAddr_name() + "]加入到分段节点集合的第一个节点上...");
			
			//第一个节点的运力信息
			deliAddrVO.setLine_pk_trans_type(parentSegVO.getPk_trans_type());
			deliAddrVO.setLine_pk_carrier(parentSegVO.getPk_carrier());
			deliAddrVO.setLine_carno(parentSegVO.getCarno());
			deliAddrVO.setLine_pk_driver(parentSegVO.getPk_driver());
			
			newAddrVOs.add(0, deliAddrVO);
			AddressVO arriAddrVO = dao.queryByCondition(AddressVO.class, "pk_address=?", parentSegVO.getPk_arrival());
			if(arriAddrVO == null) {
				throw new BusiException("运段的收货方不是有效的地址，请检查！");
			}
			logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]将父级运段的到货方[" + arriAddrVO.getAddr_code() + "|"
					+ arriAddrVO.getAddr_name() + "]加入到分段节点集合的最后一个节点上...");
			newAddrVOs.add(arriAddrVO);

			// 提货日期和到货日期增加原运段的提货日期和到货日期
			// TODO 这里要考虑时间的情况
			logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]提货日期集合加上父级运段的提货日期["
					+ parentSegVO.getReq_deli_date() + "]...");
			
			List<String> newReq_deli_date = new ArrayList<String>();
			newReq_deli_date.addAll(req_deli_date);
			List<String> newReq_arri_date = new ArrayList<String>();
			newReq_arri_date.addAll(req_arri_date);
			if(parentSegVO.getReq_deli_date() != null) {
				newReq_deli_date.add(0, parentSegVO.getReq_deli_date());
			} else {
				newReq_deli_date.add(0, null);
			}
			logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]到货日期集合加上父级运段的到货日期["
					+ parentSegVO.getReq_arri_date() + "]...");
			if(parentSegVO.getReq_arri_date() != null) {
				newReq_arri_date.add(parentSegVO.getReq_arri_date());
			} else {
				newReq_arri_date.add(null);
			}

			logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]总共有" + newAddrVOs.size() + "个节点...");
			for(int j = 0; j < newAddrVOs.size() - 1; j++) {
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]开始构建第" + (j + 1) + "个子运段...");
				AddressVO addrVO = newAddrVOs.get(j);
				AddressVO nextAddrVO = newAddrVOs.get(j + 1);
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]子运段的提货方[" + addrVO.getAddr_code() + "|"
						+ addrVO.getAddr_name() + "]...");
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]子运段的到货方[" + nextAddrVO.getAddr_code() + "|"
						+ nextAddrVO.getAddr_name() + "]...");

				SegmentVO childSegVO = new SegmentVO();// 新的运段
				//每个运段，都用自己头节点的运力信息
				childSegVO.setPk_trans_type(addrVO.getLine_pk_trans_type());
				if(StringUtils.isBlank(childSegVO.getPk_trans_type())){
					childSegVO.setPk_trans_type(parentSegVO.getPk_trans_type());
				}
				childSegVO.setPk_carrier(addrVO.getLine_pk_carrier());
				childSegVO.setPk_driver(addrVO.getLine_pk_driver());
				childSegVO.setCarno(addrVO.getLine_carno());
				
				childSegVO.setDbilldate(new UFDate());
				childSegVO.setVbillno(parentSegVO.getVbillno() + "-" + (j + 1)); // 新生成的运段运段号编码为父级运段号+中划线(-)+1
				
				childSegVO.setSeg_type(SegmentConst.SECTION); // 运段类型
				childSegVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD); // 标记为下级运段
				childSegVO.setInvoice_vbillno(parentSegVO.getInvoice_vbillno());
				childSegVO.setVbillstatus(BillStatus.SEG_WPLAN); // 待计划状态
				childSegVO.setParent_seg(parentSegVO.getPk_segment()); // 设置父级的运段
				// 计算运输里程，如果没有，则使用父级的运输里程
				Map<String, Object> mileageAndDistanceMap = invoiceService.getMileageAndDistance(addrVO.getPk_city(),
						nextAddrVO.getPk_city(), addrVO.getPk_address(), nextAddrVO.getPk_address());
				childSegVO.setMileage(parentSegVO.getMileage());
				// 区间距离，如果没有，则使用父级的运输里程
				childSegVO.setDistance(parentSegVO.getDistance());
				if(mileageAndDistanceMap != null) {
					Object mileage = mileageAndDistanceMap.get("mileage");
					if(mileage != null) {
						childSegVO.setMileage(Integer.parseInt(mileage.toString()));
					}
					Object distance = mileageAndDistanceMap.get("distance");
					if(distance != null) {
						childSegVO.setDistance(new UFDouble(distance.toString()));
					}
				}

				childSegVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				childSegVO.setCreate_time(new UFDateTime(new Date()));
				childSegVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				// 设置提货方，收货方，提货日期，收货日期
				// 当运段时第一个点和最后一个点的时候，需要使用父辈运段的地址城市等信息，因为运段里可能会修改这些信息，儿这些信息，需要以运段为准。
				if(j == 0){
					childSegVO.setPk_delivery(parentSegVO.getPk_delivery());
					childSegVO.setDeli_city(parentSegVO.getDeli_city());
					childSegVO.setDeli_province(parentSegVO.getDeli_province());
					childSegVO.setDeli_area(parentSegVO.getDeli_area());
					childSegVO.setDeli_detail_addr(parentSegVO.getDeli_detail_addr());
					childSegVO.setDeli_contact(parentSegVO.getDeli_contact());
					childSegVO.setDeli_email(parentSegVO.getDeli_email());
					childSegVO.setDeli_mobile(parentSegVO.getDeli_mobile());
					childSegVO.setDeli_phone(parentSegVO.getDeli_phone());
				}else{
					childSegVO.setPk_delivery(addrVO.getPk_address());
					childSegVO.setDeli_city(addrVO.getPk_city());
					childSegVO.setDeli_province(addrVO.getPk_province());
					childSegVO.setDeli_area(addrVO.getPk_area());
					childSegVO.setDeli_detail_addr(addrVO.getDetail_addr());
					childSegVO.setDeli_contact(addrVO.getContact());
					childSegVO.setDeli_email(addrVO.getEmail());
					childSegVO.setDeli_mobile(addrVO.getMobile());
					childSegVO.setDeli_phone(addrVO.getPhone());
				}
				
				if(j ==  newAddrVOs.size() - 2){
					childSegVO.setPk_arrival(parentSegVO.getPk_arrival());
					childSegVO.setArri_city(parentSegVO.getArri_city());
					childSegVO.setArri_province(parentSegVO.getArri_province());
					childSegVO.setArri_area(parentSegVO.getArri_area());
					childSegVO.setArri_detail_addr(parentSegVO.getArri_detail_addr());
					childSegVO.setArri_contact(parentSegVO.getArri_contact());
					childSegVO.setArri_email(parentSegVO.getArri_email());
					childSegVO.setArri_mobile(parentSegVO.getArri_mobile());
					childSegVO.setArri_phone(parentSegVO.getArri_phone());
				}else{
					childSegVO.setPk_arrival(nextAddrVO.getPk_address());
					childSegVO.setArri_city(nextAddrVO.getPk_city());
					childSegVO.setArri_province(nextAddrVO.getPk_province());
					childSegVO.setArri_area(nextAddrVO.getPk_area());
					childSegVO.setArri_detail_addr(nextAddrVO.getDetail_addr());
					childSegVO.setArri_contact(nextAddrVO.getContact());
					childSegVO.setArri_email(nextAddrVO.getEmail());
					childSegVO.setArri_mobile(nextAddrVO.getMobile());
					childSegVO.setArri_phone(nextAddrVO.getPhone());

				}

				
				// 设置提货日期、到货日期
				childSegVO.setReq_deli_date(newReq_deli_date.get(j));
				childSegVO.setReq_arri_date(newReq_arri_date.get(j));
				//检查拆段之后的时间信息
				UFDateTime childSegVOReq_deli_date = new UFDateTime(childSegVO.getReq_deli_date());
				UFDateTime childSegVOReq_arri_date = new UFDateTime(childSegVO.getReq_arri_date());
				UFDateTime parentSegVOReq_deli_date = new UFDateTime(parentSegVO.getReq_deli_date());
				UFDateTime parentSegVOReq_arri_date = new UFDateTime(parentSegVO.getReq_arri_date());
				if(childSegVOReq_deli_date.before(parentSegVOReq_deli_date)){
					throw new BusiException("时间顺序有误</br>?</br>?",childSegVOReq_deli_date.toString(),parentSegVOReq_deli_date.toString());
				} 
				if(childSegVOReq_arri_date.after(parentSegVOReq_arri_date)){
					throw new BusiException("时间顺序有误</br>?</br>?",childSegVOReq_deli_date.toString(),parentSegVOReq_deli_date.toString());
				} 
				if(childSegVOReq_deli_date.after(childSegVOReq_arri_date)){
					throw new BusiException("时间顺序有误</br>?</br>?",childSegVOReq_deli_date.toString(),childSegVOReq_arri_date.toString());
				} 
				if(parentSegVOReq_deli_date.after(parentSegVOReq_arri_date)){
					throw new BusiException("时间顺序有误</br>?</br>?",parentSegVOReq_deli_date.toString(),parentSegVOReq_arri_date.toString());
				} 

				childSegVO.setMemo(parentSegVO.getMemo());
				childSegVO.setPack_num_count(parentSegVO.getPack_num_count());
				childSegVO.setNum_count(parentSegVO.getNum_count());
				childSegVO.setWeight_count(parentSegVO.getWeight_count());
				childSegVO.setVolume_count(parentSegVO.getVolume_count());
				childSegVO.setFee_weight_count(parentSegVO.getFee_weight_count());
				childSegVO.setVolume_weight_count(parentSegVO.getVolume_weight_count());
				childSegVO.setMemo(parentSegVO.getMemo());
				childSegVO.setUrgent_level(parentSegVO.getUrgent_level());
				childSegVO.setStatus(VOStatus.NEW);
				//插入线路信息
				List<TransLineVO> transLineVOs = TransLineUtils.matchTransLine(TransLineConst.PZLX, childSegVO);
				if(transLineVOs != null && transLineVOs.size() > 0){
					childSegVO.setPz_line(transLineVOs.get(0).getPk_trans_line());
					childSegVO.setPz_mileage(transLineVOs.get(0).getMileage());
				}else{
					TransLineVO transLineVO = TransLineUtils.matchTransLineByArea(TransLineConst.PZLX, childSegVO);
					if(transLineVO != null){
						childSegVO.setPz_line(transLineVO.getPk_trans_line());
						childSegVO.setPz_mileage(transLineVO.getMileage());
					}
				}
				NWDao.setUuidPrimaryKey(childSegVO);
				toBeUpdate.add(childSegVO);

				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]构建第" + (j + 1) + "个子运段，运段号："
						+ childSegVO.getVbillno() + "的包装信息...");
				// 关联父级运段的包装信息
				int index = 1;
				for(SegPackBVO childPackVO : packVOs) {
					logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]开始构建第" + (j + 1) + "个子运段的第" + index
							+ "个包装信息...");
					childPackVO = childPackVO.clone();
					childPackVO.setPk_segment(childSegVO.getPk_segment());
					childPackVO.setStatus(VOStatus.NEW);
					childPackVO.setPk_seg_pack_b(null);
					NWDao.setUuidPrimaryKey(childPackVO);// 重新设置主键
					toBeUpdate.add(childPackVO);
					logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]完成构建第" + (j + 1) + "个子运段的第" + index
							+ "个包装信息，PK：" + childPackVO.getPk_seg_pack_b() + "...");
					index++;
				}
				logger.info("[XXX父级运段号：" + parentSegVO.getVbillno() + "]第" + (j + 1) + "个子运段构建完成，运段号："
						+ childSegVO.getVbillno() + "...");
			}
		}
		dao.saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		return null; // 实际上不需要返回了,页面使用了刷新
	}

	public List<SegmentVO> saveAutoDistSection(String[] pk_segment) {
		String cond = NWUtils.buildConditionString(pk_segment);
		cond = " pk_segment in " + cond;
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, cond);
		autoDistSection(segVOs, null);
		return null; // 实际上不需要返回了,页面使用了刷新
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void autoDistSection(SegmentVO[] segVOs, SegPackBVO[] packVOs) {
		//存储过程名称
		final String TS_PLAN_TRANS_LINE_PROC = "ts_plan_trans_line_proc";
		if(segVOs == null || segVOs.length == 0) {
			logger.info("没有运段信息，不需要自动分段...");
			return;
		}
		
		int index = 1;
		for(SegmentVO segVO : segVOs) {
			logger.info("[XXX运段号：" + segVO.getVbillno() + "]开始自动分段第" + index + "个运段...");
			// 1、根据提货方和收货方，去路线表中匹配起始地类型和目的地类型都是地址的路线，并且路线类型为运输计划的路线
			logger.info("[XXX运段号：" + segVO.getVbillno() + "]根据运段的提货方和收货方匹配路线...");
			//先按照存储过程匹配
			// 用于存储查询数据
			final List<String> retReturn = new ArrayList<String>();
			//找到运段对应的发货单
			InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno=?", segVO.getInvoice_vbillno());
			final String invoice_vbillno = invoiceVO.getVbillno();
			final String empyt = "";
			try {
				NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
					public CallableStatement createCallableStatement(Connection conn) throws SQLException {
						// 设置存储过程参数
						int count = 2;
						String storedProc = DaoHelper.getProcedureCallName(TS_PLAN_TRANS_LINE_PROC, count);
						CallableStatement cs = conn.prepareCall(storedProc);
						cs.setString(1, invoice_vbillno);
						cs.setString(2, empyt);
						return cs;
					}
				}, new CallableStatementCallback() {
					public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
						// 查询结果集
						ResultSet rs = cs.executeQuery();
						while (rs.next()) {
							retReturn.add(rs.getString("pk_trans_line"));
						}
						cs.close();
						return null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
			TransLineVO[] lineVOs = null;
			if(retReturn.size() > 0 && StringUtils.isNotBlank(retReturn.get(0))){
				//存储过程查询到了结果
				String lineCond = NWUtils.buildConditionString(retReturn.toArray(new String[retReturn.size()]));
				lineVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TransLineVO.class, "pk_trans_line in " + lineCond);
				if(lineVOs == null || lineVOs.length == 0){
					logger.info("[XXX运段号：" + segVO.getVbillno() + "]没有匹配到任何路线节点，不能分段！");
					return;
				}
			}else{
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]没有匹配到任何路线节点，不能分段！");
				return;
			}
			TransLineVO lineVO = lineVOs[0]; // 取匹配到的第一条路线
			logger.info("[XXX运段号：" + segVO.getVbillno() + "]只取第一条路线，路线编码：" + lineVO.getLine_code() + ",路线名称："
					+ lineVO.getLine_name() + "...");
			segVO.setPk_trans_type(lineVO.getLine_pk_trans_type());
			segVO.setPk_carrier(lineVO.getLine_pk_carrier());
			segVO.setCarno(lineVO.getLine_carno());
			segVO.setPk_driver(lineVO.getLine_pk_driver());
			// 读取该条路线包括的节点
			logger.info("[XXX运段号：" + segVO.getVbillno() + "]根据路线读取路线的节点...");
			String sql = "SELECT addr.*,node.s_timeline as s_timeline ,node.line_pk_trans_type AS line_pk_trans_type, "
					+ "node.line_pk_driver AS line_pk_driver ,node.line_carno AS line_carno,node.line_pk_carrier AS line_pk_carrier "
					+ "FROM ts_address addr WITH(NOLOCK),ts_line_node node WITH(NOLOCK) "
					+ "WHERE addr.pk_address=node.pk_address AND isnull(addr.dr,0)=0 AND isnull(node.dr,0)=0 "
					+ "AND isnull(node.locked_flag,'N')='N' and node.pk_trans_line=? ";
			// 这里的AddressVO实际上扩展了ts_line_node的标准时效字段，下面需要根据该字段计算提货时间和到货时间
			List<AddressVO> addrVOs = dao.queryForList(sql, AddressVO.class, lineVO.getPk_trans_line());
			if(addrVOs == null || addrVOs.size() == 0) {
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]匹配到路线，但是路线没有定义节点，不能分段！");
			} else {
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]根据该路线读取到" + addrVOs.size() + "个节点...");
				// 根据标准时效算出要求提货时间及要求到货时间
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]根据标准时效算出要求提货时间及要求到货时间...");
				List<String> req_deli_date = new ArrayList<String>(); // 提货时间
				List<String> req_arri_date = new ArrayList<String>(); // 到货时间
				for(int i = 0; i < addrVOs.size(); i++) {
					AddressVO addrVO = addrVOs.get(i);
					// 到达时间
					String beginDate = null;
					if(req_deli_date.size() == 0) {
						beginDate = segVO.getReq_deli_date() == null ? null : segVO.getReq_deli_date().toString();
					} else {
						beginDate = req_deli_date.get(i - 1);
					}
					if(beginDate != null) {
						Date date = DateUtils.parseString(beginDate);
						if(addrVO.getS_timeline() != null) {
							date = DateUtils.addHour(DateUtils.parseString(beginDate), addrVO.getS_timeline()
									.doubleValue());
						}
						String sDate = DateUtils.formatDate(date, DateUtils.DATETIME_FORMAT_HORIZONTAL);
						
						UFDateTime sDateTime = new UFDateTime(sDate);
						UFDateTime segReq_arri_date = new UFDateTime(segVO.getReq_arri_date());
						if(sDateTime.after(segReq_arri_date)){
							logger.info("要求到货时间超出！");
							//当要求到货时间超出时，取上一个点的的要求到货时间
							req_arri_date.add(segReq_arri_date.toString());
							req_deli_date.add(segReq_arri_date.toString());
						}else{
							// 这里到货日期就是下一个节点的提货日期
							req_arri_date.add(sDate);//
							req_deli_date.add(sDate);//
						}
					}
				}
				// 如果选择了多条运段进行自动分段，实际上自动运段一般只有一条
				List<SegmentVO> newSegVOs = new ArrayList<SegmentVO>();
				newSegVOs.add(segVO);
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]开始分段...");
				distSection(addrVOs, req_deli_date, req_arri_date, newSegVOs, packVOs);
				logger.info("[XXX运段号：" + segVO.getVbillno() + "]完成分段...");
				index++;
			}

		}
	}

	public List<SegmentVO> saveDistQuantity(String pk_segment, String[] pk_seg_pack_b,
			UFDouble[] dist_pack_num_countAry, int[] dist_numAry, UFDouble[] dist_weightAry, UFDouble[] dist_volumeAry) {
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		SegmentVO parentSegVO = this.getByPrimaryKey(SegmentVO.class, pk_segment); // 被分量的父级运段
		if(parentSegVO.getVbillstatus() != BillStatus.SEG_WPLAN){
			throw new BusiException("当前运段已被调度，请刷新页面！");
		}
		if(parentSegVO.getSeg_mark() == SegmentConst.SEG_MARK_PARENT){
			throw new BusiException("当前运段已被拆量，请刷新页面！");
		}
		parentSegVO.setSeg_mark(SegmentConst.SEG_MARK_PARENT); // 先定义为父级运段，该运段会隐藏
		parentSegVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(parentSegVO);

		// 拆分的
		SegmentVO distSegVO = parentSegVO.clone();
		distSegVO.setDbilldate(new UFDate());
		distSegVO.setVbillno(parentSegVO.getVbillno() + "-001");
		distSegVO.setSeg_type(SegmentConst.QUANTITY); // 分量运段
		distSegVO.setParent_seg(parentSegVO.getPk_segment()); // 设置父级的运段
		distSegVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD); // 设置为子级
		distSegVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		distSegVO.setCreate_time(new UFDateTime(new Date()));
		distSegVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		distSegVO.setStatus(VOStatus.NEW);
		distSegVO.setPk_segment(null);
		NWDao.setUuidPrimaryKey(distSegVO);
		toBeUpdate.add(distSegVO);
		// 拆分后的
		SegmentVO remainSegVO = parentSegVO.clone();
		remainSegVO.setDbilldate(new UFDate());
		remainSegVO.setVbillno(parentSegVO.getVbillno() + "-002");
		remainSegVO.setSeg_type(SegmentConst.QUANTITY); // 分量运段
		remainSegVO.setParent_seg(parentSegVO.getPk_segment()); // 设置父级的运段
		remainSegVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD); // 设置为子级
		remainSegVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		remainSegVO.setCreate_time(new UFDateTime(new Date()));
		remainSegVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		remainSegVO.setStatus(VOStatus.NEW);
		remainSegVO.setPk_segment(null);
		NWDao.setUuidPrimaryKey(remainSegVO);
		toBeUpdate.add(remainSegVO);

		int dist_num_count = 0, remain_num_count = 0;
		double dist_pack_num_count = 0, dist_weight_count = 0, dist_volume_count = 0, remain_pack_num_count = 0, remain_weight_count = 0, remain_volume_count = 0;
		for(int i = 0; i < pk_seg_pack_b.length; i++) {
			SegPackBVO packVO = this.getByPrimaryKey(SegPackBVO.class, pk_seg_pack_b[i]); // 得到包装VO
			// 拆分的数量、件数、重量、体积
			double dist_pack_num = dist_pack_num_countAry[i].doubleValue();
			int dist_num = dist_numAry[i];
			double dist_weight = dist_weightAry[i].doubleValue();
			double dist_volume = dist_volumeAry[i].doubleValue();
			if(dist_num == 0 && dist_weight == 0 && dist_volume == 0) {
				// 这个货品没有拆分
			} else {
				// 拆分的货品
				SegPackBVO distPackVO = packVO.clone();
				distPackVO.setPack_num_count(new UFDouble(dist_pack_num));
				distPackVO.setNum(dist_num);
				distPackVO.setPlan_num(dist_num);
				distPackVO.setWeight(new UFDouble(dist_weight));
				distPackVO.setVolume(new UFDouble(dist_volume));
				distPackVO.setPk_segment(distSegVO.getPk_segment());
				distPackVO.setStatus(VOStatus.NEW);
				distPackVO.setPk_seg_pack_b(null);// 重新设置主键
				NWDao.setUuidPrimaryKey(distPackVO);
				toBeUpdate.add(distPackVO);
			}
			// 计算拆分的货品的总件数、总重量、总体积，要更新到主表
			// 将数量更新到主表的总数量中
			dist_pack_num_count += dist_pack_num;
			dist_num_count += dist_num;
			dist_weight_count += dist_weight;
			dist_volume_count += dist_volume;
			// 拆分后剩余的数量、件数、重量、体积
			if(packVO.getPack_num_count() == null) {
				packVO.setPack_num_count(UFDouble.ZERO_DBL);
				// 这个值一般不会为null，主要是之前没有这个字段，后来加上的
			}

			double remain_pack_num = packVO.getPack_num_count().doubleValue() - dist_pack_num;
			int remain_num = packVO.getNum().intValue() - dist_num;
			double remain_weight = packVO.getWeight() == null ? 0.0 : packVO.getWeight().doubleValue() - dist_weight;
			double remain_volume = packVO.getVolume() == null ? 0.0 : packVO.getVolume().doubleValue() - dist_volume;
			if(remain_num == 0 && remain_weight == 0 && remain_volume == 0) {
				// 这个货品已经被拆分完了
			} else {
				// 拆分后的货品
				SegPackBVO remainPackVO = packVO.clone();
				remainPackVO.setPack_num_count(new UFDouble(remain_pack_num));
				remainPackVO.setNum(remain_num);
				remainPackVO.setPlan_num(remain_num);
				remainPackVO.setWeight(new UFDouble(remain_weight));
				remainPackVO.setVolume(new UFDouble(remain_volume));
				remainPackVO.setPk_segment(remainSegVO.getPk_segment());
				remainPackVO.setStatus(VOStatus.NEW);
				remainPackVO.setPk_seg_pack_b(null);
				NWDao.setUuidPrimaryKey(remainPackVO);
				toBeUpdate.add(remainPackVO);
			}
			// 计算拆分后的货品的总数量、总件数、总重量、总体积，要更新到主表
			remain_pack_num_count += remain_pack_num;
			remain_num_count += remain_num;
			remain_weight_count += remain_weight;
			remain_volume_count += remain_volume;
		}
		distSegVO.setPack_num_count(new UFDouble(dist_pack_num_count));
		distSegVO.setNum_count(dist_num_count);
		distSegVO.setWeight_count(new UFDouble(dist_weight_count));
		distSegVO.setVolume_count(new UFDouble(dist_volume_count));
		// 计算总计费重,总体积重
		UFDouble fee_weight_count = distSegVO.getWeight_count();
		UFDouble volume_weight_count = remainSegVO.getVolume_count();

		// 根据运段读取客户
		String pk_customer = NWDao
				.getInstance()
				.queryForObject(
						"select pk_customer from ts_invoice WITH(NOLOCK) where vbillno=(select invoice_vbillno from ts_segment where pk_segment=?)",
						String.class, parentSegVO.getPk_segment());
		UFDouble rate = custService.getFeeRate(pk_customer, distSegVO.getPk_trans_type(), distSegVO.getDeli_city(), distSegVO.getArri_city());
		if(rate != null && rate.doubleValue() != 0) {
			volume_weight_count = distSegVO.getVolume_count().multiply(rate);
			if(volume_weight_count.doubleValue() < distSegVO.getWeight_count().doubleValue()) {
				fee_weight_count = distSegVO.getWeight_count();
			} else {
				fee_weight_count = volume_weight_count;
			}
		}
		distSegVO.setFee_weight_count(fee_weight_count);
		distSegVO.setVolume_weight_count(volume_weight_count);

		remainSegVO.setPack_num_count(new UFDouble(remain_pack_num_count));
		remainSegVO.setNum_count(remain_num_count);
		remainSegVO.setWeight_count(new UFDouble(remain_weight_count));
		remainSegVO.setVolume_count(new UFDouble(remain_volume_count));
		// 计算总计费重
		fee_weight_count = remainSegVO.getWeight_count();
		volume_weight_count = remainSegVO.getVolume_count();
		rate = custService.getFeeRate(pk_customer, remainSegVO.getPk_trans_type(), remainSegVO.getDeli_city(), remainSegVO.getArri_city());
		if(rate != null && rate.doubleValue() != 0) {
			volume_weight_count = remainSegVO.getVolume_count().multiply(rate);
			if(volume_weight_count.doubleValue() < remainSegVO.getWeight_count().doubleValue()) {
				fee_weight_count = remainSegVO.getWeight_count();
			} else {
				fee_weight_count = volume_weight_count;
			}
		}
		remainSegVO.setFee_weight_count(fee_weight_count);
		remainSegVO.setVolume_weight_count(volume_weight_count);

		dao.saveOrUpdate(toBeUpdate);
		return null;
	}

	public List<SegmentVO> saveCancelSection(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			throw new RuntimeException("参数不对，不需要撤销！");
		}
		return cancelSectionOrQuantity(pk_segment, SegmentConst.SECTION);
	}

	public List<SegmentVO> saveCancelQuantity(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			throw new RuntimeException("参数不对，不需要撤销！");
		}
		return cancelSectionOrQuantity(pk_segment, SegmentConst.QUANTITY);
	}

	/**
	 * 撤销分量和撤销分段差不多
	 * 
	 * @param pk_segment
	 * @param seg_type
	 * @return
	 */
	private List<SegmentVO> cancelSectionOrQuantity(String[] pk_segment, int seg_type) {
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(String pk : pk_segment) {
			SegmentVO segVO = dao.queryByPK(SegmentVO.class, pk);
			// 0、只有待计划的分段才能撤销
			// 1、运段类型是分量运段的才能撤销 ,只有这条规则与撤销分段不一样
			// 2、如果是父级运段，则不能撤销
			// 3、其同级的运段还存在子运段，那么不能撤销
			if(segVO.getVbillstatus().intValue() != BillStatus.SEG_WPLAN) {
				throw new RuntimeException("只有待调度的分段才能撤销！");
			}
			if(segVO.getSeg_type().intValue() != seg_type) {
				throw new RuntimeException("只有运段类型为分段运段的运段才可以进行撤销！");
			}
			if(segVO.getSeg_mark().intValue() == SegmentConst.SEG_MARK_PARENT) {
				throw new RuntimeException("运段标识为父级运段的运段不能撤销！");
			}
			// 得到父级运段
			SegmentVO parentSegVO = dao.queryByCondition(SegmentVO.class, "pk_segment=?", segVO.getParent_seg());
			// 不存在父级运段，不需要撤销
			if(parentSegVO == null) {
				throw new RuntimeException("不存在父级运段，不需要撤销！");
			}
			// 返回同级运段
			SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "parent_seg=?",
					segVO.getParent_seg());
			for(SegmentVO newSegVO : segVOs) {
				if(newSegVO.getSeg_mark().intValue() == SegmentConst.SEG_MARK_PARENT) {
					throw new RuntimeException("该运段的同级运段还存在子运段，不能撤销！");
				}
				if(newSegVO.getVbillstatus().intValue() != BillStatus.SEG_WPLAN) {
					throw new RuntimeException("只有所有子运段都为[待调度]状态才能撤销！");
				}
				newSegVO.setStatus(VOStatus.DELETED); // 标记为删除
				toBeUpdate.add(newSegVO);

				// 删除运段的包装信息
				SegPackBVO[] packVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?",
						newSegVO.getPk_segment());
				for(SegPackBVO packVO : packVOs) {
					packVO.setStatus(VOStatus.DELETED);
					toBeUpdate.add(packVO);
				}
			}

			// 父级运段是否还包含父级运段，如果还有、那么运段标示改成2，否则改成0（原始运段）
			if(StringUtils.isBlank(parentSegVO.getParent_seg())) {
				parentSegVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);
			} else {
				parentSegVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
			}
			parentSegVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(parentSegVO);
		}
		dao.saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		return null;
	}

	public String[] getExportFilterAry(ParamVO paramVO) {
		return new String[] { "_processor" };
	}

	public List<Map<String, Object>> doDelegate(String pk_corp, String[] pk_segment, ParamVO paramVO) {
		if(StringUtils.isBlank(pk_corp) || pk_segment == null || pk_segment.length == 0) {
			return null;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		String cond = NWUtils.buildConditionString(pk_segment);
		SegmentVO[] segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				"pk_segment in " + cond);
		if(segVOs != null) {
			for(SegmentVO segVO : segVOs) {
				if(segVO.getVbillstatus() != BillStatus.SEG_WPLAN) {
					logger.warn("运段[" + segVO.getVbillno() + "]不是待调度状态，不能委派！");
					continue;
				}
				segVO.setDelegate_corp(pk_corp);
				segVO.setDelegate_status(SegmentConst.DELEGATE_STATUS_DO);
				segVO.setDelegate_user(WebUtils.getLoginInfo().getPk_corp());
				segVO.setDelegate_time(new UFDateTime(new Date()));
				segVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(segVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		// 执行公式并返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> datas = execFormula4Templet(paramVO, toBeUpdate);
		for(Map<String, Object> data : datas) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.HEADER, data);
			retList.add(map);
		}
		return retList;
	}

	public List<Map<String, Object>> cancelDelegate(String[] pk_segment, ParamVO paramVO) {
		if(pk_segment == null || pk_segment.length == 0) {
			return null;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		String cond = NWUtils.buildConditionString(pk_segment);
		SegmentVO[] segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				"pk_segment in " + cond);
		if(segVOs != null) {
			for(SegmentVO segVO : segVOs) {
				if(segVO.getVbillstatus() != BillStatus.SEG_WPLAN) {
					logger.warn("运段[" + segVO.getVbillno() + "]不是待调度状态，不能撤销委派！");
					continue;
				}
				if(segVO.getDelegate_status() != null && segVO.getDelegate_status() == SegmentConst.DELEGATE_STATUS_NEW) {
					logger.warn("运段[" + segVO.getVbillno() + "]已经是待委派状态，不需要撤销");
				} else {
					segVO.setDelegate_corp(null);
					segVO.setPk_carrier(null);
					segVO.setDelegate_status(SegmentConst.DELEGATE_STATUS_NEW);
					segVO.setDelegate_user(null);
					segVO.setDelegate_time(null);
					segVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(segVO);
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		// 执行公式并返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> datas = execFormula4Templet(paramVO, toBeUpdate);
		for(Map<String, Object> data : datas) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.HEADER, data);
			retList.add(map);
		}
		return retList;
	}
	
	//yaojiie 2015 10 16 添加ＧＰＳ跟踪车辆相关功能
	
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
			throw new BusiException("[?]文件不存在!",path );
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
	

	public RootVO getCurrentTrackVO(String carno) {
		if(StringUtils.isBlank(carno)) {
			return null;
		}
		String sql = "select gps_id from ts_car WITH(NOLOCK) where pk_car=?";
		String gpsid = NWDao.getInstance().queryForObject(sql, String.class,carno);
		if(StringUtils.isBlank(carno)) {
			throw new BusiException("车牌号[?]的车辆没有关联GPS_ID！",carno);
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
		logger.info("设备号：" + gpsid);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("gpsID", gpsid);
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

	/**
	 * 同步订单 songf  2015-12-14
	 * 
	 * @param pk_segment
	 * @param paramVO
	 * @return
	 */
	public void syncOrders(String[] pk_segment, ParamVO paramVO) {
		if(pk_segment == null || pk_segment.length == 0) {
			return ;
		}
		String cond = NWUtils.buildConditionString(pk_segment);
	
		try {

			// 记录成功的资料及能力信息和失败的资料及能力信息的明细主键值
			List<String> listSucces = new ArrayList<String>();
			List<String> listFailed = new ArrayList<String>();
			logger.info("开始同步订单信息。");
			syncSegment(cond, listSucces, listFailed);
			logger.info("订单信息同步结束。");
			
			//更新统计信息
			afterSyncSegmentData(listSucces, listFailed);
			
		} catch (Exception e) {
			// 记录失败的ID
			// listFailed.add(strPkAddress);
			e.printStackTrace();
		}
	}
	
	/**
	 * 同步运段信息 songf 2015-12-14
	 * 
	 * @param strPkSegment 运段主键值（XXX，XXX，XXX）逗号分隔
	 * @param listSucces 同步成功的主键列表
	 * @param listFialed 同步失败的主键列表
	 */
	@SuppressWarnings("rawtypes")
	private void syncSegment(String strPkSegment, List<String> listSucces, List<String> listFailed) {
		// soap头
		//String strHeader = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:cais=\"http://www.ortec.com/CAIS\"><soap:Header/>"
		//		+ "<soap:Body><cais:SendMessage xmlns=\"http://www.ortec.com/CAIS\">";
		// soap尾
		//String strTail = "<cais:commandName>ImportOrder</cais:commandName></cais:SendMessage></soap:Body></soap:Envelope>";

		// 查询运段信息
		String sqlSegment = "SELECT  ts.pk_segment , ts.vbillno AS id,ts.invoice_vbillno AS order_number, left(ts.create_time,10)  AS order_date ,tss.supp_code AS contactId,tspb.goods_code as productId ,"
				+ " ts.weight_count as kg, ts.volume_count as m3 ,ts.num_count as plts, "
				+ " ti.def2 AS department_code,"
				+ " ti.memo as comment ,deli_add.addr_code as pkicup_addressId ,CONVERT(char(19), CAST(ts.req_deli_date AS DATETIME), 126) as pkicup_from_instant, CONVERT(char(19), CAST(ts.req_deli_time AS DATETIME), 126) as pkicup_till_instant , "
				+ " arri_add.addr_code as delivery_addressId,CONVERT(char(19), CAST(ts.req_arri_date AS DATETIME), 126) as delivery_from_instant, CONVERT(char(19), CAST(ts.req_arri_time AS DATETIME), 126) as delivery_till_instant,"
				+ " (CASE  WHEN ts.seg_mark=0 THEN 0 WHEN ts.seg_mark=2 THEN 1 END ) AS  divided,"
				+ " ti.def5 AS customer_order "
				+ " FROM ts_segment ts WITH(NOLOCK) "
				+ " LEFT JOIN ts_seg_pack_b tspb WITH(NOLOCK) ON ts.pk_segment=tspb.pk_segment "
				+ " AND tspb.dr=0 LEFT JOIN ts_address  deli_add WITH(NOLOCK) ON ts.pk_delivery=deli_add.pk_address "
				+ " AND deli_add.dr=0 LEFT JOIN ts_address  arri_add WITH(NOLOCK) ON ts.pk_arrival=arri_add.pk_address "
				+ " AND arri_add.dr=0 LEFT JOIN ts_invoice  ti WITH(NOLOCK) ON ts.invoice_vbillno=ti.vbillno AND ti.dr=0 "
				+ " LEFT JOIN ts_supplier tss WITH(NOLOCK) ON ti.pk_supplier=tss.pk_supplier AND tss.dr=0 "
				+ " WHERE ts.dr=0 and ts.pk_segment in ";
		
		// 开始执行时间
		Calendar start = Calendar.getInstance();

		// 记录数据主键值
		String str_pk_segment = "";
		try {
	
		

			// 获取运段信息
			sqlSegment = sqlSegment  + strPkSegment;
			List<HashMap> mapSegmentList = NWDao.getInstance().queryForList(sqlSegment, HashMap.class);
			logger.info("共查询到" + mapSegmentList.size() + "条订单信息需要进行同步操作。");
			if (mapSegmentList != null && mapSegmentList.size() > 0) {
				for (int i = 0; i < mapSegmentList.size(); i++) {
					HashMap segmentRowMap = mapSegmentList.get(i);
					String strid = String.valueOf(segmentRowMap.get("id"));
					str_pk_segment = String.valueOf(segmentRowMap.get("pk_segment"));
					String strOrderNum = String.valueOf(segmentRowMap.get("order_number"));
					String strOrderDate = String.valueOf(segmentRowMap.get("order_date"));
					String strContactID = String.valueOf(segmentRowMap.get("contactid"));
					String strProductId = String.valueOf(segmentRowMap.get("productid"));
					String strKg = segmentRowMap.get("kg").toString();
					String strM3 = segmentRowMap.get("m3").toString();
					String strPlts = String.valueOf(segmentRowMap.get("plts"));
					String strDepartment_code = String.valueOf(segmentRowMap.get("department_code"));
					String strComment = String.valueOf(segmentRowMap.get("comment"));
					
					String strPkicup_addressId = String.valueOf(segmentRowMap.get("pkicup_addressid"));
					String strPkicup_from_instant= String.valueOf(segmentRowMap.get("pkicup_from_instant"));
					String strPkicup_till_instant = String.valueOf(segmentRowMap.get("pkicup_till_instant"));
					String strDelivery_addressId= String.valueOf(segmentRowMap.get("delivery_addressid"));
					String strDelivery_from_instant = String.valueOf(segmentRowMap.get("delivery_from_instant"));
					String strDelivery_till_instant = String.valueOf(segmentRowMap.get("delivery_till_instant"));
					
					String strDivided = String.valueOf(segmentRowMap.get("divided"));
					String strCustomer_order = String.valueOf(segmentRowMap.get("customer_order"));
					
					// 创建xml格式数据
					Document document = DocumentHelper.createDocument();
					//Element root = document.addElement("message");
					//Element nodeComtec = root.addElement("comtec");
					Element nodeOrder = document.addElement("transport_order");
					nodeOrder.addElement("id").addText(strid);
					nodeOrder.addElement("order_number").addText(strOrderNum);
					nodeOrder.addElement("Customer_order").addText(strCustomer_order);
					nodeOrder.addElement("order_date").addText(strOrderDate);
					nodeOrder.addElement("contactId").addText(strContactID);
					nodeOrder.addElement("productId").addText(strProductId);
					
					Element nodeAmounts = nodeOrder.addElement("amounts");
					Element nodeAmount1 = nodeAmounts.addElement("amount");
					nodeAmount1.addElement("unit_code").addText("kg");
					nodeAmount1.addElement("value").addText(strKg);
					Element nodeAmount2 = nodeAmounts.addElement("amount");
					nodeAmount2.addElement("unit_code").addText("plts");
					nodeAmount2.addElement("value").addText(strPlts);
					Element nodeAmount3 = nodeAmounts.addElement("amount");
					nodeAmount3.addElement("unit_code").addText("m3");
					nodeAmount3.addElement("value").addText(strM3);
					
					nodeOrder.addElement("department_code").addText(strDepartment_code);
					nodeOrder.addElement("comment").addText(strComment);
					
					Element node_pickup_task = nodeOrder.addElement("pickup_task");
					node_pickup_task.addElement("addressId").addText(strPkicup_addressId);
					Element node_pickup_task_window = node_pickup_task.addElement("task_window");
					node_pickup_task_window.addElement("from_instant").addText(strPkicup_from_instant);
					node_pickup_task_window.addElement("till_instant").addText(strPkicup_till_instant);
					
					Element node_delivery_task = nodeOrder.addElement("delivery_task");
					node_delivery_task.addElement("addressId").addText(strDelivery_addressId);
					Element node_delivery_task_window = node_delivery_task.addElement("task_window");
					node_delivery_task_window.addElement("from_instant").addText(strDelivery_from_instant);
					node_delivery_task_window.addElement("till_instant").addText(strDelivery_till_instant);
					
					nodeOrder.addElement("divided").addText(strDivided);
				
					
					// 生成消息 体
					Element rootMessage = document.getRootElement();
					String strMessage = rootMessage.asXML();

					//调用JNI接口
					RouteClient client = new RouteClient();
				    RouteVO routeVO = new RouteVO();
					routeVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(routeVO);
					routeVO.setCommandname("ImportOrder");
					routeVO.setMessage(strMessage);
					NWDao.getInstance().saveOrUpdate(routeVO);
					String returnMsg =client.SendMessage(routeVO.getPrimaryKey(), "yuliuziduan");
					
					if ("TRUE".equalsIgnoreCase(returnMsg)){
						String instersql="INSERT INTO edi_his_route SELECT * FROM edi_route WITH(NOLOCK) WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						String delsql="DELETE edi_route WHERE pk_route='"+routeVO.getPrimaryKey()+"'";

						dao.getJdbcTemplate().execute(instersql);
						dao.getJdbcTemplate().execute(delsql);
						
						if (!listSucces.contains(str_pk_segment)) {
							listSucces.add(str_pk_segment);
						}
						
					}else{
						String updatesql="UPDATE edi_route SET syncexp_flag='Y' , syncexp_memo='"+returnMsg+ "' WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						dao.getJdbcTemplate().execute(updatesql);
						if (!listFailed.contains(str_pk_segment)) {
							listFailed.add(str_pk_segment);
						}
						
					}

				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFailed.contains(str_pk_segment)) {
				listFailed.add(str_pk_segment);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");

	}

	/**
	 * 数据同步后处理，更新 ts_segment的状态  songf 2015-12-14
	 * 
	 * @param listSucces
	 *            同步成功明细主键列表
	 * @param listFailed
	 *            同步失败明细主键列表
	 */
	private void afterSyncSegmentData( List<String> listSucces, List<String> listFailed) {
		try {
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			List<SuperVO> segmentUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_segment 将同步成功的明细def1 改为Y
			if (listSucces.size() > 0) {
				SegmentVO[] segmentVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
						"pk_segment in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (segmentVOSuccs != null && segmentVOSuccs.length > 0) {
					for (SegmentVO segmentVO : segmentVOSuccs) {

						// 更新状态为已同步
						segmentVO.setDef1("Y");
						segmentVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						segmentUpdateSuccessList.add(segmentVO);
					}
				}
			}
			// 保存数据
			if (segmentUpdateSuccessList != null && segmentUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(segmentUpdateSuccessList);
			}
			
			List<SuperVO> segmentUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_segment 将同步失败的明细def1 改为N
			if(listFailed.size() > 0){
				SegmentVO[] segmentVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
						"pk_segment in "
								+ NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (segmentVOSuccs != null && segmentVOSuccs.length > 0) {
					for (SegmentVO segmentVO : segmentVOSuccs) {

						// 更新状态为已同步
						segmentVO.setDef1("N");
						segmentVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						segmentUpdateFailedList.add(segmentVO);
					}
				}
			}			
			// 保存数据
			if (segmentUpdateFailedList != null && segmentUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(segmentUpdateFailedList);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Map<String, Object>> delegateCarrier(String[] billIds, String carrier,ParamVO paramVO) {
		SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				"pk_segment in " + NWUtils.buildConditionString(billIds));
		if(segmentVOs == null || segmentVOs.length == 0){
			return null;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(SegmentVO segmentVO : segmentVOs){
			if(segmentVO.getDelegate_status() != null
					&& segmentVO.getDelegate_status() == SegmentConst.DELEGATE_STATUS_DO){
				throw new BusiException("操作失败，运段[?]，已委派！",segmentVO.getVbillno());
			}
			segmentVO.setPk_carrier(carrier);
			segmentVO.setDelegate_status(SegmentConst.DELEGATE_STATUS_DO);
			segmentVO.setDelegate_user(WebUtils.getLoginInfo().getPk_corp());
			segmentVO.setDelegate_time(new UFDateTime(new Date()));
			segmentVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(segmentVO);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		// 执行公式并返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> datas = execFormula4Templet(paramVO, toBeUpdate);
		for(Map<String, Object> data : datas) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.HEADER, data);
			retList.add(map);
		}
		return retList;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> getFullLoadRates(String[] ids,String[] cars) {
		Map<String,String> result = new HashMap<String,String>();
		// 用于存储查询数据
		final List<String> retList = new ArrayList<String>();
		// 调用存储过程
		// 存储过程名称
		final String TS_ASSIGN_RULE_PROC = "ts_full_load_rates_proc";
		final String pk_ids = NWUtils.join(ids, ",");
		final String pk_cars = NWUtils.join(cars, ",");
		final String pk_user = WebUtils.getLoginInfo().getPk_user();
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(TS_ASSIGN_RULE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_ids);
					cs.setString(2, pk_cars);
					cs.setString(3, pk_user);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs0 = cs.executeQuery();
					while (rs0.next()) {
						retList.add(0, rs0.getString("num_count"));
						retList.add(1, rs0.getString("volume_count"));
						retList.add(2, rs0.getString("weight_count"));
						retList.add(3, rs0.getString("volume_rate"));
						retList.add(4, rs0.getString("weight_rate"));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.put("num_count", retList.get(0));
		result.put("volume_count", retList.get(1));
		result.put("weight_count", retList.get(2));
		result.put("volume_rate", retList.get(3));
		result.put("weight_rate", retList.get(4));
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getProcDatas(String[] ids, String pk_car) {
		Map<String,Object> result = new HashMap<String,Object>();
		final List<String> MERGE_MESG = new ArrayList<String>();
		final List<Map<String,String>> CARRIERS = new ArrayList<Map<String,String>>();
		final List<Map<String,String>> CARS = new ArrayList<Map<String,String>>();
		final List<String> CARNOS = new ArrayList<String>();
		final Map<String,String> FULL_LOAD = new HashMap<String,String>();
		final String TS_ASSIGN_RULE_PROC = "ts_seg_proc";
		final String pk_ids = NWUtils.join(ids, ",");
		final String pk_user = WebUtils.getLoginInfo().getPk_user();
		final String pk_cars = pk_car;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(TS_ASSIGN_RULE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_ids);
					cs.setString(2, pk_user);
					cs.setString(3, pk_cars);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					//合并提示
					ResultSet rs0 = cs.getResultSet();
					while (rs0.next()) {
						MERGE_MESG.add(rs0.getString(1));
					}
					if(cs.getMoreResults()){
						ResultSet rs1 = cs.getResultSet();
						while (rs1.next()) {
							Map<String,String> carrMap = new HashMap<String,String>();
							
							carrMap.put("pk_carrier", rs1.getString("pk_carrier"));
							carrMap.put("carr_name", rs1.getString("carr_name"));
							carrMap.put("trans_type_name", rs1.getString("trans_type_name"));
							carrMap.put("pk_trans_type", rs1.getString("pk_trans_type"));
							carrMap.put("car_avallable_account", rs1.getString("car_avallable_account"));
							carrMap.put("ent_count", rs1.getString("ent_count"));
							carrMap.put("accid_count", rs1.getString("accid_count"));
							carrMap.put("price", rs1.getString("price"));
							carrMap.put("kpi", rs1.getString("kpi"));
							CARRIERS.add(carrMap);
						}
					}
					if(cs.getMoreResults()){
						ResultSet rs2 = cs.getResultSet();
						while (rs2.next()) {
							Map<String,String> carMap = new HashMap<String,String>();
							CARNOS.add(rs2.getString("carno"));
							carMap.put("pk_car", rs2.getString("pk_car"));
							carMap.put("carno", rs2.getString("carno"));
							carMap.put("carType",rs2.getString("ent_num") +" " + rs2.getString("carType"));
							carMap.put("pk_carrier", rs2.getString("pk_carrier"));
							//carMap.put("ent_num", rs2.getString("ent_num"));
							CARS.add(carMap);
						}
					}
					if(cs.getMoreResults()){
						ResultSet rs3 = cs.getResultSet();
						while (rs3.next()) {
							FULL_LOAD.put("volume_rate", rs3.getString("volume_rate"));
							FULL_LOAD.put("weight_rate", rs3.getString("weight_rate"));
						}
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.put("merge_msg", MERGE_MESG.get(0));
		result.put("carriers", CARRIERS);
		for(Map<String,String> carMap : CARS){
			carMap.put("carInfo", getCarInfo(carMap.get("carno").toString(), carMap.get("pk_car").toString()));
		}
		result.put("cars", CARS);
		result.put("full_load", FULL_LOAD);
		return result;
	}

	public List<SegmentVO> getSegments(String[] ids) {
		List<SegmentVO> segs = new ArrayList<SegmentVO>();
		if(ids == null || ids.length == 0){
			return segs;
		}
		String sql = "SELECT * FROM ts_segment WHERE isnull(parent_seg,'')='' AND vbillstatus = 10"
				+ " AND isnull(dr,0)=0 AND seg_mark = 0 AND invoice_vbillno IN " + NWUtils.buildConditionString(ids);
		segs = NWDao.getInstance().queryForList(sql, SegmentVO.class);
		return segs;
	}
	
	public String doImport(ParamVO paramVO, File file, String pk_import_config) throws Exception {
		logger.info("客户信息导入开始...");
		Calendar start = Calendar.getInstance();
		paramVO.setFunCode("t30226");
		pk_import_config = pk_import_config.substring(pk_import_config.indexOf(",")+1, pk_import_config.length());
		ImportConfigVO configVO = NWDao.getInstance().queryByCondition(ImportConfigVO.class, "pk_import_config=?", pk_import_config);
		if(configVO == null || StringUtils.isBlank(configVO.getImporter())){
			throw new BusiException("导入模板维护错误！");
		}
		ExcelImporter importer = new BatchSchedulingExcelImporter(paramVO, pzService,configVO);
		importer._import(file);
		logger.info("客户信息导入结束，耗时" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒.");
		return importer.getLog();
	}

	public Map<String, Object> mergeSegments(String[] billIds, ParamVO paramVO) {
		//将多个运段合并
		if(billIds == null || billIds.length < 2){
			throw new BusiException("请至少选择两条记录！");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + NWUtils.buildConditionString(billIds) + " order by vbillno");
		String invoiceVbillno = null;
		SegmentVO tempSegmentVO = null;
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble pack_num_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_weight_count = UFDouble.ZERO_DBL;
		for(SegmentVO segmentVO : segmentVOs){
			if(segmentVO.getVbillstatus() != BillStatus.SEG_WPLAN){
				throw new BusiException("只有未调度的运段才允许合并[?]",segmentVO.getVbillno());
			}
			if(invoiceVbillno == null){
				invoiceVbillno = segmentVO.getInvoice_vbillno();
			}
			if(!segmentVO.getInvoice_vbillno().equals(invoiceVbillno)){
				throw new BusiException("只有相同发货单的运段才允许合并[?]",segmentVO.getVbillno());
			}
			if(segmentVO.getSeg_type() != SegmentConst.QUANTITY){
				throw new BusiException("只有分量运段才允许合并[?]",segmentVO.getVbillno());
			}
			if(tempSegmentVO == null){
				tempSegmentVO = segmentVO;
			}else{
				segmentVO.setStatus(VOStatus.DELETED);
				toBeUpdate.add(segmentVO);
			}
			num_count += segmentVO.getNum_count();
			weight_count = weight_count.add(segmentVO.getWeight_count());
			volume_count = volume_count.add(segmentVO.getVolume_count());
			pack_num_count = pack_num_count.add(segmentVO.getPack_num_count());
			volume_weight_count = volume_weight_count.add(segmentVO.getVolume_weight_count());
			fee_weight_count = fee_weight_count.add(segmentVO.getFee_weight_count());
		}
		String sql = "SELECT  pk_invoice, pk_goods, goods_code, goods_name,pack,pk_inv_pack_b,unit_weight, unit_volume, length, width, height,"
				+ "	sum (isnull(num,0)) AS num,sum (isnull(weight,0)) AS weight,sum (isnull(volume,0)) AS volume,"
				+ " sum (isnull(pack_num_count,0)) AS pack_num_count,sum (isnull(plan_pack_num_count,0)) AS plan_pack_num_count,sum (isnull(plan_num,0)) AS plan_num "
				+ " FROM ts_seg_pack_b WITH(NOLOCK)"
				+ " WHERE isnull(dr,0)=0 AND ts_seg_pack_b.pk_segment IN " + NWUtils.buildConditionString(billIds)
				+ " GROUP BY dr,pk_invoice, pk_goods, goods_code, goods_name,pack,pk_inv_pack_b,unit_weight, unit_volume,length, width, height";
		List<SegPackBVO> segPackBVOs = NWDao.getInstance().queryForList(sql,SegPackBVO.class);
		//tempSegmentVO.
		if(segPackBVOs != null && segPackBVOs.size() > 0){
			for(SegPackBVO segPackBVO : segPackBVOs){
				segPackBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(segPackBVO);
				segPackBVO.setPk_segment(tempSegmentVO.getPk_segment());
				toBeUpdate.add(segPackBVO);
			}
			//计费重信息
		}
		//删除原有的包装
		SegPackBVO[] oldPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment in " + NWUtils.buildConditionString(billIds));
		if(oldPackBVOs != null && oldPackBVOs.length > 0){
			for(SegPackBVO oldPackBVO : oldPackBVOs){
				oldPackBVO.setStatus(VOStatus.DELETED);
				toBeUpdate.add(oldPackBVO);
			}
		}
		tempSegmentVO.setNum_count(num_count);
		tempSegmentVO.setWeight_count(weight_count);
		tempSegmentVO.setVolume_count(volume_count);
		tempSegmentVO.setPack_num_count(pack_num_count);
		tempSegmentVO.setVolume_weight_count(volume_weight_count);
		tempSegmentVO.setFee_weight_count(fee_weight_count);
		tempSegmentVO.setVbillstatus(BillStatus.SEG_WPLAN);
		tempSegmentVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(tempSegmentVO);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return null;
	}

	public void dispatch(String pk_car, String pk_carrier, String[] pk_segments) {
		
		SegmentVO[] segmentVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class,
				"pk_segment in " + NWUtils.buildConditionString(pk_segments));
		// 校验所选运段对应的发货单不能重复
		List<String> invoiceVbillnoAry = new ArrayList<String>();
		boolean ifMergeSameInvoice = ParameterHelper.getIfMergeSameInvoice();
		for (SegmentVO segVO : segmentVOs) {
			if (invoiceVbillnoAry.contains(segVO.getInvoice_vbillno()) && !ifMergeSameInvoice) {
				throw new BusiException("相同发货单的运段不能一起配载！");
			} else {
				invoiceVbillnoAry.add(segVO.getInvoice_vbillno());
			}
		}
		
		CarVO carVO = new CarVO();
		if(StringUtils.isBlank(pk_carrier)){
			String sql = "SELECT * FROM ts_car WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N')='N' AND  pk_car = ? ";
			carVO = dao.queryForObject(sql, CarVO.class, pk_car);
			if(carVO == null || StringUtils.isBlank(carVO.getPk_carrier())){
				throw new BusiException("请先选择承运商！");
			}
			pk_carrier = carVO.getPk_carrier();
		}
		DriverVO driverVO = new DriverVO();
		if(StringUtils.isNotBlank(carVO.getPk_driver())){
			driverVO = dao.queryByCondition(DriverVO.class, "pk_driver=?", carVO.getPk_driver());
		}
		CarrierVO carrierVO = NWDao.getInstance().queryByCondition(CarrierVO.class, "pk_carrier=?", pk_carrier);
		
		
		ExAggEntrustVO pzAggVO = new ExAggEntrustVO();
		PZHeaderVO headerVO = new PZHeaderVO();
		headerVO.setPk_carrier(pk_carrier);
		headerVO.setPk_trans_type(segmentVOs[0].getPk_trans_type());
		headerVO.setCarno(carVO.getCarno());
		headerVO.setPk_driver(driverVO.getDriver_code());
		headerVO.setDriver_name(driverVO.getDriver_name());
		headerVO.setDriver_mobile(driverVO.getMobile());
		headerVO.setPk_car_type(carVO.getPk_car_type());
		headerVO.setBalatype(carrierVO.getBalatype() == null ? DataDictConst.BALATYPE.MONTH.intValue() : Integer.parseInt(carrierVO.getBalatype()));
		pzAggVO.setParentVO(headerVO);
		pzAggVO.setTableVO(TabcodeConst.TS_SEGMENT, segmentVOs);
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);
		pzService.save(pzAggVO, paramVO);
	}
}

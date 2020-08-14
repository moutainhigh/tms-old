package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.AggQueryTempletVO;
import org.nw.jf.vo.AggReportTempletVO;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.BillTempletVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.QueryTempletVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.redis.RedisDao;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.BillTempletService;
import org.nw.utils.NWUtils;
import org.nw.utils.TempletHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ExAggBillTempletVO;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.vo.sys.ReportTempletVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2012-11-21 下午07:39:56
 */
@Service
public class BillTempletServiceImpl extends AbsToftServiceImpl implements BillTempletService {

	public static final String TEMPLET = "TEMPLET";
	public static final String HEADERTABLE = "HEADERTABLE";
	public static final String BODYTABLE = "BODYTABLE";

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggBillTempletVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggBillTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, BillTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, BillTempletVO.PK_BILLTEMPLET);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggBillTempletVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, BillTempletBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, "pk_billtemplet");
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_billtemplet_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_billtemplet_b");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggBillTempletVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, BillTempletTVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, BillTempletTVO.PK_BILLTEMPLET);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "nw_billtemplet_t");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "nw_billtemplet_t");

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public List<String> selectUserTable(String keyword) {
		String sql = "select name from sysobjects where xtype='U'";// 查询所有用户表
		if(StringUtils.isNotBlank(keyword)) {
			sql += " and name like '" + keyword + "%'";
		}
		sql += "  order by name";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = " SELECT TABLE_NAME FROM USER_TABLES";
			if(StringUtils.isNotBlank(keyword)) {
				sql += " and TABLE_NAME like '" + keyword + "%'";
			}
			sql += "  order by TABLE_NAME";
		}
		return dao.queryForList(sql, String.class);
	}

	@SuppressWarnings("rawtypes")
	public List<HashMap> loadTemplet(String pk_billtypecode) {
		if(StringUtils.isBlank(pk_billtypecode)) {
			return null;
		}
		String strMainSql = "select nw_billtemplet.pk_billtemplet,nw_billtemplet.pk_billtypecode,"
				+ "nw_billtemplet.bill_templetname,nw_billtemplet.bill_templetcaption,nw_billtemplet.pk_corp,nw_corp.corp_name,"
				+ "nw_billtemplet.model_type,nw_billtemplet.options,nw_billtemplet.shareflag,nw_billtemplet.nodecode,nw_billtemplet.resid ";
		strMainSql = strMainSql
				+ "from nw_billtemplet WITH(NOLOCK) left join nw_corp WITH(NOLOCK) on nw_billtemplet.pk_corp=nw_corp.pk_corp where nw_billtemplet.pk_billtemplet <> '0'";
		strMainSql = strMainSql + " and pk_billtypecode=? and isnull(nw_billtemplet.dr,0)=0";
		strMainSql = strMainSql + " order by nw_billtemplet.pk_corp,nw_billtemplet.pk_billtypecode";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			strMainSql = "select nw_billtemplet.pk_billtemplet,nw_billtemplet.pk_billtypecode,"
					+ "nw_billtemplet.bill_templetname,nw_billtemplet.bill_templetcaption,nw_billtemplet.pk_corp,nw_corp.corp_name,"
					+ "nw_billtemplet.model_type,nw_billtemplet.options,nw_billtemplet.shareflag,nw_billtemplet.nodecode,nw_billtemplet.resid ";
			strMainSql += "from nw_billtemplet left join nw_corp on nw_billtemplet.pk_corp=nw_corp.pk_corp where nw_billtemplet.pk_billtemplet <> '0'";
			strMainSql += " and pk_billtypecode=? and isnull(nw_billtemplet.dr,0)=0";
			strMainSql += " order by nw_billtemplet.pk_corp,nw_billtemplet.pk_billtypecode";
		}
		return dao.queryForList(strMainSql, HashMap.class, pk_billtypecode);
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> loadTempletTab(String pk_billtemplet) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		String headerTable = null;
		List<String> bodyTableList = new ArrayList<String>();
		String sql = "select pos,tabcode from nw_billtemplet_t WITH(NOLOCK) where pk_billtemplet=?";
		List<HashMap> templetTabs = dao.queryForList(sql, HashMap.class, pk_billtemplet);
		String testSql = getTestSql();
		for(Map tabMap : templetTabs) {
			int pos = Integer.parseInt(tabMap.get(BillTempletTVO.POS).toString());
			if(pos == UiConstants.POS[0]) {
				// 表头
				String headerTabName = tabMap.get(BillTempletTVO.TABCODE).toString();
				// 检测该headerTabName是否是表名
				Integer obj = dao.queryForObject(testSql, Integer.class, headerTabName);
				if(obj != null) {
					headerTable = headerTabName;
				}
			} else if(pos == UiConstants.POS[1]) {
				// 表体,一般tabName就是表名
				// FIXME 这里还是校验一下
				String bodyTabName = tabMap.get(BillTempletTVO.TABCODE).toString();
				Integer obj = dao.queryForObject(testSql, Integer.class, bodyTabName);
				if(obj != null) {
					bodyTableList.add(bodyTabName);
				}
			}
		}
		retMap.put(HEADERTABLE, headerTable);
		retMap.put(BODYTABLE, bodyTableList);
		return retMap;
	}

	private String getTestSql() {
		if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
			return "select 1 from information_schema.tables where table_name=?";
		} else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())) {
			return "select 1 from sysobjects where xtype='U' and name=?";
		}else if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())) {
			return "SELECT 1 FROM USER_TABLES where lower(TABLE_NAME)=?";
		}
		return "select 1 from sysobjects where xtype='U' and name=?";
	}

	@SuppressWarnings("rawtypes")
	public List<HashMap> loadTableFields(String tableName) {
		if(StringUtils.isBlank(tableName)) {
			return new ArrayList<HashMap>();
		}
		String sql = "SELECT name,length FROM syscolumns WITH(NOLOCK) WHERE id=(SELECT id FROM sysobjects WHERE type='U' AND name=?)";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select COLUMN_NAME as name , DATA_LENGTH as length from user_tab_columns where lower(TABLE_NAME) = ?";
		}
		List<HashMap> fieldAry = dao.queryForList(sql, HashMap.class, tableName);
		return fieldAry;
	}

	public Map<String, Object> loadTempletDesc(String pk_billtemplet) {
		//UiBillTempletVO templetVO = TempletHelper.getOriginalBillTempletVO(pk_billtemplet);
		UiBillTempletVO templetVO = RedisDao.getInstance().getOriginalBillTempletVO(pk_billtemplet);
		return buildTempletDesc(templetVO);
	}

	private Map<String, Object> buildTempletDesc(UiBillTempletVO templetVO) {
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		List<BillTempletTVO> tabVOs = templetVO.getTabVOs();
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		Map<String, Object> tabMap = new HashMap<String, Object>();
		Map<String, Object> retMap = new HashMap<String, Object>();
		List<String> headerTabAry = new ArrayList<String>();
		List<String> bodyTabAry = new ArrayList<String>();
		for(int i = 0; i < tabVOs.size(); i++) {
			BillTempletTVO tabVO = tabVOs.get(i);
			List<BillTempletBVO> oneTabList = new ArrayList<BillTempletBVO>();
			for(int j = 0; j < fieldVOs.size(); j++) {
				BillTempletBVO fieldVO = fieldVOs.get(j);
				if(StringUtils.isNotBlank(fieldVO.getReftype())) {
					fieldVO.setReftype(NWUtils.escape(fieldVO.getReftype()));
				}
				if(StringUtils.isNotBlank(fieldVO.getOptions())) {
					fieldVO.setOptions(NWUtils.escape(fieldVO.getOptions()));
				}
				if(tabVO.getTabcode().equals(fieldVO.getTable_code())) {
					oneTabList.add(fieldVO);
				}
			}

			fieldMap.put(tabVO.getTabcode(), oneTabList);
			tabMap.put(tabVO.getTabcode(), tabVO);
			if(tabVO.getPos().intValue() == UiConstants.POS[0] || tabVO.getPos().intValue() == UiConstants.POS[2]) {
				headerTabAry.add(tabVO.getTabcode());
			} else {
				bodyTabAry.add(tabVO.getTabcode());
			}
		}
		retMap.put("B", fieldMap);
		retMap.put("T", tabMap);
		retMap.put("headerTabAry", headerTabAry);
		retMap.put("bodyTabAry", bodyTabAry);
		return retMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> loadTempletData(String pk_billtypecode) {
		List<HashMap> templetList = loadTemplet(pk_billtypecode);
		if(templetList != null && templetList.size() > 0) {
			Map<String, Object> retMap = new HashMap<String, Object>();
			List<Map<String, Object>> retList = convertTempletToList(templetList);
			retMap.put(TEMPLET, retList);

			// 收集tab信息
			Set<String> bodyTableSet = new HashSet<String>();// 存储表体的集合，不能重复
			for(int i = 0; i < templetList.size(); i++) {
				Map templetMap = templetList.get(i);
				String pk_billtemplet = templetMap.get(BillTempletVO.PK_BILLTEMPLET).toString();
				Map<String, Object> tabMap = loadTempletTab(pk_billtemplet);
				// 只能使用一个主表
				if(retMap.get(HEADERTABLE) == null) {
					Object headerTable = tabMap.get(HEADERTABLE);
					if(headerTable != null) {
						Map<String, Object> headerTableMap = new HashMap<String, Object>();
						headerTableMap.put(Constants.TEXT, headerTable);
						headerTableMap.put(Constants.VALUE, headerTable);
						retMap.put(HEADERTABLE, headerTableMap);
					}
				}
				Object oBodyTableList = tabMap.get(BODYTABLE);
				if(oBodyTableList != null) {
					List<String> bodyTableList = (List<String>) oBodyTableList;
					bodyTableSet.addAll(bodyTableList);
				}
			}
			List<Map<String, Object>> bodyTableMapList = new ArrayList<Map<String, Object>>(bodyTableSet.size());
			for(String bodyTable : bodyTableSet) {
				Map<String, Object> bodyTableMap = new HashMap<String, Object>();
				bodyTableMap.put(Constants.TEXT, bodyTable);
				bodyTableMap.put(Constants.VALUE, bodyTable);
				bodyTableMapList.add(bodyTableMap);
			}
			retMap.put(BODYTABLE, bodyTableMapList);

			retMap.put(BillTempletVO.PK_BILLTYPECODE, pk_billtypecode);
			return retMap;
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map<String, Object>> convertTempletToList(List<HashMap> templetList) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(templetList.size());
		for(int i = 0; i < templetList.size(); i++) {
			Map templetMap = templetList.get(i);
			templetMap.put(
					Constants.TEXT,
					templetMap.get(BillTempletVO.PK_BILLTYPECODE) + " "
							+ templetMap.get(BillTempletVO.BILL_TEMPLETCAPTION));
			templetMap.put(Constants.VALUE, templetMap.get(BillTempletVO.PK_BILLTEMPLET).toString());
			results.add(templetMap);
		}
		return results;
	}

	public void deleteBillTemplet(String pk_billtemplet) {
		if(StringUtils.isBlank(pk_billtemplet)) {
			return;
		}
		AggregatedValueObject billVO = ServiceHelper.queryBillVO(this.getBillInfo(), pk_billtemplet);
		SuperVO parentVO = (SuperVO) billVO.getParentVO();
		if(checkBeforeDelete(parentVO.getTableName(), parentVO.getPrimaryKey())) {
			throw new BusiException("单据模板已经被分配，不能删除！");
		}
		NWDao.getInstance().delete(billVO);
	}

	private void fixBillTempletVO(BillTempletVO parentVO) {
		if(parentVO.getStatus() == VOStatus.NEW) {
			// 新建
			parentVO.setBill_templetname("SYSTEM");
			parentVO.setPk_corp("@@@");
		}
	}

	private void fixBillTempletBVO(BillTempletBVO fieldVO) {
		if(StringUtils.isNotBlank(fieldVO.getReftype())) {
			fieldVO.setReftype(NWUtils.unescape(fieldVO.getReftype()));
		}
		if(StringUtils.isNotBlank(fieldVO.getOptions())) {
			fieldVO.setOptions(NWUtils.unescape(fieldVO.getOptions()));
		}
		if(fieldVO.getStatus() == VOStatus.NEW) {
			fieldVO.setPk_corp("@@@@");
		}
	}

	public Map<String, Object> saveBillTemplet(AggregatedValueObject billVO) {
		BillTempletVO parentVO = (BillTempletVO) billVO.getParentVO();
		// 检查模板数据是否是最新的
		if(StringUtils.isNotBlank(parentVO.getPk_billtemplet())) {
			BillTempletVO templetVO = dao.queryByCondition(BillTempletVO.class, new String[] { "ts" },
					"pk_billtemplet=?", parentVO.getPk_billtemplet());
			if(!parentVO.getTs().toString().equals(templetVO.getTs().toString())) {
				// ts不一致，模板已经被修改
				throw new BusiException("模板已经被修改，请重新加载！");
			}
		}
		fixBillTempletVO(parentVO);

		ExAggBillTempletVO aggVO = (ExAggBillTempletVO) billVO;
		BillTempletBVO[] fieldVOs = (BillTempletBVO[]) aggVO.getTableVO("nw_billtemplet_b");
		for(BillTempletBVO fieldVO : fieldVOs) {
			fixBillTempletBVO(fieldVO);
		}
		BillTempletTVO[] tabVOs = (BillTempletTVO[]) aggVO.getTableVO("nw_billtemplet_t");
		for(BillTempletTVO tabVO : tabVOs) {
			if(tabVO.getStatus() == VOStatus.DELETED) {
				// 如果是删除的页签，那么需要删除页签的对应字段
				String sql = "delete from nw_billtemplet_b where pk_billtemplet=? and table_code=?";
				dao.getJdbcTemplate().update(sql, parentVO.getPk_billtemplet(), tabVO.getTabcode());
			}
		}
		NWDao.setNCPrimaryKey(billVO);
		dao.saveOrUpdate(billVO, false);
		
		// 构建返回值
		UiBillTempletVO templetVO = new UiBillTempletVO();
		templetVO.setFieldVOs(Arrays.asList(fieldVOs));
		templetVO.setTabVOs(Arrays.asList(tabVOs));
		Map<String, Object> retMap = this.buildTempletDesc(templetVO);
		String[] attrs = parentVO.getAttributeNames();
		for(String key : attrs) {
			retMap.put(key, parentVO.getAttributeValue(key));
		}
		// 这里返回的B的信息只需要传过来的更改过的信息，不需要所有信息，到时候更新到页面上的时候也是只更新修改过的数据
		return retMap;
	}

	public BillTempletVO copyBillTemplet(String pk_billtemplet, String bill_templetcaption, String nodecode) {
		if(StringUtils.isBlank(pk_billtemplet) || StringUtils.isBlank(bill_templetcaption)
				|| StringUtils.isBlank(nodecode)) {
			return null;
		}
		//yaojiie  2015 11 20 修改主键生成逻辑，在生成主键之前，将原有的主键值设置为空，只有为空是
		ParamVO paramVO = new ParamVO();
		paramVO.setBillId(pk_billtemplet);
		ExAggBillTempletVO aggVO = (ExAggBillTempletVO) this.queryBillVO(paramVO);
		BillTempletVO parentVO = (BillTempletVO) aggVO.getParentVO();
		parentVO.setBill_templetcaption(bill_templetcaption);
		parentVO.setNodecode(nodecode);
		parentVO.setPk_billtemplet(null);
		parentVO.setStatus(VOStatus.NEW);
		NWDao.setNCPrimaryKey(parentVO);
		BillTempletBVO[] fieldVOs = (BillTempletBVO[]) aggVO.getTableVO("nw_billtemplet_b");
		for(BillTempletBVO fieldVO : fieldVOs) {
			fieldVO.setPk_billtemplet(parentVO.getPk_billtemplet());
			fieldVO.setPk_billtemplet_b(null);
			fieldVO.setStatus(VOStatus.NEW);
			NWDao.setNCPrimaryKey(fieldVO);
		}
		BillTempletTVO[] tabVOs = (BillTempletTVO[]) aggVO.getTableVO("nw_billtemplet_t");
		for(BillTempletTVO tabVO : tabVOs) {
			tabVO.setPk_billtemplet(parentVO.getPk_billtemplet());
			tabVO.setPk_billtemplet_t(null);
			tabVO.setStatus(VOStatus.NEW);
			NWDao.setNCPrimaryKey(tabVO);
		}
		NWDao.getInstance().saveOrUpdate(aggVO);
		return parentVO;
	}

	public void buildQueryTemplet(String pk_billtemplet, String model_name, String node_code, boolean bCover) {
		if(StringUtils.isBlank(pk_billtemplet)) {
			throw new RuntimeException("单据模板PK不能为空！");
		}
		// 将node_code作为model_code进行查询，检查当前是否已经存在查询模板
		QueryTempletVO parentVO = dao.queryByCondition(QueryTempletVO.class, "model_code=?", node_code);
		if(parentVO != null) {
			if(!bCover) {
				// 不覆盖
				throw new RuntimeException("当前已经存在查询模板，如果想覆盖，请勾选[是否覆盖]复选框！");
			} else {
				// 删除原有的
				QueryConditionVO[] condVOs = dao.queryForSuperVOArrayByCondition(QueryConditionVO.class,
						"pk_templet=?", parentVO.getPk_templet());
				dao.delete(condVOs);
				dao.delete(parentVO);
			}
		}
		AggQueryTempletVO aggVO = new AggQueryTempletVO();
		parentVO = new QueryTempletVO();
		parentVO.setModel_code(node_code);
		parentVO.setModel_name(model_name);
		parentVO.setNode_code(node_code);
		parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		parentVO.setStatus(VOStatus.NEW);
		NWDao.setNCPrimaryKey(parentVO);
		aggVO.setParentVO(parentVO);

		List<BillTempletBVO> fieldVOs = TempletHelper.getFieldVOsByTplId(pk_billtemplet);
		List<QueryConditionVO> condVOs = new ArrayList<QueryConditionVO>();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() != UiConstants.POS[0]) {// 只生成表头的字段
				continue;
			}
			String itemkey = fieldVO.getItemkey();
			if(itemkey.equals("dr") || itemkey.equals("ts")) {
				continue;
			}
			QueryConditionVO condVO = TempletHelper.convert(fieldVO);
			condVO.setPk_templet(parentVO.getPk_templet());
			condVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			condVO.setStatus(VOStatus.NEW);
			NWDao.setNCPrimaryKey(condVO);
			condVOs.add(condVO);
		}
		aggVO.setChildrenVO(condVOs.toArray(new QueryConditionVO[condVOs.size()]));
		dao.saveOrUpdate(aggVO);
	}

	public void buildReportTemplet(String pk_billtemplet, String vtemplatename, String nodecode, boolean bCover) {
		if(StringUtils.isBlank(pk_billtemplet)) {
			throw new RuntimeException("单据模板PK不能为空！");
		}
		// 将nodecode作为vtemplatecode进行查询，检查当前是否已经存在查询模板
		ReportTempletVO parentVO = dao.queryByCondition(ReportTempletVO.class, "vtemplatecode=?", nodecode);
		if(parentVO != null) {
			if(!bCover) {
				// 不覆盖
				throw new RuntimeException("当前已经存在报表模板，如果想覆盖，请勾选[是否覆盖]复选框！");
			} else {
				// 删除原有的
				ReportTempletBVO[] rtBVOs = dao.queryForSuperVOArrayByCondition(ReportTempletBVO.class, "pk_templet=?",
						parentVO.getPk_report_templet());
				dao.delete(rtBVOs);
				dao.delete(parentVO);
			}
		}
		AggReportTempletVO aggVO = new AggReportTempletVO();
		parentVO = new ReportTempletVO();
		parentVO.setVtemplatecode(nodecode);
		parentVO.setVtemplatename(vtemplatename);
		parentVO.setNodecode(nodecode);
		parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		parentVO.setStatus(VOStatus.NEW);
		NWDao.setNCPrimaryKey(parentVO);
		aggVO.setParentVO(parentVO);

		List<BillTempletBVO> fieldVOs = TempletHelper.getFieldVOsByTplId(pk_billtemplet);
		List<ReportTempletBVO> rtbVOs = new ArrayList<ReportTempletBVO>();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() != UiConstants.POS[0]) {// 只生成表头的字段
				continue;
			}
			String itemkey = fieldVO.getItemkey();
			if(itemkey.equals("dr") || itemkey.equals("ts")) {
				continue;
			}
			// 只将那些列表显示的字段生成到报表模板中
			if(fieldVO.getListshowflag() == null || !fieldVO.getListshowflag().booleanValue()) {
				continue;
			}
			ReportTempletBVO rtbVO = TempletHelper.convertRtbVO(fieldVO);
			rtbVO.setPk_report_templet(parentVO.getPk_report_templet());
			rtbVO.setStatus(VOStatus.NEW);
			NWDao.setNCPrimaryKey(rtbVO);
			rtbVOs.add(rtbVO);
		}
		aggVO.setChildrenVO(rtbVOs.toArray(new ReportTempletBVO[rtbVOs.size()]));
		dao.saveOrUpdate(aggVO);
	}

}

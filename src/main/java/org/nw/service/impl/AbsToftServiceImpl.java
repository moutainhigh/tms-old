package org.nw.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.ExceptionReference;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.basic.util.ReflectionUtils;
import org.nw.basic.util.SecurityUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.dao.RefinfoDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.ExcelImporter;
import org.nw.exp.POI;
import org.nw.exp.PrintTempletUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.ulw.IRenderer;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.BillTempletVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiPrintTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.print.ImageResource;
import org.nw.redis.RedisDao;
import org.nw.service.IReferenceCheck;
import org.nw.service.IToftService;
import org.nw.service.ServiceHelper;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CodenoHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.DataFormatHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.QueryHelper;
import org.nw.utils.RefUtils;
import org.nw.utils.TempletHelper;
import org.nw.utils.VariableHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.DataDictBVO;
import org.nw.vo.sys.DataTempletVO;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.vo.sys.PortletVO;
import org.nw.vo.sys.RefInfoVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service实现类,实现基本的增删改查操作,可以被覆盖
 * 
 * @author xuqc
 * @date 2012-6-10 下午04:31:08
 */
public abstract class AbsToftServiceImpl implements IToftService {

	protected Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 由于每个service都会实例化，实例化这个dao，子类复用，子类实际上使用的是子类实例化的dao
	 */
	protected NWDao dao;

	/**
	 * 通过Spring容器注入datasource 实例化TmsDao
	 * 
	 * @param dataSource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dao = new NWDao(dataSource);
	}

	@SuppressWarnings("unchecked")
	public String getPKByCode(String code) {
		AggregatedValueObject billInfo = this.getBillInfo();
		try {
			Class<? extends SuperVO> parentVOClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			SuperVO parentVO = parentVOClass.newInstance();
			String pkField = billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
			parentVO = NWDao.getInstance().queryByCondition(parentVOClass, new String[] { pkField },
					this.getCodeFieldCode() + "=?", code);
			return parentVO.getPrimaryKey();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T getByPrimaryKey(Class<? extends SuperVO> clazz, String primaryKey) {
		if(primaryKey == null) {
			return null;
		}
		try {
			SuperVO superVO = clazz.newInstance();
			// 这里不要直接使用dao，在有些地方使用，可能根本还没有实例化，如filter中
			return (T) NWDao.getInstance().queryByCondition(clazz, superVO.getPKFieldName() + "=?", primaryKey);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] getByPrimaryKeys(Class<? extends SuperVO> clazz, String[] primaryKeys) {
		if(primaryKeys == null || primaryKeys.length == 0) {
			return null;
		}
		try {
			SuperVO superVO = clazz.newInstance();
			// 这里不要直接使用dao，在有些地方使用，可能根本还没有实例化，如filter中
			return (T[]) NWDao.getInstance().queryForSuperVOArrayByCondition(clazz, superVO.getPKFieldName() + " in " + NWUtils.buildConditionString(primaryKeys));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] getByPrimaryKeys(Class<? extends SuperVO> clazz, List<String> primaryKeys) {
		if(primaryKeys == null || primaryKeys.size() == 0) {
			return null;
		}
		try {
			SuperVO superVO = clazz.newInstance();
			// 这里不要直接使用dao，在有些地方使用，可能根本还没有实例化，如filter中
			return (T[]) NWDao.getInstance().queryForSuperVOArrayByCondition(clazz, superVO.getPKFieldName() + " in " + NWUtils.buildConditionString(primaryKeys));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据billId查询单据ＶＯ，对于展示时，可能返回的ＶＯ会经过处理，但是如果是删除等操作，那么一般需要原始的VO
	 */
	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		return ServiceHelper.queryBillVO(this.getBillInfo(), paramVO);
	}

	@Transactional
	public int saveOrUpdate(List<SuperVO> superVOs) throws Exception {
		for(SuperVO superVO : superVOs) {
			saveOrUpdate(superVO);
		}
		return superVOs.size();
	}

	@Transactional
	public int saveOrUpdate(SuperVO superVO) {
		if(superVO.getStatus() == VOStatus.NEW) {
			logger.info("新增一个SuperVO," + superVO.getPrimaryKey());
			return this.addSuperVO(superVO);
		} else if(superVO.getStatus() == VOStatus.UPDATED) {
			logger.info("更新一个SuperVO," + superVO.getPrimaryKey());
			return this.updateByPrimaryKey(superVO);
		} else if(superVO.getStatus() == VOStatus.DELETED) {
			logger.info("删除一个SuperVO," + superVO.getPrimaryKey());
			return this.deleteByPrimaryKey(superVO.getClass(), superVO.getPrimaryKey());
		}
		return 0;
	}

	@Transactional
	public int addSuperVO(SuperVO superVO) {
		logger.info("新增一个SuperVO," + superVO.getPrimaryKey());
		if(superVO.getAttributeValue(superVO.getPKFieldName()) == null) {
			// FIXME 如果没有设置主键,则自动设置,一般情况下是自己需要设置
			if(useUUIDPrimaryKey()) {
				NWDao.setUuidPrimaryKey(superVO);
			} else {
				NWDao.setNCPrimaryKey(superVO);
			}
		}
		superVO.setStatus(VOStatus.NEW);
		dao.saveOrUpdate(superVO);
		return 1;
	}

	/**
	 * 删除前校验，系统默认定义了一些主外键的关联，如果待删除的字段值已经被引用，那么不能继续删除
	 * 
	 * @param tableName
	 * @param key
	 * @return
	 * @author xuqc
	 * @date 2012-8-14
	 * 
	 */
	protected boolean checkBeforeDelete(String tableName, String key) {
		logger.info("检查档案是否被引用，表名称：" + tableName + "，主键：" + key);
		IReferenceCheck iIReferenceCheck = new ReferenceCheckImpl();
		return iIReferenceCheck.isReferenced(tableName, key);
	}

	/**
	 * 是否使用逻辑删除，一般来讲，如果存在大文本类型的字段，为例节约空间，也为了查询的效率，使用物理删除
	 * 
	 * @return
	 */
	protected boolean isLogicalDelete() {
		return true;
	}

	@Transactional
	public int deleteByPrimaryKey(Class<? extends SuperVO> clazz, String primaryKey) {
		logger.info("根据主键删除一条记录，主键：" + primaryKey);
		AggregatedValueObject billVO = ServiceHelper.queryBillVO(this.getBillInfo(), primaryKey);
		SuperVO parentVO = (SuperVO) billVO.getParentVO();
		if(checkBeforeDelete(parentVO.getTableName(), parentVO.getPrimaryKey())) {
			throw new BusiException("表[?]的ID为[?]的记录已经被引用，不能删除！",parentVO.getTableName(),primaryKey);
		}
		processBeforeDelete(billVO);
		dao.delete(billVO, isLogicalDelete());
		processAfterDelete(billVO);
		return 1;
	}

	/**
	 * 删除前动作
	 * 
	 * @param billVO
	 */
	protected void processBeforeDelete(AggregatedValueObject billVO) {
		// 如果存在附件，那么将附件也删除 //不需要了。
//		SuperVO superVO = (SuperVO) billVO.getParentVO();
//		String billtype = ServletContextHolder.getRequest().getParameter("billType");
//		if(StringUtils.isBlank(billtype)) {
//			billtype = ServletContextHolder.getRequest().getParameter("funCode");
//		}
//		String pk_bill = superVO.getPrimaryKey();
//		if(StringUtils.isNotBlank(billtype) && StringUtils.isNotBlank(pk_bill)) {
//			// FIXME 每个删除动作都需要执行到该语句，可以对这个表的这2个字段做索引
//			logger.info("删除附件表的记录，billType：" + billtype + "，主键：" + pk_bill);
//			String sql = "delete from nw_filesystem where billtype='" + billtype + "' and pk_bill='" + pk_bill + "'";
//			NWDao.getInstance().getJdbcTemplate().execute(sql);
//		}
	}
	
	protected void processAfterDelete(AggregatedValueObject billVO){
		
	}

	@Transactional
	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys) {
		for(String primaryKey : primaryKeys) {
			deleteByPrimaryKey(clazz, primaryKey);
		}
		return primaryKeys.length;
	}

	@Transactional
	public int deleteSuperVO(SuperVO superVO) {
		logger.info("删除一个SuperVO," + superVO.getPrimaryKey());
		superVO.setStatus(VOStatus.DELETED);
		if(checkBeforeDelete(superVO.getTableName(), superVO.getPrimaryKey())) {
			throw new BusiException("该条记录已经被引用，不能删除！");
		}
		dao.delete(superVO);
		return 0;
	}

	@Transactional
	public int updateByPrimaryKey(SuperVO superVO) {
		logger.info("更新一个SuperVO," + superVO.getPrimaryKey());
		superVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(superVO);
		return 1;
	}

	/**
	 * 表体页签增加操作列 图标为一个"+ -"号,功能为复制一行与删除一行
	 * 
	 * @param uiBillTempletVO
	 */
	protected void buildOperatorColumn(UiBillTempletVO uiBillTempletVO) {
		HttpServletRequest request = ServletContextHolder.getRequest();
		if(!"2".equals(request.getParameter("bbar"))) {
			// 非参照制单并且bbtn参数为1
			List<BillTempletTVO> tabVOs = uiBillTempletVO.getTabVOs();
			for(BillTempletTVO btVO : tabVOs) {
				if(btVO.getPos().intValue() == 1) {
					BillTempletBVO operateColumn = new BillTempletBVO();
					operateColumn.setItemkey("_operator");
					operateColumn.setDatatype(UiConstants.DATATYPE.TEXT.intValue());
					operateColumn.setListflag(Constants.NO);
					operateColumn.setCardflag(Constants.YES);
					operateColumn.setListshowflag(UFBoolean.FALSE);
					operateColumn.setShowflag(Constants.YES);
					operateColumn.setEditflag(Constants.NO);
					operateColumn.setLockflag(Constants.NO);
					operateColumn.setTotalflag(Constants.NO);
					operateColumn.setNullflag(Constants.NO);
					operateColumn.setReviseflag(UFBoolean.FALSE);
					operateColumn.setWidth(45);
					operateColumn.setDr(0);
					operateColumn.setPos(UiConstants.POS[1]);
					operateColumn.setTable_code(btVO.getTabcode());
					operateColumn.setTable_name(btVO.getTabname());
					operateColumn.setPk_billtemplet(btVO.getPk_billtemplet());
					operateColumn.setPk_billtemplet_b(UUID.randomUUID().toString());
					uiBillTempletVO.getFieldVOs().add(0, operateColumn);
				}
			}
		}
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		//UiBillTempletVO templetVO = TempletHelper.getOriginalBillTempletVO(templateID);
		UiBillTempletVO templetVO = RedisDao.getInstance().getOriginalBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			String defaultValue = fieldVO.getDefaultvalue();
			if(StringUtils.isNotBlank(defaultValue)) {
				defaultValue = VariableHelper.resolve(fieldVO.getDefaultvalue());
				fieldVO.setDefaultvalue(defaultValue);
			}
		}
		templetVO.setHeaderListItemKey(getHeaderListItemKey());
		buildOperatorColumn(templetVO);
		return templetVO;
	}

	public UiQueryTempletVO getQueryTempletVO(String templateID) {
		UiQueryTempletVO templetVO = TempletHelper.getOriginalQueryTempletVO(templateID);
		if(templetVO != null) {
			List<QueryConditionVO> condVOs = templetVO.getConditions();
			for(int i = 0; i < condVOs.size(); i++) {
				QueryConditionVO condVO = condVOs.get(i);
				if(condVO.getOpera_code().indexOf(UiConstants.BETWEEN_OPERA_CODE) == 0) {
					// 存在介于这个条件,并且是默认条件,那么不需要其他条件了。
					condVO.setOpera_code(UiConstants.BETWEEN_OPERA_CODE);
					condVO.setOpera_name(UiConstants.BETWEEN_OPERA_NAME);
				} else {
					condVO.setOpera_code(condVO.getOpera_code().replaceAll(UiConstants.BETWEEN_OPERA_CODE, ""));
					condVO.setOpera_name(condVO.getOpera_name().replaceAll(UiConstants.BETWEEN_OPERA_NAME, ""));
				}
			}
		}
		return templetVO;
	}

	/**
	 * 返回单据模板ID,当前可以直接返回一个ID值，以后可以做一个简单的分配页面 FIXME
	 * 
	 * @return
	 */
	public String getBillTemplateID(ParamVO paramVO) {
		if(paramVO == null) {
			return null;
		}
				return RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
				UiConstants.TPL_STYLE.BILL.intValue());
//		return TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//				UiConstants.TPL_STYLE.BILL.intValue());
	}

	/**
	 * 返回查询模板ID，当前可以直接返回一个ID值，以后可以做一个简单的分配页面 FIXME
	 * 
	 * @return
	 */
	public String getQueryTemplateID(ParamVO paramVO) {
		if(paramVO == null) {
			return null;
		}
		return RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
				UiConstants.TPL_STYLE.QUERY.intValue());
//		return TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//				UiConstants.TPL_STYLE.QUERY.intValue());
	}

	public String getPrintTempletID(ParamVO paramVO) {
		if(paramVO == null) {
			return null;
		}
			return RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
				UiConstants.TPL_STYLE.PRINT.intValue());
//		return TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//			UiConstants.TPL_STYLE.PRINT.intValue());
	}

	public UiPrintTempletVO getPrintTempletVO(String templateID) {
		return TempletHelper.getOriginalPrintTempletVO(templateID);
	}

	public String getReportTempletID(ParamVO paramVO) {
		if(paramVO == null) {
			return null;
		}
		return RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
				UiConstants.TPL_STYLE.REPORT.intValue());
//	return TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//				UiConstants.TPL_STYLE.REPORT.intValue());
	}
	
	public String getDataTempletID(ParamVO paramVO) {
		if(paramVO == null) {
			return null;
		}
		return RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
				UiConstants.TPL_STYLE.DATA.intValue());
//	return TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//				UiConstants.TPL_STYLE.REPORT.intValue());
	}

	public UiReportTempletVO getReportTempletVO(String templateID) {
		return TempletHelper.getOriginalReportTempletVO(templateID);
	}
	
	public DataTempletVO getDataTempletVO(String templateID) {
		if(StringUtils.isBlank(templateID)){
			return null;
		}
		//redis获取数据
		return RedisDao.getInstance().getDataTempletVO(templateID);
		//return TempletHelper.getDataTempletVO(templateID);
	}

	/**
	 * 返回表头的fieldCode，主要用于定义表头表格的字段的显示顺序，目前模板设计器还不支持表头表格和表头表单使用不同的顺序
	 * 已解决：这个方法的作用，可以使用单据模板的设置来解决，没必要硬编码
	 * 
	 * @return
	 * @deprecated
	 */
	public String[] getHeaderListItemKey() {
		return null;
	}

	/**
	 * 返回code字段的字段名，目前用于设置编码的默认值，编码的默认值一般使用规则生成，对于单据类型，就是单据号 XXX
	 * 这里不要返回默认值，否则经常会因为没有设置到真正的code字段而出错，并且难以查错
	 * 
	 * @return
	 */
	public String getCodeFieldCode() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public SuperVO getByCodeWithNoDr(String code) {
		AggregatedValueObject billInfo = this.getBillInfo();
		CircularlyAccessibleValueObject parentVO = billInfo.getParentVO();
		if(parentVO == null) {
			// 没有表头，当作单表体处理
			CircularlyAccessibleValueObject[] cvos = billInfo.getChildrenVO();
			if(cvos != null && cvos.length > 0) {
				parentVO = cvos[0];
			}
		}
		Class<? extends SuperVO> parentVOClass = null;
		try {
			parentVOClass = (Class<? extends SuperVO>) Class.forName(parentVO.getAttributeValue(VOTableVO.HEADITEMVO)
					.toString().trim());
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(parentVOClass == null) {
			throw new BusiException("无法实例化parentVO，请检查billInfo配置！");
		}
		String codeFieldCode = this.getCodeFieldCode();
		if(StringUtils.isBlank(codeFieldCode)) {
			throw new BusiException("没有继承getCodeFieldCode方法返回code字段名，无法根据code查询VO！");
		}
		return NWDao.getInstance().queryByWhereClause(parentVOClass, this.getCodeFieldCode() + "=?", code);
	}

	@SuppressWarnings("unchecked")
	public SuperVO getByCode(String code) {
		AggregatedValueObject billInfo = this.getBillInfo();
		CircularlyAccessibleValueObject parentVO = billInfo.getParentVO();
		if(parentVO == null) {
			// 没有表头，当作单表体处理
			CircularlyAccessibleValueObject[] cvos = billInfo.getChildrenVO();
			if(cvos != null && cvos.length > 0) {
				parentVO = cvos[0];
			}
		}
		Class<? extends SuperVO> parentVOClass = null;
		try {
			parentVOClass = (Class<? extends SuperVO>) Class.forName(parentVO.getAttributeValue(VOTableVO.HEADITEMVO)
					.toString().trim());
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(parentVOClass == null) {
			throw new BusiException("无法实例化parentVO，请检查billInfo配置！");
		}
		String codeFieldCode = this.getCodeFieldCode();
		if(StringUtils.isBlank(codeFieldCode)) {
			throw new BusiException("没有继承getCodeFieldCode方法返回code字段名，无法根据code查询VO！");
		}
		return NWDao.getInstance().queryByCondition(parentVOClass, this.getCodeFieldCode() + "=?", code);
	}

	/**
	 * 新增按钮的默认值(先从模板中读取默认值) <br>
	 * 可以被继承，处理自己的默认值 对于参照，只需设置其pk值，其显示值将通过执行公式取得
	 * 将该方法独立出来，是因为设置了默认值后，可能还需要执行显示公式 该方法只针对表头和表尾，对于表体有另外的设置方式
	 * 
	 * @return
	 */
	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = new HashMap<String, Object>();
		if(paramVO != null) {
			// 对于参照制单和单据生成另外一张单据,paramVO为null,此时不会从模板中读取默认值
			// 模板中设置的默认值是在自制单据时使用
			// 从模板表中读取默认值(注：这里只读取表头表尾的默认值--fangw)
			String templateID = paramVO.getTemplateID();
			if(templateID == null) {
				// 对于某些特殊处理情况，此时可能没有传入templateID，比如在页面打开的时候就读取默认值
				templateID = this.getBillTemplateID(paramVO);
			}
			UiBillTempletVO templetVO = this.getBillTempletVO(templateID);
			values = templetVO.getHeaderDefaultValueMap();
			for(String key : values.keySet()) {
				Object value = values.get(key);
				if(value != null && StringUtils.isNotBlank(value.toString())) {
					value = VariableHelper.resolve(value.toString());// 识别默认值，可能是公式
					values.put(key, value);
				}
			}
		}

		// 设置特殊默认值
		// nc的日期对象一般是UFDate类型，使用原生类型，在序列号的时候会进行
		// values.put("makedate", UFDate.getDate(new Date()));// 制单日期
		values.put("create_user", WebUtils.getLoginInfo().getPk_user()); // 制单人
		values.put("create_time", new UFDateTime(new Date()));// 创建时间
		values.put("pk_corp", WebUtils.getLoginInfo().getPk_corp()); // 公司
		// 部门
		if(WebUtils.getLoginInfo().getPk_dept() != null) {
			values.put("pk_dept", WebUtils.getLoginInfo().getPk_dept());
		}
		return values;
	}

	/**
	 * 返回表体的默认值，注意这里要加上页签编码<br/>
	 * <p>
	 * 格式如：{tabname1:{},tabname2:{}}
	 * </p>
	 * 
	 * @param paramVO
	 *            包含读取模板的参数
	 * @param headerMap
	 *            表头的默认值，表体的默认值通常需要引用表头的默认值
	 * @return
	 * @author xuqc
	 * @date 2012-2-9
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> values = new HashMap<String, Object>();
		if(paramVO != null) {
			// 对于参照制单和单据生成另外一张单据,paramVO为null,此时不会从模板中读取默认值
			// 模板中设置的默认值是在自制单据时使用
			// 从模板表中读取默认值(注：这里只读取表体的默认值--fangw)
			UiBillTempletVO templetVO = this.getBillTempletVO(paramVO.getTemplateID());
			// 这里的values已经根据tabname区分了
			values = templetVO.getBodyDefaultValueMap();
			for(String key : values.keySet()) {
				Object oOneTabValuesMap = values.get(key);// 得到其中一个的tab的默认值
				if(oOneTabValuesMap != null) {
					Map<String, Object> oneTabValuesMap = (Map<String, Object>) oOneTabValuesMap;
					for(String key1 : oneTabValuesMap.keySet()) {
						Object value = oneTabValuesMap.get(key1);
						if(value != null && StringUtils.isNotBlank(value.toString())) {
							value = VariableHelper.resolve(value.toString());// 识别默认值，可能是公式
							oneTabValuesMap.put(key1, value);
						}
					}
				}
			}
		}
		return values;
	}

	/**
	 * 返回一张单据的默认值，包括表头和表体，返回统一的数据格式
	 * 
	 * @param paramVO
	 * @return
	 * @author xuqc
	 * @date 2012-2-14
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getDefaultValue(ParamVO paramVO) {
		/**
		 * 表头数据处理
		 */
		Map<String, Map<String, Object>> retMap = new HashMap<String, Map<String, Object>>();
		Map<String, Object> headerDefaultValuesMap = getHeaderDefaultValues(paramVO);
		// 执行公式
		List<Map<String, Object>> model = new ArrayList<Map<String, Object>>();
		model.add(headerDefaultValuesMap);

		UiBillTempletVO templetVO = this.getBillTempletVO(paramVO.getTemplateID());
		List<BillTempletBVO> billTempletBVOList = templetVO.getFieldVOs();
		Map<String, Object> headerMap = FormulaHelper.execLoadFormula4Templet(model, billTempletBVOList, false, null,
				null).get(0);

		/**
		 * 表体数据处理
		 */
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		Map<String, Object> bodyDefaultValuesMap = getBodyDefaultValues(paramVO, headerMap);
		for(String tabcode : bodyDefaultValuesMap.keySet()) {
			Map<String, Object> tabDefaultValuesMap = new HashMap<String, Object>();
			Object oTabDefaultValuesMap = bodyDefaultValuesMap.get(tabcode);
			if(oTabDefaultValuesMap != null) {
				tabDefaultValuesMap = (Map<String, Object>) oTabDefaultValuesMap;
			}
			List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
			context.add(tabDefaultValuesMap);
			List<Map<String, Object>> bodyTabMapList = FormulaHelper.execLoadFormula4Templet(context,
					billTempletBVOList, true, new String[] { tabcode }, null);
			bodyMap.put(tabcode, bodyTabMapList);
		}
		retMap.put(Constants.HEADER, headerMap);
		retMap.put(Constants.BODY, bodyMap);

		/**
		 * 对参照做特殊处理
		 */
		Map<String, BillTempletBVO> headerRefMap = templetVO.getHeaderRefMap();
		convertRefOject(headerMap, headerRefMap);
		retMap.put(Constants.HEADER, headerMap);
		return (Map) retMap;
	}

	/**
	 * 主键的生成是否使用uuid，如果返回false，则使用一个20位的字符串，这是为了兼容旧系统，旧系统的pk长度有些设计是20。
	 * 现在一般设计成50位长度
	 * 
	 * @return
	 */
	public boolean useUUIDPrimaryKey() {
		return true;
	}

	/**
	 * 保存 <br>
	 * 
	 * @param templateID
	 * @param tabCode
	 * @param json
	 * @return
	 */
	@Transactional
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		logger.info("保存一个档案或单据，可能是新增、修改、删除。");
		processBeforeSave(billVO, paramVO);
		/**
		 * 3.进行保存处理
		 */
		// 检查子表是否包含新增的记录
		boolean newChildrenVO = false;
		CircularlyAccessibleValueObject[] cvos = null;
		if(billVO instanceof IExAggVO) {
			// 多子表
			cvos = ((IExAggVO) billVO).getAllChildrenVO();
		} else {
			// 单子表
			cvos = billVO.getChildrenVO();
		}
		if(cvos != null && cvos.length > 0) {
			for(CircularlyAccessibleValueObject cvo : cvos) {
				if(cvo.getStatus() == VOStatus.NEW) {
					newChildrenVO = true;
					break;
				}
			}
		}

		if((billVO.getParentVO() != null && billVO.getParentVO().getStatus() == VOStatus.NEW) || newChildrenVO) {
			if(useUUIDPrimaryKey()) {
				NWDao.setUuidPrimaryKey(billVO);
			} else {
				NWDao.setNCPrimaryKey(billVO);
			}
		}

		// 使用参数来标识是否逻辑删除,默认是逻辑删除
		NWDao.getInstance().saveOrUpdate(billVO, isLogicalDelete());
		processAfterSave(billVO, paramVO);
		// 2015-05-27 对于没有登录的情况下调用save方法，通常是导入的情况，这种情况不需要执行公式
		if(WebUtils.getLoginInfo() == null) {
			return null;
		}
		// 对于保存，不需要返回表体的数据，就算返回了，但是难以判断是应该增加到表体呢，还是更新到表体，直接重新刷新表体就可以了
		return billVO;
	}

	/**
	 * excel 导入保存 <br>
	 * yaojiie2015-11-30
	 * @param templateID
	 * @param tabCode
	 */
	@Transactional
	public void saveforimport(AggregatedValueObject billVO, ParamVO paramVO) {
		logger.info("保存一个档案或单据，可能是新增、修改、删除。");
		processBeforeSave(billVO, paramVO);
		/**
		 * 3.进行保存处理
		 */
		// 检查子表是否包含新增的记录
		boolean newChildrenVO = false;
		CircularlyAccessibleValueObject[] cvos = null;
		if(billVO instanceof IExAggVO) {
			// 多子表
			cvos = ((IExAggVO) billVO).getAllChildrenVO();
		} else {
			// 单子表
			cvos = billVO.getChildrenVO();
		}
		if(cvos != null && cvos.length > 0) {
			for(CircularlyAccessibleValueObject cvo : cvos) {
				if(cvo.getStatus() == VOStatus.NEW) {
					newChildrenVO = true;
					break;
				}
			}
		}

		if((billVO.getParentVO() != null && billVO.getParentVO().getStatus() == VOStatus.NEW) || newChildrenVO) {
			if(useUUIDPrimaryKey()) {
				NWDao.setUuidPrimaryKey(billVO);
			} else {
				NWDao.setNCPrimaryKey(billVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(billVO, isLogicalDelete());
	}
	
	
	/**
	 * 处理表头的编码，如果是档案类型的那么就是编码，如果是单据类型，则就是设置单据号
	 * 
	 * @param parentVO
	 * @param paramVO
	 */
	public void setCodeField(CircularlyAccessibleValueObject parentVO, ParamVO paramVO) {
		if(StringUtils.isNotBlank(getCodeFieldCode())) {
			// 子类继承了该方法，说明希望使用编码规则
			Object codeObj = parentVO.getAttributeValue(getCodeFieldCode());
			if(codeObj == null || StringUtils.isBlank(codeObj.toString())) {
				FunVO funVO = getFunVOByFunCode(paramVO.getFunCode());
				if(funVO.getIf_code_rule().booleanValue()) {// 功能注册中标记了使用编码规则生成编码
					// 如果没有录入编码，则按照规则生成一个
					String code = CodenoHelper.generateCode(paramVO.getFunCode());
					if(StringUtils.isBlank(code)) {
						throw new RuntimeException("可能没有定义编码规则，无法生成编码，请录入！");
					}
					SuperVO superVO = this.getByCodeWithNoDr(code);
					while(superVO != null) {
						// 该订单号已经存在
						code = BillnoHelper.generateBillno(paramVO.getFunCode());
						superVO = this.getByCodeWithNoDr(code);
					}
					parentVO.setAttributeValue(getCodeFieldCode(), code);
				}
			}
			codeObj = parentVO.getAttributeValue(getCodeFieldCode());
			if(codeObj == null || StringUtils.isBlank(codeObj.toString())) {
				throw new RuntimeException("没有定义使用编码规则，又没有录入编码！");
			}
		}
	}

	/**
	 * 保存前段操作,操作create_user,create_time,modify_user,modify_time等字段
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO == null) {
			throw new RuntimeException("没有需要保存的数据！");
		}
		
		UiBillTempletVO templetVO = this.getBillTempletVOByFunCode(paramVO);
		if(templetVO == null){
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		if(parentVO != null) {
			//设置通用字段 创建人 创建时间 修改人修改时间
			setCommonField(parentVO);
			setCodeField(parentVO, paramVO);
			for(String attr : parentVO.getAttributeNames()){
				if(parentVO.getAttributeValue(attr) != null &&
								(parentVO.getAttributeValue(attr).toString().indexOf(",") > 0
									||parentVO.getAttributeValue(attr).toString().indexOf("|")> 0
							  )){
					for(BillTempletBVO fieldVO : fieldVOs){
						if(fieldVO.getPos().intValue() == UiConstants.POS[0]
								&&fieldVO.getItemkey().equals(attr)
								&& fieldVO.getDatatype().equals(DATATYPE.SELECT.intValue())
								){
							
							Object value = UiTempletUtils.getSelectValue(parentVO.getAttributeValue(attr).toString().toString(), fieldVO.getReftype());
							parentVO.setAttributeValue(attr, value);
						}
					}
				}
			}
		}
		if(billVO instanceof IExAggVO) {
			// 多表体
			IExAggVO exAggVO = ((IExAggVO) billVO);
			String[] tableCodes = exAggVO.getTableCodes();
			for(String tableCode : tableCodes) {
				CircularlyAccessibleValueObject[] cvos = exAggVO.getTableVO(tableCode);
				if(cvos != null) {
					for(CircularlyAccessibleValueObject cvo : cvos) {
						setCommonField(cvo);
						for(String attr : cvo.getAttributeNames()){
							if(cvo.getAttributeValue(attr) != null &&
											(cvo.getAttributeValue(attr).toString().indexOf(",") > 0
												||cvo.getAttributeValue(attr).toString().indexOf("|")> 0
										  )){
								for(BillTempletBVO fieldVO : fieldVOs){
									if(fieldVO.getTable_code().equals(tableCode)
											&&fieldVO.getItemkey().equals(attr)
											&& fieldVO.getDatatype().equals(DATATYPE.SELECT.intValue())
											){
										
										Object value = UiTempletUtils.getSelectValue(cvo.getAttributeValue(attr).toString().toString(), fieldVO.getReftype());
										cvo.setAttributeValue(attr, value);
									}
								}
							}
						}
					}
				}
			}
		} else {
			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null && cvos.length > 0) {
				if(parentVO == null) {
					// 单表体
					setCodeField(cvos[0], paramVO);
				}
				for(CircularlyAccessibleValueObject cvo : cvos) {
					setCommonField(cvo);
					for(String attr : cvo.getAttributeNames()){
						if(cvo.getAttributeValue(attr) != null &&
										(cvo.getAttributeValue(attr).toString().indexOf(",") > 0
											||cvo.getAttributeValue(attr).toString().indexOf("|")> 0
									  )){
							for(BillTempletBVO fieldVO : fieldVOs){
								if(fieldVO.getPos().intValue() == UiConstants.POS[1]
										&&fieldVO.getItemkey().equals(attr)
										&& fieldVO.getDatatype().equals(DATATYPE.SELECT.intValue())
										){
									
									Object value = UiTempletUtils.getSelectValue(cvo.getAttributeValue(attr).toString().toString(), fieldVO.getReftype());
									cvo.setAttributeValue(attr, value);
								}
							}
						}
					}
				}
			}
		}
	}

	
	
	/**
	 * 保存后事件
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {

	}

	/**
	 * 设置通用的字段
	 * 
	 * @param cvo
	 */
	public void setCommonField(CircularlyAccessibleValueObject cvo) {
		if(cvo == null) {
			return;
		}
		try {
			Field field = null;
			if(VOStatus.NEW == cvo.getStatus()) {
				// 新增
				if(WebUtils.getLoginInfo() != null) {
					field = ReflectionUtils.findField(cvo.getClass(), "create_user");
					if(field != null) {
						ReflectionUtils.makeAccessible(field);
						field.set(cvo, WebUtils.getLoginInfo().getPk_user());
					}
				}
				field = ReflectionUtils.findField(cvo.getClass(), "create_time");
				if(field != null) {
					ReflectionUtils.makeAccessible(field);
					field.set(cvo, new UFDateTime(new Date()));
				}
			} else if(VOStatus.UPDATED == cvo.getStatus()) {
				if(WebUtils.getLoginInfo() != null) {
					field = ReflectionUtils.findField(cvo.getClass(), "modify_user");
					if(field != null) {
						ReflectionUtils.makeAccessible(field);
						field.set(cvo, WebUtils.getLoginInfo().getPk_user());
					}
				}

				field = ReflectionUtils.findField(cvo.getClass(), "modify_time");
				if(field != null) {
					ReflectionUtils.makeAccessible(field);
					field.set(cvo, new UFDateTime(new Date()));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对resultMap中的参照对象做特殊处理
	 * 
	 * @param resultMap
	 *            待处理的Map
	 * @param refMap
	 *            表头和表尾参照对象的Map
	 * @author xuqc
	 * @date 2012-2-8
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void convertRefOject(Map<String, Object> resultMap, Map<String, BillTempletBVO> refMap) {
		// 返回的值需要对参照值多特殊处理
		for(String key : resultMap.keySet()) {
			BillTempletBVO fieldVO = refMap.get(key);
			if(fieldVO != null) {
				// 这个是参照对象,返回已经构建的参照对象
				Object obj = fieldVO.genRefItem(false);// 这里返回的可能是自定义参照对象
				if(obj instanceof BaseRefModel) {
					BaseRefModel refModel = (BaseRefModel) obj;
					// 调用该参照对象的getByPk方法，返回VO值，并转化成参照VO，因为需要3个必须值，id,code,name
					Object pkValue = resultMap.get(key);
					if(pkValue != null) {
						// 可能本身就为null值
						Map<String, Object> voMap = refModel.getByPk(pkValue.toString());
						if(voMap != null && voMap.get("data") != null) {
							// 可能返回的值也是为null，如参照压根就没有实现
							RefVO refVO = null;
							// 有些getByPk是返回SuperVO，也可能返回Map
							if(voMap.get("data") instanceof SuperVO) {
								SuperVO superVO = (SuperVO) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
								refVO = RefUtils.convert(refModel, superVO);
							} else if(voMap.get("data") instanceof Map) {
								Map map = (Map) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
								// 若参照为管理档案，显示值为基本档案的name和code，则在参照中不再关联管理档案表取管理档案的pk值，直接在这边取值赋给参照的pkfieldcode
								// wpp 2012-04-11
								map.put(refModel.getPkFieldCode(), pkValue);
								refVO = RefUtils.convert(refModel, map);
							}
							// 将返回的值重新设置到该resultMap中
							resultMap.put(key, refVO);
						} else if(voMap != null && voMap.get("datas") != null) {
							// 根据多个pk去查询的情况
							List<RefVO> resultList = new ArrayList<RefVO>();
							Object[] array = (Object[]) voMap.get("datas");
							for(int i = 0; i < array.length; i++) {
								RefVO refVO = null;
								// 有些getByPk是返回SuperVO，也可能返回Map
								if(array[i] instanceof SuperVO) {
									SuperVO superVO = (SuperVO) array[i];// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
									refVO = RefUtils.convert(refModel, superVO);
								} else {
									Map map = (Map) array[i];// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
									// 若参照为管理档案，显示值为基本档案的name和code，则在参照中不再关联管理档案表取管理档案的pk值，直接在这边取值赋给参照的pkfieldcode
									// wpp 2012-04-11
									map.put(refModel.getPkFieldCode(), pkValue);
									refVO = RefUtils.convert(refModel, map);
								}
								resultList.add(refVO);
							}
							// 将返回的值重新设置到该resultMap中
							resultMap.put(key, resultList);
						} else {
							if(refModel.isFillinable() != null && refModel.isFillinable()) {
								// 可录入的参照，返回原值
								RefVO refVO = new RefVO();
								refVO.setPk(pkValue.toString());
								refVO.setName(pkValue.toString());
								refVO.setCode(pkValue.toString());
								resultMap.put(key, refVO);
							} else {
								// 查询不到记录，可能该档案已经被删除，这个数据也失去意义了
								resultMap.put(key, null);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 显示单据<br>
	 * <p>
	 * 这里返回的是整张单据的数据，包括表体的所有数据，如果表体需要使用分页，请继承该方法，只返回表头的数据，表体的数据使用发送loadData请求
	 * </p>
	 * 
	 * @param billId
	 * @return
	 */
	public AggregatedValueObject show(ParamVO paramVO) {
		logger.info("查询一张单据，billId：" + paramVO.getBillId());
		processBeforeShow(paramVO);
		AggregatedValueObject billVO = queryBillVO(paramVO);
		return billVO;
	}
	
	public void processBeforeShow(ParamVO paramVO){
		
	}
	
	public void processAfterExecFormula(Map<String, Object> result){
		
	}
	
	
	protected void processInExecFormula(ParamVO paramVO, String bodyTabCode, List<Map<String, Object>> bodyList) {
		
		UiBillTempletVO templetVO = this.getBillTempletVOByFunCode(paramVO);
		if(templetVO == null){
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		
		for(Map<String, Object> map : bodyList){
			for(BillTempletBVO fieldVO : fieldVOs){
				if(fieldVO.getTable_code().equals(bodyTabCode)
						&& fieldVO.getDatatype().equals(DATATYPE.SELECT.intValue())
						&&(map.get(fieldVO.getItemkey()) != null &&
							(map.get(fieldVO.getItemkey()).toString().indexOf(",") > 0
								||map.get(fieldVO.getItemkey()).toString().indexOf("|")> 0
						  ))){
					Object value = UiTempletUtils.getSelectText(map.get(fieldVO.getItemkey()).toString(), fieldVO.getReftype());
					map.put(fieldVO.getItemkey(), String.valueOf(value));
				}
			}
		}
	}
	
	public String buildDefaultCond(UiQueryTempletVO queryTempletVO){
		String defaultCond = QueryHelper.getDefaultCond(queryTempletVO);
		return defaultCond;
	}

	public String buildImmobilityCond(UiQueryTempletVO queryTempletVO){
		String defaultCond = QueryHelper.getImmobilityCond(queryTempletVO);
		return defaultCond;
	}

	/**
	 * 将通用的查询参数转换为sql的where条件格式，多个条件的关系是and<br>
	 * 通用查询页面复用了NC的查询模板<br>
	 * 对于档案类型，一般情况下子公司可以查询集团的档案数据，但是不做统一设置，如果需要在子类继承该方法设置
	 * 
	 * @param params
	 *            查询参数
	 * @param clazz
	 *            单据vo类
	 * @return
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return parseCondition(params, paramVO, templetVO);
	}

	/**
	 * 是否使用默认的公司查询条件，如果不使用，建议在模板中配置
	 * 
	 * @param paramVO
	 * @return
	 */
	public boolean useDefaultCorpCondition(ParamVO paramVO) {
		return true;
	}

	/**
	 * 公司条件，每个子类可以定制自己的公司条件，比如在公司之间分享数据，或者可以不使用这里的公司条件，而直接通过查询模板来配置
	 * 
	 * @param paramVO
	 * @param templetVO
	 * @return
	 */
	public String getCorpCondition(String tablePrefix, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return null;
	}

	/**
	 * 解析从前台发过来的查询条件
	 * 
	 * @return
	 */
	public String parseCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return QueryHelper.parseCondition(params, paramVO, templetVO);
	}
	

	/**
	 * 处理orderBy，子类可覆盖实现自己特殊的排序处理<br/>
	 * 如果返回一个不包括"order by"字符的字符串，那么此时返回的是某个不在clazz中的字段，在处理查询后需要根据该字段进行排序
	 * 
	 * @param billType
	 * @param params
	 * @param clazz
	 * @param orderBy
	 * @return
	 */
	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		if(orderBy == null || orderBy.trim().length() == 0) {
			return "";
		} else {
			if(clazz == null) {
				return orderBy;
			}
			orderBy = orderBy.toLowerCase().trim();
			// 读取order by的具体字段
			String fieldName = orderBy.substring(orderBy.indexOf("order by") + 8).trim();
			String dir = Constants.ORDER_ASC;
			if(fieldName.indexOf(Constants.ORDER_DESC) != -1) {
				dir = Constants.ORDER_DESC;
			}
			int index = fieldName.indexOf(",");
			if(index > -1) {
				// 说明有多个字段进行排序，取第一个字段
				fieldName = fieldName.substring(0, index);
			} else {
				index = fieldName.indexOf(" ");
				if(index > -1) {
					// 说明后面跟着asc desc
					fieldName = fieldName.substring(0, index);
				}
			}
			index = fieldName.indexOf(".");
			if(index != -1) {
				// 有前缀的字段名称
				fieldName = fieldName.substring(index + 1);
			}
			Field field = null;
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch(Exception e) {
			}
			if(field == null) {
				// 该排序字段不是类的字段
				return fieldName + "," + dir;// 返回字段的名称和排序方向
			} else {
				return " " + orderBy;
			}
		}
	}

	/**
	 * 返回主表的pk，一般主表的pk字段和在子表pk字段是相同的，但是有些因为历史原因，导致这2个不相同，该方法供子类继承
	 * 
	 * @return 不再需要了，之前是因为webnc的历史原因
	 */
	protected String getParentPkFieldInChild(ParamVO paramVO) {
		return this.getBillInfo().getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
	}

	/**
	 * 是否兼容模式，true则使用旧的模式进行查询，否则使用合并子表进行查询
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-7-20
	 */
	public boolean isCompatibleMode() {// 如果使用主子表合并方式，可能出现重复字段，而加上distinct的话效率又大打折扣
		return true;
	}

	/**
	 * 返回loadData查询的字段
	 * 
	 * @return
	 */
	public String[] getLoadDataFields() {
		return null;
	}

	/**
	 * 如果从portlet中进入的页面，那么读取配置中的查询条件，假如查询中
	 * 
	 * @return
	 */
	public String buildPortletLoadDataCondition(ParamVO paramVO, Class<? extends SuperVO> clazz) {
		String pk_portlet = ServletContextHolder.getRequest().getParameter("pk_portlet");
		if(StringUtils.isNotBlank(pk_portlet)) {
			// 从portlet中进入的页面，那么读取portlet中定义的查询条件，假如查询中，子类可以自定义
			PortletVO vo = NWDao.getInstance().queryByCondition(PortletVO.class, "pk_portlet=?", pk_portlet);
			return VariableHelper.resolve(vo.getQuery_where(), null);
		}
		return null;
	}

	/**
	 * 加载数据<br>
	 * 注：<br>
	 * 1、该方法一般只提供给之类继承，用户一般不覆盖 2、主表数据加载和子表数据加载，全部使用该方法<br>
	 * 3、为了支持带树的单据和非单据，增加了两个参数String extendCond,Object... values，对于不需要的可以传null<br>
	 * 
	 * @param isRefbill
	 *            是否是参照制单页面
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @param pks
	 * @param orderBy
	 * @param extendCond
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PaginationVO loadData(ParamVO paramVO, int offset, int pageSize, String[] pks, String orderBy,
			String extendCond, Object... values) {
		logger.info("单据列表页加载数据...");
		
		String params = ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
		try {
			//导出时，编码会错误，所以需要重新编码
			if(StringUtils.isNotBlank(params)){
				params = URLDecoder.decode(params, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.info("编码解析成功");
			e.printStackTrace();
		}
		
		/**
		 * 1.首先获取对应的superVO
		 */
		AggregatedValueObject billInfo = this.getBillInfo();
		Class<? extends SuperVO> voClass = ServiceHelper.getVOClass(billInfo, paramVO);
		UiQueryTempletVO queryTempletVO = this.getQueryTempletVO(this.getQueryTemplateID(paramVO));
		/**
		 * 2.组装查询条件-sql where
		 */
		StringBuilder where = new StringBuilder();
		if(paramVO.isBody()) {
			/**
			 * 2.1.组装前台查询条件
			 */
			String condition = null;
			condition = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
			if(StringUtils.isNotBlank(condition)) {
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(condition);
			}

			// FIXME XUQC 2012-03-29 这里如果是单表体的情况，不需要加入这个条件
			if(billInfo.getChildrenVO() != null && billInfo.getChildrenVO().length > 0) {
				if(billInfo.getParentVO() != null) {
					CircularlyAccessibleValueObject[] voArr = billInfo.getChildrenVO();
					CircularlyAccessibleValueObject childVOTable = null;
					if(voArr.length == 1) {
						// 有一个子表
						childVOTable = voArr[0];
					} else {
						// 多子表
						VOTableVO[] voTables = (VOTableVO[]) voArr;
						for(VOTableVO voTable : voTables) {
							if(paramVO.getTabCode().equals(voTable.getVotable())) {
								childVOTable = voTable;
								break;
							}
						}
					}
					// 如果是子表，则增加主表pk条件
					String pkFieldName = billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
					String parentPkInChild = childVOTable.getAttributeValue(VOTableVO.PKFIELD).toString().trim();
					String pkValue = ServletContextHolder.getRequest().getParameter(pkFieldName);
					if(StringUtils.isNotBlank(pkValue)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(parentPkInChild).append("='");
						where.append(pkValue);
						where.append("'");
					}
				} else {
					// 如果是单表体模式,那么可能需要加入默认条件和锁定条件
					if(StringUtils.isBlank(params)) {
						// 没有经过查询窗口的查询
						String defaultCond = this.buildDefaultCond(queryTempletVO);
						if(StringUtils.isNotBlank(defaultCond)) {
							if(where.length() > 0) {
								where.append(" and ");
							}
							where.append(defaultCond);
						}
					}
					// 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
					String immobilityCond = this.buildImmobilityCond(queryTempletVO);
					if(StringUtils.isNotBlank(immobilityCond)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(immobilityCond);
					}
				}
			} else {
				// 单表头模式
			}
		} else {
			String billId = ServletContextHolder.getRequest().getParameter("billId");
			if(StringUtils.isNotBlank(billId)) {
				// 如果是主表，但传入了billId（通过待办事务打开的场景）
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(getParentPkFieldInChild(paramVO)).append("='");
				where.append(billId);
				where.append("'");// 这里不再组装前台查询条件
			}else{
				String billIds = ServletContextHolder.getRequest().getParameter("billIds");
				if(StringUtils.isNotBlank(billIds)){
					String[] billIdsArr = billIds.split(",");
					String pkCond = NWUtils.buildConditionString(billIdsArr);
					where.append(getParentPkFieldInChild(paramVO)).append(" in ");
					where.append(pkCond);
				}else {
					// 组装前台查询条件
					String dataWhere = null;
					dataWhere = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
					if(StringUtils.isNotBlank(dataWhere)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(dataWhere);
					}

					/**
					 * 查询模板的相关信息
					 */
					// 这个条件都只是表头的，表体不需要查询模板的条件
					if(StringUtils.isBlank(params)) {
						// 没有经过查询窗口的查询
						String defaultCond = this.buildDefaultCond(queryTempletVO);
						if(StringUtils.isNotBlank(defaultCond)) {
							if(where.length() > 0) {
								where.append(" and ");
							}
							where.append(defaultCond);
						}
						// portlet中定义的查询条件
						String portlet_query_where = buildPortletLoadDataCondition(paramVO, voClass);
						if(StringUtils.isNotBlank(portlet_query_where)) {
							if(where.length() > 0) {
								where.append(" and ");
							}
							where.append(portlet_query_where);
						}
					}
					// 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
					String immobilityCond = this.buildImmobilityCond(queryTempletVO);
					if(StringUtils.isNotBlank(immobilityCond)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(immobilityCond);
					}
				}
			}
			
			/**
			 * 2.2.添加数据权限,只有主表需要
			 */
			DataTempletVO dataTempletVO =getDataTempletVO(getDataTempletID(paramVO));
			if(dataTempletVO != null){
				StringBuilder leftBracket = new StringBuilder("(");
				where = leftBracket.append(where);
				StringBuilder rightBracket = new StringBuilder(")");
				where.append(rightBracket);
				where.append(dataTempletVO.getSql_type()).append("(").append(dataTempletVO.getSql()).append(")");
			}
		}

		/**
		 * 处理扩展参数 如左边树的参数
		 */
		if(StringUtils.isNotBlank(extendCond)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(extendCond);
		}
		
		

		/**
		 * 2.3.处理排序
		 */

		orderBy = this.buildLoadDataOrderBy(paramVO, voClass, orderBy);
		if(orderBy != null && orderBy.length() > 0) {
			orderBy = orderBy.toLowerCase();
			if(orderBy.indexOf(Constants.ORDER_BY) != -1) {
				if(this.isCompatibleMode() || paramVO.isBody()) {
					where.append(" ");
					where.append(orderBy);
				}
			}
		}

		/**
		 * 3.进行查询
		 */
		PaginationVO paginationVO = getPaginationVO(billInfo, paramVO, voClass, offset, pageSize, where.toString(),
				orderBy, values);
		// 在执行公式之前做一些操作
		processBeforeExecFormula(paginationVO.getItems(), paramVO);

		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		// 统计行的数据
		// if(!paramVO.isBody()) {// 如果是表体，目前没有支持合计
		paginationVO.setSummaryRowMap(buildSummaryRowMap(uiBillTempletVO, voClass, where.toString()));
		// }
		// 表头时，有表头表尾等多个tableCode；表体时，只会有当前表体的tableCode
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		/**
		 * 4.转换数据格式，准备执行公式
		 */
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<?> itemList = paginationVO.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(itemList.size());
		for(Object item : itemList) {
			Map<String, Object> map = new HashMap<String, Object>();
			if(item instanceof SuperVO) {
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
			} else {
				// map结构
				map = (Map<String, Object>) item;
				for(String key : map.keySet()) {
					Object obj = map.get(key);
					if(obj != null) {
						obj = SecurityUtils.escape(obj.toString());
					}
				}
			}
			mapList.add(map);
		}

		/**
		 * 6.执行公式
		 */
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(),
				tableCodes, null);
		processAfterExecFormula(list, paramVO, orderBy);
		paginationVO.setItems(list);
		return paginationVO;
	}

	/**
	 * 返回执行查询的翻页数据
	 * 
	 * @param paramVO
	 * @param voClass
	 * @param where
	 * @param orderBy
	 * @return
	 */
	public PaginationVO getPaginationVO(AggregatedValueObject billInfo, ParamVO paramVO,
			Class<? extends SuperVO> voClass, int offset, int pageSize, String where, String orderBy, Object... values) {
		String sql;
		if(this.isCompatibleMode()) {
			sql = DaoHelper.buildSelectSql(voClass, DaoHelper.getWhereClause(voClass, where), getLoadDataFields(),
					false);
		} else {
			if(paramVO.isBody()) {
				// 表体的查询不需要left join 表体的处理
				sql = DaoHelper.buildSelectSql(voClass, DaoHelper.getWhereClause(voClass, where), getLoadDataFields(),
						true);
			} else {
				// 使用合并子表条件的模式进行查询
				sql = ServiceHelper.buildSelectSql(billInfo, paramVO, DaoHelper.getWhereClause(voClass, where),
						getLoadDataFields());
				if(orderBy != null && orderBy.length() > 0 && orderBy.indexOf(Constants.ORDER_BY) != -1) {
					sql += orderBy;
				}
			}
		}
		NWDao dao = NWDao.getInstance();
		PaginationVO paginationVO = dao.queryBySqlWithPaging(sql, voClass, offset, pageSize, values);
		return paginationVO;
	}

	/**
	 * 返回查询统计行的数据，XXX 只包括vo类中包含的字段，如果需要返回一些通过公式返回的字段，覆盖该方法,自己写个sql去查询
	 * 
	 * @param templetVO
	 * @param clazz
	 * @param where
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> buildSummaryRowMap(UiBillTempletVO templetVO, Class<? extends SuperVO> clazz,
			String where) {
		List<String[]> fieldAry = templetVO.getHeaderSummaryFieldAry();
		if(fieldAry == null || fieldAry.size() == 0) {
			return null;
		}
		where = where.toLowerCase();
		int index = where.indexOf("order by");
		if(index != -1) {
			where = where.substring(0, index);
		}

		String countSql = DaoHelper.buildSumSelectSql(clazz, where, fieldAry);
		if(StringUtils.isBlank(countSql)) {
			return null;
		}

		return NWDao.getInstance().queryForObject(countSql, HashMap.class);
	}

	/**
	 * loadData中,执行公式后的操作,如对list进行排序,这个排序字段是通过公式取得的<br/>
	 * FIXME 这里的排序只是将查询的分页数据进行重新排序，是基于内存的排序，也不是对所有数据进行排序，有局限性
	 * 
	 * @param list
	 * @param paramVO
	 */
	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, final String orderBy) {
		if(StringUtils.isNotBlank(orderBy) && orderBy.indexOf(Constants.ORDER_BY) == -1) {
			final String[] orderAry = orderBy.split(",");// 第一个是排序字段，第二个是排序方向
			// 根据orderKey进行排序
			Collections.sort(list, new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> p1, Map<String, Object> p2) {
					Object v1 = p1.get(orderAry[0]);
					Object v2 = p2.get(orderAry[0]);
					if(v1 == null) {
						return 0;
					}
					if(v2 == null) {
						return 1;
					}
					if(orderAry[1].equals(Constants.ORDER_ASC)) {
						return v2.toString().compareTo(v1.toString());
					} else {
						return v1.toString().compareTo(v2.toString());
					}
				}
			});
		}
		
		UiBillTempletVO templetVO = this.getBillTempletVOByFunCode(paramVO);
		if(templetVO == null){
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs){
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]
						&& fieldVO.getDatatype().equals(DATATYPE.SELECT.intValue())){
				for(Map<String, Object> map : list){
					if(map.get(fieldVO.getItemkey()) != null &&
							(map.get(fieldVO.getItemkey()).toString().indexOf(",") > 0
								||map.get(fieldVO.getItemkey()).toString().indexOf("|")> 0
						  )){
						Object value = UiTempletUtils.getSelectText(map.get(fieldVO.getItemkey()).toString(), fieldVO.getReftype());
						map.put(fieldVO.getItemkey(), String.valueOf(value));
					}
				}
			}
		}
	}

	/**
	 * loadData中,执行公式前的操作，如数据统计
	 * 
	 * @param superVOList
	 * @param paramVO
	 */
	protected void processBeforeExecFormula(List<SuperVO> superVOList, ParamVO paramVO) {

	}

	public AggregatedValueObject copy(ParamVO paramVO) {
		logger.info("执行拷贝单据动作...");
		// 获取单据VO
		AggregatedValueObject billVO = queryBillVO(paramVO);
		beforeProcessCopyVO(billVO, paramVO);// 拷贝前的一些动作
		// 设置为新增状态，必须将主键置为null，还有其他一些信息，如单据日期，制单人等等
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		// 通常拷贝后的公司必须是当前登陆公司
		Field field_corp = ReflectionUtils.getDeclaredField(parentVO, "pk_corp");
		if(field_corp != null) {
			parentVO.setAttributeValue("pk_corp", WebUtils.getLoginInfo().getPk_corp());
		}
		clearNoCopyField(parentVO);
		CircularlyAccessibleValueObject[] vos;
		if(billVO instanceof IExAggVO) {
			vos = ((IExAggVO) billVO).getAllChildrenVO();
		} else {
			vos = billVO.getChildrenVO();
		}
		if(vos != null) {
			for(CircularlyAccessibleValueObject vo : vos) {
				clearNoCopyField(vo);
				vo.setAttributeValue(((SuperVO) parentVO).getPKFieldName(), null); // 表头的pk设置为null;
				field_corp = ReflectionUtils.getDeclaredField(vo, "pk_corp");
				if(field_corp != null) {
					vo.setAttributeValue("pk_corp", WebUtils.getLoginInfo().getPk_corp());
				}
			}
		}
		processCopyVO(billVO, paramVO);
		return billVO;
	}

	/**
	 * 复制时不复制的数据
	 * 
	 * @param vo
	 */
	private void clearNoCopyField(CircularlyAccessibleValueObject vo) {
		try {
			vo.setPrimaryKey(null);
			vo.setAttributeValue("create_time", null);// 创建时间
			vo.setAttributeValue("create_user", null);// 创建人
			vo.setAttributeValue("modify_time", null);// 最后修改时间
			vo.setAttributeValue("modify_user", null);// 最后修改人
			vo.setAttributeValue("code", null);// 编码
			vo.setAttributeValue("name", null);// 名称
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 拷贝前的一些操作
	 * 
	 * @param copyVO
	 * @author xuqc
	 * @date 2012-4-18
	 */
	protected void beforeProcessCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {

	}

	/**
	 * 对复制的vo进行操作
	 * 
	 * @param copyVO
	 * @author xuqc
	 * @date 2012-4-18
	 */
	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
	}

	/**
	 * 读取TableCode，在执行公式中使用<br>
	 * 若是表体，则tabCode肯定只有一个，若是表头，则tabCode可能大于等于1(不是>=1，是要么1，要么2),多个tabCode是使用,分隔
	 * 
	 * @param tabCode
	 * @param isBody
	 * @return
	 */
	public String[] getTableCodes(String tabCode, boolean isBody) {
		// 获取tableCode(NW_BILLTEMPLET_T中也叫tabCode)，为了下一步执行公式
		if(isBody) {
			return new String[] { tabCode };
		} else {
			String[] arr = tabCode.split(",");
			if(arr.length == 2) {
				if(arr[0].equals(arr[1])) {
					return new String[] { arr[0] };
				}
			}
			return tabCode.split(",");
		}
	}

	/**
	 * 聚合VO执行公式，返回格式如：<br/>
	 * {HEADER:{},BODY:{TABNAME1:[],TABNAME2:[]}}
	 * 
	 * @param billVO
	 * @param paramVO
	 * @return
	 * @author xuqc
	 * @date 2011-9-22
	 */
	public Map<String, Object> execFormula4Templet(AggregatedValueObject billVO, ParamVO paramVO) {
		return this.execFormula4Templet(billVO, paramVO, true, true);
	}

	/**
	 * 聚合VO执行公式，返回格式如：<br/>
	 * {HEADER:{},BODY:{TABNAME1:[],TABNAME2:[]}}
	 * 
	 * @param billVO
	 *            单据VO
	 * @param paramVO
	 *            参数
	 * @param execHead
	 *            是否执行表头
	 * @param execBody
	 *            是否执行表体
	 * @return
	 * @author xuqc
	 * @date 2011-9-22
	 */
	public Map<String, Object> execFormula4Templet(AggregatedValueObject billVO, ParamVO paramVO, boolean execHead,
			boolean execBody) {
		if(billVO == null) {
			return null;
		}

		UiBillTempletVO templetVO = this.getBillTempletVO(paramVO.getTemplateID());
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(execHead && billVO.getParentVO() != null) {
			/**
			 * 1、处理表头
			 */
			String[] tableCodes = paramVO.getHeaderTabCode().split(",");// 表头可能有多个页签
			// 此处不过滤单据模板中没有定义的字段，前台需要根据返回值生成列表页数据和卡片页数据。
			// 也可以过滤，但是需要返回列表页和卡片页定义字段的集合
			Map<String, Object> parentMap = this.execFormula4Templet(billVO.getParentVO(), templetVO, false,
					tableCodes, null);
			List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
			context.add(parentMap);
			processAfterExecFormula(context, paramVO, null);
			parentMap=context.get(0);

			// 处理表头的Map
			// 转换参照对象
			this.convertRefOject(parentMap, templetVO.getHeaderRefMap());
			retMap.put(Constants.HEADER, parentMap);
		}

		String tabCodes = templetVO.getBodyTabCode();
		if(execBody && StringUtils.isNotBlank(tabCodes)) {
			/**
			 * 2、处理表体
			 */
			Map<String, Object> bodyMap = new HashMap<String, Object>();
			if(billVO instanceof IExAggVO) {
				// 多表体
				IExAggVO exAggVO = (IExAggVO) billVO;
				String[] bodyTabCodes = tabCodes.split(",");
				for(String bodyTabCode : bodyTabCodes) {
					CircularlyAccessibleValueObject[] childrenVOs = exAggVO.getTableVO(bodyTabCode);
					if(childrenVOs != null) {
						ArrayList<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(childrenVOs.length);
						for(CircularlyAccessibleValueObject vo : childrenVOs) {
							Map<String, Object> map = new HashMap<String, Object>();
							for(String key : vo.getAttributeNames()) {
								map.put(key, vo.getAttributeValue(key));
							}
							mapList.add(map);
						}
						List<Map<String, Object>> bodyList = this.execFormula4Templet(mapList, templetVO, true,
								new String[] { bodyTabCode }, null);
						bodyMap.put(bodyTabCode, bodyList);
						processInExecFormula(paramVO, bodyTabCode, bodyList);
					}
				}
			} else {
				// 单表体
				CircularlyAccessibleValueObject[] childrenVOs = billVO.getChildrenVO();
				if(childrenVOs != null) {
					ArrayList<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(childrenVOs.length);
					for(CircularlyAccessibleValueObject vo : childrenVOs) {
						Map<String, Object> map = new HashMap<String, Object>();
						for(String key : vo.getAttributeNames()) {
							map.put(key, vo.getAttributeValue(key));
						}
						mapList.add(map);
					}
					List<Map<String, Object>> bodyList = this.execFormula4Templet(mapList, templetVO, true,
							new String[] { paramVO.getBodyTabCode() }, null);
					bodyMap.put(paramVO.getBodyTabCode(), bodyList);
					processInExecFormula(paramVO, paramVO.getBodyTabCode(), bodyList);
				}
			}
			retMap.put(Constants.BODY, bodyMap);
		}
		return retMap;
	}

	/**
	 * 给单据使用的公式执行（自动合并model到公式结果）<br>
	 * 注：该方法专门为单据模板相关模块调用
	 * 
	 * @param vo
	 * @param templetId
	 * @param tabCodes
	 *            若是表头，此时tabCodes使用,分隔，若是表体，肯定只传一个，多表体是分开加载数据的
	 * @return
	 */
	public Map<String, Object> execFormula4Templet(CircularlyAccessibleValueObject vo, UiBillTempletVO uiBillTempletVO,
			boolean isBody, String[] tabCodes, String[] extFormulas) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(String key : vo.getAttributeNames()) {
			map.put(key, vo.getAttributeValue(key));
		}

		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		context.add(map);
		return  execFormula4Templet(context, uiBillTempletVO, isBody, tabCodes, extFormulas).get(0);
	}

	/**
	 * 给单据使用的公式执行（自动合并model到公式结果）<br>
	 * 注：该方法专门为单据模板相关模块调用
	 * 
	 * @param context
	 * @param templetId
	 * @param tabCodes
	 *            若是表头，此时tabCodes使用,分隔，若是表体，肯定只传一个，多表体是分开加载数据的(表头表尾可直接传null)
	 * @param extFormulas
	 * @return
	 */
	public List<Map<String, Object>> execFormula4Templet(List<Map<String, Object>> context,
			UiBillTempletVO uiBillTempletVO, boolean isBody, String[] tabCodes, String[] extFormulas) {
		if(context == null || context.size() == 0) {
			return context;
		}

		/**
		 * 获取单据模板信息
		 */
		List<BillTempletBVO> billTempletBVOList = uiBillTempletVO.getFieldVOs();

		/**
		 * 执行公式
		 */
		List<Map<String, Object>> list = FormulaHelper.execLoadFormula4Templet(context, billTempletBVOList, isBody,
				tabCodes, extFormulas);

		return list;
	}

	/**
	 * 执行公式
	 * 
	 * @param paramVO
	 * @param superVOs
	 * @return
	 */
	public List<Map<String, Object>> execFormula4Templet(ParamVO paramVO, List<SuperVO> superVOs) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOs.size());
		for(SuperVO vo : superVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	/**
	 * 执行模板中某个字段的公式
	 * 
	 * @param pkBilltemplet
	 * @param pkBilltempletB
	 * @param context
	 *            执行公式所需要的上下文环境
	 * @return
	 * @author xuqc
	 * @date 2012-2-10
	 */
	public Map<String, Object> execEditFormula(BillTempletBVO billTempletBVO, Map<String, Object> context) {
		// 本字段的编辑公式
		String formulas = billTempletBVO.getEditformula();
		if(formulas == null) {
			// 没有找到编辑公式，返回空(正常情况下，参照必须有编辑公式)
			logger.warn("没有找到编辑公式，pkBilltempletB=" + billTempletBVO.getPk_billtemplet_b());
			// 这里如果没有找到编辑公式，则返回空的hashmap
			return new HashMap<String, Object>();
		}

		// 3.调用执行公式
		List<Map<String, Object>> model = new ArrayList<Map<String, Object>>(1);
		model.add(context);
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			// 该方法主要是参照域在使用，当出现执行期异常的时候，返回空对象就行。
			result = FormulaHelper.execFormula(model, formulas.split(";")).get(0);
		} catch(Exception e) {
			logger.error("单据执行公式异常，该异常将捕获并不再抛出，系统会返回空对象。", e);
			e.printStackTrace();
		}
		return result;
	}

	public FunVO getFunVOByFunCode(String fun_code) {
		String strWhere = "isnull(dr,0)=0 and fun_code=?";
		return NWDao.getInstance().queryByCondition(FunVO.class, strWhere, fun_code);
	}

	/**
	 * 返回导出的excel的第一个工作簿名称，一般也只会导出一个工作簿
	 * 
	 * @return
	 */
	public String getFirstSheetName() {
		return null;
	}

	/**
	 * 返回导出时过滤的字段，只能是模板显示字段的一个子集
	 * 
	 * @return
	 */
	public String[] getExportFilterAry(ParamVO paramVO) {
		return null;
	}

	/**
	 * 返回导出Excel时的字段，，只能是模板显示字段的一个子集。如果指定了字段，那么直接使用这些字段，
	 * 而忽略getExportFilterAry定义的过滤的字段
	 * 
	 * @return
	 */
	public String[] getExportAry(ParamVO paramVO) {
		return null;
	}

	/**
	 * 返回导出的字段的title，需要和getExportAry一一对应
	 * 
	 * @param paramVO
	 * @return
	 */
	public String[] getExportTitleAry(ParamVO paramVO) {
		return null;
	}

	/**
	 * 导出时是否加入行号列
	 * 
	 * @param paramVO
	 * @return
	 */
	public boolean addNumberRow(ParamVO paramVO) {
		return true;
	}

	/**
	 * 在查询完成后的处理，比如要对数据进行修改，填充之类
	 * 
	 * @param paramVO
	 * @param dataList
	 */
	@SuppressWarnings("rawtypes")
	protected void processBeforeExport(ParamVO paramVO, List dataList) {

	}

	public HSSFWorkbook export(ParamVO paramVO, int offset, int pageSize, String orderBy, String extendCond,
			Object... values) {
		logger.info("执行导出动作...");
		Calendar start = Calendar.getInstance();
		PaginationVO paginationVO = this.loadData(paramVO, offset, pageSize, null, orderBy, extendCond, values);
		processBeforeExport(paramVO, paginationVO.getItems());
		// 这里会返回模板中只显示的列的数据
		List<List<Object>> dataList = processULWData(paginationVO.getItems(), paramVO);
		// 获取单据VO
		UiBillTempletVO templetVO = this.getBillTempletVO(getBillTemplateID(paramVO));
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		// 过虑掉模板中隐藏的列，与数据对应
		fieldVOs = filterULWFields(fieldVOs, null, paramVO);

		// 灵活的自定义字段，这些字段都必须是模板中定义的显示的字段，如果是隐藏的字段，那么请先在filterULWFields方法中先加入，通过两种方式定义
		// 1、定义要导出的字段，只导出这些字段
		// 2、定义要过滤的字段
		String[] fieldAry = getExportAry(paramVO);
		if(fieldAry != null && fieldAry.length > 0) {
			List<BillTempletBVO> definedFieldVOs = new ArrayList<BillTempletBVO>(fieldAry.length);
			List<Integer> indexAry = new ArrayList<Integer>(fieldAry.length);
			List<List<Object>> definedDataList = new ArrayList<List<Object>>();
			for(String fieldName : fieldAry) {
				for(int i = 0; i < fieldVOs.size(); i++) {
					BillTempletBVO fieldVO = fieldVOs.get(i);
					if(fieldName.equals(fieldVO.getItemkey())) {
						indexAry.add(i);
						definedFieldVOs.add(fieldVO);
						break;
					}
				}
			}
			for(List<Object> dataAry : dataList) {
				List<Object> rowDataAry = new ArrayList<Object>(fieldAry.length);
				for(Integer index : indexAry) {
					rowDataAry.add(dataAry.get(index));
				}
				definedDataList.add(rowDataAry);
			}
			fieldVOs = definedFieldVOs;
			dataList = definedDataList;
		} else {
			String[] filterAry = getExportFilterAry(paramVO);
			if(filterAry != null && filterAry.length > 0) {
				for(String filter : filterAry) {
					int index = -1;
					for(int i = 0; i < fieldVOs.size(); i++) {
						BillTempletBVO fieldVO = fieldVOs.get(i);
						if(filter.equals(fieldVO.getItemkey())) {
							index = i;
							// 移除对象，该对象会生成excel的header
							fieldVOs.remove(fieldVO);
							break;
						}
					}
					if(index != -1) {
						// 同时删除数据
						for(List<Object> dataAry : dataList) {
							dataAry.remove(index);
						}
					}
				}
			}
		}
		String[] titleAry = this.getExportTitleAry(paramVO);
		if(titleAry == null || titleAry.length == 0) {
			titleAry = UiTempletUtils.getDefaultShowname(fieldVOs, paramVO);
		}
		String[] finalTitleAry = new String[titleAry.length + 1];
		if(addNumberRow(paramVO)) {
			finalTitleAry[0] = "序号"; // 序号列
			for(int i = 0; i < titleAry.length; i++) {
				finalTitleAry[i + 1] = titleAry[i];
			}

			for(int i = 0; i < dataList.size(); i++) {
				// 插入行号列
				List<Object> objs = dataList.get(i);
				objs.add(0, (i + 1));
			}
		} else {
			for(int i = 0; i < titleAry.length; i++) {
				finalTitleAry[i] = titleAry[i];
			}
		}
		POI excel = new POI();
		if(StringUtils.isNotBlank(getFirstSheetName())) {
			excel.setFirstSheetName(getFirstSheetName());
		}
		HSSFWorkbook wb = excel.buildExcel(finalTitleAry, dataList);
		logger.info("导出动作结束，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return wb;
	}

	/**
	 * 轻量级页面，不需要返回所有数据
	 * 
	 * @param datas
	 *            带过滤的数据
	 * @param fieldNames
	 *            保留的字段
	 * @return List 过滤后的数据
	 * @author xuqc
	 * @date 2012-5-10
	 */
	@SuppressWarnings("rawtypes")
	protected List<List<Object>> filterULWData(List datas, String[] fieldNames) {
		if(datas == null || datas.size() == 0) {
			return null;
		}
		if(fieldNames == null || fieldNames.length == 0) {
			// fieldNames不能为空，否则可能返回的数据格式根本就不一样
			return null;
		}
		List<List<Object>> retList = new ArrayList<List<Object>>();
		for(int i = 0; i < datas.size(); i++) {
			Object data = datas.get(i);
			retList.add(filterULWData(data, fieldNames));
		}
		return retList;
	}

	/**
	 * 过滤单个数据对象
	 */
	@SuppressWarnings("rawtypes")
	protected List<Object> filterULWData(Object data, String[] fieldNames) {
		if(fieldNames == null || fieldNames.length == 0) {
			// fieldNames不能为空，否则可能返回的数据格式根本就不一样
			return null;
		}
		List<Object> values = new ArrayList<Object>();
		if(data instanceof SuperVO) {
			SuperVO superVO = (SuperVO) data;
			for(int j = 0; j < fieldNames.length; j++) {
				Object value = superVO.getAttributeValue(fieldNames[j]);
				values.add(value);
			}
		} else if(data instanceof Map) {
			Map dataMap = (Map) data;
			for(int j = 0; j < fieldNames.length; j++) {
				Object value = dataMap.get(fieldNames[j]);
				values.add(value);
			}
		}
		return values;
	}

	/**
	 * 对于返回的app数据，过滤其表头的字段，只返回需要的字段值
	 * 
	 * @param dataMap
	 * @param paramVO
	 * @author xuqc
	 * @date 2012-5-10
	 */
	public List<Object> processULWData(Object data, ParamVO paramVO) {
		if(data == null) {
			return null;
		}
		UiBillTempletVO templetVO = this.getBillTempletVO(getBillTemplateID(paramVO));
		if(getBillInfo().getParentVO() != null) {
			templetVO.setHeaderPkField(getBillInfo().getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString());
		}
		List<BillTempletBVO> ulwFieldVOs = UiTempletUtils.filterULWFields(templetVO.getFieldVOs(), null, paramVO);
		String[] listShowFieldNames = new String[ulwFieldVOs.size()];
		for(int i = 0; i < ulwFieldVOs.size(); i++) {
			listShowFieldNames[i] = ulwFieldVOs.get(i).getItemkey();
		}
		List<Object> valueList = filterULWData(data, listShowFieldNames);
		for(int i = 0; i < valueList.size(); i++) {
			Object value = valueList.get(i); // 值
			BillTempletBVO fieldVO = ulwFieldVOs.get(i); // 对应的模板配置
			IRenderer renderer = getULWRendererMap(fieldVO.getDatatype(), fieldVO.getReftype()).get(
					fieldVO.getDatatype().intValue());
			if(renderer != null) {
				value = renderer.render(value, fieldVO.getDatatype().intValue(), fieldVO.getReftype());
				valueList.set(i, value);
			}
		}
		return valueList;
	}

	/**
	 * renderer的实现类，根据数据类型返回map
	 * 
	 * @param datatype
	 * @param reftype
	 * @return
	 */
	public Map<Integer, IRenderer> getULWRendererMap(Integer datatype, String reftype) {
		return DataTypeConverter.ULWRendererMap;
	}

	/**
	 * 过滤ULW页面的字段，目前使用在导出中,一般是过滤掉隐藏的字段，留下显示的字段
	 * 
	 * @param list
	 * @param immobilityFields
	 * @param paramVO
	 * @return
	 */
	protected List<BillTempletBVO> filterULWFields(List<BillTempletBVO> list, List<String> immobilityFields,
			ParamVO paramVO) {
		return UiTempletUtils.filterULWFields(list, immobilityFields, paramVO);
	}

	// TODO renderer使用与报表相同的处理器
	@SuppressWarnings("rawtypes")
	public List<List<Object>> processULWData(List datas, ParamVO paramVO) {
		Map<String, List<DataDictBVO>> params = new HashMap<String, List<DataDictBVO>>();
		if (datas == null || datas.size() == 0) {
			return new ArrayList<List<Object>>();
		}
		UiBillTempletVO templetVO = this.getBillTempletVO(getBillTemplateID(paramVO));
		List<BillTempletBVO> ulwFieldVOs = filterULWFields(templetVO.getFieldVOs(), null, paramVO);
		if (ulwFieldVOs != null && ulwFieldVOs.size() > 0) {
			List<String> refTypes = new ArrayList<String>();
			String datatype_code = "";
			for (BillTempletBVO tempfieldVO : ulwFieldVOs) {
				if (tempfieldVO.getReftype() != null) {
					if (tempfieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.SF.toString())
							|| tempfieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.IF.toString())
							|| tempfieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
						datatype_code = tempfieldVO.getReftype()
								.substring(tempfieldVO.getReftype().lastIndexOf(",") + 1);
						refTypes.add(datatype_code);
					}
				}
			}
			if (refTypes.size() > 0) {
				String cond = NWUtils.buildConditionString(refTypes.toArray(new String[refTypes.size()]));
				String corpcond = CorpHelper.getCurrentCorpWithGroup();
				String sql = "select nw_data_dict_b.*,nw_data_dict.datatype_code from nw_data_dict_b WITH (NOLOCK) "
						+ "LEFT JOIN nw_data_dict WITH (NOLOCK)  ON nw_data_dict_b.pk_data_dict = nw_data_dict.pk_data_dict and isnull(nw_data_dict.dr,0)=0 "
						+ "where isnull(nw_data_dict_b.dr,0)=0 and nw_data_dict.locked_flag='N' and  " + corpcond
						+" and nw_data_dict.datatype_code in  "
						+ cond;
				List<DataDictBVO> dictBVOs = NWDao.getInstance().queryForListWithCache(sql, DataDictBVO.class);
				for (DataDictBVO dictBVO : dictBVOs) {
					String key = dictBVO.getDatatype_code().toString();
					List<DataDictBVO> voList = params.get(key);
					if (voList == null) {
						voList = new ArrayList<DataDictBVO>();
						params.put(key, voList);
					}
					voList.add(dictBVO);
				}

			}

		}
		//yaojiie 2015 12 08 一次性查出数据字典的所有结果，优化速度。
		String[] listShowFieldNames = new String[ulwFieldVOs.size()];
		for (int i = 0; i < ulwFieldVOs.size(); i++) {
			listShowFieldNames[i] = ulwFieldVOs.get(i).getItemkey();
		}
		List<List<Object>> values = filterULWData(datas, listShowFieldNames);
		for (int i = 0; i < values.size(); i++) {
			List<Object> valueList = values.get(i);
			for (int j = 0; j < valueList.size(); j++) {
				Object value = valueList.get(j); // 值
				BillTempletBVO fieldVO = ulwFieldVOs.get(j); // 对应的模板配置
				IRenderer renderer = getULWRendererMap(fieldVO.getDatatype(), fieldVO.getReftype())
						.get(fieldVO.getDatatype().intValue());
				if (renderer != null) {
					if (fieldVO.getReftype() != null) {
						if (fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.SF.toString())
								|| fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.IF.toString())
								|| fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
							String reftype = fieldVO.getReftype().substring(fieldVO.getReftype().lastIndexOf(",") + 1);
							if (params.containsKey(reftype)) {
								List<DataDictBVO> refdictBVOs = params.get(reftype);
								for (DataDictBVO dataDictBVO : refdictBVOs) {
									if (value!=null&&value.equals(dataDictBVO.getValue())) {
										value = dataDictBVO.getDisplay_name();
										break;
									}
								}
							}
						}else{
							value = renderer.render(value, fieldVO.getDatatype().intValue(), fieldVO.getReftype());
						}
					} else {
						value = renderer.render(value, fieldVO.getDatatype().intValue(), fieldVO.getReftype());
					}
					valueList.set(j, value);
				}
			}
		}
		return values;
	}

	/**
	 * 返回ulw列表页所需要的固定的字段值，这些字段是隐藏的。
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-6-4
	 */
	public List<String> getULWImmobilityFields(ParamVO paramVO) {
		List<String> fields = new ArrayList<String>();
		fields.add(this.getBillInfo().getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString()); // 主键
		fields.add("ts");// ts
		fields.add("vbillstatus");// 单据状态
		return fields;
	}

	private String buildSelectPKSql(SuperVO parentVO, ParamVO paramVO, String orderBy, String extendCond) {
		String params = ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
		/**
		 * 查询模板的相关信息
		 */
		UiQueryTempletVO queryTempletVO = this.getQueryTempletVO(getQueryTemplateID(paramVO));
		/**
		 * 2.组装查询条件-sql where
		 */
		StringBuilder where = new StringBuilder();
		// 组装前台查询条件
		String dataWhere = null;
		dataWhere = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
		if(StringUtils.isNotBlank(dataWhere)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(dataWhere);
		}

		if(StringUtils.isBlank(params)) {
			String defaultCond = this.buildDefaultCond(queryTempletVO);
			if(StringUtils.isNotBlank(defaultCond)) {
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(defaultCond);
			}
		}
		String immobilityCond = this.buildImmobilityCond(queryTempletVO);
		if(StringUtils.isNotBlank(immobilityCond)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(immobilityCond);
		}

		/**
		 * 处理扩展参数 如左边树的参数
		 */
		if(StringUtils.isNotBlank(extendCond)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(extendCond);
		}

		/**
		 * 2.2.处理排序
		 */
		orderBy = this.buildLoadDataOrderBy(paramVO, parentVO.getClass(), orderBy);
		if(orderBy != null && orderBy.length() > 0) {
			where.append(" ");
			where.append(orderBy);
		}
		return DaoHelper.buildSelectSql(parentVO.getClass(),
				DaoHelper.getWhereClause(parentVO.getClass(), where.toString()),
				new String[] { parentVO.getPKFieldName() });
	}

	public AggregatedValueObject doPrev(ParamVO paramVO, String orderBy, String extendCond, Object... values) {
		logger.info("查询当前单据的上一条单据，主键：" + paramVO.getBillId());
		AggregatedValueObject billInfo = this.getBillInfo();
		Class<? extends SuperVO> voClass = ServiceHelper.getVOClass(billInfo, paramVO);
		SuperVO parentVO = null;
		try {
			parentVO = voClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("VOTable中表头类名配置不正确！");
		}
		String sql = buildSelectPKSql(parentVO, paramVO, orderBy, extendCond);
		Object oPrevPK = NWDao.getInstance().getPreviewPk(sql, parentVO.getPKFieldName(), paramVO.getBillId(), values);
		if(oPrevPK == null) {
			return null;
		}
		paramVO.setBillId(oPrevPK.toString());
		return this.show(paramVO);
	}

	public AggregatedValueObject doNext(ParamVO paramVO, String orderBy, String extendCond, Object... values) {
		logger.info("查询当前单据的下一条单据，主键：" + paramVO.getBillId());
		AggregatedValueObject billInfo = this.getBillInfo();
		Class<? extends SuperVO> voClass = ServiceHelper.getVOClass(billInfo, paramVO);
		SuperVO parentVO = null;
		try {
			parentVO = voClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("VOTable中表头类名配置不正确！");
		}
		String sql = buildSelectPKSql(parentVO, paramVO, orderBy, extendCond);
		Object oNextPK = NWDao.getInstance().getNextPk(sql, parentVO.getPKFieldName(), paramVO.getBillId(), values);
		if(oNextPK == null) {
			return null;
		}
		paramVO.setBillId(oNextPK.toString());
		return this.show(paramVO);
	}

	/********** 打印相关 ****************/
	public Map<String, Object> getBillPrintParameterMap(HttpServletRequest request, ParamVO paramVO) {
		AggregatedValueObject billInfo = this.getBillInfo();
		String pkField = billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(pkField, paramVO.getBillId());

		// 加载图片资源
		paramMap.putAll(new ImageResource().load());
		return paramMap;
	}

	/**
	 * 返回打印模板的数据源，子类可以继承
	 * 
	 * @param paramVO
	 * @param uiBillTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	public List<Map<String, Object>> getBillPrintDataSource(ParamVO paramVO, UiBillTempletVO uiBillTempletVO,
			UiPrintTempletVO uiPrintTempletVO) {
		if(StringUtils.isNotBlank(paramVO.getBillId())) {
			// 打印一张单据的情况，用于单据的打印
			AggregatedValueObject billVO = this.queryBillVO(paramVO);
			/**
			 * 分别获取主表和子表的打印上下文，并执行单据模板中的公式
			 */
			// 1.表头表尾的上下文
			Map<String, Object> headerFooterContext = this.getHeaderFooterPrintContext(billVO, uiBillTempletVO,
					uiPrintTempletVO);
			// 2.对打印上下文进行特殊处理
			PrintTempletUtils.changeRefPkToName(headerFooterContext, uiBillTempletVO.getFieldVOs());
			// 3.转换key
			Map<String, Object> newHeaderFooterContext = new HashMap<String, Object>();
			if(StringUtils.isNotBlank(this.getHeaderPrintPrefix())) {
				for(String key : headerFooterContext.keySet()) {
					newHeaderFooterContext.put(this.getHeaderPrintPrefix() + key, headerFooterContext.get(key));
				}
			}
			if(StringUtils.isNotBlank(this.getFooterPrintPrefix())) {
				for(String key : headerFooterContext.keySet()) {
					newHeaderFooterContext.put(this.getFooterPrintPrefix() + key, headerFooterContext.get(key));
				}
			}

			// 1.表体的上下文，同时也作为整个单据的数据源，如果表头数据源存在，那么会加入表体每一行的map中
			List<Map<String, Object>> bodyContext = this.getBodyPrintContext(billVO, uiBillTempletVO, uiPrintTempletVO);
			if(bodyContext == null) {
				bodyContext = new ArrayList<Map<String, Object>>();
			}
			if(bodyContext.size() > 0) {
				// 2014-12-13将表体第一行的数据加入表头，方便有些套打单据打印使用
				Map<String, Object> firstMap = bodyContext.get(0);
				if(StringUtils.isNotBlank(this.getBodyPrintPrefix())) {
					for(String key : firstMap.keySet()) {
						newHeaderFooterContext.put(this.getBodyPrintPrefix() + key + "_0", firstMap.get(key));
					}
				} else {
					for(String key : firstMap.keySet()) {
						newHeaderFooterContext.put(this.getBodyPrintPrefix() + key + "_0", firstMap.get(key));
					}
				}
			}
			// 2.对打印上下文进行特殊处理
			PrintTempletUtils.changeRefPkToName(bodyContext, uiBillTempletVO.getFieldVOs());
			// 3.转换key
			if(StringUtils.isNotBlank(getBodyPrintPrefix())) {
				List<Map<String, Object>> newBodyContext = new ArrayList<Map<String, Object>>();
				for(Map<String, Object> map : bodyContext) {
					Map<String, Object> newMap = new HashMap<String, Object>();
					for(String key : map.keySet()) {
						newMap.put(this.getBodyPrintPrefix() + key, map.get(key));
					}
					newBodyContext.add(newMap);
				}
				bodyContext = newBodyContext;
			}

			// 把表头表尾数据加进表体第一行(表体需要打印表头变量)
			if(bodyContext.size() > 0) {
				for(Map<String, Object> rowMap : bodyContext) {
					rowMap.putAll(newHeaderFooterContext);
				}
			} else {
				bodyContext.add(newHeaderFooterContext);
			}
			return bodyContext;
		}
		return null;
	}

	public Map<String, Object> getReportPrintParameterMap(HttpServletRequest request, ParamVO paramVO) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 加载图片资源
		paramMap.putAll(new ImageResource().load());
		return paramMap;
	}

	/**
	 * 报表查询模板的上下文，其实就是整个报表的loadData数据集
	 * 
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getReportPrintDataSource(ParamVO paramVO, UiReportTempletVO uiReportTempletVO,
			UiPrintTempletVO uiPrintTempletVO) {
		int offset = Constants.DEFAULT_OFFSET_WITH_NOPAGING;
		int pageSize = Constants.DEFAULT_PAGESIZE_WITH_NOPAGING;
		PaginationVO pageVO = this.loadData(paramVO, offset, pageSize, null, null, null);
		List<Map<String, Object>> context = pageVO.getItems();
		// 对于报表的打印，只执行表头的公式
		// 取主表的tabcode(表头和表尾)
		String[] extFormulas = null;
		String head_formula = uiPrintTempletVO.getPrintTempletVO().getHead_formula();
		if(StringUtils.isNotBlank(head_formula)) {
			extFormulas = head_formula.split(";");
		}
		// 执行公式
		context = FormulaHelper.execFormula(context, extFormulas, true);

		return context;
	}

	/**
	 * 打印模板中，表头变量的前缀
	 */
	private String getHeaderPrintPrefix() {
		return "h_";
	}

	/**
	 * 打印模板中，表体变量的前缀
	 */
	private String getBodyPrintPrefix() {
		return "";
	}

	/**
	 * 打印模板中，表尾变量的前缀
	 */
	private String getFooterPrintPrefix() {
		return "h_";
	}

	/**
	 * 获取打印时的主表上下文
	 * 
	 * @param billVO
	 * @param uiBillTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	protected Map<String, Object> getHeaderFooterPrintContext(AggregatedValueObject billVO,
			UiBillTempletVO uiBillTempletVO, UiPrintTempletVO uiPrintTempletVO) {
		if(billVO.getParentVO() == null) {
			throw new RuntimeException("没有获取到主表数据，可能该单据已被删除！");
		}

		// 取主表的tabcode(表头和表尾)
		String[] tabCodes = this.getTableCodes(uiBillTempletVO.getHeaderTabCode(), false);
		String[] extFormulas = null;
		String head_formula = uiPrintTempletVO.getPrintTempletVO().getHead_formula();
		if(StringUtils.isNotBlank(head_formula)) {
			extFormulas = head_formula.split(";");
		}
		// 执行公式
		Map<String, Object> context = this.execFormula4Templet(billVO.getParentVO(), uiBillTempletVO, false, tabCodes,
				extFormulas);

		// 对小数进行格式化
		DataFormatHelper.formatDecimal(context, uiBillTempletVO, tabCodes);
		// 对下拉框进行格式化
		DataFormatHelper.formatSelect(context, uiBillTempletVO, tabCodes);

		// 主表特殊处理-打印日期、打印时间
		context.put("_print_date", PrintTempletUtils.format_date.format(new Date()));
		context.put("_print_time", PrintTempletUtils.format_datetime.format(new Date()));
		context.put("_print_user_name", WebUtils.getLoginInfo().getUser_name());
		return context;
	}

	/**
	 * 获取打印子表的tableCode(多子表时，只能打印指定tableCode的子表，单子表、单表头时，可以为空)
	 */
	protected String getBodyPrintTableCode() {
		// XXX 2013-11-5 可能就不想打印表体
		return null;
		// throw new RuntimeException("多子表时，必须指定要打印子表的tableCode！");
	}

	/**
	 * 获取打印时的子表上下文
	 * 
	 * @param billVO
	 * @param uiBillTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	protected List<Map<String, Object>> getBodyPrintContext(AggregatedValueObject billVO,
			UiBillTempletVO uiBillTempletVO, UiPrintTempletVO uiPrintTempletVO) {
		List<Map<String, Object>> bodyContext = new ArrayList<Map<String, Object>>();

		String tableCode = null;
		if(billVO instanceof IExAggVO) {
			// 如果是多子表
			tableCode = this.getBodyPrintTableCode();// 这里指定
			if(tableCode != null) {
				// 首先转换为Map的list，因为执行公式需要使用这种数据结构
				CircularlyAccessibleValueObject[] cvos = ((IExAggVO) billVO).getTableVO(tableCode);
				if(cvos != null) {
					List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(cvos.length);
					for(int i = 0; i < cvos.length; i++) {
						Map<String, Object> _map = new HashMap<String, Object>();
						String[] attrs = cvos[i].getAttributeNames();
						for(String key : attrs) {
							_map.put(key, cvos[i].getAttributeValue(key));
						}
						mapList.add(_map);
					}
					// 就算是多子表的情况，这里也只支持一个子表
					String[] extFormulas = null;
					String body_formula = uiPrintTempletVO.getPrintTempletVO().getBody_formula();
					if(StringUtils.isNotBlank(body_formula)) {
						extFormulas = body_formula.split(";");
					}
					// 执行公式
					bodyContext = this.execFormula4Templet(mapList, uiBillTempletVO, true,
							this.getTableCodes(tableCode, true), extFormulas);
				}
			}
		} else {
			// 单表体
			tableCode = uiBillTempletVO.getBodyTabCode();
			if(billVO.getChildrenVO() != null) {
				// 3.2取表体的tabcode

				// 首先转换为Map的list，因为执行公式需要使用这种数据结构
				List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(billVO.getChildrenVO().length);
				for(int i = 0; billVO.getChildrenVO() != null && i < billVO.getChildrenVO().length; i++) {
					Map<String, Object> _map = new HashMap<String, Object>();
					for(String key : billVO.getChildrenVO()[i].getAttributeNames()) {
						_map.put(key, billVO.getChildrenVO()[i].getAttributeValue(key));
					}
					mapList.add(_map);
				}
				String[] extFormulas = null;
				String body_formula = uiPrintTempletVO.getPrintTempletVO().getBody_formula();
				if(StringUtils.isNotBlank(body_formula)) {
					extFormulas = body_formula.split(";");
				}
				// 执行公式
				bodyContext = this.execFormula4Templet(mapList, uiBillTempletVO, true,
						this.getTableCodes(tableCode, true), extFormulas);
			}
		}

		// 对小数进行格式化
		DataFormatHelper.formatDecimal(bodyContext, uiBillTempletVO, this.getTableCodes(tableCode, true));
		// 对下拉框进行格式化
		DataFormatHelper.formatSelect(bodyContext, uiBillTempletVO, this.getTableCodes(tableCode, true));

		return bodyContext;
	}

	public List<Object> getLockingItemAry(ParamVO paramVO) {
		List<Object> itemAry = new ArrayList<Object>();
		List<QueryConditionVO> condVOs = getLockingConds(paramVO);
		if(condVOs != null) {
			for(QueryConditionVO condVO : condVOs) {
				Object item = UIUtils.buildLockingField(condVO, true);
				// XXX 以前的处理方式是对于between生成的两个输入框作为单独的组件加入。现在是将他们作为一个分组加入
				// if(item instanceof List) {
				// List<org.nw.jf.ext.Field> ary = (List<org.nw.jf.ext.Field>)
				// item;
				// for(org.nw.jf.ext.Field field : ary) {
				// itemAry.add(field);
				// }
				// } else {
				itemAry.add(item);
				// }
			}
		}
		return itemAry;
	}

	/**
	 * 返回锁定的条件，锁定的条件在模板中是以if_desc字段来定义的，锁定条件同时必须是使用的
	 * 
	 * @param paramVO
	 * @return
	 */
	public List<QueryConditionVO> getLockingConds(ParamVO paramVO) {
		UiQueryTempletVO queryTempletVO = getQueryTempletVO(getQueryTemplateID(paramVO));
		if(queryTempletVO == null) {
			return null;
		}
		List<QueryConditionVO> lockingConds = new ArrayList<QueryConditionVO>();
		for(QueryConditionVO condVO : queryTempletVO.getConditions()) {
			if(condVO.getIf_used().booleanValue() && condVO.getIf_desc() != null && condVO.getIf_desc().booleanValue()) {
				lockingConds.add(condVO);
			}
		}
		return lockingConds;
	}

	public UiBillTempletVO getBillTempletVOByFunCode(ParamVO paramVO) {
		if(StringUtils.isBlank(paramVO.getFunCode())) {
			throw new RuntimeException("功能注册编码不能为空！");
		}
				String templetID = RedisDao.getInstance().getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
								UiConstants.TPL_STYLE.BILL.intValue());
//		String templetID = TempletHelper.getTempletID(paramVO.getFunCode(), paramVO.getNodeKey(),
//				UiConstants.TPL_STYLE.BILL.intValue());
//		UiBillTempletVO templetVO = TempletHelper.getOriginalBillTempletVO(templetID);
		UiBillTempletVO templetVO = RedisDao.getInstance().getOriginalBillTempletVO(templetID);
		return templetVO;
	}

	public UiBillTempletVO getBillTempletVOByBilltypecode(String pk_billtypecode) {
		// FIXME 这里可能返回多条
		BillTempletVO vo = NWDao.getInstance().queryByCondition(BillTempletVO.class,
				new String[] { BillTempletVO.PK_BILLTEMPLET }, "pk_billtypecode=?", pk_billtypecode);
		if(vo == null) {
			return null;
		}
		UiBillTempletVO templetVO = TempletHelper.getOriginalBillTempletVO(vo.getPk_billtemplet());
		//UiBillTempletVO templetVO = RedisDao.getInstance().getOriginalBillTempletVO(vo.getPk_billtemplet());
		return templetVO;
	}

	public List<RefInfoVO> loadSystemRef() {
		RefinfoDao dao = new RefinfoDao();
		Map<String, RefInfoVO> map = dao.getNameRefinfoMap();
		List<RefInfoVO> vos = new ArrayList<RefInfoVO>();
		vos.addAll(map.values());
		return vos;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional
	public String doImport(ParamVO paramVO, File file,String pk_import_config) throws Exception {
		if(StringUtils.isBlank(pk_import_config) || pk_import_config.equals(",")){
			throw new BusiException("请先选择要导入的模板！");
		}
		pk_import_config = pk_import_config.substring(pk_import_config.indexOf(",")+1, pk_import_config.length());
		ImportConfigVO configVO = NWDao.getInstance().queryByCondition(ImportConfigVO.class, "pk_import_config=?", pk_import_config);
		if(configVO == null || StringUtils.isBlank(configVO.getImporter())){
			throw new BusiException("导入模板维护错误！");
		}
		logger.info("开始执行档案导入动作...");
		Calendar start = Calendar.getInstance();
		ExcelImporter importer = null;
		try {
			Class clazz = Class.forName(configVO.getImporter());
			Constructor c = clazz.getConstructor(ParamVO.class,IToftService.class,ImportConfigVO.class);
			importer = (ExcelImporter)c.newInstance(new Object[]{paramVO, this, configVO});
		} catch (Exception e) {
			throw new BusiException("处理类[" + configVO.getImporter() + "],错误！");
		}
		importer._import(file);
		logger.info("导入操作结束，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return importer.getLog();
	}
	
}

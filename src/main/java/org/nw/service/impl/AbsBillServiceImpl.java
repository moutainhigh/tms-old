package org.nw.service.impl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.apache.commons.lang.StringUtils;
import org.nw.BillStatus;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.IBillService;
import org.nw.service.ServiceHelper;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CorpHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.ProcessEngineHelper;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单据的处理逻辑
 * 
 * @author xuqc
 * @date 2012-7-7 上午10:52:13
 */
public abstract class AbsBillServiceImpl extends AbsToftServiceImpl implements IBillService {

	/**
	 * 对于单据类型，就是单据号
	 */
	public String getCodeFieldCode() {
		return "vbillno";
	}

	public String getBillStatusField() {
		return "vbillstatus";
	}
	
	public String getUnConfirmTypeField() {
		return "unconfirm_type";
	}
	public String getUnConfirmMemoField() {
		return "unconfirm_memo";
	}
	public String getCommitTimeField() {
		return "commit_time";
	}
	public String getCommitUserField() {
		return "commit_user";
	}
	public String getConfirmTimeField() {
		return "confirm_time";
	}
	public String getConfirmUserField() {
		return "confirm_user";
	}
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		templetVO.setBillType(this.getBillType());
		return templetVO;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put(getBillStatusField(), BillStatus.NEW);// 默认新建状态
		values.put("dbilldate", new UFDate(new Date()));// 默认单据日期
		return values;
	}

	/**
	 * 对于单据类型，就是设置单据号
	 */
	public void setCodeField(CircularlyAccessibleValueObject parentVO, ParamVO paramVO) {
		if(StringUtils.isNotBlank(getCodeFieldCode())) {
			// 子类继承了该方法，说明希望使用编码规则
			Object codeObj = parentVO.getAttributeValue(getCodeFieldCode());
			if(codeObj == null || StringUtils.isBlank(codeObj.toString())) {
				// 如果没有录入编码，则按照规则生成一个
				String billno = BillnoHelper.generateBillno(this.getBillType());
				if(StringUtils.isBlank(billno)) {
					throw new RuntimeException("可能没有定义单据号规则，无法生成编码！");
				}
				SuperVO superVO = this.getByCodeWithNoDr(billno);
				while(superVO != null) {
					// 该订单号已经存在
					billno = BillnoHelper.generateBillno(this.getBillType());
					superVO = this.getByCodeWithNoDr(billno);
				}
				parentVO.setAttributeValue(getCodeFieldCode(), billno);
			}
		}
	}

	/**
	 * 对于单据，一般只能查询本公司及其子公司的单据
	 */
	@SuppressWarnings("rawtypes")
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = parseCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}

		if(!paramVO.isBody()) {
			boolean useDefaultCorpCondition = useDefaultCorpCondition(paramVO);
			if(useDefaultCorpCondition) {
				Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
				try {
					SuperVO superVO = (SuperVO) clazz.newInstance();
					Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
					if(pk_corp != null) {
						String corpCond = getCorpCondition(superVO.getTableName(), paramVO, templetVO);
						if(StringUtils.isNotBlank(corpCond)) {
							fCond += " and " + corpCond;
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return fCond;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#getCorpCondition(org.nw.vo.ParamVO
	 * , org.nw.jf.vo.UiQueryTempletVO)
	 */
	public String getCorpCondition(String tablePrefix, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return CorpHelper.getCurrentCorpWithChildren(tablePrefix);
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		parentVO.setAttributeValue("vbillstatus", BillStatus.NEW); // 默认新增状态
		parentVO.setAttributeValue("vbillno", null); // 清空单据号
	}

	/**
	 * 返回确认状态，这里为了方便定义确认按钮的逻辑，子类只需要定义单据的确认状态的值
	 * 
	 * @return
	 */
	protected Integer getConfirmStatus() {
		return null;
	}

	@Transactional
	public AggregatedValueObject confirm(ParamVO paramVO) {
		logger.info("执行单据确认动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.NEW != billStatus) {
				throw new RuntimeException("只有[新建]状态的单据才能进行确认！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
		dao.saveOrUpdate(billVO);
		return billVO;
	}
	
	public SuperVO[] batchConfirm(ParamVO paramVO,String[] ids) {
		logger.info("执行单据批量确认动作！");
		SuperVO[] parentVOs = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends SuperVO> parentVOClass = (Class<? extends SuperVO>) Class.forName(this.getBillInfo().getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			parentVOs = getByPrimaryKeys(parentVOClass, ids);
		} catch (Exception e) {
			logger.info("获取主表类出错");
			throw new BusiException("获取主表类出错");
		}
		processBeforeBatchConfirm(paramVO,parentVOs);
		doProcBeforeBatchConfirmAndUnconfirm(paramVO,parentVOs,0);
		for(SuperVO parentVO : parentVOs){
			Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
			if(oBillStatus != null) {
				int billStatus = Integer.parseInt(oBillStatus.toString());
				if(BillStatus.NEW != billStatus) {
					throw new RuntimeException("只有[新建]状态的单据才能进行确认！");
				}
				parentVO.setStatus(VOStatus.UPDATED);
				parentVO.setAttributeValue(getConfirmTimeField(), new UFDateTime(new Date()));
				parentVO.setAttributeValue(getConfirmUserField(), WebUtils.getLoginInfo().getPk_user());
				parentVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
			}
		}
		dao.saveOrUpdate(parentVOs);
		return parentVOs;
	}

	/**
	 * 执行confirm前的动作
	 * 
	 * @param billVO
	 */
	public void processBeforeConfirm(AggregatedValueObject billVO, ParamVO paramVO) {

	}
	
	/**
	 * 
	 * @param paramVO 参数信息，主要获取funcode
	 * @param parentVOs 主要获取Ids
	 * @param type 类型。确认为0，反确认是1
	 */
	public void doProcBeforeBatchConfirmAndUnconfirm(ParamVO paramVO,SuperVO[] parentVOs, Integer type) {
		if(parentVOs == null || parentVOs.length == 0){
			return;
		}
		String funcode = paramVO.getFunCode();
		if(StringUtils.isBlank(funcode)){
			return;
		}
		String procName = getConfirmAndUnconfirmProcName();
		if(StringUtils.isBlank(procName)){
			return;
		}
		String ids = "";
		for(SuperVO parentVO : parentVOs){
			ids = parentVO.getPrimaryKey() + ",";
		}
		ids = ids.substring(0,ids.length()-1);
		String msg = doProcByConfirmAndUnConform(ids, procName, type);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String doProcByConfirmAndUnConform(String ids,String procName,Integer type){
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = procName;
		final String PK = ids;
		final String TYPE = type.toString();
		final String USER = WebUtils.getLoginInfo() == null ? "" : WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK);
					cs.setString(2, TYPE);
					cs.setString(3, USER);
					cs.setString(4, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						RETURN.add(rs.getString(1));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RETURN.get(0);
	} 
	
	protected String getConfirmAndUnconfirmProcName(){
		return null;
 	}
	
	/**
	 * 执行confirm前的动作
	 * 
	 * @param billVO
	 */
	public void processBeforeBatchConfirm(ParamVO paramVO,SuperVO[] parentVOs) {

	}

	@Transactional
	public AggregatedValueObject unconfirm(ParamVO paramVO) {
		logger.info("执行单据反确认动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeUnconfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(getConfirmStatus() != billStatus) {
				throw new RuntimeException("只有[确认]状态的单据才能进行反确认,单据号["
						+ parentVO.getAttributeValue(this.getCodeFieldCode()) + "]！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
		parentVO.setAttributeValue(getUnConfirmTypeField(), paramVO.getUnconfirmType());//反确认类型
		parentVO.setAttributeValue(getUnConfirmMemoField(), paramVO.getUnconfirmMemo());//反确认说明
		dao.saveOrUpdate(billVO);
		return billVO;
	}

	public SuperVO[] batchUnconfirm(ParamVO paramVO,String[] ids) {
		logger.info("执行单据批量反确认动作！");
		SuperVO[] parentVOs = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends SuperVO> parentVOClass = (Class<? extends SuperVO>) Class.forName(this.getBillInfo().getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			parentVOs = getByPrimaryKeys(parentVOClass, ids);
		} catch (Exception e) {
			logger.info("获取主表类出错");
			throw new BusiException("获取主表类出错");
		}
		processBeforeBatchUnconfirm(paramVO,parentVOs);
		doProcBeforeBatchConfirmAndUnconfirm(paramVO,parentVOs,1);
		for(SuperVO parentVO : parentVOs){
			Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
			if(oBillStatus != null) {
				int billStatus = Integer.parseInt(oBillStatus.toString());
				if(getConfirmStatus() != billStatus) {
					throw new RuntimeException("只有[确认]状态的单据才能进行反确认,单据号："
							+ parentVO.getAttributeValue(this.getCodeFieldCode()) + "；");
				}
				parentVO.setStatus(VOStatus.UPDATED);
				parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
				parentVO.setAttributeValue(getUnConfirmTypeField(), paramVO.getUnconfirmType());//反确认类型
				parentVO.setAttributeValue(getUnConfirmMemoField(), paramVO.getUnconfirmMemo());//反确认说明
			}
		}
		dao.saveOrUpdate(parentVOs);
		return parentVOs;
	}
	
	/**
	 * 执行unconfirm前的动作
	 * 
	 * @param billVO
	 */
	public void processBeforeUnconfirm(AggregatedValueObject billVO, ParamVO paramVO) throws BusiException {

	}
	
	public void processBeforeBatchUnconfirm(ParamVO paramVO,SuperVO[] parentVOs) {

	}

	@Transactional
	public AggregatedValueObject vent(ParamVO paramVO) {
		return null;
	}

	@Transactional
	public AggregatedValueObject unvent(ParamVO paramVO) {
		return null;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		if(parentVO != null) {
			Object dbilldate = parentVO.getAttributeValue("dbilldate");
			if(dbilldate == null) {
				parentVO.setAttributeValue("dbilldate", new UFDate());
			}
		}
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		// 只能删除单据状态为新增的单据
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object vbillstatus = parentVO.getAttributeValue(getBillStatusField());
		if(vbillstatus == null) {
			// 单据状态为空，这个单据不正常，可以执行删除操作，比如可能是测试数据
			logger.warn("该单据的单据状态为空,这不应该出现！");
		} else {
			if(Integer.parseInt(vbillstatus.toString()) != BillStatus.NEW) {
				throw new RuntimeException("只能删除单据状态为[新建]的单据！");
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		BillExcelImporter importer = null;
		try {
			Class clazz = Class.forName(configVO.getImporter());
			Constructor c = clazz.getConstructor(ParamVO.class,IBillService.class,ImportConfigVO.class);
			importer = (BillExcelImporter)c.newInstance(new Object[]{paramVO, this, configVO});
		} catch (Exception e) {
			throw new BusiException("处理类[" + configVO.getImporter() + "],错误！");
		}
		importer._import(file);
		logger.info("导入操作结束，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return importer.getLog();
	}

	public Map<String, Object> approve(ParamVO paramVO) {
		if(StringUtils.isBlank(paramVO.getBillId())) {
			return null;
		}
		TaskService service = ProcessEngineHelper.getProcessEngine().getTaskService();
		service.complete(paramVO.getBillId());
		return null;
	}

	public Map<String, Object> unapprove(ParamVO paramVO) {
		// TODO Auto-generated method stub
		return null;
	}
}

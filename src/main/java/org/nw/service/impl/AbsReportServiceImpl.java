package org.nw.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.basic.util.ReflectionUtils;
import org.nw.basic.util.SecurityUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.POI;
import org.nw.jf.UiConstants;
import org.nw.jf.group.GroupUtils;
import org.nw.jf.group.GroupVO;
import org.nw.jf.ulw.IRenderer;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.service.IReportService;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.QueryHelper;
import org.nw.utils.VariableHelper;
import org.nw.vo.LinkButtonVO;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.vo.sys.ReportTempletVO;
import org.nw.web.utils.ServletContextHolder;

/**
 * 报表service的抽象类，定义了报表所需要的公共的实现
 * 
 * @author xuqc
 * @date 2013-7-29 下午04:29:56
 */
public abstract class AbsReportServiceImpl extends AbsToftServiceImpl implements IReportService {

	public static final String CORRELATE_QUERY_KEY = "_link";
	public static final String CORRELATE_QUERY_TEXT = "联查";

	/**
	 * 对于报表，该方法不是必须实现的，可能只需要写个sql去查询就可以了。
	 * 当然也可以实现，比如对于发货单报表，那么只需要写将发货单的表头ＶＯ加入parent即可
	 */
	public AggregatedValueObject getBillInfo() {
		return null;
	}

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

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			// 模板中定义的order by
			UiReportTempletVO uiReportTempletVO = this.getReportTempletVO(paramVO.getTemplateID());
			ReportTempletVO vo = uiReportTempletVO.getReportTempletVO();
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				orderBy = vo.getOrder_by();
			}
		}
		return orderBy;
	}

	/**
	 * 返回报表查询的sql，如果定义了sql，那么使用该sql，不需要根据billInfo进行构造，<br/>
	 * 注意<br/>
	 * <li>1、该sql不能加入 order by, order by 的处理只能通过继承buildLoadDataOrderBy来实现</li>
	 * <li>2、必须包括where子句，实在没有where，可以写成where 1=1</li>
	 * 
	 * @return
	 */
	public String getSelectSql(ParamVO paramVO) {
		UiReportTempletVO uiReportTempletVO = this.getReportTempletVO(paramVO.getTemplateID());
		ReportTempletVO vo = uiReportTempletVO.getReportTempletVO();
		return vo.getSelect_sql();
	}

	/**
	 * 适应报表的查询
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReportVO loadReportData(ParamVO paramVO, int offset, int pageSize, String orderBy) {
		String params = ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
		UiQueryTempletVO queryTempletVO = this.getQueryTempletVO(this.getQueryTemplateID(paramVO));
		/**
		 * 2.组装查询条件-sql where
		 */
		StringBuilder where = new StringBuilder();
		// 组装前台查询条件
		String dataWhere = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
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
			String defaultCond = QueryHelper.getDefaultCond(queryTempletVO);
			if(StringUtils.isNotBlank(defaultCond)) {
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(defaultCond);
			}
		}
		// 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
		String immobilityCond = QueryHelper.getImmobilityCond(queryTempletVO);
		if(StringUtils.isNotBlank(immobilityCond)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(immobilityCond);
		}

		/**
		 * 1.获取对应的superVO
		 */
		AggregatedValueObject billInfo = this.getBillInfo();
		Class<? extends SuperVO> voClass = ServiceHelper.getVOClass(billInfo, paramVO);
		/**
		 * 2.2.处理排序
		 */
		//yaojiie 2016 1 15 去掉orderby 报表不需要
		/*orderBy = this.buildLoadDataOrderBy(paramVO, voClass, orderBy);
		if(orderBy != null && orderBy.length() > 0 && !orderBy.trim().equals("order by create_time desc")) {
			orderBy = orderBy.toLowerCase();
			if(orderBy.indexOf(Constants.ORDER_BY) != -1) {
				if(this.isCompatibleMode() || paramVO.isBody()) {
					where.append(" ");
					where.append(orderBy);
				}
			}
		}*/

		/**
		 * 3.进行查询
		 */
		NWDao dao = NWDao.getInstance();
		String sql = getSelectSql(paramVO);
		if(StringUtils.isBlank(sql)) {
			if(billInfo == null) {
				throw new BusiException("没有定义单据类型信息，则报表必须继承getSelectSql方法，返回自己的查询语句！");
			}
			if(this.isCompatibleMode()) {
				sql = DaoHelper.buildSelectSql(voClass, DaoHelper.getWhereClause(voClass, where.toString()),
						getLoadDataFields(), false);
			} else {
				// 使用合并子表条件的模式进行查询
				sql = ServiceHelper.buildSelectSql(billInfo, paramVO,
						DaoHelper.getWhereClause(voClass, where.toString()), getLoadDataFields());
			}
		} else {
			sql = VariableHelper.resolve(sql, null);
			String whereCondion = parseCondition(params, paramVO, queryTempletVO);
			UiReportTempletVO uiReportTempletVO = this.getReportTempletVO(paramVO.getTemplateID());
			ReportTempletVO vo = uiReportTempletVO.getReportTempletVO();
			String defaultWhere = VariableHelper.resolve(vo.getQuery_where(), null);
			String defaultOrderBy = vo.getOrder_by();
			if(StringUtils.isBlank(defaultWhere) && StringUtils.isNotBlank(whereCondion)){
				sql = sql + " where " + whereCondion;
			}else if(StringUtils.isNotBlank(defaultWhere) && StringUtils.isNotBlank(whereCondion)) {
				sql = sql + " where " + whereCondion + " and " + defaultWhere;
			}else if(StringUtils.isNotBlank(defaultWhere) && StringUtils.isBlank(whereCondion)){
				sql = sql + " where " + defaultWhere;
			}
			if(StringUtils.isNotBlank(defaultOrderBy)){
				sql = sql + " order by " + defaultOrderBy;
			}
			
//			if(where.toString().length() > 0) {
//				// 包括了查询条件
//				if(DB_TYPE.DB2.equals(NWDao.getCurrentDBType())) {
//					//sql = "SELECT * FROM (" + sql + ") _AA where 1=1 and " + where.toString();
//					sql = "SELECT * FROM (" + sql + ") _AA";
//				} else {
//					//yaojiie 2015 12 08添加 WITH(NOLOCK)
//					sql = "SELECT * FROM (" + sql + ") AS _AA "/* WITH(NOLOCK) where 1=1 and " + where.toString()*/;				}
//			}
		}

		//lanjian2015-12-20 处理排序报错
//		if(orderBy != null && orderBy.length() > 0 && orderBy.indexOf(Constants.ORDER_BY) != -1) {
//			sql += orderBy;
//		}
		// 适合前台自定义sql语句，这里直接返回hashMap就好了
		PaginationVO paginationVO = dao.queryBySqlWithPaging(sql, HashMap.class, offset, pageSize);
		// 在执行公式之前做一些操作
		UiReportTempletVO templetVO = this.getReportTempletVO(getReportTempletID(paramVO));
		ReportVO reportVO = new ReportVO();
		reportVO.setTempletVO(templetVO);
		reportVO.setPageVO(paginationVO);

		processReportBeforeExecFormula(reportVO, paramVO);

		// 统计行的数据
		paginationVO.setSummaryRowMap(buildReportSummaryRowMap(templetVO, voClass, where.toString()));
		/**
		 * 4.转换数据格式，准备执行公式
		 */
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<?> list = paginationVO.getItems();
		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>(list.size());
		for(Object rowObj : list) {
			if(rowObj instanceof SuperVO) {
				SuperVO vo = (SuperVO) rowObj;
				Map<String, Object> map = new HashMap<String, Object>();
				String[] attrs = vo.getAttributeNames();
				for(String key : attrs) {
					Object obj = vo.getAttributeValue(key);
					if(obj != null) {
						map.put(key, SecurityUtils.escape(obj.toString()));
					} else {
						map.put(key, obj);
					}
				}
				context.add(map);
			} else {
				HashMap map = (HashMap) rowObj;
				context.add(map);
			}
		}
		list.clear();

		/**
		 * 6.执行公式
		 */
		List<Map<String, Object>> retList = FormulaHelper.execFormula4Report(context, templetVO.getFieldVOs());
		paginationVO.setItems(retList);
		processReportAfterExecFormula(reportVO, paramVO);

		return reportVO;
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
	protected Map<String, Object> buildReportSummaryRowMap(UiReportTempletVO templetVO, Class<? extends SuperVO> clazz,
			String where) {
		List<String[]> fieldAry = templetVO.getHeaderSummaryFieldAry();
		if(fieldAry == null || fieldAry.size() == 0) {
			return null;
		}
		if(clazz == null) {
			// 没有定义billInfo，重写该方法，进行查询
			throw new BusiException("没有配置单据信息，请继承该方法，查询合计数据！");
		}
		String countSql = DaoHelper.buildSumSelectSql(clazz, where, fieldAry);
		if(StringUtils.isBlank(countSql)) {
			return null;
		}
		return NWDao.getInstance().queryForObject(countSql, HashMap.class);
	}

	@SuppressWarnings("rawtypes")
	public List<List<Object>> processULWData(ReportVO reportVO, ParamVO paramVO) {
		List datas = reportVO.getPageVO().getItems();
		if(datas == null || datas.size() == 0) {
			return new ArrayList<List<Object>>();
		}
		UiReportTempletVO templetVO = reportVO.getTempletVO();
		List<ReportTempletBVO> ulwFieldVOs = templetVO.getFieldVOs();
		ulwFieldVOs = filterULWFields(templetVO.getFieldVOs(), paramVO);
		String[] listShowFieldNames = new String[ulwFieldVOs.size()];
		for(int i = 0; i < ulwFieldVOs.size(); i++) {
			listShowFieldNames[i] = ulwFieldVOs.get(i).getItemkey();
		}
		List<List<Object>> values = filterULWData(datas, listShowFieldNames);
		for(int i = 0; i < values.size(); i++) {
			List<Object> valueList = values.get(i);
			for(int j = 0; j < valueList.size(); j++) {
				Object value = valueList.get(j); // 值
				ReportTempletBVO fieldVO = ulwFieldVOs.get(j); // 对应的模板配置
				IRenderer renderer = getULWRendererMap(fieldVO.getData_type(), fieldVO.getReftype()).get(
						fieldVO.getData_type().intValue());
				if(renderer != null) {
					value = renderer.render(value, fieldVO.getData_type().intValue(), fieldVO.getReftype());
					valueList.set(j, value);
				}
			}
		}
		return values;
	}

	public HSSFWorkbook export(ParamVO paramVO, int offset, int pageSize, String orderBy, String extendCond,
			Object... values) {
		ReportVO reportVO = this.loadReportData(paramVO, offset, pageSize, orderBy);
		// 这里会返回模板中只显示的列的数据
		List<List<Object>> dataList = processULWData(reportVO, paramVO);
		// 获取单据VO
		UiReportTempletVO templetVO = reportVO.getTempletVO();
		List<ReportTempletBVO> fieldVOs = templetVO.getFieldVOs();
		// 过虑掉模板中隐藏的列，与数据对应
		fieldVOs = filterULWFields(fieldVOs, paramVO);
		String[] fieldAry = getExportAry(paramVO);
		if(fieldAry != null && fieldAry.length > 0) {
			List<ReportTempletBVO> definedFieldVOs = new ArrayList<ReportTempletBVO>(fieldAry.length);
			List<Integer> indexAry = new ArrayList<Integer>(fieldAry.length);
			List<List<Object>> definedDataList = new ArrayList<List<Object>>();
			for(String fieldName : fieldAry) {
				for(int i = 0; i < fieldVOs.size(); i++) {
					ReportTempletBVO fieldVO = fieldVOs.get(i);
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
						ReportTempletBVO fieldVO = fieldVOs.get(i);
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
		String[] titleAry = getListDefaultShowname2(fieldVOs, paramVO);
		String[] finalTitleAry = new String[titleAry.length + 1];
		if(addNumberRow(paramVO)) {
			finalTitleAry[0] = ""; // 加入行号标题
			for(int i = 0; i < titleAry.length; i++) {
				finalTitleAry[i + 1] = titleAry[i];
			}

			for(int i = 0; i < dataList.size(); i++) {
				// 插入行号列内容
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
		return excel.buildExcel(finalTitleAry, dataList, getGroupHeaderAry(fieldVOs, paramVO));
	}

	/**
	 * 过滤ULW页面的字段，目前使用在导出中,一般是过滤掉隐藏的字段，留下显示的字段
	 * 
	 * @param list
	 * @param immobilityFields
	 * @param paramVO
	 * @return
	 */
	protected List<ReportTempletBVO> filterULWFields(List<ReportTempletBVO> list, ParamVO paramVO) {
		List<ReportTempletBVO> fieldVOs = new ArrayList<ReportTempletBVO>();
		for(ReportTempletBVO one : list) {
			if(one.getShow_flag() != null && one.getShow_flag().booleanValue()) {
				fieldVOs.add(one);
			}
		}
		return fieldVOs;
	}

	/**
	 * 返回导出的标题集合，可能是多表头
	 * 
	 * @param showFields
	 * @return
	 */
	protected List<List<GroupVO>> getGroupHeaderAry(List<ReportTempletBVO> showFields, ParamVO paramVO) {
		List<List<GroupVO>> groupVOs = GroupUtils.buildGroupHeaderAry(GroupUtils.getOptionsAry2(showFields),
				addNumberRow(paramVO) ? 1 : 0);
		return groupVOs;
	}

	/**
	 * 返回报表模板中字段对应的名称
	 * 
	 * @param showFields
	 * @return
	 */
	private String[] getListDefaultShowname2(List<ReportTempletBVO> showFields, ParamVO paramVO) {
		String[] fieldNames = new String[showFields.size()];
		for(int i = 0; i < showFields.size(); i++) {
			fieldNames[i] = showFields.get(i).getDefaultshowname();
		}
		return fieldNames;
	}

	/**
	 * 不要导出联查列
	 */
	public String[] getExportFilterAry(ParamVO paramVO) {
		return new String[] { CORRELATE_QUERY_KEY };
	}

	/**
	 * 是否加入联查列
	 * 
	 * @param paramVO
	 * @return
	 */
	protected boolean addCorrelateQuery(UiReportTempletVO templetVO, ParamVO paramVO) {
		return false;
	}

	/**
	 * 返回联查的按钮集合
	 * 
	 * @param paramVO
	 * @return
	 */
	protected LinkButtonVO[] getCorrelateButtonVOs(ParamVO paramVO) {
		return null;
	}

	/**
	 * 返回联查的列信息
	 * 
	 * @return
	 */
	protected ReportTempletBVO getCorrelateQueryCol() {
		ReportTempletBVO linkCol = new ReportTempletBVO();
		// linkCol.setPk_report_templet(firstVO.getPk_report_templet());
		linkCol.setPk_report_templet_b(UUID.randomUUID().toString());
		linkCol.setItemkey(CORRELATE_QUERY_KEY);
		linkCol.setDefaultshowname(CORRELATE_QUERY_TEXT);
		linkCol.setData_type(UiConstants.DATATYPE.TEXT.intValue());
		linkCol.setWidth(60);
		linkCol.setShow_flag(UFBoolean.TRUE);
		linkCol.setDisplay_order(0);
		return linkCol;
	}

	public UiReportTempletVO getReportTempletVO(String templateID) {
		UiReportTempletVO templetVO = super.getReportTempletVO(templateID);
		if(addCorrelateQuery(templetVO, null)) {
			List<ReportTempletBVO> fieldVOs = templetVO.getFieldVOs();
			ReportTempletBVO firstVO = fieldVOs.get(0);
			ReportTempletBVO linkCol = getCorrelateQueryCol();
			linkCol.setPk_report_templet(firstVO.getPk_report_templet());
			fieldVOs.add(0, linkCol);
		}
		return templetVO;
	}

	/**
	 * 报表加载数据后执行公式前的操作
	 * 
	 * @param templetVO
	 * @param pageVO
	 * @param paramVO
	 */
	protected void processReportBeforeExecFormula(ReportVO reportVO, ParamVO paramVO) {

	}

	/**
	 * 报表加载数据后执行公式后的操作
	 * 
	 * @param templetVO
	 * @param pageVO
	 * @param paramVO
	 * @param orderBy
	 */
	@SuppressWarnings("unchecked")
	protected void processReportAfterExecFormula(ReportVO reportVO, ParamVO paramVO) {
		// 如果使用了联查列，那么这里增加联查按钮
		LinkButtonVO[] btnVOs = getCorrelateButtonVOs(paramVO);
		if(addCorrelateQuery(null, paramVO) && btnVOs != null && btnVOs.length > 0) {
			String linkText = "";
			for(LinkButtonVO btnVO : btnVOs) {
				String linkUrl = null;
				if(StringUtils.isNotBlank(btnVO.getUrl())) {
					linkUrl = btnVO.getUrl();
				} else if(StringUtils.isNotBlank(btnVO.getFun_code())) {
					// 根据fun_code查询url
					String sql = "select class_name from nw_fun WITH(NOLOCK) where isnull(dr,0)=0 and fun_code=?";
					linkUrl = NWDao.getInstance().queryForObject(sql, String.class, btnVO.getFun_code());
					if(StringUtils.isBlank(linkUrl)) {
						throw new BusiException("功能节点[?]没有定义[请求的URL地址]！",btnVO.getFun_code());
					}
					logger.info("功能节点[" + btnVO.getFun_code() + "]定义的URL：" + linkUrl);
					if(linkUrl.indexOf("?") == -1) {
						linkUrl += "?";
					} else {
						linkUrl += "&";
					}
					linkUrl += "funCode=" + btnVO.getFun_code();
				}
				if(StringUtils.isBlank(linkUrl)) {
					throw new BusiException("联查按钮定义不正确，没有定义url或者fun_code！");
				}
				Map<String, Object> paramMap = btnVO.getParamMap();
				if(paramMap != null) {
					if(linkUrl.indexOf("?") == -1) {
						linkUrl += "?";
					} else {
						linkUrl += "&";
					}
					for(String key : paramMap.keySet()) {
						linkUrl += key + "={";
						linkUrl += paramMap.get(key);
						linkUrl += "}";
						linkUrl += "&";
					}
					linkUrl = linkUrl.substring(0, linkUrl.length() - 1);
					if(!linkUrl.toLowerCase().startsWith("http://")) {
						// 加上上下文
						linkUrl = ServletContextHolder.getRequest().getContextPath() + linkUrl;
					}
					logger.info("联查的URL：" + linkUrl);
				}
				linkText += "<a href=\"javascript:Utils.openNode('" + btnVO.getText() + "','" + linkUrl
						+ "')\" target='_blank'>" + btnVO.getText() + "</a>&nbsp;";
			}
			List<Map<String, Object>> list = reportVO.getPageVO().getItems();
			if(list != null) {
				for(Map<String, Object> map : list) {
					// 解析linkUrl中的参数
					linkText = VariableHelper.resolve(linkText, map);// 识别{key}变量
					map.put(CORRELATE_QUERY_KEY, linkText);
				}
			}
		}
	}

}

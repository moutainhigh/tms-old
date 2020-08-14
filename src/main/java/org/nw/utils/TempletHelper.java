package org.nw.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.Global;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ext.ref.userdefine.IComboxModel;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.PrintTempletVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiPrintTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.redis.RedisDao;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.DataTempletVO;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.PersonalProfileVO;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.vo.sys.ReportTempletVO;
import org.nw.vo.sys.RoleVO;
import org.nw.web.utils.WebUtils;

/**
 * 模板处理类
 * 
 * @author xuqc
 * @date 2011-6-30
 */
public class TempletHelper {
	static Logger logger = Logger.getLogger(TempletHelper.class.getName());
	private static ConcurrentHashMap<String, UiBillTempletVO> BILL_TEMPLET_CACHE_MAP = new ConcurrentHashMap<String, UiBillTempletVO>();
	private static ConcurrentHashMap<String, UiQueryTempletVO> QUERY_TEMPLET_CACHE_MAP = new ConcurrentHashMap<String, UiQueryTempletVO>();
	private static ConcurrentHashMap<String, UiPrintTempletVO> PRINT_TEMPLET_CACHE_MAP = new ConcurrentHashMap<String, UiPrintTempletVO>();
	private static ConcurrentHashMap<String, UiReportTempletVO> REPORT_TEMPLET_CACHE_MAP = new ConcurrentHashMap<String, UiReportTempletVO>();
	private static ConcurrentHashMap<String, DataTempletVO> DATA_TEMPLET_CACHE_MAP = new ConcurrentHashMap<String, DataTempletVO>();
	private static ConcurrentHashMap<String, PersonalProfileVO> PERSONAL_PROFILE_CACHE_MAP = new ConcurrentHashMap<String, PersonalProfileVO>();

	/**
	 * 是否识别默认值，单据模板编辑器中，读取的模板数据不需要识别默认值
	 * 
	 * @param templetId
	 * @param resolveValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static UiBillTempletVO getOriginalBillTempletVO(String templetId) {
		if(templetId == null) {
			throw new BusiException("模板ID不能为空！");
		}
		UiBillTempletVO templetVO = null;
		// 获取模板的ts
		String ts = NWDao.getInstance().queryForObject("select TS from NW_BILLTEMPLET WITH(NOLOCK) where PK_BILLTEMPLET=?",
				String.class, templetId);
		if(BILL_TEMPLET_CACHE_MAP.get(templetId) != null) {
			// 有缓存，判断是否失效
			UiBillTempletVO uiBillTempletVO = BILL_TEMPLET_CACHE_MAP.get(templetId);
			if(ts != null && ts.equals(uiBillTempletVO.getTs())) {
				// 缓存命中
				// templetVO = uiBillTempletVO.clone();
			} else {
				// 缓存失效，重新读取
			}
		}

		if(templetVO == null) {
			// 无缓存或缓存失效，直接读取，并放入缓存
			UiBillTempletVO vo = new UiBillTempletVO();
			vo.setTemplateID(templetId);
			vo.setTs(ts);
			List<BillTempletBVO> fieldVOs = getFieldVOsByTplId(templetId);

			vo.setFieldVOs(fieldVOs);
			vo.setTabVOs(getTabVOsByTplId(templetId));
			// 增加dr，ts字段,2013-5-26,没必要增加，之前是为了兼容nc的某些模板
			// addDrTsField(fieldVOs, vo.getTabVOs());
			if(WebUtils.getLoginInfo() != null) {// 有些地方可能还没登陆呢
				vo.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				vo.setPk_user(WebUtils.getLoginInfo().getPk_user());
			}
			// 加入表头和表尾的TabCode
			StringBuffer headerTabCode = new StringBuffer();
			StringBuffer bodyTabCode = new StringBuffer();
			List<BillTempletTVO> tabVos = vo.getTabVOs();
			for(BillTempletTVO tabVo : tabVos) {
				if(tabVo.getPos().intValue() == UiConstants.POS[1]) {
					// 表体的tabCode
					bodyTabCode.append(tabVo.getTabcode());// 区分大小写
					bodyTabCode.append(",");
				} else {
					if(tabVo.getPos().intValue() == UiConstants.POS[0]
							&& StringUtils.isBlank(vo.getFirstHeaderTabCode())) {
						// 只需要设置第一个
						vo.setFirstHeaderTabCode(tabVo.getTabcode());
					}
					// 表头和表尾的tabCode
					headerTabCode.append(tabVo.getTabcode());// 区分大小写
					headerTabCode.append(",");
				}
			}
			// 在前台tableFormLayout.js做了处理，这里不需要处理了
			// for(BillTempletBVO fieldVO : fieldVOs) {
			// if(fieldVO.getCardflag().intValue() == Constants.YES
			// && fieldVO.getShowflag().intValue() == Constants.YES) {
			// // FIXME xuqc 2013-3-22 大文本使用了label在top位置，不需要计入长度
			// if(DATATYPE.TEXTAREA.intValue() !=
			// fieldVO.getDatatype().intValue()) {
			// // 使用字节数进行比较会比较准确
			// // 这里要指定编码,避免在不同的系统中使用不同编码导致的问题
			// // 如，使用UTF-8编码，一个汉字使用3个字节存储，而使用gbk编码，则使用2个字节存储
			// try {
			// int len = fieldVO.getDefaultshowname().getBytes("UTF-8").length;
			// if(len > vo.getMaxDefaultshownameLength()) {
			// vo.setMaxDefaultshownameLength(len);
			// }
			// } catch(UnsupportedEncodingException e) {
			// e.printStackTrace();
			// }
			// }
			// }
			// }

			if(headerTabCode.length() > 0) {
				vo.setHeaderTabCode(headerTabCode.substring(0, headerTabCode.length() - 1));
			}
			if(bodyTabCode.length() > 0) {
				vo.setBodyTabCode(bodyTabCode.substring(0, bodyTabCode.length() - 1));
			}

			vo.setFmTemplate(Boolean.valueOf(Global.getPropertyValue("debug")) ? UiConstants.DEFAULT_BILL_TEMPLATE
					: UiConstants.DEFAULT_BILL_TEMPLATE_BIN);
			vo.setModuleName(UiConstants.DEFAULT_MODULE_NAME);

			// 放入缓存
			BILL_TEMPLET_CACHE_MAP.put(templetId, vo);
			templetVO = vo.clone();
		}

		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		/**
		 * 是否隐藏表体,如果表体的所有字段都是隐藏的,则为true
		 */
		boolean hideBodyGrid = true;
		// 实例化headerRefMap，headerDefaultValueMap
		for(BillTempletBVO fieldVO : fieldVOs) {
			String defaultValue = fieldVO.getDefaultvalue();
			if(StringUtils.isNotBlank(defaultValue)) {
				defaultValue = VariableHelper.resolve(fieldVO.getDefaultvalue(), null);
				if(fieldVO.getDatatype().intValue() == UiConstants.DATATYPE.SELECT.intValue()) {
					// 下拉框,FIXME 单据模板的下拉框在设置默认值时，存储的是text值，这里转换一下
					if(StringUtils.isNotBlank(defaultValue)) {
						Object obj = UiTempletUtils.getSelectValue(defaultValue, fieldVO.getReftype());
						if(obj != null) {
							defaultValue = obj.toString();
						}
					}
				}
			}
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getShowflag().equals(Constants.YES)) {
					hideBodyGrid = false;
				}

				// 处理表体的默认值
				// 这里需要区分tabcode,每个tabcode对应不同的表
				if(StringUtils.isNotBlank(defaultValue)) {
					Object oTabValuesMap = templetVO.getBodyDefaultValueMap().get(fieldVO.getTable_code().trim());
					if(oTabValuesMap == null) {
						// 创建该tab的数据Map对象
						oTabValuesMap = new HashMap<String, Object>();
						templetVO.getBodyDefaultValueMap().put(fieldVO.getTable_code().trim(), oTabValuesMap);
					}
					Map<String, Object> tabValuesMap = (Map<String, Object>) oTabValuesMap;
					tabValuesMap.put(fieldVO.getItemkey(), defaultValue);
				}
			} else {
				// 处理表头和表尾的默认值
				// 这里也不需要区分tabcode，因为目前规定：表头和表尾中不同的tab不能有相同的字段，不能是多张表的字段。
				if(StringUtils.isNotBlank(defaultValue)) {
					templetVO.getHeaderDefaultValueMap().put(fieldVO.getItemkey().trim(), defaultValue);
				}
				// 注意这里如果参照是隐藏的情况下,此时渲染到页面上是hidden的
				if(DATATYPE.REF.equals(fieldVO.getDatatype())
						&& fieldVO.getShowflag().intValue() == Constants.YES.intValue()) {
					// 参照，但是需要排除一些自定义参照，如多选框
					templetVO.getHeaderRefMap().put(fieldVO.getItemkey(), fieldVO);
				}
				// 列表下统计的列
				if(fieldVO.getTotalflag().intValue() == Constants.YES.intValue()
						&& fieldVO.getListshowflag().booleanValue()) {
					// 如果存在关键字，那么使用这个字段来统计
					templetVO.getHeaderSummaryFieldAry().add(
							new String[] {
									fieldVO.getIdcolname() == null ? fieldVO.getItemkey() : fieldVO.getIdcolname(),
									fieldVO.getItemkey() });
				}
			}
		}
		templetVO.setHideBodyGrid(hideBodyGrid);
		return templetVO;
	}

	/**
	 * 返回原始 的查询模板信息，没有经过转换器的模板信息
	 * 
	 * @param templetId
	 * @return
	 */
	public static UiQueryTempletVO getOriginalQueryTempletVO(String templetId) {
		if(templetId == null) {
			// 可能没有配置查询模板
			// throw new RuntimeException("templetId不能为空！");
			return null;
		}

		UiQueryTempletVO templetVO = null;

		// 获取模板的ts
		String ts = NWDao.getInstance().queryForObject("select ts from NW_QUERY_TEMPLET WITH(NOLOCK) where pk_templet=?", String.class,
				templetId);
		if(QUERY_TEMPLET_CACHE_MAP.get(templetId) != null) {
			// 有缓存，判断是否失效
			UiQueryTempletVO uiQueryTempletVO = QUERY_TEMPLET_CACHE_MAP.get(templetId);
			if(ts != null && ts.equals(uiQueryTempletVO.getTs())) {
				// 缓存命中
				templetVO = uiQueryTempletVO.clone();
			} else {
				// 缓存失效，重新读取
			}
		}

		if(templetVO == null) {
			// 无缓存或缓存失效，直接读取，并放入缓存
			UiQueryTempletVO uiQueryTempletVO = new UiQueryTempletVO();
			uiQueryTempletVO.setTemplateID(templetId);
			uiQueryTempletVO.setTs(ts);
			List<QueryConditionVO> condVOs = selectUsedCondVOsByTplId(templetId);
			uiQueryTempletVO.setConditions(condVOs);
			// 放入缓存
			QUERY_TEMPLET_CACHE_MAP.put(templetId, uiQueryTempletVO);
			templetVO = uiQueryTempletVO.clone();
		}
		List<QueryConditionVO> condVOs = templetVO.getConditions();
		// 处理默认值
		for(QueryConditionVO condVO : condVOs) {
			if(StringUtils.isNotBlank(condVO.getValue())) {// 识别默认值中的变量
				condVO.setValue(VariableHelper.resolve(condVO.getValue(), null));
			}
		}
		return templetVO;
	}

	/**
	 * 根据模板id返回模板配置信息
	 * 
	 * @param templetId
	 * @return
	 */
	public static List<BillTempletBVO> getFieldVOsByTplId(String templetId) {
		// 加入distinct,表头的页签与表尾的页签名称可能相同
		NWDao dao = NWDao.getInstance();
		String sql = "SELECT DISTINCT b.*,c.TABINDEX FROM NW_BILLTEMPLET_B b  WITH(NOLOCK) LEFT JOIN NW_BILLTEMPLET_T c WITH(NOLOCK) ON b.TABLE_CODE=c.TABCODE "
				+ " WHERE b.DR=0 and (b.cardflag=1 or b.listflag=1) and b.PK_BILLTEMPLET=? AND b.PK_BILLTEMPLET=c.PK_BILLTEMPLET "
				+ "order by b.POS,c.TABINDEX,b.SHOWORDER";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "SELECT DISTINCT b.*,c.TABINDEX FROM NW_BILLTEMPLET_B b  LEFT JOIN NW_BILLTEMPLET_T c ON b.TABLE_CODE=c.TABCODE "
					+ " WHERE b.DR=0 and (b.cardflag=1 or b.listflag=1) and b.PK_BILLTEMPLET=? AND b.PK_BILLTEMPLET=c.PK_BILLTEMPLET "
					+ "order by b.POS,c.TABINDEX,b.SHOWORDER";
		}
		// 排序：先根据位置、再根据页签、最后是显示顺序
		// 这里不加缓存了，在调用函数里面统一加了缓存
		return dao.queryForList(sql, BillTempletBVO.class, templetId);
	}

	/**
	 * 根据模板id查询所有tabVo
	 * 
	 * @param templetId
	 *            模板id
	 * @return
	 */
	public static List<BillTempletTVO> getTabVOsByTplId(String templetId) {
		NWDao dao = NWDao.getInstance();
		String sql = "select * from nw_billtemplet_t WITH(NOLOCK) where dr=0 and pk_billtemplet=? order by tabindex asc";
		return dao.queryForList(sql, BillTempletTVO.class, templetId);
	}

	/**
	 * 根据模板id查询【查询模板】的配置信息,只返回已使用的配置信息<br/>
	 * 排序：是否锁定、是否必输项、排序号
	 * 
	 * @param templetId
	 *            查询模板id
	 * @return
	 */
	public static List<QueryConditionVO> selectUsedCondVOsByTplId(String templetId) {
		NWDao dao = NWDao.getInstance();
		String sql = "select * from nw_query_condition q WITH(NOLOCK) where isnull(dr,0)=0 and q.if_used='Y' and q.pk_templet=?  order by q.if_immobility desc,q.if_must desc,q.disp_sequence";
		return dao.queryForList(sql, QueryConditionVO.class, templetId);
	}

	/**
	 * 根据模板id查询【查询模板】的配置信息，返回所有配置信息，包括已使用和未使用
	 * 
	 * @param templetId
	 *            查询模板id
	 * @return
	 */
	public static List<QueryConditionVO> selectCondVOsByTplId(String templetId) {
		NWDao dao = NWDao.getInstance();
		String sql = "select * from nw_query_condition q WITH(NOLOCK) where isnull(dr,0)=0 and q.pk_templet=? order by q.if_immobility desc,q.if_must desc,q.disp_sequence";
		return dao.queryForList(sql, QueryConditionVO.class, templetId);
	}

	/**
	 * 获取打印模板VO
	 * 
	 * @param templetId
	 * @return
	 */
	public static UiPrintTempletVO getOriginalPrintTempletVO(String templetId) {
		// 获取模板的ts
		String ts = NWDao.getInstance().queryForObject("select ts from nw_print_templet WITH(NOLOCK) where pk_print_templet=?",
				String.class, templetId);

		if(templetId == null) {
			throw new BusiException("模板主键不能为空,可能没有分配模板！");
		}

		if(PRINT_TEMPLET_CACHE_MAP.get(templetId) != null) {
			// 有缓存，判断是否失效
			UiPrintTempletVO uiPrintTempletVO = PRINT_TEMPLET_CACHE_MAP.get(templetId);
			if(ts != null && ts.equals(uiPrintTempletVO.getTs())) {
				// 缓存命中
				return uiPrintTempletVO.clone();
			} else {
				// 缓存失效，重新读取
			}
		}

		UiPrintTempletVO uiPrintTempletVO = new UiPrintTempletVO();
		uiPrintTempletVO.setPrintTempletId(templetId);
		uiPrintTempletVO.setTs(ts);

		String sql = "select pk_print_templet,dr,ts,pk_corp,nodecode,vtemplatename,head_formula,body_formula,file_name,file_size,memo,create_user,create_time,datasourcesql "
				+ "from nw_print_templet WITH(NOLOCK) where pk_print_templet=? and isnull(dr,0)=0";
		PrintTempletVO printTemplateVO = NWDao.getInstance().queryForObject(sql, PrintTempletVO.class, templetId);
		uiPrintTempletVO.setPrintTempletVO(printTemplateVO);
		// 放入缓存
		PRINT_TEMPLET_CACHE_MAP.put(templetId, uiPrintTempletVO);
		return uiPrintTempletVO.clone();
	}

	/**
	 * 加载模板数据
	 * 
	 * @param pk_print_templet
	 * @param templetVO
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public static InputStream getPrintTempletData(String pk_print_templet) {
		String sql = "select contentdata from nw_print_templet WITH(NOLOCK) where pk_print_templet=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_print_templet);
		if(retMap == null) {
			logger.warn("文件已经不存在,pk_print_templet=" + pk_print_templet);
			throw new BusiException("您请求的模板文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			return (InputStream) contentdata;
		}
		return null;
	}

	/**
	 * 获取报表模板VO
	 * 
	 * @param templetId
	 * @return
	 */
	public static UiReportTempletVO getOriginalReportTempletVO(String templetId) {
		// 获取模板的ts
		String ts = NWDao.getInstance().queryForObject("select ts from nw_report_templet WITH(NOLOCK) where pk_report_templet=?",
				String.class, templetId);

		if(templetId == null) {
			throw new BusiException("templetId不能为空！");
		}

		if(REPORT_TEMPLET_CACHE_MAP.get(templetId) != null) {
			// 有缓存，判断是否失效
			UiReportTempletVO uiReportTempletVO = REPORT_TEMPLET_CACHE_MAP.get(templetId);
			if(ts != null && ts.equals(uiReportTempletVO.getTs())) {
				// 缓存命中
				return uiReportTempletVO.clone();
			} else {
				// 缓存失效，重新读取
			}
		}

		UiReportTempletVO uiReportTempletVO = new UiReportTempletVO();
		uiReportTempletVO.setReportTempletId(templetId);
		uiReportTempletVO.setTs(ts);
		uiReportTempletVO
				.setFmTemplate(Boolean.valueOf(Global.getPropertyValue("debug")) ? UiConstants.DEFAULT_REPORT_TEMPLATE
						: UiConstants.DEFAULT_REPORT_TEMPLATE_BIN);
		uiReportTempletVO.setModuleName(UiConstants.DEFAULT_MODULE_NAME);

		String sql = "select * from nw_report_templet WITH(NOLOCK) where pk_report_templet=? and isnull(dr,0)=0";
		ReportTempletVO reportTemplateVO = NWDao.getInstance().queryForObject(sql, ReportTempletVO.class, templetId);
		if(reportTemplateVO == null) {
			throw new BusiException("报表模板已经不存在，可能已经被删除，templetID[?]！",templetId);
		}
		uiReportTempletVO.setReportTempletVO(reportTemplateVO);
		uiReportTempletVO.setFieldVOs(getReportTempletData(templetId));
		for(ReportTempletBVO fieldVO : uiReportTempletVO.getFieldVOs()) {
			// 列表下统计的列
			if(fieldVO.getTotal_flag() != null && fieldVO.getTotal_flag().booleanValue()) {
				// 如果存在关键字，那么使用这个字段来统计
				// uiReportTempletVO.getHeaderSummaryFieldAry().add(new String[]
				// { fieldVO.getItemkey() });
				// 如果存在关键字，那么使用这个字段来统计
				uiReportTempletVO.getHeaderSummaryFieldAry().add(
						new String[] { fieldVO.getIdcolname() == null ? fieldVO.getItemkey() : fieldVO.getIdcolname(),
								fieldVO.getItemkey() });
			}
		}
		// 放入缓存
		REPORT_TEMPLET_CACHE_MAP.put(templetId, uiReportTempletVO);
		return uiReportTempletVO.clone();
	}
	
	/**
	 * 获取数据模板VO
	 * 
	 * @param templetId
	 * @return
	 */
	public static DataTempletVO getDataTempletVO(String templetId) {
		if(templetId == null) {
			throw new BusiException("templetId不能为空！");
		}
		DataTempletVO dataTempletVO = NWDao.getInstance().queryByCondition(DataTempletVO.class, 
				"pk_datatemplet=?", templetId);
		return dataTempletVO;
	}

	/**
	 * 返回报表模板的详细数据
	 * 
	 * @param templetId
	 * @return
	 */
	private static List<ReportTempletBVO> getReportTempletData(String templetId) {
		String sql = "select * from nw_report_templet_b WITH(NOLOCK) where pk_report_templet=? and isnull(dr,0)=0 order by display_order";
		NWDao dao = NWDao.getInstance();
		return dao.queryForList(sql, ReportTempletBVO.class, templetId);
	}

	/**
	 * 增加dr，ts字段,fieldVOs中可能已经有dr，ts字段，但是这不影响
	 * 
	 * @param fieldVOs
	 */
	public static void addDrTsField(List<BillTempletBVO> fieldVOs, List<BillTempletTVO> tabVOs) {
		if(fieldVOs == null || fieldVOs.size() == 0) {
			return;
		}
		if(tabVOs == null || tabVOs.size() == 0) {
			return;
		}
		int count = 0;
		for(BillTempletTVO tabVO : tabVOs) {
			if(tabVO.getPos().intValue() == UiConstants.POS[2]) {
				// 表尾
				continue;
			}
			if(tabVO.getPos().intValue() == UiConstants.POS[1] || count == 0) {
				// 表体的情况，或者第一次执行表头的情况
				addDrTsField(fieldVOs, tabVO.getPos(), tabVO.getTabcode());
			}
			if(tabVO.getPos().intValue() == UiConstants.POS[0]) {
				// 表头
				count = 1;
			}
		}
	}

	private static void addDrTsField(List<BillTempletBVO> fieldVOs, Integer pos, String table_code) {
		BillTempletBVO fieldVO = fieldVOs.get(0);
		BillTempletBVO dr = new BillTempletBVO();
		dr.setItemkey("dr");
		// 在将nc的参照转换为webnc参照时，会根据defaultshowname进行比较，这里设置一个值，以免出现空指针异常，而且一般都需要有这个值
		dr.setDefaultshowname("dr");
		dr.setDatatype(UiConstants.DATATYPE.INTEGER.intValue());
		dr.setListflag(Constants.YES);
		dr.setCardflag(Constants.YES);
		dr.setListshowflag(new UFBoolean(false));
		dr.setShowflag(Constants.NO);
		dr.setEditflag(Constants.NO);
		dr.setLockflag(Constants.NO);
		dr.setReviseflag(new UFBoolean(false));
		dr.setTotalflag(Constants.NO);
		dr.setNullflag(Constants.NO);
		dr.setWidth(0);
		dr.setDr(0);
		dr.setPos(pos);
		dr.setTable_code(table_code);
		dr.setPk_billtemplet(fieldVO.getPk_billtemplet());
		dr.setPk_billtemplet_b(UUID.randomUUID().toString());
		fieldVOs.add(dr);

		BillTempletBVO ts = new BillTempletBVO();
		ts.setItemkey("ts");
		ts.setDefaultshowname("ts");
		ts.setDatatype(UiConstants.DATATYPE.TEXT.intValue());
		ts.setListflag(Constants.YES);
		ts.setCardflag(Constants.YES);
		ts.setListshowflag(new UFBoolean(false));
		ts.setShowflag(Constants.NO);
		ts.setEditflag(Constants.NO);
		ts.setLockflag(Constants.NO);
		ts.setReviseflag(new UFBoolean(false));
		ts.setTotalflag(Constants.NO);
		ts.setNullflag(Constants.NO);
		ts.setWidth(0);
		ts.setDr(0);
		ts.setPos(pos);
		ts.setTable_code(table_code);
		ts.setPk_billtemplet(fieldVO.getPk_billtemplet());
		ts.setPk_billtemplet_b(UUID.randomUUID().toString());
		fieldVOs.add(ts);
	}

	/**
	 * 返回单据模板ID
	 * 
	 * @param funCode
	 *            节点编码
	 * @param tempstyle
	 *            模板类型，如单据，查询，见UiConstants.TPL_STYLE
	 * @return
	 */
	public static String getTempletID(String funCode, String nodeKey, int tempstyle) {
		if(StringUtils.isBlank(funCode)) {
			return null;
		}
		//从redis里获取功能按钮信息
		FunVO funVO = RedisDao.getInstance().getFunVO("fun_code", funCode);
//		FunVO funVO = NWDao.getInstance().queryForObject(
//				"select pk_fun from nw_fun WITH(NOLOCK) where isnull(dr,0)=0 and fun_code=?", FunVO.class, funCode);
		
		// 返回当前登录用户的所有角色
		List<RoleVO> roleVOs = NWDao.getInstance().queryForList(
				"select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 "
						+ "and pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?)",
				RoleVO.class, WebUtils.getLoginInfo() == null ? "0001" : WebUtils.getLoginInfo().getPk_user());
		if(roleVOs == null || roleVOs.size() == 0) {
			logger.info("用户：" + (WebUtils.getLoginInfo() == null ? "administrator" : WebUtils.getLoginInfo().getUser_code()) + "没有分配角色，使用系统默认的模板！");
		}
		StringBuffer sRoleBuf = new StringBuffer();
		for(RoleVO roleVO : roleVOs) {
			sRoleBuf.append("'");
			sRoleBuf.append(roleVO.getPk_role());
			sRoleBuf.append("',");
		}
		String sRole = "";
		if(sRoleBuf.length() > 0) {
			sRole = sRoleBuf.subSequence(0, sRoleBuf.length() - 1).toString();
		}
		// 根据pk_fun和sRole查询当前已经分配的模板
		String baseSql = "select pk_templet from nw_templet_dist WITH(NOLOCK) where isnull(dr,0)=0 and tempstyle=? and pk_corp=? and pk_fun=?";
		if(StringUtils.isNotBlank(nodeKey)) {
			baseSql += " and nodekey='" + nodeKey + "'";
		} else {
			baseSql += " and (nodekey is null or nodekey='')";
		}
		String sql = baseSql;
		if(sRole.length() > 0) {
			sql += " and pk_role in (" + sRole + ")";
		}
		// 这里可能因为一个用户包含了多个角色，而每个角色都分配了模板，此时只取第一张模板
		List<String> templetIDs = NWDao.getInstance().queryForList(sql, String.class, tempstyle,
				WebUtils.getLoginInfo() == null ? Constants.SYSTEM_CODE : WebUtils.getLoginInfo().getPk_corp(), funVO.getPk_fun());
		if(templetIDs == null || templetIDs.size() == 0) {
			// 如果不存在，则读取集团分配的模板
			templetIDs = NWDao.getInstance().queryForList(sql, String.class, tempstyle, Constants.SYSTEM_CODE,
					funVO.getPk_fun());
		}
		if(templetIDs != null && templetIDs.size() > 0) {
			return templetIDs.get(0);
		}
		return null;
	}

	/**
	 * 返回用户个性化信息
	 * 
	 * @param pk_template
	 * @param node_code
	 * @return
	 * @author xuqc
	 * @date 2012-4-19
	 */
	public static PersonalProfileVO getPersonalProfileVO(String node_code, String pk_template, Integer templateStyle) {
		String cacheKey = new StringBuffer(WebUtils.getLoginInfo().getPk_corp())
				.append(WebUtils.getLoginInfo().getPk_user()).append(pk_template).append(templateStyle)
				.append(node_code).toString();
		// 获取模板的ts
		String ts = NWDao.getInstance().queryForObject(
				"select TS from nw_personal_profile WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? and pk_user=? "
						+ "and node_code=? and pk_template=? and templatestyle=? ", String.class,
				WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo().getPk_user(), node_code, pk_template,
				templateStyle);
		if(PERSONAL_PROFILE_CACHE_MAP.get(cacheKey) != null) {
			// 有缓存，判断是否失效
			PersonalProfileVO profileVO = PERSONAL_PROFILE_CACHE_MAP.get(cacheKey);
			if(ts != null && ts.equals(profileVO.getTs())) {
				// 缓存命中
				return profileVO;
			}
		}

		String sql = "select * from nw_personal_profile "
				+ " WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? and pk_user=? and node_code=? and pk_template=? and templatestyle=?";
		PersonalProfileVO profileVO = NWDao.getInstance().queryForObject(sql, PersonalProfileVO.class,
				WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo().getPk_user(), node_code, pk_template,
				templateStyle);
		if(profileVO != null) {
			// 加入缓存
			PERSONAL_PROFILE_CACHE_MAP.put(cacheKey, profileVO);
		}
		return profileVO;
	}

	/**
	 * 参照转成下拉，针对单据模板<br/>
	 * 这个方法是静态方法，其他方法需要相应的配置信息，该方法不需要
	 * 
	 * @param fieldVO
	 * @return
	 * @author xuqc
	 * @date 2012-9-26
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static void RefModel2Combox(BillTempletBVO fieldVO) {
		String reftype = fieldVO.getReftype();
		String refClazz = DataTypeConverter.getRefClazz(reftype, UiConstants.DATATYPE.REF.intValue());
		try {
			if(StringUtils.isNotBlank(refClazz)) {
				Class clazz = Class.forName(refClazz);
				Object obj = clazz.newInstance();
				if(obj instanceof IComboxModel) {
					IComboxModel model = (IComboxModel) obj;
					List<String[]> ds = model.load4Combox();
					if(ds != null) {
						StringBuffer buf = new StringBuffer();
						buf.append(IComboxModel.DEFAULT_COMBOX_TYPE);
						for(String[] option : ds) {
							buf.append(option[1]); // 注意这里的格式是text=value
							buf.append("=");
							buf.append(option[0]);
							buf.append(",");
						}
						reftype = buf.substring(0, buf.length() - 1);
						fieldVO.setDatatype(UiConstants.DATATYPE.SELECT.intValue());
						fieldVO.setReftype(reftype);
					} else {
						logger.warn("单据模板的参照在转换成下拉时：没有加载到下拉框的数据源！");
					}
				} else {
					logger.warn("单据模板的参照在转换成下拉时：参照类没有实现IComboxModel接口！");
				}
			}
		} catch(Exception e) {
			logger.error("单据模板的参照在转换成下拉时出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 参照转下拉，针对查询模板<br/>
	 * 这个方法是静态方法，其他方法需要相应的配置信息，该方法不需要
	 * 
	 * @param condVO
	 * @return
	 * @author xuqc
	 * @date 2012-9-26
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static void RefModel2Combox(QueryConditionVO condVO) {
		String reftype = condVO.getConsult_code();
		String refClazz = DataTypeConverter.getRefClazz(reftype, UiConstants.DATATYPE.REF.intValue());
		try {
			if(StringUtils.isNotBlank(refClazz)) {
				Class clazz = Class.forName(refClazz);
				Object obj = clazz.newInstance();
				if(obj instanceof IComboxModel) {
					IComboxModel model = (IComboxModel) obj;
					List<String[]> ds = model.load4Combox();
					if(ds != null) {
						StringBuffer buf = new StringBuffer();
						buf.append(IComboxModel.DEFAULT_COMBOX_TYPE);
						for(String[] option : ds) {
							buf.append(option[1]);// 注意这里的格式是text=value
							buf.append("=");
							buf.append(option[0]);
							buf.append(",");
						}
						reftype = buf.substring(0, buf.length() - 1);
						condVO.setData_type(UiConstants.DATATYPE.SELECT.intValue());
						condVO.setConsult_code(reftype);
					} else {
						logger.warn("查询模板的参照在转换成下拉时：没有加载到下拉框的数据源！");
					}
				} else {
					logger.warn("查询模板的参照在转换成下拉时：参照类没有实现IComboxModel接口！");
				}
			}
		} catch(Exception e) {
			logger.error("查询模板的参照在转换成下拉时出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 将单据模板配置信息转成查询模板的配置信息
	 * 
	 * @param fieldVO
	 * @return
	 */
	public static QueryConditionVO convert(BillTempletBVO fieldVO) {
		if(fieldVO == null) {
			return null;
		}
		QueryConditionVO condVO = new QueryConditionVO();
		if(StringUtils.isNotBlank(fieldVO.getTable_code())) {
			condVO.setField_code(fieldVO.getTable_code() + "." + fieldVO.getItemkey());
		} else {
			condVO.setField_code(fieldVO.getItemkey());
		}
		condVO.setField_name(fieldVO.getDefaultshowname());
		condVO.setOpera_code("=@>@>=@<@<=@like@");
		condVO.setOpera_name("等于@大于@大于等于@小于@小于等于@相似@");
		condVO.setData_type(fieldVO.getDatatype());
		if(StringUtils.isBlank(fieldVO.getReftype())) {
			condVO.setConsult_code("-99");
		} else {
			condVO.setConsult_code(fieldVO.getReftype());
		}
		condVO.setIf_default(fieldVO.getListshowflag());
		condVO.setIf_used(UFBoolean.TRUE);
		condVO.setIf_desc(UFBoolean.FALSE);
		if(fieldVO.getNullflag() != null && fieldVO.getNullflag().intValue() == 1) {
			condVO.setIf_must(UFBoolean.TRUE);
		} else {
			condVO.setIf_must(UFBoolean.FALSE);
		}
		condVO.setIf_immobility(UFBoolean.FALSE);
		// condVO.setValue(fieldVO.getDefaultvalue());
		if(fieldVO.getDatatype().intValue() == UiConstants.DATATYPE.REF.intValue()) {
			// 参照
			condVO.setReturn_type(2);
		} else {
			condVO.setReturn_type(0);
		}
		condVO.setDisp_sequence(0);
		return condVO;
	}

	/**
	 * 将单据模板配置信息转成报表模板的配置信息
	 * 
	 * @param fieldVO
	 * @return
	 */
	public static ReportTempletBVO convertRtbVO(BillTempletBVO fieldVO) {
		if(fieldVO == null) {
			return null;
		}
		ReportTempletBVO rtbVO = new ReportTempletBVO();
		rtbVO.setItemkey(fieldVO.getItemkey());
		rtbVO.setDefaultshowname(fieldVO.getDefaultshowname());
		rtbVO.setData_type(fieldVO.getDatatype());
		rtbVO.setReftype(fieldVO.getReftype());
		rtbVO.setWidth(fieldVO.getWidth() * 100);
		rtbVO.setTotal_flag(new UFBoolean(1 == fieldVO.getTotalflag()));
		rtbVO.setLoadformula(fieldVO.getLoadformula());
		rtbVO.setDisplay_order(fieldVO.getShoworder());
		rtbVO.setOptions(fieldVO.getOptions());
		rtbVO.setIdcolname(fieldVO.getIdcolname());
		rtbVO.setShow_flag(new UFBoolean(1 == fieldVO.getShowflag()));
		return rtbVO;
	}
}

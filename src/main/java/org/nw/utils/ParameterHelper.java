package org.nw.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ParameterVO;
import org.nw.web.utils.WebUtils;

/**
 * 参数工具类，用来操作nw_parameter表
 * <p>
 * 因为系统的参数一般不常改动，这里做了一次缓存,以参数名称和公司作为key
 * </p>
 * 
 * @author xuqc
 * @date 2012-7-3 下午03:04:11
 */
public class ParameterHelper {

	static Logger logger = Logger.getLogger(ParameterHelper.class);

	public static final String PAGE_SIZE_PARAM = "pageSize";
	public static final String CURRENCY_PARAM = "currency"; // 币种
	public static final String SYSTEM_LOG_PARAM = "systemLog";// 是否记录操作日志
	public static final String PRECISION = "precision";// 精度
	public static final String PAY_DEVI_TYPE = "PAY_DEVI_TYPE";// 分摊类型
	public static final String PAY_DEVI_DIMENSION = "pay_devi_dimension";// 分摊维度
	public static final String PLAN_REFRESH_PERIOD = "PLAN_REFRESH_PERIOD";// 运输计划刷新的周期，单位：分钟
	public static final String ENTRUST_AUTO_CONFIRM = "entrust_auto_confirm";// 配载时生成委托单，委托单是否自动确认
	public static final String AUTO_GEN_INSURANCE = "auto_gen_insurance";// 发货单费用明细是否自动生成对应类型的保险费
	public static final String PLPZ_MATCH_CONTRACT = "plpz_match_contract";// 批量配载是否匹配合同规则
	//yaojiie 2015 12 02 增加发货单导入是否按照路线拆分规则
	public static final String INVOICE_IMPOTER_MATCH_LINE = "invoice_impoter_match_line";// 增加发货单导入是否按照路线拆分规则
	public static final String INVOICE_IMPOTER_MERGE = "invoice_impoter_merge";// 增加发货单导入是否合并行
	
	//yaojiie 2015 1 20  增加发货单导入是否按照路线拆分规则
	public static final String MILKRUN_NODE_ACCESS_RULE = "milk_node_access_rule";// 增加发货单导入是否按照路线拆分规则
	public static final String MILKRUN_LIMIT = "milkRun_limit";// 是否允许非milkRun单据进行milkRun操作规则
	public static final String MERGE_SAME_INVOICE = "merge_same_invoice";// 是否保留修改记录
	public static final String SAVE_CHANGE_RECORD = "save_change_record";// 是否保留修改记录
	// 以参数名称和公司作为key
	private static ConcurrentHashMap<String, String> PARAM_CACHE_MAP = new ConcurrentHashMap<String, String>();
	public static final String SIMPLEUNCONFIRM = "simpleUnConfirm";//反确认时是否是简单的反确认
	
	public static final String REFRESH_INTERVAL = "refresh_interval";// 增加发货单导入是否按照路线拆分规则
	
	public static final String EXPRESS_COOPERATION_UNIT = "express_cooperation_unit";//快递合作查询单位
	
	public static final String UPDATE_SEGMENT = "update_segment";// 修改委托单是否重新生成运段
	
	public static final String SAVE_TRACKING_LIMIT = "save_tracking_limit";//保存跟踪状态的控制
	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static String getRefreshInterval() {
		String interval = getParamValue(REFRESH_INTERVAL, "");
		if(StringUtils.isBlank(interval)) {
			logger.info("系统参数中必须定义刷新时间规则，参数名称：" + REFRESH_INTERVAL);
		}
		return interval;
	}
	
	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static String getMilkRunNodeAccessRule() {
		String currency = getParamValue(MILKRUN_NODE_ACCESS_RULE, "");
		if(StringUtils.isBlank(currency)) {
			logger.info("系统参数中必须定义milkrun节点获取规则，参数名称：" + MILKRUN_NODE_ACCESS_RULE);
		}
		return currency;
	}
	
	public static String getSaveTrackingLimit() {
		String limit = getParamValue(SAVE_TRACKING_LIMIT, "");
		if(StringUtils.isBlank(limit)) {
			logger.info("系统参数中必须定义保存跟踪状态的规则，参数名称：" + SAVE_TRACKING_LIMIT);
		}
		return limit;
	}
	
	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static String getExpressCooperationUnit() {
		String expressUnit = getParamValue(EXPRESS_COOPERATION_UNIT, "");
		if(StringUtils.isBlank(expressUnit)) {
			logger.info("系统参数没有定义快递查询的合作单位，参数名称：" + EXPRESS_COOPERATION_UNIT);
		}
		return expressUnit;
	}
	

	/**
	 * 读取当前登录公司的所有配置参数
	 * 
	 * @return
	 */
	private static List<ParameterVO> getParamVOs(String pk_corp) {
		NWDao dao = NWDao.getInstance();
		String sql = "select * from nw_parameter WITH(NOLOCK) where isnull(dr,0)=0";
		if(pk_corp.equals(Constants.SYSTEM_CODE)) {
			sql += " and (pk_corp is null or pk_corp=?)";
		} else {
			sql += " and pk_corp=?";
		}
		return dao.queryForList(sql, ParameterVO.class, pk_corp);
	}

	private static String getCacheKey(String param_code) {
		if(WebUtils.getLoginInfo() != null) {
			param_code += WebUtils.getLoginInfo().getPk_corp();
		}
		return param_code;
	}

	/**
	 * 根据参数key返回参数值，如果公司存在参数则优先读取公司参数，否则读取集团参数
	 * 
	 * @param param_key
	 * @param defaultValue
	 * @return
	 */
	public static String getParamValue(String param_key, String defaultValue) {
		String value = PARAM_CACHE_MAP.get(getCacheKey(param_key));
		if(StringUtils.isBlank(value)) {
			String pk_corp = Constants.SYSTEM_CODE;
			if(WebUtils.getLoginInfo() != null) {
				pk_corp = WebUtils.getLoginInfo().getPk_corp();
			}
			List<ParameterVO> paramVOs = getParamVOs(pk_corp);
			ParameterVO target = null;
			for(ParameterVO paramVO : paramVOs) {
				if(paramVO.getParam_name().equals(param_key)) {
					target = paramVO;
					break;
				}
			}
			if(target == null) {
				// 当前公司没有定义该参数，从集团参数中读取
				paramVOs = getParamVOs(Constants.SYSTEM_CODE);
				for(ParameterVO paramVO : paramVOs) {
					if(paramVO.getParam_name().equals(param_key)) {
						target = paramVO;
						break;
					}
				}
			}
			if(target == null) {
				// 压根没有定义该参数,返回默认
				if(defaultValue == null) {
					throw new BusiException("系统没有定义名称为[?]的参数，请联系管理员！",param_key);
				} else {
					value = defaultValue;
				}
			} else {
				value = target.getParam_value();
			}
			PARAM_CACHE_MAP.put(getCacheKey(param_key), value);
		}
		return VariableHelper.resolve(value);
	}

	/**
	 * 根据参数key返回参数值，如果公司存在参数则优先读取公司参数，否则读取集团参数
	 * 
	 * @param param_key
	 * @return
	 */
	public static String getParamValue(String param_key) {
		return getParamValue(param_key, null);
	}

	/**
	 * 根据参数key返回参数值,转换成int型
	 * 
	 * @param param_key
	 * @return
	 */
	public static int getIntParam(String param_key) {
		String value = getParamValue(param_key);
		if(StringUtils.isBlank(value)) {
			return 0;
		}
		return Integer.parseInt(value.trim());
	}

	/**
	 * 根据参数key返回参数值,转换成double型
	 * 
	 * @param param_key
	 * @return
	 */
	public static double getDoubleParam(String param_key) {
		String value = getParamValue(param_key);
		if(StringUtils.isBlank(value)) {
			return 0;
		}
		return Double.parseDouble(value.trim());
	}

	/**
	 * 根据参数key返回参数值,转换成boolean型
	 * 
	 * @param param_key
	 * @return
	 */
	public static boolean getBooleanParam(String param_key) {
		String value = getParamValue(param_key);
		if(StringUtils.isBlank(value)) {
			return false;
		}
		return UFBoolean.valueOf(value.trim()).booleanValue();
	}

	/**
	 * 读取参数配置中的每页记录数
	 * 
	 * @return
	 */
	public static int getPageSize() {
		String value = getParamValue(PAGE_SIZE_PARAM, String.valueOf(Constants.DEFAULT_PAGESIZE));
		return Integer.parseInt(value);
	}

	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static String getCurrency() {
		String currency = getParamValue(CURRENCY_PARAM, "");
		if(StringUtils.isBlank(currency)) {
			logger.info("系统参数中必须定义默认的币种，参数名称：" + CURRENCY_PARAM);
		}
		return currency;
	}

	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static boolean getSystemLog() {
		String value = getParamValue(SYSTEM_LOG_PARAM, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}

	/**
	 * 读取默认精度
	 * 
	 * @return
	 */
	public static int getPrecision() {
		String value = getParamValue(PRECISION, String.valueOf(UiConstants.DEFAULT_PRECISION));
		return Integer.parseInt(value);
	}

	/**
	 * 应付明细的分摊类型
	 * 
	 * @return
	 */
	public static String getPayDeviType() {
		String payDeviType = getParamValue(PAY_DEVI_TYPE, "");
		if(StringUtils.isBlank(payDeviType)) {
			throw new BusiException("系统参数中必须定义默认的分摊类型，参数名称[?]！",PAY_DEVI_TYPE);
		}
		return payDeviType;
	}

	/**
	 * 应付明细的分摊维度
	 * 
	 * @return
	 */
	public static String getPayDeviDimension() {
		String payDeviDimension = getParamValue(PAY_DEVI_DIMENSION, "");
		if(StringUtils.isBlank(payDeviDimension)) {
			throw new BusiException("系统参数中必须定义默认的分摊维度，参数名称[?]！",PAY_DEVI_DIMENSION);
		}
		return payDeviDimension;
	}

	/**
	 * 运输计划自动刷新的时间
	 * 
	 * @return
	 */
	public static int getPlanRefreshPeriod() {
		String value = getParamValue(PLAN_REFRESH_PERIOD, String.valueOf(10));
		return Integer.parseInt(value) * 60 * 1000;
	}

	/**
	 * 配载时委托单是否自动确认
	 * 
	 * @return
	 */
	public static boolean getEntrustAutoConfirm() {
		String value = getParamValue(ENTRUST_AUTO_CONFIRM, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	
	/**
	 * 批量配载是否匹配合同规则2015-11-11 jonathan
	 * 
	 * @return
	 */
	public static boolean getBatchPZRule() {
		String value = getParamValue(PLPZ_MATCH_CONTRACT, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	
	// 增加发货单导入是否按照路线拆分规则
	public static boolean getMatchLineRule() {
		String value = getParamValue(INVOICE_IMPOTER_MATCH_LINE, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	// 增加发货单导入是否按照路线拆分规则
	public static boolean getMilkRunLimit() {
		String value = getParamValue(MILKRUN_LIMIT, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	public static boolean getUpdateSegment() {
		String value = getParamValue(UPDATE_SEGMENT, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	public static boolean getIfSaveChangeRecrod() {
		String value = getParamValue(SAVE_CHANGE_RECORD, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	public static boolean getIfMergeSameInvoice() {
		String value = getParamValue(MERGE_SAME_INVOICE, Constants.N);
		return Constants.Y.equalsIgnoreCase(value);
	}
	//增加发货单导入是否合并行
	public static boolean getMergeRule() {
		String value = getParamValue(INVOICE_IMPOTER_MERGE, Constants.Y);
		return Constants.Y.equalsIgnoreCase(value);
	}
	
	/**
	 * 读取参数配置中的
	 * 
	 * @return
	 */
	public static boolean getSimpleUnConfirm() {
		String value = getParamValue(SIMPLEUNCONFIRM, Constants.Y);
		return Constants.Y.equalsIgnoreCase(value);
	}
}

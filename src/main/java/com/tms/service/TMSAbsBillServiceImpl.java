package com.tms.service;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsBillServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.utils.ParameterHelper;
import org.nw.utils.QueryHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.WebUtils;

/**
 * 
 * @author xuqc
 * @date 2014-4-20 上午12:08:19
 */
public abstract class TMSAbsBillServiceImpl extends AbsBillServiceImpl {

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		// 币种
		values.put("currency", ParameterHelper.getCurrency());
		return values;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		String logicCond = buildLogicCondition(paramVO, templetVO);
		if(StringUtils.isNotBlank(logicCond)) {
			cond += " and " + logicCond;
		}
		String customerOrCarrier = getCustomerOrCarrierCond(paramVO);
		if(StringUtils.isNotBlank(customerOrCarrier)) {
			cond += " and " + customerOrCarrier;
		}
		return cond;
	}

	/**
	 * 客户或者承运商登陆时，只能查询对应的客户或者承运商的数据
	 * 
	 * @param paramVO
	 * @return
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
					return " pk_customer='" + userVO.getPk_customer() + "'";
				}
			} else if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CARRIER.intValue()) {
				// 如果是承运商
				Field pk_carrier = ReflectionUtils.getDeclaredField(superVO, "pk_carrier");
				if(pk_carrier != null) {
					return " pk_carrier='" + userVO.getPk_carrier() + "'";
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#useDefaultCorpCondition(org.nw
	 * .vo.ParamVO)
	 */
	public boolean useDefaultCorpCondition(ParamVO paramVO) {
		return false;
	}

	/**
	 * 目前这个逻辑条件只在tms中使用
	 */
	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(ParamVO paramVO, UiQueryTempletVO templetVO) {
		Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
		return buildLogicCondition(clazz, templetVO);
	}

	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		try {
			SuperVO superVO = (SuperVO) clazz.newInstance();
			Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
			if(pk_corp != null) {
				String corpCond = CorpHelper.getCurrentCorpWithChildren(superVO.getTableName());
				String logicCond = QueryHelper.getLogicCond(templetVO);
				if(StringUtils.isNotBlank(logicCond)) {
					corpCond += " or (" + logicCond + ")";
				}
				return "(" + corpCond + ")";
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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
		// XXX 实际上这里真正的逻辑应该是根据单据查询条件来，但是这里实际上还不能得到查询模板，所以也没办法，这里的实现只是一个默认的实现
		String where = this.getCodeFieldCode() + "=?";
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(corpCond)) {
			where += " and " + corpCond;
		}
		return NWDao.getInstance().queryByCondition(parentVOClass, where, code);
	}

	/**
	 * 解析从前台发过来的查询条件
	 * 
	 * @return
	 */
	public String parseCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String cond = QueryHelper.parseCondition(params, paramVO, templetVO);
		String timelineCond = this.parseTimelineCond(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(timelineCond)) {
			cond += " and " + timelineCond;
		}
		return cond;
	}

	/**
	 * 返回当前日期加上timeline
	 * 
	 * @return
	 */
	private Date getDate(int timeline) {
		if(timeline >= 10) {// 大于10的是到货类型
			timeline = timeline - 10;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DAY_OF_YEAR, timeline);
		return cal.getTime();
	}

	/**
	 * 识别timeline的查询条件
	 * 
	 * @param params
	 * @param paramVO
	 * @param queryTempletVO
	 * @return
	 */
	protected String parseTimelineCond(String params, ParamVO paramVO, UiQueryTempletVO queryTempletVO) {
		StringBuffer cond = new StringBuffer(" 1=1 ");
		if(StringUtils.isNotBlank(params) && !"[]".equals(params)) {
			JsonNode node = JacksonUtils.readTree(params);
			for(int m = 0; m < node.size(); m++) {
				JsonNode child = node.get(m);
				String fieldName = child.get("fieldName").getValueAsText().trim(); // 在设计参照的公式时，经常会在前面加入空格
				String value = child.get("value").getValueAsText().trim();
				// String condition =
				// child.get("condition").getValueAsText().trim();
				if(StringUtils.isBlank(value) || "null".equalsIgnoreCase(value)) {
					continue;
				}
				String tablePrefix = "";
				int index = fieldName.indexOf(".");
				if(index > 0) {
					// 包括前缀
					tablePrefix = fieldName.substring(0, index + 1);// 这里的值包括最后的点号
					fieldName = fieldName.substring(index + 1);
				}
				// 如果是timeline字段，实际需要将查询提货日期的进行推算，比如时效是1天，那么要查询提货日期在明天一整天之间的运段
				if(fieldName.equals("timeline")) {
					cond.append(" and (");
					String[] arr = value.split(",");
					for(int i = 0; i < arr.length; i++) {
						int timeline = 0;
						try {
							timeline = Integer.parseInt(arr[i]);// 时效天数
						} catch(Exception e) {

						}
						String realField = "";
						if(timeline < 10) {
							realField = "req_deli_date";
						} else {
							realField = "req_arri_date";
						}
						if(StringUtils.isNotBlank(tablePrefix)) {
							realField = tablePrefix + realField;
						}
						String s_date = DateUtils.formatDate(getDate(0), DateUtils.DATETIME_FORMAT_HORIZONTAL);
						String e_date = DateUtils.formatDate(getDate(timeline + 1),
								DateUtils.DATETIME_FORMAT_HORIZONTAL);
						cond.append(" (" + realField + " >='");
						cond.append(s_date);
						cond.append("' and " + realField + " <'");
						cond.append(e_date);
						cond.append("')");
						if(i != arr.length - 1) {
							cond.append(" or ");
						}
					}
					cond.append(") ");
					continue;
				}
			}
		}
		return cond.toString();
	}
}

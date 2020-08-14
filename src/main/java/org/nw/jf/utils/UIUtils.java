package org.nw.jf.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nw.basic.util.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.CacheUtils;
import org.nw.dao.RefinfoDao;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ext.Checkbox;
import org.nw.jf.ext.CheckboxGroup;
import org.nw.jf.ext.Combox;
import org.nw.jf.ext.ComboxStore;
import org.nw.jf.ext.Field;
import org.nw.jf.ext.LocalCombox;
import org.nw.jf.ext.MultiSelectField;
import org.nw.jf.ext.NumberField;
import org.nw.jf.ext.Radio;
import org.nw.jf.ext.RadioGroup;
import org.nw.jf.ext.TextField;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.ext.ref.userdefine.IMultiSelect;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.utils.RefUtils;
import org.nw.utils.VariableHelper;
import org.nw.vo.RefVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.RefInfoVO;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.web.utils.WebUtils;

import com.uft.webnc.cache.proxy.CacheManagerProxy;

/**
 * 生成UI的工具类
 * 
 * @author xuqc
 * @date 2011-12-22
 */
public class UIUtils {

	static Logger logger = Logger.getLogger(UIUtils.class);

	/**
	 * 返回reftype定义的clazz的一个实例,这里只针对自定义参照类型,目前只有多选类型调用到
	 * 
	 * @param reftype
	 * @param datatype
	 * @return
	 * @author xuqc
	 * @date 2011-12-29
	 */
	public static Object getReftypeObj(String reftype, int datatype) {
		if(StringUtils.isBlank(reftype) || !reftype.startsWith("<") || !reftype.endsWith(">")) {
			return null;
		}
		String refclass = reftype.substring(1, reftype.length() - 1);
		try {
			// 考虑到每次都需要创建实例，这里加入缓存
			String cacheKey = "UIUtils.getReftypeObj_" + refclass;
			if(CacheUtils.isUseCache()) {
				// 注意这里的key需要区分表头和表体的
				Object cacheValue = CacheManagerProxy.getICache().get(cacheKey);
				if(cacheValue != null) {
					return cacheValue;
				}
			}
			Class<?> clazz = Class.forName(refclass);
			Object obj = clazz.newInstance();
			if(CacheUtils.isUseCache()) {
				CacheManagerProxy.getICache().put(cacheKey, obj);
			}
			return obj;
		} catch(Exception e) {

		}
		return null;
	}

	/**
	 * 检测模板设置的是否是多选下拉框,多选的设置方式应该是自定义参照,并且参照类继承IMultiSelect接口
	 * 
	 * @param reftype
	 * @return
	 * @author xuqc
	 * @date 2011-12-22
	 */
	public static boolean isMultiSelect(String reftype, int datatype) {
		if(StringUtils.isBlank(reftype) || !reftype.startsWith("<") || !reftype.endsWith(">")) {
			return false;
		}
		Object obj = getReftypeObj(reftype, datatype);
		return obj instanceof IMultiSelect;
	}

	/**
	 * 生成查询窗口的多选下拉框
	 * 
	 * @param id
	 *            生成的组件的ID
	 * @param condVO
	 *            查询模板条件
	 * @return
	 * @author xuqc
	 * @date 2011-12-22
	 */
	public static MultiSelectField buildMultiSelectField(String id, QueryConditionVO condVO) {
		if(condVO == null) {
			return null;
		}
		if(StringUtils.isBlank(id)) {
			id = condVO.getField_code();
			int index = id.indexOf(".");
			if(index > -1) {
				id = id.substring(index + 1);
			}
		}
		MultiSelectField m_item = new MultiSelectField();
		List<String[]> values = null;
		try {
			//values = UiTempletUtils.getSelectValues(condVO.getConsult_code());
			values =  RedisDao.getInstance().getSelectValues(condVO.getConsult_code());
		} catch(Exception e) {
			e.printStackTrace();
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("查询字段:" + condVO.getField_code() + "生成select标签时出错");
			}else if( WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Query field: "+ condVO.getField_code() +" generates an error when the select tag is generated");
			}
			throw new RuntimeException("查询字段:" + condVO.getField_code() + "生成select标签时出错");
		}
		List<Checkbox> itemList = new ArrayList<Checkbox>();

		// 下拉的默认值做特殊处理
		// 可能是以[]包围的
		String[] valueArr = null;
		if(StringUtils.isNotBlank(condVO.getValue())) {
			String value = condVO.getValue();
			if(value.startsWith("[")) {
				value = value.substring(1, value.length() - 1);// 过滤前后两个[]
			}
			valueArr = value.split(",");
		}

		for(int i = 0; i < values.size(); i++) {
			String[] arr = values.get(i);
			Checkbox checkbox = new Checkbox();
			checkbox.setBoxLabel(arr[0]);
			checkbox.setWidth(null);
			checkbox.setInputValue(arr[1]);
			// 设置是否默认选择
			if(valueArr != null) {
				for(int j = 0; j < valueArr.length; j++) {
					if(arr[1].equals(valueArr[j])) {
						checkbox.setChecked(true);
						break;
					}
				}
			}
			itemList.add(checkbox);
		}

		m_item.setItems(itemList);
		m_item.setName(id);
		m_item.setId(id);
		m_item.setHideLabel(true);
		m_item.setFieldLabel(condVO.getField_name());
		m_item.setValue(VariableHelper.resolve(condVO.getValue()));// 默认值
		// 是否锁定
		if(condVO.getIf_immobility().booleanValue()) {
			m_item.setReadOnly(true);
		}

		// 能否为空
		m_item.setAllowBlank(!condVO.getIf_must().booleanValue());
		return m_item;
	}

	/**
	 * 生成单据窗口的多选下拉框
	 * 
	 * @param id
	 *            生成的组件的ID
	 * @param condVO
	 *            查询模板条件
	 * @return
	 * @author xuqc
	 * @date 2011-12-22
	 */
	public static MultiSelectField buildMultiSelectField(BillTempletBVO fieldVO, String consult_code) {
		MultiSelectField m_item = new MultiSelectField();
		List<String[]> values = null;
		try {
			//values = UiTempletUtils.getSelectValues(consult_code);
			values =  RedisDao.getInstance().getSelectValues(consult_code);
		} catch(Exception e) {
			e.printStackTrace();
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("查询字段:" + fieldVO.getItemkey() + "生成select标签时出错");
			}else if( WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Query field: "+ fieldVO.getItemkey() +" generates an error when the select tag is generated");
			}
			throw new RuntimeException("查询字段:" + fieldVO.getItemkey() + "生成select标签时出错");
		}
		List<Checkbox> itemList = new ArrayList<Checkbox>();

		// 下拉的默认值做特殊处理
		// 可能是以[]包围的
		String[] valueArr = null;
		if(StringUtils.isNotBlank(fieldVO.getDefaultvalue())) {
			String value = fieldVO.getDefaultvalue();
			if(value.startsWith("[")) {
				value = value.substring(1, value.length() - 1);// 过滤前后两个[]
			}
			valueArr = value.split(",");
		}

		for(int i = 0; i < values.size(); i++) {
			String[] arr = values.get(i);
			Checkbox checkbox = new Checkbox();
			checkbox.setBoxLabel(arr[0]);
			checkbox.setWidth(null);
			checkbox.setInputValue(arr[1]);
			// 设置是否默认选择
			if(valueArr != null) {
				for(int j = 0; j < valueArr.length; j++) {
					if(arr[1].equals(valueArr[j])) {
						checkbox.setChecked(true);
						break;
					}
				}
			}
			itemList.add(checkbox);
		}

		m_item.setItems(itemList);
		m_item.setName(fieldVO.getItemkey());
		m_item.setId(fieldVO.getItemkey());
		m_item.setValue(VariableHelper.resolve(fieldVO.getDefaultvalue()));// 默认值
		return m_item;
	}

	/**
	 * <p>
	 * 返回单个查询条件
	 * </p>
	 * 
	 * @param uiQueryTempletVO
	 * @param id
	 * @param pk
	 * @return
	 * @author xuqc
	 * @date 2012-4-19
	 */
	public static Object getItem(UiQueryTempletVO uiQueryTempletVO, String cid, String pk) {
		QueryConditionVO condVO = UiTempletUtils.getConditionByPk(uiQueryTempletVO.getConditions(), pk);
		if(condVO == null) {
			// 可能是保存的查询条件在查询模板中被删除了
			logger.warn("查询条件可能已被删除,PK:" + pk + ",组件ID:" + cid);
		}
		return getItem(condVO, cid);
	}

	public static Object getItem(QueryConditionVO condVO, String cid) {
		return getItem(condVO, cid, true);
	}

	/**
	 * <p>
	 * 返回单个查询条件
	 * </p>
	 * 注意：参照类型返回的是一个List，返回前台的数据集的标识符是datas
	 * 
	 * @param condVO
	 * @param cid
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static Object getItem(QueryConditionVO condVO, String cid, Boolean isKeepAlias) {
		if(condVO == null) {
			return null;
		}
		String idx = "";
		if(StringUtils.isNotBlank(cid)) {
			// 从查询模板来的
			idx = cid.substring(cid.lastIndexOf("_"));// 注意这里idx以下划线开头
		} else {
			// 可能field_code是包括表前缀的，这里把表前缀去掉
			cid = condVO.getField_code();
			int index = cid.indexOf(".");
			if(!isKeepAlias) {
				if(index > -1) {
					cid = cid.substring(index + 1);
				}
			}
		}
		TextField item = null;
		if(DATATYPE.INTEGER.equals(condVO.getData_type())) { // 整数
			item = new NumberField(0);
		} else if(DATATYPE.DECIMAL.equals(condVO.getData_type())) { // 小数
			int decimalPrecision = 0;
			// NC默认的consult_code是-99
			if(StringUtils.isBlank(condVO.getConsult_code()) || "-99".equals(condVO.getConsult_code())) {
				decimalPrecision = UiConstants.DEFAULT_PRECISION;
			} else {
				decimalPrecision = UiTempletUtils.getPrecision(condVO.getConsult_code());
			}
			item = new NumberField(decimalPrecision);
		} else if(DATATYPE.SELECT.equals(condVO.getData_type())) { // 下拉
			Field f = null;
			String consult_code = condVO.getConsult_code();
			if(StringUtils.isNotBlank(consult_code) && !"-99".equals(consult_code) && consult_code.length() > 3) {
				if(consult_code.indexOf("L") == 1) {// 如果第二个字母是L,单选下拉框
					condVO.setConsult_code(consult_code.replaceFirst("L", ""));
					f = buildCombox(cid, condVO);
				}
			}
			f = buildMultiSelectField(cid, condVO);
			if(condVO.getIf_must() != null && condVO.getIf_must().booleanValue()) {
				f.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
			}
			if(condVO.getIf_immobility() != null && condVO.getIf_immobility().booleanValue()) {
				f.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
			}
			return f;
		} else if(DATATYPE.CHECKBOX.equals(condVO.getData_type())) { // 逻辑
			// 对逻辑框进行特殊处理
			condVO.setConsult_code("SX,是=Y,否=N");
			Field f = buildCombox(cid, condVO);
			if(condVO.getIf_must() != null && condVO.getIf_must().booleanValue()) {
				f.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
			}
			if(condVO.getIf_immobility() != null && condVO.getIf_immobility().booleanValue()) {
				f.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
			}
			return f;
		} else if(DATATYPE.REF.equals(condVO.getData_type()) || DATATYPE.USERDEFINE.equals(condVO.getData_type())) {
			// 参照或者自定义项档案
			// 查询窗口的参照域，需要同时生成
			String refclass = DataTypeConverter.getRefClazz(condVO.getConsult_code(), condVO.getData_type());
			try {
				Class<?> clazz = Class.forName(refclass);
				BaseRefModel refModel = (BaseRefModel) clazz.newInstance();

				if(StringUtils.isBlank(refModel.getRefName())) {
					refModel.setRefName(DataTypeConverter.getRefName(condVO.getConsult_code(), condVO.getData_type()));
				}
				// 将参照名称作为参数，有些业务可能需要根据该参照名称进行判断
				refModel.getRefWindow().addParam("refName:'" + refModel.getRefName() + "'");

				// 从单据模板中读取编辑公式，注意单据模板的字段是不加前缀的，此处需要判断查询模板是否加入了前缀。
				String fieldName = condVO.getField_code();
				if(!isKeepAlias) {
					int index = fieldName.indexOf(".");
					if(index > -1) {
						// 将原有fieldName的前缀去掉，返回页面的表单域名称都不带.号
						fieldName = fieldName.substring(index + 1);
					}
				}
				refModel.setId(fieldName + idx);
				refModel.setFieldLabel(condVO.getField_name());
				refModel.setHideLabel(true);
				// refModel.setDisabled(condVO.getIf_immobility().booleanValue());
				// // 是否锁定
				refModel.setReadOnly(condVO.getIf_immobility().booleanValue());
				// 能否为空
				refModel.setAllowBlank(!condVO.getIf_must().booleanValue());
				refModel.setValue(JacksonUtils.writeValueAsString(UIUtils.getRefValue(refModel, condVO.getValue())));

				// 参照的返回值
				refModel.setReturnType(condVO.getReturn_type());
				if(refModel.getReturnType().intValue() == UiConstants.RETURN_TYPE[0]
						|| refModel.getReturnType().intValue() == UiConstants.RETURN_TYPE[1]) {
					// 如果返回值是code和name类型，则鼠标移开时不进行匹配
					refModel.setFillinable(true);
				}
				refModel.setIsMulti(condVO.getIf_autocheck().booleanValue()); // 查询条件设置为多选
				if(condVO.getIf_must() != null && condVO.getIf_must().booleanValue()) {
					refModel.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
				}
				if(condVO.getIf_immobility() != null && condVO.getIf_immobility().booleanValue()) {
					refModel.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
				}
				// 对应查询模板上面的参照，都不能使用直接输入的模式
				refModel.setFillinable(null);
				return refModel;
			} catch(Exception e) {
				logger.warn("参照类型：" + refclass, e);
				throw new RuntimeException(e);
			}
		} else if(DATATYPE.DATE.equals(condVO.getData_type())) { // 日期
			item = new TextField();
			item.setXtype(UiConstants.FORM_XTYPE.UFTDATEFIELD.toString());
		} else if(DATATYPE.TIMESTAMP.equals(condVO.getData_type())) { // 日期时间
			item = new TextField();
			item.setXtype(UiConstants.FORM_XTYPE.DATETIMEFIELD.toString());
		} else {
			item = new TextField();
		}
		item.setId(cid);
		item.setHideLabel(true);
		item.setFieldLabel(condVO.getField_name());
		item.setValue(condVO.getValue()); // 设置默认值
		// 是否锁定
		if(condVO.getIf_immobility().booleanValue()) {
			item.setReadOnly(true);
		}
		if(condVO.getIf_must() != null && condVO.getIf_must().booleanValue()) {
			item.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
		}
		if(condVO.getIf_immobility() != null && condVO.getIf_immobility().booleanValue()) {
			item.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
		}

		// 能否为空
		item.setAllowBlank(!condVO.getIf_must().booleanValue());

		Object rItem = item;
		// 如果默认的条件是介于，那么生成2个输入框
		if(condVO.getOpera_code().indexOf(UiConstants.BETWEEN_OPERA_CODE) == 0) {
			Field s_item = item;
			Field e_item = s_item.clone();
			s_item.setId(Constants.START_DATE_FIELD_PREFIX + s_item.getId());
			e_item.setId(Constants.END_DATE_FIELD_PREFIX + e_item.getId());
			List<Field> itemAry = new ArrayList<Field>();
			itemAry.add(s_item);
			itemAry.add(e_item);
			rItem = itemAry;
		}

		// 设置默认值
		if(StringUtils.isNotBlank(condVO.getValue())) {
			String[] valueAry = condVO.getValue().split(",");
			if(rItem instanceof List) {
				List<Field> itemAry = (List<Field>) rItem;
				int length = valueAry.length > itemAry.size() ? itemAry.size() : valueAry.length;
				for(int i = 0; i < length; i++) {
					itemAry.get(i).setValue(valueAry[i]);
				}
			} else {
				item.setValue(condVO.getValue().split(",")[0]);
			}
		}
		return rItem;
	}

	/**
	 * 生成锁定条件
	 * 
	 * @param condVO
	 * @param isKeepAlias
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object buildLockingField(QueryConditionVO condVO, Boolean isKeepAlias) {
		if(condVO == null) {
			return null;
		}
		Object item = UIUtils.getItem(condVO, null, isKeepAlias);
		if(item instanceof List || DATATYPE.DATE.equals(condVO.getData_type())
				|| DATATYPE.TIMESTAMP.equals(condVO.getData_type()) || DATATYPE.INTEGER.equals(condVO.getData_type())
				|| DATATYPE.DECIMAL.equals(condVO.getData_type())) {
			Field s_item = null, e_item = null;
			if(item instanceof List) {
				List<Field> itemAry = (List<Field>) item;
				s_item = itemAry.get(0);
				s_item.setName(s_item.getId());
				s_item.setId(null);
				s_item.setHideLabel(null);
				s_item.setFieldLabel(condVO.getField_name());

				e_item = itemAry.get(1);
				e_item.setName(e_item.getId());
				e_item.setId(null);
				e_item.setHideLabel(null);
				e_item.setFieldLabel("到");
			} else {
				s_item = (Field) item;
				s_item.setName(s_item.getId());
				s_item.setId(null);
				s_item.setHideLabel(null);
				s_item.setFieldLabel(condVO.getField_name());
			}
		} else if(DATATYPE.REF.equals(condVO.getData_type()) || DATATYPE.USERDEFINE.equals(condVO.getData_type())) {
			BaseRefModel refModel = (BaseRefModel) item;
			refModel.setName(refModel.getId());
			refModel.setId(null);
			refModel.setHideLabel(null);
			refModel.setFieldLabel(condVO.getField_name());
			refModel.getRefWindow().setParams(null);// 这里会有单引号
		} else {
			org.nw.jf.ext.Field field = (org.nw.jf.ext.Field) item;
			field.setName(field.getId());
			field.setId(null);
			field.setHideLabel(null);
			field.setFieldLabel(condVO.getField_name());
		}
		return item;
	}

	/**
	 * 根据pk返回参照的值对象，这是为了避免前台会根据pk去查询整个参照的值，而直接返回一个对象的值
	 * 
	 * @param refModel
	 * @param pk
	 * @return
	 * @author xuqc
	 * @date 2012-7-12
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object getRefValue(BaseRefModel refModel, String pkValue) {
		if(StringUtils.isBlank(pkValue)) {
			return null;
		}
		// 可能本身就为null值
		Map<String, Object> voMap = refModel.getByPk(pkValue);
		if(voMap != null && voMap.get("data") != null) {
			// 可能返回的值也是为null，如参照压根就没有实现
			RefVO refVO = null;
			// 有些getByPk是返回SuperVO，也可能返回Map
			if(voMap.get("data") instanceof SuperVO) {
				SuperVO superVO = (SuperVO) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
				refVO = RefUtils.convert(refModel, superVO);
			} else {
				Map map = (Map) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
				refVO = RefUtils.convert(refModel, map);
			}
			return refVO;
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
			return resultList;
		} else {
			if(refModel.isFillinable() != null && refModel.isFillinable()) {
				// 可录入的参照，返回原值
				RefVO refVO = new RefVO();
				refVO.setPk(pkValue.toString());
				refVO.setName(pkValue.toString());
				refVO.setCode(pkValue.toString());
				return refVO;
			}
		}
		return null;
	}

	/**
	 * 生成combox表单域,查询模板有时候也要生成单选的
	 * 
	 * @return
	 */
	public static Combox buildCombox(String id, QueryConditionVO condVO) {
		if(StringUtils.isBlank(id)) {
			id = condVO.getField_code();
		}
		LocalCombox combox = new LocalCombox();
		//List<String[]> valueAry = UiTempletUtils.getSelectValues(condVO.getConsult_code());
		List<String[]> valueAry =  RedisDao.getInstance().getSelectValues(condVO.getConsult_code());
		// 非必输项，默认增加空白行
		valueAry.add(0, new String[] { "&nbsp;", "" });
		combox.setStore(new ComboxStore(valueAry));
		combox.setId(id);
		// 这里不设置hiddenName，否则会因为id和HiddenName相同导致在IE7下会出现combo长度为0的情况。
		// 不设置hiddenName的情况下，hiddenName=name
		// combox.setHiddenName(this.getItemkey().trim());
		combox.setName(id);
		combox.setFieldLabel(condVO.getField_name());
		combox.setReadOnly(condVO.getIf_immobility().booleanValue());
		// 能否为空
		combox.setAllowBlank(!condVO.getIf_must().booleanValue());
		// 设置默认值
		combox.setValue(condVO.getValue());
		if(condVO.getConsult_code().startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
			combox.setNumberflag(true);
		}
		return combox;
	}

	/**
	 * 下拉框以CG,开头.默认值可以使用逗号分开
	 * 
	 * @param fieldVO
	 * @return
	 */
	public static CheckboxGroup buildCheckboxGroup(BillTempletBVO fieldVO) {
		if(fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.CG.toString())) {
			//List<String[]> valueAry = UiTempletUtils.getSelectValues(fieldVO.getReftype());
			List<String[]> valueAry =  RedisDao.getInstance().getSelectValues(fieldVO.getReftype());
			String defaultValue = fieldVO.getDefaultvalue();
			String[] valueArr = null;
			if(StringUtils.isNotBlank(defaultValue)) {
				valueArr = defaultValue.split(Constants.SPLIT_CHAR);
			}
			CheckboxGroup cg = new CheckboxGroup();
			cg.setFieldLabel(fieldVO.getDefaultshowname());
			cg.setId(fieldVO.getItemkey());
			cg.setName(fieldVO.getItemkey());

			if(fieldVO.getReviseflag().booleanValue()) {
				cg.setReviseflag(true);
			}
			cg.setHidden(fieldVO.getShowflag().intValue() == Constants.NO.intValue());
			if(!fieldVO.getNullflag().equals(Constants.NO)) {
				cg.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
				// combox.setLabelStyle(UiConstants.LABEL_STYLE);
			}
			if(fieldVO.getEditflag().equals(Constants.NO)) {
				// combox.setDisabled(true);
				cg.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
				cg.setReadOnly(true);
			}
			if(fieldVO.getNewlineflag() != null && fieldVO.getNewlineflag().booleanValue()) {
				cg.setNewlineflag(true);
			}
			cg.setColspan(fieldVO.getWidth());
			cg.setScript(fieldVO.getUserdefine1());// 前台执行的js脚本
			if(StringUtils.isNotBlank(fieldVO.getEditformula())) {
				cg.setHasEditformula(true);
				cg.setPkBilltemplet(fieldVO.getPk_billtemplet());
				cg.setPkBilltempletB(fieldVO.getPk_billtemplet_b());
			}
			for(String[] arr : valueAry) {
				Checkbox checkbox = new Checkbox();
				checkbox.setBoxLabel(arr[0]);
				checkbox.setWidth(null);
				checkbox.setInputValue(arr[1]);
				// 设置是否默认选择
				if(valueArr != null) {
					for(int j = 0; j < valueArr.length; j++) {
						if(arr[1].equals(valueArr[j])) {
							checkbox.setChecked(true);
							break;
						}
					}
				}
				cg.getItems().add(checkbox);
			}
			return cg;
		}
		return null;
	}

	/**
	 * 下拉框以RG,开头
	 * 
	 * @param fieldVO
	 * @return
	 */
	public static RadioGroup buildRadioGroup(BillTempletBVO fieldVO) {
		if(fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.RG.toString())) {
			//List<String[]> valueAry = UiTempletUtils.getSelectValues(fieldVO.getReftype());
			List<String[]> valueAry =  RedisDao.getInstance().getSelectValues(fieldVO.getReftype());
			String defaultValue = fieldVO.getDefaultvalue();
			RadioGroup rg = new RadioGroup();
			rg.setFieldLabel(fieldVO.getDefaultshowname());
			rg.setId(fieldVO.getItemkey());
			rg.setName(fieldVO.getItemkey());

			if(fieldVO.getReviseflag().booleanValue()) {
				rg.setReviseflag(true);
			}
			rg.setHidden(fieldVO.getShowflag().intValue() == Constants.NO.intValue());
			if(!fieldVO.getNullflag().equals(Constants.NO)) {
				rg.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
			}
			if(fieldVO.getEditflag().equals(Constants.NO)) {
				rg.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
				rg.setReadOnly(true);
			}
			if(fieldVO.getNewlineflag() != null && fieldVO.getNewlineflag().booleanValue()) {
				rg.setNewlineflag(true);
			}
			rg.setColspan(fieldVO.getWidth());
			rg.setScript(fieldVO.getUserdefine1());// 前台执行的js脚本
			if(StringUtils.isNotBlank(fieldVO.getEditformula())) {
				rg.setHasEditformula(true);
				rg.setPkBilltemplet(fieldVO.getPk_billtemplet());
				rg.setPkBilltempletB(fieldVO.getPk_billtemplet_b());
			}
			for(String[] arr : valueAry) {
				Radio radio = new Radio();
				radio.setBoxLabel(arr[0]);
				radio.setWidth(null);
				radio.setInputValue(arr[1]);
				radio.setName(fieldVO.getItemkey() + "_radio");
				// 设置是否默认选择
				if(StringUtils.isNotBlank(defaultValue) && defaultValue.equals(arr[1])) {
					radio.setChecked(true);
				}
				rg.getItems().add(radio);
			}
			return rg;
		}
		return null;
	}

	/**
	 * 生成combox表单域
	 * 
	 * @return
	 */
	public static Combox buildCombox(BillTempletBVO fieldVO) {
		LocalCombox combox = new LocalCombox();
		//List<String[]> valueAry = UiTempletUtils.getSelectValues(fieldVO.getReftype());
		List<String[]> valueAry =  RedisDao.getInstance().getSelectValues(fieldVO.getReftype());
		if(fieldVO.getNullflag().equals(Constants.NO)) {
			// 非必输项，默认增加空白行
			valueAry.add(0, new String[] { "&nbsp;", "" });
		}
		combox.setStore(new ComboxStore(valueAry));
		combox.setId(fieldVO.genUniqueId());
		// 这里不设置hiddenName，否则会因为id和HiddenName相同导致在IE7下会出现combo长度为0的情况。
		// 不设置hiddenName的情况下，hiddenName=name
		// combox.setHiddenName(this.getItemkey().trim());
		combox.setName(fieldVO.getItemkey());
		combox.setFieldLabel(fieldVO.getDefaultshowname());
		if(fieldVO.getNullflag().equals(Constants.YES)) {
			combox.setAllowBlank(false);
		}

		if(fieldVO.getReviseflag().booleanValue()) {
			combox.setReviseflag(true);
		}
		combox.setHidden(fieldVO.getShowflag().intValue() == Constants.NO.intValue());
		if(!fieldVO.getNullflag().equals(Constants.NO)) {
			combox.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
			// combox.setLabelStyle(UiConstants.LABEL_STYLE);
		}
		if(fieldVO.getEditflag().equals(Constants.NO)) {
			// combox.setDisabled(true);
			combox.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
			combox.setReadOnly(true);
		}
		if(fieldVO.getNewlineflag() != null && fieldVO.getNewlineflag().booleanValue()) {
			combox.setNewlineflag(true);
		}
		combox.setColspan(fieldVO.getWidth());
		// 设置默认值
		if(StringUtils.isNotBlank(fieldVO.getDefaultvalue())) {
			combox.setValue(fieldVO.getDefaultvalue());
		} else {

		}
		// 下拉框能否直接录入，usereditflag字段还不能在模板中进行设置，需要在程序中指定
		if(fieldVO.getUsereditflag().intValue() == Constants.YES.intValue()) {
			combox.setEditable(true);
		}

		combox.setScript(fieldVO.getUserdefine1());// 前台执行的js脚本
		if(StringUtils.isNotBlank(fieldVO.getEditformula())) {
			combox.setHasEditformula(true);
			combox.setPkBilltemplet(fieldVO.getPk_billtemplet());
			combox.setPkBilltempletB(fieldVO.getPk_billtemplet_b());
		}
		// 2014-11-29
		if(fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
			combox.setNumberflag(true);
		}
		return combox;
	}

	/**
	 * 生成combox表单域
	 * 
	 * @return
	 */
	public static Combox buildCombox(ReportTempletBVO fieldVO) {
		LocalCombox combox = new LocalCombox();
		//List<String[]> valueAry = UiTempletUtils.getSelectValues(fieldVO.getReftype());
		List<String[]> valueAry =  RedisDao.getInstance().getSelectValues(fieldVO.getReftype());
		combox.setStore(new ComboxStore(valueAry));
		combox.setName(fieldVO.getItemkey());
		if(fieldVO.getReftype().startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
			combox.setNumberflag(true);
		}
		return combox;
	}

	/**
	 * 返回参照对象
	 * 
	 * @param reftype
	 * @param isMulti
	 * @return
	 */
	public static BaseRefModel buildRefModel(String reftype, boolean isBody, boolean isMulti) {
		RefinfoDao dao = new RefinfoDao();
		RefInfoVO vo = dao.getRefinfoVO(reftype);
		String refclass = vo.getRefclass();
		try {
			Class<?> clazz = Class.forName(refclass);
			BaseRefModel refModel = (BaseRefModel) clazz.newInstance();

			if(StringUtils.isBlank(refModel.getRefName())) {
				refModel.setRefName(vo.getName());
			}
			// 将参照名称作为参数，有些业务可能需要根据该参照名称进行判断
			refModel.getRefWindow().addParam("refName:'" + refModel.getRefName() + "'");

			refModel.setHideLabel(true);

			// 参照的返回值
			refModel.setReturnType(2);// 默认返回pk
			refModel.setIsMulti(isMulti);
			if(isBody) {
				refModel.setXtype(UiConstants.FORM_XTYPE.BODYREFFIELD.toString());
			} else {
				refModel.setXtype(UiConstants.FORM_XTYPE.HEADERREFFIELD.toString());
			}
			return refModel;
		} catch(Exception e) {
			logger.warn("参照类型：" + refclass, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 返回Ext的record类型，类型包括：
	 * 
	 * @return
	 */
	public static String getColumnType(int data_type) {
		if(DATATYPE.INTEGER.equals(data_type)) {
			return UiConstants.COLUMN_TYPE.INT.toString();
		} else if(DATATYPE.DECIMAL.equals(data_type)) {
			return UiConstants.COLUMN_TYPE.FLOAT.toString();
		}
		// date类型使用string显示，否则在IE下显示不了日期类型数据
		// else if(DATATYPE.DATE.equals(this.getDatatype())){
		// return UiConstants.COLUMN_TYPE.DATE.toString();
		// }
		else {
			return UiConstants.COLUMN_TYPE.STRING.toString();
		}
	}

	/**
	 * 返回默认的操作符
	 * 
	 * @param condVO
	 * @return
	 */
	public static String getDefaultOperator(QueryConditionVO condVO) {
		if(condVO == null) {
			return null;
		}
		String s = condVO.getOpera_code();
		if(StringUtils.isBlank(s)) {
			return null;
		}
		String[] arr = s.split("@");
		return arr[0];
	}
}

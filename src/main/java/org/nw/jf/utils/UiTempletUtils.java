package org.nw.jf.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.redis.RedisDao;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.utils.WebUtils;

/**
 * 模板工具类
 * 
 * @author xuqc
 * @date 2011-12-22
 */
public class UiTempletUtils {
	private static final Log logger = LogFactory.getLog(UiTempletUtils.class);

	/**
	 * 根据tabCode过滤条件
	 * 
	 * @author wuqb
	 * @date 2010-8-28
	 * @version $Revision$
	 */
	static class PredicateImpl implements Predicate {

		private String tabCode;

		public String getTabCode() {
			return tabCode;
		}

		public void setTabCode(String tabCode) {
			this.tabCode = tabCode;
		}

		/**
		 * @param tabCode
		 */
		public PredicateImpl(String tabCode) {
			super();
			this.tabCode = tabCode;
		}

		public boolean evaluate(Object object) {
			BillTempletBVO field = (BillTempletBVO) object;
			return field.getTable_code().equals(tabCode);
		}
	}

	/**
	 * 将所有的List<ComUiFieldVO>根据pos区分开
	 * 
	 * @author xuqc
	 * @date 2010-9-6
	 * @version $Revision$
	 */
	static class PosPredicateImpl implements Predicate {

		private Integer pos;

		public Integer getPos() {
			return pos;
		}

		public void setPos(Integer pos) {
			this.pos = pos;
		}

		/**
		 * @param tabCode
		 */
		public PosPredicateImpl(Integer pos) {
			super();
			this.pos = pos;
		}

		public boolean evaluate(Object object) {
			if(object instanceof BillTempletBVO) {
				BillTempletBVO field = (BillTempletBVO) object;
				return field.getPos().intValue() == pos.intValue();
			} else if(object instanceof BillTempletTVO) {
				BillTempletTVO tab = (BillTempletTVO) object;
				return tab.getPos().intValue() == pos.intValue();
			}
			return false;
		}
	}

	/**
	 * 将所有的List<ComUiFieldVO>或者List<ComUiTabVo>根据pos区分开，并放入Map中
	 * 
	 * @param fieldVos
	 * @return
	 */
	public static Map<String, Collection<?>> filterByPos(List<?> list) {
		Map<String, Collection<?>> map = new LinkedHashMap<String, Collection<?>>();
		for(Integer pos : UiConstants.POS) {
			map.put(pos.toString(), CollectionUtils.select(list, new PosPredicateImpl(pos)));
		}
		return map;
	}

	/**
	 * @param list
	 * @param tabs
	 * @return
	 */
	public static Map<String, Collection<?>> filterByTab(List<BillTempletBVO> list, List<BillTempletTVO> tabs) {
		Map<String, Collection<?>> map = new LinkedHashMap<String, Collection<?>>(tabs.size());
		for(BillTempletTVO tab : tabs) {
			String tabCode = tab.getTabcode();
			map.put(tabCode, CollectionUtils.select(list, new PredicateImpl(tabCode)));
		}
		return map;
	}

	/**
	 * 返回标签类
	 * 
	 * @param list
	 * @param tabs
	 * @return
	 */
	public static BillTempletTVO filterTabByCode(List<BillTempletTVO> tabs, String tabCode) {
		for(BillTempletTVO tab : tabs) {
			if(tab.getTabcode().equals(tabCode)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * 返回卡片中的字段，包括显示和不显示的 不显示只是设置为hidden而已
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterCard(List<BillTempletBVO> list) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getCardflag().intValue() == Constants.YES) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回表格中的字段，包括显示和不显示的 不显示只是设置为hidden而已
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterList(List<BillTempletBVO> list) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getPos().intValue() == UiConstants.POS[1]) {
				// FIXME 2011-12-1 XUQC
				// 若是表体，以“是否卡片”为准
				if(field.getCardflag().intValue() == Constants.YES) {
					showList.add(field);
				}
			} else {
				if(field.getListflag().intValue() == Constants.YES) {
					showList.add(field);
				}
			}
		}
		return showList;
	}

	/**
	 * 返回所有显示的字段，包括表头、表尾、表体<br/>
	 * 格式如：{HEADER:{},BODY:{TABNAME1:[],TABNAME2:[]}}
	 * 
	 * @param fieldVOs
	 * @param bodyTabCodes
	 * @return
	 * @author xuqc
	 * @date 2011-9-22
	 */
	public static Map<String, Object> getShowedFieldNames(List<BillTempletBVO> fieldVOs, String[] bodyTabCodes) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		String[] cardFieldNames = getCardFieldNames(fieldVOs);
		retMap.put(Constants.HEADER, cardFieldNames);
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		for(String bodyTabCode : bodyTabCodes) {
			String[] bodyListFieldNames = getBodyListFieldNames(fieldVOs, bodyTabCode);
			bodyMap.put(bodyTabCode, bodyListFieldNames);
		}
		retMap.put(Constants.BODY, bodyMap);
		return retMap;
	}

	/**
	 * 返回卡片页面的所有字段集合，注意：包括表头和表尾
	 * 
	 * @param list
	 * @return
	 */
	public static String[] getCardFieldNames(List<BillTempletBVO> list) {
		List<String> fieldNames = new ArrayList<String>();
		for(BillTempletBVO field : list) {
			if(field.getPos().intValue() != UiConstants.POS[1] && field.getCardflag().intValue() == Constants.YES) {
				fieldNames.add(field.getItemkey());
			}
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * 返回表头列表页面的所有字段集合，注意：包括表头和表尾
	 * 
	 * @param list
	 * @return
	 */
	public static String[] getHeaderListFieldNames(List<BillTempletBVO> list) {
		List<String> fieldNames = new ArrayList<String>();
		for(BillTempletBVO field : list) {
			if(field.getPos().intValue() != UiConstants.POS[1] && field.getListflag().intValue() == Constants.YES) {
				fieldNames.add(field.getItemkey());
			}
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * 返回表体列表页面的所有字段集合，注意：只包括表体
	 * 
	 * @param list
	 * @return
	 */
	public static String[] getBodyListFieldNames(List<BillTempletBVO> list, String tabcode) {
		List<String> fieldNames = new ArrayList<String>();
		for(BillTempletBVO field : list) {
			if(field.getPos().intValue() == UiConstants.POS[1] && tabcode.equals(field.getTable_code())
					&& field.getCardflag().intValue() == Constants.YES) {
				fieldNames.add(field.getItemkey());
			}
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * 根据tabcode获取相应的字段名字数组
	 * 
	 * @param billTempletBVOList
	 * @param tabcode
	 * @return
	 */
	public static String[] getFieldNames(List<BillTempletBVO> billTempletBVOList, boolean isBody, String[] tabCodes) {
		List<String> fieldNames = new ArrayList<String>();
		for(BillTempletBVO field : billTempletBVOList) {
			if(tabCodes != null && isBody && field.getPos() != 1) {
				continue;
			}

			if(tabCodes == null) {
				// 如福建中烟差旅费报销单孙表的情况，就没有tabcode，这样的话就全部加入
				fieldNames.add(field.getItemkey());
			} else {
				for(String tabCode : tabCodes) {
					if(tabCode.equals(field.getTable_code())) {
						fieldNames.add(field.getItemkey());
						break;
					}
				}
			}
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * 返回表列表页面的所有字段名称集合，注意：如果是表头，则包括表尾
	 * 
	 * @param list
	 * @return
	 */
	public static String[] getDefaultShowname(List<BillTempletBVO> list, ParamVO paramVO) {
		String[] listShowFieldNames = new String[list.size()];
		for(int i = 0; i < list.size(); i++) {
			listShowFieldNames[i] = list.get(i).getDefaultshowname();
		}
		return listShowFieldNames;
	}

	/**
	 * 返回所有卡片存在且卡片显示的字段
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterCardShow(List<BillTempletBVO> list) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getCardflag().intValue() == Constants.YES && field.getShowflag().intValue() == Constants.YES) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回所有卡片存在但卡片隐藏的字段
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterCardHide(List<BillTempletBVO> list) {
		List<BillTempletBVO> hideList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getCardflag().intValue() == Constants.YES && field.getShowflag().intValue() == Constants.NO) {
				hideList.add(field);
			}
		}
		return hideList;
	}

	/**
	 * 返回所有列表存在并且列表显示的字段
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterListShow(List<BillTempletBVO> list) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getListflag().intValue() == Constants.YES && field.getListshowflag().booleanValue()) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回表头list的字段，包括显示和隐藏
	 * 
	 * @param list
	 * @return
	 * @author xuqc
	 * @date 2012-5-8
	 */
	public static List<BillTempletBVO> filterHeaderList(List<BillTempletBVO> list) {
		List<BillTempletBVO> headerList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getPos() != UiConstants.POS[1] && field.getListflag().intValue() == Constants.YES) {
				headerList.add(field);
			}
		}
		return headerList;
	}

	/**
	 * 返回所有表头和表尾列表存在并且列表显示的字段
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterHeaderListShow(List<BillTempletBVO> list) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getPos() != UiConstants.POS[1] && field.getListflag().intValue() == Constants.YES
					&& field.getListshowflag().booleanValue()) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回所有表头和表尾列表存在并且列表显示的字段,包括主键
	 * 
	 * @deprecated 使用filterULWFields代替
	 * @param list
	 * @return
	 */
	@Deprecated
	public static List<BillTempletBVO> filterHeaderListShowWithPkField(List<BillTempletBVO> list, String headerPkField) {
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getPos() != UiConstants.POS[1] && field.getListflag().intValue() == Constants.YES
					&& (field.getListshowflag().booleanValue() || field.getItemkey().equals(headerPkField))) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回ULW适用的字段，包括显示字段和pk，同时对单据状态做处理 如果单据状态是隐藏域，则必须加入
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterULWFields(List<BillTempletBVO> list, List<String> immobilityFields) {
		if(immobilityFields == null) {
			immobilityFields = new ArrayList<String>();
		}
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getPos() != UiConstants.POS[1] && field.getListflag().intValue() == Constants.YES
					&& (field.getListshowflag().booleanValue() || immobilityFields.contains(field.getItemkey()))) {
				showList.add(field);
			}
		}
		return showList;
	}

	/**
	 * 返回ULW适用的字段，包括显示字段和pk，同时对单据状态做处理 如果单据状态是隐藏域，则必须加入
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterULWFields(List<BillTempletBVO> list, List<String> immobilityFields,
			ParamVO paramVO) {
		if(immobilityFields == null) {
			immobilityFields = new ArrayList<String>();
		}
		List<BillTempletBVO> showList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO fieldVO : list) {
			if(paramVO.isBody()) {
				if(fieldVO.getPos().intValue() == UiConstants.POS[1]
						&& fieldVO.getTable_code().equals(paramVO.getTabCode())
						&& (fieldVO.getShowflag().intValue() == Constants.YES || immobilityFields.contains(fieldVO
								.getItemkey()))) {
					// FIXME 2011-10-20 表体中以"卡片是否显示"为准,忽略"列表是否显示"
					showList.add(fieldVO);
				}
			} else if(fieldVO.getPos() != UiConstants.POS[1] && fieldVO.getListflag().intValue() == Constants.YES
					&& (fieldVO.getListshowflag().booleanValue() || immobilityFields.contains(fieldVO.getItemkey()))) {
				showList.add(fieldVO);
			}
		}
		return showList;
	}

	/**
	 * 返回所有列表存在但隐藏的字段
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterListHide(List<BillTempletBVO> list) {
		List<BillTempletBVO> hideList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(field.getListflag().intValue() == Constants.YES && !field.getListshowflag().booleanValue()) {
				hideList.add(field);
			}
		}
		return hideList;
	}

	/**
	 * 返回当选下拉框
	 * 
	 * @param list
	 * @return
	 */
	public static List<BillTempletBVO> filterSelect(List<BillTempletBVO> list) {
		List<BillTempletBVO> select = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(DATATYPE.SELECT.equals(field.getDatatype())) {
				select.add(field);
			}
		}
		return select;
	}

	/**
	 * 返回多选下拉框
	 * 
	 * @param list
	 * @return
	 * @author xuqc
	 * @date 2011-12-30
	 */
	public static List<BillTempletBVO> filterMultiSelect(List<BillTempletBVO> list) {
		List<BillTempletBVO> multiSelectList = new ArrayList<BillTempletBVO>();
		for(BillTempletBVO field : list) {
			if(DATATYPE.REF.equals(field.getDatatype())) {
				// 多选的下拉框，这个比较特殊
				if(UIUtils.isMultiSelect(field.getReftype(), field.getDatatype())) {
					multiSelectList.add(field);
				}
			}
		}
		return multiSelectList;
	}

	/**
	 * IX返回的是Integer类型 SX返回的是String类型
	 * 
	 * @param vo
	 * @throws Exception
	 *             抛出不支持格式异常
	 * @return List<String[]> String[]的第一个值是text，第二个值是value
	 */
	public static List<String[]> getSelectValues(String reftype) {
		List<String[]> params = new ArrayList<String[]>();
		if(StringUtils.isBlank(reftype)) {
			return params;
		}

		if(reftype.startsWith(UiConstants.COMBOX_TYPE.P.toString()) && reftype.contains("=")) {
			reftype = "S" + reftype;
		}

		if(reftype.startsWith(UiConstants.COMBOX_TYPE.I.toString())) { // 兼容NC
																		// 如：I,1,2,3
			// 实际的combox值为0,1,2，请注意index是从0开始
			reftype = reftype.substring(2);
			String[] arr = reftype.split(",");
			for(int i = 0; i < arr.length; i++) { // i=0是类型标记
				String[] arr1 = new String[2];
				arr1[1] = i + "";
				if(StringUtils.isBlank(arr[i])) {
					arr[i] = "&nbsp;"; // 加入一个空格符，改行属于占位，如果不使用该空格符，则会出现该行选择被压缩，没法正常显示
				}
				arr1[0] = arr[i];
				params.add(arr1);
			}
		} else if(reftype.startsWith(UiConstants.COMBOX_TYPE.S.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.P.toString())) {// P,text
			// S,text
			// 返回的均为text
			reftype = reftype.substring(2);
			String[] arr = reftype.split(",");
			for(int i = 0; i < arr.length; i++) {
				String[] arr1 = new String[2];
				arr1[0] = arr[i];
				arr1[1] = arr[i];
				params.add(arr1);
			}
		} else if(reftype.startsWith(UiConstants.COMBOX_TYPE.SX.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.IX.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.CG.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.RG.toString())) {
			// SX,text=value
			// 返回value
			reftype = reftype.substring(3);
			String[] arr = reftype.split(",");
			for(int i = 0; i < arr.length; i++) {
				String[] arr1 = new String[2];
				if(arr[i].indexOf("=") == -1) {
					arr1[0] = arr[i];
					arr1[1] = arr[i];
				} else {
					arr1 = arr[i].split("=");
				}
				params.add(arr1);
			}
		} else if(reftype.startsWith(UiConstants.COMBOX_TYPE.IP.toString())) { // IP,value=text
			// IP,8=自由态,3=提交状态,1=审批通过,2=审批进行中,0=审批未通过
			// 返回8、3、1、2、0
			reftype = reftype.substring(3);
			String[] arr = reftype.split(",");
			for(int i = 0; i < arr.length; i++) {
				String[] arr1 = new String[2];
				if(arr[i].indexOf("=") == -1) {
					arr1[1] = arr[i];
					arr1[0] = arr[i];
				} else {
					arr1 = arr[i].split("=");
				}
				// 页面中第一个是text，第二个才是value
				String[] realArr = new String[2];
				realArr[0] = arr1[1];
				realArr[1] = arr1[0];
				params.add(realArr);
			}
		} else if(reftype.startsWith(UiConstants.COMBOX_TYPE.SP.toString())) { // SP,value=text
			// SP,YXJ1=项目借款单,YS81=通用借款单,YXJ2=普通借款单
			// 返回YXJ1、YS81、YXJ2
			reftype = reftype.substring(3);
			String[] arr = reftype.split(",");
			for(int i = 0; i < arr.length; i++) {
				String[] arr1 = new String[2];
				if(arr[i].indexOf("=") == -1) {
					arr1[1] = arr[i];
					arr1[0] = arr[i];
				} else {
					arr1 = arr[i].split("=");
				}
				// 页面中第一个是text，第二个才是value
				String[] realArr = new String[2];
				realArr[0] = arr1[1];
				realArr[1] = arr1[0];
				params.add(realArr);
			}
		} else if(reftype.startsWith(UiConstants.COMBOX_TYPE.SF.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.IF.toString())
				|| reftype.startsWith(UiConstants.COMBOX_TYPE.NF.toString())) {
			// IF,SF开头的，根据datatype_code从nw_data_dict表中读取
			// SF开头，返回值是string，IF开头，返回值是IF
			// NF虽然是数值型，但是放到前台也是string，只是到了设置值时需要转成数字型，避免0.5 != 0.50的情况
			String datatype_code = reftype.substring(reftype.lastIndexOf(",") + 1);
			//yaojiie 2015 12 08添加 WITH (NOLOCK)
			String sql = "select * from nw_data_dict_b WITH(NOLOCK) where pk_data_dict "
					+ "in (select pk_data_dict from nw_data_dict WITH(NOLOCK) where isnull(dr,0)=0 and locked_flag='N'"
					+ " and datatype_code=? and (pk_corp=? or pk_corp=?)) order by display_order";
			List<DataDictBVO> dictBVOs = NWDao.getInstance().queryForListWithCache(sql, DataDictBVO.class,
					//XIA 2016 8 7 接口导入时，判断字段无法获得当前登陆公司，取集团。
					datatype_code, WebUtils.getLoginInfo() == null ? Constants.SYSTEM_CODE : WebUtils.getLoginInfo().getPk_corp(), Constants.SYSTEM_CODE);
			for(DataDictBVO dictBVO : dictBVOs) {
				String[] arr = new String[2];
				arr[0] = dictBVO.getDisplay_name();
				arr[1] = dictBVO.getValue();
				params.add(arr);
			}
		} else {
			logger.warn("下拉菜单还不支持[" + reftype + "]的数据格式!");
		}
		return params;
	}

	/**
	 * 根据提供的text值返回下拉框的value
	 * 
	 * @param text
	 * @param reftype
	 * @return
	 * @author xuqc
	 * @date 2011-12-13
	 */
	public static Object getSelectValue(String text, String reftype) {
		//数据字典支持多选显示，这里text可能用 "|" 和 "," 分割
		//List<String[]> list = getSelectValues(reftype); // 这里使用一个没有实际意义的值-1
		List<String[]> list =  RedisDao.getInstance().getSelectValues(reftype);
		String[] textArr = null;
		if(text.indexOf(",") > 0){
			textArr = text.split(",");
		}else if(text.indexOf("|") > 0){
			textArr = text.split("\\|");
		}else{
			textArr = new String[]{text};
		}
		if(list != null) {
			String retStr = "";
			for(int i = 0; i < list.size(); i++) {
				String[] arr = list.get(i); // 这里的arr的长度肯定是2,第一个值是text，第二个值是value
				//如果传入"|"则按照"|"拼接，否则都是","
				for(String textUnit : textArr){
					if(textUnit.trim().equals(arr[0].trim())){
						if(text.indexOf("|") > 0){
							retStr += arr[1].trim() +"|";
						}else{
							retStr += arr[1].trim() +",";
						}
					}
				}
			}
			if(retStr.length() > 0){
				return retStr.substring(0, retStr.length()-1);
			}
		}
		return null;
	}
	
	
	/**
	 * 根据提供的text值返回下拉框的value
	 * 
	 * @param text
	 * @param reftype
	 * @return
	 * @author xuqc
	 * @date 2011-12-13
	 */
	public static Object getSelectText(String value, String reftype) {
		//数据字典支持多选显示，这里text可能用 "|" 和 "," 分割
		//List<String[]> list = getSelectValues(reftype); // 这里使用一个没有实际意义的值-1
		List<String[]> list =  RedisDao.getInstance().getSelectValues(reftype);
		String[] valueArr = null;
		if(value.indexOf(",") > 0){
			valueArr = value.split(",");
		}else if(value.indexOf("|") > 0){
			valueArr = value.split("\\|");
		}else{
			valueArr = new String[]{value};
		}
		if(list != null) {
			String retStr = "";
			for(int i = 0; i < list.size(); i++) {
				String[] arr = list.get(i); // 这里的arr的长度肯定是2,第一个值是text，第二个值是value
				//如果传入"|"则按照"|"拼接，否则都是","
				for(String valueUnit : valueArr){
					if(valueUnit.trim().equals(arr[1].trim())){
						if(value.indexOf("|") > 0){
							retStr += arr[0].trim() +"|";
						}else{
							retStr += arr[0].trim() +",";
						}
					}
				}
			}
			if(retStr.length() > 0){
				return retStr.substring(0, retStr.length()-1);
			}
		}
		return null;
	}
	

	/**
	 * 解析小数类型的精度信息 NC5.6在类型设置中的值如下2,, 以下程序支持NC5.2
	 * 
	 * @param reftype
	 */
	public static int getPrecision(String reftype) {
		String precision = null;
		if(StringUtils.isNotBlank(reftype)) {
			if(reftype.indexOf(",") > -1) {
				precision = reftype.substring(0, reftype.indexOf(","));
			} else {
				precision = reftype;
			}
		}
		if(precision == null) {
			precision = ParameterHelper.getPrecision() + "";
		}
		return new Integer(precision);
	}

	/**
	 * 零是否显示为空 格式： 2,,,Y,,,（第四位）
	 * 
	 * @param reftype
	 * @return
	 */
	public static boolean isNegativeNumberToRed(String reftype) {
		if(reftype != null && reftype.length() > 0) {
			if(reftype.indexOf(",") != -1 && reftype.split(",").length >= 4) {
				String val = reftype.split(",")[3];
				if(val != null && val.length() > 0) {
					return UFBoolean.valueOf(val).booleanValue();
				}
			}
		}
		return false;
	}

	/**
	 * 零是否显示为空 格式： 2,,,,,,Y（最后一位）
	 * 
	 * @param reftype
	 * @return
	 */
	public static boolean isZeroToNull(String reftype) {
		if(reftype != null && reftype.length() > 0) {
			if(reftype.indexOf(",") != -1 && reftype.split(",").length >= 7) {
				String val = reftype.split(",")[6];
				if(val != null && val.length() > 0) {
					return UFBoolean.valueOf(val).booleanValue();
				}
			}
		}
		return false;
	}

	/**
	 * 根据模板的配置信息返回数字的格式，默认不加上千分位
	 * 
	 * @param reftype
	 * @return
	 * @author xuqc
	 * @date 2013-4-24
	 * 
	 */
	public static String getNumberFormat(String reftype) {
		String format = null;
		int precision = UiTempletUtils.getPrecision(reftype);
		if(StringUtils.isNotBlank(reftype)) {
			String[] arr = reftype.split(",");
			if(arr.length > 5) {
				if(Constants.Y.equalsIgnoreCase(arr[5])) {
					// 模板中设置了使用千分位显示
					format = UiTempletUtils.getNumberFormat(precision);
				}
			}
		}
		if(StringUtils.isBlank(format)) {
			format = getNumberFormatWithoutThousands(precision);
		}
		return format;
	}

	/**
	 * 根据精度返回小数类型的格式,这个是EXT中使用的
	 * 
	 * @param precision
	 * @return
	 */
	public static String getNumberFormat(int precision) {
		String format = "0,000.";
		for(int i = 0; i < precision; i++) {
			format += "0";
		}
		return format;
	}

	public static String getNumberFormatWithoutThousands(int precision) {
		String format = "0000";
		if(precision > 0) {
			format += ".";
		}
		for(int i = 0; i < precision; i++) {
			format += "0";
		}
		return format;
	}

	/**
	 * 根据精度返回小数类型的格式,这个是ULW中使用的,不使用千分位
	 * 
	 * @param precision
	 * @return
	 * @author xuqc
	 * @date 2012-5-14
	 */
	public static String getULWNumberFormatWithoutThousands(int precision) {
		String format = "###0";
		if(precision > 0) {
			format += ".";
		}
		for(int i = 0; i < precision; i++) {
			format += "0";
		}
		return format;
	}

	/**
	 * 根据精度返回小数类型的格式,这个是ULW中使用的
	 * 
	 * @param precision
	 * @return
	 * @author xuqc
	 * @date 2012-5-14
	 */
	public static String getULWNumberFormat(int precision) {
		String format = "#,##0.";
		for(int i = 0; i < precision; i++) {
			format += "0";
		}
		return format;
	}

	/**
	 * 根据pk从集合中查询
	 * 
	 * @param conditions
	 * @param pk
	 * @return
	 */
	public static QueryConditionVO getConditionByPk(List<QueryConditionVO> conditions, String pk) {
		for(int i = 0; i < conditions.size(); i++) {
			QueryConditionVO condition = conditions.get(i);
			if(condition.getId().equals(pk)) {
				return condition;
			}
		}
		return null;
	}

	/**
	 * 根据field_code查询一条记录，一个查询模板中可能存在相同的field_code，但肯定属于不同的表，如一个在表头一个在表体。
	 * 这里的condVOs是表头的条件，所以不会有相同field_code的情况
	 * 
	 * @param condVOs
	 * @param field_code
	 * @return
	 * @author xuqc
	 * @date 2012-7-18
	 * 
	 */
	public static QueryConditionVO getCondVOByField_code(List<QueryConditionVO> condVOs, String field_code) {
		for(int i = 0; i < condVOs.size(); i++) {
			QueryConditionVO condVO = condVOs.get(i);
			// 2014-4-19，tms支持某些逻辑条件,可以和查询条件使用相同的key
			if(condVO.getIscondition() != null && condVO.getIscondition().booleanValue()
					&& condVO.getField_code().equals(field_code)) {
				return condVO;
			}
		}
		return null;
	}

	/**
	 * 返回默认的查询条件
	 * 
	 * @param condVOs
	 * @return
	 * @author xuqc
	 * @date 2012-5-15
	 */
	public static List<QueryConditionVO> filterDefaultConds(List<QueryConditionVO> condVOs) {
		List<QueryConditionVO> defaultList = new ArrayList<QueryConditionVO>();
		if(condVOs == null || condVOs.size() == 0) {
			return defaultList;
		}
		for(QueryConditionVO condVO : condVOs) {
			if(condVO.getIf_default().booleanValue() && condVO.getIf_used().booleanValue()) {
				defaultList.add(condVO);
			}
		}
		return defaultList;
	}

	/**
	 * 返回待选择的查询条件
	 * 
	 * @param condVOs
	 * @return
	 * @author xuqc
	 * @date 2012-5-15
	 */
	public static List<QueryConditionVO> filterUsedConds(List<QueryConditionVO> condVOs) {
		List<QueryConditionVO> usedList = new ArrayList<QueryConditionVO>();
		if(condVOs == null || condVOs.size() == 0) {
			return usedList;
		}
		for(QueryConditionVO condVO : condVOs) {
			if(condVO.getIf_used().booleanValue() && !condVO.getIf_default().booleanValue()) {
				usedList.add(condVO);
			}
		}
		return usedList;
	}

	/**
	 * 根据headerListFieldCode定义的顺序，重新排序
	 * 
	 * @param headerListFieldVOs
	 * @param headerListItemKey
	 * @return
	 */
	public static List<BillTempletBVO> reOrderHeaderList(List<BillTempletBVO> headerListFieldVOs,
			String[] headerListItemKey) {
		if(headerListItemKey == null || headerListItemKey.length == 0) {
			return headerListFieldVOs;
		}
		Map<String, BillTempletBVO> map = new HashMap<String, BillTempletBVO>();
		for(BillTempletBVO fieldVO : headerListFieldVOs) {
			map.put(fieldVO.getItemkey(), fieldVO);
		}
		List<BillTempletBVO> orderFieldVOs = new ArrayList<BillTempletBVO>();
		for(String itemKey : headerListItemKey) {
			orderFieldVOs.add(map.get(itemKey));
		}
		return orderFieldVOs;
	}
}

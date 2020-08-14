package org.nw.exp;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 处理NC打印模板的工具类
 * 
 * @author fangw
 */
public class PrintTempletUtils {
	private static final Log logger = LogFactory.getLog(PrintTempletUtils.class);

	// 格式化
	public static final DecimalFormat format_decimal = new DecimalFormat("0.00");
	public static final SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat format_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 对数值型进行格式化，double保留2位小数
	 * 
	 * @param value
	 * @return
	 */
	public static Object formatValue(Object value) {
		if(value == null) {
			value = "";
		}
		if(value instanceof UFDouble) {
			value = PrintTempletUtils.format_decimal.format(((UFDouble) value).toDouble());
		} else if(value instanceof Double) {
			value = PrintTempletUtils.format_decimal.format(value);
		}
		return value;
	}

	public static float getPadding(String options) {
		// 行间距<lineSpaceBetween>3</lineSpaceBetween>
		String lineSpaceBetween = options.substring(options.indexOf("<lineSpaceBetween>") + 18,
				options.indexOf("</lineSpaceBetween>"));
		return Float.parseFloat(lineSpaceBetween);
	}

	public static int getBorder(String lineproperty) {
		if(lineproperty == null || lineproperty.length() != 8) {
			return 0;
		}

		if(lineproperty.equalsIgnoreCase("00000000")) {
			return 0;
		} else {
			// TODO 识别具体的边框类型
			return 2;
		}
	}

	/**
	 * 转换NC单据模板参照中的下拉框数据 因为下拉框保持的只有一个index或者value，这里将转换value为text 2011-4-18
	 */
	public static List<Map<String, Object>> convertSelectName(Map<String, Object> map, BillTempletBVO billTempletBVO) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(map);
		return convertSelectName(list, billTempletBVO);
	}

	/**
	 * 转换NC单据模板参照中的下拉框数据 因为下拉框保持的只有一个index或者value，这里将转换value为text
	 * 
	 * @param list
	 * @param billTempletBVO
	 * @return
	 */
	public static List<Map<String, Object>> convertSelectName(List<Map<String, Object>> list,
			BillTempletBVO billTempletBVO) {
		try {
			String reftype = billTempletBVO.getReftype();
			if(list != null && reftype != null) {
				// 解析reftype
				if(reftype.contains("=")) {
					// 格式示例： SX,交通费=03001,通讯费=03002,招待费=03003
					Map<String, String> map = new HashMap<String, String>();
					if(reftype != null && reftype.length() > 3) {
						reftype = reftype.substring(reftype.indexOf(",") + 1);
						String[] arr = reftype.split(",");
						if(arr != null && arr.length > 0) {
							for(String valuekey : arr) {
								// valuekey格式示例：交通费=03001
								map.put(valuekey.split("=")[1], valuekey.split("=")[0]);
							}
						}
					}
					if(!map.isEmpty()) {
						for(Map<String, Object> model : list) {
							model.put(billTempletBVO.getItemkey(),
									map.get(String.valueOf(model.get(billTempletBVO.getItemkey()))));
						}
					}
				} else {
					if(reftype.startsWith("I,")) {
						// 格式示例：I,AA,BB,CC
						if(reftype != null && reftype.length() > 2) {
							reftype = reftype.substring(reftype.indexOf(",") + 1);
							String[] arr = reftype.split(",");
							if(arr != null && arr.length > 0) {
								for(Map<String, Object> model : list) {
									if(model.get(billTempletBVO.getItemkey()) != null) {
										model.put(billTempletBVO.getItemkey(), arr[(Integer.parseInt(String
												.valueOf(model.get(billTempletBVO.getItemkey()))))]);
									}
								}
							}
						}
					}
					// 格式示例： SX,交通费,通讯费,招待费
					// 这种不用处理
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("处理打印模板的下拉框时出现异常，请检查数据是否正确，打印模板ID为：" + billTempletBVO.getPk_billtemplet() + "，单元格为:"
					+ billTempletBVO.getItemkey());
		}
		return list;
	}

	/**
	 * 转换表头表尾参照pk为name（因为有的参照没有配置显示公式，这样打印出来的将都是pk）
	 * 20120307修改：不管有没有配置显示公式，都进行转换，因为nc打印模板用pk打的也是name
	 * 
	 * @param context
	 * @param uiBillTempletVO
	 */
	public static void changeRefPkToName(Map<String, Object> context, List<BillTempletBVO> billTempletBVOList) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(context);
		for(BillTempletBVO billTempletBVO : billTempletBVOList) {
			if(billTempletBVO.getPos() != UiConstants.POS[1]
					&& UiConstants.DATATYPE.REF.intValue() == billTempletBVO.getDatatype()) {
				// if(billTempletBVO.getLoadformula() == null ||
				// billTempletBVO.getLoadformula().length() == 0) {
				// 如果没有显示公式，则进行转换
				convertPkToName(list, billTempletBVO);
				// }
			}
		}
	}

	/**
	 * 转换表体参照pk为name（因为有的参照没有配置显示公式，这样打印出来的将都是pk）
	 * 20120307修改：不管有没有配置显示公式，都进行转换，因为nc打印模板用pk打的也是name
	 * 
	 * @param list
	 * @param billTempletBVOList
	 */
	public static void changeRefPkToName(List<Map<String, Object>> list, List<BillTempletBVO> billTempletBVOList) {
		for(BillTempletBVO billTempletBVO : billTempletBVOList) {
			if(billTempletBVO.getPos() == UiConstants.POS[1]
					&& UiConstants.DATATYPE.REF.intValue() == billTempletBVO.getDatatype()) {
				// if(billTempletBVO.getLoadformula() == null ||
				// billTempletBVO.getLoadformula().length() == 0) {
				// 如果没有显示公式，则进行转换
				convertPkToName(list, billTempletBVO);
				// }
			}
		}
	}

	public static void convertPkToName(List<Map<String, Object>> list, BillTempletBVO billTempletBVO) {
		if(UIUtils.isMultiSelect(billTempletBVO.getReftype(), billTempletBVO.getDatatype())) {
			// 多选下拉框不需要执行
			return;
		}
		String reftype = billTempletBVO.getReftype();
		String itemkey = billTempletBVO.getItemkey();
		String idcolname = billTempletBVO.getIdcolname();
		if(idcolname == null || idcolname.length() == 0) {
			idcolname = itemkey;
		}

		String refClazz = DataTypeConverter.getRefClazz(reftype, DATATYPE.REF.intValue());

		Class<?> clazz = null;
		try {
			clazz = Class.forName(refClazz);
		} catch(ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		if(clazz == null) {
			return;
		}

		Object owner = null;
		String nameFieldCode = null;
		try {
			owner = clazz.newInstance();
			// 获取name字段code
			Method method = ReflectionUtils.getDeclaredMethod(owner, "getNameFieldCode");
			ReflectionUtils.makeAccessible(method);
			Object ret = ReflectionUtils.invokeMethod(method, owner);
			if(ret != null) {
				nameFieldCode = ret.toString();
			}
		} catch(Exception e) {
			logger.error("类反射调用出错:" + reftype, e);
		}

		for(Map<String, Object> map : list) {
			Object idValue = map.get(idcolname);
			if(idValue == null || idValue.toString().length() == 0) {
				// 没值也用处理了
				continue;
			}

			Object ret = null;
			try {
				ret = ReflectionUtils.invokeMethod(owner, "getByPk", new Class[] { String.class },
						new Object[] { idValue });

			} catch(Exception e) {
				logger.error("参照根据pk反射获取name失败,参照名称:" + reftype + ",PK:" + idValue, e);
				continue;
			}

			if(nameFieldCode != null && ret != null && ret instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> retMap = (Map<String, Object>) ret;
				if(Boolean.TRUE.equals(retMap.get("success"))) {
					Object value = retMap.get("data");
					if(value != null && value instanceof SuperVO) {
						SuperVO vo = (SuperVO) value;
						map.put(itemkey, vo.getAttributeValue(nameFieldCode.toString()));
					}
				}
			}
		}
	}
	
	/**
	 *	根据SQL查询需要打印的模板的数据
	 * 	返回打印模板的数据源
	 * @param paramVO
	 * @param uiBillTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	public static List<HashMap> getBillPrintDataMapSourceBySql(String sql,
			Object... params) {
		List<HashMap> result = null;
		result = NWDao.getInstance().queryForList(sql, HashMap.class, params);
		return result;
	}
}

package org.nw.utils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.exp.PrintTempletUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;

public class DataFormatHelper {

	public static void formatAll(List<Map<String, Object>> list, List<BillTempletBVO> billTempletBVOList) {
		DataFormatHelper.formatCheckbox(list, billTempletBVOList);
		DataFormatHelper.formatDecimal(list, billTempletBVOList);
		DataFormatHelper.formatSelect(list, billTempletBVOList);
	}

	/**
	 * 格式化小数型
	 * 
	 * @param list
	 * @param billTempletBVOList
	 */
	public static void formatDecimal(List<Map<String, Object>> list, List<BillTempletBVO> billTempletBVOList) {
		for(BillTempletBVO billTempletBVO : billTempletBVOList) {
			if(UiConstants.DATATYPE.DECIMAL.intValue() == billTempletBVO.getDatatype()) {
				// 5.5.对小数型进行处理
				// 获取小数位数
				int precision = UiTempletUtils.getPrecision(billTempletBVO.getReftype());
				billTempletBVO.setPrecision(precision);

				// 获取0是否显示为空 2,,,,,,Y
				boolean isZeroToNull = UiTempletUtils.isZeroToNull(billTempletBVO.getReftype());
				billTempletBVO.setZeroToNull(isZeroToNull);

				// 负数是否显示为红色
				boolean isNegativeNumberToRed = UiTempletUtils.isNegativeNumberToRed(billTempletBVO.getReftype());

				DecimalFormat df = new DecimalFormat();
				df.setMinimumFractionDigits(precision);
				df.setMaximumFractionDigits(precision);
				df.setGroupingUsed(false);

				String key = billTempletBVO.getItemkey();
				for(Map<String, Object> map : list) {
					Object value = map.get(key);
					if(value != null) {
						// 格式化
						Double d = Double.valueOf(value.toString());
						if(isZeroToNull && (d - 0.0 < 0.00000001)) {
							value = null;
						} else {
							value = df.format(d);
						}
						// if(isNegativeNumberToRed && d < 0) {
						// value = "<span style='color:red;'>" +
						// value.toString() + "</span>";
						// }
					}
					map.put(key, value);
				}

			}
		}
	}

	/**
	 * 格式化下拉框，转为名称
	 * 
	 * @param list
	 * @param billTempletBVOList
	 */
	public static void formatSelect(List<Map<String, Object>> list, List<BillTempletBVO> billTempletBVOList) {
		for(BillTempletBVO billTempletBVO : billTempletBVOList) {
			if(UiConstants.DATATYPE.SELECT.intValue() == billTempletBVO.getDatatype()) {
				// 5.4.对下拉框进行特殊处理
				PrintTempletUtils.convertSelectName(list, billTempletBVO);
			}
		}
	}

	/**
	 * 格式化checkbox，转为是和否
	 * 
	 * @param list
	 * @param billTempletBVOList
	 */
	public static void formatCheckbox(List<Map<String, Object>> list, List<BillTempletBVO> billTempletBVOList) {
		for(BillTempletBVO billTempletBVO : billTempletBVOList) {
			if(UiConstants.DATATYPE.CHECKBOX.intValue() == billTempletBVO.getDatatype()) {
				// 对 checkbox 进行特殊处理
				String key = billTempletBVO.getItemkey();
				for(Map<String, Object> map : list) {
					Object value = map.get(key);
					if(value != null) {
						// 格式化
						if("Y".equals(value)) {
							value = "是";
						} else {
							value = "否";
						}
					} else {
						value = "否";
					}
					map.put(key, value);
				}
			}
		}
	}

	/**
	 * 格式化小数型
	 * 
	 * @param list
	 * @param uiBillTempletVO
	 * @param tabCodes
	 */
	public static void formatDecimal(List<Map<String, Object>> list, UiBillTempletVO uiBillTempletVO, String[] tabCodes) {
		if(list != null) {
			for(Map<String, Object> map : list) {
				formatDecimal(map, uiBillTempletVO, tabCodes);
			}
		}
	}

	/**
	 * 格式化小数型
	 * 
	 * @param map
	 * @param uiBillTempletVO
	 * @param tabCodes
	 */
	public static void formatDecimal(Map<String, Object> map, UiBillTempletVO uiBillTempletVO, String[] tabCodes) {
		for(BillTempletBVO billTempletBVO : uiBillTempletVO.getFieldVOs()) {
			for(String tabCode : tabCodes) {
				if(tabCode.equals(billTempletBVO.getTable_code())) {
					if(UiConstants.DATATYPE.DECIMAL.intValue() == billTempletBVO.getDatatype()) {
						// 5.5.对小数型进行处理
						// 获取小数位数
						int precision = UiTempletUtils.getPrecision(billTempletBVO.getReftype());
						billTempletBVO.setPrecision(precision);

						// 获取0是否显示为空 2,,,,,,Y
						boolean isZeroToNull = UiTempletUtils.isZeroToNull(billTempletBVO.getReftype());
						billTempletBVO.setZeroToNull(isZeroToNull);

						// 负数是否显示为红色
						boolean isNegativeNumberToRed = UiTempletUtils.isNegativeNumberToRed(billTempletBVO
								.getReftype());

						DecimalFormat df = new DecimalFormat();
						df.setMinimumFractionDigits(precision);
						df.setMaximumFractionDigits(precision);
						df.setGroupingUsed(false);

						String key = billTempletBVO.getItemkey();
						Object value = map.get(key);
						if(value != null && StringUtils.isNotBlank(value.toString())) {
							// 格式化
							Double d = null;
							try {
								d = Double.valueOf(value.toString());
								if(isZeroToNull && (d - 0.0 < 0.00000001)) {
									value = null;
								} else {
									value = df.format(d);
								}
							} catch(NumberFormatException e) {
								// 如果数值型格式化失败，则用null
								value = null;
								e.printStackTrace();
							}
							// if(isNegativeNumberToRed && d < 0) {
							// value = "<span style='color:red;'>" +
							// value.toString() + "</span>";
							// }
						}
						map.put(key, value);
					}
				}
			}
		}
	}

	public static void formatSelect(List<Map<String, Object>> list, UiBillTempletVO uiBillTempletVO, String[] tabCodes) {
		if(list != null) {
			for(Map<String, Object> map : list) {
				formatSelect(map, uiBillTempletVO, tabCodes);
			}
		}
	}

	/**
	 * 格式化下拉框，转为名称
	 * 
	 * @param map
	 * @param uiBillTempletVO
	 * @param tabCodes
	 */
	public static void formatSelect(Map<String, Object> map, UiBillTempletVO uiBillTempletVO, String[] tabCodes) {
		for(BillTempletBVO fieldVO : uiBillTempletVO.getFieldVOs()) {
			for(String tabCode : tabCodes) {
				if(tabCode.equals(fieldVO.getTable_code())) {
					if(UiConstants.DATATYPE.SELECT.intValue() == fieldVO.getDatatype()
							|| (UiConstants.DATATYPE.REF.intValue() == fieldVO.getDatatype() && UIUtils.isMultiSelect(
									fieldVO.getReftype(), fieldVO.getDatatype()))) {
						// 5.4.对下拉框进行特殊处理
						PrintTempletUtils.convertSelectName(map, fieldVO);
					}
				}
			}
		}
	}
}

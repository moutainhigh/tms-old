package org.nw.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.dao.NWDao;
import org.nw.formula.FormulaParser;
import org.nw.formula.PrintFormulaParser;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.web.utils.WebUtils;

/**
 * 公式解析handler<br>
 * 
 * @author fangw
 */
public class FormulaHelper {
	private static final Log logger = LogFactory.getLog(FormulaHelper.class);

	/**
	 * 根据数据集和公式数组，执行公式，并返回公式结果
	 * 
	 * @param context
	 * @param formulas
	 * @param isMergeContextToResult
	 * @return
	 */
	public static List<Map<String, Object>> execFormula(
			List<Map<String, Object>> context, String[] formulas,
			boolean isMergeContextToResult) {
		if (context == null || context.size() == 0) {
			return context;
		}

		List<Map<String, Object>> list = context;

		// 拆分公式
		List<String> formulaList = new ArrayList<String>();
		for (String f : formulas) {
			if (f.contains(";")) {
				String[] arr = f.split(";");
				for (String tmp : arr) {
					if (StringUtils.isNotBlank(tmp)) {
						formulaList.add(tmp);
					}
				}
			} else {
				if (StringUtils.isNotBlank(f)) {
					formulaList.add(f);
				}
			}
		}

		long startTime = System.currentTimeMillis();
		List<Map<String, Object>> retList = null;
		// 使用WEB端公式解析器
		logger.info("使用WEB端公式解析器...");
		FormulaParser formulaParse = new FormulaParser(NWDao.getInstance().getDataSource());
		formulaParse.setFormulas(formulaList);
		formulaParse.setContext(list);
		formulaParse.setMergeContextToResult(isMergeContextToResult);// 不把返回结果与输入数据合并
		retList = formulaParse.getResult();
		long endTime = System.currentTimeMillis();
		logger.info("公式解析耗时：" + (endTime - startTime));
		return retList;
	}

	/**
	 * 根据数据集和公式数组，执行公式，并返回公式结果<br>
	 * 注：公式结果中不包含输入数据
	 * 
	 * @param context
	 * @param formulas
	 * @return
	 */
	public static List<Map<String, Object>> execFormula(
			List<Map<String, Object>> context, String[] formulas) {
		return FormulaHelper.execFormula(context, formulas, false);
	}

	/**
	 * 执行公式
	 * 
	 * @param superVOs
	 * @param formulas
	 * @param isMergeContextToResult
	 * @return
	 */
	public static List<Map<String, Object>> execFormulaForSuperVO(
			List<? extends SuperVO> superVOs, String[] formulas,
			boolean isMergeContextToResult) {
		List<Map<String, Object>> model = new ArrayList<Map<String, Object>>();
		for (SuperVO vo : superVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : vo.getAttributeNames()) {
				map.put(key, vo.getAttributeValue(key));
			}
			model.add(map);
		}
		if (formulas == null || formulas.length == 0) {
			return model;
		}
		List<Map<String, Object>> listMap = FormulaHelper.execFormula(model,
				formulas, false);
		if (isMergeContextToResult) {
			for (int i = 0; i < listMap.size(); i++) {
				listMap.get(i).putAll(model.get(i));
			}
		}
		return listMap;
	}

	/**
	 * 执行公式,默认将结果集与参数合并
	 * 
	 * @param superVOs
	 * @param formulas
	 * @return
	 */
	public static List<Map<String, Object>> execFormulaForSuperVO(
			List<? extends SuperVO> superVOs, String[] formulas) {
		return execFormulaForSuperVO(superVOs, formulas, true);
	}

	/**
	 * 执行打印公式 注：1.打印公式跟单据模板的公式不同，这里以打印单元格为单位执行;2.打印公式的结果是最后一个公式的结果
	 * 
	 * @param context
	 * @param formulas
	 * @return
	 */
	public static Object execPrintFormula(Map<String, Object> context,
			String formulaString) {
		if (context == null || context.size() == 0) {
			return null;
		}
		if (formulaString == null || formulaString.length() == 0) {
			// 这里绝对不允许空的公式，否则如果返回null，根本不知道是公式结果为null还是没公式
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("打印公式为空，不能执行！");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Print formula is empty, can not be executed!");
			}
			throw new RuntimeException("打印公式为空，不能执行！");
		}

		// 拆分公式
		List<String> formulaList = new ArrayList<String>();
		if (formulaString.contains(";")) {
			for (String f : formulaString.split(";")) {
				if (StringUtils.isNotBlank(f)) {
					formulaList.add(f);
				}
			}
		} else {
			if (StringUtils.isNotBlank(formulaString)) {
				formulaList.add(formulaString);
			}
		}

		long startTime = System.currentTimeMillis();
		Object obj = null;
		PrintFormulaParser formulaParse = new PrintFormulaParser(NWDao
				.getInstance().getDataSource());
		formulaParse.setFormulas(formulaList);
		formulaParse.setContext(context);
		obj = formulaParse.getResult();
		long endTime = System.currentTimeMillis();
		logger.debug("公式解析耗时：" + (endTime - startTime));

		return obj;
	}

	/**
	 * 执行显示公式-for 单据模板
	 * 
	 * @param context
	 * @param billTempletBVOList
	 * @param tabCodes
	 * @param extFormulas
	 * @return
	 */
	public static List<Map<String, Object>> execLoadFormula4Templet(
			List<Map<String, Object>> context,
			List<BillTempletBVO> billTempletBVOList, boolean isBody,
			String[] tabCodes, String[] extFormulas) {
		return FormulaHelper.execFormula4Templet(context, billTempletBVOList,
				isBody, tabCodes, extFormulas, false);
	}

	/**
	 * 执行编辑公式-for 单据模板 注：主要用于新增时，需要执行编辑公式而不是显示公式
	 * 
	 * @param context
	 * @param billTempletBVOList
	 * @param tabCodes
	 * @param extFormulas
	 * @return
	 */
	public static List<Map<String, Object>> execEditFormula4Templet(
			List<Map<String, Object>> context,
			List<BillTempletBVO> billTempletBVOList, boolean isBody,
			String[] tabCodes, String[] extFormulas) {
		return FormulaHelper.execFormula4Templet(context, billTempletBVOList,
				isBody, tabCodes, extFormulas, true);
	}

	/**
	 * 私有方法：执行公式-for 单据模板
	 * 
	 * @param context
	 * @param billTempletBVOList
	 * @param tabCodes
	 * @param extFormulas
	 * @param isEditFormula
	 *            是否编辑公式：true则执行编辑公式，false则执行显示公式
	 * @return
	 */
	private static List<Map<String, Object>> execFormula4Templet(
			List<Map<String, Object>> context,
			List<BillTempletBVO> billTempletBVOList, boolean isBody,
			String[] tabCodes, String[] extFormulas, boolean isEditFormula) {
		if (context == null || context.size() == 0) {
			return context;
		}

		List<String> templetFormulaList = new ArrayList<String>();
		for (BillTempletBVO billTempletBVO : billTempletBVOList) {
			// 主表可省略tabCodes参数，传null则只判断主表
			if (tabCodes == null) {
				if (billTempletBVO.getPos().intValue() != UiConstants.POS[1]) {
					if (isEditFormula) {
						if (StringUtils.isNotBlank(billTempletBVO
								.getEditformula())) {
							templetFormulaList.add(billTempletBVO
									.getEditformula());
						}
					} else {
						if (StringUtils.isNotBlank(billTempletBVO
								.getLoadformula())) {
							templetFormulaList.add(billTempletBVO
									.getLoadformula());
						}
					}
				}
			} else {
				for (String tabCode : tabCodes) {
					if (tabCode != null
							&& billTempletBVO.getTable_code() != null
							&& tabCode.equals(billTempletBVO.getTable_code())) {
						if (isEditFormula) {
							if (StringUtils.isNotBlank(billTempletBVO
									.getEditformula())) {
								templetFormulaList.add(billTempletBVO
										.getEditformula());
							}
						} else {
							if (StringUtils.isNotBlank(billTempletBVO
									.getLoadformula())) {
								templetFormulaList.add(billTempletBVO
										.getLoadformula());
							}
						}
					}
				}
			}
		}

		// 附加公式
		if (extFormulas != null) {
			for (String f : extFormulas) {
				templetFormulaList.add(f);
			}
		}
		return execFormula(context,templetFormulaList.toArray(new String[templetFormulaList.size()]),true);
	}

	/**
	 * 执行报表模板中的公式
	 * 
	 * @param context
	 * @param fieldVOs
	 * @return
	 */
	public static List<Map<String, Object>> execFormula4Report(
			List<Map<String, Object>> context, List<ReportTempletBVO> fieldVOs) {
		return execFormula4Report(context, fieldVOs, null);
	}

	/**
	 * 执行报表模板中的公式
	 * 
	 * @param context
	 * @param fieldVOs
	 * @param extFormulas
	 * @return
	 */
	public static List<Map<String, Object>> execFormula4Report(
			List<Map<String, Object>> context, List<ReportTempletBVO> fieldVOs,
			String[] extFormulas) {
		if (context == null || context.size() == 0) {
			return context;
		}

		List<String> templetFormulaList = new ArrayList<String>();
		for (ReportTempletBVO fieldVO : fieldVOs) {
			if (StringUtils.isNotBlank(fieldVO.getLoadformula())) {
				templetFormulaList.add(fieldVO.getLoadformula());
			}
		}
		// 附加公式
		if (extFormulas != null) {
			for (String f : extFormulas) {
				templetFormulaList.add(f);
			}
		}
		return execFormula(
				context,
				templetFormulaList.toArray(new String[templetFormulaList.size()]),
				true);
	}
}

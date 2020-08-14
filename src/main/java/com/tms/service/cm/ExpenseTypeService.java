package com.tms.service.cm;

import java.util.HashMap;
import java.util.List;

import org.nw.service.IToftService;

import com.tms.vo.cm.ExpenseTypeVO;

/**
 * 费用类型操作接口
 * 
 * @author xuqc
 * @date 2012-8-28 下午09:20:18
 */
public interface ExpenseTypeService extends IToftService {

	// 其他费用合计，除了运费以外的费用
	public static final String OTHER_FEE_CODE = "other_fee_code";
	public static final String OTHER_FEE_NAME = "其他费用合计";

	/**
	 * 根据名称返回VO
	 * 
	 * @param name
	 * @return
	 */
	public ExpenseTypeVO getByName(String name);

	/**
	 * 返回费用类型的所有费用
	 * 
	 * @return
	 */
	public List<ExpenseTypeVO> getAllExpenseType();

	/**
	 * 返回公司的费用类型
	 * 
	 * @param pk_corp
	 * @return
	 */
	public List<ExpenseTypeVO> getAllExpenseType(String pk_corp);

	/**
	 * 返回公司的费用类型的code的对照
	 * 
	 * @param pk_corp
	 * @return
	 */
	public HashMap<String, String> getExpenseTypeCodeMap(String pk_corp);
}

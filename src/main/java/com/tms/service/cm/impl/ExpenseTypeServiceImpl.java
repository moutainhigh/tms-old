package com.tms.service.cm.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.cm.ExpenseTypeService;
import com.tms.vo.cm.ExpenseTypeVO;

/**
 * 费用类型,集团定义了一些费用类型，公司可以使用，子公司可以定义自己的费用类型，一个公司的费用类型是集团和公司的合集
 * 
 * @author xuqc
 * @date 2012-8-28 下午09:20:48
 */
@Service
public class ExpenseTypeServiceImpl extends AbsBaseDataServiceImpl implements ExpenseTypeService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ExpenseTypeVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ExpenseTypeVO.PK_EXPENSE_TYPE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_expense_type");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_expense_type");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public ExpenseTypeVO getByName(String name) {
		String sql = "select * from ts_expense_type where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and name=?";
		List<ExpenseTypeVO> list = dao.queryForList(sql, ExpenseTypeVO.class, name);
		if(list != null && list.size() > 0) {// 通常只会返回一个
			return list.get(0);
		}
		return null;
	}

	public ExpenseTypeVO getByCode(String code) {
		String sql = "select * from ts_expense_type where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
		List<ExpenseTypeVO> list = dao.queryForList(sql, ExpenseTypeVO.class, code);
		if(list != null && list.size() > 0) {// 通常只会返回一个
			return list.get(0);
		}
		return null;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by code";
		}
		return orderBy;
	}

	/**
	 * 这里使用缓存，这个数据并不常更新
	 */
	public List<ExpenseTypeVO> getAllExpenseType() {
		return getAllExpenseType(WebUtils.getLoginInfo().getPk_corp());
	}

	public List<ExpenseTypeVO> getAllExpenseType(String pk_corp) {
		return NWDao.getInstance().queryForListWithCache(
				"select * from ts_expense_type where (pk_corp=? or pk_corp=?) and isnull(dr,0)=0", ExpenseTypeVO.class,
				pk_corp, Constants.SYSTEM_CODE);
	}

	public HashMap<String, String> getExpenseTypeCodeMap(String pk_corp) {
		HashMap<String, String> expenseTypeCodeMap = new HashMap<String, String>();
		List<ExpenseTypeVO> list = getAllExpenseType(pk_corp);
		if(list != null) {
			for(ExpenseTypeVO etVO : list) {
				expenseTypeCodeMap.put(etVO.getCode(), etVO.getPk_expense_type());
			}
		}
		return expenseTypeCodeMap;
	}

	public String getCodeFieldCode() {
		return ExpenseTypeVO.CODE;
	}
	
	@Override
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String superCondition = super.buildLoadDataCondition(params, paramVO, templetVO);
		String corpCondition = CorpHelper.getCurrentCorpWithChildrenAndGroup();
		return superCondition + " AND " + corpCondition;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos != null && cvos.length > 0)
			for(CircularlyAccessibleValueObject cvo : cvos) {
				ExpenseTypeVO superVO = (ExpenseTypeVO) cvo;
//				if(superVO.getStatus() == VOStatus.UPDATED || superVO.getStatus() == VOStatus.UPDATED){
//					if(superVO.getPk_corp().equals("0001")){
//						if(WebUtils.getLoginInfo() != null && !WebUtils.getLoginInfo().getPk_corp().equals("0001")){
//							throw new BusiException("非集团用户不允许修改集团数据！");
//						}
//					}
//				}
				if(superVO.getStatus() == VOStatus.DELETED) {// 删除时检验是否被引用
					if(checkBeforeDelete(superVO.getTableName(), superVO.getPrimaryKey())) {
						throw new BusiException("编码为[?]的记录已经被引用，不能删除！",superVO.getCode());
					}
				}
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#buildOperatorColumn(org.nw.jf.
	 * vo.UiBillTempletVO)
	 */
	protected void buildOperatorColumn(UiBillTempletVO uiBillTempletVO) {

	}
}

/**
 * 
 */
package com.tms.service.cm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.cm.CartypeTonnageService;
import com.tms.vo.cm.CartypeTonnageVO;

/**
 * 车型吨位换算,集团会定义，公司也可以定义，如果公司已经定义，那么读取公司的，否则读取集团的
 * 
 * @author xuqc
 * @Date 2015年6月1日 下午10:15:04
 *
 */
@Service
public class CartypeTonnageServiceImpl extends AbsBaseDataServiceImpl implements CartypeTonnageService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, CartypeTonnageVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, CartypeTonnageVO.PK_CARTYPE_TONNAGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_cartype_tonnage");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_cartype_tonnage");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public Map<String, List<CartypeTonnageVO>> getCartypeTonnageVOMap() {
		if(WebUtils.getLoginInfo() != null) {
			return getCartypeTonnageVOMap(WebUtils.getLoginInfo().getPk_corp());
		}
		return getCartypeTonnageVOMap(null);

	}

	/**
	 * 返回车型吨位的对照信息，并根据费用类型分组，并且件数是从大到小排序
	 * 
	 * @return
	 */
	public Map<String, List<CartypeTonnageVO>> getCartypeTonnageVOMap(String pk_corp) {
		Map<String, List<CartypeTonnageVO>> map = new HashMap<String, List<CartypeTonnageVO>>();
		// 先查询公司的,如果不存在，查询集团的，公司的记录优先
		if(pk_corp == null) {
			pk_corp = Constants.SYSTEM_CODE;
		}
		CartypeTonnageVO[] vos = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(CartypeTonnageVO.class,
				"pk_corp=? order by num desc", pk_corp);
		if(vos == null || vos.length == 0) {
			vos = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(CartypeTonnageVO.class,
					"pk_corp=? order by num desc", Constants.SYSTEM_CODE);
		}
		if(vos != null && vos.length > 0) {
			for(CartypeTonnageVO vo : vos) {
				List<CartypeTonnageVO> list = map.get(vo.getPk_expense_type());
				if(list == null) {
					list = new ArrayList<CartypeTonnageVO>();
					map.put(vo.getPk_expense_type(), list);
				}
				list.add(vo);
			}
		}
		return map;
	}

	
	/**
	 * 返回车型吨位的对照信息，并根据费用类型分组，并且件数是从大到小排序，增加应收，应付区分
	 * 
	 * @return
	 */
	public Map<String, List<CartypeTonnageVO>> getCartypeTonnageVOMap(String pk_corp,int type) {
		Map<String, List<CartypeTonnageVO>> map = new HashMap<String, List<CartypeTonnageVO>>();
		// 先查询公司的,如果不存在，查询集团的，公司的记录优先
		if(pk_corp == null) {
			pk_corp = Constants.SYSTEM_CODE;
		}
		//将按数量排序改成按吨位排序2015-11-6 Jonathan
		CartypeTonnageVO[] vos = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(CartypeTonnageVO.class,
				"pk_corp=? and def1=? order by weight desc", pk_corp,type);
		if(vos == null || vos.length == 0) {
			vos = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(CartypeTonnageVO.class,
					"pk_corp=? and  def1=? order by weight desc", Constants.SYSTEM_CODE,type);
		}
		if(vos != null && vos.length > 0) {
			for(CartypeTonnageVO vo : vos) {
				List<CartypeTonnageVO> list = map.get(vo.getPk_expense_type());
				if(list == null) {
					list = new ArrayList<CartypeTonnageVO>();
					map.put(vo.getPk_expense_type(), list);
				}
				list.add(vo);
			}
		}
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#buildOperatorColumn(org.nw.jf.
	 * vo.UiBillTempletVO)
	 */
	@Override
	protected void buildOperatorColumn(UiBillTempletVO uiBillTempletVO) {

	}
}

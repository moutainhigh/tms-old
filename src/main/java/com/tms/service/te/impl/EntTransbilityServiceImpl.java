package com.tms.service.te.impl;

import java.util.ArrayList;
import java.util.List;

import org.nw.dao.NWDao;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.springframework.stereotype.Service;

import com.tms.service.te.EntTransbilityService;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;

/**
 * 委托单运力信息的维护接口，在“异常跟踪”模块中维护
 * 
 * @author xuqc
 * @date 2013-8-25 上午09:56:22
 */
@Service
public class EntTransbilityServiceImpl extends AbsToftServiceImpl implements EntTransbilityService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, EntTransbilityBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, EntTransbilityBVO.PK_ENT_TRANS_BILITY_B);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_transbility_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_transbility_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	//yaojiie 2015 1 15 保存页面传入的信息，并删除本地原有的运力信息
	// 当页面选择一条委托单时，查出这个批次下所有单据的车辆，统一进行运力信息的更新
	public void saveTransbility(AggregatedValueObject billVO,String pk_entrust) {
		EntTransbilityBVO[] transbilityBVOs = (EntTransbilityBVO[]) billVO.getChildrenVO();
		EntrustVO[] parentVOs = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "pk_entrust=?",pk_entrust);		
				
		EntrustVO[] entrustVOs = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "lot=?",parentVOs[0].getLot());
		String[] pk_entrusts = new String[entrustVOs.length];
		for(int i=0;i<entrustVOs.length;i++){
			pk_entrusts[i] = entrustVOs[i].getPk_entrust();
		}
		String cond = NWUtils.buildConditionString(pk_entrusts);
		EntTransbilityBVO[] oldTransbilityBVOs = dao.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust in "+cond);
		List<SuperVO>  toBeUpDate = new ArrayList<SuperVO>();
		for(EntTransbilityBVO oldTransbilityBVO : oldTransbilityBVOs){
			oldTransbilityBVO.setStatus(VOStatus.DELETED);
			toBeUpDate.add(oldTransbilityBVO);
		}
		
		if(transbilityBVOs != null && transbilityBVOs.length > 0){
			for(EntTransbilityBVO transbilityBVO : transbilityBVOs){
				for(EntrustVO entrustVO : entrustVOs){
					EntTransbilityBVO newTransbilityBVO = (EntTransbilityBVO) transbilityBVO.clone();
					newTransbilityBVO.setStatus(VOStatus.NEW);
					newTransbilityBVO.setPk_ent_trans_bility_b(null);
					NWDao.setUuidPrimaryKey(newTransbilityBVO);
					newTransbilityBVO.setPk_entrust(entrustVO.getPk_entrust());
					newTransbilityBVO.setLot(entrustVO.getLot());
					toBeUpDate.add(newTransbilityBVO);
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpDate);
	}
}

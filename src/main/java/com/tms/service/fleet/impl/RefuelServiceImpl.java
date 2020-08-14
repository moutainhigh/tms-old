package com.tms.service.fleet.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.RefuelService;
import com.tms.vo.base.FuelCardBVO;
import com.tms.vo.base.FuelCardVO;
import com.tms.vo.fleet.RefuelVO;

@Service
public class RefuelServiceImpl extends TMSAbsBillServiceImpl implements RefuelService {

	public String getBillType() {
		return BillTypeConst.REF;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.REF_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, RefuelVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, RefuelVO.PK_REFUEL);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_refuel");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_refuel");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				}
			}
		}
		return templetVO;
	}

	@Override
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);
		RefuelVO refuelVO = (RefuelVO) billVO.getParentVO();
		if(StringUtils.isBlank(refuelVO.getPk_fuelcard())){
			return;
		}
		if(StringUtils.isBlank(refuelVO.getPk_refuel())){
			NWDao.setUuidPrimaryKey(refuelVO);;
		}
		FuelCardVO fuelCardVO = dao.queryByCondition(FuelCardVO.class, "pk_fuelcard=?", refuelVO.getPk_fuelcard());
		if(fuelCardVO == null){
			throw new BusiException("加油卡不存在！");
		}
		//新建
		FuelCardBVO cardBVO = new FuelCardBVO();
		if(refuelVO.getStatus() == VOStatus.NEW){
			cardBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(cardBVO);
			cardBVO.setPk_fuelcard(fuelCardVO.getPk_fuelcard());
			fuelCardVO.setAmount((fuelCardVO.getAmount() == null ? UFDouble.ZERO_DBL : fuelCardVO.getAmount())
					.sub(refuelVO.getAmount() == null ?  UFDouble.ZERO_DBL : refuelVO.getAmount()));
		}else{
			cardBVO = dao.queryByCondition(FuelCardBVO.class, "pk_refuel=?", refuelVO.getPk_refuel());
			if(cardBVO == null){
				cardBVO = new FuelCardBVO();
				cardBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(cardBVO);
				cardBVO.setPk_fuelcard(fuelCardVO.getPk_fuelcard());
				cardBVO.setPk_refuel(refuelVO.getPk_refuel());
				fuelCardVO.setAmount((fuelCardVO.getAmount() == null ? UFDouble.ZERO_DBL : fuelCardVO.getAmount())
						.sub(refuelVO.getAmount() == null ?  UFDouble.ZERO_DBL : refuelVO.getAmount()));
			}else{
				cardBVO.setStatus(VOStatus.UPDATED);
				fuelCardVO.setAmount((fuelCardVO.getAmount() == null ? UFDouble.ZERO_DBL : fuelCardVO.getAmount())
						.sub(refuelVO.getAmount() == null ?  UFDouble.ZERO_DBL : refuelVO.getAmount())
						.add(cardBVO.getAmount() == null ?  UFDouble.ZERO_DBL : cardBVO.getAmount()));
			}
		}
		cardBVO.setPk_refuel(refuelVO.getPk_refuel());
		cardBVO.setAmount(refuelVO.getAmount());
		cardBVO.setOperator(WebUtils.getLoginInfo().getPk_user());
		cardBVO.setOperat_date(new UFDateTime(new Date()));
		cardBVO.setOperation_type(0);
		cardBVO.setMemo(refuelVO.getMemo());
		cardBVO.setSystem_create(UFBoolean.TRUE);
		dao.saveOrUpdate(cardBVO);
		fuelCardVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(fuelCardVO);
	}
	
	
	@Override
	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		RefuelVO refuelVO = (RefuelVO) billVO.getParentVO();
		if(StringUtils.isBlank(refuelVO.getPk_fuelcard())){
			return;
		}
		FuelCardVO fuelCardVO = dao.queryByCondition(FuelCardVO.class, "pk_fuelcard=?", refuelVO.getPk_fuelcard());
		if(fuelCardVO == null){
			return;
		}
		FuelCardBVO cardBVO = dao.queryByCondition(FuelCardBVO.class, "pk_refuel=?", refuelVO.getPk_refuel());
		if(cardBVO  == null){
			return;
		}
		dao.delete(cardBVO);
		fuelCardVO.setAmount((fuelCardVO.getAmount() == null ? UFDouble.ZERO_DBL : fuelCardVO.getAmount())
				.add(refuelVO.getAmount() == null ? UFDouble.ZERO_DBL : refuelVO.getAmount()));
		fuelCardVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(fuelCardVO);
	}
}

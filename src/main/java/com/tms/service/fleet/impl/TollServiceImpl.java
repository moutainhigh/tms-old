package com.tms.service.fleet.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.TollService;
import com.tms.vo.base.ETCBVO;
import com.tms.vo.base.ETCVO;
import com.tms.vo.fleet.ExAggTollVO;
import com.tms.vo.fleet.TollBVO;
import com.tms.vo.fleet.TollVO;

//yaojiie 2015 12 16 路桥费管理
@Service
public class TollServiceImpl extends TMSAbsBillServiceImpl implements TollService {

	public String getBillType() {
		return BillTypeConst.TOLL;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.TOLL_CONFIRM;
	}

	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggTollVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggTollVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, TollVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, TollVO.PK_TOLL);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggTollVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, TollBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, TollBVO.PK_TOLL);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_toll_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_toll_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO};
			billInfo.setChildrenVO(childrenVO);
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
			}else if(fieldVO.getPos().intValue() == UiConstants.POS[1]){
				if (fieldVO.getTable_code().equals("ts_toll_b")){
					if (fieldVO.getItemkey().equals("amount")) { // 更新件数时，更新其他信息，及表头统计信息
						fieldVO.setUserdefine1("updateHeaderCostAmount(record)");
					}
				}
			}
		}
		return templetVO;
	}
	
	
	@Override
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		TollVO tollVO = (TollVO) billVO.getParentVO();
		if(StringUtils.isBlank(tollVO.getPk_etc())){
			return;
		}
		if(StringUtils.isBlank(tollVO.getPk_toll())){
			NWDao.setUuidPrimaryKey(tollVO);
		}
		ETCVO etcvo = dao.queryByCondition(ETCVO.class, "pk_etc=?", tollVO.getPk_etc());
		if(etcvo == null){
			throw new BusiException("ETC卡不存在！");
		}
		ETCBVO etcbvo = dao.queryByCondition(ETCBVO.class, "pk_toll=?", tollVO.getPk_toll());
		if(etcbvo == null){
			etcbvo = new ETCBVO();
			etcbvo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(etcbvo);
			etcbvo.setPk_etc(etcvo.getPk_etc());
			etcbvo.setPk_toll(tollVO.getPk_toll());
			etcvo.setAmount((etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
					.sub(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount()));
		}else{
			etcbvo.setStatus(VOStatus.UPDATED);
			etcvo.setAmount((etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
					.add(etcbvo.getAmount() == null ? UFDouble.ZERO_DBL : etcbvo.getAmount())
					.sub(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount()));
		}
		etcbvo.setAmount(tollVO.getCost_amount());
		etcbvo.setOperator(WebUtils.getLoginInfo().getPk_user());
		etcbvo.setOperation_type(0);
		etcbvo.setOperat_date(new UFDateTime(new Date()));
		etcbvo.setMemo(tollVO.getMemo());
		etcbvo.setSystem_create(UFBoolean.TRUE);
		dao.saveOrUpdate(etcbvo);
		etcvo.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(etcvo);
	}
	
	
	@Override
	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		TollVO tollVO = (TollVO) billVO.getParentVO();
		if(StringUtils.isBlank(tollVO.getPk_etc())){
			return;
		}
		ETCVO etcvo = dao.queryByCondition(ETCVO.class, "pk_etc=?", tollVO.getPk_etc());
		if(etcvo == null){
			return;
		}
		ETCBVO etcbvo = dao.queryByCondition(ETCBVO.class, "pk_toll=?", tollVO.getPk_toll());
		if(etcbvo == null){
			return;
		}
		dao.delete(etcbvo);
		etcvo.setStatus(VOStatus.UPDATED);
		etcvo.setAmount((etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
				.add(etcbvo.getAmount() == null ? UFDouble.ZERO_DBL : etcbvo.getAmount()));
		dao.saveOrUpdate(etcvo);
	}
	
}

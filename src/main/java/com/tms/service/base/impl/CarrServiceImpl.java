package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.utils.AreaHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.service.base.CarrService;
import com.tms.service.base.TransTypeService;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CarrRateVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.TransTypeVO;

/**
 * 承运商档案
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:21:54
 */
@Service
public class CarrServiceImpl extends AbsBaseDataServiceImpl implements CarrService {

	@Autowired
	private TransTypeService transTypeService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CarrierVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CarrierVO.PK_CARRIER);
			billInfo.setParentVO(vo);

			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, CarrRateVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, CarrRateVO.PK_CARRIER);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_carr_rate");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_carr_rate");

			CircularlyAccessibleValueObject[] childrenVO = { childVO2 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0] && fieldVO.getItemkey().equals("pk_city")) {
				fieldVO.setUserdefine3("area_level=5");
			}
		}
		return templetVO;
	}

	public String getCodeFieldCode() {
		return CarrierVO.CARR_CODE;
	}

	public CarrierVO getByCode(String code) {
		return dao.queryByCondition(CarrierVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carr_code=?",
				code);
	}

	public String getCheckHead(String pk_carrier) {
		CarrierVO vo = dao.queryByCondition(CarrierVO.class, "pk_carrier=?", pk_carrier);
		if(vo == null) {
			logger.warn("承运商档案已经被删除，pk_carrier:" + pk_carrier);
			return null;
		}
		return vo.getBilling_payable();
	}
	
	public String getDefaultCheckType(String pk_carrier) {
		CarrierVO vo = dao.queryByCondition(CarrierVO.class, "pk_carrier=?", pk_carrier);
		if(vo == null) {
			logger.warn("承运商档案已经被删除，pk_carrier:" + pk_carrier);
			return null;
		}
		String defaultCheckType = vo.getPk_billing_type();
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With(nolock) "
				+ "LEFT JOIN nw_data_dict With(nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,defaultCheckType);
	}
	
	public UFDouble getFeeRate(String pk_carrier,String pk_trans_type,
			String start_area,String end_area) {
		UFDouble rate = UFDouble.ONE_DBL;
		if(StringUtils.isBlank(pk_trans_type)){
			return rate;
		}
		if(StringUtils.isBlank(pk_carrier)){
			TransTypeVO transTypeVO = dao.queryByCondition(TransTypeVO.class, "pk_trans_type=?", pk_trans_type);
			return transTypeVO == null ? rate : (transTypeVO.getRate() == null ? rate : transTypeVO.getRate());
		}
		List<AreaVO> startAreaVOs = AreaHelper.getCurrentAreaVOWithParents(start_area);
		String startAreas = "";
		if(startAreaVOs == null || startAreaVOs.size() == 0){
			startAreas = "('')";
		}else{
			for(AreaVO areaVO : startAreaVOs) {
				startAreas += "'";
				startAreas += areaVO.getPk_area();
				startAreas += "',";
			}
			if(startAreas.length() > 0) {
				startAreas = "(" + startAreas.substring(0, startAreas.length() - 1) + ")";
			}
		}
		List<AreaVO> endAreaVOs = AreaHelper.getCurrentAreaVOWithParents(end_area);
		String endAreas = "";
		if(endAreaVOs == null || endAreaVOs.size() == 0){
			endAreas = "('')";
		}else{
			for(AreaVO areaVO : endAreaVOs) {
				endAreas += "'";
				endAreas += areaVO.getPk_area();
				endAreas += "',";
			}
			if(endAreas.length() > 0) {
				endAreas = "(" + endAreas.substring(0, endAreas.length() - 1) + ")";
			}
		}

		String sql = "SELECT * FROM ts_carr_rate WITH(NOLOCK) "
				+ " WHERE isnull(dr,0)=0 AND pk_carrier = ? AND pk_trans_type = ? "
				+ " AND start_area IN " + startAreas+ " AND end_area IN " + endAreas;
		List<CarrRateVO> rateVOs = dao.queryForList(sql, CarrRateVO.class, pk_carrier,pk_trans_type);
		if(rateVOs == null || rateVOs.size() == 0){
			TransTypeVO transTypeVO = dao.queryByCondition(TransTypeVO.class, "pk_trans_type=?", pk_trans_type);
			return transTypeVO == null ? rate : (transTypeVO.getRate() == null ? rate : transTypeVO.getRate());
		}
		//1，找出起始区域等级最高的rate (其实区域可能为空哦)
		AreaVO hightStratArea = null;
		if(startAreaVOs != null && startAreaVOs.size() >= 0){
			for(AreaVO startArea : startAreaVOs){
				if(hightStratArea == null){
					hightStratArea = startArea;
				}else{
					if(startArea.getArea_level() >= hightStratArea.getArea_level()){
						hightStratArea = startArea;
					}
				}
			}
		}
		//取出对应的rate
		List<CarrRateVO> stratRates = new ArrayList<CarrRateVO>();
		for(CarrRateVO rateVO : rateVOs){
			if(hightStratArea == null){
				if(StringUtils.isBlank(rateVO.getStart_area())){
					stratRates.add(rateVO);
				}
			}else{
				if(hightStratArea.getPk_area().equals(rateVO.getStart_area())){
					stratRates.add(rateVO);
				}
			}
		}
		if(stratRates.size() == 1){
			return stratRates.get(0).getRate() == null ? UFDouble.ONE_DBL : stratRates.get(0).getRate();
		}
		//2，找到目的区域，等级最高的rate
		AreaVO hightEndArea = null;
		if(endAreaVOs != null && endAreaVOs.size() >= 0){
			for(AreaVO endArea : endAreaVOs){
				if(hightEndArea == null){
					hightEndArea = endArea;
				}else{
					if(endArea.getArea_level() >= hightEndArea.getArea_level()){
						hightEndArea = endArea;
					}
				}
			}
		}
		for(CarrRateVO rateVO : stratRates){
			if(hightEndArea == null){
				if(StringUtils.isBlank(rateVO.getEnd_area())){
					return rateVO.getRate() == null ? UFDouble.ONE_DBL : rateVO.getRate();
				}
			}else{
				if(hightEndArea.getPk_area().equals(rateVO.getEnd_area())){
					return rateVO.getRate() == null ? UFDouble.ONE_DBL : rateVO.getRate();
				}
			}
		}
		return rate;
	}
	
	
	@Override
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String superCond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier())){
			superCond += " and pk_carrier='" +  WebUtils.getLoginInfo().getPk_carrier() + "'";
		}
		return superCond;
	}
}

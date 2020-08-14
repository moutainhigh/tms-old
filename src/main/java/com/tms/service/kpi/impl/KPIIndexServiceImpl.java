package com.tms.service.kpi.impl;

import java.util.ArrayList;
import java.util.List;

import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.trade.pub.IExAggVO;
import org.springframework.stereotype.Service;

import com.tms.service.kpi.KPIIndexService;
import com.tms.vo.kpi.ExAggKPIIndexVO;
import com.tms.vo.kpi.KPIIndexBVO;
import com.tms.vo.kpi.KPIIndexVO;

@Service
public class KPIIndexServiceImpl extends AbsToftServiceImpl implements KPIIndexService {

	private AggregatedValueObject billInfo;
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggKPIIndexVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggKPIIndexVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, KPIIndexVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, KPIIndexVO.PK_KPI_INDEX);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggKPIIndexVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, KPIIndexBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, KPIIndexBVO.PK_KPI_INDEX);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_kpi_index_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_kpi_index_b");
			
			CircularlyAccessibleValueObject[] childrenVO = {childVO};
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO){
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] cvos = ((IExAggVO) billVO).getAllChildrenVO();
		//检查明细编码是否充分
		if(cvos != null && cvos.length > 0){
			List<Integer> codes = new ArrayList<Integer>();
			for(int i=0;i<cvos.length;i++){
				if(((KPIIndexBVO)cvos[i]).getStatus() != VOStatus.DELETED){
					codes.add(((KPIIndexBVO)cvos[i]).getCode());
				}
			}
			if(codes.size() > 1){
				for(int i=0; i<codes.size()-1;i++){
					for(int j=i+1;j<codes.size();j++){
						if(codes.get(i).equals(codes.get(j)) ){
							throw new BusiException("指标明细编码不允许重复！");
						}
					}
				}
			}
		}
		
		//生成行号
		int index = 0;
		if(cvos != null && cvos.length > 0){
			for(CircularlyAccessibleValueObject cvo : cvos){
				if(cvo instanceof KPIIndexBVO ){
					index += 10;
					KPIIndexBVO kPIIndexBVO = (KPIIndexBVO)cvo;
					kPIIndexBVO.setSerialno(index);
				}
			}
		}
	}

}

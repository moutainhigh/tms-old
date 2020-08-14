package com.tms.web.cm;

import java.util.ArrayList;
import java.util.List;

import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.vo.cm.ContractBVO;

/**
 * 承运商报价
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/cm/caq")
public class CarrQuoteController extends AbsToftController {

	@Autowired
	private ContractService contractService;

	public ContractService getService() {
		return contractService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ContractBVO[] contractBVOs = (ContractBVO[])billVO.getChildrenVO();
		List<ContractBVO> saveContractBVOs = new ArrayList<ContractBVO>();
		for(ContractBVO contractBVO : contractBVOs){
			//删除的不需要管他
			if(contractBVO.getStatus() != VOStatus.DELETED){
				saveContractBVOs.add(contractBVO);
			}
		}
		if(saveContractBVOs.size() > 0){
			CMUtils.checkRepeat(CMUtils.splicString(saveContractBVOs.toArray(new ContractBVO[saveContractBVOs.size()])));
		}
	}

}

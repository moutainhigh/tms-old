package com.tms.web.base;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.impl.SupplierService;
import com.tms.vo.base.SuppAddrVO;

/**
 * 供应商
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:17:47
 */
@Controller
@RequestMapping(value = "/base/supplier")
public class SupplierController extends AbsToftController {

	@Autowired
	private SupplierService supplierService;

	public SupplierService getService() {
		return supplierService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		// 收发货地址不能相同
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos != null) {
			Map<String, Integer> addrMap = new HashMap<String, Integer>();
			int index = 1;
			for(CircularlyAccessibleValueObject cvo : cvos) {
				SuppAddrVO addrVO = (SuppAddrVO) cvo;
				if(StringUtils.isBlank(addrVO.getPk_address())) {
					continue;
				}
				Integer row = addrMap.get(addrVO.getPk_address());
				if(row != null) {
					throw new BusiException("第[?]行的地址和第[?]行的地址相同，请重新修改！",index+"",row+"");
				} else {
					addrMap.put(addrVO.getPk_address(), index);
				}
				index++;
			}
		}
	}
}

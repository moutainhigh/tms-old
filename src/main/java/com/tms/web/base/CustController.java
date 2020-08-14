package com.tms.web.base;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.exception.BusiException;
import org.nw.utils.ImageUtil;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.service.base.CustService;
import com.tms.vo.base.CustAddrVO;
import com.tms.vo.base.CustRateVO;
import com.tms.vo.base.ExAggCustVO;

/**
 * 客户信息管理
 * 
 * @author xuqc
 * @date 2012-7-17 下午11:14:50
 */
@Controller
@RequestMapping(value = "/base/cust")
public class CustController extends AbsToftController {

	@Autowired
	private CustService custService;

	public CustService getService() {
		return custService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ExAggCustVO aggVO = (ExAggCustVO) billVO;
		// 收发货地址不能相同
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO("ts_cust_addr");
		if(cvos != null) {
			Map<String, Integer> addrMap = new HashMap<String, Integer>();
			int index = 1;
			for(CircularlyAccessibleValueObject cvo : cvos) {
				CustAddrVO addrVO = (CustAddrVO) cvo;
				if(StringUtils.isBlank(addrVO.getPk_address())) {
					continue;
				}
				Integer row = addrMap.get(addrVO.getPk_address());
				if(row != null) {
					throw new BusiException("第[?行的地址和第[?]行的地址相同，请重新修改！",index+"",row+"");
				} else {
					addrMap.put(addrVO.getPk_address(), index);
				}
				index++;
			}
		}

		// 运输方式不能相同
//		CircularlyAccessibleValueObject[] cvos1 = aggVO.getTableVO("ts_cust_rate");
//		if(cvos1 != null) {
//			Map<String, Integer> typeMap = new HashMap<String, Integer>();
//			int index = 1;
//			for(CircularlyAccessibleValueObject cvo : cvos1) {
//				CustRateVO rateVO = (CustRateVO) cvo;
//				if(StringUtils.isBlank(rateVO.getPk_trans_type())) {
//					continue;
//				}
//				Integer row = typeMap.get(rateVO.getPk_trans_type());
//				if(row != null) {
//					throw new BusiException("第[?]行的运输方式和第[?]行的运输方式相同，请重新修改！",index+"",row+"");
//				} else {
//					typeMap.put(rateVO.getPk_trans_type(), index);
//				}
//				index++;
//			}
//		}
	}
	
	protected String getUploadField() {
		return "photo";
	}
	
	protected String getOtherUseURL(){
		return getServletContext().getRealPath("/") + "\\images\\cust_photo";
	}
	
}

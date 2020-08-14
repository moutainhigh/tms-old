package com.tms.web.wh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.wh.OutstorageService;
import com.tms.vo.wh.OutstorageBVO;
import com.tms.vo.wh.OutstorageVO;

/**
 * 出库单
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:37:28
 */
@Controller
@RequestMapping(value = "/wh/outstorage")
public class OutstorageController extends AbsBillController {

	@Autowired
	private OutstorageService outstorageService;

	public OutstorageService getService() {
		return outstorageService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		// 检查表头的总接收量是否等于表体的总接收量的和
		OutstorageVO parentVO = (OutstorageVO) billVO.getParentVO();
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		UFDouble order_count = new UFDouble(0);
		UFDouble volume_count = new UFDouble(0);
		UFDouble weight_count = new UFDouble(0);
		if(cvos != null && cvos.length > 0) {
			for(CircularlyAccessibleValueObject cvo : cvos) {
				OutstorageBVO childVO = (OutstorageBVO) cvo;
				if(childVO.getStatus() != VOStatus.DELETED) {
					if(childVO.getOrder_count() != null) {
						order_count = order_count.add(childVO.getOrder_count());
					}
					if(childVO.getVolume() != null) {
						volume_count = volume_count.add(childVO.getVolume());
					}
					if(childVO.getWeight() != null) {
						weight_count = weight_count.add(childVO.getWeight());
					}
				}
			}
		}
		if(parentVO.getOrder_count() != null) {
			if(parentVO.getOrder_count().doubleValue() != order_count.doubleValue()) {
				throw new BusiException("表体订单数量的和不等于表头的总订单数量！");
			}
		}
		if(parentVO.getWeight_count() != null) {
			if(parentVO.getWeight_count().doubleValue() != weight_count.doubleValue()) {
				throw new BusiException("表体订单重量的和不等于表头的总重量！");
			}
		}
		if(parentVO.getVolume_count() != null) {
			if(parentVO.getVolume_count().doubleValue() != volume_count.doubleValue()) {
				throw new BusiException("表体订单体积的和不等于表头的总体积！");
			}
		}
	}

	/**
	 * 关闭按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/close.json")
	@ResponseBody
	public Map<String, Object> close(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new RuntimeException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().close(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量关闭时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未关闭成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
}

package com.tms.web.wh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.constants.TabcodeConst;
import com.tms.service.wh.ReceiptService;
import com.tms.vo.base.GoodsVO;
import com.tms.vo.wh.ExAggInstorageVO;
import com.tms.vo.wh.InstorageBVO;
import com.tms.vo.wh.InstorageVO;

/**
 * 收货
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:37:28
 */
@Controller
@RequestMapping(value = "/wh/receipt")
public class ReceiptController extends AbsBillController {

	@Autowired
	private ReceiptService receiptService;

	public ReceiptService getService() {
		return receiptService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		// 检查表头的总接收量是否等于表体的总接收量的和
		InstorageVO parentVO = (InstorageVO) billVO.getParentVO();
		if(parentVO.getVbillstatus().intValue() != BillStatus.INSTO_NEW
				&& parentVO.getVbillstatus().intValue() != BillStatus.INSTO_PART_REC) {
			throw new BusiException("必须是[新建]和[部分收货]的入库单才能进行收货！");
		}
		ExAggInstorageVO aggVO = (ExAggInstorageVO) billVO;
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO(TabcodeConst.TS_INSTORAGE_B);
		UFDouble expect_count = new UFDouble(0);
		UFDouble volume_count = new UFDouble(0);
		UFDouble weight_count = new UFDouble(0);
		if(cvos != null && cvos.length > 0) {
			int index = 1;
			for(CircularlyAccessibleValueObject cvo : cvos) {
				InstorageBVO childVO = (InstorageBVO) cvo;
				if(childVO.getOrder_count() != null) {
					expect_count = expect_count.add(childVO.getOrder_count());
				}
				if(childVO.getVolume() != null) {
					volume_count = volume_count.add(childVO.getVolume());
				}
				if(childVO.getWeight() != null) {
					weight_count = weight_count.add(childVO.getWeight());
				}
				// 每一行的接收量不能大于订单量减去已接收量
				double order_count = childVO.getOrder_count() == null ? 0 : childVO.getOrder_count().doubleValue();// 订单量
				double accepted_count = childVO.getAccepted_count() == null ? 0 : childVO.getAccepted_count()
						.doubleValue();// 已接收量
				double accept_count = childVO.getAccept_count() == null ? 0 : childVO.getAccept_count().doubleValue();// 当前接收量
				if(accept_count > (order_count - accepted_count)) {
					throw new BusiException("第[?]行的接收量不能大于订单量减去已接收量！",index+"");
				}
				index++;
			}
		}
		if(parentVO.getWeight_count() != null) {
			if(parentVO.getWeight_count().doubleValue() != weight_count.doubleValue()) {
				throw new BusiException("表体重量的和不等于表头的总重量！");
			}
		}
		if(parentVO.getVolume_count() != null) {
			if(parentVO.getVolume_count().doubleValue() != volume_count.doubleValue()) {
				throw new BusiException("表体体积的和不等于表头的总体积！");
			}
		}
	}

	/**
	 * 全部收货按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/receiptAll.json")
	@ResponseBody
	public Map<String, Object> receiptAll(HttpServletRequest request, HttpServletResponse response) {
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
				AggregatedValueObject billVO = this.getService().receiptAll(paramVO);
				Map<String,Object> retMap = this.getService().execFormula4Templet(billVO, paramVO, true, false);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量收货时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未收货成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 根据生产日期计算商品的失效日期
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getExpireDate.json")
	@ResponseBody
	public Map<String, Object> getExpireDate(HttpServletRequest request, HttpServletResponse response) {
		String pk_goods = request.getParameter("pk_goods");
		if(StringUtils.isBlank(pk_goods)) {
			return null;
		}
		String produce_date = request.getParameter("produce_date");
		if(StringUtils.isBlank(produce_date)) {
			return null;
		}
		GoodsVO vo = NWDao.getInstance().queryByCondition(GoodsVO.class, "pk_goods=?", pk_goods);
		if(vo == null) {
			return null;
		}
		if(vo.getPeriod() == null || vo.getPeriod().intValue() == 0) {
			return null;
		}
		return this.genAjaxResponse(true, null,
				DateUtils.formatDate(DateUtils.addDay(DateUtils.parseString(produce_date), vo.getPeriod().intValue())));
	}
}

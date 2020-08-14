package com.tms.web.wh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.wh.MoveService;

/**
 * 库内移动
 * 
 * @author xuqc
 * @date 2014-3-29 下午01:04:40
 */
@Controller
@RequestMapping(value = "/wh/move")
public class MoveController extends AbsToftController {

	@Autowired
	private MoveService moveService;

	public MoveService getService() {
		return moveService;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/show.json")
	@ResponseBody
	public Map<String, Object> show(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String sql = this.getService().getBaseSql();
		sql += "and pk_lot_qty=?";
		HashMap valuesMap = NWDao.getInstance().queryForObject(sql, HashMap.class, paramVO.getBillId());
		if(valuesMap == null) {
			throw new BusiException("该单据已经被删除，请刷新页面！");
		}
		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		context.add(valuesMap);
		UiBillTempletVO uiBillTempletVO = this.getService().getBillTempletVO(this.getService().getBillTemplateID(paramVO));
		String[] tableCodes = paramVO.getHeaderTabCode().split(",");// 表头可能有多个页签
		List<Map<String, Object>> retList = this.getService().execFormula4Templet(context, uiBillTempletVO, false, tableCodes, null);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(Constants.HEADER, retList.get(0));
		return this.genAjaxResponse(true, null, retMap);
	}

}

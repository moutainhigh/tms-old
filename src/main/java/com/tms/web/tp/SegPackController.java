package com.tms.web.tp;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.tp.SegPackService;

/**
 * 运段明细操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/tp/segpack")
public class SegPackController extends AbsToftController {

	@Autowired
	private SegPackService segPackService;

	public SegPackService getService() {
		return segPackService;
	}

	public String getConditionString(HttpServletRequest request) {
		String pk_segment = request.getParameter("pk_segment"); // 运段pk,这里查询的是子表
		if(StringUtils.isBlank(pk_segment)) {
			throw new BusiException("请先选择运段！");
		}
		return "pk_segment='" + pk_segment + "'";
	}
}

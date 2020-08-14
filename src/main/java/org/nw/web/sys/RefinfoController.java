package org.nw.web.sys;

import org.nw.service.sys.RefinfoService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 用户操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/refinfo")
public class RefinfoController extends AbsToftController {

	@Autowired
	private RefinfoService refinfoService;

	public RefinfoService getService() {
		// 这里事务使用spring自动管理，service必须从springbean管理器中取得
		return refinfoService;
	}
}

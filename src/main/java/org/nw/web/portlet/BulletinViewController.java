package org.nw.web.portlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.service.sys.BulletinService;
import org.nw.vo.sys.BulletinVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 不需要登录以及不需要权限检查的页面，如首页的新闻、公告、公文等等
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:07
 */
@Controller
@RequestMapping(value = "/c/bulletin")
public class BulletinViewController extends AbsToftController {

	@Autowired
	private BulletinService bulletinService;

	public BulletinService getService() {
		return bulletinService;
	}

	protected String getFunHelpName(String funCode) {
		return "/default/more/bulletin.jsp";
	}

	/**
	 * 查询最新的5条记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTop5.html")
	public ModelAndView getTop5(HttpServletRequest request, HttpServletResponse response) {
		List<BulletinVO> vos = this.getService().getTop5();
		request.setAttribute("dataList", vos);
		return new ModelAndView("/default/autoLoad/bulletin.jsp");
	}
}

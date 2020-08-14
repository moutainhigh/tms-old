package org.nw.web.index;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.dao.NWDao;
import org.nw.service.index.QuickLinkService;
import org.nw.vo.index.QuickLinkVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.AbsBaseController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 快捷菜单
 * 
 * @author xuqc
 * 
 */
@Controller
@RequestMapping(value = "/common/quick")
public class QuickLinkController extends AbsBaseController {

	@Autowired
	private QuickLinkService quickLinkService;

	/**
	 * 增加快捷菜单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/add.json")
	@ResponseBody
	public Map<String, Object> addQuickLink(HttpServletRequest request, HttpServletResponse response) {
		String pk_user = WebUtils.getLoginInfo().getPk_user();
		String pk_fun = request.getParameter("pk_fun");
		String display_name = request.getParameter("fun_name");
		QuickLinkVO vo = new QuickLinkVO();
		vo.setPk_fun(pk_fun);
		vo.setDisplay_name(display_name);
		vo.setPk_user(pk_user);
		vo.setCreate_time(new UFDateTime(new Date()));
		vo.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(vo);// 设置新的pk值
		quickLinkService.addSuperVO(vo);
		return this.genAjaxResponse(true, null, null); // 这里不需要返回vo了，已经使用前台的数据了
	}

	@RequestMapping(value = "/edit.json")
	@ResponseBody
	public Map<String, Object> editQuickLink(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		String display_name = request.getParameter("fun_name");
		NWDao dao = NWDao.getInstance();
		QuickLinkVO vo = dao.queryByCondition(QuickLinkVO.class, "pk_fun=? and pk_user=?", pk_fun, WebUtils
				.getLoginInfo().getPk_user());
		vo.setDisplay_name(display_name);
		vo.setStatus(VOStatus.UPDATED);
		try {
			quickLinkService.saveOrUpdate(vo);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 移除快捷菜单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/remove.json")
	@ResponseBody
	public Map<String, Object> removeQuickLink(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		NWDao dao = NWDao.getInstance();
		QuickLinkVO vo = dao.queryByCondition(QuickLinkVO.class, "pk_fun=? and pk_user=?", pk_fun, WebUtils
				.getLoginInfo().getPk_user());
		try {
			quickLinkService.deleteSuperVO(vo);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 返回当前用户的快捷菜单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getQuickLinks.json")
	@ResponseBody
	public Map<String, Object> getQuickLink(HttpServletRequest request, HttpServletResponse response) {
		return this.genAjaxResponse(true, null, quickLinkService.getQuickLinks(WebUtils.getLoginInfo().getPk_user()));
	}
}

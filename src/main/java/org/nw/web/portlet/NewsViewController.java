package org.nw.web.portlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.service.sys.NewsService;
import org.nw.vo.sys.NewsVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 不需要登录以及不需要权限检查的页面，如首页的新闻、公告、公文等等
 * 
 * @author xuqc
 * @date 2013-9-18 上午11:00:37
 */
@Controller
@RequestMapping(value = "/c/news")
public class NewsViewController extends AbsToftController {

	public NewsService getService() {
		return newsService;
	}

	@Autowired
	private NewsService newsService;

	/**
	 * 查询最新的5条记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTop5.html")
	public ModelAndView getTop5(HttpServletRequest request, HttpServletResponse response) {
		List<NewsVO> vos = this.getService().getTop5();
		request.setAttribute("dataList", vos);
		return new ModelAndView("/default/autoLoad/news.jsp");
	}

	/**
	 * 查看首页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/viewList.html")
	public ModelAndView viewList(HttpServletRequest request, HttpServletResponse response) {
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		String keyword = request.getParameter("keyword");
		if(StringUtils.isNotBlank(keyword)) {
			request.setAttribute("keyword", keyword);
		}

		PaginationVO pageVO = this.getService().getPageVO(offset, pageSize, keyword);
		request.setAttribute("newsVOs", pageVO.getItems());
		request.setAttribute("totalCount", pageVO.getTotalCount());
		request.setAttribute("pageHtml", pageVO.getHtml(PaginationVO.script));
		request.setAttribute("PAGE_PARAM_START", pageVO.getStartOffset());
		request.setAttribute("PAGE_PARAM_LIMIT", pageVO.getPageSize());
		return new ModelAndView("/default/more/viewList.jsp");
	}

	/**
	 * 新闻详细页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/detail.html")
	public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) {
		String pk_news = request.getParameter("pk_news");
		if(StringUtils.isBlank(pk_news)) {
			throw new BusiException("主键不能为空！");
		}
		this.getService().updateReadNum(pk_news);
		NewsVO vo = this.getService().getByPK(pk_news);
		request.setAttribute("newsVO", vo);
		return new ModelAndView("/default/more/newsDetail.jsp");
	}
}

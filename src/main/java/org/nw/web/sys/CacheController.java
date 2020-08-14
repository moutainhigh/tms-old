package org.nw.web.sys;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheException;
import net.sf.cache4j.CacheFactory;

import org.nw.web.AbsBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.uft.webnc.cache.proxy.CacheManagerProxy;

/**
 * 缓存控制器，目前没有具体的范围界面。目前使用的ICache功能较简单
 * 
 * @author xuqc
 * 
 */
@Controller
@RequestMapping("/cache")
public class CacheController extends AbsBaseController {

	@RequestMapping("/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/sys/cache.jsp");
	}

	/**
	 * 清除缓存
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/clearAllCache.json")
	@ResponseBody
	public Map<String, Object> clearAllCache(HttpServletRequest request, HttpServletResponse response) {
		CacheFactory cacheFactory = CacheFactory.getInstance();
		try {
			Cache cache = cacheFactory.getCache("defaultCache");
			cache.clear();
		} catch(CacheException e) {
			e.printStackTrace();
			return this.genAjaxResponse(false, e.getMessage(), null);
		}
		return this.genAjaxResponse(true, "缓存清除成功！", null);
	}

	/**
	 * 清除某个缓存
	 * 
	 * @param request
	 * @param response
	 * @param cacheName
	 * @param cacheKey
	 * @return
	 */
	@RequestMapping("/clearElement.json")
	@ResponseBody
	public Map<String, Object> clearElement(HttpServletRequest request, HttpServletResponse response, String cacheKey) {
		CacheManagerProxy.getICache().remove(cacheKey);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 查询所有缓存
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/getAllCache.json")
	@ResponseBody
	public Map<String, Object> getAllCache(HttpServletRequest request, HttpServletResponse response) {
		// List<EHCacheVO> vos = new ArrayList<EHCacheVO>();
		// CacheManager cacheMgr =
		// CacheManagerProxy.getInstance().getCacheManager();
		//
		// Cache cache =
		// cacheMgr.getCache(WebUtils.getClientConfig().getCacheName());
		// @SuppressWarnings("rawtypes")
		// List keys = cache.getKeys();
		// for(int j = 0; j < keys.size(); j++) {
		// Element element = cache.get(keys.get(j));
		// if(element != null) {
		// EHCacheVO vo = new EHCacheVO();
		// vo.setCacheName(cache.getName());
		// vo.setCacheKey(keys.get(j).toString());
		// if(element.getObjectValue() != null) {
		// if(element.getObjectValue() instanceof String) {
		// vo.setValue((String) element.getObjectValue());
		// } else {
		// try {
		// vo.setValue(element.getObjectValue().toString());
		// } catch(Exception e) {
		// logger.error(element.getObjectValue(), e);
		// }
		// }
		// }
		// vo.setHitCount(element.getHitCount());
		// vo.setExpirationTime(DateUtils.formatDate(new
		// Date(element.getExpirationTime()),
		// DateUtils.DATETIME_FORMAT_HORIZONTAL));
		// vo.setCreationTime(DateUtils.formatDate(new
		// Date(element.getCreationTime()),
		// DateUtils.DATETIME_FORMAT_HORIZONTAL));
		// vo.setLastAccessTime(DateUtils.formatDate(new
		// Date(element.getLastAccessTime()),
		// DateUtils.DATETIME_FORMAT_HORIZONTAL));
		// vos.add(vo);
		// }
		// }
		//
		// PaginationVO paginationVO = new PaginationVO();
		// paginationVO.setItems(vos);
		// paginationVO.setTotalCount(vos.size());
		return this.genAjaxResponse(true, null, null);
	}
}
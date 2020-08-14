package org.nw.web.index;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.constants.FunRegisterConst;
import org.nw.dao.CacheUtils;
import org.nw.exception.BusiAlertException;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.service.sys.FunService;
import org.nw.utils.HttpUtils;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.FunVO;
import org.nw.web.AbsBaseController;
import org.nw.web.utils.WebUtils;
import org.nw.webSocket.WebsocketEndPoint;

import com.tms.constants.BaiduMapConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 系统首页的操作类
 * 
 * @author xuqc
 * 
 */
@Controller
public class IndexController extends AbsBaseController {

	@Autowired
	private FunService funService;

	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		FunVO[] funVOs = funService.getTopLevelFunVOs(WebUtils.getLoginInfo().getPk_user(), WebUtils.getLoginInfo()
				.getPk_corp());
		request.setAttribute("funVOs", JacksonUtils.writeValueAsString(funVOs));
		return new ModelAndView("/index/index.jsp");
	}
	
	
	@RequestMapping(value = "/rfIndex.html")
	public ModelAndView rfIndex(HttpServletRequest request, HttpServletResponse response) {
		FunVO[] funVOs = funService.getTopLevelFunVOs(WebUtils.getLoginInfo().getPk_user(), WebUtils.getLoginInfo()
				.getPk_corp());
		request.setAttribute("funVOs", JacksonUtils.writeValueAsString(funVOs));
		return new ModelAndView("/index/rfIndex.jsp");
	}

	/**
	 * 查询当前用户授权的菜单
	 * 
	 * @param request
	 * @param response
	 * @param parentFunCode
	 *            父级节点编码
	 * @return
	 */
	@RequestMapping(value = "/getFunTree.json")
	@ResponseBody
	public List<TreeVO> getFunTree(HttpServletRequest request, HttpServletResponse response, String parent_id) {
		List<TreeVO> func = funService.getFunTree(WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo()
				.getPk_user(), parent_id);
		if(func == null) {
			// 返回一个null对象，js会报错
			func = new ArrayList<TreeVO>();
		} else {
			if(CacheUtils.isUseCache()) {
				// 如果是非debug模式，过滤“资源压缩”这个节点
				for(TreeVO treeVO : func) {
					if(treeVO.getCode().equals(FunRegisterConst.COMPRESSOR_FUN_CODE)) {
						func.remove(treeVO);
						break;
					}
				}
			}
		}
		return func;
	}
	
	/**
	 * 天气预报
	 * @author XIA
	 * @param request
	 * @param response
	 * @param parent_id
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getWeatherForecast.json")
	@ResponseBody
	public Map<String,String> getWeatherForecast(HttpServletRequest request, HttpServletResponse response, String parent_id) {
		//获取城市信息 
		String msg = "";
		try {
			msg = HttpUtils.get(BaiduMapConst.BAIDU_ADDRESS_LOCATION_URL);
		} catch (Exception e1) {
			logger.info("百度获取地址出错：" + e1.getMessage());
		}
		Map<String,Object> addrmsg = JacksonUtils.readValue(msg, Map.class);
		if(addrmsg == null){
			return null;
		}
		Map<String,Map<String,String>> content = (Map<String, Map<String, String>>) addrmsg.get("content");
		String city = String.valueOf(content.get("address_detail").get("city"));
		if(StringUtils.isBlank(city)){
			return null;
		}
		String weather= "";
		try {
			weather = HttpUtils.get(BaiduMapConst.BAIDU_WEATHER_URL + city);
		} catch (Exception e1) {
			logger.info("百度获取天气出错：" + e1.getMessage());
		}
		Map<String,Object> weathermsg = JacksonUtils.readValue(weather, Map.class);
		List<Map<String, Object>> results = (List<Map<String, Object>>) weathermsg.get("results");
		//获取当日天气即可
		Map<String,String> todayWeather = new HashMap<String, String>();
		try {
			List<Map<String,String>> weather_data = (List<Map<String, String>>) results.get(0).get("weather_data");
			todayWeather = weather_data.get(0);
		} catch (Exception e) {
			return null;
		}
		UFDateTime now = new UFDateTime(new Date());
		if(now.getHour() > 5 && now.getHour() < 18){
			//白天
			todayWeather.put("pictureUrl", todayWeather.get("dayPictureUrl"));
		}else{
			//晚上
			todayWeather.put("pictureUrl", todayWeather.get("nightPictureUrl"));
		}
		
		return todayWeather;
	}
	
	/**
	 * 获取弹窗信息，这里应该用websocket解决的，但是目前系统不支持，spring版本和tomcat版本过低
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getPopup.json")
	@ResponseBody
	public Map<String,String> getPopup(HttpServletRequest request, HttpServletResponse response) {
		//获取城市信息 
		String pk_user = WebUtils.getLoginInfo().getPk_user();
		//这里储存的是<pk_portlet,portlet_name(count)>形式
		Map<String,String> userPortletInfo = RedisDao.getInstance().getUserPortletInfo(pk_user);
		if(userPortletInfo != null && userPortletInfo.size() > 0){
			//转换成list返回
//			List<String> infoList = new ArrayList<String>();
//			for(String key : userPortletInfo.keySet()){
//				infoList.add(userPortletInfo.get(key));
//			}
//			return infoList;
			return userPortletInfo;
		}
		return null;
	}
	

	/**
	 * 首页的快捷入口
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/quickEntry.json")
	@ResponseBody
	public Map<String, Object> quickEntry(HttpServletRequest request, HttpServletResponse response) {
		String fun_name = request.getParameter("query");
		if(StringUtils.isBlank(fun_name)) {
			return null;
		}
		// 查询轻量级web节点，并且是有权限的
		List<FunVO> funVOs = funService.getPowerFunVO(false);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for(FunVO vo : funVOs) {
			if(vo.getFun_property().intValue() == FunRegisterConst.LFW_FUNC_NODE
					&& vo.getFun_name().indexOf(fun_name) != -1) {// 轻量级节点
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(Constants.TEXT, vo.getFun_name());
				String url = request.getContextPath() + vo.getClass_name();
				if(url.indexOf("?") == -1) {
					url += "?";
				} else {
					url += "&";
				}
				url += "funCode=" + vo.getFun_code();
				map.put(Constants.VALUE, url);
				results.add(map);
			}
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("records", results);
		return retMap;
	}

	/**
	 * 页面定时请求这个url，保证session不过期
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/keepSessionAlive.json")
	@ResponseBody
	public void keepSessionAlive(HttpServletRequest request, HttpServletResponse response) {

	}

	/**
	 * 检测许可证
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/checkLicense.json")
	public void checkLicense(HttpServletRequest request, HttpServletResponse response) {
		try {
			int remainDays = WebUtils.checkLicense();
			if(remainDays != -1 && remainDays <31) {
				throw new BusiAlertException("您的软件，将在[?]天后过期，请联系销售顾问购买许可证！",remainDays+"");
			}
		} catch(Exception e) {
			throw new BusiAlertException(e.getMessage());
		}
	}
}

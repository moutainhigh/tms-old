package org.nw.web.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.exception.BusiException;
import org.nw.service.sys.UserPortletService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.UserPortletVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户门户配置
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 * @deprecated
 */
@Controller
@RequestMapping(value = "/up")
public class UserPortletController extends AbsToftController {

	@Autowired
	private UserPortletService userPortletService;

	public UserPortletService getService() {
		return userPortletService;
	}

	/**
	 * 返回用户定义的门户配置信息，默认首页的portal信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getUserPortlet.json")
	@ResponseBody
	public Map<String, Object> getUserPortlet(HttpServletRequest request, HttpServletResponse response) {
		return this.genAjaxResponse(true, null, this.getService().getUserPortlet(WebUtils.getLoginInfo().getPk_user()));
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		UserPortletVO[] upVOs = (UserPortletVO[]) billVO.getChildrenVO();
		if(upVOs.length > 0) {
			List<String> portletIDs = new ArrayList<String>();
			for(int i = 0; i < upVOs.length; i++) {
				if(upVOs[i].getStatus() != VOStatus.DELETED) {
					if(!portletIDs.contains(upVOs[i].getPortlet_id())) {
						portletIDs.add(upVOs[i].getPortlet_id());
					} else {
						throw new BusiException("portlet ID列不能重复！");
					}
				}
			}
		}
	}

}

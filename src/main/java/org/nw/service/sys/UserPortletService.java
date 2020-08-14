package org.nw.service.sys;

import org.nw.service.IToftService;
import org.nw.vo.sys.UserPortletVO;

/**
 * 门户配置接口
 * 
 * @author xuqc
 * @date 2013-11-4 下午05:22:17
 */
public interface UserPortletService extends IToftService {

	/**
	 * 返回用户的门户配置信息,如果用户没有定义，那么使用管理员定义的默认门户信息
	 * 
	 * @param pk_user
	 * @return
	 */
	UserPortletVO[] getUserPortlet(String pk_user);
}

package org.nw.service.index;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.sys.FunVO;


/**
 * 快捷菜单操作类
 * 
 * @author xuqc
 * @date 2012-7-3 下午10:51:34
 */
public interface QuickLinkService extends IToftService {

	/**
	 * 返回用户的快捷菜单,这里和nw_fun进行关联
	 * 
	 * @param pk_user
	 * @return
	 */
	public List<FunVO> getQuickLinks(String pk_user);
}

package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.sys.FunVO;


/**
 * 自定义菜单操作接口
 * 
 * @author xuqc
 * @date 2012-7-8 下午02:16:14
 */
public interface UFunService extends IToftService {

	/**
	 * 自定义菜单中，更新菜单的顺序
	 * 
	 * @param funVOs
	 * @return
	 */
	public int saveDisplayOrder(List<FunVO> funVOs);
}

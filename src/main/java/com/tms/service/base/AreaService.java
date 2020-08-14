package com.tms.service.base;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;

import com.tms.vo.base.AreaVO;

/**
 * 地区管理处理接口
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:17:01
 */
public interface AreaService extends IToftService {

	/**
	 * 根据父节点返回区域树
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<TreeVO> getAreaTree(String parent_id, String keyword);

	/**
	 * 根据几次返回区域树，如返回城市所在级次开始的树
	 * 
	 * @param level
	 * @return
	 */
	public List<TreeVO> getAreaTree(int level);

	/**
	 * 根据城市名称返回城市VO
	 * 
	 * @param name
	 * @return
	 */
	public AreaVO getCityByName(String name);
	
	/**
	 * 根据城市编码返回城市VO
	 * 
	 * @param name
	 * @return
	 */
	public AreaVO getCityByCode(String code);
}

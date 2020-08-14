package org.nw.service.sys;

import java.util.List;

import org.nw.dao.PaginationVO;
import org.nw.service.IToftService;
import org.nw.vo.sys.BtnTempletDistVO;

/**
 * 按钮模板分配
 * 
 * @author xuqc
 * @date 2012-7-8 上午11:38:14
 */
public interface BtnTempletDistService extends IToftService {

	/**
	 * 根据功能节点查询当前已经分配的模板
	 * 
	 * @param pk_fun
	 * @param pk_role
	 * @return
	 */
	public List<BtnTempletDistVO> loadTempletDist(String pk_fun);

	/**
	 * 根据功能节点返回可分配的模板
	 * 
	 * @param pk_fun
	 * @return
	 */
	public PaginationVO loadTemplet(String pk_fun);

	/**
	 * 保存模板分配，先删除原有的模板分配
	 * 
	 * @param distVOs
	 *            这里每个distVO的pk_fun都是相同的
	 * @return
	 */
	public int saveTempletDist(List<BtnTempletDistVO> distVOs, String pk_fun);
}

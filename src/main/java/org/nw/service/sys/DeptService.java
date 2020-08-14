package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.DeptVO;


/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午03:23:10
 */
public interface DeptService extends IToftService {

	/**
	 * 根据父级节点返回部门树
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<TreeVO> getDeptTree(String parent_id);

	/**
	 * 根据部门编码返回部门VO
	 * 
	 * @param dept_code
	 * @return
	 */
	public DeptVO getByDept_code(String dept_code);

	/**
	 * 返回公司下的所有部门，不分级次
	 * 
	 * @param pk_corp
	 * @return
	 */
	public List<DeptVO> getByPkCorp(String pk_corp);
}

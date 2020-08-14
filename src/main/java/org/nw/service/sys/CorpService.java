package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.CorpVO;


/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午02:51:26
 */
public interface CorpService extends IToftService {

	/**
	 * 根据父级公司，返回所有子公司,注意：只返回直接下级公司
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<CorpVO> getCorpVOs(String parent_id);

	/**
	 * 根据父级节点返回所有子节点，并构建树形结构
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<TreeVO> getCorpTree(String parent_id);

	/**
	 * 根据公司编码返回公司VO
	 * 
	 * @param corp_code
	 */
	public CorpVO getCorpVOByCorpCode(String corp_code);

	/**
	 * 返回当前登录用户的公司及其子公司
	 * 
	 * @return
	 */
	public List<CorpVO> getCurrentCorpVOs();

	/**
	 * 返回公司的条件，目前默认是包括当前公司的子公司、集团的，即当前公司可以看到子公司和集团的数据
	 * 
	 * @return
	 */
	public String getCurrentCorpCondition();
}

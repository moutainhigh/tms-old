package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.CodeRuleVO;

/**
 * 基础数据编码规则操作接口
 * 
 * @author xuqc
 * @date 2012-7-27 下午10:44:03
 */
public interface CodeRuleService extends IToftService {

	/**
	 * 返回基础数据的功能编码树
	 * 
	 * @return
	 */
	public List<TreeVO> getBaseDataFunCodeTree();

	/**
	 * 返回功能节点树,只返回需要使用编码规则的节点，同时要排除有单据类型的节点
	 * 
	 * @return
	 */
	public List<TreeVO> getFunCodeTree();

	/**
	 * 根据fun_code返回编码规则,如果公司没有定义规则，则返回集团定义的规则
	 * 
	 * @return
	 */
	public CodeRuleVO getByFunCode(String fun_code);
}

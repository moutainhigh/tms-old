package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.FunVO;

/**
 * 菜单管理操作接口
 * 
 * @author xuqc
 * @date 2012-6-16 下午06:20:54
 */
public interface FunService extends IToftService {

	/**
	 * 返回功能节点树,不使用display_order排序
	 * 
	 * @return
	 */
	public List<TreeVO> getOriginalFunTree(String parent_id);

	/**
	 * 返回功能节点树
	 * 
	 * @return
	 */
	public List<TreeVO> getFunTree(String parent_id);

	/**
	 * 根据funCode查询funVO，不加入dr条件，用于判断fun_code是否存在
	 * 
	 * @param fun_code
	 * @return
	 */
	public FunVO getFunVOByFunCodeWithNoDr(String fun_code);

	/**
	 * 返回所有子功能节点VO
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<FunVO> getFunVOs(String parent_id, boolean encludeLockedFlag);

	/**
	 * 检测用户是否具有pk_fun的权限
	 * 
	 * @param pk_user
	 * @param pk_fun
	 * @return
	 */
	public boolean isPowerByUserFun(String pk_user, String pk_fun);

	/**
	 * 根据单据类型返回节点VO
	 * 
	 * @param bill_type
	 * @return
	 */
	public FunVO getFunVOByBillType(String bill_type);

	/**
	 * 返回单据类型树，功能节点中包括单据类型的所有节点<br/>
	 * 并且该功能节点必须是有权限的<br/>
	 * XXX 2013-9-17 必须是定义了单据类型并且使用编码规则的节点,才能定义其单据号规则，之前是只要定义了单据类型的节点都能定义单据号规则
	 * 
	 * @return
	 */
	public List<TreeVO> getBillTypeTree();

	/**
	 * 返回授权的功能菜单
	 * 
	 * @param encludeLockedFlag
	 *            是否包含锁定
	 * @return
	 */
	public List<FunVO> getPowerFunVO(boolean encludeLockedFlag);

	/**
	 * 返回授权的功能菜单和按钮
	 * 
	 * @param encludeLockedFlag
	 * @return
	 */
	public List<FunVO> getPowerFunVOWithBtn(boolean encludeLockedFlag);

	/**
	 * 返回授权的功能菜单
	 * 
	 * @param pk_user
	 * @param pk_corp
	 * @param parent_id
	 * @return
	 */
	public List<TreeVO> getFunTree(String pk_user, String pk_corp, String parent_id);

	/**
	 * 返回授权的功能菜单，包括按钮
	 * 
	 * @param pk_user
	 * @param pk_corp
	 * @param parent_id
	 * @return
	 */
	public List<TreeVO> getFunTreeWithBtn(String pk_user, String pk_corp, String parent_id);

	/**
	 * 返回授权的功能菜单 <br/>
	 * XXX 2013-4-16,返回一个数组，需要排序
	 * 
	 * @param pk_user
	 * @param pk_corp
	 * @param parent_id
	 * @param encludeLockedFlag
	 *            是否加入锁定的纪录
	 * @return
	 */
	public TreeVO[] getFunTree(String pk_user, String pk_corp, String parent_id, boolean encludeLockedFlag);

	/**
	 * 返回授权的第一级的功能节点<br/>
	 * XXX 2013-4-16,返回一个数组，需要排序
	 * 
	 * @param pk_user
	 * @param pk_corp
	 * @return
	 */
	public FunVO[] getTopLevelFunVOs(String pk_user, String pk_corp);

	/**
	 * 根据是否使用编码规则返回节点，并且是web功能节点
	 * 
	 * @param if_code_rule
	 * @return
	 */
	public List<FunVO> getByIf_code_rule(boolean if_code_rule);
}

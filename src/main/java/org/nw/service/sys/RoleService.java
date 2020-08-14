package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.PortletPlanVO;
import org.nw.vo.sys.PowerFunVO;
import org.nw.vo.sys.RoleVO;
import org.nw.vo.sys.UserRoleVO;

public interface RoleService extends IToftService {

	/**
	 * 根据角色id返回角色权限树
	 * 
	 * @param pk_role
	 * @return
	 */
	public List<TreeVO> loadPowerFunTree(String pk_role);
	
	/**
	 * 根据角色id返回角色工作台权限树
	 * 
	 * @param pk_role
	 * @return
	 */
	public List<TreeVO> loadWorkBenchPowerTree(String pk_role);
	
	/**
	 * 保存工作台
	 * 
	 * @param powerFunVOs
	 * @return
	 */
	public int saveWorkBenchPower(String pk_role, String[] pk_WorkBenchAry);

	/**
	 * 根据角色id返回角色权限
	 * 
	 * @param pk_role
	 * @return
	 */
	public List<PowerFunVO> loadPowerFun(String pk_role);

	/**
	 * 保存功能权限
	 * 
	 * @param powerFunVOs
	 * @return
	 */
	public int savePowerFun(String pk_role, String[] pkFunAry);

	/**
	 * 返回用户具有的角色列表
	 * 
	 * @param pk_user
	 * @return
	 */
	public List<RoleVO> getRoleByUser(String pk_user);

	/**
	 * 返回用户具有的角色，这里返回的是关联VO
	 * 
	 * @param pk_user
	 * @return
	 */
	public List<UserRoleVO> getUserRoleByUser(String pk_user);

	/**
	 * 返回用户不具有的角色列表
	 * 
	 * @param pk_user
	 * @return
	 */
	public List<RoleVO> getUnAuthorizeRoleByUser(String pk_user);

	/**
	 * 保存用户的角色
	 * 
	 * @param pk_user
	 * @param pkRoleAry
	 * @return
	 */
	public int addRoleToUser(String pk_user, String[] pkRoleAry);

	/**
	 * 返回当前公司及其子公司的所有角色，并转成TreeVO
	 * 
	 * @param pk_corp
	 * @return
	 */
	public List<TreeVO> getRoleTree();

	/**
	 * 根据角色编码返回角色VO
	 * 
	 * @param role_code
	 * @return
	 */
	public RoleVO getByRole_code(String role_code);

	/**
	 * 查询角色具有的门户方案，目前只支持一个角色只能有一个方案
	 * 
	 * @param pk_role
	 * @return
	 */
	public List<PortletPlanVO> getPlanByRole(String pk_role);

	/**
	 * 查询可供角色分配的方案
	 * 
	 * @param pk_role
	 * @return
	 */
	public List<PortletPlanVO> getUnAuthorizePlanByRole(String pk_role);

	/**
	 * 为角色增加方案
	 * 
	 * @param pk_role
	 * @param pkPlanAry
	 */
	public int addPlanToRole(String pk_role, String[] pkPlanAry);
}

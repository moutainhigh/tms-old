package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.redis.RedisDao;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.FunService;
import org.nw.service.sys.RoleService;
import org.nw.service.sys.WorkBenchService;
import org.nw.utils.CodenoHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.TreeUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.PortletPlanVO;
import org.nw.vo.sys.PowerFunVO;
import org.nw.vo.sys.PowerWorkbenchVO;
import org.nw.vo.sys.RolePlanVO;
import org.nw.vo.sys.RoleVO;
import org.nw.vo.sys.UserRoleVO;
import org.nw.vo.sys.UserVO;
import org.nw.vo.sys.WorkBenchVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 日志管理操作类
 * 
 * @author xuqc
 * 
 */
@Service
public class RoleServiceImpl extends AbsToftServiceImpl implements RoleService {

	@Autowired
	public FunService funService;
	
	@Autowired
	public WorkBenchService workBenchService;

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, RoleVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, RoleVO.PK_ROLE);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public List<TreeVO> loadPowerFunTree(String pk_role) {
		// 返回所有功能节点
		List<FunVO> funVOs = funService.getFunVOs(null, true);
		// 根据角色返回当前用户的所有权限菜单
		List<String> pkFunAry = dao.queryForList("select pk_fun from nw_power_fun WITH(NOLOCK) where isnull(dr,0)=0 and pk_role=?",
				String.class, pk_role);
		List<TreeVO> treeVOs = TreeUtils.convertFunVOByParent(funVOs, null, pkFunAry);
		return treeVOs;
	}

	public List<PowerFunVO> loadPowerFun(String pk_role) {
		return dao.queryForList("select * from nw_power_fun WITH(NOLOCK) where isnull(dr,0)=0 and pk_role=?", PowerFunVO.class,
				pk_role);
	}
	public List<PowerWorkbenchVO> loadWorkBenchVO(String pk_role) {
		return dao.queryForList("select * from nw_power_workbench WITH(NOLOCK) where isnull(dr,0)=0 and pk_role=?", PowerWorkbenchVO.class,
				pk_role);
	}

	public int savePowerFun(String pk_role, String[] pkFunAry) {
		// 删除原有的权限数据
		List<PowerFunVO> oldPowerFunVOs = loadPowerFun(pk_role);
		List<String> sqlList = new ArrayList<String>();
		for(PowerFunVO powerFunVO : oldPowerFunVOs) {
			powerFunVO.setStatus(VOStatus.DELETED);
			sqlList.add(DaoHelper.getDeleteSQL(powerFunVO, false));
		}

		sqlList = dao.getTransSql(sqlList);

		// 进行保存处理
		if(sqlList != null && sqlList.size() > 0) {
			dao.getJdbcTemplate().batchUpdate(sqlList.toArray(new String[sqlList.size()]));
		}

		// 加入新的权限数据
		PowerFunVO[] powerFunVOs = new PowerFunVO[pkFunAry.length];
		for(int i = 0; i < pkFunAry.length; i++) {
			PowerFunVO powerFunVO = new PowerFunVO();
			powerFunVO.setPk_fun(pkFunAry[i]);
			powerFunVO.setPk_role(pk_role);
			powerFunVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(powerFunVO);
			powerFunVOs[i] = powerFunVO;
		}
		dao.saveOrUpdate(powerFunVOs);
		return pkFunAry.length;
	}
	
	public List<TreeVO> loadWorkBenchPowerTree(String pk_role) {
		// 返回所有功能节点
		List<WorkBenchVO> workBenchVOs = workBenchService.getWorkBenchVOs();
		// 根据角色返回当前用户的所有权限菜单
		List<String> workBenchPKs = workBenchService.getWorkBenchPKsByRole(pk_role);
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		if(workBenchVOs == null || workBenchVOs.size() == 0 ){
			return treeVOs;
		}
		for(WorkBenchVO workBenchVO : workBenchVOs) {
			TreeVO treeVO = new TreeVO();
			treeVO.setId(workBenchVO.getPk_workbench());
			treeVO.setCode(workBenchVO.getWorkbench_code());
			treeVO.setText(workBenchVO.getWorkbench_name());
			treeVO.setLeaf(true);
			treeVOs.add(treeVO);
			if(workBenchPKs != null && workBenchPKs.size() > 0){
				for(String pk : workBenchPKs){
					if(pk.equals(workBenchVO.getPk_workbench())){
						treeVO.setChecked(true);
					}
				}
			}
		}
		return treeVOs;
	}
	
	public int saveWorkBenchPower(String pk_role, String[] pk_WorkBenchAry) {
		// 删除原有的权限数据
		List<PowerWorkbenchVO> oldPowerWorkbenchVOs = loadWorkBenchVO(pk_role);
		List<String> sqlList = new ArrayList<String>();
		for(PowerWorkbenchVO powerWorkbenchVO : oldPowerWorkbenchVOs) {
			powerWorkbenchVO.setStatus(VOStatus.DELETED);
			sqlList.add(DaoHelper.getDeleteSQL(powerWorkbenchVO, false));
		}

		sqlList = dao.getTransSql(sqlList);

		// 进行保存处理
		if(sqlList != null && sqlList.size() > 0) {
			dao.getJdbcTemplate().batchUpdate(sqlList.toArray(new String[sqlList.size()]));
		}

		// 加入新的权限数据
		PowerWorkbenchVO[] powerWorkbenchVOs = new PowerWorkbenchVO[pk_WorkBenchAry.length];
		for(int i = 0; i < pk_WorkBenchAry.length; i++) {
			PowerWorkbenchVO powerWorkbenchVO = new PowerWorkbenchVO();
			powerWorkbenchVO.setPk_workbench(pk_WorkBenchAry[i]);;
			powerWorkbenchVO.setPk_role(pk_role);
			powerWorkbenchVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(powerWorkbenchVO);
			powerWorkbenchVOs[i] = powerWorkbenchVO;
		}
		dao.saveOrUpdate(powerWorkbenchVOs);
		return pk_WorkBenchAry.length;
	}

	public List<UserRoleVO> getUserRoleByUser(String pk_user) {
		String sql = "select * from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?";
		return dao.queryForList(sql, UserRoleVO.class, pk_user);
	}

	public int addRoleToUser(String pk_user, String[] pkRoleAry) {
		// 删除原有的权限数据
		List<UserRoleVO> oldUserRoleVOs = getUserRoleByUser(pk_user);
		List<String> sqlList = new ArrayList<String>();
		for(UserRoleVO userRoleVO : oldUserRoleVOs) {
			userRoleVO.setStatus(VOStatus.DELETED);
			sqlList.add(DaoHelper.getDeleteSQL(userRoleVO, false));
		}
		sqlList = dao.getTransSql(sqlList);

		// 进行保存处理
		if(sqlList != null && sqlList.size() > 0) {
			dao.getJdbcTemplate().batchUpdate(sqlList.toArray(new String[sqlList.size()]));
		}
		// 加入新的权限数据
		UserRoleVO[] userRoleVOs = new UserRoleVO[pkRoleAry.length];
		for(int i = 0; i < pkRoleAry.length; i++) {
			UserRoleVO userRoleVO = new UserRoleVO();
			userRoleVO.setPk_role(pkRoleAry[i]);
			userRoleVO.setPk_user(pk_user);
			userRoleVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(userRoleVO);
			userRoleVOs[i] = userRoleVO;
		}
		dao.saveOrUpdate(userRoleVOs);
		return pkRoleAry.length;
	}

	public List<RoleVO> getRoleByUser(String pk_user) {
		return NWDao.getInstance().queryForList(
				"select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 "
						+ "and pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?)",
				RoleVO.class, pk_user);

	}

	public List<RoleVO> getUnAuthorizeRoleByUser(String pk_user) {
		UserVO userVO = dao.queryByCondition(UserVO.class, "pk_user=?", pk_user);
		// 查询用户所在公司(以及集团)所具有的角色,,并且该角色当前用户还不具有
		String pk_corp = userVO.getPk_corp();
		String sql;
		if(pk_corp.equals(Constants.SYSTEM_CODE)) {
			// 登录公司是集团
			//sql = "select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? "
					//+ "and pk_role not in (select pk_role from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?)";
			sql = "select * from (select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp<>? ";
			sql += " union ";
			sql += "select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp='" + Constants.SYSTEM_CODE + "' ) as T";
			sql += " where pk_role not in (select pk_role from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?)";
			
		} else {
			sql = "select * from (select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? ";
			sql += " union ";
			sql += "select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp='" + Constants.SYSTEM_CODE + "' ) as T";
			sql += "  where pk_role not in (select pk_role from nw_user_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_user=?)";
		}
		return dao.queryForList(sql, RoleVO.class, userVO.getPk_corp(), pk_user);
	}

	public List<TreeVO> getRoleTree() {
		List<CorpVO> corpVOs = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
		//List<CorpVO> corpVOs = CorpHelper.getCurrentCorpVOsWithChildren();
		List<TreeVO> vos = new ArrayList<TreeVO>();
		for(CorpVO corpVO : corpVOs) {
			TreeVO parentNode = convert(corpVO);
			String sql = "select * from nw_role WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=?";
			List<RoleVO> roleVOs = dao.queryForList(sql, RoleVO.class, corpVO.getPk_corp());
			if(roleVOs != null && roleVOs.size() > 0) {
				List<TreeVO> treeVOs = convert(roleVOs);
				parentNode.setChildren(treeVOs);
			} else {
				parentNode.setChildren(new ArrayList<TreeVO>());
			}
			vos.add(parentNode);
		}
		return vos;
	}

	private TreeVO convert(CorpVO corpVO) {
		if(corpVO == null) {
			return null;
		}
		TreeVO treeVO = new TreeVO();
		treeVO.setId(corpVO.getPk_corp());
		treeVO.setCode(corpVO.getCorp_code());
		treeVO.setText(corpVO.getCorp_code() + " " + corpVO.getCorp_name());
		treeVO.setLeaf(false);
		return treeVO;
	}

	/**
	 * 从roleVO转成TreeVO
	 * 
	 * @param roleVOs
	 * @return
	 */
	private List<TreeVO> convert(List<RoleVO> roleVOs) {
		if(roleVOs == null) {
			return null;
		}
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(RoleVO roleVO : roleVOs) {
			TreeVO treeVO = new TreeVO();
			treeVO.setId(roleVO.getPk_role());
			treeVO.setCode(roleVO.getRole_code());
			treeVO.setText(roleVO.getRole_code() + " " + roleVO.getRole_name());
			treeVO.setLeaf(true);
			treeVOs.add(treeVO);
		}
		return treeVOs;
	}

	public RoleVO getByRole_code(String role_code) {
		return dao.queryByCondition(RoleVO.class, "role_code=?", role_code);
	}

	protected void beforeProcessCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.beforeProcessCopyVO(copyVO, paramVO);
		RoleVO parentVO = (RoleVO) copyVO.getParentVO();
		parentVO.setSrc_role(parentVO.getPk_role());
		parentVO.setRole_code(CodenoHelper.generateCode(paramVO.getFunCode()));
	}

	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		billVO = super.save(billVO, paramVO);
		//Map<String, Object> retMap = super.save(billVO, paramVO);
		RoleVO roleVO = (RoleVO) billVO.getParentVO();
		if(StringUtils.isNotBlank(roleVO.getSrc_role())) {
			// 如果是从复制单据来的，那么同时复制角色的权限
			List<PowerFunVO> powerFunVOs = loadPowerFun(roleVO.getSrc_role());
			for(PowerFunVO powerFunVO : powerFunVOs) {
				powerFunVO.setPk_power_fun(null);
				powerFunVO.setPk_role(roleVO.getPk_role());
				powerFunVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(powerFunVO);
			}
			dao.saveOrUpdate(powerFunVOs.toArray(new PowerFunVO[powerFunVOs.size()]));
		}
		return billVO;
	}

	/**
	 * 基础资料，可以看到当前公司及集团的数据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "pk_role <> '" + Constants.SYSTEM_CODE + "'";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		if(!paramVO.isBody() && !WebUtils.getLoginInfo().getPk_corp().equals(Constants.SYSTEM_CODE)) {
			String corpCond = CorpHelper.getCurrentCorp();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}

	public List<PortletPlanVO> getPlanByRole(String pk_role) {
		if(StringUtils.isBlank(pk_role)) {
			return null;
		}
		return NWDao
				.getInstance()
				.queryForList(
						"select * from nw_portlet_plan WITH(NOLOCK) where isnull(dr,0)=0 and "
								+ "pk_portlet_plan in (select pk_portlet_plan from nw_role_plan WITH(NOLOCK) where pk_role=? and isnull(dr,0)=0)",
						PortletPlanVO.class, pk_role);
	}

	public List<PortletPlanVO> getUnAuthorizePlanByRole(String pk_role) {
		if(StringUtils.isBlank(pk_role)) {
			return null;
		}
		// 查询当前公司以及集团具有的方案
		String sql = "select * from (select * from nw_portlet_plan WITH(NOLOCK) where isnull(dr,0)=0 and (pk_corp=? or pk_corp=?)) as T ";
		sql += " where pk_portlet_plan not in (select pk_portlet_plan from nw_role_plan WITH(NOLOCK) where isnull(dr,0)=0 and pk_role=?)";
		return dao.queryForList(sql, PortletPlanVO.class, Constants.SYSTEM_CODE, WebUtils.getLoginInfo().getPk_corp(),
				pk_role);
	}

	public int addPlanToRole(String pk_role, String[] pkPlanAry) {
		if(StringUtils.isBlank(pk_role)) {
			return 0;
		}
		// 先删除旧的数据
		RolePlanVO[] oldRolePlanVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(RolePlanVO.class,
				"pk_role=?", pk_role);
		if(oldRolePlanVOs != null) {
			NWDao.getInstance().delete(oldRolePlanVOs);
		}

		if(pkPlanAry == null || pkPlanAry.length == 0) {
			return 0;
		}
		// 加入新的数据
		RolePlanVO[] rolePlanVOs = new RolePlanVO[pkPlanAry.length];
		for(int i = 0; i < pkPlanAry.length; i++) {
			RolePlanVO rolePlanVO = new RolePlanVO();
			rolePlanVO.setPk_role(pk_role);
			rolePlanVO.setPk_portlet_plan(pkPlanAry[i]);
			rolePlanVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(rolePlanVO);
			rolePlanVOs[i] = rolePlanVO;
		}
		dao.saveOrUpdate(rolePlanVOs);
		return pkPlanAry.length;
	}

}

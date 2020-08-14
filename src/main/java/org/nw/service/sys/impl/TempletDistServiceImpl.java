package org.nw.service.sys.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.TPL_STYLE_NAME;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.TempletDistService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.RoleVO;
import org.nw.vo.sys.TempletDistVO;
import org.springframework.stereotype.Service;

/**
 * 模板分配处理类
 * 
 * @author xuqc
 * @date 2012-7-8 上午11:38:52
 */
@Service
public class TempletDistServiceImpl extends AbsToftServiceImpl implements TempletDistService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, TempletDistVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, TempletDistVO.PK_TEMPLET_DIST);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public List<TempletDistVO> loadTempletDist(String pk_fun, String pk_role) {
		String sql = "select * from nw_templet_dist WITH(NOLOCK) where isnull(dr,0)=0 and pk_fun=? and pk_role=?";
		// 已分配的模板
		List<TempletDistVO> distVOs = NWDao.getInstance().queryForList(sql, TempletDistVO.class, pk_fun, pk_role);
		return distVOs;
	}

	/**
	 * XXX 查询模板的时候实际上不需要加上一个like 'ts%'的查询，但是为了以后导出模板数据的方便，web的模板编码都需要使用ts开头 xxx
	 * 已经全部移除nc的模板，不再需要ts标识了
	 */
	@SuppressWarnings("rawtypes")
	public PaginationVO loadTemplet(String pk_fun) {
		FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, "pk_fun=?", pk_fun);
		// 读取可分配的单据模板
		String sql = "select pk_billtemplet as pk_templet,pk_billtypecode as templet_code,bill_templetcaption as templet_name,"
				+ UiConstants.TPL_STYLE.BILL.intValue()
				+ " as tempstyle,'"
				+ TPL_STYLE_NAME.BILL.toString()
				+ "' as tempstyle_name " + "from nw_billtemplet WITH(NOLOCK) where isnull(dr,0)=0 and nodecode=? ";
		sql += " union ";
		// 读取可分配的查询模板
		sql += "select pk_templet,model_code as templet_code,model_name as templet_name,"
				+ UiConstants.TPL_STYLE.QUERY.intValue() + " as tempstyle,'" + TPL_STYLE_NAME.QUERY.toString()
				+ "' as temptyle_name " + "from nw_query_templet WITH(NOLOCK) where isnull(dr,0)=0 and node_code=? ";
		sql += " union ";
		// 读取可分配的打印模板
		sql += "select pk_print_templet as pk_templet,vtemplatecode as templet_code,vtemplatename as templet_name,"
				+ UiConstants.TPL_STYLE.PRINT.intValue() + " as tempstyle,'" + TPL_STYLE_NAME.PRINT.toString()
				+ "' as tempstyle_name " + "from nw_print_templet WITH(NOLOCK) where isnull(dr,0)=0 and nodecode=? ";
		// 读取可分配的报表模板
		sql += " union ";
		sql += "select pk_report_templet as pk_templet,vtemplatecode as templet_code,vtemplatename as templet_name,"
				+ UiConstants.TPL_STYLE.REPORT.intValue() + " as tempstyle,'" + TPL_STYLE_NAME.REPORT.toString()
				+ "' as tempstyle_name " + "from nw_report_templet WITH(NOLOCK) where isnull(dr,0)=0 and nodecode=? ";
		// 读取可分配的数据模板
		sql += " union ";
		sql += "select pk_datatemplet as pk_templet,data_templetcaption as templet_code,data_templetcaption as templet_name,"
				+ UiConstants.TPL_STYLE.DATA.intValue() + " as tempstyle,'" + TPL_STYLE_NAME.DATA.toString()
				+ "' as tempstyle_name " + "from nw_datatemplet WITH(NOLOCK) where isnull(dr,0)=0 and nodecode=? ";
		sql += " order by tempstyle";
		
		List<HashMap> list = (List<HashMap>) NWDao.getInstance().queryForList(sql, HashMap.class, funVO.getFun_code(),
				funVO.getFun_code(), funVO.getFun_code(), funVO.getFun_code(), funVO.getFun_code());
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return paginationVO;
	}

	public int saveTempletDist(List<TempletDistVO> distVOs, String pk_fun, String pk_role) {
		if(StringUtils.isBlank(pk_role) || StringUtils.isBlank(pk_fun)) {
			return 0;
		}
		RoleVO roleVO = NWDao.getInstance().queryByCondition(RoleVO.class, "pk_role=?", pk_role);
		// 先删除旧的数据
		TempletDistVO[] toBeDel = dao.queryForSuperVOArrayByCondition(TempletDistVO.class,
				"pk_fun=? and pk_role=? and pk_corp=?", pk_fun, pk_role, roleVO.getPk_corp());
		dao.delete(toBeDel, false);// 这里使用物理删除
		if(distVOs == null || distVOs.size() == 0) {
			return 0;
		}
		for(TempletDistVO distVO : distVOs) {
			distVO.setStatus(VOStatus.NEW);
			// 父级公司可以为子公司的角色分配模板，这里的公司需要设置角色所在的公司
			distVO.setPk_corp(roleVO.getPk_corp());
			// distVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			NWDao.setUuidPrimaryKey(distVO);
		}
		dao.saveOrUpdate(distVOs.toArray(new TempletDistVO[distVOs.size()]));
		return distVOs.size();
	}

}

package org.nw.web.sys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.service.sys.DataTempletService;
import org.nw.service.sys.FunService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportColumnVO;
import org.nw.vo.sys.ImportConfigBVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据模板
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/dt")
public class DataTempletController extends AbsToftController {

	@Autowired
	private DataTempletService templetService;
	
	@Autowired
	private FunService funService;

	public DataTempletService getService() {
		return templetService;
	}
	
	/**
	 * 查询当前用户授权的菜单
	 * 
	 * @param request
	 * @param response
	 * @param parentFunCode
	 *            父级节点编码
	 * @return
	 */
	@RequestMapping(value = "/getFunTree.json")
	@ResponseBody
	public List<TreeVO> getFunTree(HttpServletRequest request, HttpServletResponse response) {
		TreeVO[] treeVOs = funService.getFunTree(WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo()
				.getPk_user(), Constants.SYSTEM_CODE, true);
		List<TreeVO> funTree = null;
		if(treeVOs == null) {
			funTree = new ArrayList<TreeVO>();
		}
		funTree = Arrays.asList(treeVOs);
		return funTree;
	}
	
	public String getTreePkField() {
		return "pk_fun";
	}
	
	/**
	 * 加载数据<br>
	 * 注：主表和子表都用这一个方法
	 * 
	 * @param request
	 * @param response
	 * @param funCode
	 * @param templateID
	 * @param tabCode
	 * @param isBody
	 * @return
	 */
	@RequestMapping(value = "/loadData.json")
	@ResponseBody
	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		String pk_corp = request.getParameter("pk_corp");
		String extendCond = "1=1", treePk = null;
		if(StringUtils.isBlank(pk_corp) || Constants.SYSTEM_CODE.equals(pk_corp)) {
			extendCond += " and (pk_corp is null or pk_corp='" + Constants.SYSTEM_CODE + "') ";
		} else {
			extendCond += " and pk_corp='" + pk_corp + "'";
		}

		PaginationVO paginationVO = null;

		if(getTreePkField() != null) {
			// 判断是否存在树的情况
			treePk = request.getParameter(getTreePkField());
		}
		if(StringUtils.isNotBlank(treePk)) {
			extendCond += " and " + this.getTreePkField() + "=?";
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond, treePk);
		} else {
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, null);
		}

		return this.genAjaxResponse(true, null, paginationVO);
	}

	@RequestMapping(value = "/move.json")
	@ResponseBody
	public Map<String, Object> move(HttpServletRequest request, HttpServletResponse response) {
		String sql = "select distinct pk_fun from nw_import_column WITH(NOLOCK) where dr=0 and pk_corp=?";
		List<String> funAry = NWDao.getInstance().queryForList(sql,String.class,"1b2bc00334674853baf78818feadf24e");
		ImportColumnVO[] arr = NWDao.getInstance().queryForSuperVOArrayByCondition(ImportColumnVO.class,
				"pk_corp=?","1b2bc00334674853baf78818feadf24e");
		for(String pk_fun : funAry) {
			AggregatedValueObject aggVO = new HYBillVO();
			ImportConfigVO parentVO = new ImportConfigVO();
			parentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(parentVO);
			aggVO.setParentVO(parentVO);
			List<ImportConfigBVO> childVOs = new ArrayList<ImportConfigBVO>();
			for(ImportColumnVO vo : arr) {
				if(vo.getPk_fun().equals(pk_fun)) {
					parentVO.setPk_fun(vo.getPk_fun());
					parentVO.setFun_code(vo.getFun_code());
					if(vo.getChecker() != null) {
						parentVO.setChecker(vo.getChecker());
					}
					parentVO.setPk_corp(vo.getPk_corp());

					ImportConfigBVO childVO = new ImportConfigBVO();
					childVO.setField_code(vo.getField_code());
					childVO.setField_name(vo.getField_name());
					childVO.setKeyfield_flag(vo.getKeyfield_flag());
					childVO.setDisplay_order(vo.getDisplay_order());
					childVO.setStatus(VOStatus.NEW);
					childVO.setPk_import_config(parentVO.getPk_import_config());
					NWDao.setUuidPrimaryKey(childVO);
					childVOs.add(childVO);
				}
			}
			aggVO.setChildrenVO(childVOs.toArray(new ImportConfigBVO[childVOs.size()]));
			NWDao.getInstance().saveOrUpdate(aggVO);
		}

		return this.genAjaxResponse(true, null, null);
	}
}

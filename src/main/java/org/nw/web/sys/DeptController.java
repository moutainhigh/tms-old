package org.nw.web.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.service.sys.DeptService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.DeptVO;
import org.nw.web.AbsTreeFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午03:22:36
 */
@Controller
@RequestMapping(value = "/dept")
public class DeptController extends AbsTreeFormController {

	@Autowired
	private DeptService deptService;

	public DeptService getService() {
		return deptService;
	}

	public String getTreePkField() {
		return DeptVO.PK_DEPT;
	}

	public String getTreeTextField() {
		return DeptVO.DEPT_NAME;
	}

	public String getTreeCodeField() {
		return DeptVO.DEPT_CODE;
	}

	@RequestMapping(value = "/getDeptTree.json")
	@ResponseBody
	public List<TreeVO> getDeptTree(HttpServletRequest request, HttpServletResponse response) {
		String parent_id = request.getParameter("parent_id");
		String isRoot = request.getParameter("isRoot");
		if(Constants.TRUE.equals(isRoot)) {
			parent_id = null;
		}
		return this.getService().getDeptTree(parent_id);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object dept_code = parentVO.getAttributeValue("dept_code");
		if(dept_code == null) {
			throw new RuntimeException("编码不能为空！");
		}
		try {
			DeptVO deptVO = new DeptVO();
			if(StringUtils.isNotBlank(((DeptVO)parentVO).getFatherdept())){
				deptVO = NWDao.getInstance().queryByCondition(DeptVO.class, "dept_code=? AND fatherdept=?", ((DeptVO)parentVO).getDept_code(),((DeptVO)parentVO).getFatherdept());
			}else{
				deptVO = NWDao.getInstance().queryByCondition(DeptVO.class, "dept_code=? AND isnull(fatherdept,'')=''", ((DeptVO)parentVO).getDept_code());
			}
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(deptVO != null) {
					throw new RuntimeException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(deptVO != null) {
					if(!parentVO.getPrimaryKey().equals(deptVO.getPk_dept())) {
						throw new RuntimeException("编码已经存在！");
					}
				}
			}
		} catch(BusinessException e) {
			e.printStackTrace();
		}
	}
}

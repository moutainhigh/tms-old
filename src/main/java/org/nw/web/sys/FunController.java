package org.nw.web.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.service.sys.FunService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.FunVO;
import org.nw.web.AbsTreeFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能注册操作接口
 * 
 * @author xuqc
 * @date 2012-6-16 下午06:23:50
 */
@Controller
@RequestMapping(value = "/fun")
public class FunController extends AbsTreeFormController {

	@Autowired
	private FunService funService;

	public FunService getService() {
		return funService;
	}

	public String getTreePkField() {
		return FunVO.PK_FUN;
	}

	public String getTreeTextField() {
		return FunVO.FUN_NAME;
	}

	public String getTreeCodeField() {
		return null;
	}

	// @Override
	// protected String getFunHelpName(String funCode) {
	// return "/sys/fun.jsp";
	// }

	@RequestMapping(value = "/getFunTree.json")
	@ResponseBody
	public List<TreeVO> getFunTree(HttpServletRequest request, HttpServletResponse response) {
		String parent_id = request.getParameter("parent_id");
		if(StringUtils.isBlank(parent_id)) {
			parent_id = request.getParameter("node");
		}
		String isRoot = request.getParameter("isRoot");
		if(Constants.TRUE.equals(isRoot)) {
			parent_id = null;
		}
		return this.getService().getOriginalFunTree(parent_id);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object fun_code = parentVO.getAttributeValue("fun_code");
		if(fun_code == null) {
			throw new BusiException("功能菜单编码不能为空！");
		}
		try {
			FunVO funVO = this.getService().getFunVOByFunCode(fun_code.toString());
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(funVO != null) {
					throw new BusiException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(funVO != null) {
					if(!parentVO.getPrimaryKey().equals(funVO.getPk_fun())) {
						throw new RuntimeException("编码已经存在！");
					}
				}
			}
			// XXX 2013-9-17有些节点实际上使用同一个单据类型
			// // 单据类型必须唯一
			// Object bill_type = parentVO.getAttributeValue("bill_type");
			// if(bill_type != null &&
			// StringUtils.isNotBlank(bill_type.toString())) {
			// funVO =
			// this.getService().getFunVOByBillType(bill_type.toString());
			// if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
			// // 新增的情况
			// if(funVO != null) {
			// throw new RuntimeException("单据类型已经存在！");
			// }
			// } else {
			// // 修改的情况
			// if(funVO != null) {
			// if(!parentVO.getPrimaryKey().equals(funVO.getPk_fun())) {
			// throw new RuntimeException("单据类型已经存在！");
			// }
			// }
			// }
			// }
		} catch(BusinessException e) {
			e.printStackTrace();
		}
	}
}

package org.nw.web.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.service.sys.CorpService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.CorpVO;
import org.nw.web.AbsTreeFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午02:50:27
 */
@Controller
@RequestMapping(value = "/corp")
public class CorpController extends AbsTreeFormController {

	@Autowired
	private CorpService corpService;

	public CorpService getService() {
		return corpService;
	}

	public String getTreePkField() {
		return CorpVO.PK_CORP;
	}

	public String getTreeCodeField() {
		return CorpVO.CORP_CODE;
	}

	public String getTreeTextField() {
		return CorpVO.CORP_NAME;
	}

	@RequestMapping(value = "/getCorpTree.json")
	@ResponseBody
	public List<TreeVO> getCorpTree(HttpServletRequest request, HttpServletResponse response) {
		String parent_id = request.getParameter("parent_id");
		String isRoot = request.getParameter("isRoot");
		if(Constants.TRUE.equals(isRoot)) {
			parent_id = null;
		}
		return this.getService().getCorpTree(parent_id);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object corp_code = parentVO.getAttributeValue("corp_code");
		if(corp_code == null) {
			throw new BusiException("公司编码不能为空！");
		}
		try {
			CorpVO corpVO = this.getService().getCorpVOByCorpCode(corp_code.toString());
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(corpVO != null) {
					throw new BusiException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(corpVO != null) {
					if(!parentVO.getPrimaryKey().equals(corpVO.getPk_corp())) {
						throw new BusiException("编码已经存在！");
					}
				}
			}
		} catch(BusinessException e) {
			e.printStackTrace();
		}
	}
}

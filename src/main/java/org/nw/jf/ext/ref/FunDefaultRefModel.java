package org.nw.jf.ext.ref;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nw.service.sys.FunService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 功能注册参照
 * 
 * @author xuqc
 * @date 2010-10-25
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "/ref/common/fun")
public class FunDefaultRefModel extends AbstractTreeRefModel {

	@Autowired
	protected FunService funService;

	private static final long serialVersionUID = 4748153476251550291L;

	public Boolean isFillinable() {
		return true;
	}

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		return funService.getFunTree(WebUtils.getLoginInfo().getPk_user(), WebUtils.getLoginInfo().getPk_corp(), null);
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(FunVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(FunVO.class, pk));
	}

	public String getPkFieldCode() {
		return FunVO.FUN_CODE;
	}

	public String getCodeFieldCode() {
		return FunVO.FUN_CODE;
	}

	public String getNameFieldCode() {
		return FunVO.FUN_CODE;
	}
}

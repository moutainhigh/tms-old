package org.nw.jf.ext.ref;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nw.vo.TreeVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 功能注册参照,包括按钮
 * 
 * @author xuqc
 * @date 2010-10-25
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "/ref/common/funwithbtn")
public class FunWithBtnDefaultRefModel extends FunDefaultRefModel {

	private static final long serialVersionUID = 4748153476251550291L;

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		return funService.getFunTreeWithBtn(WebUtils.getLoginInfo().getPk_user(), WebUtils.getLoginInfo().getPk_corp(),
				null);
	}

}

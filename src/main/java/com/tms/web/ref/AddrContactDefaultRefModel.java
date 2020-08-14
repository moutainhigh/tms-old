package com.tms.web.ref;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.AddrContactVO;

/**
 * 地址档案管理的联系人
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/addrcontact")
public class AddrContactDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { AddrContactVO.CONTACT, AddrContactVO.PHONE, AddrContactVO.MOBILE, AddrContactVO.EMAIL, AddrContactVO.MEMO  };
	}

	protected String[] getFieldName() {
		return new String[] { "联系人", "电话", "手机", "邮箱" ,"备注"};
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, AddrContactVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" isnull(locked_flag,'N')='N'");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String pk_address = request.getParameter("pk_address");
		if(StringUtils.isNotBlank(pk_address)) {
			// 选择了地址后才能选择联系人，否则直接录入联系人
			cond.append(" and pk_address='");
			cond.append(pk_address);
			cond.append("'");
		} else {
			// 没有选择地址，不要查询所有联系人，直接返回空，希望用户手动填入联系人
			throw new RuntimeException("必须指定地址PK");
			// return this.genAjaxResponse(true, null, null);
		}

		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(AddrContactVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return AddrContactVO.CONTACT;
	}

	public String getCodeFieldCode() {
		return AddrContactVO.CONTACT;
	}

	public String getNameFieldCode() {
		return AddrContactVO.CONTACT;
	}

	public Map<String, Object> getByCode(String code) {
		// 不需要去查了，返回的都是contact这个字段
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AddrContactVO.CONTACT, code);
		return this.genAjaxResponse(true, null, map);
	}

	public Map<String, Object> getByPk(String pk) {
		// 不需要去查了，返回的都是contact这个字段
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AddrContactVO.CONTACT, pk);
		return this.genAjaxResponse(true, null, map);
	}

	public Boolean isFillinable() {
		return true;
	}

}

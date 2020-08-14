package com.tms.web.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.AddressVO;

/**
 * 地址档案参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/addr")
public class AddressDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { AddressVO.ADDR_CODE, AddressVO.ADDR_NAME,  AddressVO.MEMO,AddressVO.DETAIL_ADDR };
	}

	protected String[] getFieldName() {
		return new String[] { "地址编码", "地址名称", "备注" ,"详细地址"};
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { AddressVO.ADDR_TYPE };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, AddressVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" isnull(locked_flag,'N')='N' ");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
		if(StringUtils.isNotBlank(corpCond)) {
			cond.append(" and ");
			cond.append(corpCond);
		}
		// 如果存在客户参数，那么只选择该客户关联的地址
		String pk_customer = request.getParameter("pk_customer");
		if(StringUtils.isNotBlank(pk_customer) && !"null".equals(pk_customer)) {
			cond.append(" and pk_address in (select pk_address from ts_cust_addr where isnull(dr,0)=0 and pk_customer='");
			cond.append(pk_customer);
			cond.append("')");
		}

		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(AddressVO.class, offset, pageSize, cond.toString());
		List<SuperVO> superVOList = page.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOList.size());
		for(SuperVO vo : superVOList) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		superVOList.clear();

		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getFormulas(), true);
		page.setItems(list);
		return this.genAjaxResponse(true, null, page);
	}

	public String[] getFormulas() {
		return new String[] {
				"datatype_code->\"addr_type\"",
				"pk_data_dict->getcolvalue(nw_data_dict,pk_data_dict,datatype_code,datatype_code)",
				"addr_type_name->getcolvaluemorewithcond(\"nw_data_dict_b\",\"display_name\",\"pk_data_dict\",pk_data_dict,\"value\",addr_type,\"\")" };
	}

	public String getPkFieldCode() {
		return AddressVO.PK_ADDRESS;
	}

	public String getCodeFieldCode() {
		return AddressVO.ADDR_CODE;
	}

	public String getNameFieldCode() {
		return AddressVO.ADDR_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, NWDao.getInstance().queryByCondition(AddressVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and addr_code=? ", code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(AddressVO.class, pk));
	}

	public String[] getMnecode() {
		return new String[] { "addr_name" };
	}

	// @Override
	// public Boolean isFillinable() {
	// return true;
	// }

}

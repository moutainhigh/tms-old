package com.tms.web.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.formula.FormulaParser;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.vo.pub.SuperVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.CarVO;

/**
 * 车辆参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/car")
public class CarDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { CarVO.CARNO, "car_type_name", "car_prop_name" ,"carr_name", CarVO.MEMO};
	}

	protected String[] getFieldName() {
		return new String[] { "车牌号", "车辆类型", "车辆性质" ,"承运商","备注"};
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { CarVO.PK_CAR_TYPE, CarVO.CAR_PROP };
	}

	public Boolean isFillinable() {
		return true;
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String pk_carrier = request.getParameter("pk_carrier");
		String condition = getGridQueryCondition(request, CarVO.class);
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
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier())){
			cond.append(" and ");
			cond.append(" pk_carrier='" +  WebUtils.getLoginInfo().getPk_carrier() + "'");
		}
		if(StringUtils.isNotBlank(pk_carrier)){
			cond.append(" and ");
			cond.append(" pk_carrier='" +  pk_carrier + "'");
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO paginationVO = baseRefService.queryForPagination(CarVO.class, offset, pageSize, cond.toString());

		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<SuperVO> superVOList = paginationVO.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOList.size());
		for(SuperVO vo : superVOList) {
			Map<String, Object> map = new HashMap<String, Object>();
			for(String key : vo.getAttributeNames()) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		superVOList.clear();

		// 执行公式
		FormulaParser formulaParse = new FormulaParser(NWDao.getInstance().getDataSource());
		formulaParse.setFormulas(getFormulas());
		formulaParse.setContext(mapList);
		formulaParse.setMergeContextToResult(true);//
		List<Map<String, Object>> retList = formulaParse.getResult();

		paginationVO.setItems(retList);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	public String[] getFormulas() {
		return new String[] { "car_type_name->getColValue(ts_car_type,name,pk_car_type,pk_car_type)","carr_name->getColValue(ts_carrier,carr_name,pk_carrier,pk_carrier)",
				"car_prop_name->iif(car_prop==1,\"自有\",iif(car_prop==2,\"外协\",iif(car_prop==3,\"挂靠\",car_prop)))" };
	}

	public String getPkFieldCode() {
		return CarVO.CARNO;
	}

	public String getCodeFieldCode() {
		return CarVO.CARNO;
	}

	public String getNameFieldCode() {
		return CarVO.CARNO;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(CarVO.class, code, " locked_flag='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(CarVO.class, pk));
	}

}

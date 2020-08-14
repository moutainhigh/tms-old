package org.nw.web.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.jf.ext.Checkbox;
import org.nw.service.sys.DataDictService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.DataDictBVO;
import org.nw.vo.sys.DataDictVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 数据字典
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:11:10
 */
@Controller
@RequestMapping(value = "/datadict")
public class DataDictController extends AbsToftController {

	@Autowired
	private DataDictService dataDictService;

	public DataDictService getService() {
		return dataDictService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object datatype_code = parentVO.getAttributeValue("datatype_code");
		if(datatype_code == null) {
			throw new BusiException("编码不能为空！");
		}
		try {
			DataDictVO vo = this.getService().getByDatatypeCode(datatype_code.toString());
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(vo != null) {
					throw new BusiException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(vo != null) {
					if(!parentVO.getPrimaryKey().equals(vo.getPk_data_dict())) {
						throw new BusiException("编码已经存在！");
					}
				}
			}
		} catch(BusinessException e) {
			throw new BusiException(e);
		}
	}

	/**
	 * 读取数据字典中的子表数据，为下拉组件提供数据源
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getDataDict4Combo.json")
	@ResponseBody
	public Map<String, Object> getDataDict4Combo(HttpServletRequest request, HttpServletResponse response) {
		String datatype_code = request.getParameter("datatype_code");
		if(StringUtils.isBlank(datatype_code)) {
			throw new BusiException("读取数据字典总的下拉项时，数据类型编码不能为空！");
		}
		AggregatedValueObject billVO = this.dataDictService.getAggVOByDatatypeCode(datatype_code);
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		if(billVO != null) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null) {
				for(CircularlyAccessibleValueObject cvo : cvos) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.TEXT, cvo.getAttributeValue(DataDictBVO.DISPLAY_NAME));
					map.put(Constants.VALUE, cvo.getAttributeValue(DataDictBVO.VALUE));
					list.add(map);
				}
				recordsMap.put("records", list);
			}
		}
		return recordsMap;
	}

	/**
	 * 读取数据字典中的子表数据，为多选组件提供数据源
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getDataDict4MultiSelect.json")
	@ResponseBody
	public List<Checkbox> getDataDict4MultiSelect(HttpServletRequest request, HttpServletResponse response) {
		String datatype_code = request.getParameter("datatype_code");
		if(StringUtils.isBlank(datatype_code)) {
			throw new RuntimeException("读取数据字典总的下拉项时，数据类型编码不能为空！");
		}
		AggregatedValueObject billVO = this.dataDictService.getAggVOByDatatypeCode(datatype_code);
		if(billVO != null) {
			List<Checkbox> list = new ArrayList<Checkbox>();
			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null) {
				for(CircularlyAccessibleValueObject cvo : cvos) {
					Checkbox checkbox = new Checkbox();
					checkbox.setInputValue(cvo.getAttributeValue(DataDictBVO.VALUE).toString());
					checkbox.setBoxLabel(cvo.getAttributeValue(DataDictBVO.DISPLAY_NAME).toString());
					list.add(checkbox);
				}
				return list;
			}
		}
		return null;
	}
}

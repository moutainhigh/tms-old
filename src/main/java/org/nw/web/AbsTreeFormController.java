package org.nw.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.basic.util.Assert;
import org.nw.constants.Constants;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;

/**
 * 用于处理tree-form模式的Panel，这种模式的页面只对树结构进行编辑
 * 
 * @author xuqc
 * @date 2011-2-16
 */
public abstract class AbsTreeFormController extends AbsToftController {

	public static final String IS_ROOT_PARAM = "isRoot";
	public static final String PARENT_NODE_ID_PARAM = "parentNodeId";

	/**
	 * 返回树的text值的字段名称
	 * 
	 * @return
	 */
	public abstract String getTreeTextField();

	/**
	 * 返回树的code值的字段名称
	 * 
	 * @return
	 */
	public String getTreeCodeField() {
		return null;
	}

	/**
	 * 从map数据中返回作为树id的值<br/>
	 * map.get("pk_project_doc_item"),有时候并不一定使用主键
	 * 
	 * @param map
	 *            执行公式后返回的map值，这个map值包含了树所需要的节点信息
	 * @return
	 */
	public String getTreeIdValue(Map<String, Object> map) {
		return (String) map.get(getTreePkField());
	}

	/**
	 * 从map数据中返回作为树text的值<br/>
	 * 有可能是code+name
	 * 
	 * @param map
	 *            执行公式后返回的map值，这个map值包含了树所需要的节点信息
	 * @return
	 */
	public String getTreeTextValue(Map<String, Object> map) {
		return (String) map.get(getTreeTextField());
	}

	/**
	 * 从map数据中返回作为树code的值<br/>
	 * 
	 * @param map
	 *            执行公式后返回的map值，这个map值包含了树所需要的节点信息
	 * @return
	 */
	public String getTreeCodeValue(Map<String, Object> map) {
		return (String) map.get(getTreeCodeField());
	}

	/**
	 * 属性菜单编辑界面的返回信息。
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> genAjaxResponseWithTree(Map<String, Object> view) {
		Object oHeader = view.get(Constants.HEADER);
		if(oHeader != null) {
			Map<String, Object> headerMap = (Map<String, Object>) oHeader;
			view.put("_id", getTreeIdValue(headerMap));
			view.put("_text", getTreeTextValue(headerMap));
			view.put("_code", getTreeCodeValue(headerMap));
		}
		return this.genAjaxResponse(true, null, view);
	}

	public Map<String, Object> show(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		Assert.notNull(getTreePkField());
		String treePk = request.getParameter(getTreePkField());
		Assert.notNull(treePk);
		// 这种情况属于单独编辑一棵树，此时的treePkField其实就是主键
		paramVO.setBillId(treePk);
		String isRoot = request.getParameter(IS_ROOT_PARAM);
		if(isRoot != null && "true".equals(isRoot)) {
			return this.genAjaxResponse(true, null, null);
		}
		AggregatedValueObject billVO = this.getService().show(paramVO);
		Map<String,Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
		return this.genAjaxResponse(true, null, retMap);
	}

	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		billVO = this.getService().save(billVO, paramVO);
		return this.genAjaxResponseWithTree(this.getService().execFormula4Templet(billVO, paramVO));
	}

}

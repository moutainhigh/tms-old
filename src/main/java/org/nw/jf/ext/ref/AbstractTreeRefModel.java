package org.nw.jf.ext.ref;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.jf.ext.ref.win.AbstractRefWindow;
import org.nw.jf.ext.ref.win.TreeRefWindow;
import org.nw.vo.TreeVO;
import org.nw.web.utils.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 树形参照抽象类
 * 
 * @author xuqc
 * @date 2011-6-17
 */
public abstract class AbstractTreeRefModel extends BaseRefModel {

	private static final long serialVersionUID = -6999436682005076667L;

	/**
	 * 树加载数据时请求的url
	 * 
	 * @return
	 */
	final protected String getTreeDataUrl() {
		return WebUtils.getContextPath() + getRequestMappingValue() + "/load4Tree.json";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uft.webnc.jf.ext.ref.BaseRefModel#genRefWindow()
	 */
	public AbstractRefWindow genRefWindow() {
		return new TreeRefWindow(this.getTreeDataUrl());
	}

	/**
	 * 树加载数据的方法
	 * 
	 * 
	 */
	@RequestMapping(value = "/load4Tree.json")
	@ResponseBody
	public abstract List<TreeVO> load4Tree(HttpServletRequest request);

	/**
	 * 异步树的模糊查询是发送一个异步请求，根据code，name进行匹配，返回不分级次的树结构。
	 * 
	 * @param request
	 * @return
	 */
	public String getTreeQueryCondition(HttpServletRequest request, String prefix) {
		String searchValue = request.getParameter(Constants.TREE_QUERY_KEYWORD);
		if(StringUtils.isBlank(searchValue)) {
			return "";
		}
		StringBuffer sb = new StringBuffer("(");
		if(StringUtils.isNotBlank(prefix)) {
			// 加入表名
			sb.append(prefix).append(".");
		}
		sb.append(getCodeFieldCode()).append(" like '%").append(searchValue).append("%' or ");
		if(StringUtils.isNotBlank(prefix)) {
			// 加入表名
			sb.append(prefix).append(".");
		}
		sb.append(getNameFieldCode()).append(" like '%").append(searchValue).append("%' )");
		return sb.toString();
	}
}

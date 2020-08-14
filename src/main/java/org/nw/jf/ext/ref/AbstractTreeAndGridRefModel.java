package org.nw.jf.ext.ref;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nw.jf.ext.ref.win.AbstractRefWindow;
import org.nw.jf.ext.ref.win.TreeAndGridRefWindow;
import org.nw.vo.TreeVO;
import org.nw.web.utils.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 左树右表参照抽象类
 * 
 * @author xuqc
 * @date 2011-6-17
 */
public abstract class AbstractTreeAndGridRefModel extends AbstractGridRefModel {

	private static final long serialVersionUID = -2968153562739455924L;

	/**
	 * 树的pk字段
	 * 
	 * @return
	 */
	protected abstract String getTreePkFieldCode();

	/**
	 * 加载左边树的数据请求的url，子类不需要改变
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
		return new TreeAndGridRefWindow(this.getTreePkFieldCode(), getTreeDataUrl(), getGridDataUrl(), genListColumn());
	}

	/**
	 * 左树右表参照中，加载左边树的数据的方法
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/load4Tree.json")
	@ResponseBody
	public abstract List<TreeVO> load4Tree(HttpServletRequest request);
}

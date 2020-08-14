package org.nw.jf.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.jf.UiConstants;
import org.nw.jf.group.GroupUtils;
import org.nw.jf.utils.FreeMarkerUtils;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.json.JacksonUtils;

/**
 * bill标签解析类 注意：freemarker对null值将直接报错，故对模板变量需要设置默认值
 */
public class ReportTag extends TagSupport {
	private static final long serialVersionUID = -1918472608488600046L;
	private static final Log logger = LogFactory.getLog(ReportTag.class);

	private UiReportTempletVO templetVO;
	private String headerGridPagination; // 表头是否加入工具栏
	private String headerGridDataUrl; // headerGrid加载数据的Url
	private String headerGridImmediatelyLoad = "false"; // 是否即时加载表头的数据,默认false
	private String headerGridPlugins;// 表头的插件
	private String headerGridSortable; // 表头是否能够排序，如果设置成false，那么不能排序，否则根据列定义的排序方式

	private String headerGridPageSizePlugin;// 表头的分页栏是否加上页数选择插件
	private Integer headerGridPageSize;// 表头的页大小，如果定义了这个，那么就不使用全局定义的了
	private String headerGridCheckboxSelectionModel; // 表头是否使用CheckboxSelectionModel
	private String headerGridSingleSelect;// 表头是否可以多选

	public int doEndTag() throws JspException {
		int result = super.doEndTag();
		String templetString = null;
		JspWriter writer = this.pageContext.getOut();

		long beginTime = System.currentTimeMillis();
		// 处理表头的字段分组
		String _headerGridPlugins = null;
		String headerGroupString = GroupUtils.buildGroupHeaderString(
				GroupUtils.getOptionsAry2(templetVO.getFieldVOs()), 1);// 包括一列行号
		if(StringUtils.isNotBlank(headerGroupString)) {
			// 存在分组设置，此时hreaderGroupString是类似[{},{},{}]
			_headerGridPlugins = reBuildPluginString(headerGroupString, headerGridPlugins);
		}

		Map<String, Object> view = new HashMap<String, Object>();
		view.put("fieldVOs", templetVO.getFieldVOs());
		view.put("moduleName", templetVO.getModuleName() == null ? "" : templetVO.getModuleName()); // 模块名称
		view.put("templateID", templetVO.getReportTempletId() == null ? "" : templetVO.getReportTempletId());
		view.put("funCode", templetVO.getFunCode());
		view.put("nodeKey", templetVO.getNodeKey() == null ? "" : templetVO.getNodeKey());
		view.put("lockingItemAry", JacksonUtils.writeValueAsString(templetVO.getLockingItemAry()));

		view.put("isDynReport", String.valueOf(templetVO.isDynReport()));
		if(templetVO.isDynReport()) {// 2015-1-17如果是动态报表，暂时先不分页了
			headerGridPagination = "false";
		}
		view.put("headerGridPagination", headerGridPagination == null ? "true" : headerGridPagination);
		if(headerGridDataUrl == null) {
			headerGridDataUrl = UiConstants.REPORT_LOADDATA_URL;
		}
		view.put("headerGridDataUrl", headerGridDataUrl);
		view.put("headerGridImmediatelyLoad", headerGridImmediatelyLoad);
		view.put("headerGridPlugins", _headerGridPlugins == null ? (headerGridPlugins == null ? "null"
				: headerGridPlugins) : _headerGridPlugins); // 这里默认null，不能用空串
		// 2015-1-20 如果是动态报表，默认进来是不能排序的
		view.put("headerGridSortable", templetVO.isDynReport() ? "false" : (headerGridSortable == null ? "true"
				: headerGridSortable));// 默认不能排序
		view.put("headerGridPageSizePlugin", headerGridPageSizePlugin == null ? "false" : headerGridPageSizePlugin);
		view.put("headerGridPageSize", headerGridPageSize == null ? "null" : headerGridPageSize);
		view.put("headerGridCheckboxSelectionModel", headerGridCheckboxSelectionModel == null ? "false"
				: headerGridCheckboxSelectionModel);
		view.put("headerGridSingleSelect", headerGridSingleSelect == null ? "true" : headerGridSingleSelect);

		// NC功能注册中与生成模板相关的配置项
		view.put("headerHeight", templetVO.getFuncRegisterPropertyVO().getHeaderHeight() == null ? "" : templetVO
				.getFuncRegisterPropertyVO().getHeaderHeight());
		view.put(
				"btnArray",
				templetVO.getFuncRegisterPropertyVO().getBtnArray() == null ? "" : JacksonUtils
						.writeValueAsString(templetVO.getFuncRegisterPropertyVO().getBtnArray()));

		logger.debug("准备FreeMarker模板数据时间:" + (System.currentTimeMillis() - beginTime));
		try {
			beginTime = System.currentTimeMillis();
			templetString = FreeMarkerUtils.processTemplateIntoString(templetVO.getFmTemplate(), view);
			logger.debug("FreeMarker模板解析时间:" + (System.currentTimeMillis() - beginTime));
		} catch(Exception e) {
			logger.error("freemarker解析错误！", e);
			try {
				writer.append("<!-- FREEMARKER ERROR MESSAGE STARTS HERE -->"
						+ "<script language=javascript>//\"></script>"
						+ "<script language=javascript>//\'></script>"
						+ "<script language=javascript>//\"></script>"
						+ "<script language=javascript>//\'></script>"
						+ "</title></xmp></script></noscript></style></object>"
						+ "</head></pre></table>"
						+ "</form></table></table></table></a></u></i></b>"
						+ "<div align='center' style='font-family:Arial,font-style: normal;font-size:20px'><b>FreeMarker模板解析错误</b></div><div align=left "
						+ "style='background-color:white; color:black; " + "display:block;padding:2pt; "
						+ "font-size:medium; font-family:Arial,sans-serif; "
						+ "font-style: normal; font-variant: normal; " + "font-weight: normal; text-decoration: none; "
						+ "text-transform: none'>" + "<b style='font-size:medium'>" + e.getMessage() + "</b><br>"
						+ (e.getCause() == null ? "" : e.getCause().getMessage()) + "" + "<br/>模板ID:"
						+ templetVO.getReportTempletId() + "<pre><xmp>");
				writer.append("</xmp></pre></div></html>");
				writer.flush();
			} catch(IOException e1) {
			}
			return result;
		}
		try {
			writer.write(templetString);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @param groupString
	 *            new Ext.ux.grid.ColumnHeaderGroup({rows : []})
	 * @param oriPluginString
	 *            [abc,null]
	 * @return
	 * @author xuqc
	 * @date 2013-3-28
	 * 
	 */
	public String reBuildPluginString(String groupString, String oriPluginString) {
		if(StringUtils.isNotBlank(oriPluginString)) {
			if(!oriPluginString.startsWith("[") || !oriPluginString.endsWith("]")) {
				// 插件必须以[]开头和结尾
				return "null";
			}
			String str = oriPluginString.substring(1, oriPluginString.length() - 1);// 过滤第一个和最后一个字符
			return "[" + groupString + "," + str + "]";
		} else {
			return "[" + groupString + "]";
		}
	}

	public String getHeaderGridDataUrl() {
		return headerGridDataUrl;
	}

	public void setHeaderGridDataUrl(String headerGridDataUrl) {
		this.headerGridDataUrl = headerGridDataUrl;
	}

	public String getHeaderGridImmediatelyLoad() {
		return headerGridImmediatelyLoad;
	}

	public void setHeaderGridImmediatelyLoad(String headerGridImmediatelyLoad) {
		this.headerGridImmediatelyLoad = headerGridImmediatelyLoad;
	}

	public String getHeaderGridPagination() {
		return headerGridPagination;
	}

	public void setHeaderGridPagination(String headerGridPagination) {
		this.headerGridPagination = headerGridPagination;
	}

	public String getHeaderGridPlugins() {
		return headerGridPlugins;
	}

	public void setHeaderGridPlugins(String headerGridPlugins) {
		this.headerGridPlugins = headerGridPlugins;
	}

	public String getHeaderGridSortable() {
		return headerGridSortable;
	}

	public void setHeaderGridSortable(String headerGridSortable) {
		this.headerGridSortable = headerGridSortable;
	}

	public String getHeaderGridPageSizePlugin() {
		return headerGridPageSizePlugin;
	}

	public void setHeaderGridPageSizePlugin(String headerGridPageSizePlugin) {
		this.headerGridPageSizePlugin = headerGridPageSizePlugin;
	}

	public Integer getHeaderGridPageSize() {
		return headerGridPageSize;
	}

	public void setHeaderGridPageSize(Integer headerGridPageSize) {
		this.headerGridPageSize = headerGridPageSize;
	}

	public UiReportTempletVO getTempletVO() {
		return templetVO;
	}

	public void setTempletVO(UiReportTempletVO templetVO) {
		this.templetVO = templetVO;
	}

	public String getHeaderGridCheckboxSelectionModel() {
		return headerGridCheckboxSelectionModel;
	}

	public void setHeaderGridCheckboxSelectionModel(String headerGridCheckboxSelectionModel) {
		this.headerGridCheckboxSelectionModel = headerGridCheckboxSelectionModel;
	}

	public String getHeaderGridSingleSelect() {
		return headerGridSingleSelect;
	}

	public void setHeaderGridSingleSelect(String headerGridSingleSelect) {
		this.headerGridSingleSelect = headerGridSingleSelect;
	}
}

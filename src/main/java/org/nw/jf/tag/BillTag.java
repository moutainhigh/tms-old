package org.nw.jf.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.basic.util.BeanUtils;
import org.nw.basic.util.ClassUtils;
import org.nw.constants.Constants;
import org.nw.jf.UiConstants;
import org.nw.jf.group.GroupUtils;
import org.nw.jf.utils.FreeMarkerUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.SuperVO;
import org.springframework.beans.factory.parsing.BeanEntry;

/**
 * bill标签解析类 注意：freemarker对null值将直接报错，故对模板变量需要设置默认值
 */
public class BillTag extends TagSupport {
	private static final long serialVersionUID = -1918472608488600046L;
	private static final Log logger = LogFactory.getLog(BillTag.class);

	private UiBillTempletVO templetVO;
	private String isBuildHeaderCard; // 是否生成headerCard组件
	private String isBuildHeaderGrid; // 是否生成headerGrid组件
	private Integer labelWidth = UiConstants.DEFAULT_LABEL_WIDTH;// 卡片的标签宽度
	private Integer tableColumns = UiConstants.DEFAULT_TABLE_COLUMNS; // 卡片的列数
	private String headerGridPagination; // 表头是否加入工具栏
	private String headerGridDataUrl; // headerGrid加载数据的Url
	private String headerGridImmediatelyLoad; // 是否即时加载表头的数据,默认false
	private String headerGridSingleSelect;// 表头是否可以多选
	private String headerGridCheckboxSelectionModel; // 表头是否使用CheckboxSelectionModel
	private String headerGridPlugins;// 表头的插件
	private String headerGridSortable; // 表头是否能够排序，如果设置成false，那么不能排序，否则根据列定义的排序方式
	private String headerGridPageSizePlugin;// 表头的分页栏是否加上页数选择插件
	private Integer headerGridPageSize;// 表头的页大小，如果定义了这个，那么就不使用全局定义的了
	private String headerGridBufferView;// 表头是否使用BufferView

	private String bodyGridsImmediatelyLoad; // 是否即时加载表体的数据,默认都为false，注意多表体使用,分隔
	private String bodyGridsCheckboxSelectionModel; // 表体是否使用CheckboxSelectionModel，多个表体以,分隔
	private String bodyGridsSingleSelect; // 表体是否可以多选，多个表体以,分隔
	private String bodyGridsPagination;// 表体是否加入分页工具栏，多个表体以,分隔
	private String bodyGridsBufferView;// 表体是否使用BufferView
	private String bodyGridsDataUrl; // 表体的url，多个表体以,分隔
	private String bodyGridsType; // 表体的类型，多个类型使用,分隔
	private String bodyGridsPlugins;// 表体的插件
	private String bodyGridsNewRowWhenWalkInLastCell;// 在最后一个单元格中点击Enter时，是否自动增加一行
	private String bodyGridsDragDropRowOrder;// 表体能否通过鼠标的拖放来调整顺序,开启该功能需要引入相应的DD包
	private String bodyGridsSortable;// 表体是否能够排序，如果设置成false，那么不能排序，否则根据列定义的排序方式

	private Boolean useFieldSetInHeader = false; // 表头的多页签是否使用fieldSet显示
	// 表头是否使用tab页签，默认情况根据表头定义的tab个数来确定，如果定义了多个tab，那么使用tabpanel。但是某些情况在定义一个tab的情况下也想使用tabpanel
	private Boolean isShowTab = false;

	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		int result = super.doEndTag();
		String templetString = null;
		JspWriter writer = this.pageContext.getOut();

		long beginTime = System.currentTimeMillis();
		// if(templetVO.getMaxDefaultshownameLength() <= 12) {
		// this.labelWidth = UiConstants.LABEL_WIDTH[0];
		// } else if(templetVO.getMaxDefaultshownameLength() <= 18) {
		// this.labelWidth = UiConstants.LABEL_WIDTH[1];
		// } else {
		// this.labelWidth = UiConstants.LABEL_WIDTH[2];
		// }
		Map<String, Map<String, Object>> components = new LinkedHashMap<String, Map<String, Object>>();
		Map<String, Collection<?>> fieldVosMap = UiTempletUtils.filterByPos(templetVO.getFieldVOs());
		Map<String, Collection<?>> tabVosMap = UiTempletUtils.filterByPos(templetVO.getTabVOs());
		int bodyTabNum = 0;
		// 处理表头分组
		String _headerGridPlugins = null;
		// 处理表体部分的字段分组
		String[] bodyGridsPluginsAry = null;
		if(StringUtils.isNotBlank(bodyGridsPlugins)) {
			bodyGridsPluginsAry = bodyGridsPlugins.split(",");
		}
		String[] _bodyGridsCheckboxSelectionModel = null;
		for(Integer pos : UiConstants.POS) {
			Map<String, Object> temp = new HashMap<String, Object>();
			List<BillTempletBVO> onePosFieldVos = (List<BillTempletBVO>) fieldVosMap.get(pos.toString());
			List<BillTempletTVO> onePosTabVos = (List<BillTempletTVO>) tabVosMap.get(pos.toString());
			Map<String, Collection<?>> onePosFieldVosMapWithTab = UiTempletUtils.filterByTab(onePosFieldVos,
					onePosTabVos);
			if(pos.intValue() != UiConstants.POS[1]) {
				// 处理表头的字段分组
				String headerGroupString = GroupUtils
						.buildGroupHeaderString(GroupUtils.getOptionsAry(onePosFieldVos),
								(headerGridCheckboxSelectionModel == null || "false"
										.equals(headerGridCheckboxSelectionModel)) ? 1 : 2);// 默认包括一个行号列，如果加上checkbox列则数字为2
				if(StringUtils.isNotBlank(headerGroupString)) {
					// 存在分组设置，此时hreaderGroupString是类似[{},{},{}]
					_headerGridPlugins = reBuildPluginString(headerGroupString, headerGridPlugins);
				}
			}
			temp.put("onePosFieldVos", onePosFieldVos);
			temp.put("onePosTabVos", onePosTabVos);
			temp.put("onePosFieldVosMapWithTab", onePosFieldVosMapWithTab);
			components.put(String.valueOf(pos), temp);
			if(pos == UiConstants.POS[1]) {
				// 设置tab number
				bodyTabNum = onePosTabVos.size();

				if(bodyGridsCheckboxSelectionModel == null) {
					if(bodyTabNum == 0) {
						// 表示没有表体，但是freemarker模板中有引用到该值，所以必须设置一个默认值
						_bodyGridsCheckboxSelectionModel = new String[] { "false" };
					} else {
						_bodyGridsCheckboxSelectionModel = new String[bodyTabNum];
						for(int i = 0; i < bodyTabNum; i++) {
							_bodyGridsCheckboxSelectionModel[i] = "false";
						}
					}
				} else {
					_bodyGridsCheckboxSelectionModel = filling(bodyGridsCheckboxSelectionModel.split(","), bodyTabNum,
							"false");
				}

				if(bodyGridsPluginsAry == null) {
					bodyGridsPluginsAry = new String[onePosFieldVosMapWithTab.size()];
				}
				int index = 0;
				for(String key : onePosFieldVosMapWithTab.keySet()) {
					List<BillTempletBVO> onePosFieldVosWithTab = (List<BillTempletBVO>) onePosFieldVosMapWithTab
							.get(key);
					String bodyGroupStringWithTab = GroupUtils.buildGroupHeaderString(
							GroupUtils.getOptionsAry(onePosFieldVosWithTab),
							"false".equals(_bodyGridsCheckboxSelectionModel[index]) ? 1 : 2);
					if(StringUtils.isNotBlank(bodyGroupStringWithTab)) {
						// 存在分组设置，此时hreaderGroupString是类似[{},{},{}]
						bodyGridsPluginsAry[index] = reBuildPluginString(bodyGroupStringWithTab,
								bodyGridsPluginsAry[index]);
					} else {
						// 如果从tag中传入了，则不改变，否则设置默认值null
						bodyGridsPluginsAry[index] = bodyGridsPluginsAry[index] == null ? "null"
								: bodyGridsPluginsAry[index];
					}
					index++;
				}
			}
			// 表尾的部分通常是加入表头的列表显示
			if(pos == UiConstants.POS[2]) {
				List<BillTempletBVO> pos0FieldVos = (List<BillTempletBVO>) components.get("0").get("onePosFieldVos");
				for(int i = 0; i < onePosFieldVos.size(); i++) {
					BillTempletBVO vo = onePosFieldVos.get(i);
					if(vo.getListflag().equals(Constants.YES)) {
						pos0FieldVos.add(vo);
					}
				}
			}
		}
		Map<String, Object> view = new HashMap<String, Object>();
		view.put("moduleName", templetVO.getModuleName() == null ? "" : templetVO.getModuleName()); // 模块名称
		view.put("components", components);
		view.put("templateID", templetVO.getTemplateID() == null ? "" : templetVO.getTemplateID());
		view.put("headerTabCode", templetVO.getHeaderTabCode() == null ? "" : templetVO.getHeaderTabCode());
		view.put("firstHeaderTabCode",
				templetVO.getFirstHeaderTabCode() == null ? "" : templetVO.getFirstHeaderTabCode());
		view.put("bodyTabCode", templetVO.getBodyTabCode() == null ? "" : templetVO.getBodyTabCode());
		view.put("funCode", templetVO.getFunCode());
		view.put("nodeKey", templetVO.getNodeKey() == null ? "" : templetVO.getNodeKey());
		view.put("headerPkField", templetVO.getHeaderPkField() == null ? "" : templetVO.getHeaderPkField());
		view.put("childrenPkFieldMap", templetVO.getChildrenPkFieldMap() == null ? new HashMap<String, Object>()
				: templetVO.getChildrenPkFieldMap());
		view.put("lockingItemAry", JacksonUtils.writeValueAsString(templetVO.getLockingItemAry()));
		view.put("billType", templetVO.getBillType() == null ? "" : templetVO.getBillType());
		view.put("billId", templetVO.getBillId() == null ? "" : templetVO.getBillId());
		view.put("billIds", templetVO.getBillIds() == null ? "" : templetVO.getBillIds());
		view.put("pk_checkflow", templetVO.getPk_checkflow() == null ? "" : templetVO.getPk_checkflow());
		/**
		 * 表头部分的设置
		 */
		view.put("isBuildHeaderCard", isBuildHeaderCard == null ? "true" : isBuildHeaderCard);
		if(UiConstants.NODEKEY_VIEW.equalsIgnoreCase(templetVO.getNodeKey())) {
			view.put("isBuildHeaderGrid", "false");
		} else {
			view.put("isBuildHeaderGrid", isBuildHeaderGrid == null ? "true" : isBuildHeaderGrid);
		}

		view.put("labelWidth", labelWidth == null ? 0 : labelWidth);
		view.put("tableColumns", isIE6() ? "3" : tableColumns); // 如果是IE6，则始终使用3列
		view.put("headerGridPagination", headerGridPagination == null ? "true" : headerGridPagination);
		view.put("headerGridBufferView", headerGridBufferView == null ? "false" : headerGridBufferView);
		if(headerGridDataUrl == null) {
			headerGridDataUrl = UiConstants.BILL_LOADDATA_URL;
		}
		view.put("headerGridDataUrl", headerGridDataUrl);
		// 一定要转成string类型，神经病一样
		if(StringUtils.isNotBlank(templetVO.getBillId())) {
			// 2011-08-30
			// 当存在billId时，不自动加载表头数据
			headerGridImmediatelyLoad = "false";
		}
		if(StringUtils.isNotBlank(templetVO.getBillIds())) {
			// 2016-07-21 XIA
			// 当存在billIds时，自动加载表头数据
			headerGridImmediatelyLoad = "true";
		}
		view.put("headerGridImmediatelyLoad",
				headerGridImmediatelyLoad == null ? templetVO.isHeaderGridImmediatelyLoad() + ""
						: headerGridImmediatelyLoad);
		
		view.put("headerGridCheckboxSelectionModel", headerGridCheckboxSelectionModel == null ? "false"
				: headerGridCheckboxSelectionModel);
		view.put("headerGridSingleSelect", headerGridSingleSelect == null ? "true" : headerGridSingleSelect);
		view.put("headerGridPlugins", _headerGridPlugins == null ? (headerGridPlugins == null ? "null"
				: headerGridPlugins) : _headerGridPlugins); // 这里默认null，不能用空串
		view.put("headerGridSortable", headerGridSortable == null ? "true" : headerGridSortable);
		view.put("headerGridPageSizePlugin", headerGridPageSizePlugin == null ? "false" : headerGridPageSizePlugin);
		view.put("headerGridPageSize", headerGridPageSize == null ? "null" : headerGridPageSize);

		/**
		 * 是否生成表体部分,当表体的所有字段都隐藏的时候不生成表体组件
		 */
		view.put("hideBodyGrid", String.valueOf(templetVO.isHideBodyGrid()));
		/**
		 * 表体部分的设置
		 */
		String[] _bodyGridsPagination = null;
		String[] _bodyGridsBufferView = null;
		String[] _bodyGridsDataUrl = null;
		String[] _bodyGridsType = null;
		String[] _bodyGridsImmediatelyLoad = null;
		String[] _bodyGridsSingleSelect = null;
		String[] _bodyGridsNewRowWhenWalkInLastCell = null;
		String[] _bodyGridsDragDropRowOrder = null;
		String[] _bodyGridsSortable = null;

		if(bodyGridsCheckboxSelectionModel == null) {
			if(bodyTabNum == 0) {
				// 表示没有表体，但是freemarker模板中有引用到该值，所以必须设置一个默认值
				_bodyGridsCheckboxSelectionModel = new String[] { "false" };
			} else {
				_bodyGridsCheckboxSelectionModel = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsCheckboxSelectionModel[i] = "false";
				}
			}
		} else {
			_bodyGridsCheckboxSelectionModel = filling(bodyGridsCheckboxSelectionModel.split(","), bodyTabNum, "false");
		}
		if(bodyGridsSingleSelect == null) {
			// 没有设置bodyGridsDataUrl时，所有tab页都使用headGridDataUrl
			if(bodyTabNum == 0) {
				_bodyGridsSingleSelect = new String[] { "true" };
			} else {
				_bodyGridsSingleSelect = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsSingleSelect[i] = "true";
				}
			}
		} else {
			_bodyGridsSingleSelect = filling(bodyGridsSingleSelect.split(","), bodyTabNum, "true");
		}
		if(bodyGridsPagination == null) {
			if(bodyTabNum == 0) {
				_bodyGridsPagination = new String[] { "false" };
			} else {
				_bodyGridsPagination = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsPagination[i] = "false";
				}
			}
		} else {
			_bodyGridsPagination = filling(bodyGridsPagination.split(","), bodyTabNum, "false");
		}
		if(bodyGridsBufferView == null) {
			if(bodyTabNum == 0) {
				_bodyGridsBufferView = new String[] { "false" };
			} else {
				_bodyGridsBufferView = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsBufferView[i] = "false";
				}
			}
		} else {
			_bodyGridsBufferView = filling(bodyGridsBufferView.split(","), bodyTabNum, "false");
		}
		if(bodyGridsDataUrl == null) {
			if(bodyTabNum == 0) {
				_bodyGridsDataUrl = new String[] { UiConstants.BILL_LOADDATA_URL };
			} else {
				_bodyGridsDataUrl = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsDataUrl[i] = headerGridDataUrl;
				}
			}
		} else {
			_bodyGridsDataUrl = filling(bodyGridsDataUrl.split(","), bodyTabNum, UiConstants.BILL_LOADDATA_URL);
		}
		if(bodyGridsType == null) {
			// 没有设置bodyGridsType时，所有tab也都使用editorGrid
			if(bodyTabNum == 0) {
				_bodyGridsType = new String[] { UiConstants.EXT_GRID_CLASS[1] };
			} else {
				_bodyGridsType = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsType[i] = UiConstants.EXT_GRID_CLASS[1];
				}
			}
		} else {
			_bodyGridsType = filling(bodyGridsType.split(","), bodyTabNum, UiConstants.EXT_GRID_CLASS[1]);
		}
		if(bodyGridsImmediatelyLoad == null) {
			if(bodyTabNum == 0) {
				_bodyGridsImmediatelyLoad = new String[] { "false" };
			} else {
				_bodyGridsImmediatelyLoad = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsImmediatelyLoad[i] = "false";
				}
			}
		} else {
			_bodyGridsImmediatelyLoad = filling(bodyGridsImmediatelyLoad.split(","), bodyTabNum, "false");
		}
		if(bodyGridsNewRowWhenWalkInLastCell == null) {
			if(bodyTabNum == 0) {
				_bodyGridsNewRowWhenWalkInLastCell = new String[] { "true" };
			} else {
				_bodyGridsNewRowWhenWalkInLastCell = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsNewRowWhenWalkInLastCell[i] = "true";
				}
			}
		} else {
			_bodyGridsNewRowWhenWalkInLastCell = filling(bodyGridsNewRowWhenWalkInLastCell.split(","), bodyTabNum,
					"true");
		}
		if(bodyGridsDragDropRowOrder == null) {
			if(bodyTabNum == 0) {
				_bodyGridsDragDropRowOrder = new String[] { "false" };
			} else {
				_bodyGridsDragDropRowOrder = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsDragDropRowOrder[i] = "false";
				}
			}
		} else {
			_bodyGridsDragDropRowOrder = filling(bodyGridsDragDropRowOrder.split(","), bodyTabNum, "false");
		}
		if(bodyGridsSortable == null) {
			if(bodyTabNum == 0) {
				_bodyGridsSortable = new String[] { "true" };
			} else {
				_bodyGridsSortable = new String[bodyTabNum];
				for(int i = 0; i < bodyTabNum; i++) {
					_bodyGridsSortable[i] = "true";
				}
			}
		} else {
			_bodyGridsSortable = filling(bodyGridsSortable.split(","), bodyTabNum, "true");
		}

		view.put("bodyGridsCheckboxSelectionModel", _bodyGridsCheckboxSelectionModel);
		view.put("bodyGridsSingleSelect", _bodyGridsSingleSelect);
		view.put("bodyGridsPagination", _bodyGridsPagination);
		view.put("bodyGridsBufferView", _bodyGridsBufferView);
		view.put("bodyGridsDataUrl", _bodyGridsDataUrl);
		view.put("bodyGridsType", _bodyGridsType);
		view.put("bodyGridsImmediatelyLoad", _bodyGridsImmediatelyLoad);
		view.put("bodyGridsPlugins", bodyGridsPluginsAry);
		view.put("bodyGridsNewRowWhenWalkInLastCell", _bodyGridsNewRowWhenWalkInLastCell);
		view.put("bodyGridsDragDropRowOrder", _bodyGridsDragDropRowOrder);
		view.put("bodyGridsSortable", _bodyGridsSortable);

		view.put("useFieldSetInHeader", useFieldSetInHeader);
		view.put("isShowTab", isShowTab);

		// NC功能注册中与生成模板相关的配置项
		view.put("headerHeight", templetVO.getFuncRegisterPropertyVO().getHeaderHeight() == null ? "" : templetVO
				.getFuncRegisterPropertyVO().getHeaderHeight());
		view.put("headerSplit", templetVO.getFuncRegisterPropertyVO().getHeaderSplit() == null ? "false" : templetVO
				.getFuncRegisterPropertyVO().getHeaderSplit().toString());
		view.put("waterfallScene", templetVO.getFuncRegisterPropertyVO().getWaterfallScene() == null ? "false"
				: templetVO.getFuncRegisterPropertyVO().getWaterfallScene().toString());
		view.put("bodyWaterfallScene", templetVO.getFuncRegisterPropertyVO().getBodyWaterfallScene() == null ? "false"
				: templetVO.getFuncRegisterPropertyVO().getBodyWaterfallScene().toString());
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
						+ templetVO.getTemplateID() + "<pre><xmp>");
				writer.append("</xmp></pre></div></html>");
				writer.flush();
			} catch(IOException e1) {
			}
			labelWidth = null;
			return result;
		}
		try {
			writer.write(templetString);
		} catch(IOException e) {
			e.printStackTrace();
		}
		labelWidth = null;
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

	/**
	 * 判断浏览器的版本是否是IE6
	 * 
	 * @return
	 * @author xuqc
	 * @date 2011-11-4
	 */
	private boolean isIE6() {
		HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
		String userAgent = request.getHeader("User-Agent");
		return userAgent.indexOf("MSIE 6.0") > -1;
	}

	/**
	 * 填充original数组中的元素，达到num个数，使用defaultValue默认值<br/>
	 * 对于参照制单，页面只定义一个默认值，但是可能有多个页签
	 * 
	 * @param original
	 * @param num
	 * @return
	 */
	private String[] filling(String[] original, int num, String defaultValue) {
		if(original.length >= num) {
			return original;
		}
		String[] arr = new String[num];
		for(int i = 0; i < original.length; i++) {
			arr[i] = original[i];
		}
		for(int i = original.length; i < num; i++) {
			arr[i] = defaultValue;
		}
		return arr;
	}

	public UiBillTempletVO getTempletVO() {
		return templetVO;
	}

	public void setTempletVO(UiBillTempletVO templetVO) {
		this.templetVO = templetVO;
	}

	public Integer getTableColumns() {
		return tableColumns;
	}

	public void setTableColumns(Integer tableColumns) {
		this.tableColumns = tableColumns;
	}

	public Integer getLabelWidth() {
		return labelWidth;
	}

	public void setLabelWidth(Integer labelWidth) {
		this.labelWidth = labelWidth;
	}

	public String getHeaderGridDataUrl() {
		return headerGridDataUrl;
	}

	public void setHeaderGridDataUrl(String headerGridDataUrl) {
		this.headerGridDataUrl = headerGridDataUrl;
	}

	public String getBodyGridsDataUrl() {
		return bodyGridsDataUrl;
	}

	public void setBodyGridsDataUrl(String bodyGridsDataUrl) {
		this.bodyGridsDataUrl = bodyGridsDataUrl;
	}

	public String getHeaderGridImmediatelyLoad() {
		return headerGridImmediatelyLoad;
	}

	public void setHeaderGridImmediatelyLoad(String headerGridImmediatelyLoad) {
		this.headerGridImmediatelyLoad = headerGridImmediatelyLoad;
	}

	public String getBodyGridsType() {
		return bodyGridsType;
	}

	public void setBodyGridsType(String bodyGridsType) {
		this.bodyGridsType = bodyGridsType;
	}

	public String getHeaderGridCheckboxSelectionModel() {
		return headerGridCheckboxSelectionModel;
	}

	public void setHeaderGridCheckboxSelectionModel(String headerGridCheckboxSelectionModel) {
		this.headerGridCheckboxSelectionModel = headerGridCheckboxSelectionModel;
	}

	/**
	 * @return the headerGridSingleSelect
	 */
	public String getHeaderGridSingleSelect() {
		return headerGridSingleSelect;
	}

	/**
	 * @param headerGridSingleSelect
	 *            the headerGridSingleSelect to set
	 */
	public void setHeaderGridSingleSelect(String headerGridSingleSelect) {
		this.headerGridSingleSelect = headerGridSingleSelect;
	}

	/**
	 * @return the bodyGridsPagination
	 */
	public String getBodyGridsPagination() {
		return bodyGridsPagination;
	}

	/**
	 * @param bodyGridsPagination
	 *            the bodyGridsPagination to set
	 */
	public void setBodyGridsPagination(String bodyGridsPagination) {
		this.bodyGridsPagination = bodyGridsPagination;
	}

	public String getIsBuildHeaderCard() {
		return isBuildHeaderCard;
	}

	public void setIsBuildHeaderCard(String isBuildHeaderCard) {
		this.isBuildHeaderCard = isBuildHeaderCard;
	}

	public String getIsBuildHeaderGrid() {
		return isBuildHeaderGrid;
	}

	public void setIsBuildHeaderGrid(String isBuildHeaderGrid) {
		this.isBuildHeaderGrid = isBuildHeaderGrid;
	}

	/**
	 * @return the bodyGridsImmediatelyLoad
	 */
	public String getBodyGridsImmediatelyLoad() {
		return bodyGridsImmediatelyLoad;
	}

	/**
	 * @param bodyGridsImmediatelyLoad
	 *            the bodyGridsImmediatelyLoad to set
	 */
	public void setBodyGridsImmediatelyLoad(String bodyGridsImmediatelyLoad) {
		this.bodyGridsImmediatelyLoad = bodyGridsImmediatelyLoad;
	}

	/**
	 * @return the bodyGridsCheckboxSelectionModel
	 */
	public String getBodyGridsCheckboxSelectionModel() {
		return bodyGridsCheckboxSelectionModel;
	}

	/**
	 * @param bodyGridsCheckboxSelectionModel
	 *            the bodyGridsCheckboxSelectionModel to set
	 */
	public void setBodyGridsCheckboxSelectionModel(String bodyGridsCheckboxSelectionModel) {
		this.bodyGridsCheckboxSelectionModel = bodyGridsCheckboxSelectionModel;
	}

	/**
	 * @return the bodyGridsSingleSelect
	 */
	public String getBodyGridsSingleSelect() {
		return bodyGridsSingleSelect;
	}

	/**
	 * @param bodyGridsSingleSelect
	 *            the bodyGridsSingleSelect to set
	 */
	public void setBodyGridsSingleSelect(String bodyGridsSingleSelect) {
		this.bodyGridsSingleSelect = bodyGridsSingleSelect;
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

	public String getBodyGridsPlugins() {
		return bodyGridsPlugins;
	}

	public void setBodyGridsPlugins(String bodyGridsPlugins) {
		this.bodyGridsPlugins = bodyGridsPlugins;
	}

	public String getBodyGridsNewRowWhenWalkInLastCell() {
		return bodyGridsNewRowWhenWalkInLastCell;
	}

	public void setBodyGridsNewRowWhenWalkInLastCell(String bodyGridsNewRowWhenWalkInLastCell) {
		this.bodyGridsNewRowWhenWalkInLastCell = bodyGridsNewRowWhenWalkInLastCell;
	}

	public Boolean getUseFieldSetInHeader() {
		return useFieldSetInHeader;
	}

	public void setUseFieldSetInHeader(Boolean useFieldSetInHeader) {
		this.useFieldSetInHeader = useFieldSetInHeader;
	}

	public String getBodyGridsDragDropRowOrder() {
		return bodyGridsDragDropRowOrder;
	}

	public void setBodyGridsDragDropRowOrder(String bodyGridsDragDropRowOrder) {
		this.bodyGridsDragDropRowOrder = bodyGridsDragDropRowOrder;
	}

	public String getHeaderGridSortable() {
		return headerGridSortable;
	}

	public void setHeaderGridSortable(String headerGridSortable) {
		this.headerGridSortable = headerGridSortable;
	}

	public String getBodyGridsSortable() {
		return bodyGridsSortable;
	}

	public void setBodyGridsSortable(String bodyGridsSortable) {
		this.bodyGridsSortable = bodyGridsSortable;
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

	public String getHeaderGridBufferView() {
		return headerGridBufferView;
	}

	public void setHeaderGridBufferView(String headerGridBufferView) {
		this.headerGridBufferView = headerGridBufferView;
	}

	public String getBodyGridsBufferView() {
		return bodyGridsBufferView;
	}

	public void setBodyGridsBufferView(String bodyGridsBufferView) {
		this.bodyGridsBufferView = bodyGridsBufferView;
	}

	public Boolean getIsShowTab() {
		return isShowTab;
	}

	public void setIsShowTab(Boolean isShowTab) {
		this.isShowTab = isShowTab;
	}

}

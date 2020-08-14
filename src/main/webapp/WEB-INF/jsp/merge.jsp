<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="uft" uri="/WEB-INF/tlds/compress.tld" %>
<html>
	<head>
		<title>JS & CSS 合并压缩</title>
		<uft:compress jsTo="/js/base-min.js">
			<!--	注意引入文件的顺序不能随意修改	-->
			<script type="text/javascript" src="/js/core/common.js"></script>
			<script type="text/javascript" src="/js/core/date.js"></script>
			<script type="text/javascript" src="/js/core/json2.js"></script>
			<script type="text/javascript" src="/js/core/Utils.js"></script>
			
			<!--Ext框架-->
			<script type="text/javascript" src="/js/ext/extjs/adapter/ext/ext-base-debug.js"></script>
			<script type="text/javascript" src="/js/ext/extjs/ext-all-debug.js"></script>
			<script type="text/javascript" src="/js/ext/extjs/locale/ext-lang-zh_cn.js"></script> 
			<script type="text/javascript" src="/js/ext/import/ext-init.js"></script>
			
			<!--扩展框架-->
			<script type="text/javascript" src="/js/ext/uft.Utils.js"></script>
			<script type="text/javascript" src="/js/ext/KeyMap.js"></script>
			<script type="text/javascript" src="/js/ext/Internal.js"></script>
			<script type="text/javascript" src="/js/ext/ux/IFrameComponent.js"></script>
			<script type="text/javascript" src="/js/ext/ux/TabCloseMenu.js"></script>
			<script type="text/javascript" src="/js/ext/ux/Ext.ux.util.js"></script>
			<script type="text/javascript" src="/js/ext/ux/Ext.ux.grid.Search.js"></script>
			<script type="text/javascript" src="/js/ext/ux/GridSummary.js"></script>
			<script type="text/javascript" src="/js/ext/ux/FileUploadField.js"></script>
			<script type="text/javascript" src="/js/ext/ux/TableFormLayout.js"></script>
			<script type="text/javascript" src="/js/ext/ux/TreeCheckNodeUI.js"></script>
			<script type="text/javascript" src="/js/ext/ux/MultiSelect.js"></script>
			<script type="text/javascript" src="/js/ext/ux/ItemSelector.js"></script>
			<script type="text/javascript" src="/js/ext/ux/ToolbarKeyMap.js"></script>
			<script type="text/javascript" src="/js/ext/ux/ColumnHeaderGroup.js"></script>
			<script type="text/javascript" src="/js/ext/ux/InlineToolbarTabPanel.js"></script>
			<script type="text/javascript" src="/js/ext/ux/GridValidator.js"></script>
			<script type="text/javascript" src="/js/ext/ux/Spinner.js"></script>
			<script type="text/javascript" src="/js/ext/ux/SpinnerField.js"></script>
			<script type="text/javascript" src="/js/ext/ux/DateTimeField.js"></script>
			<script type="text/javascript" src="/js/ext/ux/pPageSize.js"></script>
			<script type="text/javascript" src="/js/ext/ux/AutoSizeColumns.js"></script>
			<script type="text/javascript" src="/js/ext/ux/htmleditor/MidasCommand.js"></script>
			<script type="text/javascript" src="/js/ext/ux/htmleditor/Image.js"></script>
			<script type="text/javascript" src="/js/ext/ux/htmleditor/Link.js"></script>
			<script type="text/javascript" src="/js/ext/ux/htmleditor/Table.js"></script>
			<script type="text/javascript" src="/js/ext/ux/BufferView.js"></script>
			<script type="text/javascript" src="/js/ext/ux/LockingHeaderGroupView.js"></script>
			<script type="text/javascript" src="/js/ext/ux/MonthPickerPlugin.js"></script>
			
			<script type="text/javascript" src="/js/ext/extend/Button.js"></script>
			<script type="text/javascript" src="/js/ext/extend/PagingToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/extend/data/Store.js"></script>
			<script type="text/javascript" src="/js/ext/extend/tip/Tip.js"></script>
			<script type="text/javascript" src="/js/ext/extend/tree/Tree.js"></script>
			<script type="text/javascript" src="/js/ext/extend/tree/CheckboxTree.js"></script>
			<script type="text/javascript" src="/js/ext/extend/tree/ContextMenu.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/VType.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/FormPanel.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/Hidden.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/NullField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/Combox.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/HeaderRefField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/BodyRefField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/RefWindow.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/Checkbox.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/DateField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/NumberField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/BigTextField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/BigTextWindow.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/FormulaField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/FormulaWindow.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/MultiSelectField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/MultiSelectWindow.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/MonthPicker.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/FilterField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/ImageField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/IdentityField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/PercentField.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/HtmlEditor.js"></script>
			<script type="text/javascript" src="/js/ext/extend/form/ColorPicker.js"></script>
			
			<script type="text/javascript" src="/js/ext/extend/grid/RowLocation.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/BasicGrid.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/RefGrid.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/EditorGrid.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/PropertyGrid.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/ReportGrid.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/ComboColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/CheckColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/RefColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/DateColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/DateTimeColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/MonthColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/MultiSelectColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/PercentColumn.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/CellSelectionModel.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/dd/GridDD.js"></script>
			<script type="text/javascript" src="/js/ext/extend/grid/dd/GridDragDropRowOrder.js"></script>
			
			<script type="text/javascript" src="/js/ext/extend/UploadWindow.js"></script>
			
			<script type="text/javascript" src="/js/ext/UIToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/CustomBtnWindow.js"></script>
			<script type="text/javascript" src="/js/ext/jf/Constants.js"></script>
			<script type="text/javascript" src="/js/ext/jf/ReportToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/ToftToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/BillToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/StatusMgr.js"></script>
			<script type="text/javascript" src="/js/ext/jf/CacheMgr.js"></script>
			<script type="text/javascript" src="/js/ext/jf/BodyAssistToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/BodyToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/TreeFormToolbar.js"></script>
			<script type="text/javascript" src="/js/ext/jf/Context.js"></script>
			<script type="text/javascript" src="/js/ext/jf/UIPanel.js"></script>
			<script type="text/javascript" src="/js/ext/jf/ToftPanel.js"></script>
			<script type="text/javascript" src="/js/ext/jf/BillPanel.js"></script>
			<script type="text/javascript" src="/js/ext/jf/ReportPanel.js"></script>
			<script type="text/javascript" src="/js/ext/jf/QueryWindow.js"></script>
			<script type="text/javascript" src="/js/ext/jf/QueryFormPanel.js"></script>
			<script type="text/javascript" src="/js/ext/jf/FileUpload.js"></script>
			<script type="text/javascript" src="/js/ext/jf/FileManager.js"></script>
			<script type="text/javascript" src="/js/ext/jf/WorkflowApprove.js"></script>
			<script type="text/javascript" src="/js/ext/jf/WorkflowNote.js"></script>
			<script type="text/javascript" src="/js/ext/jf/WorkflowViewer.js"></script>
	    </uft:compress>
		<uft:compress cssTo="/theme/default/css/base-min.css">
			<link href="/theme/default/css/style.css" rel="stylesheet" type="text/css"></link>
			<link href="/theme/default/css/ext-all.css" rel="stylesheet" type="text/css"></link>
			<link href="/theme/default/css/xtheme.css" rel="stylesheet" type="text/css"></link>
	    </uft:compress>
	</head>
	<body>
	</body>
</html>
<%
out.println("压缩完成！");
%>

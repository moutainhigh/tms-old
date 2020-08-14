<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_edit);
				btns.push(this.btn_copy);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				return btns;
			},
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.toolbar.on('edit',setFieldStatus);
		//参数类型改变时
		//当起始地址类型改变时
		function afterEditStart_addr_type(record){
			var grid = Ext.getCmp('ts_assign_rule_b'); //合同子表
			uft.Utils.setColumnValue(record,'start_addr',null); //重置起始区域
			var start_addr_type = uft.Utils.getColumnValue(record,'start_addr_type');
			var start_addr_editor = uft.Utils.getColumnEditor(grid,'start_addr');
			start_addr_editor.addExtendParams({addr_type:start_addr_type});
		}
		//当目的地地址类型改变时
		function afterEditEnd_addr_type(record){
			var grid = Ext.getCmp('ts_assign_rule_b'); //合同子表
			uft.Utils.setColumnValue(record,'end_addr',null); //重置起始区域
			var end_addr_type = uft.Utils.getColumnValue(record,'end_addr_type');
			var end_addr_editor = uft.Utils.getColumnEditor(grid,'end_addr');
			end_addr_editor.addExtendParams({addr_type:end_addr_type});
		}
		
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

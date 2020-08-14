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
		function afterEditMatter_type1(record){
			var grid = Ext.getCmp('ts_merge_rule_b'); //规则子表
			uft.Utils.setColumnValue(record,'matter1',null); //重置内容
			var matter_type1 = uft.Utils.getColumnValue(record,'matter_type1');
			var matter_type1_editor = uft.Utils.getColumnEditor(grid,'matter1');
			matter_type1_editor.addExtendParams({matter_type:matter_type1});
		}
		function afterEditMatter_type2(record){
			var grid = Ext.getCmp('ts_merge_rule_b'); //规则子表
			uft.Utils.setColumnValue(record,'matter2',null); //重置内容
			var matter_type2 = uft.Utils.getColumnValue(record,'matter_type2');
			var matter_type2_editor = uft.Utils.getColumnEditor(grid,'matter2');
			matter_type2_editor.addExtendParams({matter_type:matter_type2});
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

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
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				return btns;
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_del_handler : function(){
				if(grid.id == 'ts_etc_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					 if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 uft.Utils.showWarnMsg('不能删除系统创建的明细！');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_cop_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_cop_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_pas_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				afterEditBodyAssistToolbar();
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		var grid = Ext.getCmp('ts_etc_b');
		if(grid){
			grid.on('afteredit',function(e){
				this.afterEditBodyAssistToolbar();
			},this);
		}
		
		function afterEditBodyAssistToolbar(){
			var grid = app.getActiveBodyGrid();
			if(grid.id=="ts_etc_b"){
				var store = grid.getStore(), count = store.getCount();
				var cost_amount = 0;
				if(count > 0){
					for(var i=0;i<count;i++){
						var record = store.getAt(i);
						var amount = record.get('amount');
						var operation_type = record.get('operation_type');
						if(typeof(amount) != 'undefined' && typeof(operation_type) != 'undefined'){
							if(operation_type == 0){//消费
								cost_amount = cost_amount - amount;
							}else if(operation_type == 1){
								cost_amount = cost_amount + amount;
							}
						}
					}
					uft.Utils.getField('amount').setValue(cost_amount);
				}else{
					uft.Utils.getField('amount').setValue(0);
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

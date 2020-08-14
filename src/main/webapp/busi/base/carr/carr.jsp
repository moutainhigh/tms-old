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

		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
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
				btns.push(this.btn_import);
				btns.push(this.btn_export);
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var carr_code = uft.Utils.getField('carr_code');
				carr_code.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		app.statusMgr.addAfterUpdateCallback(function(){
			var grid = Ext.getCmp('ts_carr_rate');
			setBodyAssistToolbarEnable(grid);
		});
		
		app.getBodyTabPanel().addListener('tabchange',function(){
			var grid = app.getActiveBodyGrid();
			if(grid.id == 'ts_carr_rate'){
				setBodyAssistToolbarEnable(grid);
			}
			
		},this);
		
		function setBodyAssistToolbarEnable(grid){
			var bat = app.bodyAssistToolbar;
			var pageStatus = app.statusMgr.getCurrentPageStatus();
			if(pageStatus == uft.jf.pageStatus.OP_EDIT || pageStatus == uft.jf.pageStatus.OP_ADD){
				bat.setOperatorEnabled(grid,{'add':true,'cop':false,'del':true});
			}else{
				bat.setOperatorEnabled(grid,{'add':false,'cop':false,'del':false});
			}
		}
		
		
		afterEditBody = function(e){
			var grid = e.grid,ds = grid.getStore(),count = ds.getCount(),record = e.record;
			if(e.grid.id == 'ts_carr_rate'){//体积重换算比
				if(e.field == 'trans_type_name'){
					var pk_trans_type = record.get('pk_trans_type');
					var start_area = record.get('start_area');
					var end_area = record.get('end_area');
					var key1 = pk_trans_type + start_area + end_area
					for(var i=0;i<count;i++){
						var r = ds.getAt(i);
						if(r.id != record.id){
							var pk = r.get('pk_trans_type');
							var start = r.get('start_area');
							var end = r.get('end_area');
							var key2 = pk + start + end
							if(key2 == key1){
								uft.Utils.showWarnMsg('您选择的换算比已经存在，不能重复录入！');
								record.set('pk_trans_type',null);
								record.set('trans_type_name',null);
								record.set('start_area',null);
								record.set('start_area_name',null);
								record.set('end_area',null);
								record.set('end_area_name',null);
								return false;
								return false;
							}
						}
					}
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

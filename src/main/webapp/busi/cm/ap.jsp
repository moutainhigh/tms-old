<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/jquery/ajaxfileupload.js"/>"></script>
		<style type="text/css">
			.btnLock {
				background-image: url(../../busi/cm/images/lock_close.png) !important;
			}
			
			.btnOpen {
				background-image: url(../../busi/cm/images/lock_open.png) !important;
			}
		
		</style>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" 
		bodyGridsPagination="false,false" headerGridPageSize="20" bodyGridsCheckboxSelectionModel="true"
		bodyGridsDataUrl="loadData.json,loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
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
				btns.push(new uft.extend.Button({
					text : '开启',
					iconCls : 'btnOpen',
					scope : this,
					handler : function(){
						var grid = Ext.getCmp('ts_account_period_b');
						var pk = Ext.getCmp('pk_account_period').getValue();
						var ids = uft.Utils.getSelectedRecordIds(grid,"pk_account_period_b");
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}else if(ids.length > 1){
							uft.extend.tip.Tip.msg('warn','只能选择一条记录！');
							return;
						}
						var params = this.app.newAjaxParams();
						params["pk"] = pk;
						params["id"] = ids[0];
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : params,
					    	url : 'periodCommit.json',
					    	success : function(values){
					    		if(values && values.data){
					    			this.app.setAppValues(values.data);
					    		}
 					    	}
					    });	
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				btns.push(new uft.extend.Button({
					text : '关闭',
					iconCls : 'btnLock',
					scope : this,
					handler : function(){
						var grid = Ext.getCmp('ts_account_period_b');
						var pk = Ext.getCmp('pk_account_period').getValue();
						var ids = uft.Utils.getSelectedRecordIds(grid,"pk_account_period_b");
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}else if(ids.length > 1){
							uft.extend.tip.Tip.msg('warn','只能选择一条记录！');
							return;
						}
						var params = this.app.newAjaxParams();
						params["pk"] = pk;
						params["id"] = ids[0];
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : params,
					    	url : 'periodUncommit.json',
					    	success : function(values){
					    		if(values && values.data){
					    			this.app.setAppValues(values.data);
					    		}
					    	}
					    });
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var carno = uft.Utils.getField('carno');
				carno.setReadOnly(true);
			}			
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		app.bodyAssistToolbar.setDisabled(true);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

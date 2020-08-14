<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridSingleSelect="true" headerGridCheckboxSelectionModel="false" headerGridImmediatelyLoad="true" />
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
// 				btns.push(this.btn_copy);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
 				btns.push(new uft.extend.Button({
 					text : '执行任务',
 					scope : this,
 					handler : function(){
 						var id = uft.Utils.getSelectedRecordId(this.app.headerGrid,this.app.getHeaderPkField());
 						if(!id){
 							uft.Utils.showWarnMsg('请先选中一条记录！');
 							return;
 						}
 						uft.Utils.doAjax({
 					    	scope : this,
 					    	params : {pk_job_def:id},
 					    	url : 'testTask.json',
 					    	success : function(values){
 					    	}
 					    });
 					}
 				}));
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		afterEditHead = function(field,value,oriValue){
			if(field.id == 'exec_type'){
				//选择客户，那么客户是可以
				setExecTimeOrInterval(value);
			}
		}
		
		function setExecTimeOrInterval(exec_type){
			if(!exec_type){
				return;
			}
			var interval = Ext.getCmp('interval');
			var exec_time = Ext.getCmp('exec_time');
			var ps = app.statusMgr.getCurrentPageStatus();
			if(!interval || !exec_time){
				return;
			}
			
			if(ps == uft.jf.pageStatus.OP_ADD || ps == uft.jf.pageStatus.OP_EDIT){
				if(String(exec_type) == '1'){//间隔时间
					interval.setReadOnly(false);
					interval.allowBlank = false;
					exec_time.setReadOnly(true);
					exec_time.allowBlank = true;
					exec_time.setValue(null);
				}else if(String(exec_type) == '2'){//每天执行
					interval.setReadOnly(true);
					interval.allowBlank = true;
					exec_time.setReadOnly(false);
					exec_time.allowBlank = false;
					interval.setValue(null);
				}
			}
		}
		
		app.statusMgr.addAfterUpdateCallback(function(){
			var exec_type = Ext.getCmp('exec_type');
			if(exec_type){
				var ut = exec_type.getValue();
				setExecTimeOrInterval(ut);
			}
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

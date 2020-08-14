<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/sys/UserPassword.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/UserRole.js"/>"></script>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false" headerGridImmediatelyLoad="true" />
	</body>
	<script type="text/javascript" defer>
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_copy);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(new uft.extend.Button({
					text : '修改密码', 
					handler : this.btn_edit_pass_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '分配角色',
					handler : this.btn_dist_role_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(this.btn_export);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				return btns;
			},
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				if('0001' == '<%=loginInfo.getPk_user()%>'){
					//如果是超级管理员
					uft.Utils.getField('pk_corp').setReadOnly(false);
				}
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var user_code = uft.Utils.getField("user_code");
				user_code.setReadOnly(true);
				if('0001' == '<%=loginInfo.getPk_user()%>'){
					//如果是超级管理员
					uft.Utils.getField('pk_corp').setReadOnly(false);
				}
			},	
			btn_copy_handler : function(){
				MyToolbar.superclass.btn_copy_handler.call(this);
				if('0001' == '<%=loginInfo.getPk_user()%>'){
					//如果是超级管理员
					uft.Utils.getField('pk_corp').setReadOnly(false);
				}
			},			
			//修改密码
			btn_edit_pass_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
				if(ids.length != 1){
					uft.Utils.showErrorMsg('有且只能选择一行记录!');
					return false;
				}
				var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
				var user_code = record.get('user_code');
				new uft.user.UserPassword({
					title : '修改['+user_code+']的密码',
					pk_user : ids[0]
				}).show();
			},
			//分配角色
			btn_dist_role_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
				if(ids.length != 1){
					uft.Utils.showErrorMsg('有且只能选择一行记录!');
					return false;
				}
				var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
				var user_code = record.get('user_code');
				new uft.user.UserRole({title:'为['+user_code+']分配角色',pk_user:ids[0]}).show();
			}
		});
		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		//公司的change事件
		function afterChangePk_corp(value,originalValue){
			var pk_dept = uft.Utils.getField('pk_dept');
			pk_dept.addExtendParams({pk_corp:value});
		}
		
		afterEditHead = function(field,value,oriValue){
			if(field.id == 'user_type'){
				//选择客户，那么客户是可以
				setCustomerOrCarrider(value);
			}
		}
		
		function setCustomerOrCarrider(user_type){
			if(!user_type){
				return;
			}
			var pk_customer = Ext.getCmp('pk_customer');
			var pk_carrier = Ext.getCmp('pk_carrier');
			var ps = app.statusMgr.getCurrentPageStatus();
			if(!pk_customer || !pk_carrier){
				return;
			}
			
			if(ps == uft.jf.pageStatus.OP_ADD || ps == uft.jf.pageStatus.OP_EDIT){
				if(String(user_type) == '2'){//客户
					pk_customer.setReadOnly(false);
					pk_carrier.setReadOnly(true);
				}else if(String(user_type) == '3'){//承运商
					pk_customer.setReadOnly(true);
					pk_carrier.setReadOnly(false);
				}else{
					pk_customer.setReadOnly(true);
					pk_carrier.setReadOnly(true);
				}
			}
		}
		
		app.statusMgr.addAfterUpdateCallback(function(){
			var user_type = Ext.getCmp('user_type');
			if(user_type){
				var ut = user_type.getValue();
				setCustomerOrCarrider(ut);
			}
		});
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/sys/PowerFun.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/RolePlan.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/WorkBenchPower.js"/>"></script>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridSingleSelect="true" headerGridImmediatelyLoad="true"/>
	</body>
	<script type="text/javascript">
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
					text : '权限分配',
					handler : this.btn_auth_handler,
			        enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '分配门户方案',
					handler : this.btn_dist_plan_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '分配工作台方案',
					handler : this.btn_workBench_plan_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var role_code = uft.Utils.getField("role_code");
				role_code.setReadOnly(true);
			},	
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				if('0001' == '<%=loginInfo.getPk_user()%>'){
					//如果是超级管理员
					uft.Utils.getField('pk_corp').setReadOnly(false);
				}
			},
			btn_auth_handler : function(){
				var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
				if(record){
					var pk_role = record.data[this.app.headerPkField];
					var role_name= record.data['role_name'];
					new uft.role.PowerFun({title:'为['+role_name+']分配权限',pk_role:pk_role}).show();
				}else{
					uft.Utils.showWarnMsg('请选择一行记录！');
					return;
				}
			},
			//分配门户方案
			btn_dist_plan_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
				if(ids.length != 1){
					uft.Utils.showErrorMsg('有且只能选择一行记录!');
					return false;
				}
				var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
				var role_name = record.get('role_name');
				new uft.role.RolePlan({title:'为['+role_name+']分配门户方案',pk_role:ids[0]}).show();
			},
			//分配工作台方案
			btn_workBench_plan_handler : function(){
				var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
				if(record){
					var pk_role = record.data[this.app.headerPkField];
					var role_name= record.data['role_name'];
					new uft.role.WorkBenchPower({title:'为['+role_name+']分配工作台方案',pk_role:pk_role}).show();
				}else{
					uft.Utils.showWarnMsg('请选择一行记录！');
					return;
				}
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
			function vbillstatusBeforeRenderer(value,meta,record){
				meta.css = 'css'+value;
			}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css142{
				background-color: #FAC090;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" useFieldSetInHeader="true" headerGridPageSize="20" headerGridImmediatelyLoad="true"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
	var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_confirm.enabledBizStatus = [uft.jf.bizStatus.NEW];
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.VIO_CONFIRM];
			},
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
				btns.push(this.btn_confirm);
				btns.push(this.btn_unconfirm);
				btns.push(this.btn_export);
				btns.push(this.btn_print);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

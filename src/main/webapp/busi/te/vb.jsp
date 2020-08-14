<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/te/PackRecord.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/Authentication.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/VBLotPay.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/VehiclePay.js?v=${version}" />'></script>
		<script type="text/javascript">
			function entrust_vbillnoRenderer(value,meta,record){
				var url = ctxPath+"/te/ent/index.html?funCode=t501&nodeKey=view&_waterfallScene=true&_vbillno="+value;
				return "<a href=\"javascript:uft.Utils.openNode('"+value+"','委托单','"+url+"')\">"+value+"</a>";
			}
			function vbillstatusBeforeRenderer(value,meta,record){
				meta.css = 'css'+value;
			}
		</script>
		<style type="text/css">
			.css1 {
				background-color: #92D050;
			}
			.css2 {
				background-color: #FAC090;
			}
			.css3 {
				background-color: #FFFF00;
			}
			.css4 {
				background-color: #538ED5;
			}
			.css0{
				background-color: #92D050;
			}
			.css21{
				background-color: #FAC090;
			}
			.css22{
				background-color: #FFFF00;
			}
			.css23{
				background-color: #538ED5;
			}
			.css24{
				background-color: #696969;
			}
			.css25{
				background-color: #538ED5;
			}
			.css26{
				background-color: #FFFF00;
			}
			.css27{
				background-color: #888888;
			}
			.btnNodeArri{
				background: url(../../busi/te/images/nodeArri.png) no-repeat left 0px !important;
			}
			.btnCertificate{
				background: url(../../busi/te/images/certificate.png) no-repeat left 0px !important;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" useFieldSetInHeader="true" headerGridPageSize="20" headerGridImmediatelyLoad="false"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>	
	<object id="CertCtl" type="application/cert-reader" width="0" height="0"></object>			
	</body>
	<script type="text/javascript">
	var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
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
				btns.push(this.btn_confirm);
				btns.push(this.btn_unconfirm);
				btns.push(this.btn_export);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				btns.push(new uft.extend.Button({
					text : '&nbsp;&nbsp;&nbsp;身份验证', 
					handler : this.btn_authentication_handler,
					iconCls : 'btnCertificate',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push({
					text : '批次费用', 
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}else if(records.length > 1){
							uft.Utils.showWarnMsg('只能选择一条记录！');
							return;
						}
						var win = new uft.te.VBLotPay({record:records[0]}).show();
					}
				});
				btns.push({
					text : '车队费用', 
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}else if(records.length > 1){
							uft.Utils.showWarnMsg('只能选择一条记录！');
							return;
						}
						var win = new uft.te.VehiclePay({record:records[0]}).show();
					}
				});
				return btns;
			},
			btn_authentication_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var lot = record.get('lot');
					
					var CertCtl = document.getElementById("CertCtl");
					CertCtl.connect();
					var result = CertCtl.readCert();
					CertCtl.disconnect();
					result = eval('('+result+')');  
					if(result.resultFlag == -1){
						uft.Utils.showWarnMsg("没有读取到身份证信息！");
						return;
					}
					var retMap = Utils.doSyncRequest('authentication.json',{'lot':lot,'card_msg':JSON.stringify(result.resultContent)});
					new uft.te.Authentication({app:this.app,record:record,retMap:retMap}).show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='expandPackRecord()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(Ext.getCmp('ts_entrust'),'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;	
		}
		
		//展开委托单包装修改记录
		function expandPackRecord(){
			var record = Ext.getCmp('ts_entrust').getSelectedRow();
			var pk_entrust = record.get('pk_entrust')
			if(record){
				new uft.te.PackRecord({pk_entrust:pk_entrust,grid:Ext.getCmp('ts_entrust')}).show();
			}
		}
		
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

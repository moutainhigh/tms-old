<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==106 || value == 108 || value == 110){
				meta.style+='color:#fff;';
			}
		}
		function req_ship_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus < 1){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function act_ship_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 108){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css102{
				background-color: #FAC090;
			}
			.css104{
				background-color: #EBD6D6;
			}
			.css106{
				background-color: #7030A0;
			}
			.css108{
				background-color: #44964C;
			}
			.css110{
				background-color: red;
			}
			.cssRed{
				background-color: #FF0000;
			}
			.btnCanSeg {
				background-image: url(../../busi/tp/images/cxfd.png) !important;
			}
			.btnWei {
				background-image: url(../../busi/tp/images/fl.png) !important;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" headerGridPageSizePlugin="true"
		bodyGridsDataUrl="loadData.json" bodyGridsCheckboxSelectionModel="true,true" bodyGridsSingleSelect="false,false" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(new uft.extend.Button({
					text : '发货',
					iconCls : 'btnYes',
					scope : this,
					handler : function(){
						var grid = this.app.getActiveBodyGrid();
						var records = uft.Utils.getSelectedRecords(grid);
						if(records){
							var ids = uft.Utils.getSelectedRecordIds(grid,grid.pkFieldName);
							var params=this.app.newAjaxParams();
							params[grid.pkFieldName]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : true,
						    	method : 'GET',
						    	url : 'ship.json',
						    	success : function(values){
						    		this.btn_ref_handler();//刷新整张单据即可
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请选择出库单明细表中的记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus:[uft.jf.bizStatus.OUTSTO_PART_PICK,uft.jf.bizStatus.OUTSTO_PICKED,uft.jf.bizStatus.OUTSTO_PART_SHIP]
				}));
// 				btns.push(new uft.extend.Button({
// 					text : '取消发货',
// 					iconCls : 'btnCancel',
// 					scope : this,
// 					handler : function(){
// 						var grid = this.app.getActiveBodyGrid();
// 						if(grid.id != 'ts_outstorage_b'){
// 							uft.Utils.showWarnMsg('请选择出库单明细表中的记录！')
// 							return;
// 						}
// 						var records = uft.Utils.getSelectedRecords(grid);
// 						if(records){
// 							var ids = uft.Utils.getSelectedRecordIds(grid,grid.pkFieldName);
// 							var params=this.app.newAjaxParams();
// 							params[this.app.getBillIdField()]=ids;
// 						    uft.Utils.doAjax({
// 						    	scope : this,
// 						    	params : params,
// 						    	isTip : true,
// 						    	method : 'GET',
// 						    	url : 'ship.json',
// 						    	success : function(values){
// 						    		this.btn_ref_handler();//刷新整张单据即可
// 						    		if(values.append){
// 						    			uft.Utils.showWarnMsg(values.append);
// 						    		}
// 						    	}
// 						    });
// 						}else{
// 							uft.Utils.showWarnMsg('请选择出库单明细表中的记录！');
// 							return;
// 						}
// 					},
// 					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD],
// 					enabledBizStatus:[uft.jf.bizStatus.OUTSTO_PART_SHIP,uft.jf.bizStatus.OUTSTO_SHIPED]
// 				}));
				btns.push(new uft.extend.Button({
					text : '自动分配',
					iconCls : 'btnWei',
					scope : this,
					handler : function(){
						var grid = this.app.getActiveBodyGrid();
						if(grid.id != 'ts_outstorage_b'){
							uft.Utils.showWarnMsg('请选择出库单明细表中的记录！')
							return;
						}
						var records = uft.Utils.getSelectedRecords(grid);
						if(records){
							var ids = uft.Utils.getSelectedRecordIds(grid,grid.pkFieldName);
							var params=this.app.newAjaxParams();
							params[grid.pkFieldName]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : true,
						    	method : 'GET',
						    	url : ctxPath +'/wh/pick/autopick.json',
						    	success : function(values){
						    		this.btn_ref_handler();//刷新整张单据即可
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请先选择要自动分配的出库单明细记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus:[uft.jf.bizStatus.OUTSTO_PART_PICK,uft.jf.bizStatus.NEW]
				}));					
				btns.push(new uft.extend.Button({
					text : '取消分配',
					iconCls : 'btnCanSeg',
					scope : this,
					handler : function(){
						var grid = this.app.getActiveBodyGrid();
						var records = uft.Utils.getSelectedRecords(grid);
						if(records){
							var ids = uft.Utils.getSelectedRecordIds(grid,grid.pkFieldName);
							var params=this.app.newAjaxParams();
							params[grid.pkFieldName]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : true,
						    	method : 'GET',
						    	url : ctxPath +'/wh/pick/unpick.json',
						    	success : function(values){
						    		this.btn_ref_handler();//刷新整张单据即可
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请先选择要取消分配的明细记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus:[uft.jf.bizStatus.OUTSTO_PART_SHIP,uft.jf.bizStatus.OUTSTO_PART_PICK,uft.jf.bizStatus.OUTSTO_PICKED]
				}));				
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_export);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		var ts_outstorage_b = Ext.getCmp('ts_outstorage_b');
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/edit.png' border=0 onclick='doPick()' style='cursor:pointer' title='手动分配'>";
		};
		var processorColumn = uft.Utils.getColumn(ts_outstorage_b,'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;	
		}
		//打开分配
		function doPick(){
			var record = uft.Utils.getSelectedRecord(ts_outstorage_b);
			if(record){
				var pk_outstorage_b = record.get(ts_outstorage_b.pkFieldName);
				window.openDialog(ctxPath + '/wh/pick/index.html?funCode=t1210&pk_outstorage_b='+pk_outstorage_b,null);
				app.headerGrid.getStore().reload();
				app.toolbar.btn_ref_handler();
			}
		}
		//打开对话框
		function openDialog(url,params){
			var wparams = "dialogWidth:900px"
				+";dialogHeight:520px"
				+";dialogLeft:"+(window.screen.availWidth-900)/2+"px"
				+";dialogTop:"+(window.screen.availHeight-520)/2+"px"
				+";status:no;scroll:no;resizable:no;help:no;center:yes";
			if(Ext.isChrome){//chrome 从37版本开始不支持showModalDialog方法
				window.open(url,params,wparams);
			}else{
				window.showModalDialog(url,params,wparams);
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

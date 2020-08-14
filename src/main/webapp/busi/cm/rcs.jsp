<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/cm/CheckSheetReceivable.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/ReceCheckSheetRecord.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/ReceCheckSheetInvoice.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/uploadRCSAttachment.js?v=${version}" />'></script>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==53 || value == 54){
				meta.style+='color:#fff;';
			}
		}
		//对于客户索赔类型，金额为负数，使用红色字体
		function amountBeforeRenderer(value,meta,record){
			if(value < 0){//负数
				meta.style+='color:red;';
			}
		}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css51{
				background-color: #FAC090;
			}
			.css53{
				background-color: #7030A0;
			}
			.css54{
				background-color: #44964C;
			}
		</style>		
	</head>
	<body>
		<nw:Bill templetVO="${templetVO}" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>						
	</body>
	<script type="text/javascript">
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_confirm.enabledBizStatus = [uft.jf.bizStatus.NEW];
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.RCS_CONFIRM];
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
// 				btns.push(this.btn_add);
// 				btns.push(this.btn_copy);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				//btns.push(this.btn_attach);
				btns.push(this.btn_confirm);
				btns.push(this.btn_unconfirm);
				btns.push({
					xtype : 'button',
					text : '收款',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.CheckSheetReceivable({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.RCS_CONFIRM,uft.jf.bizStatus.RCS_PART_CAVLOAN]
				});
				btns.push({
					xtype : 'button',
					text : '开票',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.ReceCheckSheetInvoice({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.RCS_CONFIRM]
				});
				btns.push({
					xtype : 'button',
					text : '查看收款记录',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.ReceCheckSheetRecord({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.RCS_CAVLOAN,uft.jf.bizStatus.RCS_PART_CAVLOAN]
				});
				btns.push(this.btn_export);
				btns.push({
					xtype : 'button',
					text : '导出对账单明细',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							this.exportBodyGrid('ts_rece_check_sheet_b');
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST]
				});
				btns.push({
					xtype : 'button',
					text : '导出收款记录',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var config = {};
							config.funCode = 't481';
							config.tabCode = 'ts_rece_check_sheet_record';
							config.isBody = 'true';
							var headerGrid = this.app.getHeaderGrid();
							config[this.app.getHeaderPkField()] = uft.Utils.getSelectedRecordId(headerGrid,this.app.getHeaderPkField());
							var url="exportReceCheckSheetRecord.do";
							var index = 0;
							for(var key in config){
								if(index == 0){
									url += "?";
								}else{
									url += "&";
								}
								url += key + "=" + config[key];
								index++;
							}
							window.open(encodeURI(url));								
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.RCS_CAVLOAN,uft.jf.bizStatus.RCS_PART_CAVLOAN]
				});
				btns.push({
					xtype : 'button',
					text : '集货应收明细导出',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
						    var url="exportPickupReceCheckSheetSheet.do";
							var index = 0;
							for(var key in receCheckSheetGrid.selectedRows.receDetails){
								if(index == 0){
									url += "?";
								}else{
									url += "&";
								}
								url +="key=" + key;
								index++;
							}
							window.open(encodeURI(url));								
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]//,
					//enabledBizStatus : [uft.jf.bizStatus.NEW]
				});
				btns.push({
					xtype : 'button',
					iconCls : 'btnAttach',
					text : '应收对账附件',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.uploadRCSAttachment({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				});
				btns.push(this.btn_export);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				return btns;
			},
			oneRecordCheck : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！')
					return false;
				}
				if(records.length != 1){
					uft.Utils.showWarnMsg('只能选择一条记录！')
					return false;
				}
				return true;
			},
			btn_edit_handler : function(){
				//只有新建状态的对账单才可以修改
				var vbillstatus = uft.Utils.getNumberFieldValue('vbillstatus');
				if(vbillstatus != 0){
					uft.Utils.showWarnMsg('只有[新建]状态的对账单才可以修改！')
					return;
				}
				MyToolbar.superclass.btn_edit_handler.call(this);
			},
			btn_del_handler : function(){
				//只有新建状态的对账单才可以删除
				var vbillstatus = uft.Utils.getNumberFieldValue('vbillstatus');
				if(vbillstatus != 0){
					uft.Utils.showWarnMsg('只有[新建]状态的对账单才可以删除！')
					return;
				}
				MyToolbar.superclass.btn_del_handler.call(this);
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_del_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				updateHeaderCostAmount();
			}
		});
		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		var sm = app.statusMgr,grid = Ext.getCmp('ts_rece_check_sheet_b'),bat = app.bodyAssistToolbar;
		sm.addAfterUpdateCallback(function(){
			bat.setOperatorEnabled(grid,{'add':false,'cop':false});
		},this);
		
		
		/**
		 * 增加应收对账收跨页面选择功能   2015-11-09 XIA
		 */
		//表格加载完成后，选中之前选择过的 
		//应收对账增加跨页选择 
			var receCheckSheetGrid = Ext.getCmp('ts_rece_check_sheet');		
			receCheckSheetGrid.store.on("load",function(store,records,param){
				var recordsToSelect = [];
				for(var i=0;i<records.length;i++){
					if(receCheckSheetGrid.selectedRows.receDetails[records[i].data.pk_rece_check_sheet]!=null){
						recordsToSelect.push(records[i]);
					}
				}
				receCheckSheetGrid.getSelectionModel().selectRecords(recordsToSelect);
			});
			
			var selectedRows = {};
			receCheckSheetGrid.selectedRows = selectedRows;
			selectedRows.receDetails={};
			var receDetail={};
			var pk_rece_check_sheet;
			
			//增加headerGrid的行选择事件
			receCheckSheetGrid.getSelectionModel().addListener({
				'rowselect' : onRowselect,scope : this
			});	
			//增加headerGrid的行选择事件
			receCheckSheetGrid.getSelectionModel().addListener({
				'rowdeselect' : onRowDeSelect,scope : this
			});
			//行选择事件
			function onRowselect(sm, rowIndex, record) {
				pk_rece_check_sheet = record.get('pk_rece_check_sheet');
				receDetail.vbillno = record.get('vbillno');
				selectedRows.receDetails[pk_rece_check_sheet] = receDetail;
			}
			//取消行选择事件
			function onRowDeSelect(sm, rowIndex, record) {
				pk_rece_check_sheet = record.get('pk_rece_check_sheet');
				if(selectedRows.receDetails[pk_rece_check_sheet]!=null){	
					delete selectedRows.receDetails[pk_rece_check_sheet];			
				}			
			}

		
		function updateHeaderCostAmount(){
			debugger;
			var resultMap = uft.Utils.getGridSumValueMap('ts_rece_check_sheet_b','cost_amount');//费用明细的金额
			var got_amount  = uft.Utils.getNumberFieldValue('got_amount');
			var cost_amount = resultMap['cost_amount'];
			var ungot_amount = cost_amount-got_amount;
			uft.Utils.getField('cost_amount').setValue(cost_amount);
			uft.Utils.getField('ungot_amount').setValue(ungot_amount);
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

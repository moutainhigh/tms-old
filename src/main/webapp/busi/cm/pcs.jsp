<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/cm/CheckSheetPayable.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/PayCheckSheetRecord.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/PayCheckSheetInvoice.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/uploadPCSAttachment.js?v=${version}" />'></script>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==63 || value == 64){
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
			.css61{
				background-color: #FAC090;
			}
			.css63{
				background-color: #7030A0;
			}
			.css64{
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
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.PCS_CONFIRM];
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
					text : '付款',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.CheckSheetPayable({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.PCS_CONFIRM,uft.jf.bizStatus.PCS_PART_CAVLOAN]
				});
				btns.push({
					xtype : 'button',
					text : '开票',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.PayCheckSheetInvoice({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.PCS_CONFIRM]
				});
				btns.push({
					xtype : 'button',
					text : '查看付款记录',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.PayCheckSheetRecord({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.PCS_CAVLOAN,uft.jf.bizStatus.PCS_PART_CAVLOAN]
				});
 				btns.push(this.btn_export);
				btns.push({
					xtype : 'button',
					text : '导出对账单明细',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							this.exportBodyGrid('ts_pay_check_sheet_b');
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST]
				});
				btns.push({
					xtype : 'button',
					text : '导出付款记录',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var config = {};
							config.funCode = 't491';
							config.tabCode = 'ts_pay_check_sheet_record';
							config.isBody = 'true';
							var headerGrid = this.app.getHeaderGrid();
							config[this.app.getHeaderPkField()] = uft.Utils.getSelectedRecordId(headerGrid,this.app.getHeaderPkField());
							var url="exportPayCheckSheetRecord.do";
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
					enabledBizStatus : [uft.jf.bizStatus.PCS_CAVLOAN,uft.jf.bizStatus.PCS_PART_CAVLOAN]
				});
				btns.push({
					xtype : 'button',
					iconCls : 'btnAttach',
					text : '应付对账附件',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.uploadPCSAttachment({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				});
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
		
		var sm = app.statusMgr,grid = Ext.getCmp('ts_pay_check_sheet_b'),bat = app.bodyAssistToolbar;
		sm.addAfterUpdateCallback(function(){
			bat.setOperatorEnabled(grid,{'add':false,'cop':false});
		},this);
		
		function updateHeaderCostAmount(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_pay_check_sheet_b','cost_amount');//费用明细的金额
			var got_amount  = uft.Utils.getNumberFieldValue('got_amount');
			var cost_amount = resultMap['cost_amount'];
			var ungot_amount = cost_amount-got_amount;
			uft.Utils.getField('cost_amount').setValue(cost_amount);
			uft.Utils.getField('ungot_amount').setValue(ungot_amount);
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

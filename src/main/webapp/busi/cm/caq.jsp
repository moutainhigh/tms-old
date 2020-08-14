<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
		<nw:Bill templetVO="${templetVO}" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>	
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
					text : '导入报价明细',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！')
							return;
						}
						if(records.length != 1){
							uft.Utils.showWarnMsg('只能选择一条记录！')
							return;
						}
						var params = this.app.newAjaxParams();
						var billId = records[0].get(this.app.headerPkField);
						params['billId'] = billId;
						var win = Ext.getCmp('importWin');
						if(!win){
							win = new uft.extend.UploadWindow({
								id:'importWin',
								isLog : true,
								params : params,
								fun_code : this.app.context.getFunCode(),
								permitted_extensions : ['xls','xlsx']
							});
						}
						win.show();
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '导出报价明细',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！')
							return;
						}
						if(records.length != 1){
							uft.Utils.showWarnMsg('只能选择一条记录！')
							return;
						}
						this.exportBodyGrid();
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(this.btn_attach);
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var code = uft.Utils.getField('code');
				code.setReadOnly(true);
			},
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this,{useTransitionStatus:false});
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		${moduleName}.appUiConfig.toolbar.on('edit',setFieldStatus);
/* 		function setFieldStatus(){
			var contractType = uft.Utils.getField('contract_type').getValue();
			var pk_carrier = uft.Utils.getField('pk_carrier');
			var bala_customer = uft.Utils.getField('bala_customer');
			var notEditCls = 'uft-form-label-not-edit';
			if(contractType === 0 || contractType == '0'){
				//合同类型为客户，承运商不能编辑
				pk_carrier.setReadOnly(true);
				pk_carrier.label.addClass(notEditCls);
				bala_customer.setReadOnly(false);
				bala_customer.label.removeClass(notEditCls);
			}else if(contractType == 1 || contractType == '1'){
				//合同类型为承运商，结算客户不能编辑
				pk_carrier.setReadOnly(false);
				pk_carrier.label.removeClass(notEditCls);
				bala_customer.setReadOnly(true);
				bala_customer.label.addClass(notEditCls);
			}
		} */ 
		//当起始地址类型改变时
		function afterEditStart_addr_type(record){
			var grid = Ext.getCmp('ts_contract_b'); //合同子表
			uft.Utils.setColumnValue(record,'start_addr',null); //重置起始区域
			uft.Utils.setColumnValue(record,'start_addr_name',null); //重置起始区域
			var start_addr_type = uft.Utils.getColumnValue(record,'start_addr_type');
			var start_addr_editor = uft.Utils.getColumnEditor(grid,'start_addr_name');
			start_addr_editor.addExtendParams({addr_type:start_addr_type});
		}
		//当目的地地址类型改变时
		function afterEditEnd_addr_type(record){
			var grid = Ext.getCmp('ts_contract_b'); //合同子表
			uft.Utils.setColumnValue(record,'end_addr',null); //重置起始区域
			uft.Utils.setColumnValue(record,'end_addr_name',null); //重置起始区域
			var end_addr_type = uft.Utils.getColumnValue(record,'end_addr_type');
			var end_addr_editor = uft.Utils.getColumnEditor(grid,'end_addr_name');
			end_addr_editor.addExtendParams({addr_type:end_addr_type});
		}
		//切换到卡片页的时候，如果此时选择了记录，则执行查询单据动作，合同的子表要支持分页，所以需要单独处理
		//2015-3-3为了方便表体使用定位功能，这里不在分页
// 		app.statusMgr.addAfterUpdateCallback(function(){
// 			var record = uft.Utils.getSelectedRecord(app.headerGrid);
// 			if(record){
				//卡片页显示的时候并且不是新增状态
// 				if(app.headerCard.hidden != true && app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_ADD){
// 					var headerPk = record.get(app.getHeaderPkField());
// 					app.loadBodyGrids(headerPk);
// 				}
// 			}
// 		},this);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

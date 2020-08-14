<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/fleet/vmcheck.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/fleet/vmsend.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/fleet/vmdispatch.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/fleet/vmreturn.js?v=${version}" />'></script>
		<script type="text/javascript">
			function vbillstatusBeforeRenderer(value,meta,record){
				meta.css = 'css'+value;
			}
		</script>
				<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css128{
				background-color: #44964C;
			}
			.css130{
				background-color: #FAC090;
			}
			.css132{
				background-color: #FFFF00;
			}
			.css134{
				background-color: #538ED5;
			}
			.css136{
				background-color: #7030A0;
			}
			.css138{
				background-color: #FF3EFF;
			}
			.cssRed{
				background-color: #FF0000;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" useFieldSetInHeader="true" headerGridPageSize="20" headerGridImmediatelyLoad="true"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(){
				MyToolbar.superclass.constructor.call(this);
				this.btn_edit.enabledBizStatus=[uft.jf.bizStatus.NEW,uft.jf.bizStatus.YCGL_REFUSE];
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
				btns.push({
					variable : 'btn_commit',
					text : '提交', 
					iconCls : 'btnYes',
					handler : function(){
						//提交的时候需要检查哪些是必输项，需要做提示
						var bol = this.checkBeforeConfirm();
						if(!bol){//检测不通过
							return;
						}
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
						if(ids.length > 0){
							if(this.fireEvent('beforecommit',this,ids) !== false){
								var params=this.app.newAjaxParams();
								params[this.app.getBillIdField()]=ids;
							    uft.Utils.doAjax({
							    	scope : this,
							    	params : params,
							    	isTip : true,
							    	method : 'GET',
							    	url : 'commit.json',
							    	success : function(values){
							    		this.app.setHeaderValues(records,values.datas);
							    		if(values.datas&&values.datas.length>0){
							    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
							    		}
							    		this.app.statusMgr.updateStatus();
							    		if(values.append){
							    			uft.Utils.showWarnMsg(values.append);
							    		}
							    		this.fireEvent('commit',this,values.datas,values);
							    	}
							    });
							}
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.NEW,uft.jf.bizStatus.YCGL_REFUSE]
				});
				btns.push({
					variable : 'btn_uncommit',
					text : '反提交', 
					iconCls : 'btnCancel',
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
						if(ids.length > 0){
							if(this.fireEvent('beforeuncommit',this,ids) !== false){
								var params=this.app.newAjaxParams();
								params[this.app.getBillIdField()]=ids;
							    uft.Utils.doAjax({
							    	scope : this,
							    	params : params,
							    	isTip : true,
							    	method : 'GET',
							    	url : 'uncommit.json',
							    	success : function(values){
							    		this.app.setHeaderValues(records,values.datas);
							    		if(values.datas&&values.datas.length>0){
							    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
							    		}
							    		this.app.statusMgr.updateStatus();
							    		if(values.append){
							    			uft.Utils.showWarnMsg(values.append);
							    		}
							    		this.fireEvent('uncommit',this,values.datas,values);
							    	}
							    });
							}
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_CONFIRMING]
				});	
				btns.push({
					xtype : 'button',
					text : '审核',
					iconCls : 'btnYes',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！')
							return false;
						}
						new uft.vm.vmcheck({app:this.app,records:records}).show();
						
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_CONFIRMING]
				});
				btns.push({
					variable : 'btn_unconfirm',
					text : '重审', 
					iconCls : 'btnCancel',
					scope : this,
					handler : function(){		
					var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
					var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
					if(ids.length > 0){
						var params=this.app.newAjaxParams();
						params[this.app.getBillIdField()]=ids;
						  uft.Utils.doAjax({
						    scope : this,
						    params : params,
						    isTip : true,
						    method : 'GET',
						    url : 'vmrecheck.json',
						    success : function(values){
						    	app.headerGrid.getStore().reload();
						    }
						 });
					}else{
						uft.Utils.showWarnMsg('请先选择记录！');
						return;
					}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_CONFIRM,uft.jf.bizStatus.YCGL_REFUSE]
				});
				btns.push({
					xtype : 'button',
					text : '派车',
					iconCls : 'btnSetting',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！')
							return false;
						}
						new uft.vm.vmsend({app:this.app,records:records}).show();
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_CONFIRM]
				});
				btns.push({
					xtype : 'button',
					text : '出车',
					iconCls : 'btnSetting',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.vm.vmdispatch({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_SEND]
				});
				btns.push({
					xtype : 'button',
					text : '收车',
					iconCls : 'btnSetting',
					scope : this,
					handler : function(){
						if(this.oneRecordCheck()){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.vm.vmreturn({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.YCGL_DIS]
				});
				
				btns.push(this.btn_export);
				btns.push(this.btn_print);
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
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

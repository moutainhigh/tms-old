<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/te/uploadExpAccdentAttachment.js?v=${version}" />'></script>
		<script type="text/javascript">
			//不同的单据状态使用不同的颜色
			function vbillstatusBeforeRenderer(value,meta,record){
				meta.css = 'css'+value;
				if(value ==73 || value == 74){
					meta.style+='color:#fff;';
				}
			}
			function invoice_vbillnoRenderer(value,meta,record){
				if(value && !Ext.isObject(value)){
					var arr = value.split('|'),str='';
					for(var i=0;i<arr.length;i++){
						var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
						str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
						str+="|";
					}
					return str.substring(0,str.length-1);
				}else{
					return '';
				}
			}
			function entrust_vbillnoRenderer(value,meta,record){
				var url = ctxPath+"/te/ent/index.html?funCode=t501&nodeKey=view&_waterfallScene=true&_vbillno="+value;
				return "<a href=\"javascript:uft.Utils.openNode('"+value+"','委托单','"+url+"')\">"+value+"</a>";
			}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css71{
				background-color: #FFFF00;
			}
			.css72{
				background-color: #FAC090;
			}
			.css73{
				background-color: #7030A0;
			}
			.css74{
				background-color: #44964C;
			}
		</style>		
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
		<nw:Bill templetVO="${templetVO}" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>			
	</body>
	<script type="text/javascript">
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(){
				MyToolbar.superclass.constructor.call(this);
				this.btn_confirm.setText('提交');
				this.btn_unconfirm.setText('反提交');
				this.btn_confirm.enabledBizStatus=[uft.jf.bizStatus.NEW];
				this.btn_unconfirm.enabledBizStatus=[uft.jf.bizStatus.EA_WHANDLE];
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
				btns.push(new uft.extend.Button({
					text : '处理',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var billId = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField)[0];
							var appBufferData = this.app.cacheMgr.getEntity(billId);
							if(appBufferData){
								this.app.setAppValues(appBufferData);
								this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REVISE);
								this.app.statusMgr.updateStatus();
							}else{
								var params=this.app.newAjaxParams();
								params[this.app.getBillIdField()]=billId;
							    uft.Utils.doAjax({
							    	scope : this,
							    	params : params,
							    	isTip : false,
							    	method : 'GET',
							    	url : 'show.json',
							    	success : function(values){
							    		if(values && values.data){
								    		this.app.setAppValues(values.data,{saveToCache:true});
											this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REVISE);
											this.app.statusMgr.updateStatus();
							    		}
							    	}
							    });
							}
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.EA_WHANDLE,uft.jf.bizStatus.EA_HANDLING]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销处理',
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('您确认要撤销处理吗？'), function(btn) {
								if(btn == 'yes'){
									var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
									var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
									var params=this.app.newAjaxParams();
									params[this.app.getBillIdField()]=ids;
								    uft.Utils.doAjax({
								    	scope : this,
								    	params : params,
								    	isTip : false,
								    	method : 'GET',
								    	url : 'revocation.json',
								    	success : function(values){
								    		this.app.setHeaderValues(records,values.datas);
								    		this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
								    		this.app.statusMgr.updateStatus();
								    		if(values.append){
								    			uft.Utils.showWarnMsg(values.append);
								    		}
								    	}
								    });
								}
							});
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.EA_HANDLING]
				}));
				btns.push(new uft.extend.Button({
					text : '结案',//将状态更新为已处理
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
							var params=this.app.newAjaxParams();
							params[this.app.getBillIdField()]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : false,
						    	method : 'GET',
						    	url : 'finish.json',
						    	success : function(values){
						    		this.app.setHeaderValues(records,values.datas);
						    		this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		this.app.statusMgr.updateStatus();
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });	
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.EA_HANDLING]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销结案',//将状态更新为处理中
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
							var params=this.app.newAjaxParams();
							params[this.app.getBillIdField()]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : false,
						    	method : 'GET',
						    	url : 'unfinish.json',
						    	success : function(values){
						    		this.app.setHeaderValues(records,values.datas);
						    		this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		this.app.statusMgr.updateStatus();
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });	
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.EA_HANDLED]
				}));				
				btns.push(new uft.extend.Button({
					text : '关闭',//将状态更新为已关闭
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
							var params=this.app.newAjaxParams();
							params[this.app.getBillIdField()]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : false,
						    	method : 'GET',
						    	url : 'close.json',
						    	success : function(values){
						    		this.app.setHeaderValues(records,values.datas);
						    		this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		this.app.statusMgr.updateStatus();
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });	
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.EA_NEW,uft.jf.bizStatus.EA_WHANDLE,uft.jf.bizStatus.EA_HANDLING,uft.jf.bizStatus.EA_HANDLED]
				}));
				btns.push({
					xtype : 'button',
					text : '异常附件',
					iconCls : 'btnAttach',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.te.uploadExpAccdentAttachment({app:this.app,record:record}).show();
						}
					},
				});
				//btns.push(this.btn_attach);
				btns.push(this.btn_export);
				return btns;
			},
			recordCheck : function(onlyOne){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！');
					return false;
				}
				if(onlyOne){
					if(records.length > 1){
						uft.Utils.showWarnMsg('只能选择一条记录！');
						return false;
					}
				}
				return true;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

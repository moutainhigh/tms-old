<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<%if(debug){ %>
		<script type="text/javascript" src='<c:url value="/busi/cm/BuildReceCheckSheet.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/Receivable.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/ReceRecord.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/AddToReceCheckSheet.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/cm/uploadRDAttachment.js?v=${version}" />'></script>
		<%}else{ %>
		<script type="text/javascript" src='<c:url value="/busi/cm/rece-min.js?v=${version}" />'></script>	
		<%} %>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==33 || value == 34){
				meta.style+='color:#fff;';
			}
		}
		function invoice_vbillnoRenderer(value,meta,record){
			if(value==null || 'undefined'== typeof(value))
	    	{
	    	value = "";
	    	}
			var arr = value.split('|'),str='';
			for(var i=0;i<arr.length;i++){
				var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
				str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
				str+="|";
			}
			return str.substring(0,str.length-1);
		}
		//对于客户索赔类型，金额为负数，使用红色字体
		function amountBeforeRenderer(value,meta,record){
			if(value < 0){//负数
				meta.style+='color:red;';
			}
		}
		function lotRenderer(value,meta,record){
			var url = ctxPath+"/inv/orderlot/index.html?funCode=t203&_vbillno="+value;
			str= "<a href=\"javascript:uft.Utils.openNode('"+value+"','订单批次管理','"+url+"')\">"+value+"</a>";
			return str;
		}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css31{
				background-color: #FAC090;
			}
			.css32{
				background-color: #EBD6D6;
			}
			.css33{
				background-color: #7030A0;
			}
			.css34{
				background-color: #44964C;
			}
			.css35{
				background-color: #FFFF00;
			}
			.css36{
				background-color: #888888;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_import.text = "导入应收明细";
				this.btn_confirm.enabledBizStatus = [uft.jf.bizStatus.NEW,uft.jf.bizStatus.RD_CONFIRMING];
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.RD_CONFIRM];
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
				//btns.push(this.btn_attach);
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
					enabledBizStatus : [uft.jf.bizStatus.NEW]
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
					enabledBizStatus : [uft.jf.bizStatus.RD_CONFIRMING]
				});
				btns.push(this.btn_confirm);
				btns.push(this.btn_unconfirm);
				
				btns.push(new uft.extend.Button({
					text : '关闭',  
					iconCls : 'btnYes',
					handler : this.btn_close_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.NEW]
				}));
				
				btns.push(new uft.extend.Button({
					text : '取消关闭',  
					iconCls : 'btnCancel',
					handler : this.btn_unclose_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.RD_CLOSE]
				}));
				
				btns.push({
					text  : '对账单',
					menu : {
						xtype : 'menu',
						items : [{
							text : '创建对账单',
							scope : this,
							handler : function(){
								var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
								if(this.beforeAddCheckSheet(records)!==false){
									new uft.cm.BuildReceCheckSheet({app:this.app,records:records}).show();
								}
							},
							enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
							enabledBizStatus : [uft.jf.bizStatus.RD_CONFIRM]
						},{
							text : '加入对账单',
							scope : this,
							handler : function(){
								var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
								if(this.beforeAddCheckSheet(records)!==false){
									new uft.cm.AddToReceCheckSheet({app:this.app,records:records}).show();
								}
							},
							enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
							enabledBizStatus : [uft.jf.bizStatus.RD_CONFIRM]
						}]
					}
				});
				
				btns.push({
					text  : '收款',
					menu : {
						xtype : 'menu',
						items : [{
								text : '部分收款',
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
									new uft.cm.Receivable({app:this.app,record:records[0]}).show();
								},
								enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
								enabledBizStatus : [uft.jf.bizStatus.RD_CONFIRM,uft.jf.bizStatus.RD_PART_CAVLOAN]
							},{
								text : '全额收款',
								scope : this,
								handler : function(){
									var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
									if(!records || records.length == 0){
										uft.Utils.showWarnMsg('请选择记录！')
										return;
									}
									var params = this.app.newAjaxParams(),arr=[];
									for(var i=0;i<records.length;i++){
										arr.push(records[i].data['pk_receive_detail']);
									}
									params['pk_receive_detail'] = arr;
									uft.Utils.doAjax({
								    	scope : this,
								    	params : params,
								    	url : 'receivableAll.json',
								    	success : function(values){
								    		if(values){//保存成功直接销毁窗口
								    			if(values.datas){
								    				this.app._setHeaderValues(records,values.datas);
								    				this.app.statusMgr.setBizStatus(values.datas[0][this.app.getBillStatusField()]);
								    				this.app.statusMgr.updateStatus();
								    			}
								    		}
								    	}
								    });						
								},
								enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
								enabledBizStatus : [uft.jf.bizStatus.RD_CONFIRM,uft.jf.bizStatus.RD_PART_CAVLOAN]
							},{
								text : '查看收款记录',
								scope : this,
								handler : function(){
									var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
									if(!records || records.length == 0){
										uft.Utils.showWarnMsg('请选择记录！')
										return;
									}
									if(records.length > 1){
										uft.Utils.showWarnMsg('只能选择一条记录！')
										return;
									}
									new uft.cm.ReceRecord({app:this.app,record:records[0]}).show();
								},
								enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
								enabledBizStatus : [uft.jf.bizStatus.RD_PART_CAVLOAN,uft.jf.bizStatus.RD_CAVLOAN]
							}]
					}
				});
	
				btns.push({
					text : '重算金额', 
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
						if(ids.length > 0){
							Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('耗时操作，重算金额成功后请刷新页面！'), function(btn) {
								if (btn == 'yes') {
									var params=this.app.newAjaxParams();
									params[this.app.getBillIdField()]=ids;
								    uft.Utils.doAjax({
								    	scope : this,
								    	params : params,
								    	url : 'reComputeMny.json',
								    	success : function(values){
								    		if(values.datas){
							    				this.app._setHeaderValues(records,values.datas);
							    				//this.app.statusMgr.setBizStatus(values.datas[0][this.app.getBillStatusField()]);
							    				//this.app.statusMgr.updateStatus();
							    			}
								    	}
								    });
								}
							},this);
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.NEW]
				});
				/**
				 * 增加新的导出按钮，名称为 “集货应收明细导出” add by songf 2015-11-03
				**/
				
				btns.push({
					text  : '导入导出',
					menu : {
						xtype : 'menu',
						items : [{
							xtype : 'button',
							text : '集货应收明细导出',
							scope : this,
							handler : function(){
								if(this.oneRecordCheck()){
								    var url="exportPickupReceiveDetail.do";
									var index = 0;
									for(var key in receiveDetailGrid.selectedRows.receiveDetails){
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
						},
						this.btn_import,
						this.btn_export
						]
					}
				});
				btns.push({
					xtype : 'button',
					text : '应收附件',
					iconCls : 'btnAttach',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.cm.uploadRDAttachment({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				});
 
				/**  end songf  **/
				/* btns.push(this.btn_export);
				btns.push(this.btn_import);  */
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
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
			},
			//增加选择判断   songf  2015-11-03
			oneRecordCheck : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！')
					return false;
				}
				return true;
			},
			rebuildBySegtype : function(seg_type){
				var app = this.app;
				var ids = [];
				for(var key in receiveDetailGrid.selectedRows.receiveDetails){
					ids.push(key);
				}
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录');
					return false;
				}
				var params=app.newAjaxParams();
				params[app.getBillIdField()]=ids;
				params['seg_type'] = seg_type;
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	url : 'rebuildBySegtype.json',
			    	success : function(values){
			    		if(values.append){
			    			uft.Utils.showWarnMsg(values.append);
			    		}
			    		this.btn_ref.fireEvent("click");
			    	}
			    });
			},
			beforeAddCheckSheet : function(records){
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！')
					return false;
				}
				var bala_customer = records[0].data['bala_customer'];//结算客户
				for(var i=1;i<records.length;i++){
					var newBala_customer = records[i].data['bala_customer'];
					var vbillstatus = records[i].data['vbillstatus'];
					if(vbillstatus != uft.jf.bizStatus.RD_CONFIRM){
						uft.Utils.showWarnMsg('必须是已确认的应收明细才能加入对账单！')
						return false;
					}
					if(bala_customer != newBala_customer){
						uft.Utils.showWarnMsg('必须是同一个结算客户的应收明细才能加入对账单！')
						return false;
					}
				}
			},
			setFieldStatus : function(isAdd){
				//单据类型,发货单号,客户,结算客户,结算方式
				var group1 = ['rece_type','invoice_vbillno','pk_customer','bala_customer'];
				var value;
				if(isAdd){
					value = 1;
				}else{
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					if(record){
						value = record.data['rece_type'];
					}
				}
				if(value === 0 || value === '0'){//可能是一个number，也可能是string
					this.setReadOnlyFields(group1,true);
				}else{
					this.setReadOnlyFields(group1,false);
				}
			},
			//针对单据类型为原始单据和其他单据的不同，有些字段可以编辑，有些不行
			setReadOnlyFields : function(fieldAry,readOnly){
				for(var i=0;i<fieldAry.length;i++){
					var field = uft.Utils.getField(fieldAry[i]);
					field.setReadOnly(readOnly);
					if(readOnly){
						field.label.addClass('uft-form-label-not-edit');
					}else{
						field.label.removeClass('uft-form-label-not-edit');
					}
				}
			},
			btn_close_handler : function(){
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
					    	url : 'close.json',
					    	success : function(values){
					    		this.app.setHeaderValues(records,values.datas);
					    		if(values.datas&&values.datas.length>0){
					    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
					    		}
					    	}
					    });
					
				}else{
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
			},
			btn_unclose_handler : function(){
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
						    	url : 'unclose.json',
						    	success : function(values){
						    		this.app.setHeaderValues(records,values.datas);
						    		if(values.datas&&values.datas.length>0){
						    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		}
						    	}
						    });
						}
			},
			
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				this.setFieldStatus(false);
			},
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				this.setFieldStatus(true);
			},
			//保存后处理表体,不需要重新加载运力信息和包装明细的tab
			processBodyAfterSave : function(headerPk){
				if(this.app.hasBodyGrid()) {
					var bodyGrids = this.app.getBodyGrids();
					for(var i = 0; i < bodyGrids.length; i++) {
						var id = bodyGrids[i].id;
						if(id != 'ts_trans_bility_b' && id != 'ts_inv_pack_b' && id != 'ts_pay_devi_b'){
							this.app.loadBodyGrid(bodyGrids[i],headerPk);
						}
					}
				}
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_add_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_add_handler.call(this);
				updateHeaderCostAmount();
			},
			btn_row_del_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_rece_detail_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					 if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 //不能修改系统创建的费用明细（从合同匹配而来的记录）
						 uft.Utils.showWarnMsg('不能删除系统费用明细！');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				updateHeaderCostAmount();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				updateHeaderCostAmount();
			},
			btn_row_pas_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_rece_detail_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 //不能修改系统创建的费用明细（从合同匹配而来的记录）
						 uft.Utils.showWarnMsg('系统费用明细不允许复制');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				updateHeaderCostAmount();
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		var tabPanel = app.getBodyTabPanel();
		tabPanel.addListener('tabchange',function(tabPanel,activePanel){
			if(activePanel.id=='ts_inv_pack_b' || activePanel.id=='ts_trans_bility_b'){
				//货品包装信息和运力信息
				app.bodyAssistToolbar.setDisabled(true);
			}else{
				if(app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_ADD || app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_EDIT){
					app.bodyAssistToolbar.setDisabled(false);
				}
			}
		});
		app.statusMgr.addBeforeUpdateCallback(function(){
			var rece_type = Ext.getCmp('rece_type');
			if(app.statusMgr.getTransitionPageStatus() == uft.jf.pageStatus.OP_ADD){
				//新增状态，移除原始单据选项
				var r = rece_type.findRecord(rece_type.valueField, 0);
				if(r){
					rece_type.store.remove(r);
				}
			}else{
				//判断是否存在原始单据选项，不存在则加上
				var r = rece_type.findRecord(rece_type.valueField, 0);
				if(!r){
					r = new Ext.data.Record();
					r.set(rece_type.displayField,'原始单据');
					r.set(rece_type.valueField,0);
					rece_type.store.insert(1,r);
				}
			}
		},this);
		var ts_rece_detail_b = Ext.getCmp('ts_rece_detail_b');//费用明细表
		ts_rece_detail_b.on('beforeedit',function(e){
			 var r = e.record,system_create = r.get('system_create');
			 if(String(system_create) == 'true' || String(system_create) == 'Y'){
				 //不能修改系统创建的费用明细（从合同匹配而来的记录）
				 //2015-01-07 可以修改系统创建的金额，使用一个合同金额来显示原始匹配的金额
				 //return false;
			 }
		 });
		var store = ts_rece_detail_b.getStore();
		function compute(valuationType,v){
			for(var i=0;i<store.getCount();i++){
				var record = store.getAt(i);
				var quote_type = record.get('quote_type'); //报价类型
				var valuation_type = record.get('valuation_type'); //计价方式
				var price_type = record.get('price_type'); //价格类型
				var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
				if(valuation_type==null ||valuation_type==='' || !price){
					return;
				}
				if(String(quote_type)=="0"){//区间报价
					if(String(price_type)=="0"){//价格类型=单价
						var i_valuation_type = parseInt(valuation_type);
						if(i_valuation_type == valuationType){//件数，重量（实际上是计费重），体积
							record.set('amount',v*price);
						}
					}
				}
			}
			//更新表头的总金额
			updateHeaderCostAmount();
		}
		//表头编辑计费件数后，读取费用明细中计价方式是件数的记录，计算总金额
		function afterChangeNumCount(field,value,originalValue){
			compute(2,value);
		}
		//表头编辑计费重量后，读取费用明细中计价方式是重量的记录，计算总金额
		function afterChangeFeeWeightCount(field,value,originalValue){
			compute(0,value);
		}
		//表头编辑计费体积后，读取费用明细中计价方式是体积的记录，计算总金额
		function afterChangeVolumeCount(field,value,originalValue){
			compute(1,value);
		}
		
		//编辑计价方式、报价类型、单价时，更新金额，及表头的总金额
		function afterEditQuoteTypeOrValuationTypeOrPrice(record){
			var quote_type = record.get('quote_type'); //报价类型
			var valuation_type = record.get('valuation_type'); //计价方式
			var price_type = record.get('price_type'); //价格类型
			var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
			if(valuation_type==null ||valuation_type==='' || !price){
				return;
			}
			if(String(quote_type)=="0"){//区间报价
				if(String(price_type)=="0"){//价格类型=单价
					var amount = 0;
					var i_valuation_type = parseInt(valuation_type);
					switch(i_valuation_type){ 
						case 0: //重量
							var fee_weight_count = uft.Utils.getNumberFieldValue('fee_weight_count');//总计费重
							amount = fee_weight_count*price;
							break;
						case 1: //体积
							var volume_count = uft.Utils.getNumberFieldValue('volume_count'); //总体积
							amount = volume_count*price;
							break;
						case 2: //件数
							var num_count = uft.Utils.getNumberFieldValue('num_count'); //总件数
							amount = num_count*price;
							break;
						case 3: //设备
							var valueMap = uft.Utils.getGridSumValueMap('ts_trans_bility_b','num'); //车辆数量
							amount = valueMap['num']*price;
							break;
						case 4: //吨公里，应收明细更改到这个没有意义
							
							break;
						case 6: //节点 FIXME 后面会用到，应收明细更改到这个没有意义
							break;
					}
					uft.Utils.setColumnValue(record,'amount',amount);
				}
			}else{
				uft.Utils.setColumnValue(record,'price',null); //将单价置为空
			}
			//更新表头的总金额
			updateHeaderCostAmount();
		}	
		
		/**
		 * 增加应收跨页面选择功能   2015-11-02 songf
		 */
		//表格加载完成后，选中之前选择过的 
			//应收明细增加跨页选择 
			var receiveDetailGrid = Ext.getCmp('ts_receive_detail');		
			receiveDetailGrid.store.on("load",function(store,records,param){
				var recordsToSelect = [];
				for(var i=0;i<records.length;i++){
					if(receiveDetailGrid.selectedRows.receiveDetails[records[i].data.pk_receive_detail]!=null){
						recordsToSelect.push(records[i]);
					}
				}
				receiveDetailGrid.getSelectionModel().selectRecords(recordsToSelect);
			});
			
			//存放在cookie中的已选择行
			//receiveDetails:{
			//	pk_receive_detail1:{vbillno:YFMXXXXXXXXX,orderno:EDXXXXXXX,invoice_vbillno:FHDXXXXXXX},
			//	pk_receive_detail2:{vbillno:YFMXXXXXXXXX,orderno:EDXXXXXXX,invoice_vbillno:FHDXXXXXXX}
			//}
			//以这样的key:value形式存放在对象中
			var selectedRows = {};
			receiveDetailGrid.selectedRows = selectedRows;
			selectedRows.receiveDetails={};
			var receiveDetail={};
			var pk_receive_detail;
			//增加headerGrid的行选择事件
			receiveDetailGrid.getSelectionModel().addListener({
				'rowselect' : onRowselect,scope : this
			});	
			//增加headerGrid的行选择事件
			receiveDetailGrid.getSelectionModel().addListener({
				'rowdeselect' : onRowDeSelect,scope : this
			});
			//行选择事件
			function onRowselect(sm, rowIndex, record) {
				pk_receive_detail = record.get('pk_receive_detail');
				receiveDetail.vbillno = record.get('vbillno');
				receiveDetail.orderno = record.get('orderno');
				receiveDetail.invoice_vbillno = record.get('invoice_vbillno');
				selectedRows.receiveDetails[pk_receive_detail] = receiveDetail;
			}
			//取消行选择事件
			function onRowDeSelect(sm, rowIndex, record) {
				pk_receive_detail = record.get('pk_receive_detail');
				if(selectedRows.receiveDetails[pk_receive_detail]!=null){	
					delete selectedRows.receiveDetails[pk_receive_detail];			
				}			
			}

		
		/**
		 * 检测费用类型是否唯一
		 * @param {} record
		 */
		function afterEditExpenseTypeName(record){
			var grid = Ext.getCmp('ts_rece_detail_b');
			var store = grid.getStore();
			var pk_expense_type = record.get('pk_expense_type');
			var expense_type_code = record.get('expense_type_code');
			if(expense_type_code == 'ET10'){
				for(var i=0;i<store.getCount();i++){
					var _record = store.getAt(i);
					if(record.id != _record.id && pk_expense_type == _record.get('pk_expense_type')){
						uft.Utils.showErrorMsg('该费用类型已经存在！');
						record.set('pk_expense_type',null);
						record.set('expense_type_name',null);
						return;
					}
				}
			}
		}
		//编辑表体金额时，更新表头的总金额
		function updateHeaderCostAmount(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_rece_detail_b','amount');//费用明细的金额
			uft.Utils.getField('cost_amount').setValue(resultMap['amount']);
			//未收金额等于总金额减去已收金额
			var got_amount = uft.Utils.getNumberFieldValue('got_amount');
			var cost_amount = uft.Utils.getNumberFieldValue('cost_amount');
			uft.Utils.getField('ungot_amount').setValue(cost_amount-got_amount);
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

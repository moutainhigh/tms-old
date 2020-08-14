<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==4 || value == 6){
				meta.style+='color:#fff;';
			}
		}
		
		function urgent_levelBeforeRenderer(value,meta,record){
			if(value ==2){
				meta.css = 'cssRed';
				meta.style+='color:#fff;';
			}
		}
		
		function req_deli_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 1){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function req_arri_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 2){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function exp_flagBeforeRenderer(v,meta,record){
			if(v && (v == 1 || v == 'Y' || v == 'true')){
				meta.css = 'cssRed';
			}
			return v;
		}
		
		
		function orderTracking(record){
			window.open(ctxPath+"/busi/ts/orderTrackingInfo.html"); 
		}
		
		function trackInfoRenderer(value,meta,record){
			var arr = value.split('|'),str='';
			for(var i=0;i<arr.length;i++){
				str+= "<a href=\"javascript:orderTracking('"+ record +"')\">"+arr[i]+"</a>";
				str+="|";
			}
			return str.substring(0,str.length-1);
		}
		
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css1{
				background-color: #FAC090;
			}
			.css2{
				background-color: #FFFF00;
			}
			.css3{
				background-color: #538ED5;
			}
			.css4{
				background-color: #7030A0;
			}
			.css6{
				background-color: #44964C;
			}
			.cssRed{
				background-color: #FF0000;
			}
			.css7{
				background-color: #FFFF00;
			}
			.css8{
				background-color: #538ED5;
			}
			.css9{
				background-color:#888888;
			}
		</style>
	</head>
	<body>
		<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="false"  headerGridPageSizePlugin="true"		bodyGridsPagination="false,false,false" 
		bodyGridsDataUrl="loadData.json,loadData.json,refreshReceDetail.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>			
	</body>
	<script type="text/javascript">
		var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_confirm.enabledBizStatus = [uft.jf.bizStatus.NEW];
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.INV_CONFIRM];
				this.btn_revise.enabledBizStatus = [uft.jf.bizStatus.INV_CONFIRM,uft.jf.bizStatus.INV_DELIVERY,uft.jf.bizStatus.INV_PART_DELIVERY,uft.jf.bizStatus.INV_ARRIVAL,uft.jf.bizStatus.INV_PART_ARRIVAL];
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_copy);
				btns.push(this.btn_edit);
				btns.push(this.btn_revise);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_confirm);
				btns.push(this.btn_unconfirm);
				btns.push(this.btn_sms);
				
				btns.push({
					text  : '导入操作',
					menu : {
						xtype : 'menu',
						items : [{
							variable : 'btn_stard_import',
							text : '集货导入',
							scope:this,
							handler : function(){
								new uft.extend.UploadWindow({
									id:'stard_import',
									params : this.app.newAjaxParams(),
									extendItems : [new Ext.BoxComponent({
										height: 25,
										html : '<div><a href="customDownloadTemplet.do?funCode=t205" target="_blank">下载模板</a></div>'
									})],
									submitUrl : 'customImport.do',
									isLog : true,
									closeAction : 'close',
									permitted_extensions : ['xls','xlsx']
								}).show();
							},
							enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
						},
						this.btn_import,
						{
							variable : 'btn_ds_update',
							text : '集货更新',
							scope:this,
							handler : function(){
								new uft.extend.UploadWindow({
									id:'importWin_update',
									params : this.app.newAjaxParams(),
									submitUrl : 'ds_update.do',
									isLog : true,
									closeAction : 'close',
									permitted_extensions : ['xls','xlsx']
								}).show();
							},
							enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
						},
						this.btn_export,
						{
							variable : 'btn_milkRun_import',
							text : 'MilkRun导入',
							scope:this,
							handler : function(){
								new uft.extend.UploadWindow({
									id:'milkRun_import',
									params : this.app.newAjaxParams(),
									extendItems : [new Ext.BoxComponent({
										height: 25,
										html : '<div><a href="milkRunDownloadTemplet.do?funCode=t207" target="_blank">下载模板</a></div>'
									})],
									submitUrl : 'milkRunImport.do',
									isLog : true,
									closeAction : 'close',
									permitted_extensions : ['xls','xlsx']
								}).show();
							},
							enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
						},]
					}
				});
				
				btns.push(new uft.extend.Button({
					text : 'Milkrun', 
					iconCls : 'btnSetting',
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择发货单！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_invoice:ids},
					    	url : 'milkRun.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_CONFIRM]
				}));
				
				btns.push(new uft.extend.Button({
					text : '一键配载', 
					iconCls : 'btnSetting',
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择发货单！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_invoice:ids},
					    	url : 'keyStowage.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_CONFIRM]
				}));
				
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
					enabledBizStatus : [uft.jf.bizStatus.INV_CLOSE]
				}));
				
				btns.push(this.btn_print);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				return btns;
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
			
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				//定位到第一个可编辑的项
				var arr = this.headerCard.focusNextField();
			},
			btn_milkrun_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
				if(!ids || ids.length == 0){
					uft.extend.tip.Tip.msg('warn','请先选择发货单！');
					return;
				}
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : {pk_invoice:ids},
			    	url : 'milkRun.json',
			    	success : function(values){
			    		//重新加载数据
			    		this.app.headerGrid.getStore().reload();
			    	}
			    });	
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var vbillno = uft.Utils.getField('vbillno');
				vbillno.setReadOnly(true);
// 				var cust_orderno = uft.Utils.getField('cust_orderno');
// 				cust_orderno.setReadOnly(true);
// 				var orderno = uft.Utils.getField('orderno');
// 				orderno.setReadOnly(true);
			},
			processBodyAfterSave : function(headerPk){
				if(this.app.hasBodyGrid()) {
					//加载所有表体的数据
					for(var i = 0; i < this.app.bodyGrids.length; i++) {
						if(this.app.bodyGrids[i].id != 'ts_rece_detail_b'){
							var ds = this.app.bodyGrids[i].getStore();
							ds.setBaseParam(this.app.headerPkField, headerPk);
							ds.reload();
						}else{
							refreshReceDetail();	
						}
					}
				}
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_add_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_add_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_del_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_rece_detail_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					 if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 //不能修改系统创建的费用明细（从合同匹配而来的记录）
						 uft.Utils.showWarnMsg('不能删除系统创建的费用明细！');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			getRowPasDefaultValue : function(gridId,selectRecordValue){
				var value = MyBodyAssistToolbar.superclass.getRowPasDefaultValue.call(this,gridId,selectRecordValue);
				value['expense_type_name']=null;//清空费用类型
				value['pk_expense_type']=null;
				return value;
			},
			btn_row_cop_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_rece_detail_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					 if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 uft.Utils.showWarnMsg('不能复制系统创建的费用明细！');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_cop_handler.call(this);
			},
			btn_row_pas_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				afterEditBodyAssistToolbar();
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		${moduleName}.appUiConfig.toolbar.on('beforerevise',function(toolbar,record){
			//校验是否能够进行修订
			var billId = record.get('pk_invoice');
			var result = Utils.request({
				type : false,//同步请求
				url : 'checkBeforeRevise.json',
				params : {billId : billId}
			});
			if(result.data != 'Y'){//不能修订
				return false;
			}
			return true;
		},this);
		
		//如果系统匹配了2个相同类型的费用，那么需要提示
		${moduleName}.appUiConfig.toolbar.on('beforesave',function(toolbar,params){
			var grid = Ext.getCmp('ts_rece_detail_b');
			var store = grid.getStore();
			var pks = [];
			for(var i=0;i<store.getCount();i++){
				var _record = store.getAt(i);
				var system_create = _record.get('system_create');
				if(!system_create || (String(system_create) != 'Y' && String(system_create) != 'true')){
					continue;
				}
				var pk = _record.get('pk_expense_type');
				var code = _record.get('expense_type_code');
				var exist = false;
				for(var j=0;j<pks.length;j++){
					if(pk == pks[j]){
						//pk已经存在
						exist = true;
						break;
					}
				}
				if(!exist){
					pks.push(pk);
				}else{
					var r=confirm("费用明细中存在相同费用类型["+code+"]的记录，您确认要保存吗？");
					if(r==true){
						
					}else{
						return false;
					}
				}
			}
		},this);
		
		//使用一个ajax缓存去读取运输方式和换算比率的缓存map
		var transTypeFeeMap = {};
		afterEditHead = function(field,value,oriValue){
			if(field.id == 'bala_customer'){
				//编辑了客户以后，加载运输方式和换算比率的缓存map
				uft.Utils.doAjax({
			    	scope : this,
			    	isTip : false,
			    	params : {bala_customer : value},
			    	url : 'getTransTypeRateMap.json',
			    	success : function(values){
			    		if(values.data){
			    			transTypeFeeMap = values.data;
			    		}
			    	}
			    });
			}else if(field.id == 'req_arri_date'){
				//合同日期等于要求到货日期
				var con_arri_date = Ext.getCmp('con_arri_date');
				if(con_arri_date){
					con_arri_date.setValue(value);
				}
			}
		}		
		
		
		//编辑要求提货日期，比较下提货日期和收货日期
		function afterChangeReq_deli_date(field,value,originalValue){
			var req_arri_date = Ext.getCmp('req_arri_date').getValue();
			if(value && req_arri_date){
				if(value > req_arri_date){
					uft.Utils.showWarnMsg('要求提货日期不能大于要求收货日期！');
					return;
				}
			}
		}
		//编辑要求收货日期，比较下提货日期和收货日期
		function afterChangeReq_arri_date(field,value,originalValue){
			var req_deli_date = Ext.getCmp('req_deli_date').getValue();
			if(value && req_deli_date){
				if(value < req_deli_date){
					uft.Utils.showWarnMsg('要求收货日期不能小于要求提货日期！');
					return;
				}
			}
		}
		//当选择提货方时，加载提货方的相关信息，这里本来是使用公式就可以了，但是对于返回空值需要做特殊处理，所以在该函数中处理
		function afterChangePk_delivery(){
			var pk_delivery = uft.Utils.getField('pk_delivery').getValue();
			if(pk_delivery){
				loadAddrInfoByPkAddress(pk_delivery,0);
			}
		}
		//当选择到货方时，加载到货方的相关信息，这里本来是使用公式就可以了，但是对于返回空值需要做特殊处理，所以在该函数中处理
		function afterChangePk_arrival(){
			var pk_arrival = uft.Utils.getField('pk_arrival').getValue();
			if(pk_arrival){
				loadAddrInfoByPkAddress(pk_arrival,1);
			}
		}
		//当结算方式为现金到付时要求到付金额为必输项
		function afterChageBalatype(field,value,originalValue){
			//现金到付的值，在数据字典中定义的
			var arri_pay = '<%=com.tms.constants.DataDictConst.BALATYPE.ARRI_PAY.intValue()%>';
			var arri_pay_amount = uft.Utils.getField('arri_pay_amount');
			if(String(value) == arri_pay){
				arri_pay_amount.allowBlank = false;
				arri_pay_amount.label.parent().addClass('uft-form-label-not-null');
			}else{
				arri_pay_amount.allowBlank = true;
				arri_pay_amount.label.parent().removeClass('uft-form-label-not-null');
			}
		}
		function loadAddrInfoByPkAddress(pk_address,type){
			var body = Ext.getBody();
			body.mask(uft.jf.Constants.PROCESS_MSG);//显示操作提示
			var values = Utils.request({
				type : false,//不能使用异步请求，道理同执行公式一样
				params : {pk_address:pk_address,type:type},
				url : 'loadAddrInfoByPkAddress.json'
			});
			if(values.data){
				for(var key in values.data){
					uft.Utils.getField('key').setValue(values.data[key]);
				}
			}
			body.unmask();
		}
		// 默认根据提货方城市和收货方城市去匹配路线类型为运输里程的线路，以此获取运输里程
		function getMileageAndDistance(){
			var deli_city = uft.Utils.getField('deli_city').getValue();//提货地城市
			var arri_city = uft.Utils.getField('arri_city').getValue();//收货地城市
			var pk_delivery = uft.Utils.getField('pk_delivery').getValue();//提货方
			var pk_arrival = uft.Utils.getField('pk_arrival').getValue();//收货方
			if(pk_delivery && pk_delivery.length>0 && pk_arrival && pk_arrival.length>0 && pk_delivery != pk_arrival){
				uft.Utils.doAjax({
			    	scope : this,
			    	params : {deli_city:deli_city,arri_city:arri_city,pk_arrival:pk_arrival,pk_delivery:pk_delivery},
			    	isTip : false,
			    	url : 'getMileageAndDistance.json',
			    	success : function(values){
			    		if(values.data){
				    		uft.Utils.getField('mileage').setValue(values.data['mileage']);
				    		uft.Utils.getField('distance').setValue(values.data['distance']);
			    		}
			    	}
			    });
			}else{
				uft.Utils.getField('mileage').setValue(null);
	    		uft.Utils.getField('distance').setValue(null);
			}
		}
		//编辑件数后，计算重量和体积，更新表头的重量和体积
		function afterEditNumOfPack(record){
			var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			record.beginEdit();
			uft.Utils.setColumnValue(record,'weight',num*unit_weight);
			uft.Utils.setColumnValue(record,'volume',num*unit_volume);
			//数量=件数×包装的数量
			var pack_num = uft.Utils.getNumberColumnValue(record,'pack_num');
			uft.Utils.setColumnValue(record,'pack_num_count',pack_num*num);
			
			record.endEdit();
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//更新数量
		function afterEditPackNumCount(record){
			updateHeaderPackSummary();
		}
		//编辑包装后，如果匹配到货品，则带出包装的信息，同时需要更新统计信息
		function afterEditPack(record){
			var pk_goods = uft.Utils.getColumnValue(record,'pk_goods');
			var pack = uft.Utils.getColumnValue(record,'pack'); //包装字段的值
			if(pk_goods && pack){
				//读取包装单位的其他信息，从(ts_goods_pack_rela)读取
				uft.Utils.doAjax({
			    	scope : this,
			    	params : {pk_goods:pk_goods,pk_goods_packcorp:pack},
			    	isTip : false,
			    	url : 'getGoodsPackcorpInfo.json',
			    	success : function(values){
			    		if(values.data){
			    			record.beginEdit();
			    			var unit_weight = values.data['weight'];
			    			var unit_volume = values.data['volume'];
			    			uft.Utils.setColumnValue(record,'unit_weight',unit_weight); //设置单位重
			    			uft.Utils.setColumnValue(record,'unit_volume',unit_volume);//单位体积
			    			uft.Utils.setColumnValue(record,'length',values.data['length']);//长
			    			uft.Utils.setColumnValue(record,'width',values.data['width']);//宽
			    			uft.Utils.setColumnValue(record,'height',values.data['height']);//高
			    			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			    			uft.Utils.setColumnValue(record,'weight',num*unit_weight);
			    			uft.Utils.setColumnValue(record,'volume',num*unit_volume);
			    			//数量=件数×包装的数量
			    			var pack_num = values.data['pack_num'];//最小单位的数量
			    			uft.Utils.setColumnValue(record,'pack_num',pack_num);
			    			if(pack_num){
			    				uft.Utils.setColumnValue(record,'pack_num_count',num*pack_num);
			    			}
			    			record.endEdit();
			    			updateHeaderPackSummary();
			    			updateCostDetailAmount();
			    			updateHeaderCostAmount();
			    		}
			    	}
			    });
			}
		}
		// 编辑“货品”时，需要将货品的条件加入包装的参照中
		function afterEditGoodsCode(record){
			var pk_goods = uft.Utils.getColumnValue(record,'pk_goods');
			var grid = Ext.getCmp('ts_inv_pack_b');
			if(grid){
				var packEditor = uft.Utils.getColumnEditor(grid,'pack_name');
				packEditor.addExtendParams({pk_goods:pk_goods});
			}
		}
		//更新长宽高时，自动计算单位体积，体积，以及汇总信息
		function afterEditLengthOrWidthOrHeight(record){
			var length = uft.Utils.getNumberColumnValue(record,'length');
			var width = uft.Utils.getNumberColumnValue(record,'width');
			var height = uft.Utils.getNumberColumnValue(record,'height');
			var unit_volume = length*width*height;
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			record.beginEdit();
			uft.Utils.setColumnValue(record,'unit_volume',unit_volume); //单位体积
			uft.Utils.setColumnValue(record,'volume',unit_volume*num); //体积
			record.endEdit();
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//更新体积时，更新汇总信息
		function afterEditVolume(record){
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//更新重时，更新汇总信息
		function afterEditWeight(record){
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//更新单位体积时，计算体积，以及汇总信息
		function afterEditUnit_volume(record){
			unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			uft.Utils.setColumnValue(record,'volume',unit_volume*num);
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//更新单位重时，计算总重量，以及汇总信息
		function afterEditUnit_weight(record){
			unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			uft.Utils.setColumnValue(record,'weight',unit_weight*num);
			updateHeaderPackSummary();
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//编辑运力信息的件数
		function afterEditNumOfCar(record){
			//计算金额
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
			uft.Utils.setColumnValue(record,'amount',price*num);
			updateCostDetailAmount();
			updateHeaderCostAmount();
		}
		//编辑运力信息的单价
		function afterEditPriceOfCar(record){
			//计算金额
			var num = uft.Utils.getNumberColumnValue(record,'num'); //件数
			var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
			uft.Utils.setColumnValue(record,'amount',price*num);
		}		
		//编辑计价方式、报价类型、单价时，更新金额，及表头的总金额
		function afterEditQuoteTypeOrValuationTypeOrPrice(record,autoUpdateHeader){
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
						case 4: //吨公里
							var weight_count = uft.Utils.getNumberFieldValue('weight_count');//总重量
							var distance = uft.Utils.getField('distance').getValue();//区间距离
							amount = (weight_count/1000)*distance*price;
							break;
						case 6: //节点 FIXME 后面会用到
							break;
						case 7:
							var pack_num_count = uft.Utils.getNumberFieldValue('pack_num_count');//总数量
							break;
					}
					uft.Utils.setColumnValue(record,'amount',amount);
				}
			}else{
				uft.Utils.setColumnValue(record,'price',null); //将单价置为空
			}
			if(autoUpdateHeader !== false){
				//更新表头的总金额
				updateHeaderCostAmount();
			}
		}
		//更新表头的总计费重(fee_weight_count)，更新运输方式时以及更新统计信息时会更新总计费重
		function updateHeaderFeeWeightCount(){
			var pk_trans_type = uft.Utils.getField('pk_trans_type').getValue();
			if(pk_trans_type && pk_trans_type.length > 0){
				var rate = transTypeFeeMap[pk_trans_type];
				if(rate){
					//缓存中已经存在换算比率
					var volume_count = uft.Utils.getField('volume_count').getValue(); //总体积
	    			var weight_count = uft.Utils.getField('weight_count').getValue(); //总重量
	    			var volume_weight_count = volume_count*rate;//总体积重=总体积*体积重换算比率
	    			uft.Utils.getField('volume_weight_count').setValue(volume_weight_count);
	    			var fee = volume_weight_count; //总体积/体积重换算比率
	    			if(fee < weight_count){
	    				fee =  weight_count;
	    			}
		    		uft.Utils.getField('fee_weight_count').setValue(fee);
				}else{
					var bala_customer = Ext.getCmp('bala_customer').getValue();
					uft.Utils.doAjax({
				    	scope : this,
				    	params : {pk_trans_type:pk_trans_type,bala_customer:bala_customer},
				    	isTip : false,
				    	url : 'getTransTypeRate.json',
				    	success : function(values){
				    		if(values.data){
				    			var volume_count = uft.Utils.getField('volume_count').getValue(); //总体积
				    			var weight_count = uft.Utils.getField('weight_count').getValue(); //总重量
				    			var fee = volume_count*values.data; //总体积*体积重换算比率
				    			uft.Utils.getField('volume_weight_count').setValue(fee);
				    			if(fee < weight_count){
				    				fee =  weight_count;
				    			}
					    		uft.Utils.getField('fee_weight_count').setValue(fee);
				    		}
				    	}
				    });
				}
			}
		}
		//更新了表头的汇总信息后，需要更新费用明细的金额信息
		function updateCostDetailAmount(){
			var grid = Ext.getCmp('ts_rece_detail_b');
			if(grid){
				var store = grid.getStore(), count = store.getCount();
				for(var i=0;i<count;i++){
					var record = store.getAt(i);
					afterEditQuoteTypeOrValuationTypeOrPrice(record,false); //更新每行的金额
				}
			}
		}
		//更新表头的包装类别的统计信息，包括总件数、总重量、总体积
		function updateHeaderPackSummary(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_inv_pack_b',['pack_num_count','num','weight','volume']);
			uft.Utils.getField('pack_num_count').setValue(resultMap['pack_num_count']);
			uft.Utils.getField('num_count').setValue(resultMap['num']);
			uft.Utils.getField('weight_count').setValue(resultMap['weight']);
			uft.Utils.getField('volume_count').setValue(resultMap['volume']);
			updateHeaderFeeWeightCount(); //更新总计费重
		}		
		//更新表头的总金额
		function updateHeaderCostAmount(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_rece_detail_b','amount');//费用明细的金额
			uft.Utils.getField('cost_amount').setValue(resultMap['amount']);
		}
		//操作辅助工具栏时的更新内容 
		function afterEditBodyAssistToolbar(){
			var grid = app.getActiveBodyGrid();
			if(grid.id == 'ts_inv_pack_b'){
				//货品包装明细
				updateHeaderPackSummary();
				updateCostDetailAmount();
				updateHeaderCostAmount();
			}else if(grid.id == 'ts_trans_bility_b'){
				//运力信息
				updateCostDetailAmount();
				updateHeaderCostAmount();
			}else if(grid.id == 'ts_rece_detail_b'){
				updateHeaderCostAmount();
				//refreshReceDetail();
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
						uft.Utils.showWarnMsg('费用类型为[运费]的记录已经存在！');
						record.set('pk_expense_type',null);
						record.set('expense_type_name',null);
						record.set('expense_type_code',null);
						return;
					}
				}
			}
		}
		 var receDetailGrid = Ext.getCmp('ts_rece_detail_b'); //费用明细表
		 receDetailGrid.on('beforeedit',function(e){
			 var r = e.record,system_create = r.get('system_create');
			 if(String(system_create) == 'true' || String(system_create) == 'Y'){
				 //不能修改系统创建的费用明细（从合同匹配而来的记录）
				 return false;
			 }
		 });
		//加载费用明细
		function refreshReceDetail(){
			var bala_customer = uft.Utils.getField('bala_customer').getValue();//结算客户
			var pk_trans_type = uft.Utils.getField('pk_trans_type').getValue();//运输方式
			var pk_delivery = uft.Utils.getField('pk_delivery').getValue();//提货方
			var pk_arrival = uft.Utils.getField('pk_arrival').getValue();//收货货方
			var deli_city = uft.Utils.getField('deli_city').getValue();//提货城市
			var arri_city = uft.Utils.getField('arri_city').getValue();//收货城市
			//yaojiie 2016 1 11 增加3个字段
			var urgent_level = uft.Utils.getField('urgent_level').getValue();//紧急程度
			var item_code = uft.Utils.getField('item_code').getValue();//项目编码
			var pk_trans_line = uft.Utils.getField('pk_trans_line').getValue();//线路
			var ts_trans_bility_b = Ext.getCmp('ts_trans_bility_b');
			var pk_car_type = [];//车辆类型
			var store = ts_trans_bility_b.getStore();
			for(var i=0;i<store.getCount();i++){
				var record = store.getAt(i);
				pk_car_type.push(record.get('pk_car_type'));
			}
			if(bala_customer && pk_trans_type && pk_delivery && pk_arrival){
				var pack_num_count = uft.Utils.getNumberFieldValue('pack_num_count'); //总数量
				var num_count = uft.Utils.getNumberFieldValue('num_count');//总件数
				var weight_count = uft.Utils.getNumberFieldValue('weight_count'); //总体积
				var volume_count = uft.Utils.getNumberFieldValue('volume_count'); //总体积
				var fee_weight_count = uft.Utils.getNumberFieldValue('fee_weight_count'); //总计费重
				var options={};
				options.params={};
				options.params['pk_invoice'] = Ext.getCmp('pk_invoice').getValue();
				options.params['pk_corp'] = Ext.getCmp('pk_corp').getValue();
				options.params['req_arri_date'] = Ext.getCmp('req_arri_date').getValue();
				options.params['pack_num_count']=pack_num_count;
				options.params['num_count']=num_count;
				options.params['weight_count']=weight_count;
				options.params['volume_count']=volume_count;
				options.params['fee_weight_count']=fee_weight_count;
				options.params['bala_customer']=bala_customer;
				options.params['pk_trans_type']=pk_trans_type;
				options.params['pk_car_type']=pk_car_type;
				options.params['pk_delivery']=pk_delivery;
				options.params['pk_arrival']=pk_arrival;
				options.params['deli_city']=deli_city;
				options.params['arri_city']=arri_city;
				options.params['urgent_level']=urgent_level;
				options.params['item_code']=item_code;
				options.params['pk_trans_line']=pk_trans_line;
				receDetailGrid.getStore().reload(options);
			}
		}
		//当加载了费用明细的记录后，需要更新表头的总金额
		receDetailGrid.getStore().on('load',function(){
			afterReloadDetailGrid();
		},this);
		//重新加载了费用明细后的动作
		afterReloadDetailGrid = function(){
			updateHeaderCostAmount();
			//匹配合同的费用明细后，处理运力信息的单价和金额
			var ts_trans_bility_b = Ext.getCmp('ts_trans_bility_b');
			var store = ts_trans_bility_b.getStore();
			var ts_rece_detail_b = Ext.getCmp('ts_rece_detail_b');
			var store1 = ts_rece_detail_b.getStore();
			for(var i=0;i<store.getCount();i++){
				var record = store.getAt(i);
				for(var j=0;j<store1.getCount();j++){
					var record1 = store1.getAt(j);
					var valuation_type = record1.get('valuation_type');
					if(valuation_type != null && valuation_type !== '' && parseInt(valuation_type)==3){//设备
						//设置单价和金额
						var price = uft.Utils.getNumberColumnValue(record1,'price');
						var num = uft.Utils.getNumberColumnValue(record,'num');
						record.beginEdit();
						record.set('price',price);
						record.set('amount',num*price);
						record.endEdit();
					}
				}
			}
		}
		//货品根据表头的客户进行过滤
		beforeEditBody = function(e){
			if(e.field == 'goods_code'){
				var pk_customer = Ext.getCmp('pk_customer').getValue();
				if(pk_customer){
					var grid = e.grid;
					var column = grid.originalColumns[e.column];
					var editor = column.editor;
					editor.addExtendParams({_cond:"pk_customer='"+pk_customer+"'"});
				}
			}
		}
		//展开跟踪明细
		function openTrackingInfo(){
			var record = uft.Utils.getSelectedRecord(app.headerGrid);
			if(record){
				var vbillno = record.data['vbillno']
				var url = ctxPath + "/inv/it/toIndex.json?vbillno="+vbillno;
				uft.Utils.openNode('','订单跟踪',url)

			}
		}

		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='openTrackingInfo()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(app.headerGrid,'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;
		}

		//打开对话框
		function openDialog(url,params){
			var wparams = "dialogWidth:"+window.screen.availWidth+"px"
				+";dialogHeight:"+window.screen.availHeight+"px"
				+";dialogLeft:0px"
				+";dialogTop:0px"
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
	

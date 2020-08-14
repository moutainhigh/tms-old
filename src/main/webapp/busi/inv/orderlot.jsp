<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="false"  headerGridPageSizePlugin="true"		bodyGridsPagination="false" bodyGridsDataUrl="loadData.json"  />	
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(new uft.extend.Button({
					text : '计算',
					scope : this,
					handler : function(){
						var app = this.app;
						var r = uft.Utils.getSelectedRecord(app.headerGrid);
						if(!r){
							uft.Utils.showWarnMsg('请先选择一行记录');
							return false;
						}
						Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('您确定要重算金额吗？'), function(btn) {
							if(btn == 'yes'){
								var lot = r.get('lot');
								var grid = Ext.getCmp('ts_orderlot_devi'),ds = grid.getStore(),count = ds.getCount();
								var invoice_vbillnoAry = [];
								for(var i=0;i<count;i++){
									var _r = ds.getAt(i);
									invoice_vbillnoAry.push(_r.get('invoice_vbillno'));
								}
								var params=app.newAjaxParams();
								params['lot']=lot;
								params['invoice_vbillnoAry'] = invoice_vbillnoAry;
								params
							    uft.Utils.doAjax({
							    	scope : this,
							    	params : params,
							    	method : 'POST',
							    	url : 'recompute.json',
							    	success : function(values){
							    		if(values && values.data){
							    			this.app.setAppValues(values.data);
							    			recompute = false;
							    		}
							    		app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
										app.statusMgr.updateStatus();
							    	}
							    });
							}
						},this);
						
					},
					enabledStatus:[uft.jf.pageStatus.OP_EDIT],
					visibleStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_EDIT]
				}));
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				recompute = false;
			},
			getAppParams : function(config){
				if(!config){
					config = {};
				}
				Ext.apply(config,{bodyGridOnlyModify:false});//保存的时候提交表体的所有数据
				return MyToolbar.superclass.getAppParams.call(this,config);
			}
		});
		
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btnEnableStatus : {'ts_orderlot_devi':{'add':false,'cop':false,'del':true},'ts_orderlot_rd':{'add':true,'cop':false,'del':true}},
			btn_row_del_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_orderlot_rd'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					 if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 //不能修改系统创建的费用明细（从合同匹配而来的记录）
						 uft.Utils.showWarnMsg('不能删除系统创建的费用明细！');
						 return false;
					 }
				}else if(grid.id == 'ts_orderlot_devi'){
					recompute = true;
				}
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
			}
		});
		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		var recompute = false;//是否要重新计算下，保存之前校验
		${moduleName}.appUiConfig.toolbar.on('beforesave',function(){
			if(recompute){
				uft.Utils.showWarnMsg('修改了发货单信息，请使用[计算]操作！');
				return false;
			}
		},this);
		
		${moduleName}.appUiConfig.bodyAssistToolbar.on('rowdel',function(grid,data){
			//删除行的时候，将同一个发货单的其他行也删除
			var ds = grid.getStore(),invoice_vbillno=data['invoice_vbillno'];
			for(var i=0;i<ds.getCount();i++){
				var _r = ds.getAt(i);
				var _v = _r.get('invoice_vbillno');
				if(_v == invoice_vbillno){
					ds.remove(_r);
				}
			}
		},this);
		
		beforeEditBody = function(e){
			var g = e.grid,r = e.record;
			if(g.id == 'ts_orderlot_rd'){
				var system_create = String(r.get('system_create'));
				if(('Y' == system_create || 'true' == system_create) && e.field != 'amount'){//系统创建的行只能修改金额
					return false;
				}
			}
		}
		
		afterEditBody = function(e){
			var g = e.grid,r = e.record,f = e.field;
			if(g.id == 'ts_orderlot_rd'){
				if(f == 'price' || f == 'weight_count' || f == 'volume_count' || f == 'num_count' || f == 'node_count' || f == 'car_num'){
					setBodyAmount(r);
				}
			}
		}
		function setBodyAmount(record){
			var valuation_type = record.get('valuation_type'); //计价方式
			var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
			if(valuation_type==null ||valuation_type==='' || !price){
				return;
			}
			var amount = 0;
			var i_valuation_type = parseInt(valuation_type);
			switch(i_valuation_type){ 
				case 0: //重量
					var weight_count = uft.Utils.getNumberColumnValue(record,'weight_count');//总计费重
					amount = weight_count*price;
					break;
				case 1: //体积
					var volume_count = uft.Utils.getNumberColumnValue(record,'volume_count'); //总体积
					amount = volume_count*price;
					break;
				case 2: //件数
					var num_count = uft.Utils.getNumberColumnValue(record,'num_count'); //总件数
					amount = num_count*price;
					break;
				case 9: //提货点
					var node_count = uft.Utils.getNumberColumnValue(record,'node_count'); //总件数
					amount = node_count*price;
					break;
				case 8: //车型吨位
					var car_num = uft.Utils.getNumberColumnValue(record,'car_num');
					amount = car_num*price;
					break;
				case 6: //节点 FIXME 后面会用到，应收明细更改到这个没有意义
					break;
			}
			uft.Utils.setColumnValue(record,'amount',amount);
		}	
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
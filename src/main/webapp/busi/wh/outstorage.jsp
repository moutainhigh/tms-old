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
			if(date < curDate && vbillstatus == 0){
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
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
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
					text : '关闭',
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
						    	url : 'close.json',
						    	success : function(values){
						    		this.app.setHeaderValues(records,values.datas);
						    		if(values.datas&&values.datas.length>0){
						    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		}
						    		this.app.statusMgr.updateStatus();
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.NEW,uft.jf.bizStatus.OUTSTO_SHIPED]
				}));
				btns.push(this.btn_export);
				return btns;
			},
			getAppParams : function(){
				//表体不能没有记录
				return MyToolbar.superclass.getAppParams.call(this,{firstBodyGridNotNull:true});
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_add_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_add_handler.call(this);
				updateHeaderSummary();
			},
			btn_row_del_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				updateHeaderSummary();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				updateHeaderSummary();
			},
			btn_row_pas_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				updateHeaderSummary();
			}
		});		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		//表头的编辑后事件
		function afterEditHead(field,value,originalValue){
			
		}
		
		//表体的编辑后事件
		function afterEditBody(e){
			var f = e.field,r = e.record;
			if(f == 'order_count'){
				afterEditOrder_count(r);
			}else if(f == 'pack_name'){
				afterEditPack_name(r);
			}else if(f == 'goods_code'){
				afterEditGoods_code(r);
			}else if(f == 'length' || f == 'width' || f == 'height'){
				afterEditLengthOrWidthOrHeight(r);
			}else if(f == 'volume'){
				afterEditVolume(r);
			}else if(f == 'weight'){
				afterEditWeight(r);
			}else if(f == 'unit_volume'){
				afterEditUnit_volume(r);
			}else if(f == 'unit_weight'){
				afterEditUnit_weight(r);
			}
		}
		
		//更新表头的包装类别的统计信息，包括总预期量、总重量、总体积
		function updateHeaderSummary(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_outstorage_b',['order_count','weight','volume'])
			uft.Utils.getField('order_count').setValue(resultMap['order_count']);
			uft.Utils.getField('weight_count').setValue(resultMap['weight']);
			uft.Utils.getField('volume_count').setValue(resultMap['volume']);
		}		
		
		//编辑件数后，计算重量和体积，更新表头的重量和体积
		function afterEditOrder_count(record){
			var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var num = uft.Utils.getNumberColumnValue(record,'order_count');
			record.beginEdit();
			uft.Utils.setColumnValue(record,'weight',num*unit_weight);
			uft.Utils.setColumnValue(record,'volume',num*unit_volume);
			record.endEdit();
			updateHeaderSummary();
		}
		//编辑包装后，如果匹配到货品，则带出包装的信息，同时需要更新统计信息
		function afterEditPack_name(record){
			var pk_goods = uft.Utils.getColumnValue(record,'pk_goods');
			var pack = uft.Utils.getColumnValue(record,'pack'); //包装字段的值
			if(pk_goods && pack){
				//读取包装单位的其他信息，从(ts_goods_pack_rela)读取
				uft.Utils.doAjax({
			    	scope : this,
			    	params : {pk_goods:pk_goods,pk_goods_packcorp:pack},
			    	isTip : false,
			    	url : ctxPath + '/inv/inv/getGoodsPackcorpInfo.json',
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
			    			var num = uft.Utils.getNumberColumnValue(record,'order_count'); //订单数量
			    			uft.Utils.setColumnValue(record,'weight',num*unit_weight);
			    			uft.Utils.setColumnValue(record,'volume',num*unit_volume);
			    			record.endEdit();
			    			updateHeaderSummary();
			    		}
			    	}
			    });
			}
		}
		// 编辑“货品”时，需要将货品的条件加入包装的参照中
		function afterEditGoods_code(record){
			var pk_goods = uft.Utils.getColumnValue(record,'pk_goods');
			var grid = Ext.getCmp('ts_outstorage_b');
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
			var num = uft.Utils.getNumberColumnValue(record,'order_count'); //订单数量
			record.beginEdit();
			uft.Utils.setColumnValue(record,'unit_volume',unit_volume); //单位体积
			uft.Utils.setColumnValue(record,'volume',unit_volume*num); //体积
			record.endEdit();
			updateHeaderSummary();
		}
		//更新体积时，更新汇总信息
		function afterEditVolume(record){
			updateHeaderSummary();
		}
		//更新重时，更新汇总信息
		function afterEditWeight(record){
			updateHeaderSummary();
		}
		//更新单位体积时，计算体积，以及汇总信息
		function afterEditUnit_volume(record){
			var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var num = uft.Utils.getNumberColumnValue(record,'order_count'); //件数
			uft.Utils.setColumnValue(record,'volume',unit_volume*num);
			updateHeaderSummary();
		}
		//更新单位重时，计算总重量，以及汇总信息
		function afterEditUnit_weight(record){
			var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var num = uft.Utils.getNumberColumnValue(record,'order_count'); //件数
			uft.Utils.setColumnValue(record,'weight',unit_weight*num);
			updateHeaderSummary();
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
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

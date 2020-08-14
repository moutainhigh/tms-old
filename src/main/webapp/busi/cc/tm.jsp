<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
			function speed_statusBeforeRenderer(value,meta,record){
				if(value == '超速'){
					meta.css = 'cssRed';
				}else if(value == '离线'){
					meta.css = 'css9';
				}else{
					meta.css = 'css0';
				}
				
			}
			function temp_statusBeforeRenderer(value,meta,record){
				meta.css = 'css'+value;
				if(value == '正常'){
					meta.css = 'css0';
				}else if(value == '高温'){
					meta.css = 'cssRed';
				}else if(value == '低温'){
					meta.css = 'cssBlue';
				}
				var highTemp = record.data.hight_temp;
				var lowTemp = record.data.low_temp;
				var temp = record.data.temp;
				if(highTemp && highTemp && temp){
					if((highTemp*1 - temp*1) < 1 || (temp*1 - lowTemp*1) < 1){
						meta.css = 'cssYellow';
					}
				}
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
			.cssBlue{
				background-color: #0000CC ;
			}
			.cssYellow{
				background-color: #FFCC22 ;
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
		<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true"  headerGridPageSizePlugin="true"		bodyGridsPagination="false,false,false" 
		bodyGridsDataUrl="loadData.json,loadData.json,refreshReceDetail.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>			
	</body>
	<script type="text/javascript">
	var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push(this.btn_export);
				btns.push(this.btn_print);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		//定时刷新下表格 2min
		setUftInterval(function(){
			var grid = app.headerGrid;
			var options={};
			options.params={};
			options.params[grid.getStore().defaultParamNames.start]=0;
			options.params[grid.getStore().defaultParamNames.limit]=grid.pageSize;
			grid.getStore().reload(options);
		},this,1000*30);
		//定时刷新数据 30s
/* 		setUftInterval(function(){
			var grid = app.headerGrid;
			if(grid){
				var store = grid.getStore();
				var count = store.getCount();
				var ids = [];
				for(var i=0;i<count;i++){
					var record = store.getAt(i);
					var id = uft.Utils.getColumnValue(record,'pk_ent_lot_track_b');
					ids.push(id);
				}
				var values = Utils.doSyncRequest(ctxPath+'/cc/tm/getAjaxData.json',{ids:ids});
				if(values && values.success && values.datas.length > 0){
					for(var i=0;i<count;i++){
						var record = store.getAt(i);
						for(var j=0;j<values.datas.length;j++){
							if(record.data.pk_ent_lot_track_b == values.datas[j].pk_ent_lot_track_b){
								this.app.setAppValues(values.datas[j],{updateToHeaderGrid:true,updateRecord:record,saveToCache:true,updateBody:false});
								break;
							}
						}
					}
					if(typeof(this.app.getBillStatusField) == 'function'){
		    			//this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
		    		}
					this.app.statusMgr.updateStatus();
				}
			}
		},this,1000*30) */
		
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

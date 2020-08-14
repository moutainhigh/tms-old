<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/jquery/ajaxfileupload.js"/>"></script>
		<%if(debug){ %>
			<script type="text/javascript" src='<c:url value="/busi/te/NodeArrivalWindow.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/BatchNodeArrivalWindow.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/Tracking.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/BatchTracking.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/Transbility.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/CurrentTracking.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/uploadTrackingAttachment.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/DeliConfirm.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/ArriConfirm.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/Operation.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/DeliWindow.js?v=${version}" />'></script>
			<script type="text/javascript" src='<c:url value="/busi/te/ArriWindow.js?v=${version}" />'></script>
		<%}else{ %>
			<script type="text/javascript" src="<c:url value="/busi/te/tracking-min.js?v=${version}" />"></script>
		<%} %>
		
		<script type="text/javascript" src='http://api.map.baidu.com/api?v=2.0&ak=EMfamWxMfh0n812MGeVBImXn'></script>
		<script type="text/javascript" src='http://api.map.baidu.com/library/LuShu/1.2/src/LuShu.js'></script>
		<script type="text/javascript">
			//不同的单据状态使用不同的颜色
			function vbillstatusBeforeRenderer(value,meta,record){
				if(value ==24){
					meta.style+='color:#fff;';
				}
				meta.css = 'css'+value;
			};
			function req_deli_dateBeforeRenderer(value,meta,record){
				if(value==undefined || value=='')
					return "";
				var date = Date.parseDate(value,"Y-m-d H:i:s");
				var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
				var curDate = new Date();
				if(date < curDate && vbillstatus <= 21){
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
				if(date < curDate && vbillstatus <= 22){
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
			function invoice_vbillnoRenderer(value,meta,record){
				var arr = value.split(','),str='';
				for(var i=0;i<arr.length;i++){
					var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
					str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
					str+="|";
				}
				return str.substring(0,str.length-1);
			}
			function vbillnoRenderer(value,meta,record){
				var url = ctxPath+"/te/ent/index.html?funCode=t501&nodeKey=view&_waterfallScene=true&_vbillno="+value;
				return "<a href=\"javascript:uft.Utils.openNode('"+value+"','委托单','"+url+"')\">"+value+"</a>";
			}
			//点击车牌号,查询gps信息
			function carno_nameRenderer(value,meta,record){
				if(value==undefined || value=='')
					return "";
				var arr = value.split(','),str='';
				for(var i=0;i<arr.length;i++){
					str+= "<a href=\"javascript:openCurrentTracking('"+arr[i]+"','')\">"+arr[i]+"</a>";
					str+=",";
				}
				return str.substring(0,str.length-1);
			}
			//点击司机,查询gps信息
			function pk_driverRenderer(value,meta,record){
				if(value==undefined || value=='')
					return "";
				var arr = value.split(','),str='';
				for(var i=0;i<arr.length;i++){
					str+= "<a href=\"javascript:openCurrentTracking('','"+arr[i]+"')\">"+arr[i]+"</a>";
					str+=",";
				}
				return str.substring(0,str.length-1);
			}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css21{
				background-color: #FAC090;
			}
			.css22{
				background-color: #FFFF00;
			}
			.css23{
				background-color: #538ED5;
			}
			.css24{
				background-color: #696969;
			}
			.cssRed{
				background-color: #FF0000;
			}
			.btnNodeArri{
				background: url(../../busi/te/images/nodeArri.png) no-repeat left 0px !important;
			}
			.btnNodeDeli{
				background: url(../../busi/te/images/nodeDeli.png) no-repeat left 0px !important;
			}
			.btnTracking{
				background: url(../../busi/te/images/tracking.png) no-repeat left 0px !important;
			}
			#secDiv{
				float: left;
				z-index: 10000;
				background: rgb(247, 249, 250);
				position: relative;
			}
			#gpsDiv1{
				-moz-border-radius-topright: 10px;
				-moz-border-radius-bottomright: 10px;
				-webkit-border-top-right-radius: 10px;
				-webkit-border-bottom-right-radius: 10px;
				float: left;
				z-index: 10000;
				position: absolute;
				left: 0px;
				bottom: 0px;
				width:230px;
				height:300px;
				opacity: 0.4;
				filter:alpha(opacity=50);
				background-color: rgb(247, 249, 250);
				opacity: 0.9;
				border:#B4D2AB 1px solid;
			}
			#gpsDiv .omBtnClosed {
				background-position: -27px -40px;
			}
			#gpsDiv .omBtn {
				background-position: -40px -40px;
			}
			.gpsDiv_om {
				height: 13px;
				width: 13px;
				position: absolute;
				cursor: pointer;
				overflow: hidden;
				background: url(../../busi/te/images/mapctrls.gif) no-repeat;
				z-index: 10001;
			}
		</style>
	</head>
	<body>
		<nw:Bill templetVO="${templetVO}" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				Ext.apply(this, config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_import.text = "导入";
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(new uft.extend.Button({
					text : '节点到货', 
					handler : this.btn_node_arrival_handler,
					iconCls : 'btnNodeArri',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '批量节点到货', 
					handler : this.btn_batch_node_arrival_handler,
					iconCls : 'btnNodeArri',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '提货', 
					handler : this.btn_deli_handler,
					iconCls : 'btnNodeDeli',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '到货', 
					handler : this.btn_arri_handler,
					iconCls : 'btnNodeArri',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '异常跟踪', 
					handler : this.btn_tracking_handler,
					iconCls : 'btnTracking',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '跟踪',
					iconCls : 'btnTracking',
					handler : this.btn_batch_tracking_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				
				btns.push(new uft.extend.Button({
					text : '提货确认',
				//	iconCls : 'btnTracking',
					handler : this.btn_deliconfirm_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				
				btns.push(new uft.extend.Button({
					text : '到货确认',
				//	iconCls : 'btnTracking',
					handler : this.btn_arriconfirm_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '运力信息', 
					iconCls : 'btnTransbility',
					handler : this.btn_transbility_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '作业反馈', 
					iconCls : 'btnWork',
					handler : this.btn_operation_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '跟踪附件', 
					iconCls : 'btnAttach',
					handler : this.btn_attachment_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				btns.push(this.btn_import);
				btns.push(this.btn_export);
				return btns;
			},
			btn_node_arrival_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var vbillno = record.get('vbillno');
					var win = new uft.te.NodeArrivalWindow({title:'对单据['+vbillno+']节点到货',currentRecord:record,pk_entrust:ids[0]}).show();
					//FIXME XUQC 如果重新加载记录，那么会出现所选的记录被清空的问题
// 					win.on('close',function(){
						//app.headerGrid.getStore().reload();//关闭窗口后，重新加载数据
// 					});
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_batch_node_arrival_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				//2014-09-18 根据运段排序下
				var segmentVbillnoAry = [],map = {};
				for(var i=0;i<records.length;i++){
					var v1 = records[i].get('segment_vbillno');
					segmentVbillnoAry.push(v1);
					map[v1] = records[i];
				}
				segmentVbillnoAry = segmentVbillnoAry.sort();
				var newRS = [];
				for(var i=0;i<segmentVbillnoAry.length;i++){
					newRS.push(map[segmentVbillnoAry[i]]);
				}
				delete segmentVbillnoAry,map;
				var win = new uft.te.BatchNodeArrivalWindow({currentRecords:newRS}).show();
				win.on('close',function(){
// 					app.headerGrid.getStore().reload();
				});
			},
			btn_deli_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var vbillno = record.get('vbillno');
					var win = new uft.te.DeliWindow({title:'提货',currentRecord:record,pk_entrust:ids[0]});
					win.show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_arri_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var vbillno = record.get('vbillno');
					var win = new uft.te.ArriWindow({title:'到货',currentRecord:record,pk_entrust:ids[0]});
					win.show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_tracking_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(records.length == 1){
					var win = new uft.te.Tracking({record:records[0]}).show();
					win.on('close',function(){
						if(app.toReload){
							app.headerGrid.getStore().reload();//关闭窗口后，重新加载数据
						}
					});
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_batch_tracking_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				var win = new uft.te.BatchTracking({records:records}).show();
				//FIXME XUQC 跟踪时，此时处于列表页，可以直接刷新
				//2015-06-06 根据是否保存来判断要不要刷新
// 				win.on('close',function(){
// 					app.headerGrid.getStore().reload();//关闭窗口后，重新加载数据
// 				});
			},
			btn_deliconfirm_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				var win = new uft.te.DeliConfirm({records:records}).show();
			},
			btn_arriconfirm_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				var win = new uft.te.ArriConfirm({records:records}).show();
			},
			btn_transbility_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(records.length == 1){
					new uft.te.Transbility({record:records[0]}).show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_operation_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(records.length == 1){
					new uft.te.Operation({record:records[0]}).show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_attachment_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				var win = new uft.te.uploadTrackingAttachment({records:records}).show();
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.ajaxLoadDefaultValue = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		app.headerGrid.getStore().on('load',function(){
			this.statusMgr.setPageStatus(this.initPageStatus);
			this.statusMgr.updateStatus();
		},app);
		
		//导出委托单信息，作为导入跟踪信息的模板
		function exportEntrust(){
			var context = app.context;
			var config = {};
			config.funCode = context.getFunCode();
			config.nodeKey = context.getNodeKey();
			config.templateID = context.getTemplateID();
			config.tabCode = context.getHeaderTabCode();
			config.isBody = false;
			config.entrust = true;//是否只导出委托单的信息
			var headerGrid = app.getHeaderGrid();
			var PUB_PARAMS = headerGrid.getStore().baseParams.PUB_PARAMS;
			if(PUB_PARAMS){
				//存在查询参数
				config.PUB_PARAMS = PUB_PARAMS;//这里不需要Ext.encode了，doExport的encodeURI已经会处理
			}
			headerGrid.doExport(config);
		}
		
		//打开跟踪地图
		function openCurrentTracking(carno,pk_driver){
			var r = uft.Utils.getSelectedRecord(app.headerGrid);
			if(r){
				var pk_entrust = r.get('pk_entrust');
				var win = Ext.getCmp('CurrentTracking');
				if(!win){
					win = new uft.te.CurrentTracking({pk_entrust:pk_entrust,carno:carno,pk_driver:pk_driver});
				}
				win.show();
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

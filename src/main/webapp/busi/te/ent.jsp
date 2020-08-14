<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/te/NodeArrivalWindow.js?v=${version}" />'></script>
		<script type="text/javascript" src='http://api.map.baidu.com/api?v=2.0&ak=EMfamWxMfh0n812MGeVBImXn'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/CurrentTracking.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/Vent.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/Authentication.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/Express.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/Receipt.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/ExpReceipt.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/uploadEntAttachment.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/te/PackRecord.js?v=${version}" />'></script>
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
			function invoice_vbillnoRenderer(value,meta,record){
				if(value==undefined || value=='')
					return "";
				var arr = value.split(','),str='';
				for(var i=0;i<arr.length;i++){
					var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
					str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
					str+=",";
				}
				return str.substring(0,str.length-1);
			}
			//点击车牌号,查询gps信息
			function carno_nameRenderer(value,meta,record){
				if(value==undefined || value=='')
					return "";
				var arr = value.split(','),str='';
				for(var i=0;i<arr.length;i++){
					str+= "<a href=\"javascript:openCurrentTracking('"+arr[i]+"')\">"+arr[i]+"</a>";
					str+=",";
				}
				return str.substring(0,str.length-1);
			}
		</script>
		<style type="text/css">
		
			.css1 {
				background-color: #92D050;
			}
			
			.css2 {
				background-color: #FAC090;
			}
			
			.css3 {
				background-color: #FFFF00;
			}
			
			.css4 {
				background-color: #538ED5;
			}
		
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
			.btnVent{
				background: url(../../busi/te/images/vent.png) no-repeat left 0px !important;
			}
			.btnUnvent{
				background: url(../../busi/te/images/unvent.png) no-repeat left 0px !important;
			}
			.btnNodeArri{
				background: url(../../busi/te/images/nodeArri.png) no-repeat left 0px !important;
			}
			.btnNodeDeli{
				background: url(../../busi/te/images/nodeDeli.png) no-repeat left 0px !important;
			}
			.btnEmail{
				background: url(../../busi/te/images/mail.png) no-repeat left 0px !important;
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
			.btnReceipt{
				background: url(../../busi/pod/images/receipt.png) no-repeat left 0px !important;
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
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="false" headerGridPageSizePlugin="true"	headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"
		bodyGridsPagination="false,false,false,false"  bodyGridsDataUrl="loadData.json,loadData.json,loadData.json,refreshPayDetail.json" 
		bodyGridsDragDropRowOrder="false,true,false,false" bodyGridsSortable="true,false,true,true"/>
		<object id="CertCtl" type="application/cert-reader" width="0" height="0"></object>
	</body>
	<script type="text/javascript">
		var simpleUnConfirm= ${templetVO.funcRegisterPropertyVO.simpleUnConfirm};
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				MyToolbar.prototype.simpleUnConfirm=simpleUnConfirm;
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_revise.text = "货量更新";
				this.btn_confirm.enabledBizStatus = [uft.jf.bizStatus.ENT_UNCONFIRM];
				this.btn_unconfirm.enabledBizStatus = [uft.jf.bizStatus.ENT_CONFIRM];
				this.btn_revise.enabledBizStatus = [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.NEW,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL];
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
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
				btns.push(new uft.extend.Button({
					text : '退单', 
					iconCls : 'btnVent',
					handler : this.btn_vent_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销退单', 
					iconCls : 'btnUnvent',
					handler : this.btn_unvent_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_VENT]
				}));
				btns.push(new uft.extend.Button({
					text : '节点到货', 
					handler : this.btn_node_arrival_handler,
					iconCls : 'btnNodeArri',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push({
					text  : '生成...',
					menu : {
						xtype : 'menu',
						items : [{
							text : '生成入库单', 
							handler : this.build_instorage_handler,
							enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
							enabledBizStatus : 'ALL'
						},{
							text : '生成出库单', 
							handler : this.build_outstorage_handler,
							enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
							enabledBizStatus : 'ALL'
						}]
					}
				});
				btns.push(new uft.extend.Button({
					text : '发送邮件', 
					handler : this.btn_sendEmail_handler,
					iconCls : 'btnEmail',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '身份证验证', 
					handler : this.btn_authentication_handler,
					//iconCls : 'btnAuthentication',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_CONFIRM,uft.jf.bizStatus.ENT_DELIVERY,uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '快递查询', 
					handler : this.btn_express_handler,
					//iconCls : 'btnAuthentication',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_DELIVERY]
				}));
				btns.push(new uft.extend.Button({
					text : '回单', 
					handler : this.btn_receipt_handler,
					iconCls : 'btnReceipt',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '异常回单', 
					handler : this.btn_expReceipt_handler,
					iconCls : 'btnReceipt',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销回单', 
					handler : this.btn_unreceipt_handler,
					iconCls : 'btnUnvent',
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.ENT_ARRIVAL]
				}));
				btns.push(this.btn_export);
				btns.push(this.btn_import);
				btns.push(new uft.extend.Button({
					text : '附件管理', 
					iconCls : 'btnAttach',
					handler : this.btn_attachment_handler,
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				btns.push(this.btn_import);
				btns.push(this.btn_print);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				return btns;
			},
			//保存后处理表体
			processBodyAfterSave : function(headerPk){
				if(this.app.hasBodyGrid()) {
					//加载所有表体的数据
					var bodyGrids = this.app.getBodyGrids();
					for(var i = 0; i < bodyGrids.length; i++) {
						if(bodyGrids[i].id != 'ts_pay_detail_b'){ //不需要重新加载费用明细
							this.app.loadBodyGrid(bodyGrids[i],headerPk);
						}else{
							refreshPayDetail();
						}
					}
				}
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var vbillno = uft.Utils.getField('vbillno');
				vbillno.setReadOnly(true);
				var invoice_vbillno = uft.Utils.getField('invoice_vbillno');
				invoice_vbillno.setReadOnly(true);
			},
			//yaojiie 2015 12 27 退单重写
			btn_vent_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				new uft.te.Vent({app:this.app,record:records}).show();
			},
			
			btn_unvent_handler : function(){
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
				    	url : 'unvent.json',
				    	success : function(values){
				    		this.app.setHeaderValues(records,values.datas);
				    		if(values.datas&&values.datas.length>0){
				    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
				    		}
				    		this.app.statusMgr.updateStatus();
				    		if(values.append){
				    			uft.Utils.showWarnMsg(values.append);
				    		}
				    	}
				    });
				}else{
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
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
					var win = new uft.te.NodeArrivalWindow({title:'对单据['+vbillno+']节点到货',currentRecord:record,pk_entrust:ids[0]});
					win.show();
					//FIXME 避免选中的记录丢失的问题
// 					win.on('close',function(){
// 						app.headerGrid.getStore().reload();//关闭窗口后，重新加载数据
// 					});
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_sendEmail_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					//如果没有勾选任何单据，我们默认是选择所有的单据 调用loadData()
				}
				var params = this.app.newAjaxParams();
				params['ids']=ids;
				 uft.Utils.doAjax({
				    	scope : this,
				    	params : {ids:ids},
				    	url : 'sendEntEmail.json',
				    	success : function(values){
				    		if(values){
				    			//提示
					    		uft.Utils.showInfoMsg(values);
				    		}else{
				    			uft.Utils.showInfoMsg('邮件发送成功！');
				    			this.app.headerGrid.getStore().reload();
				    		}
				    	}
				    });	
			}, 
			btn_authentication_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var lot = record.get('lot');
					
					var CertCtl = document.getElementById("CertCtl");
					CertCtl.connect();
					var result = CertCtl.readCert();
					CertCtl.disconnect();
					result = eval('('+result+')');  
					if(result.resultFlag == -1){
						uft.Utils.showWarnMsg("没有读取到身份证信息！");
						return;
					}
					var retMap = Utils.doSyncRequest('authentication.json',{'lot':lot,'card_msg':JSON.stringify(result.resultContent)});
					new uft.te.Authentication({app:this.app,record:record,retMap:retMap}).show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_receipt_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				new uft.te.Receipt({app:this.app,records:records}).show();
			},
			btn_expReceipt_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
					var vbillno = record.get('vbillno');
					new uft.te.ExpReceipt({app:this.app,record:record}).show();
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			btn_unreceipt_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('没有选择单据！');
					return;
				}
				var params=this.app.newAjaxParams(),vbillnos=[];
				for(var i=0;i<records.length;i++){
					vbillnos.push(records[i].data['vbillno']);
				}
				params["vbillnos"]=vbillnos;
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	isTip : true,
			    	method : 'GET',
			    	url : 'unreceipt.json',
			    	success : function(values){
			    		if(values && values.datas){//保存成功直接销毁窗口
		    				for(var i=0;i<values.datas.length;i++){
			    				this.app.setAppValues(values.datas[i],{updateToHeaderGrid:true,updateRecord:records[i],saveToCache:true});
		    				}
		    				if(typeof(this.app.getBillStatusField) == 'function'){
				    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
				    		}
			    			this.app.statusMgr.updateStatus();
			    		}
			    	}
			    });
				
				
				
			},
			btn_express_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(ids.length > 0){
					var params=this.app.newAjaxParams();
					params["ids"]=ids;
				    uft.Utils.doAjax({
				    	scope : this,
				    	params : {ids:ids},
				    	isTip : true,
				    	method : 'GET',
				    	url : 'syncExpress.json',
				    	success : function(values){
				    		if(values){
				    			new uft.te.Express({app:this.app,values:values}).show();
				    		}else{
				    			uft.Utils.showWarnMsg('没有返回信息！');
								return;
				    		}
				    	}
				    });
				}else{
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				
			},
			btn_attachment_handler : function(){
				var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				var win = new uft.te.uploadEntAttachment({records:records}).show();
			},
			build_instorage_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var params=this.app.newAjaxParams();
					params[this.app.getBillIdField()]=ids[0];
				    uft.Utils.doAjax({
				    	scope : this,
				    	params : params,
				    	isTip : true,
				    	method : 'GET',
				    	url : 'buildInstorage.json',
				    	success : function(values){
				    		
				    	}
				    });
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			build_outstorage_handler : function(){
				var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
				if(!ids || ids.length == 0){
					uft.Utils.showWarnMsg('请先选择记录！');
					return;
				}
				if(ids.length == 1){
					var params=this.app.newAjaxParams();
					params[this.app.getBillIdField()]=ids[0];
				    uft.Utils.doAjax({
				    	scope : this,
				    	params : params,
				    	isTip : true,
				    	method : 'GET',
				    	url : 'buildOutstorage.json',
				    	success : function(values){
				    		
				    	}
				    });
				}else{
					uft.Utils.showWarnMsg('有且只能选择一条记录！');
					return;
				}
			},
			getAppParams : function(config){
				//路线信息，传入所有行记录，保存时需要使用，避免重新查询一次
				return MyToolbar.superclass.getAppParams.call(this,{bodyGridOnlyModify:[true,false,true,true]});
			}
		});
		//辅助工具栏
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_add_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_add_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_del_handler : function(){
				var tp = this.app.bodyTabPanel;
				var grid = tp.getActiveTab();
				if(grid.id == 'ts_ent_line_b'){//路线tab
					var r = uft.Utils.getSelectedRecord(grid);
					var segment_node = r.get('segment_node');
					if(segment_node == 'Y' || String(segment_node)=='true'){//运段的节点
						uft.Utils.showWarnMsg('运段本身的节点不能删除！');
						return;
					}
				}				
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_pas_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				if(grid.id == 'ts_pay_detail_b'){
					var record = grid.getSelectedRow();
					var system_create = record.get('system_create');
					if(String(system_create) == 'true' || String(system_create) == 'Y'){
						 //不能修改系统创建的费用明细（从合同匹配而来的记录）
						 uft.Utils.showWarnMsg('系统费用明细不允许复制！');
						 return false;
					 }
				}
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			getRowPasDefaultValue : function(gridId,selectRecordValue){
				var value = MyBodyAssistToolbar.superclass.getRowPasDefaultValue.call(this,gridId,selectRecordValue);
				value['segment_node'] = null;
				value['addr_flag'] = null;
				value['pk_segment'] = null;
				value['serialno'] = null;
				return value;
			}
		});				
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		${moduleName}.appUiConfig.ajaxLoadDefaultValue = false;//不需要去读取默认值
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		${moduleName}.appUiConfig.toolbar.on('beforerevise',function(toolbar,record){
			//校验是否能够进行修订
			var billId = record.get('pk_entrust');
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
		
		app.statusMgr.addAfterUpdateCallback(function(){
			var tp = app.bodyTabPanel;
			var grid = tp.getActiveTab();
			if(grid && grid.id == 'ts_ent_pack_b'){
				//app.bodyAssistToolbar.setDisabled(true);
			}
		},this);
		var tabPanel = app.getBodyTabPanel();
		tabPanel.addListener('tabchange',function(tabPanel,ap){
			if(ap.id=='ts_ent_pack_b'){ //货物信息tab
				//app.bodyAssistToolbar.setDisabled(true);
			}else{
				var status = app.statusMgr.getCurrentPageStatus();
				if(status == uft.jf.pageStatus.OP_EDIT){
					app.bodyAssistToolbar.setDisabled(false);
				}else{
					app.bodyAssistToolbar.setDisabled(true);
				}
			}
		});		
		
		${moduleName}.appUiConfig.toolbar.on({'beforesave':function(toolbar,params){
				//保存之前校验
				return checkEntLineB();
		},scope:this});
		
		//当切换到作业指令tab时，禁用辅助工具栏
		var tabPanel = app.getBodyTabPanel();
		tabPanel.addListener('tabchange',function(tabPanel,activePanel){
			if(activePanel.id=='ts_ent_operation_b'){ //作业指令tab
				app.bodyAssistToolbar.setDisabled(true);
			}else{
				app.bodyAssistToolbar.setDisabled(false);
			}
		});
		
		//编辑件数后，计算重量和体积，更新表头的重量和体积
		function afterEditNum(record){
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
		//更新表头的包装类别的统计信息，包括总件数、总重量、总体积
		function updateHeaderPackSummary(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_ent_pack_b',['pack_num_count','num','weight','volume']);
			uft.Utils.getField('pack_num_count').setValue(resultMap['pack_num_count']);
			uft.Utils.getField('num_count').setValue(resultMap['num']);
			uft.Utils.getField('weight_count').setValue(resultMap['weight']);
			uft.Utils.getField('volume_count').setValue(resultMap['volume']);
			updateHeaderFeeWeightCount(); //更新总计费重
		}
		//更新表头的总金额
		function updateHeaderCostAmount(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_pay_detail_b','amount');//费用明细的金额
			uft.Utils.getField('cost_amount').setValue(resultMap['amount']);
		}
		//编辑计价方式、报价类型、单价时，更新金额，及表头的总金额
		function afterEditQuoteTypeOrValuationTypeOrPrice(record,updateHeader){
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
							break;
						case 4: //吨公里
							var weight_count = uft.Utils.getNumberFieldValue('weight_count');//总重量
							var distance = uft.Utils.getField('distance').getValue();//区间距离
							amount = (weight_count/1000)*distance*price;
							break;
						case 6: //节点 FIXME 后面会用到
							break;
					}
					uft.Utils.setColumnValue(record,'amount',amount);
				}
			}else{
				uft.Utils.setColumnValue(record,'price',null); //将单价置为空
			}
			if(updateHeader !== false){
				//更新表头的总金额
				updateHeaderCostAmount();
			}
		}
		//更新了表头的汇总信息后，需要更新费用明细的金额信息
		function updateCostDetailAmount(){
			var grid = Ext.getCmp('ts_pay_detail_b');
			if(grid){
				var store = grid.getStore(), count = store.getCount();
				for(var i=0;i<count;i++){
					var record = store.getAt(i);
					afterEditQuoteTypeOrValuationTypeOrPrice(record,false); //更新每行的金额
				}
			}
		}		
		var transFeeCode = 'ET10';
		
		//打开跟踪地图
		function openCurrentTracking(carno){
			var r = uft.Utils.getSelectedRecord(app.headerGrid);
			if(r){
				var pk_entrust = r.get('pk_entrust');
				var win = Ext.getCmp('CurrentTracking');
				if(!win){
					win = new uft.te.CurrentTracking({pk_entrust:pk_entrust,carno:carno});
				}
				win.show();
			}
		}
		
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='expandPackRecord()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(app.headerGrid,'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;	
		}
		
		//展开委托单包装修改记录
		function expandPackRecord(){
			var record = uft.Utils.getSelectedRecord(app.headerGrid);
			var pk_entrust = record.get('pk_entrust')
			if(record){
				new uft.te.PackRecord({pk_entrust:pk_entrust,grid:app.headerGrid}).show();
			}
		}
	</script>
	<script type="text/javascript" src='<c:url value="/busi/te/EntrustEdit.js?v=${version}" />'></script>
	<%@ include file="/common/footer.jsp"%>
</html>
	

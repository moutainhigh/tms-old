<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<%if(debug){ %>
		<script type="text/javascript" src='<c:url value="/busi/pod/POD.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/PODExp.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/Receipt.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/ReceiptExp.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/PodAttach.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/uploadSignBillAttachment.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/uploadReceiptBillAttachment.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/uploadAttachment.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/pod/uploader.js?v=${version}" />'></script>
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js?v=${version}"/>"></script>
		 <!-- 引入额外JS用来支持附件批量上传 -->
		<script src="//cdn.staticfile.org/webuploader/0.1.0/Uploader.swf"></script>
		<script type="text/javascript" src="//cdn.staticfile.org/webuploader/0.1.0/webuploader.min.js"></script>
		<link href="<c:url value="/busi/pod/uploader/webuploader.css"/>" rel="stylesheet" type="text/css"/>
		<link href="<c:url value="/busi/pod/uploader/loader.css"/>" rel="stylesheet" type="text/css"/>
		
		
		
		<%}else{ %>
		<script type="text/javascript" src='<c:url value="/busi/pod/pod-min.js?v=${version}" />'></script>	
		<%} %>
		<script type="text/javascript" src='<c:url value="/busi/te/ExpAccident.js?v=${version}" />'></script>

		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==4 || value == 6){
				meta.style+='color:#fff;';
			}
		}
		//当签收异常时，背景使用红色
		function pod_expBeforeRenderer(v, meta, record){
			if(v && (v == 1 || v == 'Y' || v == 'true')){
				meta.css = 'cssRed';
			}
			return v;
		}
		//当回单异常时，背景使用红色
		function receipt_expBeforeRenderer(v, meta, record){
			if(v && (v == 1 || v == 'Y' || v == 'true')){
				meta.css = 'cssRed';
			}
			return v;
		}	
		function vbillnoRenderer(value,meta,record){
			var arr = value.split('|'),str='';
			for(var i=0;i<arr.length;i++){
				var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
				str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
				str+="|";
			}
			return str.substring(0,str.length-1);
		}
		</script>
		<style type="text/css">
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
			.btnExpPod{
				background: url(../../busi/pod/images/expPod.png) no-repeat left 0px !important;
			}
			.btnExpReceipt{
				background: url(../../busi/pod/images/expReceipt.png) no-repeat left 0px !important;
			}
			.btnPod{
				background: url(../../busi/pod/images/pod.png) no-repeat left 0px !important;
			}
			.btnReceiptAttcah{
				background: url(../../busi/pod/images/receipt_attcah.png) no-repeat left 0px !important;
			}
			.btnReceipt{
				background: url(../../busi/pod/images/receipt.png) no-repeat left 0px !important;
			}
			.btnVent{
				background: url(../../busi/te/images/vent.png) no-repeat left 0px !important;
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
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push({
					xtype : 'button',
					text : '签收',
					iconCls : 'btnPod',
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							new uft.pod.POD({app:this.app,records:records}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_ARRIVAL]
				});
				btns.push({
					xtype : 'button',
					text : '异常签收',
					iconCls : 'btnExpPod',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.pod.PODExp({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_ARRIVAL]
				});
				btns.push({
					xtype : 'button',
					text : '撤销签收',
					iconCls : 'btnVent',
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,'pk_invoice');
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定要撤销吗?'), function(btn) {
								if(btn == 'yes') {
									var params = this.app.newAjaxParams();
									params['pk_invoice'] = ids;
									uft.Utils.doAjax({
								    	scope : this,
								    	params : params,
								    	isTip : false,
								    	url : 'unpod.json',
								    	success : function(values){
								    		if(values && values.datas){
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
								}
							},this);
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_SIGN]
				});
				btns.push({
					xtype : 'button',
					text : '回单',
					iconCls : 'btnReceipt',
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							if(this.recordCheck()){
								var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
								new uft.pod.Receipt({app:this.app,records:records}).show();
							}
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_SIGN]
				});
				btns.push({
					xtype : 'button',
					text : '异常回单',
					iconCls : 'btnExpReceipt',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.pod.ReceiptExp({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_SIGN]
				});
				btns.push({
					xtype : 'button',
					text : '撤销回单',
					iconCls : 'btnVent',
					scope : this,
					handler : function(){
						if(this.recordCheck()){
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,'pk_invoice');
							var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
							Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定要撤销吗?'), function(btn) {
								if(btn == 'yes') {
									var params = this.app.newAjaxParams();
									params['pk_invoice'] = ids;
									uft.Utils.doAjax({
								    	scope : this,
								    	params : params,
								    	isTip : false,
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
								}
							},this);
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_BACK]
				});
				/* btns.push({
					xtype : 'button',
					text : '上传POD签收单',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.pod.PodAttach({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_ARRIVAL,uft.jf.bizStatus.INV_SIGN,uft.jf.bizStatus.INV_BACK]
				}); */
				
				btns.push({
					xtype : 'button',
					text : '签单附件',
					iconCls : 'btnAttach',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.pod.uploadSignBillAttachment({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_ARRIVAL,uft.jf.bizStatus.INV_SIGN,uft.jf.bizStatus.INV_BACK]
				});
				
				btns.push({
					xtype : 'button',
					text : '回单附件',
					iconCls : 'btnReceiptAttcah',
					scope : this,
					handler : function(){
						if(this.recordCheck(true)){
							var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
							new uft.pod.uploadReceiptBillAttachment({app:this.app,record:record}).show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus : [uft.jf.bizStatus.INV_ARRIVAL,uft.jf.bizStatus.INV_SIGN,uft.jf.bizStatus.INV_BACK]
				});
				
				btns.push({
					xtype : 'button',
					text : '回单智能上传',
					id : 'upload',
					scope : this,
					handler : function(){
						new uft.pod.uploader({app:this.app}).show();
					},
					
				});
			

				btns.push(this.btn_export);
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
			
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
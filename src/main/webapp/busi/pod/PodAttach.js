Ext.ns('uft.pod');
/**
 * 上传POD签收单
 * @param {} config
 */
uft.pod.PodAttach = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择一条记录！');
		return false;
	}
	var values = Utils.doSyncRequest(ctxPath+'/pod/at/getTempletMap.json',{funCode:'t602',tabCode:'ts_pod_attach'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode：t602,tabcode:ts_pod_attach！');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t602,tabcode:ts_pod_attach！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	params['isBody'] = 'false';
	params['pk_invoice'] = this.record.data['pk_invoice'];
	this.grid = new uft.extend.grid.BasicGrid({
		id : 'ts_pod_attach',
		pkFieldName : 'pk_pod_attach',
		dataUrl:ctxPath+'/pod/at/loadData.json',
		immediatelyLoad : true,
		singleSelect : false,
		isCheckboxSelectionModel : true,
		isAddBbar : false,
		params : params,
		recordType : R,
		columns : C
	});
	var uploadUrl = ctxPath+'/pod/at/uploadPod.do?pk_invoice='+this.record.data['pk_invoice'];
	var toolbar = new Ext.Toolbar({
		defaults : {
			xtype : 'button'
		},
		items : [{
			text : '上传',
			iconCls : 'btnUpload',
			scope : this,
			handler : function(){
				//允许的文件类型
				var permitted_extensions = ['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png'];
				var fileUpload = new uft.jf.FileUpload({uploadUrl:uploadUrl,permitted_extensions:permitted_extensions}).show();
				fileUpload.on('fileupload',function(uploadField){
					this.grid.getStore().reload();
					this.fireEvent('upload',this); //这里抛出文件上传成功后的事件
				},this);
			}
		},{
			text : '查看',
			iconCls : 'btnDownload',
			scope : this,
			handler : function(){
				var records = uft.Utils.getSelectedRecords(this.grid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！');
					return false;
				}
				if(records.length > 1){
					uft.Utils.showWarnMsg('只能选择一条记录进行查看！');
					return false;
				}
				window.location.href=ctxPath+"/pod/at/download.do?pk_pod_attach="+records[0].data['pk_pod_attach'];
//				window.open(ctxPath+"/pod/at/download.do?pk_pod_attach="+records[0].data['pk_pod_attach']);
			}
		},{
			text : '批量下载',
			scope : this,
			handler : function(){
				var records = uft.Utils.getSelectedRecords(this.grid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！');
					return false;
				}
				var idStr = '';
				for(var i=0;i<records.length;i++){
					idStr += records[i].data['pk_pod_attach'];
					if(i != records.length-1){
						idStr += ',';
					}
				}
				window.location.href=ctxPath+"/pod/at/zipDownload.do?pk_pod_attach="+idStr;
//				window.open(ctxPath+"/pod/at/zipDownload.do?pk_pod_attach="+idStr);
			}
		},{
			text : '删除',
			iconCls : 'btnDel',
			scope : this,
			handler : function(){
				var records = uft.Utils.getSelectedRecords(this.grid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！');
					return false;
				}
				var ids = [];
				for(var i=0;i<records.length;i++){
					ids.push(records[i].data['pk_pod_attach']);
				}
				Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('您确定要删除所选记录吗？'), function(btn) {
					if (btn == 'yes') {
						var params = {};
						params['billId'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'GET',
					    	url : ctxPath+'/pod/at/delete.json',
					    	actionType : '删除',
					    	params : params,
					    	success : function(values){
					    		for(var i=0;i<records.length;i++){
					    			this.grid.getStore().remove(records[i]);
					    		}
					    	}
					    });
					}
				},this);
			}
		},{
			text : '刷新',
			iconCls : 'btnRef',
			scope : this,
			handler : function(){
				this.grid.getStore().reload();
			}
		},{
			text : '预览',
			iconCls : 'btnQuery',
			scope : this,
			handler : function(){
				var records = uft.Utils.getSelectedRecords(this.grid);
				if(!records || records.length == 0){
					uft.Utils.showWarnMsg('请选择记录！');
					return false;
				}
				if(records.length > 1){
					uft.Utils.showWarnMsg('只能选择一条记录进行查看！');
					return false;
				}
				window.open(ctxPath+'/pod/at/preview.do?pk_pod_attach='+records[0].data['pk_pod_attach']);
			}
		}]
	});
	uft.pod.PodAttach.superclass.constructor.call(this, {
		title : '上传POD签收单',
		width : 800,
		height : 500,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		tbar : toolbar,
		items : [this.grid],
		buttons : [{
			iconCls : 'btnCancel',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}]
    });	
};
Ext.extend(uft.pod.PodAttach,Ext.Window, {
});

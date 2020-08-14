Ext.ns('uft.cm');
uft.cm.uploadRDAttachment = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择一条记录！');
		return false;
	}
	var values = Utils.doSyncRequest(ctxPath+'/attach/attach/getTempletMap.json',{funCode:'t4021',tabCode:'ts_attachment'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode：t4021,tabcode:ts_attachment!');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t4021,tabcode:ts_attachment！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	params['isBody'] = 'false';
	params['pk_receive_detail'] = this.record.data['pk_receive_detail'];
	this.grid = new uft.extend.grid.BasicGrid({
		id : 'ts_attachment',
		pkFieldName : 'pk_attachment',
		dataUrl:ctxPath+'/attach/attach/loadData.json',
		immediatelyLoad : true,
		singleSelect : false,
		isCheckboxSelectionModel : true,
		isAddBbar : false,
		params : params,
		recordType : R,
		columns : C
	});
	var uploadUrl = ctxPath+'/attach/attach/uploadAttachment.do?pk_receive_detail='+this.record.data['pk_receive_detail']+'&funCode=t4021';
	var toolbar = new Ext.Toolbar({
		defaults : {
			xtype : 'button'
		},
		items : [{
			text : '上传',
			iconCls : 'btnUpload',
			scope : this,
			params : values,
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
				window.location.href=ctxPath+"/attach/attach/downloadAttachment.do?pk_attachment="+records[0].data['pk_attachment'];
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
					idStr += records[i].data['pk_attachment'];
					if(i != records.length-1){
						idStr += ',';
					}
				}
				window.location.href=ctxPath+"/attach/attach/zipAttachmentDownload.do?pk_attachment="+idStr;
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
					ids.push(records[i].data['pk_attachment']);
				}
				Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('您确定要删除所选记录吗？'), function(btn) {
					if (btn == 'yes') {
						var params = {};
						params['billId'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'GET',
					    	url : ctxPath+'/attach/attach/deleteAttachment.json',
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
				window.open(ctxPath+'/attach/attach/previewAttachment.do?pk_attachment='+records[0].data['pk_attachment']);
			}
		}]
	});
	uft.cm.uploadRDAttachment.superclass.constructor.call(this, {
		title : '附件管理',
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
Ext.extend(uft.cm.uploadRDAttachment,Ext.Window, {
});

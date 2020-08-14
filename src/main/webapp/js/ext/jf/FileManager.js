Ext.ns('uft.jf');
/**
 * 上传POD签收单
 * @param {} config
 */
uft.jf.FileManager = function(config){
	Ext.apply(this,config);
	if(!config.billtype || !config.pk_bill){
		uft.Utils.showErrorMsg('单据类型和单据PK不能为空！');
		return false;
	}
	var b = this.checkBillStatus(false);
    if(!b){
    	//非新增状态下，只能查看附件，不能删除
    	this.viewOnly = true;
    }
	
	var values = Utils.doSyncRequest(ctxPath+'/attach/attach/getTempletMap.json',{funCode:'t028',tabCode:'ts_attachment'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode：t028,tabcode:ts_attachment！');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t028,tabcode:ts_attachment！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	params['isBody'] = 'false';
	params['billtype'] = config.billtype;
	params['pk_bill'] = config.pk_bill;
	params['funCode'] = config.funCode;
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
	var uploadUrl =ctxPath+'/attach/attach/uploadAttachment.do?funCode='+config.funCode+'&pk_bill='+config.pk_bill+'&billtype='+config.billtype;
	var items = [];
	if(this.viewOnly !== true){
		items.push({
			text : '上传',
			iconCls : 'btnUpload',
			scope : this,
			handler : function(){
				if(this.checkBillStatus() !== false){
					//允许的文件类型
					var permitted_extensions = ['doc','docx','xls','xlsx','ppt','pptx','pdf','jpg','jpeg','png'];
					var fileUpload = new uft.jf.FileUpload({uploadUrl:uploadUrl,permitted_extensions:permitted_extensions}).show();
					fileUpload.on('fileupload',function(uploadField){
						this.grid.getStore().reload();
						this.fireEvent('upload',this); //这里抛出文件上传成功后的事件
					},this);
				}
			}
		});		
	}
	items.push({
			text : '下载',
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
		});
	items.push({
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
		});
	if(this.viewOnly !== true){
		items.push({
			text : '删除',
			iconCls : 'btnDel',
			scope : this,
			handler : function(){
				if(this.checkBillStatus() !== false){
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
			}
		});
	}
	items.push({
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
		});
	
	var toolbar = new Ext.Toolbar({
		defaults : {
			xtype : 'button'
		},
		items : items
	});
	uft.jf.FileManager.superclass.constructor.call(this, {
		title : '附件管理',
		width : 650,
		height : 350,
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
				this.close();
			}
		}]
    });	
};
Ext.extend(uft.jf.FileManager,Ext.Window, {
	//检测当前的单据状态，一般如果不是新建状态，那么不能上传和删除附件
	checkBillStatus : function(showMsg){
		if(!this.isCheckBillStatus){//如果设置了不检测状态，那么任何状态都能上传附件
			return true;
		}
		if(!this.app || !this.app.statusMgr){
			return false;
		}
		var currentBillStatus = this.app.statusMgr.getCurrentBizStatus();
		if(currentBillStatus != null && currentBillStatus != uft.jf.bizStatus.NEW){
			if(showMsg !== false){
				uft.Utils.showWarnMsg("只有[新增]状态的单据才能添加或删除附件！");
			}
			return false;
		}
		return true;
	}
});

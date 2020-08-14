Ext.ns('uft.cm');
/**
 * 反确认
 * @param {} config
 */
uft.cm.UnConfirm = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要反确认的记录！');
		return false;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 60,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:2},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield'
				},      
				items : [{
					name : 'unconfirm_type',
					fieldLabel : '撤销类型',
					value : '',
					xtype : 'uftcombo',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					dataUrl : 'getUnConfirmTypeList.json',
					colspan : 1
				},{
					name : 'unconfirm_memo',
					fieldLabel : '撤销说明',
					colspan : 2
				}]
			}]
	});
	uft.cm.UnConfirm.superclass.constructor.call(this, {
		title : '撤销确认',
		width : 500,
		height : 300,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		items : [this.formPanel],
		buttons : [{
			iconCls : 'btnYes',
			text : '确&nbsp;&nbsp;认',
			actiontype : 'submit',
			scope : this,
			handler : this.saveAction
		},{
			iconCls : 'btnCancel',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}]
    });	
};
Ext.extend(uft.cm.UnConfirm,Ext.Window, {
	saveAction : function(){
			var form = this.formPanel.getForm();
			var errors = this.formPanel.getErrors();
			if(errors.length==0) {
			var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
			if(ids.length > 0){
				if(this.fireEvent('beforeunconfirm',this,ids) !== false){
					var params=this.app.newAjaxParams();
					var values = form.getFieldValues(false);
					params[this.app.getBillIdField()]=ids;
					params[uft.jf.Constants.HEADER] = Ext.encode(values);
				    uft.Utils.doAjax({
				    	scope : this,
				    	params : params,
				    	isTip : true,
				    	url : 'batchUnconfirm.json',
				    	success : function(values){
				    		this.app.setHeaderValues(this.record,values.datas);
				    		if(values.datas&&values.datas.length>0){
				    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
				    		}
				    		this.app.statusMgr.updateStatus();
				    		if(values.append){
				    			uft.Utils.showWarnMsg(values.append);
				    		}
				    		this.fireEvent('unconfirm',this,values.datas,values);
				    		this.destroy();
				    		}
				    	});
					}
				}
			}else{
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}
	}
});

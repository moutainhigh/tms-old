Ext.ns('uft.vm');
uft.vm.vmcheck = function(config){
	Ext.apply(this,config);
	if(!this.records){
		uft.Utils.showErrorMsg('请先选择要审核的记录！');
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
					xtype : 'uftcombo',
					name : 'choice',
					fieldLabel : '是否同意',
					value : '',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					dataUrl : ctxPath+'/fleet/vm/getChoice.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'reason_type',
					fieldLabel : '拒绝原因',
					dataUrl : ctxPath+'/fleet/vm/getReasonType.json',
					colspan : 1
				},{
					name : 'memo',
					fieldLabel : '备注',
					colspan : 2
				}]
			}]
	});

	uft.vm.vmcheck.superclass.constructor.call(this, {
		title : '审核',
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
			text : '保&nbsp;&nbsp;存',
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
Ext.extend(uft.vm.vmcheck,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		Ext.apply(values,{ids:ids});
		var params = app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'vmcheck.json',
	    	params : params,
	    	success : function(values){
	    		app.headerGrid.getStore().reload();
	    		this.close();
	    	}
		});
	}
});


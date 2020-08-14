Ext.ns('uft.te');
uft.te.DeliConfirm = function(config){
	Ext.apply(this,config);
	if(!this.records){
		uft.Utils.showErrorMsg('请先选择要确认的记录！');
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
					xtype : 'datetimefield',
					name : 'confirm_date',
					fieldLabel : '确认日期',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					value : DateUtils.formatDate(new Date(),'yyyy-MM-dd H:m:s'),
					newlineflag : true,
					colspan : 1
				},{
					name : 'confirm_memo',
					fieldLabel : '确认信息',
					colspan : 2
				}]
			}]
	});
	uft.te.DeliConfirm.superclass.constructor.call(this, {
		title : '提货确认',
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
Ext.extend(uft.te.DeliConfirm,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		
		var ids = [],rs = this.records;
		for(var i=0;i<rs.length;i++){
			ids.push(rs[i].get('pk_entrust'));//委托单号
		}
		Ext.apply(values,{ids:ids});
		var params = app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'deliConfirm.json',
	    	params : params,
	    	success : function(values){
	    		app.headerGrid.getStore().reload();
	    		this.close();
	    	}
		});
	}
});

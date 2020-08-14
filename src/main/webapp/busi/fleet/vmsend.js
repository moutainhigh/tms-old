Ext.ns('uft.vm');
uft.vm.vmsend = function(config){
	Ext.apply(this,config);
	if(!this.records){
		uft.Utils.showErrorMsg('请先选择要派车的记录！');
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
					name : 'carno',
					fieldLabel : '车牌号',
					editable:true, 
					value : '',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					dataUrl : ctxPath+'/fleet/vm/getCarno.json',
					colspan : 1
				},
				{
					xtype : 'uftcombo',
					name : 'main_driver',
					fieldLabel : '主驾驶',
					editable:true, 
					value : '',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					dataUrl : ctxPath+'/fleet/vm/getDriver.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'deputy_drive',
					fieldLabel : '副驾驶',
					editable:true, 
					value : '',
					dataUrl : ctxPath+'/fleet/vm/getDriver.json',
					colspan : 1
				},{
					name : 'memo',
					fieldLabel : '备注',
					colspan : 2
				}]
			}]
	});

	uft.vm.vmsend.superclass.constructor.call(this, {
		title : '派车',
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
	Ext.extend(uft.vm.vmsend,Ext.Window, {
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
		    	url : 'vmsend.json',
		    	params : params,
		    	success : function(values){
		    		app.headerGrid.getStore().reload();
		    		this.close();
		    	}
			});
		}
	});
}